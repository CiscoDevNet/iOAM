# Sample Topology and Configs

To create a topology as shown below, follow below stpes:

![Alt text](./topology.png?raw=true "Topology")

Steps:

1. Pull the VPP Code
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
2. Install LXC and copy templete file.

		sudo apt-get install -y lxc lxctl lxc-templates util-linux
		cp <git_checkout_path>/iOAM/scripts/vpp_sandbox/lxc-vpp-ext /usr/share/lxc/templates/lxc-vpp-ext

3. Edit /usr/share/lxc/templetes/lxc-vpp-ext file:
``` 
   Look for below lines:
   	# Uncomment the following line
	#lxc.mount.entry = /dev dev none ro,bind 0 0

	# Add VPP Specific mounts here
	#lxc.mount.entry = <Local directory> scratch none ro,bind 0 0
	eg: lxc.mount.entry = <git_checkout_path>/iOAM/scripts/vpp_sandbox scratch none ro,bind 0 0

   	Uncomment lxc.mount.entry line and replace <Local directory> by path up to 
	<git_checkout_path>/scripts/vpp_sandbox
```
4. Start the network of containers and setup the given topology:
```

   cd <git_checkout_path>/iOAM/scripts/vpp_sandbox/
   sudo ./example/ioam-nova-tgen/start_setup.sh 
   
   This script performs the following,
   	Launch all the containers with the interfaces between them
	Connect the containers
	Configure the rack4 device with tap0 int for export
	Attach the containers and start the vpp with the startup config
	Configure the TGNs with ip6 address and ip6 default route
	Configure the rack4 tap0 with ip address
	
	Note: Once the script is executed sucessfully, attach to the containers and telnet to vpp
	(telnet 0 5002) verify the interfaces and the address configured (show int addr)

```
5. Verify end to end connectivity with trace working . 
```
   5.1 Shell 1: sudo lxc-attach -n TGN1
   5.2 Shell 2: sudo lxc-attach -n TGN2
   5.3 Shell 3: sudo lxc-attach -n rack4 (which is our exporter node)
   5.4 Shell 3: telnet 0 5002; clear trace; trace add af-packet-input 20 then quit
   5.5 Shell 1: connect to TGN1 and start ping to TGN2 - lxc-attach -n TGN1, ping6 db12::1
   5.6 Shell 3: show trace, quit
   5.7 Shell 3: sudo lxc-attach -n rack4, telnet 0 5002, show trace
   5.8 Shell 2: connect to TGN2 and start ping to TGN1 - lxc-attach -n TGN2, ping6 db11::1
   
   Verify the end to end connectivity working between TGN1 and TGN2
   Verify the iOAM traces are displayed in the show trace in rack4
   
   Eg of show trace with iOAM trace and seq no enabled:
   	DBGvpp# sh trace
	------------------- Start of thread 0 vpp_main -------------------
	Packet 1

	00:34:55:198527: af-packet-input
  	af_packet: hw_if_index 1 next-index 4
    	tpacket2_hdr:
      		status 0x20000001 len 174 snaplen 174 mac 66 net 80
      		sec 0x59c3a6f4 nsec 0xe4c196c vlan 0 vlan_tpid 0
	00:34:55:198548: ethernet-input
  	 IP6: 02:fe:c0:90:a0:41 -> 02:fe:f3:a6:0e:ab
	00:34:55:198579: ip6-input
  	 IP6_HOP_BY_HOP_OPTIONS: db11::1 -> db12::1
    	 tos 0x00, flow label 0x413e9, hop limit 60, payload length 120
	00:34:55:198587: ip6-inacl
  	 INACL: sw_if_index 1, next_index 1, table 0, offset 192
	00:34:55:198595: ip6-lookup
  	 fib 0 dpo-idx 9 flow hash: 0x00000000
  	 IP6_HOP_BY_HOP_OPTIONS: db11::1 -> db12::1
    	 tos 0x00, flow label 0x413e9, hop limit 60, payload length 120
	00:34:55:198606: ip6-hop-by-hop
     	 IP6_HOP_BY_HOP: next index 15 len 56 traced 56  Trace Type 0x9 , 0 elts left
    	[0] ttl 0x3c node id 0x4 ts 0xaa812031 

    	[1] ttl 0x3d node id 0x7 ts 0xaa7bb0ca 

    	[2] ttl 0x3e node id 0x9 ts 0xaa78cf11 

    	[3] ttl 0x3f node id 0x5 ts 0xaa7ff007 

    	[4] ttl 0x40 node id 0x1 ts 0xaa771f67 

	SeqNo = 0x30e
	00:34:55:198614: ip6-export
  	 EXPORT: flow_label 1610879977, next index 0
	00:34:55:198620: ip6-pop-hop-by-hop
  	 IP6_POP_HOP_BY_HOP: next index 5
	00:34:55:198627: ip6-rewrite
  	 tx_sw_if_index 3 adj-idx 9 : ipv6 via db12::1 host-l_rack43: 6a2da37a15c202fe654d2bc886dd flow hash: 0x00000000
  	 00000000: 6a2da37a15c202fe654d2bc886dd600413e900403a3bdb110000000000000000
  	 00000020: 000000000001db1200000000000000000000000000018000e0ac00340303f4a6
  	 00000040: c359000000006ba7030000000000101112131415161718191a1b1c1d1e1f2021
  	 00000060: 22232425262728292a2b2c2d2e2f3031323334353637000000000000
	 00:34:55:198630: host-l_rack43-output
  	 host-l_rack43
  	 IP6: 02:fe:65:4d:2b:c8 -> 6a:2d:a3:7a:15:c2
  	ICMP6: db11::1 -> db12::1
    	 tos 0x00, flow label 0x413e9, hop limit 59, payload length 64
  	ICMP echo_request checksum 0xe0ac
```
6. Install iperf Traffic generator in TGNs . 
```
    apt-get install git-core  //install git tool
    apt-get install make    //install make tool, to make iperf3
    git clone https://github.com/esnet/iperf   //clone iperf3 source code
    cd iPerf  //go to the iperf3 source code folder, and compile it
    ./configure
    make
    make install
```
7. Configure iperf Traffic generator in TGNs to generate traffic streams . 
```
Start the iperf server in TGN2
    7.1 Shell 1: sudo lxc-attach -n TGN2
    7.2 Shell 1: Go to the iperf3 source code folder
    7.3 Shell 1: issue the command "iperf -s -V -u -B db12::1"
    - s - server
    - V - V6
    - u - UDP Traffic
    - B - bind to the v6 address
    
    Eg: 
    root@kharicha-VirtualBox:~/pinger/iperf-3.0.6# iperf -s -V -u -B db12::1
	
	------------------------------------------------------------
	Server listening on UDP port 5001
	Binding to local address db12::1
	Receiving 1470 byte datagrams
	UDP buffer size:  208 KByte (default)
	------------------------------------------------------------

	[  3] local db12::1 port 5001 connected with db11::1 port 35823

Start the iperf client in TGN1 
    7.1 Shell 1: sudo lxc-attach -n TGN1
    7.2 Shell 1: Go to the iperf3 source code folder
    7.3 Shell 1: issue the command "iperf -u -t 3000 -i 1 -V -c db12::1 -b 100k -l 1200"
    - s - server
    - V - V6
    - u - UDP Traffic
    - t - timeout
    - i - interval
    - c - client
    - b - bandwidth
    - l - length
    
    Eg: 
    root@kharicha-VirtualBox:~/pinger/iOAM/scripts/vpp_sandbox# iperf -u -t 3000 -i 1 -V -c db12::1 -b 100k -l 1200

	------------------------------------------------------------
	Client connecting to db12::1, UDP port 5001
	Sending 1200 byte datagrams
	UDP buffer size:  208 KByte (default)
	------------------------------------------------------------
	[  3] local db11::1 port 60740 connected with db12::1 port 5001
	[ ID] Interval       Transfer     Bandwidth
	[  3]  0.0- 1.0 sec  12.9 KBytes   106 Kbits/sec
	[  3]  1.0- 2.0 sec  11.7 KBytes  96.0 Kbits/sec
	[  3]  2.0- 3.0 sec  12.9 KBytes   106 Kbits/sec
	!
	!
	!
	[  3]  2999.0- 3000.0 sec  12.9 KBytes   106 Kbits/sec 
```
8. Verify exporter works . 
```
    8.1 Shell 1: sudo lxc-attach -n rack4
    8.2 Shell 1: ping 10.255.0.233 > source of the exporter
    8.3 Shell 1: tcpdump -nxi tap0
    Verify the tcp dump is recieved for the interface tap0 successfully.
    Eg:
    root@kharicha-VirtualBox:~# tcpdump -nxi tap0
    tcpdump: verbose output suppressed, use -v or -vv for full protocol decode
    listening on tap0, link-type EN10MB (Ethernet), capture size 262144 bytes
    11:45:24.847644 IP 10.255.0.233.4739 > 10.255.0.254.4739: UDP, length 1364
    0x0000:  4500 0570 0000 0000 fd11 a098 0aff 00e9
    0x0010:  0aff 00fe 1283 1283 055c 0000 000a 0554
    0x0020:  59c3 a658 0000 0058 0000 0000 0110 0544
    0x0030:  6000 0000 0078 003c db11 0000 0000 0000
    0x0040:  0000 0000 0000 0001 db12 0000 0000 0000
    0x0050:  0000 0000 0000 0001 3a06 3b2a 0900 3c00
    0x0060:  0004 a0a0 65a8 3d00 0007 a09a f655 3e00
    0x0070:  0009 a098 148f 3f00 0005 a09f 3564 4000
    0x0080:  0001 a096 64b5 1d06 0000 0000 0269 0000
    0x0090:  8000 922a 0034 025e 4ea6 c359 0000 0000
    0x00a0:  57cf 0c00 0000 0000 1011 1213 1415 1617
    0x00b0:  0000 0000 0000 0000 0000 0000 0000 0000
    0x00c0:  0000 0000 0000 0000 0000 0000 0000 0000
    0x00d0:  0000 0000 0000 0000 0000 0000 0000 0000
    0x00e0:  0000 0000 0000 0000 0000 0000 0000 0000
    0x00f0:  6000 0000 0078 003c db11 0000 0000 0000   >>>> db11 src ipv6 addr
    0x0100:  0000 0000 0000 0001 db12 0000 0000 0000   >>>> db12 dst ipv6 addr
    0x0110:  0000 0000 0000 0001 3a06 3b2a 0900 3c00   >>>> 3b2a ioam option
    0x0120:  0004 a0af b9e1 3d00 0007 a0aa 4a8a 3e00   >>>> 0004 node id 0007 node id
    0x0130:  0009 a0a7 68de 3f00 0005 a0ae 8461 4000   >>>> 0009 node id 0005 node id
    0x0140:  0001 a0a5 b3ba 1d06 0000 0000 026a 0000   >>>> 0001 node id 026a seq no
```
9. Collect exported data using python script which writes it to a file . 
```
    8.1 Shell 1: sudo lxc-attach -n rack4
    8.2 Shell 2: got to the following location "<git_checkout_path>/scripts/vpp_sandbox/example_scripts"
    8.3 Shell 3: Execute the python script :python parser.py"
    
    Eg:
    root@kharicha-VirtualBox:~/pinger# pwd
    /home/kharicha/pinger
    root@kharicha-VirtualBox:~/pinger# python parser.py
    
    The data is written to the file /tmp/ioam-data-sample.txt

    root@kharicha-VirtualBox:~/pinger# ls -l /tmp
    total 916
    -rw-r--r-- 1 root root 927953 Sep 20 11:15 ioam-data-sample.txt
    -rw-r--r-- 1 root root   2165 Sep 20 09:14 vpp.log
    root@kharicha-VirtualBox:~/pinger# 
    
    Eg:
    Output printed in the shell:
    	received trace option
    	0000   09 00 3C 00 00 04 17 E1  D0 AD 3D 00 00 07 17 B4   ..<.......=.....
	0010   88 EE 3E 00 00 09 17 E9  C2 F4 3F 00 00 05 17 E1   ..>.......?.....
	0020   53 1A 40 00 00 01 17 E6  21 C8                     S.@.....!.
	###[ iOAM Trace ]###
  	optionType= 59
  	optionLen = 42
  	trace-type= 9
  	elements-left= 0
  	\trace-elt \
   	|###[ iOAM node data ]###
   	|  hoplim    = 60
   	|  nodeID    = 4L
   	|  timestamp = 400674989
   	|###[ iOAM node data ]###
   	|  hoplim    = 61
   	|  nodeID    = 7L
   	|  timestamp = 397707502
   	|###[ iOAM node data ]###
   	|  hoplim    = 62
   	|  nodeID    = 9L
   	|  timestamp = 401195764
   	|###[ iOAM node data ]###
   	|  hoplim    = 63
   	|  nodeID    = 5L
   	|  timestamp = 400642842
   	|###[ iOAM node data ]###
   	|  hoplim    = 64
   	|  nodeID    = 1L
   	|  timestamp = 400957896
	received E2E option
	0000   00 00 00 00 AE 30                                  .....0
	###[ iOAM E2E ]###
  	optionType= 29
  	optionLen = 6
  	e2e_type  = 0
  	e2e_res   = 0
  	seq_np    = 44592
	out_str =  db11::1 to db12::1 44592 
	out_str =  db11::1 to db12::1 44592 4, 400674989 -> 
	out_str =  db11::1 to db12::1 44592 4, 400674989 -> 7, 397707502 -> 
	out_str =  db11::1 to db12::1 44592 4, 400674989 -> 7, 397707502 -> 9, 401195764 -> 
	out_str =  db11::1 to db12::1 44592 4, 400674989 -> 7, 397707502 -> 9, 401195764 -> 5, 400642842 -> 
	out_str =  db11::1 to db12::1 44592 4, 400674989 -> 7, 397707502 -> 9, 401195764 -> 5, 400642842 -> 1, 400957896 -> 
	out_str =  db11::1 to db12::1 44592 4, 400674989 -> 7, 397707502 -> 9, 401195764 -> 5, 400642842 -> 1, 400957896

	Processed message is:
	{"src": "db11::1", "node_6_id": 4, "node_5_id": 7, "end_to_end_delay": -282907, "dst": "db12::1", "node_4_id": 9, "topic": 	          "ioam-trace", "node_3_id": 5, "node_2_id": 1}

Output printed in the file:
	db11::1 to db12::1 35119 4, 3791958029 -> 8, 3792512390 -> 10, 3792474387 -> 6, 3791854200 -> 1, 3792239688

	Data format:
	Flow-name - SrcIP-SrcPort to DestIP-DestPort : db11::1 to db12::1
	Seq-no - Integer : 35119 
	path - PathMap in format [nodeID1], [timestamp] -> [nodeID2], [timestamp]... : 4, 3791958029 -> 8, 3792512390   ...
    
```
10. To delete the topology and clear all containers:
```

   cd <git_checkout_path>/iOAM/scripts/vpp_sandbox/
   sudo ./example/ioam-nova-tgen/kill_setup.sh 
   
   This script performs the following,
   	Delete all the containers Launched
	
   Once the script is exceuted successfully please perform a "sudo reboot now" to clear all the bridge
   connections.
```

