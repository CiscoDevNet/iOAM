# Sample Topology and Configs

To create a topology as shown below, follow below stpes:


![Topology](./p4_vpp_topology.png?raw=true "Topology")
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
* Pull this repo only if you are not using the VM provided !!:
``` 
git clone https://github.com/manishjangid/tutorials.git
2. cd tutorials
3. git checkout p4_programs
4. cd my_exercises.

```

* 
* Update the Ioam P4 GIT
cd /home/osboxes/p4git/p4_tutorials/tutorials/my_exercises
git pull

and do compile the code again :) , its pretty simple 
cd /home/osboxes/p4git/p4_tutorials/tutorials/my_exercises/ipv6_examples/ioam/vpp_p4/
./run.sh 


And this will compile it for you , else you can manually do it also by going in build folder and running this 

p4c-bm2-ss --p4v 16 "ioam_demo.p4" -o "ioam_demo.p4.json"



Once its compile , you are good to run P4 Demo 



* Install LXC and copy templete file.

		sudo apt-get install -y lxc lxctl lxc-templates util-linux
		 cp <git_checkout_path>/iOAM/scripts/vpp_sandbox/lxc-vpp-p4-ext /usr/share/lxc/templates/lxc-vpp-p4-ext


 
## Steps to running this example


* Start the network of containers:
```

   cd /home/osboxes/ioam/iOAM/scripts/vpp_sandbox
   sudo ./example/simple_p4_vpp_ip6/start.sh 
```

* Open 15 shells. 
```
Shell 1: sudo lxc-attach -n a
Shell 2: telnet 0 5002; trace add af-packet-input 20 then quit
Shell 3: sudo lxc-attach -n b
Shell 4: telnet 0 5002; trace add af-packet-input 20
Shell 5: sudo lxc-attach -n c
Shell 6: telnet 0 5002; trace add af-packet-input 20
Shell 7: sudo lxc-attach -n S1
	And run : 
	export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib
	simple_switch -i 1@l_S11 -i 2@l_S12 --pcap --thrift-port 9090 --nanolog ipc:///tmp/bm-0-log.ipc /home/osboxes/p4git/p4_tutorials/tutorials/my_exercises/ipv6_examples/ioam/vpp_p4/build/ioam_demo.p4.json --log-console –debugger
Shell 8: sudo lxc-attach -n S1
        And run :
	export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib
        /home/osboxes/p4git/p4_tutorials/tutorials/my_exercises/ipv6_examples/ioam/vpp_p4/configure_switch_entries.py /home/osboxes/p4git/p4_tutorials/tutorials/my_exercises/ipv6_examples/ioam/vpp_p4/s1-commands.txt 9090
Shell 9: sudo lxc-attach -n S2
	And run : 
	export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib
	simple_switch -i 1@l_S21 -i 2@l_S22 --pcap --thrift-port 9090 --nanolog ipc:///tmp/bm-0-log.ipc /home/osboxes/p4git/p4_tutorials/tutorials/my_exercises/ipv6_examples/ioam/vpp_p4/build/ioam_demo.p4.json --log-console –debugger
Shell 10: sudo lxc-attach -n S2
       And run :
	export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib
/home/osboxes/p4git/p4_tutorials/tutorials/my_exercises/ipv6_examples/ioam/vpp_p4/configure_switch_entries.py /home/osboxes/p4git/p4_tutorials/tutorials/my_exercises/ipv6_examples/ioam/vpp_p4/s2-commands.txt 9090

Shell 11:  Connect to host1 
sudo lxc-attach -n host1

and do 

cd /home/osboxes/p4git/p4_tutorials/tutorials/my_exercises/ipv6_examples/ioam/vpp_p4 
./send_from_h1.py db05::2 "Hello from Host1"



Shell 12:  Connect to host2 
sudo lxc-attach -n host2


cd /home/osboxes/p4git/p4_tutorials/tutorials/my_exercises/ipv6_examples/ioam/vpp_p4 
./receive.py l_host21

Shell 13: sudo lxc-attach -n a, telnet 0 5002, show trace
Shell 14: sudo lxc-attach -n b, telnet 0 5002, show trace
Shell 15: sudo lxc-attach -n c, telnet 0 5002, show trace

### Sample output
```
* Packet which was sent from Host1

