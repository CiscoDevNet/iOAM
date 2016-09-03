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
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <string.h>
#include "pot_util.h"
#include "math64.h"

pot_main_t pot_main;

static void pot_profile_cleanup (pot_profile * profile);

static void
pot_main_profiles_reset (void)
{
  pot_main_t *sm = &pot_main;
  int i = 0;

  for (i = 0; i < MAX_POT_PROFILES; i++)
    {
      pot_profile_cleanup (&(sm->profile_list[i]));
    }
  sm->active_profile_id = 0;
  sm->profile_list_name[0] = 0;
}

int
pot_util_init (void)
{
  pot_main_profiles_reset ();

  return (0);
}

static void
pot_profile_init (pot_profile * new, u8 id)
{
  if (new)
    {
      memset (new, 0, sizeof (pot_profile));
      new->id = id;
    }
}

pot_profile *
pot_profile_find (u8 id)
{
  pot_main_t *sm = &pot_main;

  if (id < MAX_POT_PROFILES)
    {
      return (&(sm->profile_list[id]));
    }
  return (NULL);
}

static int
pot_profile_name_equal (u8 * name0, u8 * name1)
{
  int len0, len1;

  len0 = strlen (name0);
  len1 = strlen (name1);
  if (len0 != len1)
    return (0);
  return (0 == strncmp ((char *) name0, (char *) name1, len0));
}

int
pot_profile_list_is_enabled (u8 * name)
{
  pot_main_t *sm = &pot_main;
  return (pot_profile_name_equal (sm->profile_list_name, name));
}

void
pot_profile_list_init (u8 * profile_list_name)
{
  pot_main_t *sm = &pot_main;
  int i = 0;

  /* If it is the same profile list skip reset */
  if (pot_profile_name_equal (sm->profile_list_name, profile_list_name))
    {
      return;
    }

  pot_main_profiles_reset ();
  strncpy (&sm->profile_list_name[0], profile_list_name, NAME_LEN);
  sm->active_profile_id = 0;

  for (i = 0; i < MAX_POT_PROFILES; i++)
    {
      pot_profile_init (&(sm->profile_list[i]), i);
    }
}

static void
pot_profile_cleanup (pot_profile * profile)
{
  u16 id = profile->id;

  memset (profile, 0, sizeof (pot_profile));
  profile->id = id;		/* Restore id alone */
}

int
pot_profile_create (pot_profile * profile, u64 prime,
		    u64 poly2, u64 lpc, u64 secret_share)
{
  if (profile && !profile->in_use)
    {
      pot_profile_cleanup (profile);
      profile->prime = prime;
      profile->primeinv = 1.0 / prime;
      profile->lpc = lpc;
      profile->poly_pre_eval = poly2;
      profile->secret_share = secret_share;
      profile->total_pkts_using_this_profile = 0;
      profile->valid = 1;
      return (0);
    }

  return (-1);
}

int
pot_set_validator (pot_profile * profile, u64 key)
{
  if (profile && !profile->in_use)
    {
      profile->validator = 1;
      profile->secret_key = key;
      return (0);
    }
  return (-1);
}

u64
pot_update_cumulative_inline (u64 cumulative, u64 random,
			      u64 secret_share, u64 prime, u64 lpc,
			      u64 pre_split, double prime_inv)
{
  u64 share_random = 0;
  u64 cumulative_new = 0;

  /*
   * calculate split share for random
   */
  share_random = add64_mod (pre_split, random, prime, prime_inv);

  /*
   * lpc * (share_secret + share_random)
   */
  share_random = add64_mod (share_random, secret_share, prime, prime_inv);
  share_random = mul64_mod (share_random, lpc, prime, prime_inv);

  cumulative_new = add64_mod (cumulative, share_random, prime, prime_inv);

  return (cumulative_new);
}

u64
pot_update_cumulative (pot_profile * profile, u64 cumulative, u64 random)
{
  if (profile && profile->valid != 0)
    {
      return (pot_update_cumulative_inline
	      (cumulative, random, profile->secret_share, profile->prime,
	       profile->lpc, profile->poly_pre_eval, profile->primeinv));
    }
  return (0);
}

u8
pot_validate_inline (u64 secret, u64 prime, double prime_inv,
		     u64 cumulative, u64 random)
{
  if (cumulative == (random + secret))
    {
      return (1);
    }
  else if (cumulative == add64_mod (random, secret, prime, prime_inv))
    {
      return (1);
    }
  return (0);
}

/*
 * return True if the cumulative matches secret from a profile
 */
u8
pot_validate (pot_profile * profile, u64 cumulative, u64 random)
{
  if (profile && profile->validator)
    {
      return (pot_validate_inline (profile->secret_key, profile->prime,
				   profile->primeinv, cumulative, random));
    }
  return (0);
}

/*
 * Utility function to get random number per pack
 */
u64
pot_generate_random (pot_profile * profile)
{
  u64 random = 0;
  int32_t second_half;

  /*
   * Upper 4 bytes seconds
   */
  random = (u64) time (NULL);

  random &= 0xffffffff;
  random = random << 32;
  /*
   * Lower 4 bytes random number
   */
  srand (time (NULL));
  second_half = rand ();
  random |= second_half;

  if (profile != NULL)
    {
      random &= profile->bit_mask;
    }
  return (random);
}

int
pot_profile_set_bit_mask (pot_profile * profile, u16 bits)
{
  int sizeInBits;

  if (profile && !profile->in_use)
    {
      sizeInBits = sizeof (profile->bit_mask) * 8;
      profile->bit_mask =
	(bits >= sizeInBits ? (u64) - 1 : (u64) ((u64) 1 << (u64) bits) - 1);
      return (0);
    }
  return (-1);
}

/* Function to get no of set bits in binary
   representation of passed binary no. */
static int
countSetBits (uint64_t n)
{
  unsigned int count = 0;
  while (n)
    {
      n &= (n - 1);
      count++;
    }
  return count;
}

int
pot_profile_to_str (pot_profile * profile, char *buf, int n)
{
  int written = 0;
  int total_written = 0;
  int i = 0;
  int no_of_ser = 0;
  if (profile && buf)
    {
      total_written += written =
	snprintf (buf, n, "<pot-profile> profile-id %d ", profile->id);
      n -= written;
      buf += written;
      if (profile->validator)
	{
	  total_written += written = snprintf (buf, n, "validate-key 0x%llx ",
					       profile->secret_key);
	  n -= written;
	  buf += written;
	}
      total_written += written =
	snprintf (buf, n,
		  "prime-number 0x%llx secret_share 0x%llx lpc 0x%llx polynomial2 0x%llx ",
		  profile->prime, profile->secret_share, profile->lpc,
		  profile->poly_pre_eval);
      n -= written;
      buf += written;
      total_written += written =
	snprintf (buf, n, "bits-in-random %d</pot-profile>\n",
		  countSetBits (profile->bit_mask));
      n -= written;
      buf += written;
    }
  return total_written;
}
