import networkx as nx
import ipaddr
import random
from random import randint
import datetime
from argparse import ArgumentParser
from dateutil.tz import tzlocal
import math

'''
To simulate packet loss & delay
test with -l 2 -s 1 -f 2 and
spine_node_processing = 0.000001  #  MPPS
leaf_node_processing = 0.000001  #  MPPS
link_speed_gbps = 1  # link speed in Gbps
queue_depth = 100
'''

spine_node_processing = 12500*1e-6 #  MPPS
leaf_node_processing = 2500*1e-6 #  MPPS
link_speed_gbps = 1 #link speed in Gbps
queue_depth = 100
local_tz = tzlocal()
flow_pps = 100


def TimestampMillisec64(fortime):
    return int((fortime - datetime.datetime(year=1970, month=1, day=1)).total_seconds() * 1000)


class iOAMTraceRecord:
    def __init__(self, created_at, flow_id, seq_no, path_trace):
        self.flow_id = flow_id
        self.created_at = created_at
        self.seq_no = seq_no
        self.path_trace = path_trace

    def __repr__(self):
        string_record = self.flow_id + " " + str(self.seq_no) + " "
        path = ""
        first = True
        for node_trace in self.path_trace:
            if not first:
                path += " -> "
            first = False
            first_elm = True
            for element in node_trace:
                if not first_elm:
                    path += ", "
                path += str(element)
                first_elm = False
        return string_record + " " + path +"\n"

    def __cmp__(self, other):
        return (self.created_at - other.created_at)


class flow:
    flow_seq_no = 1

    def __init__(self, mynet, sourceNode, destinationNode, pps = 100):
        self.sourceNode = sourceNode
        self.destinationNode = destinationNode
        self.mynet = mynet
        self.flow_id = flow.flow_seq_no
        flow.flow_seq_no += 1
        self.start_time = datetime.datetime.utcnow()
        self.epoch_ms = TimestampMillisec64(self.start_time)
        self.pps = pps #packets per second
        self.set5Tuple()
        list = mynet.findPath(sourceNode,destinationNode)
        ecmp_path_index = self.flow_id % len(list)
        self.path = list[ecmp_path_index]
        self.path_trace = []
        for node_p in self.path:
            self.path_trace.append(self.mynet.nodes[node_p].node_id)
        self.startSeqNo = self.ioamSeqNo = int(self.start_time.strftime('%Y%m%d'))
        self.lostSeq = []



    def set5Tuple(self):
        self.sourceip6 = self.mynet.get_ip6_for_flow(self.sourceNode, self.flow_id)
        self.destip6 = self.mynet.get_ip6_for_flow(self.destinationNode, self.flow_id)
        self.sourceport = randint(1000,50000)
        self.destport = randint(1000,50000)
        self.flow_id_string = str(self.sourceip6) + "-" + str(self.sourceport)
        self.flow_id_string += "_to_"
        self.flow_id_string += str(self.destip6) + "-" + str(self.destport)
        self.flow_id_string += " "
        #print("flow id = " + str(self.flow_id) + self.record_hdr + self.start_time.strftime("%c"))

    def updateNodeResource(self, start_time, end_time):
        if start_time < self.start_time:
            return # this flow didnt exist in the given window
        for node_p in self.path:
            self.mynet.nodes[node_p].update_cpps(self.pps/1e6)

    def updateFlowPPS(self, newpps):
        self.pps = newpps

    def skipRecords(self, noOfRecords):
        print("for flow " + self.flow_id_string + " skipped "+str(noOfRecords)+" at " + str(self.ioamSeqNo))
        self.ioamSeqNo += noOfRecords

    def appendFlowRecords(self, start_epoch, start_time, end_time, records, shuffle=False):
        global local_tz
        if start_time < self.start_time:
            return
        start_ioam_seqno = self.ioamSeqNo
        number_of_packets = int((end_time.second - start_time.second) * self.pps)
        end_ioam_seqno = start_ioam_seqno + number_of_packets
        self.ioamSeqNo = end_ioam_seqno
        seq_no_list = range(start_ioam_seqno,end_ioam_seqno)
        drop = 0
        for node_p in self.path:
            if (self.mynet.nodes[node_p].percent_drop):
                drop_at_this_node  = number_of_packets * self.mynet.nodes[node_p].percent_drop / 100
                drop += drop_at_this_node
                number_of_packets -= drop_at_this_node
        for i in range(0,int(drop)):
            remove_index = x = randint(0, (end_ioam_seqno - start_ioam_seqno) - 1)
            if (x > 1):
                remove_index = int(math.sin((1 / x) * (1 / (1 - x))))
            while (seq_no_list[remove_index] == 0):
                remove_index = (remove_index + 1) % (end_ioam_seqno-start_ioam_seqno)
            self.lostSeq.append(seq_no_list[remove_index])
            seq_no_list[remove_index] = 0
        interpacket_time_ms = (1000 / self.pps)
        this_flow_packet_time_ms = start_epoch
        if shuffle:
            random.shuffle(seq_no_list)
            print(" For flow " + self.flow_id_string + " out of order seq nos ", seq_no_list)
        for i in range(0,len(seq_no_list)):
            if (seq_no_list[i] == 0):
                continue
            dt = datetime.datetime.fromtimestamp(this_flow_packet_time_ms / 1e3, local_tz)
            first = True
            begin_time = dt.replace(minute=0)
            timestamp = begin_time.microsecond / 1e3
            path_timestamps = []
            for node_p in self.path:
                timestamp += self.mynet.nodes[node_p].packet_processing_time_ms  #node processing time
                if not first:
                    # assuming 64B packet size - link delay in ms = 64 * 1e3/(link_speed_in_Gbps/8 * 1e9)
                    timestamp += 8 / (self.mynet.gr.edge[previous_node][node_p]['speed_gbps'] * 1e6) #link processing time
                path_timestamps.append(timestamp)
                first = False
                previous_node = node_p
            records.append(iOAMTraceRecord(this_flow_packet_time_ms,
                                           self.flow_id_string,
                                           seq_no_list[i],
                                           list(zip(self.path_trace,path_timestamps))))
            this_flow_packet_time_ms = this_flow_packet_time_ms + interpacket_time_ms


