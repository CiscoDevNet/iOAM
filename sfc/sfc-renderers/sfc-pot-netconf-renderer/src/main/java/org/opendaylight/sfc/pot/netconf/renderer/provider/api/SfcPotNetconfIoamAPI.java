/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.sfc.pot.netconf.renderer.provider.api;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.sfc.provider.api.SfcProviderServiceForwarderAPI;

import org.opendaylight.sfc.pot.netconf.renderer.utils.SfcPotNetconfReaderWriterAPI;

import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.sb.pot.rev160615.PotProfiles;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.sb.pot.rev160615.PotProfilesBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.sb.pot.rev160615.ProfileIndexRange;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.sb.pot.rev160615.pot.profile.PotProfileList;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.sb.pot.rev160615.pot.profile.PotProfileListBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.sb.pot.rev160615.pot.profiles.PotProfileSet;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.sb.pot.rev160615.pot.profiles.PotProfileSetBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.SffName;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.rsp.rev140701.rendered.service.paths.RenderedServicePath;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.rsp.rev140701.rendered.service.paths.rendered.service.path.RenderedServicePathHop;

import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.nb.pot.rev150717.PolyAlg;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.nb.pot.rev150717.RspIoamPotAugmentation;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.nb.pot.rev150717.RspIoamPotHopAugmentation;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.nb.pot.rev150717.ioam.pot.algorithm.ext.AlgorithmParameters;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.nb.pot.rev150717.ioam.pot.algorithm.ext.algorithm.parameters.PolyParams;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.nb.pot.rev150717.ioam.pot.algorithm.ext.algorithm.parameters.poly.params.PolyParameters;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.nb.pot.rev150717.ioam.pot.algorithm.ext.algorithm.parameters.poly.params.poly.parameters.PolyParameter;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.nb.pot.rev150717.ioam.pot.algorithm.ext.algorithm.parameters.poly.params.poly.parameters.poly.parameter.Coeffs;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.nb.pot.rev150717.ioam.pot.algorithm.ext.algorithm.parameters.poly.params.poly.parameters.poly.parameter.Lpcs;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.nb.pot.rev150717.ioam.pot.hop.secret.algorithm.type.Poly;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.nb.pot.rev150717.ioam.pot.hop.secret.algorithm.type.poly.PolySecrets;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.ioam.nb.pot.rev150717.ioam.pot.hop.secret.algorithm.type.poly.poly.secrets.PolySecret;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to send south-bound configuration SFC PoT via Netconf.
 *
 * @author Xiao Liang, Srihari Raghavan (srihari@cisco.com)
 * @version 0.1
 * @see org.opendaylight.sfc.pot.netconf.renderer.provider.api.SfcPotNetconfIoamAPI
 * @since 2015-08-26
 */
public class SfcPotNetconfIoamAPI {
    private static final long APP_DATA_VAL = 16;
    private static final long SB_BITMASK_VALUE = 32;
    private final SfcPotNetconfNodeManager nodeManager;
    private final static Logger LOG = LoggerFactory.getLogger(SfcPotNetconfIoamAPI.class);

    public static final InstanceIdentifier<PotProfiles> POT_PROFILES_IID =
            InstanceIdentifier.create(PotProfiles.class);


    private static class Config {
        NodeId nodeId;
        InstanceIdentifier iid;
        Config(NodeId nodeId, InstanceIdentifier iid) {
            this.nodeId = nodeId;
            this.iid = iid;
        }
    }

    public Map<String, List<Config>> pathConfig;

    public SfcPotNetconfIoamAPI(SfcPotNetconfNodeManager nodeManager) {
        pathConfig = new HashMap<>();
        this.nodeManager = nodeManager;
    }

    /**
     * Returns an PolyParameters object representing the PoT related augmentation from RSP.
     *
     * @param rsp Rendered Service Path from which augmentation needs to be got.
     * @return PolyParameters object.
     * @see sfc-ioam-nb-pot.yang
     */
    private PolyParameters getPotParameters(RenderedServicePath rsp) {
        PolyParameters params = null;
        RspIoamPotAugmentation potAugmentation = rsp.getAugmentation(RspIoamPotAugmentation.class);
        if (potAugmentation != null) {
            AlgorithmParameters algorithmParameters = potAugmentation.getAlgorithmParameters();
            if (potAugmentation.getIoamPotAlgorithm().equals(PolyAlg.class) &&
                    algorithmParameters instanceof PolyParams) {
                params = ((PolyParams) algorithmParameters).getPolyParameters();
            }
        }
        return params;
    }


