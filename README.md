# In-band OAM (iOAM)

In-band OAM (or "in situ OAM") is an implementation study to record operational
information in the packet while the packet traverses a path between two points
in the network. In-band OAM is to complement current out-of-band OAM (sometimes
also called "active" OAM) mechanisms based on ICMP or other types of probe
packets.
   
# Overview

"In-band" OAM describes an approach to record OAM and telemetry information
within the data packet while the data packet traverses a network or a
particular network domain.  The term "in-band" refers to the fact that the OAM
and telemetry data is carried within data packets rather than being sent
within packets specifically dedicated to OAM.  In-band OAM mechanisms, which
are sometimes also referred to as embedded network telemetry are a current
topic of discussion.  In-band network telemetry has been defined for [P4].  The
SPUD prototype [SPUD] uses a similar logic that allows
network devices on the path between endpoints to participate explicitly in the
tube outside the end-to-end context.  Even the IPv4 route-record option defined
in [RFC0791] can be considered an in-band OAM mechanism.  In-band OAM
complements "out-of-band" mechanisms such as ping or traceroute, or more recent
active probing mechanisms, as described in [I-D.lapukhov-dataplane-probe].
In-band OAM mechanisms can be leveraged where current out-of-band mechanisms do
not apply or do not offer the desired characteristics or requirements, such as
proving that a certain set of traffic takes a pre-defined path,
strict congruency is desired, checking service level agreements for
the live data traffic, detailed statistics on traffic distribution
paths in networks that distribute traffic across multiple paths, or
scenarios where probe traffic is potentially handled differently from
regular data traffic by the network devices.  [RFC7276] presents an
overview of OAM tools.

Compared to probably the most basic example of "in-band OAM" which is
IPv4 route recording [RFC0791], an in-band OAM approach has the
following capabilities:

- A flexible data format to allow different types of information to
  be captured as part of an in-band OAM operation, including not
  only path tracing information, but additional operational and
  telemetry information such as timestamps, sequence numbers, or
  even generic data such as queue size, geo-location of the node
  that forwarded the packet, etc.

- A data format to express node as well as link identifiers to
  record the path a packet takes with a fixed amount of added data.

- The ability to detect whether any nodes were skipped while
  recording in-band OAM information (i.e., in-band OAM is not
  supported or not enabled on those nodes).
   
- The ability to actively process information in the packet, for
  example to prove in a cryptographically secure way that a packet
  really took a pre-defined path using some traffic steering method
  such as service chaining or traffic engineering.

- The ability to include OAM data beyond simple path information,
  such as timestamps or even generic data of a particular use case.

- The ability to include OAM data in various different transport
  protocols.

A detailed description in-band OAM concepts, capabilities,
data-formats and transport encapsulations can be found in the
following IETF internet drafts:

