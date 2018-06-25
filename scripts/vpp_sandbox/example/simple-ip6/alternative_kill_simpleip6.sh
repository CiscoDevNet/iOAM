#!/bin/bash

SANDBOXDIR="$HOME/git/iOAM/scripts/vpp_sandbox"

sudo ${SANDBOXDIR}/launch_lxc_nobridge.sh -n host1 -d
sudo ${SANDBOXDIR}/launch_lxc_nobridge.sh -n host2 -d
sudo ${SANDBOXDIR}/launch_lxc_nobridge.sh -n a -d
sudo ${SANDBOXDIR}/launch_lxc_nobridge.sh -n b -d
sudo ${SANDBOXDIR}/launch_lxc_nobridge.sh -n c -d
sudo ${SANDBOXDIR}/connect_lxc_nobridge.sh -c ${SANDBOXDIR}/example/simple-ip6/config_nobridge.txt -d -f ${SANDBOXDIR}/example/simple-ip6/connect.log
