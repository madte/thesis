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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.network.interceptors.MessageTracer;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.ScandiumLogger;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;

public class CoapEndDevice extends CoapServer {

    static {
        CaliforniumLogger.initialize();
        CaliforniumLogger.setLevel(Level.CONFIG);
        ScandiumLogger.initialize();
        ScandiumLogger.setLevel(Level.FINER);
    }

    // allows configuration via Californium.properties
    public static final int DTLS_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_SECURE_PORT);
    private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);

    public CoapEndDevice() {

        super();
        this.add(new StringR("id", "dev1"));
        this.add(new Switch("led1", "OFF"));
        this.add(new Switch("led2", "OFF"));
        this.add(new StringR("string1", "empty"));

        // add endpoints on all IP addresses
        this.addEndpoints();
        this.start();

        // add special interceptor for message traces
        for (Endpoint ep : this.getEndpoints()) {
            ep.addInterceptor(new MessageTracer());
        }

        System.out.println(" CoAP end-device is listening on port " + COAP_PORT);

    }

    public CoapEndDevice(String _identity, String _psk) {

        super();

        this.add(new StringR("id", "secureDev1"));
        this.add(new Switch("led1", "OFF"));
        this.add(new Switch("led2", "OFF"));
        this.add(new StringR("string1", "empty"));

        // Pre-shared secrets
        InMemoryPskStore pskStore = new InMemoryPskStore();
        pskStore.setKey(_identity, _psk.getBytes());

        DtlsConnectorConfig.Builder config = new DtlsConnectorConfig.Builder(new InetSocketAddress(DTLS_PORT));
        config.setSupportedCipherSuites(new CipherSuite[] { CipherSuite.TLS_PSK_WITH_AES_128_CCM_8 });
        config.setPskStore(pskStore);

        DTLSConnector connector = new DTLSConnector(config.build());

        this.addEndpoint(new CoapEndpoint(connector, NetworkConfig.getStandard()));
        this.start();

        // add special interceptor for message traces
        for (Endpoint ep : this.getEndpoints()) {
            ep.addInterceptor(new MessageTracer());
        }

        System.out.println("Secure CoAP end-device is listening on port " + DTLS_PORT);

    }

    /**
     * Add individual endpoints listening on default CoAP port on all IPv4 addresses of all network interfaces.
     */
    private void addEndpoints() {
        for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
            // only binds to IPv4 addresses and localhost
            if (addr instanceof Inet4Address || addr.isLoopbackAddress()) {
                InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
                addEndpoint(new CoapEndpoint(bindToAddress));
            }
        }
    }

    public static void main(String[] args) {

        CoapEndDevice secureDev1 = new CoapEndDevice("Client_identity", "secretPSK");
        CoapEndDevice dev1 = new CoapEndDevice();

    }

}
