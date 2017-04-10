#!/bin/bash

USAGETXT="Usage: connect_lxc.sh -c <Path of config file> [-f <Log File Path>] [-d for delete operation ]"

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
    if [ "$DEL" == 0 ]; then
    	log_print "Creating bridge $bridge"
	    sudo brctl addbr $bridge >> $LOG_FILE
		if [ $? -ne 0 ]; then
			log_print "\tError with $?"
			exit $?
		else
			log_print "\tSuccess"
		fi

		log_print "Add interfaces to bridge - $p"
	    sudo brctl addif $p >> $LOG_FILE
		if [ $? -ne 0 ]; then
			log_print "\tError with $?"
			exit $?
		else
			log_print "\tSuccess"
		fi

		log_print "Bring up bridge - $bridge"
	    sudo ifconfig $bridge up >> $LOG_FILE
		if [ $? -ne 0 ]; then
			log_print "\tError with $?"
			exit $?
		else
			log_print "\tSuccess"
		fi
	else
		log_print "Bring down bridge - $bridge"
	    sudo ifconfig $bridge down >> $LOG_FILE
		if [ $? -ne 0 ]; then
			log_print "\tError with $?"
			exit $?
		else
			log_print "\tSuccess"
		fi

		log_print "Deleting bridge $bridge"
	    sudo brctl delbr $bridge >> $LOG_FILE
	    if [ $? -ne 0 ]; then
			log_print "\tError with $?"
			exit $?
		else
			log_print "\tSuccess"
		fi
    fi
done <"$CONFIG"