    private List<BigInteger> getCoefficients(PolyParameter params) {
        ArrayList<BigInteger> coeffs = new ArrayList<>();
        for (Coeffs coeff : params.getCoeffs()) {
            coeffs.add(BigInteger.valueOf(coeff.getCoeff()));
        }
        return coeffs;
    }

    private List<Long> getLpcs(PolyParameter params) {
        ArrayList<Long> lpcs = new ArrayList<>();
        for (Lpcs lpc: params.getLpcs()) {
            lpcs.add(lpc.getLpc());
        }
        return lpcs;
    }

    /**
     * Returns an PotProfiles object representing the PoT related configuration.
     *
     * @param rsp Rendered Service Path
     * @return PolyParameters object.
     * @see sfc-ioam-nb-pot.yang
     */
    private PotProfiles buildProfile(String name, String acl, String rspName, PolyParameters params,
                                     PolySecrets secrets, int posIndex, int activeIndex) {
        Long maskval = Long.valueOf(SB_BITMASK_VALUE);
        List<BigInteger> coeffs;
        List<Long> lpcs;
        List<PolyParameter> paramList = params.getPolyParameter();
        List<PolySecret> secretList = secrets.getPolySecret();

        long numProfiles = paramList.size();

        PotProfilesBuilder pbuilder = new PotProfilesBuilder();

        ArrayList<PotProfileList> potProfileList = new ArrayList<>();

        for (int j = 0; j < numProfiles; j++) {
            PotProfileListBuilder builder = new PotProfileListBuilder();

            /* paramList from RSP typically seems to be out of order in its contents. Correct
             * the order and use it.
             */
            PolyParameter paramObj = null;
            for (int k = 0; k < paramList.size(); k++) {
                if (paramList.get(k).getPindex() == j) {
                    LOG.debug("buildProfile: param: actual index:{}, should have been:{}", k, j);
                    paramObj = paramList.get(k);
                }
            }

            PolySecret secretObj = null;
            for (int m = 0; m < secretList.size(); m++) {
                if (secretList.get(m).getPindex() == j) {
                    LOG.debug("buildProfile: secrets: actual index:{}, should have been:{}", m, j);
                    secretObj= secretList.get(m);
                }
            }

            coeffs  = getCoefficients(paramObj);
            lpcs    = getLpcs(paramObj);

            builder.setIndex(new ProfileIndexRange(Integer.valueOf(j)))
                   .setPrimeNumber(BigInteger.valueOf(paramObj.getPrime()))
                   .setLpc(BigInteger.valueOf(lpcs.get(posIndex)))
                   .setSecretShare(BigInteger.valueOf(secretObj.getSecretShare()))
                   .setPublicPolynomial(coeffs.get(posIndex))
                   .setBitmask(new BigInteger(maskval.toString()));

            if (secretObj.getSecret() != null) {
                builder.setValidator(true).setValidatorKey(BigInteger.valueOf(secretObj.getSecret()));
            } else {
                builder.setValidator(false);
            }

            potProfileList.add(builder.build());
        }

        PotProfileSetBuilder sbuilder = new PotProfileSetBuilder();

        sbuilder.setPotProfileList(potProfileList)
                .setName(name)
                .setActiveProfileIndex(new ProfileIndexRange(Integer.valueOf(activeIndex)))
                .setPathIdentifier(acl);

        ArrayList<PotProfileSet> potProfileSet = new ArrayList<>();
        potProfileSet.add(sbuilder.build());
        pbuilder.setPotProfileSet(potProfileSet);

        return pbuilder.build();
    }

    /* Sends out the configuration to the SB nodes using netconf*/
    private boolean configSF(List<Config> configList, final NodeId nodeId, PotProfiles profile) {
        LOG.info("configSF: Sending PoT config:{} to node:{}",profile, nodeId.getValue());
        InstanceIdentifier iid = POT_PROFILES_IID;
        if (SfcPotNetconfReaderWriterAPI.put(nodeId, LogicalDatastoreType.CONFIGURATION, iid, profile)) {
            LOG.info("Successfully configured SF node {}", nodeId.getValue());
            configList.add(new Config(nodeId, iid));
        } else {
            LOG.error("Error configuring SF node {} through NETCONF", nodeId.getValue());
            return false;
        }

        return true;
    }