bash-4.3# ./send_from_h1.py db05::2 "Hello" 
addr is :db05::2
addr is  [(10, 1, 6, '', ('db05::2', 0, 0, 0)), (10, 2, 17, '', ('db05::2', 0, 0, 0)), (10, 3, 0, '', ('db05::2', 0, 0, 0))]
sending on interface l_host11 to db05::2
###[ Ethernet ]###
  dst       = 01:02:04:03:05:06
  src       = ca:43:3b:e3:db:88
  type      = 0x86dd
###[ IPv6 ]###
     version   = 6L
     tc        = 0L
     fl        = 0L
     plen      = 13
     nh        = UDP
     hlim      = 64
     src       = db00::2
     dst       = db05::2
###[ UDP ]###
        sport     = 1234
        dport     = 4321
        len       = 13
        chksum    = 0x1045
###[ Raw ]###
           load      = 'Hello'
bash-4.3# 



* After sending packet, vpp trace in a:

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

vpp# show trace
------------------- Start of thread 0 vpp_main -------------------
Packet 1

00:12:08:577086: af-packet-input
  af_packet: hw_if_index 1 next-index 4
    tpacket2_hdr:
      status 0x1 len 67 snaplen 67 mac 66 net 80
      sec 0x5a139fb3 nsec 0x2a3e9410 vlan 0 vlan_tpid 0
00:12:08:577101: ethernet-input
  IP6: ca:43:3b:e3:db:88 -> 01:02:04:03:05:06
00:12:08:577114: ip6-input
  UDP: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 64, payload length 13
  UDP: 1234 -> 4321
    length 13, checksum 0x1045
00:12:08:577117: ip6-inacl
  INACL: sw_if_index 1, next_index 1, table 0, offset 192
00:12:08:577121: ip6-add-hop-by-hop
  IP6_ADD_HOP_BY_HOP: next index 1
00:12:08:577122: ip6-lookup
  fib 0 dpo-idx 4 flow hash: 0x00000000
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 64, payload length 53
00:12:08:577128: ip6-hop-by-hop
  IP6_HOP_BY_HOP: next index 5 len 40 traced 40  (Pre-alloc) Trace Type 0x9 , 2 elts left
    [0] ttl 0x0 node id 0x0 ts 0x0 

    [1] ttl 0x0 node id 0x0 ts 0x0 

    [2] ttl 0x40 node id 0x1 ts 0x5a12e136 

SeqNo = 0x2
00:12:08:577131: ip6-rewrite
  tx_sw_if_index 2 adj-idx 4 : ipv6 via db01::2 host-l_a2: 02533493496b02fe27ae1b4986dd flow hash: 0x00000000
  00000000: 02533493496b02fe27ae1b4986dd600000000035003fdb000000000000000000
  00000020: 000000000002db05000000000000000000000000000211043b1a090200000000
  00000040: 000000000000000000000000400000015a12e1361d06000000000002000004d2
  00000060: 10e1000d104548656c6c6f0000000000000000000000000000000000
00:12:08:577135: host-l_a2-output
  host-l_a2
  IP6: 02:fe:27:ae:1b:49 -> 02:53:34:93:49:6b
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 63, payload length 53

vpp#     

* At Switch S1 , which is a P4 Enabled Switch , we insert the Incremental Hop-By-Hop Header . At interface S11, There was no incremental
  Header , it had only the Pre-allocated header inserted by the VPP at node a.
```

![S1-S11_Snapshot](./snapshots/S1-S11_Sanpshot.png?raw=true "Wireshark-S1-S11_Snapshot")

At interface S12 , P4 had inserted the new incremental Hop-By-Hop Header along with Pre-allocated header inserted by the VPP at node a

![S1-S12_Snapshot](./snapshots/S1-S12_Sanpshot.png?raw=true "Wireshark-S1-S12_Snapshot")


* Vpp trace in b:
```
vpp# clear trace
vpp# trace add af-packet-input 50
vpp# 
vpp# show trace
------------------- Start of thread 0 vpp_main -------------------
Packet 1

00:11:38:301254: af-packet-input
  af_packet: hw_if_index 1 next-index 4
    tpacket2_hdr:
      status 0x1 len 123 snaplen 123 mac 66 net 80
      sec 0x5a139fb3 nsec 0x2a5ebf7e vlan 0 vlan_tpid 0
00:11:38:301266: ethernet-input
  IP6: 02:53:34:93:49:6b -> 02:fe:db:7e:c9:0a
