# Sample Topology and Configs

To create a topology as shown below, follow below steps:

![Topology](./Topology-linux.png?raw=true "Topology")
## Prereq:
* Build Linux kernel with iOAM6 support by pulling and following instructions [https://github.com/FrancartJ/iOAM-IPv6-LinuxKernel]
* Pull this repo:
``` 
git clone https://github.com/CiscoDevNet/iOAM.git
```

* Install LXC and copy template file.

		sudo apt-get install -y lxc lxctl lxc-templates util-linux
		cp <git_checkout_path>/iOAM/scripts/vpp_sandbox/example/linux/lxc-vpp-ext /usr/share/lxc/templates/lxc-vpp-ext
		cp <git_checkout_path>/iOAM/scripts/vpp_sandbox/example/linux/ioam6-hook /var/lib/lxc/

* Edit /usr/share/lxc/templates/lxc-vpp-ext file:
   Look for below lines:

		# Add VPP Specific mounts here
		#lxc.mount.entry = <Local directory> scratch none ro,bind 0 0

   Uncomment lxc.mount.entry line and replace "\<Local directory\>" by path to <git_checkout_path>/scripts/vpp_sandbox

 
## Steps to running this example


* Start the network of containers:
```

   cd <git_checkout_path>/iOAM/scripts/vpp_sandbox/
   sudo ./example/linux/start.sh 
```

* Open 2 shells. 
```
   4.1 Shell 1: cd <git_checkout_path>/iOAM/scripts/vpp_sandbox/example/linux; gcc -o test test.c
   4.2 Shell 1: sudo lxc-attach -n athos
   4.3 Shell 1: cd /scratch/example/linux/; ./test -m 1 -h 2 -f 1 -o 32768 -i l_athos1
   4.6 Shell 2:  Connect to alpha and start ping to beta - lxc-attach -n alpha, ping6 db03::2
   4.7 Shell 1: On host: check /var/log/syslog
```
* To delete the topology and clear all containers:
```

   cd <git_checkout_path>/iOAM/scripts/vpp_sandbox/
   sudo ./example/linux/kill.sh 
```

### Sample output

* After ping, in porthos: 

```
$lxc-attach -n porthos
bash-4.3# tcpdump -vv -l -i l_porthos1
tcpdump: listening on l_porthos1, link-type EN10MB (Ethernet), capture size 262144 bytes
04:51:03.680262 IP6 (flowlabel 0xeeaeb, hlim 63, next-header Options (0) payload length: 88) db00::2 > db03::2: HBH (opt_type 0x21: len=14)(padn) [icmp6 sum ok] ICMP6, echo request, seq 39
04:51:03.680293 IP6 (flowlabel 0x2b07b, hlim 62, next-header ICMPv6 (58) payload length: 64) db03::2 > db00::2: [icmp6 sum ok] ICMP6, echo reply, seq 39
04:51:04.704261 IP6 (flowlabel 0xeeaeb, hlim 63, next-header Options (0) payload length: 88) db00::2 > db03::2: HBH (opt_type 0x21: len=14)(padn) [icmp6 sum ok] ICMP6, echo request, seq 40
04:51:04.704294 IP6 (flowlabel 0x2b07b, hlim 62, next-header ICMPv6 (58) payload length: 64) db03::2 > db00::2: [icmp6 sum ok] ICMP6, echo reply, seq 40
04:51:05.728610 IP6 (flowlabel 0xeeaeb, hlim 63, next-header Options (0) payload length: 88) db00::2 > db03::2: HBH (opt_type 0x21: len=14)(padn) [icmp6 sum ok] ICMP6, echo request, seq 41
04:51:05.728640 IP6 (flowlabel 0x2b07b, hlim 62, next-header ICMPv6 (58) payload length: 64) db03::2 > db00::2: [icmp6 sum ok] ICMP6, echo reply, seq 41
04:51:06.752251 IP6 (flowlabel 0xeeaeb, hlim 63, next-header Options (0) payload length: 88) db00::2 > db03::2: HBH (opt_type 0x21: len=14)(padn) [icmp6 sum ok] ICMP6, echo request, seq 42

```

* On the host machine: 
```

root@linux-r4ap:~/ioam/iOAM/scripts/vpp_sandbox# tail -f /var/log/syslog
Jun 20 01:01:52 linux-r4ap kernel: [56467.041575] 3f  0  0  1
Jun 20 01:01:52 linux-r4ap kernel: [56467.041576] [IOAM] TRACE END
Jun 20 01:01:53 linux-r4ap kernel: [56468.065889] [IOAM] TRACE START
Jun 20 01:01:53 linux-r4ap kernel: [56468.065892] 80  0  c  0
Jun 20 01:01:53 linux-r4ap kernel: [56468.065893] node size : 4
Jun 20 01:01:53 linux-r4ap kernel: [56468.065894] node 0:
Jun 20 01:01:53 linux-r4ap kernel: [56468.065895] 3e  0  0  2
Jun 20 01:01:53 linux-r4ap kernel: [56468.065896] node 1:
Jun 20 01:01:53 linux-r4ap kernel: [56468.065897] 3f  0  0  1
Jun 20 01:01:53 linux-r4ap kernel: [56468.065898] [IOAM] TRACE END

```

