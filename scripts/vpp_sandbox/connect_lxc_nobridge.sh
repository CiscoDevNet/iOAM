#!/bin/bash

USAGETXT="Usage: connect_lxc_nobridge.sh -c <Path of config file> [-f <Log File Path>] [-d for delete operation ]"

LOG_FILE=""
CONFIG=""
DEL=0

function log_print {
    printf "$1\n"
    printf "$1\n" >> $LOG_FILE
}

while getopts "h?c:f:d" opt; do
    case "$opt" in
    h|\?)
        echo "$USAGETXT"
        exit 0
        ;;
    c)  CONFIG=$OPTARG
        ;;
    f)  LOG_FILE=$OPTARG
        ;;
    d)  DEL=1
        ;;
    esac
done

if [ "$CONFIG" == "" ]; then
    echo "Config file missing for delete operation"
    echo "$USAGETXT"
    exit -1
fi

if [ ! -f "$CONFIG" ]; then
    echo "File not found!"
    echo "$USAGETXT"
    exit -2
fi

if [ "$LOG_FILE" == "" ] ; then
    LOG_FILE="./connect_$CONFIG.log"
fi

echo "*********************$(date) Starting **********************" > $LOG_FILE

echo "CONFIG - $CONFIG, DEL - $DEL, LOG_FILE - $LOG_FILE"
echo -n "Press [ENTER] to continue,...: "
read var_name

while read p; do
    echo $p
    bridge=$(echo "$p" | awk '{print $1;}')
    link1=$(echo "$p" | awk '{print $2;}')
    link2=$(echo "$p" | awk '{print $3;}')
    IFS='_' read -ra nodes <<EOF
      $bridge
EOF
    client_pid_1=$(sudo lxc-info --name ${nodes[0]} | grep "PID:" | awk '{print $2}')
    client_pid_2=$(sudo lxc-info --name ${nodes[1]} | grep "PID:" | awk '{print $2}')
    echo "node 1 ns:$client_pid_1 node 2 ns:$client_pid_2 " >> $LOG_FILE
    if [ "$DEL" == 0 ]; then
    	log_print "Creating link $bridge"
	log_print "Create veth pair for link $link1 to $link2"
	sudo ip link add name $link1 type veth peer name $link2 >> $LOG_FILE
	if [ $? -ne 0 ]; then
	    log_print "\tError with $?"
	    exit $?
	else
	    log_print "\tSuccess"
	fi

	log_print "Moving $link1 to namespace $client_pid_1"
	sudo ip link set netns $client_pid_1 dev $link1 >> $LOG_FILE
	if [ $? -ne 0 ]; then
	    log_print "\tError with $?"
	    exit $?
	else
	    log_print "\tSuccess"
	fi

	log_print "Moving $link2 to namespace $client_pid_2"
	sudo ip link set netns $client_pid_2 dev $link2 >> $LOG_FILE
	if [ $? -ne 0 ]; then
	    log_print "\tError with $?"
	    exit $?
	else
	    log_print "\tSuccess"
	fi

	#Admin up
	log_print "Admin up veth pair for link $link1"
	sudo nsenter -t $client_pid_1 -n ifconfig $link1 up >> $LOG_FILE
	if [ $? -ne 0 ]; then
	    log_print "\tError with $?"
	    exit $?
	else
	    log_print "\tSuccess"
	fi

	#Admin up
	log_print "Admin up veth pair for link $link2"
	sudo nsenter -t $client_pid_2 -n ifconfig $link2 up >> $LOG_FILE
	if [ $? -ne 0 ]; then
	    log_print "\tError with $?"
	    exit $?
	else
	    log_print "\tSuccess"
	fi
    else
	log_print "Deleting link between $bridge"
	sudo ip link delete dev $link1 >> $LOG_FILE
	if [ $? -ne 0 ]; then
	    log_print "\tError with $?"
	else
	    log_print "\tSuccess"
	fi
    fi
done <"$CONFIG"