00:11:38:301282: ip6-input
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 62, payload length 69
00:11:38:301285: ip6-lookup
  fib 0 dpo-idx 5 flow hash: 0x00000000
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 62, payload length 69
00:11:38:301290: ip6-hop-by-hop
  IP6_HOP_BY_HOP: next index 5 len 56 traced 56  (Incr) Trace Type 0x9 , 1 max elts
    [0] ttl 0x3f node id 0x2 ts 0x73ac989b 

  (Pre-alloc) Trace Type 0x9 , 1 elts left
    [0] ttl 0x0 node id 0x0 ts 0x0 

    [1] ttl 0x3e node id 0x3 ts 0x5a12e136 

    [2] ttl 0x40 node id 0x1 ts 0x5a12e136 


    unrecognized option 29 length 6
00:11:38:301295: ip6-rewrite
  tx_sw_if_index 2 adj-idx 5 : ipv6 via db03::2 host-l_b2: 727cfef3c2a402fe4e28dd9a86dd flow hash: 0x00000000
  00000000: 727cfef3c2a402fe4e28dd9a86dd600000000045003ddb000000000000000000
  00000020: 000000000002db05000000000000000000000000000211063d0e09013f000002
  00000040: 73ac989b000000003b1a090100000000000000003e0000035a12e13640000001
  00000060: 5a12e1361d06000000000002000004d210e1000d104548656c6c6f00
00:11:38:301306: host-l_b2-output
  host-l_b2
  IP6: 02:fe:4e:28:dd:9a -> 72:7c:fe:f3:c2:a4
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 61, payload length 69

vpp#  


* At Switch S2 , which is a P4 Enabled Switch , we insert the Incremental Hop-By-Hop Header . At interface S21, There was one incremental
  Header which was added at the Switch S1 , also it has the Pre-allocated header inserted by the VPP at node a and node b.
```

![S1-S21_Snapshot](./snapshots/S1-S21_Sanpshot.png?raw=true "Wireshark-S1-S21_Snapshot")

At interface S22 , P4 had inserted the another incremental Hop-By-Hop Header along with Pre-allocated header inserted by the VPP at node a and node b. 

![S1-S22_Snapshot](./snapshots/S1-S22_Sanpshot.png?raw=true "Wireshark-S1-S22_Snapshot")


* Vpp trace in c:
```

vpp# 
vpp# show trace
------------------- Start of thread 0 vpp_main -------------------
Packet 1

00:11:08:273987: af-packet-input
  af_packet: hw_if_index 1 next-index 4
    tpacket2_hdr:
      status 0x1 len 131 snaplen 131 mac 66 net 80
      sec 0x5a139fb3 nsec 0x2a6bce16 vlan 0 vlan_tpid 0
00:11:08:273997: ethernet-input
  IP6: 72:7c:fe:f3:c2:a4 -> 02:fe:2c:49:62:2b
00:11:08:274001: ip6-input
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 60, payload length 77
00:11:08:274005: ip6-inacl
  INACL: sw_if_index 1, next_index 1, table 0, offset 192
00:11:08:274009: ip6-lookup
  fib 0 dpo-idx 5 flow hash: 0x00000000
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 60, payload length 77
00:11:08:274013: ip6-hop-by-hop
  IP6_HOP_BY_HOP: next index 12 len 64 traced 64  (Incr) Trace Type 0x9 , 2 max elts
    [0] ttl 0x3d node id 0x4 ts 0x743f4b17 

    [1] ttl 0x3f node id 0x2 ts 0x73ac989b 

  (Pre-alloc) Trace Type 0x9 , 0 elts left
    [0] ttl 0x3c node id 0x5 ts 0x5a12e136 

    [1] ttl 0x3e node id 0x3 ts 0x5a12e136 

    [2] ttl 0x40 node id 0x1 ts 0x5a12e136 


    unrecognized option 29 length 6
00:11:08:274015: ip6-pop-hop-by-hop
  IP6_POP_HOP_BY_HOP: next index 5
00:11:08:274018: ip6-rewrite
  tx_sw_if_index 2 adj-idx 5 : ipv6 via db05::2 host-l_c2: fe371265f1e602fe2de7cddb86dd flow hash: 0x00000000
  00000000: fe371265f1e602fe2de7cddb86dd60000000000d113bdb000000000000000000
  00000020: 000000000002db05000000000000000000000000000204d210e1000d10454865
  00000040: 6c6c6f0000000000000000000000000000000000000000000000000000000000
  00000060: 00000000000000000000000000000000000000000000000000000000
00:11:08:274020: host-l_c2-output
  host-l_c2
  IP6: 02:fe:2d:e7:cd:db -> fe:37:12:65:f1:e6
  UDP: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 59, payload length 13
  UDP: 1234 -> 4321
    length 13, checksum 0x1045



vpp#   

