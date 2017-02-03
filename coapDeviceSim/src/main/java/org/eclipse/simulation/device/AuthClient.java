package org.eclipse.simulation.device;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

public class AuthClient {

    public AuthClient(String test) {
        // TODO Auto-generated constructor stub

        Security.addProvider(new BouncyCastleProvider());
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("Curve25519");
        KeyPairGenerator g;
        KeyPair pair;
        try {
            g = KeyPairGenerator.getInstance("ECDH", "BC");
            g.initialize(ecSpec, new SecureRandom());
            pair = g.generateKeyPair();

            pair.getPrivate();
            pair.getPublic();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
