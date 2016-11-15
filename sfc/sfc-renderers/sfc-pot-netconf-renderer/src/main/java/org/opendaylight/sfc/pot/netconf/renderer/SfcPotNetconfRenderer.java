/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.sfc.pot.netconf.renderer;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.sfc.pot.netconf.renderer.listener.SfcPotNetconfNodeListener;
import org.opendaylight.sfc.pot.netconf.renderer.listener.SfcPotNetconfRSPListener;
import org.opendaylight.sfc.pot.netconf.renderer.provider.api.SfcPotNetconfNodeManager;
import org.opendaylight.sfc.pot.netconf.renderer.provider.api.SfcPotNetconfIoamAPI;
import org.opendaylight.sfc.provider.OpendaylightSfc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialize all necessary SFC Proof of Transit Netconf Renderer components
 *
 * @author  Srihari Raghavan (srihari@cisco.com)
 * @version 0.1
 * @since   2016-09-01
 * @see     https://github.com/CiscoDevNet/iOAM
 */
public class SfcPotNetconfRenderer {

    final OpendaylightSfc opendaylightSfc = OpendaylightSfc.getOpendaylightSfcObj();

    private final SfcPotNetconfRSPListener sfcPotNetconfRSPListener;
    private final SfcPotNetconfIoamAPI     sfcPotNetconfIoamAPI;
    private final SfcPotNetconfNodeManager nodeManager;
    private final SfcPotNetconfNodeListener nodeListener;

    private static final Logger LOG = LoggerFactory.getLogger(SfcPotNetconfRenderer.class);
    private static final String POT_STR = "ioam";

    public SfcPotNetconfRenderer(DataBroker dataBroker,
                  BindingAwareBroker bindingAwareBroker) {

        nodeManager = new SfcPotNetconfNodeManager(dataBroker, bindingAwareBroker, POT_STR);
        nodeListener = new SfcPotNetconfNodeListener(dataBroker, nodeManager, POT_STR);

        sfcPotNetconfIoamAPI = new SfcPotNetconfIoamAPI(nodeManager);

        /* Add a listener to handle RSP updates */
        sfcPotNetconfRSPListener = new SfcPotNetconfRSPListener(opendaylightSfc,
                                                                sfcPotNetconfIoamAPI);
    }

    public void unregisterListeners() {
    }

    public void close() {
        sfcPotNetconfRSPListener.getDataChangeListenerRegistration().close();
        nodeListener.getRegistrationObject().close();
    }
}