class node:
    #mpps = 10 #million packet per seconds that this node can process
    #cpps = 0  #current MPPS at this node
    #node_id = 0
    baseNet = ipaddr.IPv6Network('2001:cafe::/32')
    possible48s = ipaddr.IPv6Network('2001:cafe::/32').subnet(16)
    def __init__(self, name, mpps = 10, node_id = 0):
        self.mpps = mpps
        self.node_id = node_id
        self.cpps = 0
        self.ip6net = node.possible48s[node_id]
        self.name = name
        self.packet_processing_time_ms = 1000 / (self.mpps * 1e6)
        self.drop_pps = 0
        self.percent_drop = 0
    def reset_cpps (self):
        self.cpps = 0
        self.packet_processing_time_ms = 1000 / (self.mpps * 1e6)
        self.drop_pps = 0
        self.percent_drop = 0
    def update_cpps (self, flow_pps):
        global queue_depth
        queue_time_ms = 1000 / (self.mpps * 1e6)
        queue_depth_packets = queue_depth/1e6 #queue depth in MPPS
        self.cpps += flow_pps
        if (self.cpps > (self.mpps + queue_depth_packets)):
            self.packet_processing_time_ms = 1000 / (self.mpps * 1e6) + \
                                             queue_depth_packets * queue_time_ms #add queuing delay
            self.drop_pps = self.cpps - (self.mpps + queue_depth_packets)
            self.percent_drop = 100 * self.drop_pps/self.cpps
            print(self.name + " " + datetime.datetime.now().strftime("%c") + " experiencing drop rate " + str(self.percent_drop))
        elif (self.cpps > self.mpps):
            self.packet_processing_time_ms = 1000 / (self.mpps * 1e6) + \
                                             (self.cpps - self.mpps) * queue_time_ms #add queuing delay
            print(self.name + " " + datetime.datetime.now().strftime("%c") + " experiencing queueing delay " +
                  str(self.packet_processing_time_ms))


class network:

    def createTopology(self):
        global leaf_node_processing, spine_node_processing, link_speed_gbps
        leaf_nodes = []
        for i in range(0, self.leaf):
            name = "leaf" + str(i)
            self.nodes[name] = node(name, leaf_node_processing, self.node_id_seq)
            leaf_nodes.append(self.nodes[name])
            self.gr.add_node(name, {'node_id': str(self.node_id_seq)})
            self.node_id_seq += 1

        for i in range(0, self.spine):
            name = "spine" + str(i)
            self.nodes[name] = node(name, spine_node_processing, self.node_id_seq)
            self.gr.add_node(name, {'node_id': str(self.node_id_seq)})
            self.node_id_seq += 1
            for leaf in leaf_nodes:
                #print(leaf.name, name)
                self.gr.add_edge(name, leaf.name, speed_gbps = link_speed_gbps)

    def print_all_shortest_paths(self, source, dest):
        p = nx.all_shortest_paths(self.gr, source, dest)
        for path in p:
            print path
            str_path = ""
            for node_p in path:
                str_path += self.gr.node[node_p]['node_id'] + "->"

            print str_path
            # nx.draw(gr,pos=nx.spring_layout(gr))
            # plt.savefig("network.png")
            # plt.show()

    def findPath(self, source, dest):
        paths = []
        for p in nx.all_shortest_paths(self.gr, source, dest):
            paths.append(p)
        return(paths)

    def get_ip6_for_flow(self, node_name, flow_id):
       if node_name in self.nodes:
           return(self.nodes[node_name].ip6net.ip + flow_id)
       else:
            return ipaddr.IPv6Network('::/128').ip

    def __init__(self, spine, leaf):
        self.node_id_seq = 1
        self.nodes = {}
        self.gr = nx.Graph()
        self.leaf = leaf
        self.spine = spine
        self.createTopology()




