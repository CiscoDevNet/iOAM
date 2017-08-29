import networkx as nx
import ipaddr
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

class flow:
    flow_seq_no = 1

    def __init__(self, mynet, sourceNode, destinationNode, pps = 100):
        self.sourceNode = sourceNode
        self.destinationNode = destinationNode
        self.mynet = mynet
        self.flow_id = flow.flow_seq_no
        flow.flow_seq_no += 1
        self.start_time = datetime.datetime.utcnow()
        self.epoch_ms = self.TimestampMillisec64()
        self.pps = pps #packets per second
        self.set5Tuple()
        list = mynet.findPath(sourceNode,destinationNode)
        ecmp_path_index = self.flow_id % len(list)
        self.path = list[ecmp_path_index]
        self.ioamSeqNo = int(self.start_time.strftime('%Y%m%d'))


    def set5Tuple(self):
        self.sourceip6 = self.mynet.get_ip6_for_flow(self.sourceNode, self.flow_id)
        self.destip6 = self.mynet.get_ip6_for_flow(self.destinationNode, self.flow_id)
        self.sourceport = randint(1000,50000)
        self.destport = randint(1000,50000)
        self.record_hdr = str(self.sourceip6)+"-"+str(self.sourceport)
        self.record_hdr += " to "
        self.record_hdr += str(self.destip6)+"-"+str(self.destport)
        self.record_hdr += " "
        #print("flow id = " + str(self.flow_id) + self.record_hdr + self.start_time.strftime("%c"))

    def TimestampMillisec64(self):
        return int((self.start_time - datetime.datetime(year=1970, month=1, day=1)).total_seconds() * 1000)

    def updateNodeResource(self, start_time, end_time):
        if start_time < self.start_time:
            return # this flow didnt exist in the given window
        for node_p in self.path:
            self.mynet.nodes[node_p].update_cpps(self.pps/1e6)

    def updateFlowPPS(self, newpps):
        self.pps = newpps

    def printFlowRecords(self, start_time, end_time):
        global local_tz
        if start_time < self.start_time:
            return
        time_elapsed = start_time - self.start_time
        start_ioam_seqno = self.ioamSeqNo + time_elapsed.seconds * self.pps
        number_of_packets = (end_time.second - start_time.second) * self.pps
        end_ioam_seqno = start_ioam_seqno + number_of_packets
        seq_no_list = range(start_ioam_seqno,end_ioam_seqno)
        drop = 0
        for node_p in self.path:
            if (self.mynet.nodes[node_p].percent_drop):
                drop_at_this_node  = number_of_packets * self.mynet.nodes[node_p].percent_drop / 100
                drop += drop_at_this_node
                number_of_packets -= drop_at_this_node
        for i in range(0,int(drop)):
            remove_index = x = randint(0, (end_ioam_seqno-start_ioam_seqno) - 1)
            if (x > 1):
            	remove_index = int(math.sin((1 / x) * (1 / (1 - x))))
            while (seq_no_list[remove_index] == 0):
                remove_index = (remove_index + 1) % (end_ioam_seqno-start_ioam_seqno)
            seq_no_list[remove_index] = 0
        for i in range(0,len(seq_no_list)):
            if (seq_no_list[i] == 0):
                continue
            record = self.record_hdr
            this_flow_packet_time_ms = self.epoch_ms + (1000 / self.pps) * seq_no_list[i]
            dt = datetime.datetime.fromtimestamp(this_flow_packet_time_ms / 1e3, local_tz)
#            record += dt.strftime('%Y%m%d %X')
#            record += " "
            record += str(seq_no_list[i])
            str_path = " "
            first = True
            begin_time = dt.replace(minute=0)
            timestamp = begin_time.microsecond / 1e3
            for node_p in self.path:
                timestamp += (self.mynet.nodes[node_p].packet_processing_time_ms) #node processing time
                if not first:
                    str_path += " -> "
                    # assuming 64B packet size - link delay in ms = 64 * 1e3/(link_speed_in_Gbps/8 * 1e9)
                    timestamp += 8 / (self.mynet.gr.edge[previous_node][node_p]['speed_gbps'] * 1e6) #link processing time
                str_path += str(self.mynet.nodes[node_p].node_id) + ", "
                str_path += str(timestamp)
                first = False
                previous_node = node_p
            record += str_path
            print(record)

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
        queue_time_ms = 0.0001
        queue_depth_packets = queue_depth/1e6 #queue depth in MPPS
        self.cpps += flow_pps
        if (self.cpps > (self.mpps + queue_depth_packets)):
            self.drop_pps = self.cpps - (self.mpps + queue_depth_packets)
            self.percent_drop = 100 * self.drop_pps/self.cpps
            print(self.name + " " + datetime.datetime.now().strftime("%c") + " experiencing drop rate" + str(self.percent_drop))
        elif (self.cpps > self.mpps):
            self.packet_processing_time_ms = 1000 / (self.mpps * 1e6) + \
                                             (self.cpps - self.mpps) * queue_time_ms #add queuing delay
            print(self.name + " " + datetime.datetime.now().strftime("%c") + " experiencing queueing delay" + str(self.percent_drop))


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
            this_flow = flow(my_network, "leaf"+str(source), "leaf"+str(dest))
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
    for i in range(0,number_of_seconds_of_data):
        window_end = window_start + datetime.timedelta(seconds=1)
        #print("Starting data creation for flows in the window "+
             # window_start.strftime("%c") + " to "+window_end.strftime("%c"))
        for flow in flowList:
            flow.updateNodeResource(window_start, window_end)
        for flow in flowList:
            flow.printFlowRecords(window_start, window_end)
        window_start = window_end
        for node in my_network.nodes:
            my_network.nodes[node].reset_cpps()
        #simulate delay in the next window
        if i == delay_window:
            flowList[higher_rate_flow].updateFlowPPS(150)
        if i == drop_window:
            flowList[drop_rate_flow].updateFlowPPS(300)
        if i == (delay_window + 1):
            flowList[higher_rate_flow].updateFlowPPS(100)
        if i == (drop_window + 1):
            flowList[drop_rate_flow].updateFlowPPS(100)







