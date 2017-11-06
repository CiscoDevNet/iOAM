#!/bin/bash

sudo ./launch_lxc.sh -n host1 -t vpp-p4-ext -l 2
sudo ./launch_lxc.sh -n a -t vpp-p4-ext -l 2
sudo ./launch_lxc.sh -n S1 -t vpp-p4-ext -l 2
sudo ./launch_lxc.sh -n b -t vpp-p4-ext -l 2
sudo ./launch_lxc.sh -n S2 -t vpp-p4-ext -l 2
sudo ./launch_lxc.sh -n c -t vpp-p4-ext -l 2
sudo ./launch_lxc.sh -n host2 -t vpp-p4-ext -l 2
sudo ./connect_lxc.sh -c ./example/simple_p4_vpp_ip6/config.txt -f ./example/simple_p4_vpp_ip6/connect.log
sudo ip link set host1_a up
sudo ip link set a_S1 up
sudo ip link set S1_b up
sudo ip link set b_S2 up
sudo ip link set S2_c up
sudo ip link set c_host2 up
sleep 10
lxc-attach -n a -- vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/simple_p4_vpp_ip6/a.conf cli-listen localhost:5002 }
#lxc-attach -n a -- ip -6 address add db00::1/64 dev l_a1
#lxc-attach -n a -- ip -6 address add db01::1/64 dev l_a2
#lxc-attach -n a -- ip -6 route add db02::0/64 via db01::2
#lxc-attach -n a -- ip -6 route add db03::0/64 via db01::2
#lxc-attach -n a -- ip -6 route add db04::0/64 via db01::2
#lxc-attach -n a -- ip -6 route add db05::0/64 via db01::2
lxc-attach -n S1 -- ip -6 address add db01::2/64 dev l_S11
lxc-attach -n S1 -- ip -6 address add db02::1/64 dev l_S12
lxc-attach -n S1 -- ip -6 route add db00::0/64 via db01::1
lxc-attach -n S1 -- ip -6 route add db03::0/64 via db02::2 
lxc-attach -n S1 -- ip -6 route add db04::0/64 via db02::2
lxc-attach -n S1 -- ip -6 route add db05::0/64 via db02::2
#lxc-attach -n S1 -- vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/simple_p4_vpp_ip6/s1.conf cli-listen localhost:5002 }
sleep 30
lxc-attach -n b -- vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/simple_p4_vpp_ip6/b.conf cli-listen localhost:5002 }
#lxc-attach -n b -- ip -6 address add db02::2/64 dev l_b1
#lxc-attach -n b -- ip -6 address add db03::1/64 dev l_b2
#lxc-attach -n b -- ip -6 route add db00::0/64 via db02::1
#lxc-attach -n b -- ip -6 route add db01::0/64 via db02::1
#lxc-attach -n b -- ip -6 route add db04::0/64 via db03::2
#lxc-attach -n b -- ip -6 route add db05::0/64 via db03::2
lxc-attach -n S2 -- ip -6 address add db03::2/64 dev l_S21
lxc-attach -n S2 -- ip -6 address add db04::1/64 dev l_S22
lxc-attach -n S2 -- ip -6 route add db00::0/64 via db03::1
lxc-attach -n S2 -- ip -6 route add db01::0/64 via db03::1 
lxc-attach -n S2 -- ip -6 route add db02::0/64 via db03::1
lxc-attach -n S2 -- ip -6 route add db05::0/64 via db04::2
#lxc-attach -n S2 -- vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/simple_p4_vpp_ip6/s2.conf cli-listen localhost:5002 }
sleep 30
lxc-attach -n c -- vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/simple_p4_vpp_ip6/c.conf cli-listen localhost:5002 }
sleep 10
#lxc-attach -n c -- ip -6 address add db04::2/64 dev l_c1
#lxc-attach -n c -- ip -6 address add db05::1/64 dev l_c2
#lxc-attach -n c -- ip -6 route add db00::0/64 via db04::1
#lxc-attach -n c -- ip -6 route add db01::0/64 via db04::1
#lxc-attach -n c -- ip -6 route add db02::0/64 via db04::1
#lxc-attach -n c -- ip -6 route add db03::0/64 via db04::1
lxc-attach -n host1 -- ip -6 address add db00::2/64 dev l_host11
lxc-attach -n host1 -- ip -6 route add default via db00::1
lxc-attach -n host2 -- ip -6 address add db05::2/64 dev l_host21
lxc-attach -n host2 -- ip -6 route add default via db05::1

