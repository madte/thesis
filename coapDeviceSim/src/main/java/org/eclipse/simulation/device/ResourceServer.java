package org.eclipse.simulation.device;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

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

    private static final String TRUST_STORE_PASSWORD = "rootPass";
    private static final String KEY_STORE_PASSWORD = "endPass";
    private static final String KEY_STORE_LOCATION = "certs/keyStore.jks";
    private static final String TRUST_STORE_LOCATION = "certs/trustStore.jks";

    public ResourceServer(String _ipAddr) {
        // TODO Auto-generated constructor stub
        this.add(new Switch("led1", "OFF"));
        this.add(new Switch("led2", "OFF"));
        this.add(new StringR("string1", "empty"));

        this.addEndpoint(new CoapEndpoint(new InetSocketAddress(_ipAddr, COAP_PORT)));

        // add endpoints on all IP addresses
        // this.addEndpoints();

        this.start();

        // add special interceptor for message traces
        for (Endpoint ep : this.getEndpoints()) {
            ep.addInterceptor(new MessageTracer());
        }

        System.out.println("ResourceServer is listening on port " + COAP_PORT);

    }

    public ResourceServer(String _ipAddr, DtlsRawPublicKeyPair _keypair) {
        // TODO Auto-generated constructor stub
        this.add(new Switch("led1", "OFF"));
        this.add(new Switch("led2", "OFF"));
        this.add(new StringR("string1", "empty"));

        InputStream in = null;
        InputStream inTrust = null;

        try {

            // load the trust store
            KeyStore trustStore = KeyStore.getInstance("JKS");
            inTrust = getClass().getClassLoader().getResourceAsStream(TRUST_STORE_LOCATION);
            trustStore.load(inTrust, TRUST_STORE_PASSWORD.toCharArray());

            // You can load multiple certificates if needed
            Certificate[] trustedCertificates = new Certificate[1];
            trustedCertificates[0] = trustStore.getCertificate("root");

            // load the key store
            KeyStore keyStore = KeyStore.getInstance("JKS");
            in = getClass().getClassLoader().getResourceAsStream(KEY_STORE_LOCATION);
            keyStore.load(in, KEY_STORE_PASSWORD.toCharArray());

            DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(
                    new InetSocketAddress(_ipAddr, DTLS_PORT));
            builder.setSupportedCipherSuites(new CipherSuite[] { CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8 });
            builder.setIdentity((PrivateKey) keyStore.getKey("server", KEY_STORE_PASSWORD.toCharArray()),
                    keyStore.getCertificateChain("server"), true);
            builder.setTrustStore(trustedCertificates);

            DTLSConnector connector = new DTLSConnector(builder.build());
            this.addEndpoint(new CoapEndpoint(connector, NetworkConfig.getStandard()));

            this.start();

        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException
                | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /*
         * DtlsConnectorConfig.Builder config = new DtlsConnectorConfig.Builder(new InetSocketAddress(_ipAddr,
         * DTLS_PORT));
         * config.setSupportedCipherSuites(new CipherSuite[] { CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8 });
         * config.setIdentity(_keypair.kp.getPrivate(), _keypair.kp.getPublic());
         */

        // add endpoints on all IP addresses
        // this.addEndpoints();

        // add special interceptor for message traces
        for (Endpoint ep : this.getEndpoints()) {
            ep.addInterceptor(new MessageTracer());
        }

        System.out.println("ResourceServer is listening on port " + DTLS_PORT);

    }

    public ResourceServer(String _ipAddr, DtlsPskParams params) {

        this.add(new Switch("led1", "OFF"));
        this.add(new Switch("led2", "OFF"));
        this.add(new StringR("string1", "DtlsPskSecured"));

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
