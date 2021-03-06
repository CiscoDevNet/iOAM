#!/bin/bash
sudo sysctl -w net.ipv6.conf.all.forwarding=1
#Launch all the containers with the interfaces between them
sudo ./launch_lxc.sh -n rack1 -t vpp-ext -l 3
sudo ./launch_lxc.sh -n rack2 -t vpp-ext -l 2
sudo ./launch_lxc.sh -n rack3 -t vpp-ext -l 2
sudo ./launch_lxc.sh -n rack4 -t vpp-ext -l 3
sudo ./launch_lxc.sh -n fabric1 -t vpp-ext -l 4
sudo ./launch_lxc.sh -n fabric2 -t vpp-ext -l 4
sudo ./launch_lxc.sh -n fabric3 -t vpp-ext -l 4
sudo ./launch_lxc.sh -n fabric4 -t vpp-ext -l 4
sudo ./launch_lxc.sh -n spine1 -t vpp-ext -l 4
sudo ./launch_lxc.sh -n spine2 -t vpp-ext -l 4
sudo ./launch_lxc.sh -n TGN1 -t vpp-ext -l 1
sudo ./launch_lxc.sh -n TGN2 -t vpp-ext -l 1

#connect the containers
sudo ./connect_lxc.sh -c ./example/ioam-nova-tgen/config.txt -f ./example/ioam-nova-tgen/connect.log

#configure the rack4 device with tap0 int for export
sudo lxc-attach -n rack4 -- nohup bash -c 'mkdir /dev/net'
sudo lxc-attach -n rack4 -- nohup bash -c 'mknod /dev/net/tun c 10 200'
sudo lxc-attach -n rack4 -- nohup bash -c 'chmod 666 /dev/net/tun'

#attach the containers and start the vpp
sudo lxc-attach -n rack1 --  nohup bash -c 'ifconfig eth0 10.0.3.2 netmask 255.255.255.0; vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/ioam-nova-tgen/rack_sw_1.conf cli-listen localhost:5002 } stats {default} plugins {plugin dpdk_plugin.so { disable } } statseg {per-node-counters on}; echo "Sleeping for 10 seconds";sleep 10; nohup vpp_prometheus_export  &'
sudo lxc-attach -n rack2 --  nohup bash -c 'ifconfig eth0 10.0.3.3 netmask 255.255.255.0; vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/ioam-nova-tgen/rack_sw_2.conf cli-listen localhost:5002 } stats {default} plugins {plugin dpdk_plugin.so { disable } } statseg {per-node-counters on};echo "Sleeping for 10 seconds";sleep 10; nohup vpp_prometheus_export  &'
sudo lxc-attach -n rack3 --  nohup bash -c 'ifconfig eth0 10.0.3.4 netmask 255.255.255.0; vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/ioam-nova-tgen/rack_sw_3.conf cli-listen localhost:5002 } stats {default} plugins {plugin dpdk_plugin.so { disable } } statseg {per-node-counters on};echo "Sleeping for 10 seconds";sleep 10; nohup vpp_prometheus_export  &'
sudo lxc-attach -n rack4 --  nohup bash -c 'ifconfig eth0 10.0.3.5 netmask 255.255.255.0; vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/ioam-nova-tgen/rack_sw_4.conf cli-listen localhost:5002 } stats {default} plugins {plugin dpdk_plugin.so { disable } } statseg {per-node-counters on};echo "Sleeping for 10 seconds";sleep 10; nohup vpp_prometheus_export  &'
sudo lxc-attach -n fabric1 --  nohup bash -c 'ifconfig eth0 10.0.3.6 netmask 255.255.255.0; vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/ioam-nova-tgen/fabric_sw_1.conf cli-listen localhost:5002 } stats {default} plugins {plugin dpdk_plugin.so { disable } } statseg {per-node-counters on};echo "Sleeping for 10 seconds";sleep 10; nohup vpp_prometheus_export  &'
sudo lxc-attach -n fabric2 --  nohup bash -c 'ifconfig eth0 10.0.3.7 netmask 255.255.255.0; vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/ioam-nova-tgen/fabric_sw_2.conf cli-listen localhost:5002 } stats {default} plugins {plugin dpdk_plugin.so { disable } } statseg {per-node-counters on};echo "Sleeping for 10 seconds";sleep 10; nohup vpp_prometheus_export  &'
sudo lxc-attach -n fabric3 --  nohup bash -c 'ifconfig eth0 10.0.3.8 netmask 255.255.255.0; vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/ioam-nova-tgen/fabric_sw_3.conf cli-listen localhost:5002 } stats {default} plugins {plugin dpdk_plugin.so { disable } } statseg {per-node-counters on};echo "Sleeping for 10 seconds";sleep 10; nohup vpp_prometheus_export  &'
sudo lxc-attach -n fabric4 --  nohup bash -c 'ifconfig eth0 10.0.3.9 netmask 255.255.255.0; vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/ioam-nova-tgen/fabric_sw_4.conf cli-listen localhost:5002 } stats {default} plugins {plugin dpdk_plugin.so { disable } } statseg {per-node-counters on};echo "Sleeping for 10 seconds";sleep 10; nohup vpp_prometheus_export  &'
sudo lxc-attach -n spine1 --  nohup bash -c 'ifconfig eth0 10.0.3.10 netmask 255.255.255.0; vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/ioam-nova-tgen/spine_sw_1.conf cli-listen localhost:5002 } stats {default} plugins {plugin dpdk_plugin.so { disable } } statseg {per-node-counters on};echo "Sleeping for 10 seconds";sleep 10; nohup vpp_prometheus_export  &'
sudo lxc-attach -n spine2 --  nohup bash -c 'ifconfig eth0 10.0.3.11 netmask 255.255.255.0; vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/ioam-nova-tgen/spine_sw_2.conf cli-listen localhost:5002 } stats {default} plugins {plugin dpdk_plugin.so { disable } } statseg {per-node-counters on};echo "Sleeping for 10 seconds";sleep 10; nohup vpp_prometheus_export &'

#configure the TGNs
sudo lxc-attach -n TGN1 -- nohup bash -c 'ip -6 address add db11::1/64 dev l_TGN11 ' 
sudo lxc-attach -n TGN1 -- nohup bash -c 'ip -6 route add default via db11::2' 
sudo lxc-attach -n TGN2 -- nohup bash -c 'ip -6 address add db12::1/64 dev l_TGN21' 
sudo lxc-attach -n TGN2 -- nohup bash -c 'ip -6 route add default via db12::2'

#configure the rack4 tap0 with ip address
sudo lxc-attach -n rack4 -- nohup bash -c 'ifconfig tap0 10.255.0.254 netmask 255.255.255.0'
