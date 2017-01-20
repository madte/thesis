package org.eclipse.simulation.device;

import java.net.InetSocketAddress;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.network.interceptors.MessageTracer;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;

public class ResourceServer extends CoapServer {

    public static final int DTLS_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_SECURE_PORT);
    private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);

    public ResourceServer(String _ipAddr) {
        // TODO Auto-generated constructor stub
        this.add(new Switch("led1", "OFF"));
        this.add(new Switch("led2", "OFF"));
        this.add(new StringR("string1", "empty"));

        // add endpoints on all IP addresses
        // this.addEndpoints();
        this.addEndpoint(new CoapEndpoint(new InetSocketAddress(_ipAddr, COAP_PORT)));
        this.start();

        // add special interceptor for message traces
        for (Endpoint ep : this.getEndpoints()) {
            ep.addInterceptor(new MessageTracer());
        }

        System.out.println("ResourceServer is listening on port " + COAP_PORT);

    }

    public ResourceServer(String _ipAddr, DtlsParams params) {

        this.add(new Switch("led1", "OFF"));
        this.add(new Switch("led2", "OFF"));
        this.add(new StringR("string1", "empty"));

        // Pre-shared secrets
        InMemoryPskStore pskStore = new InMemoryPskStore();
        pskStore.setKey(params.identity, params.psk.getBytes());

        DtlsConnectorConfig.Builder config = new DtlsConnectorConfig.Builder(new InetSocketAddress(_ipAddr, DTLS_PORT));
        config.setSupportedCipherSuites(new CipherSuite[] { CipherSuite.TLS_PSK_WITH_AES_128_CCM_8 });
        config.setPskStore(pskStore);

        DTLSConnector connector = new DTLSConnector(config.build());

        this.addEndpoint(new CoapEndpoint(connector, NetworkConfig.getStandard()));
        this.start();

        // add special interceptor for message traces
        for (Endpoint ep : this.getEndpoints()) {
            ep.addInterceptor(new MessageTracer());
        }

    }

}
