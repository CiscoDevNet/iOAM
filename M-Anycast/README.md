# M-Anycast - smart service selection and load-balancing for micro-services

M-Anycast is a load-balancing solution for micro-service deployments inspired
by John Leddy (Comcast). M-Anycast combines Segment-Routing for IPv6 (SRv6)
with IOAM to offer an intelligent service selection and loadbalancing solution
for micro-service deployments which adapts to the conditions of the network and
the services, while only keeping state for a short period of time within the
network and avoiding the loadbalancer to be in the forwarding path of the
packet flow.

In micro-service deployments the same service is offered by multiple servers or
PODs in the case of Kubernetes. It is the nature of micro-services that the
individual instance of an application that delivers the service does not need
to be very stable or available, i.e. individual instances can come and go.
In an SP context, the service could for example be a video service, as shown by
one of the recent demos of M-Anycast at IETF 98 (see below). A client desires
to connect to a service which is most optimal for him. The setup is shown
below:

![Alt text](./use-case.png?raw=true "Micro-service load-balancing")


The M-Anycast chooses an appropriate micro-service instance and server for
the client, while keeping the load-balancer very light weight. It does so
by inserting a so call "M-Anycast" server into the initial TCP SYN/SYN-ACK
handshake to choose an appropriate server for the client to deliver the
service. Once the server is selected the TCP session establishment will
be completed with the selected server and the M-Anycast service selector/
load-balancer is no longer in the traffic path. 

![Alt text](./m-anycast-overview.png?raw=true "M-Anycast Overview")

The picture shows a set of service instances (S1, S2, ..., S12) (a service
instance would equate a POD in Kubernetes) hosted on a set of servers.
All service instances are assumed to deliver the same service. 
The service is reachable by an Anycast address which would be hosted on
one or multiple M-Anycast nodes (the picture only shows one M-Anycast
server). In addition, all Servers have the Anycast address of the service
configured as an alias/secondary address, along with the main/native
address.

For service instance selection the following steps are carried out:

1. The client desires to establish a TCP session to the service
   which is desribed by an Anycast address. The client sends out a
   TCP SYN. The TCP SYN is received by the M-Anycast node and
   replicated to a subset of the available service instances. 
   The TCP SYNs are steered towards the chosen set of service instances
   using Segment Routing Policy. In addition, the M-Anycast node 
   adds IOAM meta-data to the TCP SYN so that packet level telemetry
   information such as network delay, TCP host stack delay, server or
   service load information, or application
   response time can be measured and propagated back to the M-Anycast
   node.
2. The servers respond with a TCP ACK back to the M-Anycast node 
   (the responses are also steered towards the M-Anycast node 
   using segment routing policy). The M-Anycast node uses the IOAM
   meta-data contained in the TCP SYN-ACK to choose the "optimal"
   SYN-ACK received. Selection can be based on criteria such as
   shortest server to M-Anycast node delay, shortest application
   response time within the server, load-information of the server, etc.
   The server sends the chosen TCP SYN-ACK back to the client.
   All other SYN-ACKs will be dropped and a RST is sent to those
   servers to clean up connection state.
3. The SYN-ACK that the client receives contains segment routing policy
   which reveals the source of the SYN-ACK. In our case here, service 4
   was chosen. The client will send the TCP ACK directly towards service 4,
   now omitting the de-tour via the M-Anycast node. The client does so
   by using segment routing policy with service 4 as the destination SID.
4. Once the TCP 3-way handshake is completed, a TCP session is established
   between the service 4 and the client. Data traffic can now flow directly
   between client and service 4. 

Note that the M-Anycast node is only in the packet path for the initial
SYN - SYN-ACK exchange. In additon, the M-Anycast node also only needs to
keep session state for the duration of the SYN - SYN-ACK exchange, rather
than for the duration of the entire TCP session. This makes the M-Anycast
concept very scalable. Also note, that multiple M-Anycast nodes can
operate in parallel, in case the M-Anycast service needs to be scaled.

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

![Alt text](./Demo-M-AnyCast-SYN.png?raw=true "M-Anycast node: SYN processing")

![Alt text](./Demo-M-AnyCast-SYN-ACK.png?raw=true "M-Anycast node: SYN-ACK processing")

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
