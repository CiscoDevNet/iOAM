The source code in this folder provides APIs for implementing proof-of-transit.
For details on proof-of-transit,
see the IETF draft [iOAM-ietf-proof-of-transit].

The Proof of transit mechanism implemented here is based on
Shamir's Secret Sharing algorithm.
The overall algorithm uses two polynomials
POLY-1 and POLY-2. The degree of polynomials depends on number of nodes
to be verified for transit.
POLY-1 is secret and constant. Each node gets a point on POLY-1
at setup-time and keeps it secret.
POLY-2 is public, random and per packet.
Each node is assigned a point on POLY-1 and POLY-2 with the same x index.
Each node derives its point on POLY-2 each time a packet arrives at it.
A node then contributes its points on POLY-1 and POLY-2 to construct
POLY-3 (POLY-3 = POLY-1 + POLY-2) using lagrange extrapolation and
forwards it towards the verifier by updating POT data in the packet.
The verifier constructs POLY-3 from the accumulated value from all the nodes
and its own points on POLY-1 and POLY-2 and verifies whether
POLY-3 = POLY-1 + POLY-2.  Only the verifier knows POLY-1.
The solution leverages finite field arithmetic in a field of size "prime number"
for reasons explained in description of Shamir's secret sharing algorithm.
##Build
```sh
make all
rebuild: make clean all
Library only: make libpot.a
```
test and libpot.a will be built.

##Test
The test code here parses proof-of-transit profiles generated for [VPP] for all 
the transit points and verifies the correctness of profiles using the library APIs.
```sh
#./test <config text file>
For Example: ./test sample_config.txt
```

[iOAM-ietf-proof-of-transit]:<https://tools.ietf.org/html/draft-brockners-proof-of-transit-01> 
[VPP]:<https://wiki.fd.io/view/VPP>

##Sample output

```sh
./test sample_config.txt 

<sc-profile> profile-id 0 prime-number 0xffffffbd3f0331 secret_share 0x23dc164fa011aa lpc 0x1 polynomial2 0x5dc0a0163d348 bits-in-random 56</sc-profile>

<sc-profile> profile-id 0 prime-number 0xffffffbd3f0331 secret_share 0x3fc03a91e02ea lpc 0x3 polynomial2 0x1fc0011e554b0 bits-in-random 56</sc-profile>

<sc-profile> profile-id 0 validate-key 0xffffffbd3f032f prime-number 0xffffffbd3f0331 secret_share 0xff00b18fe08ce lpc 0xffffffbd3f032e polynomial2 0x3f00367b145c8 bits-in-random 56</sc-profile>

 Cumulative for random 56670260664905063 (0xc9554d4442ad67) is 68413183647715929 (0xf30d6d95469259) @pass 1 

 Cumulative for random 56670260664905063 (0xc9554d4442ad67) is 27291396257585097 (0x60f5615b5b97c9) @pass 2 

 Cumulative for random 56670260664905063 (0xc9554d4442ad67) is 56670260664905061 (0xc9554d4442ad65) @pass 3 

 Now Verifying...

 Valid cumulative... verification result passed
```
