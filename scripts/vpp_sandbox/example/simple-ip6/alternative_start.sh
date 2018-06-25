#!/bin/bash

SANDBOXDIR="$HOME/git/iOAM/scripts/vpp_sandbox"

sudo ${SANDBOXDIR}/launch_lxc_nobridge.sh -n host1 -t vpp-ext -l 2
sleep 1
sudo ${SANDBOXDIR}/launch_lxc_nobridge.sh -n a -t vpp-ext -l 2
sleep 1
sudo ${SANDBOXDIR}/launch_lxc_nobridge.sh -n b -t vpp-ext -l 2
sleep 1
sudo ${SANDBOXDIR}/launch_lxc_nobridge.sh -n c -t vpp-ext -l 2
sleep 1
sudo ${SANDBOXDIR}/launch_lxc_nobridge.sh -n host2 -t vpp-ext -l 2
sleep 1
sudo ${SANDBOXDIR}/connect_lxc_nobridge.sh -c ${SANDBOXDIR}/example/simple-ip6/config_nobridge.txt -f ${SANDBOXDIR}/example/simple-ip6/connect.log
sleep 1

sudo lxc-attach -n c -- mkdir /dev/net
sleep 1
sudo lxc-attach -n c -- mknod /dev/net/tun c 10 200
sleep 1
sudo lxc-attach -n c -- chmod 666 /dev/net/tun
sleep 1

sudo lxc-attach -n a -- bash -c 'vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/simple-ip6/a.conf cli-listen /run/vpp/cli.sock } plugins {plugin dpdk_plugin.so { disable } }; echo "Sleeping for 10 seconds"; sleep 10'
sleep 1
sudo lxc-attach -n b -- bash -c 'vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/simple-ip6/b.conf cli-listen /run/vpp/cli.sock } plugins {plugin dpdk_plugin.so { disable } }; echo "Sleeping for 10 seconds"; sleep 10'
sleep 1
sudo lxc-attach -n c -- bash -c 'vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/simple-ip6/c.conf cli-listen /run/vpp/cli.sock } plugins {plugin dpdk_plugin.so { disable } }; echo "Sleeping for 10 seconds"; sleep 10'
sleep 1

sudo lxc-attach -n host1 -- ip -6 address add db00::2/64 dev l_host11
sleep 1
sudo lxc-attach -n host1 -- ip -6 route add default via db00::1
sleep 1
sudo lxc-attach -n host2 -- ip -6 address add db03::2/64 dev l_host21
sleep 1
sudo lxc-attach -n host2 -- ip -6 route add default via db03::1
sleep 1
sudo lxc-attach -n c -- ifconfig tap0 10.255.0.254/24
sleep 1

sudo lxc-attach -n c -- touch /tmp/collector-in.log
sleep 1
sudo lxc-attach -n c -- touch /tmp/collector-out.log
sleep 1
sudo lxc-attach -n c -- touch /tmp/collector-err.log
sleep 1
sudo lxc-attach -n c -- touch /tmp/daemon-ioam-collector.pid
sleep 1
sudo lxc-attach -n c -- python /scratch/example/simple-ip6/collector.py start
sleep 1
