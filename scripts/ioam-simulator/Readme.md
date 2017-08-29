# Overview

network.py will create a leaf-spine topology with a configurable
number of leaf and spine nodes.
It will then simulate flows through this network and dump ioam trace data.

```
python network.py --help
usage: network.py [-h] [-l LEAF] [-s SPINE] [-f FLOWS] [-n NOOFSECONDS]

optional arguments:
  -h, --help            show this help message and exit
  -l LEAF, --leaf LEAF  number of leaf nodes
  -s SPINE, --spine SPINE
                        number of spine nodes
  -f FLOWS, --flows FLOWS
                        number of flows
  -n NOOFSECONDS, --noofseconds NOOFSECONDS
                        number of seconds of data
```

## Pre-install

```
sudo pip install python-dateutil
sudo pip install networkx
sudo pip install ipaddr
```


## Example
To generate a topology of 2 leaf nodes and a spine node and create 2 random flows,
with 5 seconds of ioam trace data collected for the flows do this:

```
python network.py -l 2 -s 1 -f 2 -n 5
```

