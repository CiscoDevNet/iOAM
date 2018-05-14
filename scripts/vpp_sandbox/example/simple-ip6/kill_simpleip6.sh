#!/bin/bash

sudo ./launch_lxc_nobridge.sh -n host1 -d
sudo ./launch_lxc_nobridge.sh -n host2 -d
sudo ./launch_lxc_nobridge.sh -n a -d
sudo ./launch_lxc_nobridge.sh -n b -d
sudo ./launch_lxc_nobridge.sh -n c -d
sudo ./connect_lxc_nobridge.sh -c ./example/simple-ip6/config_nobridge.txt -d -f ./example/simple-ip6/connect.log
