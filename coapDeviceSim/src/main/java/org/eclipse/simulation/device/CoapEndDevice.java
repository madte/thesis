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

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.logging.Level;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
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

    public CoapEndDevice(String _ipAddr, String _id, String _type, DtlsRawPublicKeyPair _kp) {

        infoServer = new InfoServer(_ipAddr, _id, _type);

        resourceServer = new ResourceServer(_ipAddr, _kp);

        System.out.println("DTLS_RawPublicKey CoAP end-device " + _id + " initialized.");

    }

    public CoapEndDevice(String _ipAddr, String _id, String _type, DtlsPskParams _dtlsParams) {

        infoServer = new InfoServer(_ipAddr, _id, _type);

        resourceServer = new ResourceServer(_ipAddr, _dtlsParams);

        System.out.println("DTLS_PSK CoAP end-device " + _id + " is initialized");
    }

    public static KeyPair generateEcdsaKeypair(String _curve) {

        KeyPair kp = null;

        try {
            kp = generateAsyncKeypair("ECDSA", _curve, "BC").generateKeyPair();

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return kp;

    }

    public static KeyPairGenerator generateAsyncKeypair(String _keyAgreement, String _ellipticCurve, String _provider)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator kpgen = KeyPairGenerator.getInstance(_keyAgreement, _provider);
        // X9ECParameters ecP = CustomNamedCurves.getByName("Curve25519");
        // ECParameterSpec ecSpec = new ECParameterSpec(ecP.getCurve(), ecP.getG(), ecP.getN(), ecP.getH(),
        // ecP.getSeed());

        // kpgen.initialize(ecSpec, new SecureRandom());

        kpgen.initialize(new ECGenParameterSpec(_ellipticCurve), new SecureRandom());
        return kpgen;
    }

    public static void main(String[] args) {

        // AuthClient testor = new AuthClient("test");

        @SuppressWarnings("unused")
        CoapEndDevice dtlsPskDev1 = new CoapEndDevice("[0:0:0:0:0:ffff:7f00:1]", "dtlsPskDev1", "generic",
                new DtlsPskParams("Client_identity", "secretPSK"));

        @SuppressWarnings("unused")
        CoapEndDevice dev1 = new CoapEndDevice("[0:0:0:0:0:ffff:7f00:2]", "dev1", "generic");

        DtlsRawPublicKeyPair kp = new DtlsRawPublicKeyPair(generateEcdsaKeypair("secp256r1"));

        @SuppressWarnings("unused")
        CoapEndDevice dtlsRawPkDev1 = new CoapEndDevice("[0:0:0:0:0:ffff:7f00:3]", "dtlsRawPkDev1", "generic", kp);

    }

}
