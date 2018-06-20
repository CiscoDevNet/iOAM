#!/bin/bash
sudo sysctl -w net.ipv6.conf.all.forwarding=1
sudo ./launch_lxc_nobridge.sh -n alpha -t vpp-ext -l 2
sudo ./launch_lxc_nobridge.sh -n athos -t vpp-ext -l 2
sudo ./launch_lxc_nobridge.sh -n porthos -t vpp-ext -l 2
sudo ./launch_lxc_nobridge.sh -n aramis -t vpp-ext -l 2
sudo ./launch_lxc_nobridge.sh -n beta -t vpp-ext -l 2
sudo ./connect_lxc_nobridge.sh -c ./example/linux/config_nobridge.txt -f ./example/linux/connect.log
sudo lxc-attach -n aramis -- bash -c 'mkdir /dev/net; mknod /dev/net/tun aramis 10 200 ; chmod 666 /dev/net/tun'
sudo lxc-attach -n alpha -- bash -c 'ip -6 address add db00::2/64 dev l_alpha1; ip -6 route add default via db00::1'
sudo lxc-attach -n beta -- bash -c 'ip -6 address add db03::2/64 dev l_beta1; ip -6 route add default via db03::1'
sudo lxc-attach -n athos -- bash -c 'ifconfig l_athos1 inet6 add db00::1/64; ifconfig l_athos2 inet6 add db01::1/64; ip route add db03::0/64 via db01::2 dev l_athos2; ip route add db02::0/64 via db01::2 dev l_athos2'
sudo lxc-attach -n porthos -- bash -c 'ifconfig l_porthos1 inet6 add db01::2/64 ; ifconfig l_porthos2 inet6 add db02::1/64  ; ip route add db00::0/64 via db01::1 dev l_porthos1 ; ip route add db03::0/64 via db02::2 dev l_porthos2 '
sudo lxc-attach -n aramis -- bash -c 'ifconfig l_aramis1 inet6 add db02::2/64 ; ifconfig l_aramis2 inet6 add db03::1/64; ip route add db00::0/64 via db02::1 dev l_aramis1; ip route add db01::0/64 via db02::1 dev l_aramis1'
sudo lxc-attach -n athos -- bash -c 'sysctl -w net.ipv6.conf.all.forwarding=1 ; sysctl -w net.ipv6.ioam6_node_id=1'
sudo lxc-attach -n porthos -- bash -c 'sysctl -w net.ipv6.conf.all.forwarding=1; sysctl -w net.ipv6.ioam6_node_id=2'
sudo lxc-attach -n aramis -- bash -c 'sysctl -w net.ipv6.conf.all.forwarding=1; sysctl -w net.ipv6.ioam6_node_id=3; sysctl -w net.ipv6.conf.l_aramis1.ioam6_if_decap=1'
