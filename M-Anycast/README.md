# M-Anycast

M-Anycast is a load-balancing solution for micro-service deployments inspired
by John Leddy (Comcast). M-Anycast combines Segment-Routing for IPv6 (SRv6)
with IOAM to offer an intelligent service selection and loadbalancing solution
for micro-service deployments which adapts to the conditions of the network and
the services, while only keeping state for a short period of time within the
network and avoiding the loadbalancer to be in the forwarding path of the
packet flow.

# Overview

# Packet flow walk through

# Demos

M-Anycast has been demostrated at IETF Bits-n-Bites in IETF 97 (Seoul) and IETF
98 (Chicago). Overview videos:

 - IETF 98 BnB - M-Anycast Demo (Big Buck Bunny): https://www.youtube.com/watch?v=-jqww8ydWQk
 - IETF 97 BnB - M-Anycast Demo (concept): https://www.youtube.com/watch?v=zPhrGWilLSg

# Code

M-Anycast has been implemented in [FD.io/VPP]. The key patch is found here:
https://gerrit.fd.io/r/#/c/4746/

# References

[FD.io/VPP]: https://wiki.fd.io/view/VPP
