The source code in this folder implements profile and configuration generation for proof-of-transit.
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
The code here generates proof-of-transit profiles and can dump configuration 
for [VPP] and IOS for all the transit points.

```sh
$ python pot_config.py <number_of_transit_points> <max_num_of_bits> <(optional)config_for(VPP/IOS)>'
```

[iOAM-ietf-proof-of-transit]:<https://tools.ietf.org/html/draft-brockners-proof-of-transit-01> 
[VPP]:<https://wiki.fd.io/view/VPP>

##Sample output

###VPP

To generate [VPP] configuration for POT via 3 VPP nodes:
```sh
$ python pot_config.py 3 63 VPP
Encap Node:
set pot profile name example id 0 prime-number 0x762b42a59295a7e9 secret_share 0x4e079185d5c8656cL lpc 0x762b42a59295a7e6L polynomial2 0x203944b6adaee7bbL bits-in-random 63

Intermediate Node 1:
set pot profile name example id 0 prime-number 0x762b42a59295a7e9 secret_share 0x1cd7b41fc888eee3L lpc 0x1L polynomial2 0x6b6b8864cdd12e9dL bits-in-random 63

Dencap/Verifier Node:
set pot profile name example id 0 validate-key 0xbdc7fa0606476aa prime-number 0x762b42a59295a7e9 secret_share 0x6fc240e7e3ee1ffc lpc 0x3L polynomial2 0x4b3243ae20224822L bits-in-random 63
```

###IOS
To generate IOS configuration for POT via 6 nodes:

```sh
$ python pot_config.py 6 31 IOS
Encap Node:
ipv6 ioam service-chaining example
service-chain insert
prime number 0x7971cb9f
secret key 0x710778f7
lpc 0x6
polynomial2 0x4160579b
bits-in-random 31

Intermediate Node 1:
ipv6 ioam service-chaining example
prime number 0x7971cb9f
secret key 0x17d2db69
lpc 0x6
polynomial2 0x3a74f3bc
bits-in-random 31

Intermediate Node 2:
ipv6 ioam service-chaining example
prime number 0x7971cb9f
secret key 0x5a03c79c
lpc 0x7971cb90
polynomial2 0x7211cb22
bits-in-random 31

Intermediate Node 3:
ipv6 ioam service-chaining example
prime number 0x7971cb9f
secret key 0x535061b2
lpc 0x14
polynomial2 0x228a6fa9
bits-in-random 31

Intermediate Node 4:
ipv6 ioam service-chaining example
prime number 0x7971cb9f
secret key 0x21a00364
lpc 0x7971cb90
polynomial2 0x4c3a504e
bits-in-random 31

Dencap/Verifier Node:
ipv6 ioam service-chaining example
service-chain analyze
prime number 0x7971cb9f
secret key 0x26fcfd89
verifier key 0x6c088b23
lpc 0x7971cb9e
polynomial2 0x24b06d22
bits-in-random 31

```

###Generic profiles
To generate POT profiles for transit via 4 transit points:

```sh
$ python pot_config.py 4 63
Encap Node:
('node index', 8)
('prime number', 9173428787777945503)
('secret_share', 8747388548068043745L)
('lpc', '0x7f4e90e12bd8bf9eL')
('polynomial2 ', 6501012465313961440L)
('bits-in-random', 63)

Intermediate Node 1:
('node index', 6)
('prime number', 9173428787777945503)
('secret_share', 863366680943605488L)
('lpc', '0x4L')
('polynomial2 ', 4084672613142777182L)
('bits-in-random', 63)

Intermediate Node 2:
('node index', 4)
('prime number', 9173428787777945503)
('secret_share', 1759725238437644658L)
('lpc', '0x7f4e90e12bd8bf99L')
('polynomial2 ', 7312536604555728835L)
('bits-in-random', 63)

Dencap/Verifier Node:
('node index', 2)
('prime number', 9173428787777945503)
('verifier key', 9173428787777945480)
('secret_share', 8549782707618845181L)
('lpc', '0x4L')
('polynomial2 ', 3922671016130333679L)
('bits-in-random', 63)

```
