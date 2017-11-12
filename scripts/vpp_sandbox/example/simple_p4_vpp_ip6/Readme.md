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

DBGvpp# show trace
------------------- Start of thread 0 vpp_main -------------------
Packet 1
01:15:45:525612: af-packet-input
  af_packet: hw_if_index 1 next-index 4
    tpacket2_hdr:
      status 0x20000001 len 78 snaplen 78 mac 66 net 80
      sec 0x5a0767ed nsec 0xdd68d5f vlan 0 vlan_tpid 0
01:15:45:525638: ethernet-input
  IP6: c6:2b:09:15:47:17 -> 01:02:04:03:05:06
01:15:45:525686: ip6-input
  UDP: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 64, payload length 24
  UDP: 1234 -> 4321
    length 24, checksum 0x3642
01:15:45:525695: ip6-inacl
  INACL: sw_if_index 1, next_index 1, table 0, offset 192
01:15:45:525708: ip6-add-hop-by-hop
  IP6_ADD_HOP_BY_HOP: next index 1
01:15:45:525713: ip6-lookup
  fib 0 dpo-idx 4 flow hash: 0x00000000
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 64, payload length 64
01:15:45:525728: ip6-hop-by-hop
  IP6_HOP_BY_HOP: next index 5 len 40 traced 40  (Pre-alloc) Trace Type 0x9 , 2 elts left
    [0] ttl 0x0 node id 0x0 ts 0x0 

    [1] ttl 0x0 node id 0x0 ts 0x0 

    [2] ttl 0x40 node id 0x1 ts 0x5a0767f6 

SeqNo = 0x7
01:15:45:525738: ip6-rewrite
  tx_sw_if_index 2 adj-idx 4 : ipv6 via db01::2 host-l_a2: 16bfab540b6402fe27ae1b4986dd flow hash: 0x00000000
  00000000: 16bfab540b6402fe27ae1b4986dd600000000040003fdb000000000000000000
  00000020: 000000000002db05000000000000000000000000000211043b1a090200000000
  00000040: 000000000000000000000000400000015a0767f61d06000000000007000004d2
  00000060: 10e10018364248656c6c6f2066726f6d20486f737431000000000000
01:15:45:525745: host-l_a2-output
  host-l_a2
  IP6: 02:fe:27:ae:1b:49 -> 16:bf:ab:54:0b:64
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 63, payload length 64


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
Packet 3

00:20:00:308335: af-packet-input
  af_packet: hw_if_index 1 next-index 4
    tpacket2_hdr:
      status 0x20000001 len 142 snaplen 142 mac 66 net 80
      sec 0x5a0767ed nsec 0xee70608 vlan 0 vlan_tpid 0
00:20:00:308366: ethernet-input
  IP6: 62:cb:aa:fb:a2:f4 -> 02:fe:2c:49:62:2b
00:20:00:308392: ip6-input
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 60, payload length 88
00:20:00:308407: ip6-inacl
  INACL: sw_if_index 1, next_index 1, table 0, offset 192
00:20:00:308429: ip6-lookup
  fib 0 dpo-idx 5 flow hash: 0x00000000
  IP6_HOP_BY_HOP_OPTIONS: db00::2 -> db05::2
    tos 0x00, flow label 0x0, hop limit 60, payload length 88
00:20:00:308461: ip6-hop-by-hop
  IP6_HOP_BY_HOP: next index 12 len 64 traced 64  (Incr) Trace Type 0x9 , 2 max elts
    [0] ttl 0x3d node id 0x4 ts 0x81c0b6b 

    [1] ttl 0x3f node id 0x2 ts 0xaff214c 

  (Pre-alloc) Trace Type 0x9 , 0 elts left
    [0] ttl 0x3c node id 0x5 ts 0x5a0767ef 

    [1] ttl 0x3e node id 0x3 ts 0x5a076801 

    [2] ttl 0x40 node id 0x1 ts 0x5a0767f6 



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


