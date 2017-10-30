# Sample Topology and Configs

To create a topology as shown below, follow below stpes:

![Topology](./Topology-simple_ip6.png?raw=true "Topology")
## Prereq:
* Pull the VPP code
```

git clone https://gerrit.fd.io/r/p/vpp.git
 
make install-dep
 
make bootstrap
 
cd build-root
 
make V=0 PLATFORM=vpp TAG=vpp install-deb
 
sudo dpkg -i *.deb
```
* Delete dpdk plugin:

```
sudo rm -rf /usr/lib/vpp_plugins/dpdk*.so
```
this will save space for the vpp created

* Pull this repo:
``` 
git clone https://github.com/CiscoDevNet/iOAM.git
```

* Install LXC and copy templete file.

		sudo apt-get install -y lxc lxctl lxc-templates util-linux
		 cp <git_checkout_path>/iOAM/scripts/vpp_sandbox/lxc-vpp-ext /usr/share/lxc/templates/lxc-vpp-ext

* Edit /usr/share/lxc/templetes/lxc-vpp-ext file:
   Look for below lines:

		# Add VPP Specific mounts here
		#lxc.mount.entry = <Local directory> scratch none ro,bind 0 0

   Uncomment lxc.mount.entry line and replace "\<Local directory\>" by path to <git_checkout_path>/scripts/vpp_sandbox

 
## Steps to running this example


* Start the network of containers:
```

   cd <git_checkout_path>/iOAM/scripts/vpp_sandbox/
   sudo ./example/simple-ip6/start.sh 
```

* Open 2 shells. 
```
   4.1 Shell 1: sudo lxc-attach -n a
   4.2 Shell 1: telnet 0 5002; trace add af-packet-input 20 then quit
   4.3 Shell 1: sudo lxc-attach -n c
   4.3 Shell 1: telnet 0 5002; trace add af-packet-input 20
   4.4 Shell 2:  Connect to host1 and start ping to host2 - lxc-attach -n host1, ping6 db03::2
   4.5 Shell 1: show trace, quit
   4.6 Shell 1: sudo lxc-attach -n a, telnet 0 5002, show trace
```
* To delete the topology and clear all containers:
```

   cd <git_checkout_path>/iOAM/scripts/vpp_sandbox/
   sudo ./example/simple-ip6/kill_simpleip6.sh 
```

### Sample output

* After ping, vpp trace in a:

```
VirtualBox:~/pinger/iOAM/scripts/vpp_sandbox$ sudo lxc-attach -n a
VirtualBox:~/pinger/iOAM/scripts/vpp_sandbox# telnet 0 5002
Trying 0.0.0.0...
Connected to 0.
Escape character is '^]'.
    _______    _        _   _____  ___ 
 __/ __/ _ \  (_)__    | | / / _ \/ _ \
 _/ _// // / / / _ \   | |/ / ___/ ___/
 /_/ /____(_)_/\___/   |___/_/  /_/    

DBGvpp# show trace
------------------- Start of thread 0 vpp_main -------------------
Packet 1

00:51:30:188630: af-packet-input
  af_packet: hw_if_index 1 next-index 4
    tpacket2_hdr:
      status 0x20000001 len 118 snaplen 118 mac 66 net 80
      sec 0x59b652f5 nsec 0xe3b17ba vlan 0 vlan_tpid 0
00:51:30:188663: ethernet-input
  IP6: 7a:da:8f:d7:f6:9c -> 02:fe:89:ba:7f:62
00:51:30:188754: ip6-input
  ICMP6: db00::2 -> db03::2
    tos 0x00, flow label 0x4a6d0, hop limit 64, payload length 64
  ICMP echo_request checksum 0xed23
00:51:30:188767: ip6-inacl
  INACL: sw_if_index 1, next_index 1, table 0, offset 192
00:51:30:188785: ip6-add-hop-by-hop
  IP6_ADD_HOP_BY_HOP: next index 1
00:51:30:188793: ip6-lookup
  fib 0 dpo-idx 4 flow hash: 0x00000000
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db03::2
    tos 0x00, flow label 0x4a6d0, hop limit 64, payload length 104
00:51:30:188812: ip6-hop-by-hop
  IP6_HOP_BY_HOP: next index 5 len 40 traced 40  Trace Type 0x9 , 2 elts left
    [0] ttl 0x0 node id 0x0 ts 0x0 

    [1] ttl 0x0 node id 0x0 ts 0x0 

    [2] ttl 0x40 node id 0x1 ts 0x4bb1da27 

SeqNo = 0x9
00:51:30:188826: ip6-rewrite
  tx_sw_if_index 2 adj-idx 4 : ipv6 via db01::2 host-l_a2: 02fe2e65644602fe375884a486dd flow hash: 0x00000000
  00000000: 02fe2e65644602fe375884a486dd6004a6d00068003fdb000000000000000000
  00000020: 000000000002db0300000000000000000000000000023a043b1a090200000000
  00000040: 000000000000000000000000400000014bb1da271d0600000000000900008000
  00000060: ed2300340001f552b659000000006ea4030000000000101112131415
00:51:30:188836: host-l_a2-output
  host-l_a2
  IP6: 02:fe:37:58:84:a4 -> 02:fe:2e:65:64:46
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db03::2
    tos 0x00, flow label 0x4a6d0, hop limit 63, payload length 104
 ```