if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument("-l", "--leaf", help="number of leaf nodes",
                        )
    parser.add_argument("-s", "--spine", help="number of spine nodes",
                        )
    parser.add_argument("-f", "--flows", help="number of flows",
                        )
    parser.add_argument("-n", "--noofseconds", help="number of seconds of data",
                        )
    args = parser.parse_args()
    leaf_nodes = 12
    spine_nodes = 1
    flows = 2
    if args.leaf:
        leaf_nodes = int(args.leaf)
    if args.spine:
        spine_nodes = int(args.spine)
    if args.flows:
        flows = int(args.flows)
    if args.noofseconds:
        noofseconds = int(args.noofseconds)
    file_out = open('./ioam-data-'+datetime.datetime.utcnow().strftime('%Y%m%d')+'.txt', 'w+')
    my_network = network(spine_nodes, leaf_nodes)
    # my_network.print_all_shortest_paths("leaf1", "leaf0")
    # for i in range(1,20):
    #    print(my_network.get_ip6_for_flow("leaf"+str(i),1))
    flowList = []
    for i in range (0, flows):
        source = randint(0,leaf_nodes - 1)
        dest = randint(0,leaf_nodes - 1)
        if (source == dest):
            dest = (dest + 1) % (leaf_nodes)
        try:
            this_flow = flow(my_network, "leaf"+str(source), "leaf"+str(dest),flow_pps)
            flowList.append(this_flow)
        except nx.exception.NetworkXNoPath:
            print("no path from leaf"+str(source)+" to destination leaf"+str(dest))
        except:
            print("error creating flow from leaf" + str(source) + " to destination leaf" + str(dest))
    print("Number of flows created = "+str(len(flowList)))
    number_of_seconds_of_data = noofseconds
    data_start = flowList[0].start_time #earliest flows start time
    window_start = data_start
    delay_window = randint(0,number_of_seconds_of_data)
    higher_rate_flow = randint(0,len(flowList)-1)
    drop_window = randint(0,number_of_seconds_of_data)
    drop_rate_flow = randint(0,len(flowList)-1)
    burst_loss_flow = randint(0,len(flowList)-1)
    burst_loss_window = randint(0,number_of_seconds_of_data)
    shuffle_flow = randint(0,len(flowList)-1)
    for i in range(0,number_of_seconds_of_data):
        window_end = window_start + datetime.timedelta(seconds=1)
        #print("Starting data creation for flows in the window "+
             # window_start.strftime("%c") + " to "+window_end.strftime("%c"))
        records = []
        start_epoch = TimestampMillisec64(window_start)
        if i%2 ==0 && i == burst_loss_window:
            flowList[burst_loss_flow].skipRecords(100)
            burst_loss_window = randint(i, number_of_seconds_of_data)
        elif i == burst_loss_window:
            flowList[burst_loss_flow].skipRecords(1000)
            burst_loss_window = randint(i,number_of_seconds_of_data)
        for flow in flowList:
            flow.updateNodeResource(window_start, window_end)
        for index, flow in enumerate(flowList):
            flow.appendFlowRecords(start_epoch, window_start, window_end, records,
                                   ((index==shuffle_flow) and (i == burst_loss_window)))
        sortedRecs = sorted(records)
        for record in sortedRecs:
            file_out.write(str(record))

        window_start = window_end
        for node in my_network.nodes:
            my_network.nodes[node].reset_cpps()
        #simulate delay in the next window
        if i == delay_window:
            flowList[higher_rate_flow].updateFlowPPS(flow_pps * 1.3)
        if i == drop_window:
            flowList[drop_rate_flow].updateFlowPPS(flow_pps * 1.7)
        if i == (delay_window + 1):
            flowList[higher_rate_flow].updateFlowPPS(flow_pps)
        if i == (drop_window + 1):
            flowList[drop_rate_flow].updateFlowPPS(flow_pps)
    #Print stats
    print ("Output in "+file_out.name)
    print ("Number of spine nodes "+str(spine_nodes))
    print ("Number of leaf nodes "+str(leaf_nodes))
    print ("Number of flows "+str(len(flowList)))
    for flow in flowList:
        if len(flow.lostSeq) > 0:
            print("Flow "+flow.flow_id_string+" lost "+str(len(flow.lostSeq)))
            print(" lost seq nos ", flow.lostSeq)
    file_out.close()





