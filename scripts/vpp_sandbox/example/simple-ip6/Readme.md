# Sample Topology and Configs

To create a topology as shown below, follow below stpes:

![Topology](./Topology-simple_ip6.png?raw=true "Topology")
Prereq:
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

* Pull the IAOM GIT
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

   Uncomment lxc.mount.entry line and replace <Local directory> by path up to <git_checkout_path>/scripts/vpp_sandbox

 
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
