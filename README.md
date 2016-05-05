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
   implementation is open sourced as part of the [FD.io] project.
   
# Motivation   

   In several scenarios it is beneficial to make available information
   about which path a packet took through the network.  This includes
   tasks like debugging, troubleshooting, as well as network planning
   and network optimization but also policy or SLA compliance checks.
   This section discusses the motivation to introduce new options for
   enhanced network diagnostics.
   
## Passive OAM

   Mechanisms which add tracing information to the regular data traffic,
   sometimes also referred to as "in-band" or "passive OAM" can
   complement active, probe-based mechanisms such as ping or traceroute,
   which are sometimes considered as "out-of-band", because the messages
   are transported independently from regular data traffic.  "In-band"
   mechanisms do not require extra traffic to be sent and hence don't
   change the traffic mix within the network.  Traceroute and ping for
   example use ICMP messages: New packets are injected to get tracing
   information.  Those add to the number of messages in a network, which
   already might be highly loaded or suffering performance issues for a
   particular path or traffic type.

   Packet scheduling algorithms, especially for balancing traffic across
   equal cost paths or links, often leverage information contained
   within the packet, such as protocol number, IP-address or MAC-
   address.  Probe packets would thus either need to be sent from the
   exact same endpoints with the exact same parameters, or probe packets
   would need to be artificially constructed as "fake" packets and
   inserted along the path.  Both approaches are often not feasible from
   an operational perspective, be it that access to the end-system is
   not feasible, or that the diversity of parameters and associated
   probe packets to be created is simply too large.  An in-band
   mechanism is an alternative in those cases.

   In-band mechanisms also don't suffer from implementations, where
   probe traffic is handled differently (and potentially forwarded
   differently) by a router than regular data traffic.
   
## Overlay and underlay correlation

   Several network deployments leverage tunneling mechanisms to create
   overlay or service-layer networks.  Examples include VXLAN, GRE, or
   LISP.  One typical attribute of overlay networks is that they do not
   offer the user of the overlay any insight into the underlay network.
   This means that the path that a particular tunneled packet takes, nor
   other operational details such as the per-hop delay/jitter in the
   underlay are visible to the user of the overlay network, giving rise
   to diagnosis and debugging challenges in case of connectivity or
   performance issues.  The scope of OAM tools like ping or traceroute
   is limited to either the overlay or the underlay which means that the
   user of the overlay has typically no access to OAM in the underlay,
   unless specific operational procedures are put in place.  With in-
   band OAM the operator of the underlay can offer details of the
   connectivity in the underlay to the user of the overlay.  The
   operator of the egress tunnel router could choose to share the
   recorded information about the path with the user of the overlay.
   
   Coupled with mechanisms such as segment routing
   [segment-routing], overlay network and underlay
   network can be more tightly coupled: The user of the overlay has
   detailed diagnostic information available in case of failure
   conditions.  The user of the overlay can also use the path recording
   information as input to traffic steering or traffic engineering
   mechanisms, to for example achieve path symmetry for the traffic
   between two endpoints.  [lisp-sr] is an example for how
   these methods can be applied to LISP.

## Analytics and diagnostics

Network planners and operators benefit from knowledge of the actual traffic distribution in the network.  Deriving an overall network connectivity traffic matrix one typically needs to correlate data gathered from each individual device in the network.  If the path of a packet is recorded while the packet is forwarded, the entire path that a packet took through the network is available to the egress-system.  This obviates the need to retrieve individual traffic statistics from every device in the network and correlate those statistics, or employ other mechanisms such as leveraging traffic-engineering with null-bandwidth tunnels just to retrieve the appropriate statistics to generate the traffic matrix.

In addition, with individual path recording, information is available at packet level granularity, rather than only at aggregate level - as is usually the case with IPFIX-style methods which employ flow- filters at the network elements.  Data-center networks with heavy use of equal-cost multipath (ECMP) forwarding are one example where detailed statistics on flow distribution in the network are highly desired.  If a network supports ECMP one can create detailed statistics for the different paths packets take through the network at the egress system, without a need to correlate/aggregate statistics from every router in the system.  Transit devices are off-loaded from the task of gathering packet statistics.

## Proof of Transit

   Several deployments use traffic engineering, policy routing, segment
   routing or service function chaining (SFC) to steer packets through a
   specific set of nodes.  In certain cases regulatory obligations or a
   compliance policy require to prove that all packets that are supposed
   to follow a specific path are indeed being forwarded across the exact
   set of nodes specified.  I.e. if a packet flow is supposed to go
   through a series of service functions or network nodes, it has to be
   proven that all packets of the flow actually went through the service
   chain or collection of nodes specified by the policy.  In case the
   packets of a flow weren't appropriately processed, a verification
   device would be required to identify the policy violation and take
   corresponding actions (e.g. drop or redirect the packet, send an
   alert etc.) corresponding to the policy.  In today's deployments, the
   proof that a packet traversed a particular service chain is typically
   delivered in an indirect way: Service appliances and network
   forwarding are in different trust domains.  Physical hand-off-points
   are defined between these trust domains (i.e. physical interfaces).
   Or in other terms, in the "network forwarding domain" things are
   wired up in a way that traffic is delivered to the ingress interface
   of a service appliance and received back from an egress interface of
   a service appliance.  This "wiring" is verified and trusted.  The
   evolution to Network Function Virtualization (NFV) and modern service
   chaining concepts (using technologies such as LISP, NSH, Segment
   Routing, etc.) blurs the line between the different trust domains,
   because the hand-off-points are no longer clearly defined physical
   interfaces, but are virtual interfaces.  Because of that very reason,
   networks operators require that different trust layers not to be
   mixed in the same device.  For an NFV scenario a different proof is
   required.  Offering a proof that a packet traversed a specific set of
   service functions would allow network operators to move away from the
   above described indirect methods of proving that a service chain is
   in place for a particular application.

   A solution approach is based on meta-data which is added to every
   packet.  The meta data is updated at every hop and is used to verify
   whether a packet traversed all required nodes.  A particular path is
   either described by a set of secret keys, or a set of shares of a
   single secret.  Nodes on the path retrieve their individual keys or
   shares of a key (using for e.g.  Shamir's Shared Sharing Secret
   scheme) from a central controller.  The complete key set is only
   known to the verifier - which is typically the ultimate node on a
   path that requires verification.  Each node in the path uses its
   secret or share of the secret to update the meta-data of the packets
   as the packets pass through the node.  When the verifier receives a
   packet, it can use its key(s) along with the meta-data to validate
   whether the packet traversed the service chain correctly.  The
   detailed mechanisms used for path verification along with the
   procedures applied to the meta-data carried in the packet for path
   verification are beyond the scope of this document.  Details will be
   addressed in a separate document.