- [Requirements for In-band
  OAM](https://tools.ietf.org/html/draft-brockners-inband-oam-requirements-03)
  discusses the motivation and requirements for including
  specific operational and telemetry information into data packets
  while the data packet traverses a path between two points in the
  network.

- [Data Formats for In-band
  OAM](https://tools.ietf.org/html/draft-brockners-inband-oam-data-07)
  discusses the data types and data formats for in-band OAM data
  records.

- [Encapsulations for In-band OAM
  Data](https://tools.ietf.org/html/draft-brockners-inband-oam-transport-05) 
  outlines how in-band OAM data records can be transported in protocols such as
  NSH, Segment Routing, VXLAN-GPE, native IPv6 (via extension header), and IPv4.

- [Proof of
  Transit](https://tools.ietf.org/html/draft-brockners-proof-of-transit-03)
  defines mechanisms to securely prove that traffic transited the defined path.
  Several technologies such as traffic engineering, service function
  chaining, or policy based routing, are used to steer traffic through
  a specific, user-defined path.  The mechanisms described in this 
  document allow to securely verify whether all packets traversed all
  those nodes of a given path that they are supposed to visit.

# Code

## Dataplane implementation in FD.io/VPP

- The in-band OAM dataplane is implemented as a plugin in VPP:
  https://git.fd.io/cgit/vpp/tree/plugins/ioam-plugin.
- Documentation for the in-band OAM in VPP:
  - Overview user guide: https://docs.fd.io/vpp/16.12/md_plugins_ioam-plugin_ioam_Readme.html
  - Command line references:
    - https://docs.fd.io/vpp/16.12/plugins_ioam-plugin_ioam_encap.html
    - https://docs.fd.io/vpp/16.12/plugins_ioam-plugin_ioam_export.html
    - https://docs.fd.io/vpp/16.12/plugins_ioam-plugin_ioam_lib-pot.html
    - https://docs.fd.io/vpp/16.12/plugins_ioam-plugin_ioam_lib-trace.html
- FD.io wiki on in-band OAM configuration: https://wiki.fd.io/view/VPP/Command-line_Interface_(CLI)_Guide#Inline_IPv6_OAM_Commands.


## Dataplane implementation in Cisco IOS

The dataplane implementation in Cisco IOS is focused on IPv6 only.

- Documentation for in-band OAM is found in the [In-band OAM for
   IPv6](http://www.cisco.com/c/en/us/td/docs/ios-xml/ios/ipv6_nman/configuration/15-mt/ip6n-15-mt-book/ioam-ipv6.html) guide of the
"IPv6 Network Management Configuration Guide, Cisco IOS Release
15M&T". 
- The IOS software can be downloaded from [here](https://software.cisco.com/download/navigator.html?mdfid=282774227&flowid=78210).
IPv6 in-band OAM is supported on Cisco 1900/2900/3900/3900e Integrated Services Routers and vIOS on Cisco [VIRL] (http://virl.cisco.com/).

- To configure Proof-of-Transit for IOS, a series of 
  configuration parameters are needed. To help with
  computing the appropriate values, you can use the
  scripts provided here, see
https://github.com/CiscoDevNet/iOAM/tree/master/scripts/config_generator.
  Note that those apply mostly for the IOS dataplane application.
  For VPP, one can use the App for OpenDaylight.

## In-band OAM Controller Application in OpenDaylight

In-band OAM is reflected in two applications within OpenDaylight:

- *SFC*: In-band OAM "Proof of Transit" can be used as part of 
  OpenDaylight Service Function Chaining (SFC). The extensions to 
  ODL SFC enable ODL to serve POT control data (secrets etc.) required
  for POT. Full support is expected as part of the OpenDaylight Carbon release.

  - The following features are supported.
    - Utilizes Java-based libraries to generate iOAM parameters.
    - REST/Yang based APIs for north-bound to configure iOAM via OSS like postman or via programmatic triggers.
    - Augments and enhances base SFC application.
    - NETCONF/Yang based APIs for south-bound.
    - Feature capability is split into two modules to be installed: sfc-pot and sfc-pot-netconf-renderer.
  - The following are the feature commits.
    -  https://git.opendaylight.org/gerrit/#/c/40669/
    -  https://git.opendaylight.org/gerrit/#/c/48766/
    -  https://git.opendaylight.org/gerrit/#/c/49636/
    
- *Path-tracing*: Configuration application to enable and control in-band OAM tracing.
  The tracing application will be included in a future version of
  OpenDaylight as a separate module.  For now, it is dependent on the SFC git (but not SFC functionality) and is available as below.
    - https://github.com/CiscoDevNet/iOAM/tree/master/sfc
  
## In-band OAM Configuration Agent in Honeycomb

Honeycomb is a java-based agent that runs on the same host as a VPP instance, and exposes yang models via netconf or restconf to allow the management of that VPP instance from off box controllers like OpenDaylight.  The iOAM module in the Honeycomb agent helps exposes NETCONF and RESTCONF interfaces to allow iOAM trace and SFC verification features supported in the VPP instance behind it.

- *Path-tracing*: Configuration agent application to enable and control in-band OAM tracing.
  - The following are the feature commits.
    - https://gerrit.fd.io/r/#/c/3607/
    - https://gerrit.fd.io/r/#/c/4215/
- *Proof-of-Transit*: Configuration agent application to enable and control in-band OAM Proof of Transit.
  - The following are the feature commits.
    - https://gerrit.fd.io/r/#/c/4268

# Additional Resources

## Presentations

- *In-band OAM Overview*: http://www.slideshare.net/frankbrockners/nextgen-network-telemetry-is-within-your-packets-inband-oam

- *Proof of transit*: Securely verifying a path or service chain: http://www.slideshare.net/frankbrockners/proof-of-transit-securely-verifying-a-path-or-service-chain

## Blogs

- What if you had a trip-recorder for all your traffic at line rate performance? http://blogs.cisco.com/getyourbuildon/a-trip-recorder-for-all-your-traffic
- Verify my service chain! http://blogs.cisco.com/getyourbuildon/verify-my-service-chain 

## Demo videos on YouTube

Youtube In-Band OAM channel: https://www.youtube.com/channel/UC0WJOAKBTrftyosP590RrXw 

## References



 - [draft-brockners-inband-oam-requirements]
              Brockners, F., Bhandari, S., Dara, S., Pignataro, C.,
              Gedler, H., Leddy, J., Youell, S., Mozes, D., Mizrahi, T.,
              Lapukhov, P., Chang, R., "Requirements
              for in-situ OAM", October 2016.

 - [draft-brockners-inband-oam-data]
              Brockners, F., Bhandari, S., Dara, S., Pignataro, C.,
              Gedler, H., Leddy, J., Youell, S., Mozes, D., Mizrahi, T.,
              Lapukhov, P., Chang, R., "Data Formats for in-situ
              OAM", October 2016.

 - [draft-brockners-inband-oam-transport]
              Brockners, F., Bhandari, S., Dara, S., Pignataro, C.,
              Gedler, H., Leddy, J., Youell, S., Mozes, D., Mizrahi, T.,
              Lapukhov, P., Chang, R., "Encapsulations for in-situ
              OAM", October 2016.

 - [draft-brockners-proof-of-transit]
              Brockners, F., Bhandari, S., Dara, S., Pignataro, C.,
              Gedler, H., Leddy, J., Youell, S., Mozes, D., Mizrahi, T.,
              "Proof of transit", October 2016.

 - [SPUD]
              Hildebrand, J. and B. Trammell, "Substrate Protocol for
              User Datagrams (SPUD) Prototype", draft-hildebrand-spud-
              prototype-03 (work in progress), March 2015.

 - [P4]       Kim, , "P4: In-band Network Telemetry (INT)", September
              2015.

 - [I-D.lapukhov-dataplane-probe]
              Lapukhov, P. and Chang R., "Data-plane
              probe for in-band telemetry collection", draft-lapukhov-
              dataplane-probe-02 (work in progress), June 2016.

# Team:

Current team:
- Frank Brockners
- Shwetha Bhandari
- Srihari Raghavan
- Vengada Prasad Govindan 
- Ananthakrishnan Rajamani
- Akshaya Nadahalli
- Carlos Pignataro

Former team members include:
- Ranganathan T.S
- Karthik Babu Harichandra Babu 
- Sagar Srivatsav
- Manaswi G Reddy

# Current Status
In development

[draft-brockners-inband-oam-requirements]: https://tools.ietf.org/html/draft-brockners-inband-oam-requirements-03
[draft-brockners-proof-of-transit]: https://tools.ietf.org/html/draft-brockners-proof-of-transit-03
[draft-brockners-inband-oam-data]: https://tools.ietf.org/html/draft-brockners-inband-oam-data-07
[draft-brockners-inband-oam-transport]: https://tools.ietf.org/html/draft-brockners-inband-oam-transport-05
[p4]: http://p4.org/p4/inband-network-telemetry/
[SPUD]: https://tools.ietf.org/html/draft-hildebrand-spud-prototype-03
[fd.io]: http://fd.io
[RFC0791]: https://tools.ietf.org/html/rfc0791.html
[segment-routing]: https://tools.ietf.org/html/draft-ietf-spring-segment-routing-07
[segment-routing-header]: https://tools.ietf.org/html/draft-ietf-6man-segment-routing-header-02
[lisp-sr]: https://tools.ietf.org/html/draft-brockners-lisp-sr-02
[VPP ioam configuration]: https://wiki.fd.io/view/VPP/Command-line_Interface_(CLI)_Guide#Inline_IPv6_OAM_Commands
[I-D.lapukhov-dataplane-probe]: https://tools.ietf.org/html/draft-lapukhov-dataplane-probe-02
[RFC7276]: https://tools.ietf.org/html/rfc7276
