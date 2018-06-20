#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <inttypes.h>
#include <arpa/inet.h>
#include <stdlib.h>

#define IFNAMSIZ 16
#define cmd 1

struct ioam_register{
	int mode;
	int hop_nb;
	unsigned int freq;
	uint8_t pot_type;
	uint8_t e2e_type;
	uint16_t ioam_trace_type;
	uint32_t schemaID;
	int seg_nb;
	struct in6_addr *seg;
	char ifname[IFNAMSIZ];
};

void usage_error(){
    printf("-m mode([0,7]\n -h hop nb (>0)\n -f frequency(>0)\n -p pot type(0,1)\n -e e2e type(0,1)\n -o ioam trace type([1,4095]) \n -sId schema ID\n -s segment nb([0,128])\n -a list of ipv6 addresses\n -i interface name\n");
}

int main(int argc, char *argv[]){
	struct ioam_register ioamR = {0, 0, 0, 0, 0, 0, 0, 0, NULL, ""};
	FILE* f = NULL;
	int fd, i = 1, error = 0, j;
	char *split;
	struct in6_addr *seg;
	
	if(argc < 11){
	    usage_error();
	    return -1;
	}
	
	while(i < argc && !error){
	    if(i+1 >= argc){
	        error = 1;
	        break;
	    }

	    if(strcmp(argv[i],"-m") == 0){
            if(atoi(argv[i + 1]) < 0 || atoi(argv[i + 1]) > 7)
                error = 1;
            else
                ioamR.mode = atoi(argv[i + 1]);

            i += 2;
	    }
	    else if(strcmp(argv[i],"-h") == 0){
            if(atoi(argv[i + 1]) <= 0)
                error = 1;
            else
                ioamR.hop_nb = atoi(argv[i + 1]);
            i += 2;
	    }
	    else if(strcmp(argv[i],"-f") == 0){
            if(atoi(argv[i + 1]) <= 0)
                error = 1;
            else
                ioamR.freq = atoi(argv[i + 1]);
            i += 2;
	    }
	    else if(strcmp(argv[i],"-p") == 0){
            if(atoi(argv[i + 1]) != 0 || atoi(argv[i + 1]) != 1)
                error = 1;
            else
                ioamR.pot_type = atoi(argv[i + 1]);
            i += 2;
	    }
	    else if(strcmp(argv[i],"-e") == 0){
            if(atoi(argv[i + 1]) != 0 || atoi(argv[i + 1]) != 1)
                error = 1;
            else
                ioamR.e2e_type = atoi(argv[i + 1]);
            i += 2;
	    }
	    else if(strcmp(argv[i],"-o") == 0){
            if(atoi(argv[i + 1]) <=0 || atoi(argv[i + 1]) > 65535)
                error = 1;
            else
                ioamR.ioam_trace_type = atoi(argv[i + 1]);
            i += 2;
	    }
	    else if(strcmp(argv[i],"-sId") == 0){
            if(atoi(argv[i + 1]) != 0 || atoi(argv[i + 1]) != 1)
                error = 1;
            else
                ioamR.schemaID = atoi(argv[i + 1]);
            i += 2;
	    }
	    else if(strcmp(argv[i],"-s") == 0){
            if(atoi(argv[i + 1]) <= 0 || atoi(argv[i + 1]) > 128)
                error = 1;
            else
                ioamR.seg_nb = atoi(argv[i + 1]);
            i += 2;
	    }
	    else if(strcmp(argv[i],"-a") == 0){
            if(ioamR.seg != NULL)
                error = 1;
            ioamR.seg = malloc(sizeof(struct in6_addr) * ioamR.seg_nb);
            j = 1;
            while(argv[i + j][0] != '-' && i + j < argc){
                if(inet_pton(AF_INET6, argv[i + j], ioamR.seg + (j-1))!= 1){
                    printf("invalid ipv6 address\n");
                    error = 1;
                    break;
                }
                ++j;
            }
            i += j;
            if(j-1 != ioamR.seg_nb){
                printf("seg_nb and nb of ipv6 address doesn't correspond\n");
                error = 1;
            }
	    }
	    else if(strcmp(argv[i],"-i") == 0){
            for(j = 0; j < IFNAMSIZ && argv[i+1][j] != '\0'; ++j)
                ioamR.ifname[j] = argv[i+1][j];
            i += 2;
	    }
	    else
	        error = 1;
	}
    
    if(error){
        usage_error();
        if(ioamR.seg != NULL)
            free(ioamR.seg);
        return -1;
    }

    f=fopen("/dev/ioam6", "r+");
	if(f == NULL)
		printf("null\n");
	else
	{
		fd = fileno(f);
		if(f)
		{
			printf("result : %d\n", ioctl(fd, i , &ioamR));
			fclose(f);
		}
	}
	
	if(ioamR.seg != NULL)
            free(ioamR.seg);
	return 0;
}
