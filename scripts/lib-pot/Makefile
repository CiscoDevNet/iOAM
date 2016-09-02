CC=gcc
CFLAGS= -O0 -g -I/usr/include/
LDFLAGS= -Wl,--gc-sections
LIBSOURCES=./pot_util.h ./pot_util.c ./math64.h
LIBFILES=./pot_util.c 
LIBOBJFILES = ./pot_util.o

.PHONY: all
all: libpot.a test 

libpot.a: $(LIBSOURCES) 
	$(CC) -c $(CFLAGS) $(LIBFILES)
	ar -rcs libpot.a $(LIBOBJFILES)
test:
	$(CC) $(CFLAGS) -o $@ test.c libpot.a -static 
clean:
	rm *.o *.a test



