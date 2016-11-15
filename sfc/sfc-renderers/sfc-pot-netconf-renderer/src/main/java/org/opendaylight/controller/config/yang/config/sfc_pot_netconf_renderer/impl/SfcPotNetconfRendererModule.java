/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.config.yang.config.sfc_pot_netconf_renderer.impl;

import org.opendaylight.sfc.pot.netconf.renderer.SfcPotNetconfRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SfcPotNetconfRendererModule extends
             org.opendaylight.controller.config.yang.config.sfc_pot_netconf_renderer.impl.AbstractSfcPotNetconfRendererModule {

    private static final Logger LOG = LoggerFactory.getLogger(SfcPotNetconfRendererModule.class);

    private static final class AutoCloseableSfcPotNetconfRenderer implements AutoCloseable {
        SfcPotNetconfRenderer sfcpotnetconfrenderer;

        AutoCloseableSfcPotNetconfRenderer (SfcPotNetconfRenderer sfcpotnetconfrenderer) {
            this.sfcpotnetconfrenderer = sfcpotnetconfrenderer;
        }

        @Override
        public void close() {
            sfcpotnetconfrenderer.unregisterListeners();
            sfcpotnetconfrenderer.close();
            LOG.info("SFC Proof of Transit Netconf Renderer listeners closed");
        }
    }

    public SfcPotNetconfRendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
                                       org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }


    public SfcPotNetconfRendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
                                org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
                                org.opendaylight.controller.config.yang.config.sfc_pot_netconf_renderer.impl.SfcPotNetconfRendererModule oldModule,
                                java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("SFC Proof of Transit Netconf Renderer Module initializing");

        final SfcPotNetconfRenderer sfcpotnetconfrenderer = new SfcPotNetconfRenderer(getDataBrokerDependency(),
                                                                                      getBindingRegistryDependency());

        java.lang.AutoCloseable ret = new AutoCloseableSfcPotNetconfRenderer(sfcpotnetconfrenderer);

        LOG.info("SFC Proof of Transit Netconf Renderer Module initialized: (instance {})", ret);

        return ret;
    }
}
