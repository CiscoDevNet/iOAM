#!/bin/bash

sudo ./launch_lxc.sh -n host1 -t vpp-ext -l 2
sudo ./launch_lxc.sh -n a -t vpp-ext -l 2
sudo ./launch_lxc.sh -n b -t vpp-ext -l 2
sudo ./launch_lxc.sh -n c -t vpp-ext -l 2
sudo ./launch_lxc.sh -n host2 -t vpp-ext -l 2
sudo ./connect_lxc.sh -c ./example/simple-ip6/config.txt -f ./example/simple-ip6/connect.log
lxc-attach -n a -- bash -c 'vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/simple-ip6/a.conf cli-listen localhost:5002 }; echo "Sleeping for 10 seconds"; sleep 10'
lxc-attach -n b -- bash -c 'vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/simple-ip6/b.conf cli-listen localhost:5002 }; echo "Sleeping for 10 seconds"; sleep 10'
lxc-attach -n c -- bash -c 'vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/simple-ip6/c.conf cli-listen localhost:5002 }; echo "Sleeping for 10 seconds"; sleep 10'
lxc-attach -n host1 -- ip -6 address add db00::2/64 dev l_host11
lxc-attach -n host1 -- ip -6 route add default via db00::1
lxc-attach -n host2 -- ip -6 address add db03::2/64 dev l_host21
lxc-attach -n host2 -- ip -6 route add default via db03::1
sudo lxc-attach -n c -- mkdir /dev/net
sudo lxc-attach -n c -- mknod /dev/net/tun c 10 200
sudo lxc-attach -n c -- chmod 666 /dev/net/tun
sudo lxc-attach -n c -- ifconfig tap0 10.255.0.254/24
