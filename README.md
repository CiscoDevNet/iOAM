# In-Situ OAM (IOAM)

In-situ OAM (sometimes also referred to as inband OAM, which was the term originally used before IETF chose "in-situ OAM" for the technology) is a technology to record operational
information in the packet while the packet traverses a path between two points
in the network. In-situ OAM is to complement current out-of-band OAM (sometimes also called "active" OAM) mechanisms based on ICMP or other types of probe
packets.
   
# Overview

"In-situ" OAM describes an approach to record OAM and telemetry information
within the data packet while the data packet traverses a network or a
particular network domain.  The term "in-situ" refers to the fact that the OAM
and telemetry data is carried within data packets rather than being sent
within packets specifically dedicated to OAM.  In-situ OAM mechanisms, which
are sometimes also referred to as embedded network telemetry are a current
topic of discussion.  In-band network telemetry has been defined for [P4].  The
SPUD prototype [SPUD] uses a similar logic that allows
network devices on the path between endpoints to participate explicitly in the
tube outside the end-to-end context.  Even the IPv4 route-record option defined
in [RFC0791] can be considered an in-situ OAM mechanism.  In-situ OAM
complements "out-of-band" mechanisms such as ping or traceroute, or more recent
active probing mechanisms, as described in [I-D.lapukhov-dataplane-probe].
In-situ OAM mechanisms can be leveraged where current out-of-band mechanisms do
not apply or do not offer the desired characteristics or requirements, such as
proving that a certain set of traffic takes a pre-defined path,
strict congruency is desired, checking service level agreements for
the live data traffic, detailed statistics on traffic distribution
paths in networks that distribute traffic across multiple paths, or
scenarios where probe traffic is potentially handled differently from
regular data traffic by the network devices.  [RFC7276] presents an
overview of OAM tools.

Compared to probably the most basic example of "in-situ OAM" which is
IPv4 route recording [RFC0791], an in-situ OAM approach has the
following capabilities:

- A flexible data format to allow different types of information to
  be captured as part of an in-situ OAM operation, including not
  only path tracing information, but additional operational and
  telemetry information such as timestamps, sequence numbers, or
  even generic data such as queue size, geo-location of the node
  that forwarded the packet, etc.

- A data format to express node as well as link identifiers to
  record the path a packet takes with a fixed amount of added data.

- The ability to detect whether any nodes were skipped while
  recording in-situ OAM information (i.e., in-situ OAM is not
  supported or not enabled on those nodes).
   
- The ability to actively process information in the packet, for
  example to prove in a cryptographically secure way that a packet
  really took a pre-defined path using some traffic steering method
  such as service chaining or traffic engineering.

- The ability to include OAM data beyond simple path information,
  such as timestamps or even generic data of a particular use case.

- The ability to include OAM data in various different transport
  protocols.

A detailed description in-situ OAM concepts, capabilities,
data-formats and transport encapsulations can be found in the
following IETF internet drafts:

