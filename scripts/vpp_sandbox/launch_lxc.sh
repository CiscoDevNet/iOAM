#!/bin/bash

USAGETXT="Usage: launch_lxc.sh -n <Name of container> -t <Templete Name> -l <Number of VEth Links> [-f <Log File Path>] [-d for delete operation ]"

LOG_FILE=""
NAME=""
TEMPLETE=""
LINKS=0
DEL=0

function log_print {
    printf "$1\n"
    printf "$1\n" >> $LOG_FILE
}

while getopts "h?n:t:l:f:d" opt; do
    case "$opt" in
    h|\?)
        echo "$USAGETXT"
        exit 0
        ;;
    n)  NAME=$OPTARG
        ;;
    t)  TEMPLETE=$OPTARG
        ;;
    l)  LINKS=$OPTARG
        ;;
    f)  LOG_FILE=$OPTARG
        ;;
    d)  DEL=1
        ;;
    esac
done

if [ "$DEL" == 1 ] ; then
    if  [ "$NAME" == "" ] ; then
        echo "Container name missing for delete operation"
        echo "$USAGETXT"
        exit -1
    fi
elif [ "$NAME" == "" ] || [ "$TEMPLETE" == "" ] || [ "$LINKS" == 0 ] ; then
    echo "Invalid Input Params"
    echo "$USAGETXT"
    exit -2
fi

if [ "$LOG_FILE" == "" ] ; then
    LOG_FILE="./lxc_$NAME.log"
fi

echo "*********************$(date) Starting **********************" > $LOG_FILE

echo "NAME - $NAME, TEMPLETE - $TEMPLETE, LINKS - $LINKS, DEL - $DEL, LOG_FILE - $LOG_FILE"
#echo -n "Press [ENTER] to continue,...: "
#read var_name

#if delete operation then clear container
if [ "$DEL" == 1 ]; then
    log_print "Stopping $NAME Container"
    sudo lxc-stop -n $NAME >> $LOG_FILE
    log_print "Destroying $NAME Container"
    sudo lxc-destroy -n $NAME >> $LOG_FILE
    exit 0
fi

#Create the container from templete
log_print "Creating $NAME Container with templete $TEMPLETE"
sudo lxc-create -n $NAME -t $TEMPLETE >> $LOG_FILE
if [ $? -ne 0 ]; then
    log_print "\tError with $?"
    exit $?
else
    log_print "\tSuccess"
fi

#Start the container
log_print "Starting $NAME Container"
sudo lxc-start -n $NAME -d >> $LOG_FILE
if [ $? -ne 0 ]; then
    log_print "\tError with $?"
    exit $?
else
    log_print "\tSuccess"
fi

#Extract container PID
client_pid=$(sudo lxc-info --name $NAME | grep "PID:" | awk '{print $2}')
if [ $? -ne 0 ]; then
    log_print "Failed to get PID of container - Error with $?"
    exit $?
else
    log_print "Container PID - $client_pid"
fi
#ToDo - validation of pid

for (( i=1; i <= $LINKS; i++ ))
do
    #create veth pairs
	log_print "Create veth pair for link $NAME$i"
	sudo ip link add name h_$NAME$i type veth peer name l_$NAME$i >> $LOG_FILE
	if [ $? -ne 0 ]; then
	    log_print "\tError with $?"
	    exit $?
	else
	    log_print "\tSuccess"
	fi

	#Admin up veth pairs
	log_print "Admin up veth pair for link h_$NAME$i"
	sudo ifconfig h_$NAME$i up >> $LOG_FILE
	if [ $? -ne 0 ]; then
	    log_print "\tError with $?"
	    exit $?
	else
	    log_print "\tSuccess"
	fi

	#Move one end of veth pair to container namespace
	log_print "Moving eth_client1 to namespace $client_pid"
	sudo ip link set netns $client_pid dev l_$NAME$i >> $LOG_FILE
	if [ $? -ne 0 ]; then
	    log_print "\tError with $?"
	    exit $?
	else
	    log_print "\tSuccess"
	fi

	#Admin up
	log_print "Admin up veth pair for link l_$NAME$i"
	sudo nsenter -t $client_pid -n ifconfig l_$NAME$i up >> $LOG_FILE
	if [ $? -ne 0 ]; then
	    log_print "\tError with $?"
	    exit $?
	else
	    log_print "\tSuccess"
	fi

done

