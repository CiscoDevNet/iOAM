from scapy.all import *
import requests
import json

packets_received = 0
packets_lost = 0

def publish_to_kafka (src, dst, trace_option, e2e_option):
    new_message = {}
    new_message['topic'] = "ioam-trace"
    new_message['src'] = src
    new_message['dst'] = dst
    elts_left = trace_option.getfieldval("elements-left")
    trace_elts = trace_option.getfieldval("trace-elt")
    total_elts = len(trace_elts)
    path_len = total_elts - elts_left
    last_timestamp = trace_elts[elts_left].getfieldval("timestamp")
    first_timestamp = trace_elts[total_elts-1].getfieldval("timestamp")
    new_message['end_to_end_delay']= last_timestamp - first_timestamp
    out_str = "%s to %s %s " % (src, dst, str(e2e_option.getfieldval("seq_np")))
    print "out_str = ", out_str
    for i in range(total_elts):
      if (i < elts_left):
        continue
      node_data = trace_elts[i]
      node_id = node_data.getfieldval("nodeID")
      timestamp = node_data.getfieldval("timestamp")
      new_message['node_'+str(path_len-i+1)+"_id"] = node_id
      out_str = out_str + "%s, %s -> " % (str(node_id),str(timestamp))
      print "out_str = ", out_str
    out_str = out_str.rstrip('-> ') + '\n'
    print "out_str = ", out_str
    fd = open("/tmp/ioam-data-sample.txt", "a+")
    fd.write(out_str)
    fd.close()
    print("Processed message is:")
    print(json.dumps(new_message))
    #producer.send_messages(("ioam_pp").encode(), json.dumps(new_message))
    return



class IOAM_NODE_TRACE(Packet):
  name = "iOAM node data"
  fields_desc = [ ByteField("hoplim",0),
                  BitField("nodeID",0, 24),
                  #ShortField("ingress_if_id",0),
                  #ShortField("egress_if_id",0),
                  IntField("timestamp", 0)]
                  #IntField("appdata", 0)]
  
  def extract_padding(self, s):
        return '', s
  
class IOAM_TRACE_OPTION(Packet):
  name = "iOAM Trace"
  fields_desc = [ ByteField("optionType",0),
                  ByteField("optionLen",0),
                  ByteField("trace-type",0),
                  ByteField("elements-left",0),
                  PacketListField("trace-elt",None, IOAM_NODE_TRACE,
                                  count_from = lambda pkt: (pkt.optionLen - 2)/8)]
  
  def extract_padding(self, s):
        return '', s

class IOAM_E2E(Packet):
  name = "iOAM E2E"
  fields_desc = [ ByteField("optionType",0),
                  ByteField("optionLen",0),
                  ByteField ("e2e_type",0),
                  ByteField ("e2e_res",0),
                  IntField ("seq_np", 0)]
               
  def extract_padding(self, s):
        return '', s
  
class IOAM_RECORD(Packet):
  name = "IOAM_RECORD"
  fields_desc = [ BitField("ioam6",0,192*8)]
  
  def extract_padding(self, s):
        return '', s
  
class IPFIX(Packet):
  name = "IPFIX"
  fields_desc = [ShortField("version", None),
                 ShortField("length", None),
                 IntField("exporttime", 0),
                 IntField("sequence", 0),
                 IntField("domainid", 0),
                 ShortField("setID", 0),
                 ShortField("setLen",0),
                 PacketListField("records",None, IOAM_RECORD,
                                 count_from = lambda pkt: (pkt.setLen - 4)/192)]

bind_layers( UDP, IPFIX, dport=4739)
                

#myreader = PcapReader("./trace.pcap")
while True:
  myreader = sniff(count=3, iface="tap0", filter="udp port 4739")
  for p in myreader:
    pkt = p.payload
    try:
      ipfix = pkt[IPFIX]
    except:
      print 'Invalid IPFIX packet found..'
      continue
    ipfix.summary()
    #hexdump(ipfix.payload)
    totallen = ipfix.getfieldval("setLen")
    #totallen = len(ipfix.payload)
    print "total len =", totallen
    print "No of records = ",(totallen - 4)/192
    records = ipfix.getfieldval("records")
    for p in records:
      v6p = IPv6(str(p))
      #v6p.show2()
      hbh = v6p[IPv6ExtHdrHopByHop]
      #hbh.show()
      trace_option = None
      e2e_option = None
      for option in hbh.getfieldval("options"):
        if (option.otype == 59):
          trace_option = IOAM_TRACE_OPTION(str(option))
          print "received trace option"
          hexdump(option.optdata)
          trace_option.show()
          #publish_to_kafka(v6p.getfieldval("src"), v6p.getfieldval("dst"),
          #                 trace_option)
        if (option.otype == 29):
          e2e_option = IOAM_E2E(str(option))
          print "received E2E option"
          hexdump(option.optdata)
          e2e_option.show()
      if trace_option and e2e_option:
          publish_to_kafka(v6p.getfieldval("src"), v6p.getfieldval("dst"),
                       trace_option, e2e_option)
  #continue_loop = 0

