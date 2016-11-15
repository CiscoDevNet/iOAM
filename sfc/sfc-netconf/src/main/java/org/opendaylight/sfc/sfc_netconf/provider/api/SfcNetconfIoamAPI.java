/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.sfc.sfc_netconf.provider.api;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.sfc.provider.api.*;

import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.sb.trace.rev160512.IoamTraceConfig;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.sb.trace.rev160512.ioam.trace.config.TraceConfig;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.sb.trace.rev160512.ioam.trace.config.TraceConfig.TraceOp;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.sb.trace.rev160512.ioam.trace.config.TraceConfig.TraceTsp;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.sb.trace.rev160512.ioam.trace.config.TraceConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.sb.trace.rev160512.ioam.trace.config.trace.config.NodeInterfaces;

import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.path.rev151129.*;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.path.rev151129.rendered.ioam.paths.*;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.path.rev151129.rendered.ioam.paths.rendered.ioam.path.*;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.scv.rev160602.ScvProfiles;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.scv.rev160602.ScvProfilesBuilder;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.scv.rev160602.scv.profiles.ScvProfile;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.scv.rev160602.scv.profiles.ScvProfileBuilder;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.acl.rev151001.Actions1;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.acl.rev151001.Matches1;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.rsp.rev140701.rendered.service.paths.RenderedServicePath;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.rsp.rev140701.rendered.service.paths.rendered.service.path.RenderedServicePathHop;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.scf.rev140701.service.function.classifiers.ServiceFunctionClassifier;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.scf.rev140701.service.function.classifiers.service.function.classifier.SclServiceFunctionForwarder;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunction;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfcv.rev150717.*;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfcv.rev150717.sfcv.algorithm.ext.AlgorithmParameters;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfcv.rev150717.sfcv.algorithm.ext.algorithm.parameters.PolyParams;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfcv.rev150717.sfcv.algorithm.ext.algorithm.parameters.poly.params.PolyParameters;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfcv.rev150717.sfcv.algorithm.ext.algorithm.parameters.poly.params.poly.parameters.*;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfcv.rev150717.sfcv.algorithm.ext.algorithm.parameters.poly.params.poly.parameters.poly.parameter.Coeffs;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfcv.rev150717.sfcv.hop.secret.algorithm.type.Poly;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfcv.rev150717.sfcv.hop.secret.algorithm.type.poly.PolySecrets;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfcv.rev150717.sfcv.hop.secret.algorithm.type.poly.poly.secrets.*;

//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.ServiceFunctionForwarder;
//import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.service.function.paths.ServiceFunctionPath;


import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.AccessLists;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.AclBase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.Ipv4Acl;
//import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.Acl;
//import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.AclBuilder;
//import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.Ace;
//import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.AccessListEntriesBuilder;
//import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.AceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.*;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.matches.ace.type.*;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160218.access.lists.acl.access.list.entries.ace.matches.ace.type.ace.ip.ace.ip.version.*;

//import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.math.BigInteger;
import java.util.*;


/**
 * This class is used to handle south-bound configuration generation for SFC verification.
 *
 * @author Xiao Liang, Srihari Raghavan (srihari@cisco.com)
 * @version 0.1
 * @see org.opendaylight.sfc.sfc_netconf.provider.api.SfcNetconfIoamAPI
 *      <p>
 * @since 2015-08-26
 */
public class SfcNetconfIoamAPI {
    private static final String ACE_IP = "AceIp";
    private static final String ACE_IPV6 = "AceIpv6";
    private static final long APP_DATA_VAL = 16;
    private static final int SB_MARK_VALUE = 7;
    private static final long SB_BITMASK_VALUE = 32;
    private final java.lang.Class<? extends AclBase> ACL_TYPE = Ipv4Acl.class;
    private final static Logger LOG = LoggerFactory.getLogger(SfcNetconfIoamAPI.class);

    public static final InstanceIdentifier<IoamTraceConfig> IOAM_NODE_PROFILES_IID =
            InstanceIdentifier.create(IoamTraceConfig.class);

    public static final InstanceIdentifier<AccessLists> ACL_IID =
            InstanceIdentifier.create(AccessLists.class);

    private class Config {
        NodeId nodeId;
        InstanceIdentifier iid;
        Config(NodeId nodeId, InstanceIdentifier iid) {
            this.nodeId = nodeId;
            this.iid = iid;
        }
    }

    public Map<String, List<Config>> pathConfig;

    private SfcNetconfIoamAPI() {
        pathConfig = new HashMap<>();
    }

