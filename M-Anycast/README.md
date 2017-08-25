# M-Anycast

M-Anycast is a load-balancing solution for micro-service deployments inspired
by John Leddy (Comcast). M-Anycast combines Segment-Routing for IPv6 (SRv6)
with IOAM to offer an intelligent service selection and loadbalancing solution
for micro-service deployments which adapts to the conditions of the network and
the services, while only keeping state for a short period of time within the
network and avoiding the loadbalancer to be in the forwarding path of the
packet flow.

# Overview

![Alt text](./use-case.png?raw=true "Micro-service load-balancing")

![Alt text](./m-anycast-overview.png?raw=true "M-Anycast Overview")

# M-Anycast - Step by Step 

## Packet flow walk through

### SYN
![Alt text](./SYN.png?raw=true "SYN")

### SYN-ACK
![Alt text](./SYN-ACK.png?raw=true "SYN-ACK")

### ACK
![Alt text](./ACK.png?raw=true "ACK")

### Subsequent traffic
![Alt text](./subsequent-traffic.png?raw=true "Subsequent traffic")

## Example network views 

![Alt text](./Demo-M-AnyCast-SYN.png?raw=true "M-Anycast server: SYN processing")

![Alt text](./Demo-M-AnyCast-SYN-ACK.png?raw=true "M-Anycast server: SYN-ACK processing")

![Alt text](./Demo-Server1-SYN.png?raw=true "Server: SYN processing")

![Alt text](./Demo-Server1-SYN-ACK.png?raw=true "Server: SYN-ACK processing")

![Alt text](./Demo-Client.png?raw=true "Client")

# Demos

M-Anycast has been demostrated at IETF Bits-n-Bites in IETF 97 (Seoul) and IETF
98 (Chicago). Overview videos:

 - IETF 98 BnB - M-Anycast Demo (Big Buck Bunny): https://www.youtube.com/watch?v=-jqww8ydWQk
 - IETF 97 BnB - M-Anycast Demo (concept): https://www.youtube.com/watch?v=zPhrGWilLSg

# Code

M-Anycast has been implemented in [FD.io/VPP]. The key patch is found here:
https://gerrit.fd.io/r/#/c/4746/


[FD.io/VPP]: https://wiki.fd.io/view/VPP
