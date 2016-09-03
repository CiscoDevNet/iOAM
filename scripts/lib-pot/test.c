/*
 * Copyright (c) 2016 Cisco and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <stdbool.h>
#include <stdint.h>
#include <string.h>
#include "pot_util.h"

#define MAX_STR_LEN 300
#define MAX_SERVICES 101

pot_profile profile[MAX_SERVICES];

/* return the current arg, and advance the buff to the next space-separated word */
char *
get_next_arg (char **pbuff)
{
  char *pc = NULL;
  while ((*pbuff) && (**pbuff) && ((**pbuff == ' ') || (**pbuff == '\n')))
    {
      **pbuff = 0;
      (*pbuff)++;
    }
  pc = *pbuff;

  while ((*pbuff) && (**pbuff) && ((**pbuff != ' ') && (**pbuff != '\n')))
    {
      (*pbuff)++;
    }

  while ((*pbuff) && (**pbuff) && ((**pbuff == ' ') || (**pbuff == '\n')))
    {
      **pbuff = 0;
      (*pbuff)++;
    }

  if ((pc) && (0 == *pc))
    {
      pc = NULL;
    }
  return pc;
}

static int
strtou16 (char *num, int base, u16 * result)
{
  *result = (u16) strtol (num, NULL, base);
  return (0);
}

static int
strtou64 (char *num, int base, u64 * result)
{
  *result = (u64) strtoll (num, NULL, base);
  return (0);
}

static void
pot_config_parse (char *buf, int profile_no)
{
  u64 prime;
  u64 secret_share;
  u64 secret_key;
  u16 my_service_index = 0;
  char *buff;
  char *arg_name;
  u16 mark = 0;
  char *num = NULL;
  bool validator = false;
  int i;
  u16 id;
  u16 bits;
  u64 lpc;
  u64 poly2;

  buff = buf;
  arg_name = NULL;


  while (NULL != (arg_name = get_next_arg (&buff)))
    {
      num = NULL;
      if (0 == strcmp (arg_name, "id"))
	{
	  num = get_next_arg (&buff);
	  id = 0;
	  if (num && (0 == strtou16 (num, 0, &id)))
	    {
	    }
	}
      else if (0 == strcmp (arg_name, "validate-key"))
	{
	  num = get_next_arg (&buff);
	  if (num && (0 == strtou64 (num, 0, (u64 *) & secret_key)))
	    {
	      validator = true;
	    }
	}
      else if (0 == strcmp (arg_name, "lpc"))
	{
	  num = get_next_arg (&buff);
	  if (num && (0 == strtou64 (num, 0, (u64 *) & lpc)))
	    {
	    }
	}
      else if (0 == strcmp (arg_name, "polynomial2"))
	{
	  num = get_next_arg (&buff);
	  if (num && (0 == strtou64 (num, 0, (u64 *) & poly2)))
	    {
	    }
	}
      else if (0 == strcmp (arg_name, "prime-number"))
	{
	  num = get_next_arg (&buff);
	  if (num && (0 == strtou64 (num, 0, (u64 *) & prime)))
	    {
	    }
	}
      else if (0 == strcmp (arg_name, "secret_share"))
	{
	  num = get_next_arg (&buff);
	  if (num && (0 == strtou64 (num, 0, (u64 *) & secret_share)))
	    {
	    }
	}
      else if (0 == strcmp (arg_name, "bits-in-random"))
	{
	  num = get_next_arg (&buff);
	  bits = MAX_BITS;
	  if (num && (0 == strtou16 (num, 0, &bits)))
	    {
	      if (bits > MAX_BITS)
		{
		  bits = MAX_BITS;
		}
	    }
	}
      else
	{
	  printf ("Skipping \"%s\"\n", arg_name);
	}

    }
  pot_profile_create (&profile[profile_no], prime, poly2, lpc, secret_share);
  if (validator)
    pot_set_validator (&profile[profile_no], secret_key);
  pot_profile_set_bit_mask (&profile[profile_no], bits);
}

#define BUFSIZE 4000

int
main (int argc, char *argv[])
{
  FILE *fp = NULL;
  char buff[BUFSIZE];
  u64 random = 0;
  u64 cumulative = 0;
  int count = 0;
  int i;
  char *cmd = "set pot profile";
  char *tail = buff;
  fp = fopen (argv[1], "r");
  pot_util_init ();
  while (fp && fgets (buff, BUFSIZE - 1, fp) != NULL)
    {
      tail = buff;
      if (0 != (tail = strstr (tail, cmd)))
	{
	  /*
	   * set pot profile name example id 0 prime-number 0xffffffbd3f0331
	   * secret_share 0x23dc164fa011aa lpc 0x1 polynomial2 0x5dc0a0163d348
	   * bits-in-random 56
	   */
	  tail += strlen (cmd);
	  pot_config_parse (tail, count);
	  count++;


	}
    }
  fclose (fp);
  if (count == 0)
    {
      printf ("\n Unable to parse profile \n");
      exit (1);
    }
  for (i = 0; i < count; i++)
    {
      pot_profile_to_str (&profile[i], buff, BUFSIZE - 1);
      printf ("\n%s", buff);

    }
  random = pot_generate_random (&profile[0]);
  cumulative = 0;
  for (i = 0; i < count; i++)
    {
      cumulative = pot_update_cumulative (&profile[i], cumulative, random);
      printf
	("\n Cumulative for random %lld (0x%llx) is %llu (0x%llx) @pass %d \n",
	 random, random, cumulative, cumulative, i + 1);
    }
  printf ("\n Now Verifying...\n");
  if (pot_validate (&profile[count - 1], cumulative, random))
    {
      printf ("\n Valid cumulative... verification result passed");
    }
  else
    {
      printf ("\n Invalid cumulative.. verification result failed");
    }

  printf ("\n");

}
