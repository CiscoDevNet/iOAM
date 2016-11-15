/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.sfc.sfc_netconf.provider.listener;

import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.sfc.provider.OpendaylightSfc;
import org.opendaylight.sfc.sfc_netconf.provider.api.SfcNetconfIoamAPI;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.ioam.path.rev151129.rendered.ioam.paths.*;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 */
public class SfcNetconfRIPDataListener extends SfcNetconfAbstractDataListener {
    private static final Logger LOG = LoggerFactory.getLogger(SfcNetconfRIPDataListener.class);

    public SfcNetconfRIPDataListener(OpendaylightSfc opendaylightSfc) {
        setOpendaylightSfc(opendaylightSfc);
        setDataBroker(opendaylightSfc.getDataProvider());
        setInstanceIdentifier(OpendaylightSfc.RIP_ENTRY_IID);
        setDataStoreType(LogicalDatastoreType.OPERATIONAL);
        LOG.warn("\nSRI:Register for RenderedIoamPath:");
        registerAsDataChangeListener();
    }

    @Override
    public void onDataChanged(
            final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {

        Map<InstanceIdentifier<?>, DataObject> dataCreatedObject = change.getCreatedData();
        Map<InstanceIdentifier<?>, DataObject> dataOriginalDataObject = change.getOriginalData();

        // RIP creation
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : dataCreatedObject.entrySet()) {
            if (entry.getValue() instanceof RenderedIoamPath) {
                RenderedIoamPath rip = (RenderedIoamPath) entry.getValue();
                LOG.warn("\nSRI:Created RenderedIoamPath: {}", rip.getName());
                SfcNetconfIoamAPI.getSfcNetconfIoamAPI().processRipCreate(rip);
            }
        }

        // RIP update
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change.getUpdatedData().entrySet()) {
            if (entry.getValue() instanceof RenderedIoamPath &&
                    !dataCreatedObject.containsKey(entry.getKey())) {
                RenderedIoamPath rip = (RenderedIoamPath) entry.getValue();
                LOG.warn("\nSRI:Updated RenderedIoamPath: {}", rip.getName());
                //SfcNetconfIoamAPI.getSfcNetconfIoamAPI().processRipUpdate(rip);
            }
        }

        //RIP deletion
        for (InstanceIdentifier iid : change.getRemovedPaths()) {
            if (dataOriginalDataObject.get(iid) instanceof RenderedIoamPath) {
                RenderedIoamPath rip = (RenderedIoamPath) dataOriginalDataObject.get(iid);
                LOG.debug("\nDeleted RenderedIoamPath: {}", rip.getName());
                SfcNetconfIoamAPI.getSfcNetconfIoamAPI().processRipDelete(rip);
            }
        }
    }
}
