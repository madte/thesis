/*******************************************************************************
 * Copyright (c) {DATE} {INITIAL COPYRIGHT OWNER} {OTHER COPYRIGHT OWNERS}.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Kessel - initial implementation
 *******************************************************************************/
package org.eclipse.simulation.device;

import java.util.logging.Level;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.scandium.ScandiumLogger;

public class CoapEndDevice {

    static {
        CaliforniumLogger.initialize();
        CaliforniumLogger.setLevel(Level.CONFIG);
        ScandiumLogger.initialize();
        ScandiumLogger.setLevel(Level.FINER);
    }

    @SuppressWarnings("unused")
    private InfoServer infoServer;
    @SuppressWarnings("unused")
    private ResourceServer resourceServer;

    // allows configuration via Californium.properties

    public CoapEndDevice(String _ipAddr, String _id, String _type) {

        infoServer = new InfoServer(_ipAddr, _id, _type);

        resourceServer = new ResourceServer(_ipAddr);

        System.out.println(" CoAP end-device " + _id + " initialized.");

    }

    public CoapEndDevice(String _ipAddr, String _id, String _type, DtlsParams _dtlsParams) {

        infoServer = new InfoServer(_ipAddr, _id, _type);

        resourceServer = new ResourceServer(_ipAddr, _dtlsParams);

        System.out.println("DTLS CoAP end-device " + _id + " is initialized");
    }

    public static void main(String[] args) {

        @SuppressWarnings("unused")
        CoapEndDevice secureDev1 = new CoapEndDevice("127.0.0.1", "DtlsDev1", "generic",
                new DtlsParams("Client_identity", "secretPSK"));
        @SuppressWarnings("unused")
        CoapEndDevice dev1 = new CoapEndDevice("127.0.0.2", "dev1", "generic");

    }

}