## Frame replication/elimination decision for bi-casting/active-active networks

Bandwidth- and power-constrained, time-sensitive, or loss-intolerant networks (e.g. networks for industry automation/control, health care) require efficient OAM methods to decide when to replicate packets to a secondary path in order to keep the loss/error-rate for the receiver at a tolerable level - and also when to stop replication and eliminate the redundant flow. Many IoT networks are time sensitive and cannot leverage automatic retransmission requests (ARQ) to cope with transmission errors or lost packets. Transmitting the data over multiple disparate paths (often called bi-casting or live-live) is a method used to reduce the error rate observed by the receiver. TSN receive a lot of attention from the manufacturing industry as shown by a various standardization activities and industry forums being formed (see e.g. IETF 6TiSCH, IEEE P802.1CB,AVnu).

## Example use-cases of iOAM6
<table border="3" align="left">
  <tr>
    <td><b>Use Case</b></td>
    <td><b>Description</b></td>
  </tr>
  <tr>
    <td>Traffic Matrix</td>
    <td>Derive the network traffic matrix: Traffic for a given time interval between any two edge nodes of a given domain. Could be performed for all traffic or per QoS-class.</td>
  </tr>
  <tr>
    <td>Flow Debugging </td>
    <td>Discover which path(s) a particular set of traffic (identified by an n-tuple) takes in the network. Especially useful in case traffic is balanced across multiple paths, like with link aggregation (LACP) or equal cost multi-pathing (ECMP). </td>
  </tr>
  <tr>
    <td>Loss statistics per path </td>
    <td>Retrieve loss statistics per flow and path in the network</td>
  </tr>
  <tr>
    <td>Path Heat Maps </td>
    <td>Discover highly utilized links in the network </td>
  </tr>
  <tr>
    <td>Trend analysis on traffic patterns </td>
    <td>Analyze if (and if so how) the forwarding path for a specific set of traffic changes over time (can give hints to routing issues, instable links etc.)</td>
  </tr>
  <tr>
    <td>Network delay distribution </td>
    <td>Show delay distribution across network by node or links. If enabled per application or a specific flow then display the path taken with delay at each node. </td>
  </tr>
  <tr>
    <td>Low-Power networks</td>
    <td>Include application level OAM information (e.g. battery charge level) into data traffic to avoid sending extra OAM traffic which incur an extra cost on the devices. Using the battery charge level as example, we could avoid sending extra OAM packets just to communicate battery health, and as such would save battery on sensors.</td>
  </tr>
  <tr>
    <td>Path verification or service chain verification</td>
    <td>Proof and verification of packets traversing check points in the network, where check points can be nodes in the network or service functions. </td>
  </tr>
</table>

   

# References


 - [SPUD]
              Hildebrand, J. and B. Trammell, "Substrate Protocol for
              User Datagrams (SPUD) Prototype", draft-hildebrand-spud-
              prototype-03 (work in progress), March 2015.
 - [P4]       Kim, , "P4: In-band Network Telemetry (INT)", September
              2015.
 - [segment-routing] Filsfils, C., Previdi, S., Bashandy, A., Decraene, B.,
              Litkowski, S., Horneffer, M., Milojevic, I., Shakir, R.,
              Ytti, S., Henderickx, W., Tantsura, J., and E. Crabbe,
              "Segment Routing Architecture", draft-filsfils-rtgwg-
              segment-routing-07 (work in progress), June 2016.
 - [segment-routing-header]
              Previdi, S., Filsfils, C., Field, B., Leung, I., Linkova,
              J., Kosugi, T., Vyncke, E., and D. Lebrun, "IPv6 Segment
              Routing Header (SRH)", draft-ietf-6man-segment-routing-
              header-01 (work in progress), March 2016.
 - [lisp-sr]  Brockners, F., Systems, C., Maino, F., and D. Lewis, "LISP
              Extensions for Segment Routing", draft-brockners-lisp-
              sr-00 (work in progress), July 2013.

[p4]: http://p4.org/p4/inband-network-telemetry/
[SPUD]: https://tools.ietf.org/html/draft-hildebrand-spud-prototype-03
[fd.io]: http://fd.io
[RFC0791]: https://tools.ietf.org/html/rfc0791.html
[segment-routing]: https://tools.ietf.org/html/draft-ietf-spring-segment-routing-07
[segment-routing-header]: https://tools.ietf.org/html/draft-ietf-6man-segment-routing-header-01
[lisp-sr]: https://tools.ietf.org/html/draft-brockners-lisp-sr-01
