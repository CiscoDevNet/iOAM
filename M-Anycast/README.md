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

The packet flow walk through assumes a setup with 3 servers hosting the same service instance. We use symbolic addresses here:

* Client address: C::1
* Anycast address of the service: A::1 (hosted on M-Anycast node). In addition, all servers have the service address configured as an alias address
* Server1 address: S::1
* Server2 address: S::2
* Server3 address: S::3

The SRv6 and IOAM functionality is offered through the VPP implementation (see Code section for details). 

### SYN
![Alt text](./SYN.png?raw=true "SYN")

1. Client sends a TCP SYN to the anycast address of the service (A::1).
   The TCP SYN packet is a regular packet and does not contain any
   segment routing policy.
2. The M-Anycast node receives the TCP SYN packet, adds an IOAM header
   and sends a copy of the TCP SYN to each of the three servers.
   It does so by adding segment routing policy to the packet, i.e.
   the address of each server is used as the SID in the segment routing
   policy. 
3. Each TCP SYN is traversing the network towards the servers hosting
   an instance of the service. Note that the packets contain 2 IPv6
   extension headers at this point: The SRv6 routing extension header as
   well as the IOAM Hop-by-Hop extension header.
4. The TCP SYN is received by the VPP on each server. The SR-header (SRH)
   and the IOAM header are stripped off. The VPP caches the IOAM header
   information (such as timestamp information). 
5. VPP passes the "cleaned-up" TCP SYN packet to the host stack of the
   server. At this point, the TCP SYN packet is a vanilla packet without
   any IOAM or SRv6 information in it.

### SYN-ACK
![Alt text](./SYN-ACK.png?raw=true "SYN-ACK")

1. Each server host stack responds with a TCP SYN-ACK. VPP intercepts 
   the SYN-ACK and adds a SRH as well as an IOAM header to it.
2.  The IOAM header contains the information cached earlier. The SRH
   header includes the address of the M-Anycast node as the SID for the
   next SR hop. This way the SYN-ACKs are all steered back to the
   M-Anycast node.
3. The M-Anycast node receives the 3 SYN-ACKs from the 3 different servers.
   Based on the IOAM information, it selects the most appropriate server.
   This selection can be based on a variety of metrics - and could also
   include historic data from earlier SYN - SYN-ACK handshakes. In the
   demo referred to below, we use the 1-way delay between server and
   M-Anycast node as the metric. I.e. the server which shows the shortest
   delay between server and M-Anycast node is chosen. This example
   could represent a typical streaming video service, where the customer
   is interested in the lowest delay from server to client, which is 
   important for real-time media, such as sports events. 
   In our case we choose server 2 as the optimal server. The M-Anycast
   node does not strip the SRH, but updates it using C::1 as the next hop SID.
   This way the SYN-ACK will be further steered towards the Client.
4. The SYN-ACK is propagated towards the client and received by VPP on the
   client node.
5. VPP removes the SRH and creates a dynamic SR policy which steers traffic
   with destination address anycast A::1 to Server 2 (S::2). This means
   that any follow-up traffic (including the ACK) will be sent directly
   to server 2 using segment routing policy.
6. The "cleaned-up" TCP SYN-ACK is handed to the host stack. The client's
   host stack is unaware of the SR and IOAM gymnastics used.

### ACK
![Alt text](./ACK.png?raw=true "ACK")

1. The client host stack responds with a TCP ACK to the receipt of the
   TCP SYN-ACK. The ACK will have the source address of the client (C::1)
   and the destination address of the anycast service (A::1).
2. VPP intercepts the packet and adds an SRH to it. The segment routing
   policy of the SRH contains S::2 as the next hop SID. That way
   the ACK is directly sent to server 2.
3. The TCP ACK is steered directly to server 2 using SID S::2. 

### Subsequent traffic
![Alt text](./subsequent-traffic.png?raw=true "Subsequent traffic")

Subsequent traffic flows directly from client to server and vice versa.
Traffic from server to client is sent directly (without the use of SR
policy) (marked with (2) in the picture), whereas traffic from client to
server leverages the above established segment routing policy to steer
traffic directly to server 2.

## Example network views 

### Example views of the M-Anycast node
![Alt text](./Demo-M-AnyCast-SYN.png?raw=true "M-Anycast node: SYN processing")

![Alt text](./Demo-M-AnyCast-SYN-ACK.png?raw=true "M-Anycast node: SYN-ACK processing")

### Example views of the Server

![Alt text](./Demo-Server1-SYN.png?raw=true "Server: SYN processing")

![Alt text](./Demo-Server1-SYN-ACK.png?raw=true "Server: SYN-ACK processing")

### Example views of the Client
![Alt text](./Demo-Client.png?raw=true "Client")

# Demos

M-Anycast has been demostrated at IETF Bits-n-Bites in IETF 97 (Seoul) and IETF
98 (Chicago). Overview videos:

 - IETF 98 BnB - M-Anycast Demo (Big Buck Bunny): https://www.youtube.com/watch?v=-jqww8ydWQk. This demo combines IOAM, SRv6 and 6CN/MPEG-DASH (Glass2Glass)
   technologies for seamless media delivery. It was jointly delivered
   by Cisco, Comcast, and Drexel University.
 - IETF 97 BnB - M-Anycast Demo (concept): https://www.youtube.com/watch?v=zPhrGWilLSg

# Code

M-Anycast has been implemented in [FD.io/VPP]. The key patch is found here:
https://gerrit.fd.io/r/#/c/4746/


[FD.io/VPP]: https://wiki.fd.io/view/VPP
