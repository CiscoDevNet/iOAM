# Sample Topology and Configs

To create a topology as shown below, follow below stpes:

![Alt text](./topology.png?raw=true "Topology")

Steps:

1. Install LXC and copy templete file.

		sudo apt-get install -y lxc lxctl lxc-templates
		cp lxc-vpp-ext /usr/share/lxc/templates/lxc-vpp-ext

2. Start creating containers:

		sudo ./launch_lxc.sh -n client -t vpp-ext -l 2
		sudo ./launch_lxc.sh -n manycast -t vpp-ext -l 2
		sudo ./launch_lxc.sh -n router -t vpp-ext -l 8 
		sudo ./launch_lxc.sh -n server1 -t vpp-ext -l 2 
		sudo ./launch_lxc.sh -n server2 -t vpp-ext -l 2 
		sudo ./launch_lxc.sh -n server3 -t vpp-ext -l 2

3. Connect Containers:

		sudo ./connect_lxc.sh -c config.txt

4. Connect to each container and start VPP with startup config files provided.

		Client lxc:
		vpp unix { log /tmp/vpp.log full-coredump startup-config client.conf cli-listen localhost:5002 }

		Router lxc:
		vpp unix { log /tmp/vpp.log full-coredump startup-config router.conf cli-listen localhost:5002 }

		Manycast lxc:
		vpp unix { log /tmp/vpp.log full-coredump startup-config manycast.conf cli-listen localhost:5002 }

		Server1 lxc:
		vpp unix { log /tmp/vpp.log full-coredump startup-config server1.conf cli-listen localhost:5002 }

		Server2 lxc:
		vpp unix { log /tmp/vpp.log full-coredump startup-config server2.conf cli-listen localhost:5002 }

		Server3 lxc:
		vpp unix { log /tmp/vpp.log full-coredump startup-config server3.conf cli-listen localhost:5002 }

5. To delete the topology and clear all containers:

		sudo ./launch_lxc.sh -n manycast -d
		sudo ./launch_lxc.sh -n router -d
		sudo ./launch_lxc.sh -n server1 -d
		sudo ./launch_lxc.sh -n server2 -d
		sudo ./launch_lxc.sh -n server3 -d