* After ping, vpp trace in c:
```
VirtualBox:~/pinger/iOAM/scripts/vpp_sandbox$ sudo lxc-attach -n c
VirtualBox:~/pinger/iOAM/scripts/vpp_sandbox# telnet 0 5002
Trying 0.0.0.0...
Connected to 0.
Escape character is '^]'.
    _______    _        _   _____  ___ 
 __/ __/ _ \  (_)__    | | / / _ \/ _ \
 _/ _// // / / / _ \   | |/ / ___/ ___/
 /_/ /____(_)_/\___/   |___/_/  /_/    

DBGvpp# show trace
------------------- Start of thread 0 vpp_main -------------------
Packet 1

00:01:34:127464: af-packet-input
  af_packet: hw_if_index 1 next-index 4
    tpacket2_hdr:
      status 0x20000001 len 158 snaplen 158 mac 66 net 80
      sec 0x59b64747 nsec 0x29326d0 vlan 0 vlan_tpid 0
00:01:34:127488: ethernet-input
  IP6: 02:fe:cf:e6:ba:24 -> 02:fe:a5:47:ea:cb
00:01:34:127555: ip6-input
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db03::2
    tos 0x00, flow label 0x4a6d0, hop limit 62, payload length 104
00:01:34:127568: ip6-lookup
  fib 0 dpo-idx 5 flow hash: 0x00000000
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db03::2
    tos 0x00, flow label 0x4a6d0, hop limit 62, payload length 104
00:01:34:127585: ip6-hop-by-hop
  IP6_HOP_BY_HOP: next index 5 len 40 traced 40  Trace Type 0x9 , 0 elts left
    [0] ttl 0x3e node id 0x3 ts 0x991d2a81 

    [1] ttl 0x3f node id 0x2 ts 0x991da42e 

    [2] ttl 0x40 node id 0x1 ts 0x991e0361 


    SeqNo = 0xa
00:01:34:127596: ip6-rewrite
  tx_sw_if_index 2 adj-idx 5 : ipv6 via db03::2 host-l_c2: a67fd05a5d7e02fe7c71bdd186dd flow hash: 0x00000000
  00000000: a67fd05a5d7e02fe7c71bdd186dd6004a6d00068003ddb000000000000000000
  00000020: 000000000002db0300000000000000000000000000023a043b1a09003e000003
  00000040: 991d2a813f000002991da42e40000001991e03611d0600000000000600008000
  00000060: dd2e003300014747b659000000002fa6000000000000101112131415
00:01:34:127609: host-l_c2-output
  host-l_c2
  IP6: 02:fe:7c:71:bd:d1 -> a6:7f:d0:5a:5d:7e
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db03::2
```

