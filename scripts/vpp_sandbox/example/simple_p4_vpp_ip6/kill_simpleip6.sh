#!/bin/bash

sudo ./launch_lxc.sh -n host1 -d
sudo ./launch_lxc.sh -n a -d
sudo ./launch_lxc.sh -n S1 -d
sudo ./launch_lxc.sh -n b -d
sudo ./launch_lxc.sh -n S2 -d
sudo ./launch_lxc.sh -n c -d
sudo ./launch_lxc.sh -n host2 -d
sudo ip link set host1_a down
sudo brctl delbr host1_a
sudo ip link set a_S1 down
sudo brctl delbr a_S1
sudo ip link set S1_b down
sudo brctl delbr S1_b
sudo ip link set b_S2 down
sudo brctl delbr b_S2
sudo ip link set S2_c down
sudo brctl delbr S2_c
sudo ip link set c_host2 down
sudo brctl delbr c_host2


