# In-band OAM (iOAM)

iOAM is an implementation study to record operational information invthe packet while the packet traverses a path between two points in
the network.iOAM is to complement current out-of-band OAM (sometimes also called "active" OAM) mechanisms based on ICMP or other types of probe packets.
   
# Team:
- Frank Brockners
- Shwetha Bhandari
- Srihari Raghavan
- Ranganathan T.S
- Karthik Babu Harichandra Babu 
- Vengada Prasad Govindan 
- Ananthakrishnan Rajamani
- Sagar Srivatsav
- Manaswi G Reddy

# Current Status
In development. Initial beta version availble in VPP [fd.io]

# Overview

"in-band" operation, administration, and maintenance (iOAM) mechanisms to record OAM information in a packet while the packet traverses a particular network domain. iOAM is an implementation study to record operational information in the packet while the packet traverses a path between two points in the network. iOAM defines a set of meta-data that is carried as part of the live traffic, i.e. it is added to the packets.  In-band OAM mechanisms, which are sometimes also referred to as embedded network telemetry or passive OAM are an active topic of discussions. In-band network telemetry has been defined for P4 [P4] and the SPUD prototype (see [SPUD]) uses a similar logic in that it allows network devices on the path between endpoints to participate explicitly in the tube outside the end-to-end context.  Even the IPv4
route-record option defined in [RFC0791] can be considered an in-band OAM mechanism.  This draft defines several transport options for carrying iOAM meta-data in packets.  Those transport options include plain IPv4 and IPv6 as well as VXLAN-GPE, Segment Routing for IPv6 and Network Service Header (NSH).

  iOAM is to complement "out-of-band" or "active" mechanisms such as
   ping or trace-route. iOAM mechanisms can be leveraged where current
   out-of-band mechanisms do not apply or do not offer the desired
   results, such as proving that a certain set of traffic takes a pre-
   defined path, SLA verification for the live data traffic, detailed
   statistics on traffic distribution paths in networks that distribute
   traffic across multiple paths, or scenarios where probe traffic is
   potentially handled differently from regular data traffic by the
   network devices.

   Compared to probably the most prominent example of "in-band OAM"
   which is IPv4 route recording [RFC0791], the iOAM approach explores a
   set of new capabilities including:

   -  Flexible data format to allow different types of information to
       be captured as part of an in-band OAM operation, including not
       only path tracing information, but information such as
       timestamps, sequence numbers, or even generic meta-data.

   -  Data format to express node and link identifiers to record the
       path a packet takes within the packet with a fixed amount of
       added meta-data.
   -  Ability to detect whether any nodes where skipped while recording
       in-band OAM information (i.e. iOAM is disabled on those nodes).
   -  Ability to actively process information in the packet to e.g.
       prove in a cryptographically secure way that a packet really took
       a pre-defined path using some traffic steering method such as
       service chaining or traffic engineering.

   -  Ability to include meta-data beyond simple path information (e.g.
       time-stamps or even generic meta-data of a particular use case).

   iOAM is an implementation study and should be considered as a "tool
   box" to showcase how passive OAM can complement active OAM for
   different deployments and packet transport formats.  One example
   implementation is open sourced as part of the FD.io [FD.io] project.
   
   

# References


 - [SPUD]
              Hildebrand, J. and B. Trammell, "Substrate Protocol for
              User Datagrams (SPUD) Prototype", draft-hildebrand-spud-
              prototype-03 (work in progress), March 2015.
 - [P4]       Kim, , "P4: In-band Network Telemetry (INT)", September
              2015.


[p4]: http://p4.org/p4/inband-network-telemetry/
[SPUD]: https://tools.ietf.org/html/draft-hildebrand-spud-prototype-03
[fd.io]: http://fd.io
[RFC0791]: https://tools.ietf.org/html/rfc0791.html
