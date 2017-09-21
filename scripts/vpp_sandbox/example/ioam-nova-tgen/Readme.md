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
    
```
9. Collect exported data using python script and write it to a file . 
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

