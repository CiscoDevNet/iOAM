# Sample Topology and Configs

To create a topology as shown below, follow below stpes:

![Alt text](./topology.png?raw=true "Topology")

Steps:

1. Install LXC and copy templete file.

		sudo apt-get install -y lxc lxctl lxc-templates util-linux
		cp lxc-vpp-ext /usr/share/lxc/templates/lxc-vpp-ext

2. Edit /usr/share/lxc/templetes/lxc-vpp-ext file:
   Look for below lines:

		# Add VPP Specific mounts here
		#lxc.mount.entry = <Local directory> scratch none ro,bind 0 0

   Uncomment lxc.mount.entry line and replace <Local directory> by path up to <git_checkout_path>/scripts/vpp_sandbox

3. Start creating containers:

		sudo ./launch_lxc.sh -n TGN1 -t vpp-ext -l 4
		sudo ./launch_lxc.sh -n TGN2 -t vpp-ext -l 4
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
		sudo ./launch_lxc.sh -n spine3 -t vpp-ext -l 4
		sudo ./launch_lxc.sh -n spine4 -t vpp-ext -l 4

4. Connect Containers:

		sudo ./connect_lxc.sh -c ./example/udp_probe/config.txt -f ./example/udp_probe/connect.log

5. Connect to each container and start VPP with startup config files provided.

		Rack1 lxc:
		sudo lxc-attach -n rack1
		vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/rack_sw_1.conf cli-listen localhost:5002 } &

		Rack2 lxc:
                sudo lxc-attach -n rack2
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/rack_sw_2.conf cli-listen localhost:5002 } &

		Rack3 lxc:
                sudo lxc-attach -n rack3
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/rack_sw_3.conf cli-listen localhost:5002 } &

		Rack4 lxc:
                sudo lxc-attach -n rack4
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/rack_sw_4.conf cli-listen localhost:5002 } &

		Fabric1 lxc:
                sudo lxc-attach -n fabric1
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/fabric_sw_1.conf cli-listen localhost:5002 } &

		Fabric2 lxc:
                sudo lxc-attach -n fabric2
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/fabric_sw_2.conf cli-listen localhost:5002 } &

		Fabric3 lxc:
                sudo lxc-attach -n fabric3
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/fabric_sw_3.conf cli-listen localhost:5002 } &

		Fabric4 lxc:
                sudo lxc-attach -n fabric4
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/fabric_sw_4.conf cli-listen localhost:5002 } &

		Spine1 lxc:
                sudo lxc-attach -n spine1
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/spine_sw_1.conf cli-listen localhost:5002 } &

		Spine2 lxc:
                sudo lxc-attach -n spine2
                vpp unix { log /tmp/vpp.log full-coredump startup-config /scratch/example/udp_probe/spine_sw_2.conf cli-listen localhost:5002 } &


6. To delete the topology and clear all containers:

		sudo ./launch_lxc.sh -n rack1 -d
		sudo ./launch_lxc.sh -n rack2 -d
		sudo ./launch_lxc.sh -n rack3 -d
		sudo ./launch_lxc.sh -n rack4 -d
		sudo ./launch_lxc.sh -n fabric1 -d
		sudo ./launch_lxc.sh -n fabric2 -d
		sudo ./launch_lxc.sh -n fabric3 -d
		sudo ./launch_lxc.sh -n fabric4 -d
		sudo ./launch_lxc.sh -n spine1 -d
		sudo ./launch_lxc.sh -n spine2 -d
