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

##Configuration Generator
The code here generates proof-of-transit profiles for [VPP/IOS] for all 
the transit points.
```sh
#python pot_config.py <number_of_services> <max_num_of_bits> <(optional)config_for(VPP/IOS)>'
For Example: python pot_config.py 5 32 VPP
```

[iOAM-ietf-proof-of-transit]:<https://tools.ietf.org/html/draft-brockners-proof-of-transit-01> 
[VPP]:<https://wiki.fd.io/view/VPP>

##Sample output

```sh
python pot_config.py 3 32 VPP

Encap Node:
set pot profile name example id 0 prime-number 0xc839c18f secret_share 0x547d7d87 lpc 0x3 polynomial2 0x8b430c79 bits-in-random 32

Intermediate Node 1:
set pot profile name example id 0 prime-number 0xc839c18f secret_share 0xad2656c6 lpc 0xc839c18c polynomial2 0x8152aee6 bits-in-random 32

Dencap/Verifier Node:
set pot profile name example id 0 validate-key 0xc839c18b prime-number 0xc839c18f secret_share 0x41c0ca2a lpc 0x1 polynomial2 0xaa68a8d6 bits-in-random 32
```
