# Use VPP with lxc/docker

1. Install lxc on ubuntu:
    

    sudo apt-get install lxc lxctl lxc-templates

2. Copy the template file lxc-vpp-ext to /usr/share/lxc/templates
3. To start creating multiple containers and connect them, use the script launch_lxc.sh. 
	
        Usage: launch_lxc.sh -n <Name of container> -t <Templete Name> -l <Number of VEth Links> [-f <Log File Path>] [-d for delete operation ]

	This script does:

		a. Start a lxc container with the name suggested in the command.
			
			sudo lxc-start -n <container_name>

		b. Create Veth pair with one end on Ubuntu host and other end on lxc conatiner namespace.

			#Create VEth pairs
			sudo ip link add name <link on host> type veth peer name <link to be moved to container>
			sudo ifconfig <link on host> up
			#Find LXC PID which will be lxc network namespace
			sudo lxc-info --name <container_name> | grep "PID:" | awk '{print $2}'
			#Move link to container namespace
			sudo ip link set netns <lxc-pid> dev <link to be moved to container>
			sudo nsenter -t $<lxc-pid> -n ifconfig <link to be moved to container> up

		c. Step b is repeated for the number links required.

		d. Veth Pair naming convention followed is 
			h_<container_name><link_id> : This will be interface in host name space which can be used to bridge across to other container.
			l_<container_name><link_id> : This will be interface in lxc name space which can be used by VPP.

4. Create multiple containers as required. To connect interfaces across container, use connect_lxc.sh.

        Usage: connect_lxc.sh -c <Path of config file> [-f <Log File Path>] [-d for delete operation ]

	This script would setup connection across Veth pairs as specified in Config file. Config file format is simple for now – its a file containing entries in below format. There can be multiple lines in a file.
 
        <BridgeName1> <Host Veth1> <Host Veth2> ……….. <Host Vethn>

5. Install VPP on host. By default VPP image will appear on all conatiners if templete lxc-vpp-ext is used.
	
        sudo dpkg -i <all vpp deb files>

6. Connect to lxc and start VPP.

	    sudo lxc-attach --name <Container Name>
	    sudo vpp unix { nodaemon log /tmp/vpp.log full-coredump cli-listen localhost:5002 } &

	If VPP complains about huge pages then do below config on host Ubuntu:
	
	    sudo sysctl -w vm.nr_hugepages=512 ---> May need to increase depending on number of container one wants to start.
	    echo "2048" > /proc/sys/vm/max_map_count

7. Within container connect to VPP and configure it to take over Veth interface exposed.
        
        telnet 0 5002
	    create host-interface name <link moved to container namespace>
        set interface ip address host-<link moved to container namespace> <IP4/6 address>/<Prefix len>
        set interface state host-<link moved to container namespace> up


8. Repeat step 7 for all interfaces within the container and across all containers.

9. For docker, same steps can be followed. Also refer to below links for docker networking with VPP:

        https://wiki.fd.io/view/VPP/Configure_VPP_TAP_Interfaces_For_Container_Routing
        https://wiki.fd.io/view/VPP/Configure_VPP_As_A_Router_Between_Namespaces

10. Sample topology and script usage can be found in the folder - example.
