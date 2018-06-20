#!/bin/bash
sudo sysctl -w net.ipv6.conf.all.forwarding=1
sudo ./launch_lxc_nobridge.sh -n host1 -t vpp-ext -l 2
sudo ./launch_lxc_nobridge.sh -n a -t vpp-ext -l 2
sudo ./launch_lxc_nobridge.sh -n b -t vpp-ext -l 2
sudo ./launch_lxc_nobridge.sh -n c -t vpp-ext -l 2
sudo ./launch_lxc_nobridge.sh -n host2 -t vpp-ext -l 2
sudo ./connect_lxc_nobridge.sh -c ./example/simple-ip6/config_nobridge.txt -f ./example/simple-ip6/connect.log
sudo lxc-attach -n c -- bash -c 'mkdir /dev/net; mknod /dev/net/tun c 10 200 ; chmod 666 /dev/net/tun'
sudo lxc-attach -n host1 -- bash -c 'ip -6 address add db00::2/64 dev l_host11; ip -6 route add default via db00::1'
sudo lxc-attach -n host2 -- bash -c 'ip -6 address add db03::2/64 dev l_host21; ip -6 route add default via db03::1'
sudo lxc-attach -n a -- bash -c 'ifconfig l_a1 inet6 add db00::1/64; ifconfig l_a2 inet6 add db01::1/64; ip route add db03::0/64 via db01::2 dev l_a2; ip route add db02::0/64 via db01::2 dev l_a2'
sudo lxc-attach -n b -- bash -c 'ifconfig l_b1 inet6 add db01::2/64 ; ifconfig l_b2 inet6 add db02::1/64  ; ip route add db00::0/64 via db01::1 dev l_b1 ; ip route add db03::0/64 via db02::2 dev l_b2 '
sudo lxc-attach -n c -- bash -c 'ifconfig l_c1 inet6 add db02::2/64 ; ifconfig l_c2 inet6 add db03::1/64; ip route add db00::0/64 via db02::1 dev l_c1; ip route add db01::0/64 via db02::1 dev l_c1'
sudo lxc-attach -n a -- bash -c 'sysctl -w net.ipv6.conf.all.forwarding=1 ; sysctl -w net.ipv6.ioam6_node_id=1'
sudo lxc-attach -n b -- bash -c 'sysctl -w net.ipv6.conf.all.forwarding=1; sysctl -w net.ipv6.ioam6_node_id=2'
sudo lxc-attach -n c -- bash -c 'sysctl -w net.ipv6.conf.all.forwarding=1; sysctl -w net.ipv6.ioam6_node_id=3; sysctl -w net.ipv6.conf.l_c1.ioam6_if_decap=1'