    /*
     * This function processes RSP updates to send out related configuration for
     * PoT creation, renewal or refresh configuration options.
     */
    public void processRspUpdate(RenderedServicePath rsp) {
        Preconditions.checkNotNull(rsp);
        PolyParameters params = getPotParameters(rsp);
        String rspName = rsp.getName().getValue();
        List<Config> configList = new ArrayList<>();
        int i = 0;
        int posIndex = 0;
        int activeIndex = 0;
        SffName sffName;

        if (params == null) {
            LOG.debug("SFC Proof of Transit params not present for: {}", rsp.getName());
            //To handle the case where the RSP is updated to remove the PoT configuration
            //but leave the RSP itself without any other changes, delete the PoT
            //configuration if present.
            deleteRsp(rsp);
            return;
        }

        activeIndex = params.getActiveProfileIndex().getValue();

        List<RenderedServicePathHop> hopList = rsp.getRenderedServicePathHop();

        if (hopList != null) {
            for (RenderedServicePathHop h : hopList) {
                NodeId sffNode = null;
                sffName = h.getServiceFunctionForwarder();
                sffNode = getSffNodeId(sffName);
                if (sffNode == null) {
                    LOG.error("sffNode is null for sffName: {}", sffName);
                    return;
                }

                RspIoamPotHopAugmentation potHopAugmentation = h.getAugmentation(RspIoamPotHopAugmentation.class);

                if (potHopAugmentation != null &&
                        potHopAugmentation.getAlgorithmType().getImplementedInterface().equals(Poly.class)) {
                    PolySecrets secrets = ((Poly)potHopAugmentation.getAlgorithmType()).getPolySecrets();

                    /* Initially, all profiles are downloaded with invalid activeIndex */
                    PotProfiles profile =
                        buildProfile(rspName + '-' + h.getServiceIndex(), null, rspName, params,
                                     secrets, posIndex, -1);
                    if (!configSF(configList, sffNode, profile)) {
                        /* Error already logged */
                        return;
                    }
                    posIndex++;
                }
            }

            /* When the initial download is successful, the first node of the hop is
             * notified of what is the activeIndex.  This is as per the protocol required
             * at the SB nodes.
             */
            posIndex = 0;
            for (RenderedServicePathHop h : hopList) {
                NodeId sffNode = null;
                sffName = h.getServiceFunctionForwarder();
                sffNode = getSffNodeId(sffName);
                if (sffNode == null) {
                    LOG.error("sffNode is null for sffName: {}", sffName);
                    return;
                }

                RspIoamPotHopAugmentation potHopAugmentation = h.getAugmentation(RspIoamPotHopAugmentation.class);
                if (potHopAugmentation != null &&
                        potHopAugmentation.getAlgorithmType().getImplementedInterface().equals(Poly.class)) {
                    PolySecrets secrets = ((Poly)potHopAugmentation.getAlgorithmType()).getPolySecrets();

                    /* Initially, all profiles are downloaded with invalid activeIndex */
                    PotProfiles profile =
                        buildProfile(rspName + '-' + h.getServiceIndex(), null, rspName, params,
                                     secrets, posIndex, activeIndex);
                    if (!configSF(configList, sffNode, profile)) {
                        /* Error already logged */
                        return;
                    }
                }
                /* Exit as active index intended only for the first node */
                break;
            }
        }

        pathConfig.put(rspName, configList);

    }

    /*
     * This function processes RSP deletes to send out related configuration for
     * PoT deletion configurations to the nodes.
     */
    public void deleteRsp(RenderedServicePath rsp) {
        List<Config> configList = pathConfig.get(rsp.getName().getValue());
        if (configList != null) {
            for (Config cfg : configList) {
                SfcPotNetconfReaderWriterAPI.delete(cfg.nodeId, LogicalDatastoreType.CONFIGURATION, cfg.iid);
            }
            pathConfig.remove(rsp.getName().getValue());
        }
    }


    private NodeId getSffNodeId(SffName sffName) {
        // Read SFF from Controller CONF
        org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.ServiceFunctionForwarder sfcForwarder =
                SfcProviderServiceForwarderAPI.readServiceFunctionForwarder(sffName);
        if (sfcForwarder == null) {
            LOG.error("SFF name {} not found in data store", sffName.getValue());
            return null;
        }
        IpAddress sffMgmtIp = sfcForwarder.getIpMgmtAddress();
        if (sffMgmtIp == null) {
            LOG.error("Unable to obtain management IP for SFF {}", sffName.getValue());
            return null;
        }

        return nodeManager.getNodeIdFromIpAddress(new IpAddress(new Ipv4Address(sffMgmtIp.getIpv4Address()
                .getValue())));
    }
}
