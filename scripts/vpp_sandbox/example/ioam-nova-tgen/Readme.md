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


* To delete the topology and clear all containers:
```

   cd <git_checkout_path>/iOAM/scripts/vpp_sandbox/
   sudo ./example/simple-ip6/kill_simpleip6.sh 
```

4. Connect Containers:

		sudo ./connect_lxc.sh -c ./example/udp_probe/config.txt -f ./example/udp_probe/connect.log

5. Connect to each container and start VPP with startup config files provided.

		Rack1 lxc:
		sudo lxc-attach -n rack1
		vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/rack_sw_1.conf cli-listen localhost:5002 } &

		Rack2 lxc:
                sudo lxc-attach -n rack2
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/rack_sw_2.conf cli-listen localhost:5002 } &

		Rack3 lxc:
                sudo lxc-attach -n rack3
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/rack_sw_3.conf cli-listen localhost:5002 } &

		Rack4 lxc:
                sudo lxc-attach -n rack4
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/rack_sw_4.conf cli-listen localhost:5002 } &

		Fabric1 lxc:
                sudo lxc-attach -n fabric1
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/fabric_sw_1.conf cli-listen localhost:5002 } &

		Fabric2 lxc:
                sudo lxc-attach -n fabric2
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/fabric_sw_2.conf cli-listen localhost:5002 } &

		Fabric3 lxc:
                sudo lxc-attach -n fabric3
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/fabric_sw_3.conf cli-listen localhost:5002 } &

		Fabric4 lxc:
                sudo lxc-attach -n fabric4
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/fabric_sw_4.conf cli-listen localhost:5002 } &

		Spine1 lxc:
                sudo lxc-attach -n spine1
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/spine_sw_1.conf cli-listen localhost:5002 } &

		Spine2 lxc:
                sudo lxc-attach -n spine2
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/spine_sw_2.conf cli-listen localhost:5002 } &


6. To delete the topology and clear all containers:

		sudo ./launch_lxc.sh -n rack1 -d
		sudo ./launch_lxc.sh -n rack2 -d
		sudo ./launch_lxc.sh -n rack3 -d
		sudo ./launch_lxc.sh -n rack4 -d
		sudo ./launch_lxc.sh -n fabric1 -d
		sudo ./launch_lxc.sh -n fabric2 -d
		sudo ./launch_lxc.sh -n fabric3 -d
		sudo ./launch_lxc.sh -n fabric4 -d
		sudo ./launch_lxc.sh -n spine1 -d
		sudo ./launch_lxc.sh -n spine2 -d
