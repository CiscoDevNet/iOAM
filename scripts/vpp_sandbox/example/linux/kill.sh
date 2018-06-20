#!/bin/bash

sudo ./launch_lxc_nobridge.sh -n alpha -d
sudo ./launch_lxc_nobridge.sh -n beta -d
sudo ./launch_lxc_nobridge.sh -n athos -d
sudo ./launch_lxc_nobridge.sh -n porthos -d
sudo ./launch_lxc_nobridge.sh -n aramis -d
sudo ./connect_lxc_nobridge.sh -c ./example/linux/config_nobridge.txt -d -f ./example/linux/connect.log