    private static SfcNetconfIoamAPI api = null;

    /**
     * Returns an SfcNetconfIoamAPI singleton object.
     *
     * @return SfcNetconfIoamAPI Object
     */
    public static SfcNetconfIoamAPI getSfcNetconfIoamAPI() {
        if (api != null) {
            return api;
        }
        synchronized (SfcNetconfIoamAPI.class) {
            if (api != null) {
                return api;
            }
            api = new SfcNetconfIoamAPI();
        }
        return api;
    }


    private TraceConfig buildTraceSBProfile(String name, String acl, Short t_type, short node_idx,
                                               short nElt, TraceOp tr_op, String exportProfile, List<NodeInterfaces> nodeInterfaces, String transEncapProfileName) {
        Long node_idx_l = new Long(node_idx);
        TraceTsp tr_tsp = TraceTsp.Milliseconds;

        TraceConfigBuilder builder = new TraceConfigBuilder();

        Short numElt = new Short(nElt);

        Long appData = new Long(APP_DATA_VAL);

        builder.setTraceConfigName(name)
               .setTraceTsp(tr_tsp)
               .setNodeId(node_idx_l)
               .setTraceType(t_type)
               .setTraceNumElt(numElt)
               .setTraceAppData(appData)
               .setTraceOp(tr_op)
               .setAclName(acl)
               .setDataExportProfileName(exportProfile)
               .setNodeInterfaces(nodeInterfaces)
               .setTransportEncapProfileName(transEncapProfileName);

        return builder.build();
    }


    private void configSFCT(List<Config> configList, final NodeId nodeId, TraceConfig profile) {
        InstanceIdentifier iid = IOAM_NODE_PROFILES_IID.child(TraceConfig.class, profile.getKey());
        LOG.warn("SRI:configSFCT: Sending SFCT config:{} to node:{}",profile, nodeId.getValue());
        if (SfcNetconfReaderWriterAPI.merge(nodeId, LogicalDatastoreType.CONFIGURATION, iid, profile)) {
            if (configList != null) {
                configList.add(new Config(nodeId, iid));
            }
        } else {
        }
    }

    public void processRipCreate(RenderedIoamPath rip) {
        String ripName = rip.getName();
        Short t_type = null;
        List<Config> configList = new ArrayList<>();

        LOG.warn("SRI:inside processRipCreate");
        t_type = rip.getSbTraceType();
        List<SbNodesBorder> nodesBorderList = rip.getSbNodesBorder();
        List<SbNodesInternal> nodesInternalList = rip.getSbNodesInternal();
        short nElt = (short)(nodesBorderList.size() + nodesInternalList.size());
        for (SbNodesBorder sbb: nodesBorderList) {
            TraceOp tr_op;
            NodeId sfNode = sbb.getNetconfNode();
            /* TODO: currently hardcoded. first node in border nodes is encap */
            if (sbb.getIndex().intValue() == 1) {
                tr_op = TraceOp.Add;
            } else {
                tr_op = TraceOp.Remove;
            }

            TraceConfig traceSBProfile =
                    buildTraceSBProfile(ripName, "testAcl", t_type, (short)sbb.getNodeId().intValue(), nElt, tr_op, null, null, null);  //TODO:give real values instead of null
            String aclName = "testAcl";
            /*
            Acl acl = removeAclAugmentation(SfcProviderAclAPI.readAccessList(aclName, ACL_TYPE));
             try{
            configSFF(configList, sfNode, acl);}catch(Exception e){ LOG.info("sfNode {}", sfNode);
            }*/
            configSFCT(configList, sfNode, traceSBProfile);
        }

        for (SbNodesInternal sbi: nodesInternalList) {
            TraceOp tr_op = TraceOp.Update;
            NodeId sfNode = sbi.getNetconfNode();

            TraceConfig traceSBProfile =
                    buildTraceSBProfile(ripName, "testAcl", t_type, (short)sbi.getNodeId().intValue(), nElt, tr_op, null, null, null);  //TODO:give real values instead of null
            configSFCT(configList, sfNode, traceSBProfile);
        }

        pathConfig.put(ripName, configList);
    }

    public void processRipDelete(RenderedIoamPath rip) {
        List<Config> configList = pathConfig.get(rip.getName());
        if (configList != null) {
            for (Config cfg : configList) {
                SfcNetconfReaderWriterAPI.delete(cfg.nodeId, LogicalDatastoreType.CONFIGURATION, cfg.iid);
            }
            pathConfig.remove(rip.getName());
        }
    }
}
