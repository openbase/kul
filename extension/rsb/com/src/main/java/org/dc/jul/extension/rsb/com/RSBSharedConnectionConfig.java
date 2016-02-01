/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import rsb.Factory;
import rsb.config.ParticipantConfig;
import rsb.transport.spread.InPushConnectorFactoryRegistry;
import rsb.transport.spread.SharedInPushConnectorFactory;

/**
 *
 * @author mpohling
 */
public class RSBSharedConnectionConfig {

    private static boolean init = false;
    private static ParticipantConfig participantConfig;

    public synchronized static void load() {
        if (init) {
            return;
        }

        final String inPushFactoryKey = "shareIfPossible";
        // register a (spread-specifc) factory to create the appropriate in push
        // connectors. In this case the factory tries to share all connections
        // except the converters differ. You can implement other strategies to
        // better match your needs.
        InPushConnectorFactoryRegistry.getInstance().registerFactory(
                inPushFactoryKey, new SharedInPushConnectorFactory());

        // instruct the spread transport to use your newly registered factory
        // for creating in push connector instances
        participantConfig = Factory.getInstance().getDefaultParticipantConfig();
        participantConfig.getOrCreateTransport("spread")
                .getOptions()
                .setProperty("transport.spread.java.infactory", inPushFactoryKey);

        init = true;
    }

    public static ParticipantConfig getParticipantConfig() {
        return participantConfig.copy();
    }
}