- [Requirements for In-band
  OAM](https://tools.ietf.org/html/draft-brockners-inband-oam-requirements-03)
  discusses the motivation and requirements for including
  specific operational and telemetry information into data packets
  while the data packet traverses a path between two points in the
  network.

- [Data Formats for In-Situ
  OAM](https://tools.ietf.org/html/draft-ietf-ippm-ioam-data-06)
  discusses the data types and data formats for in-situ OAM data
  records.

- Encapsulations for IOAM data. These drafts describe how IOAM data fields
  are encapsulated in "parent" protocols: 
   - [NSH encapsulation for IOAM data](https://tools.ietf.org/html/draft-ietf-sfc-ioam-nsh-01)
   - [Geneve encapsulation for IOAM data](https://tools.ietf.org/html/draft-brockners-ippm-ioam-geneve-02)
   - [VXLAN-GPE encapsulation for IOAM data](https://tools.ietf.org/html/draft-brockners-ippm-ioam-vxlan-gpe-02)
   - [IPv6 encapsulation for IOAM data](https://tools.ietf.org/html/draft-ioametal-ippm-6man-ioam-ipv6-options-01)
   - [SRv6 encapsulation for IOAM data](https://tools.ietf.org/html/draft-ali-spring-ioam-srv6-01)
   - [Encapsulations for protocols which use Ethertype for next protocol (e.g. GRE, Geneve)](https://tools.ietf.org/html/draft-weis-ippm-ioam-eth-01). Note: For IPv4, it
  is suggested to use a GRE header sequenced in with an IOAM header to carry IOAM data fields.

- [Proof of
  Transit](https://tools.ietf.org/html/draft-ietf-sfc-proof-of-transit-02)
  defines mechanisms to securely prove that traffic transited the defined path.
  Several technologies such as traffic engineering, service function
  chaining, or policy based routing, are used to steer traffic through
  a specific, user-defined path.  The mechanisms described in this 
  document allow to securely verify whether all packets traversed all
  those nodes of a given path that they are supposed to visit.

- [Export of IOAM data in raw format](https://www.ietf.org/id/draft-spiegel-ippm-ioam-rawexport-00.txt) describes how IOAM information can be exported in raw, i.e. uninterpreted, format from network devices to systems, such as monitoring or analytics systems using IPFIX.

A wide variety of use-cases can leverage IOAM:

* Service/Quality Assurance - Fabric OAM
  * Prove traffic SLAs, as opposed to probe-traffic SLAs; Overlay/Underlay
  * Service/Path Verification (Proof of Transit) - prove that
    traffic follows a pre-defined path
* Micro-Service/NFV deployments
  * Smart service selection based on network criteria - "M-Anycast"
    (intelligent micro-service selection and load-balancing):
    https://github.com/CiscoDevNet/iOAM/tree/master/M-Anycast.
    For the VPP implementation of m-anycast, see https://docs.fd.io/vpp/17.04/ioam_manycast_doc.html
* Operations Support - Fabric Visibility
  * Network Fault Detection and Fault Isolation through
    efficient network probing: By using IOAM's loopback option
    an issue can be identified within a single packet roundtrip time.
  * Path Tracing - debug ECMP, brown-outs, network delays
  * Derive Traffic Matrix
  * Custom/Service Level Telemetry 

# Code

## Dataplane implementation in FD.io/VPP

- The in-situ OAM dataplane is implemented as a plugin in VPP:
  https://git.fd.io/cgit/vpp/tree/plugins/ioam-plugin.
- Documentation for the in-situ OAM in VPP:
  - Overview user guide: https://docs.fd.io/vpp/16.12/md_plugins_ioam-plugin_ioam_Readme.html
  - Command line references:
    - https://docs.fd.io/vpp/17.04/ioam_plugin_doc.html
    - https://docs.fd.io/vpp/16.12/plugins_ioam-plugin_ioam_encap.html
    - https://docs.fd.io/vpp/16.12/plugins_ioam-plugin_ioam_export.html
    - https://docs.fd.io/vpp/16.12/plugins_ioam-plugin_ioam_lib-pot.html
    - https://docs.fd.io/vpp/16.12/plugins_ioam-plugin_ioam_lib-trace.html
- FD.io wiki on in-situ OAM configuration: https://wiki.fd.io/view/VPP/Command-line_Interface_(CLI)_Guide#Inline_IPv6_OAM_Commands.

## Dataplane implementation in the Linux Kernel

The University of Liege in Belgium created an IOAM implementation for the Linux Kernel:
https://github.com/IurmanJ/kernel_ipv6_ioam

## Dataplane implementation in Cisco IOS

The dataplane implementation in Cisco IOS is focused on IPv6 only.

- Documentation for in-situ OAM is found in the [In-band OAM for
   IPv6](http://www.cisco.com/c/en/us/td/docs/ios-xml/ios/ipv6_nman/configuration/15-mt/ip6n-15-mt-book/ioam-ipv6.html) guide of the
"IPv6 Network Management Configuration Guide, Cisco IOS Release
15M&T". 
- The IOS software can be downloaded from [here](https://software.cisco.com/download/navigator.html?mdfid=282774227&flowid=78210).
IPv6 in-situ OAM is supported on Cisco 1900/2900/3900/3900e Integrated Services Routers and vIOS on Cisco [VIRL] (http://virl.cisco.com/).

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
    
- *Path-tracing*: Configuration application to enable and control in-situ OAM tracing.
  The tracing application will be included in a future version of
  OpenDaylight as a separate module.  For now, it is dependent on the SFC git (but not SFC functionality) and is available as below.
    - https://github.com/CiscoDevNet/iOAM/tree/master/sfc
  
## In-band OAM Configuration Agent in Honeycomb

Honeycomb is a java-based agent that runs on the same host as a VPP instance, and exposes yang models via netconf or restconf to allow the management of that VPP instance from off box controllers like OpenDaylight.  The iOAM module in the Honeycomb agent helps exposes NETCONF and RESTCONF interfaces to allow iOAM trace and SFC verification features supported in the VPP instance behind it.

- *Path-tracing*: Configuration agent application to enable and control in-situ OAM tracing.
  - The following are the feature commits.
    - https://gerrit.fd.io/r/#/c/3607/
    - https://gerrit.fd.io/r/#/c/4215/
- *Proof-of-Transit*: Configuration agent application to enable and control in-situ OAM Proof of Transit.
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
              for inband OAM", October 2016. (no longer maintained.
              Draft served the purpose of fueling the discussion at IETF).

 - [draft-ietf-ippm-ioam-data]
              Brockners, F., Bhandari, S., Dara, S., Pignataro, C.,
              Gedler, H., Leddy, J., Youell, S., Mozes, D., Mizrahi, T.,
              Lapukhov, P., Chang, R., "Data Formats for in-situ
              OAM", July 2019.

 - [draft-ioametal-ippm-6man-ioam-ipv6-options]
	      S. Bhandari et al., "In-situ OAM IPv6 Options", March 2019.

 - [draft-ioametal-ippm-6man-ioam-ipv6-deployment]
	      S. Bhandari et al., "Deployment Considerations for In-situ OAM with IPv6 Options", March 2019.

 - [draft-ietf-sfc-ioam-nsh]
              Brockners et al., "NSH Encapsulation for In-situ OAM Data",
              March 2019.

 - [draft-brockners-ippm-ioam-vxlan-gpe]
              Brockners et al., "VXLAN-GPE Encapsulation for In-situ OAM Data",
              July 2019.

 - [draft-brockners-ippm-ioam-geneve]
              Brockners et al., "Geneve Encapsulation for In-situ OAM Data",
              March 2019.

 - [draft-weis-ippm-ioam-eth] 
              Weis et al., "GRE Encapsulation for In-situ OAM Data",
              March 2019.

 - [draft-spiegel-ippm-ioam-rawexport]
              Spiegel et al., "In-situ OAM raw data export with IPFIX",
              July 2019.

 - [draft-brockners-inband-oam-transport]
              Brockners, F., Bhandari, S., Dara, S., Pignataro, C.,
              Gedler, H., Leddy, J., Youell, S., Mozes, D., Mizrahi, T.,
              Lapukhov, P., Chang, R., "Encapsulations for in-situ
              OAM", October 2016. (replaced by individual encapsulation
              drafts - see above).

 - [draft-ietf-sfc-proof-of-transit]
              Brockners, F., Bhandari, S., Dara, S., Pignataro, C.,
              Gedler, H., Leddy, J., Youell, S., Mozes, D., Mizrahi, T.,
              "Proof of transit", March 2019.

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

[draft-ioametal-ippm-6man-ioam-ipv6-options]:https://tools.ietf.org/html/draft-ioametal-ippm-6man-ioam-ipv6-options-02
[draft-ioametal-ippm-6man-ioam-ipv6-deployment]:https://tools.ietf.org/html/draft-ioametal-ippm-6man-ioam-ipv6-deployment-01
[draft-brockners-inband-oam-requirements]: https://tools.ietf.org/html/draft-brockners-inband-oam-requirements-03
[draft-ietf-sfc-proof-of-transit]: https://tools.ietf.org/html/draft-ietf-sfc-proof-of-transit-02
[draft-ietf-ippm-ioam-data]: https://tools.ietf.org/html/draft-ietf-ippm-ioam-data-02
[draft-brockners-inband-oam-transport]: https://tools.ietf.org/html/draft-brockners-inband-oam-transport-05
[draft-spiegel-ippm-ioam-rawexport]:https://www.ietf.org/id/draft-spiegel-ippm-ioam-rawexport-02.txt
[draft-ietf-sfc-ioam-nsh]:https://tools.ietf.org/html/draft-ietf-sfc-ioam-nsh-01
[draft-brockners-ippm-ioam-vxlan-gpe]:https://tools.ietf.org/html/draft-brockners-ippm-ioam-vxlan-gpe-00
[draft-brockners-ippm-ioam-geneve]:https://tools.ietf.org/html/draft-brockners-ippm-ioam-geneve-00
[draft-weis-ippm-ioam-eth]:https://tools.ietf.org/html/draft-weis-ippm-ioam-eth-01
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
