package org.eclipse.simulation.device;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;

import javax.crypto.KeyAgreement;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;

public class ECDH_BC {
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    final static String ecName = "brainpoolp256t1";
    final static String kaName = "ECDH";
    final static String providerName = "BC";

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] savePublicKey(PublicKey key) throws Exception {
        // return key.getEncoded();

        ECPublicKey eckey = (ECPublicKey) key;
        return eckey.getQ().getEncoded(true);
    }

    public static PublicKey loadPublicKey(byte[] data) throws Exception {
        /*
         * KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
         * return kf.generatePublic(new X509EncodedKeySpec(data));
         */

        ECParameterSpec params = ECNamedCurveTable.getParameterSpec(ecName);
        ECPublicKeySpec pubKey = new ECPublicKeySpec(params.getCurve().decodePoint(data), params);
        KeyFactory kf = KeyFactory.getInstance(kaName, providerName);
        return kf.generatePublic(pubKey);
    }

    public static byte[] savePrivateKey(PrivateKey key) throws Exception {
        // return key.getEncoded();

        ECPrivateKey eckey = (ECPrivateKey) key;
        return eckey.getD().toByteArray();
    }

    public static PrivateKey loadPrivateKey(byte[] data) throws Exception {
        // KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
        // return kf.generatePrivate(new PKCS8EncodedKeySpec(data));

        ECParameterSpec params = ECNamedCurveTable.getParameterSpec(ecName);
        ECPrivateKeySpec prvkey = new ECPrivateKeySpec(new BigInteger(data), params);
        KeyFactory kf = KeyFactory.getInstance(kaName, providerName);
        return kf.generatePrivate(prvkey);
    }

    public static byte[] doECDH(String name, byte[] dataPrv, byte[] dataPub) throws Exception {
        KeyAgreement ka = KeyAgreement.getInstance(kaName, providerName);
        ka.init(loadPrivateKey(dataPrv));
        ka.doPhase(loadPublicKey(dataPub), true);
        byte[] secret = ka.generateSecret();
        System.out.println(name + bytesToHex(secret) + " Length: " + secret.length);
        return secret;
    }

    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpgen = generateAsyncKeypair(kaName, ecName, providerName);
        KeyPair pairA = kpgen.generateKeyPair();
        KeyPair pairB = kpgen.generateKeyPair();

        System.out.println("Alice: " + pairA.getPrivate());
        System.out.println("Alice: " + pairA.getPublic());
        System.out.println("Bob:   " + pairB.getPrivate());
        System.out.println("Bob:   " + pairB.getPublic());
        byte[] dataPrvA = savePrivateKey(pairA.getPrivate());
        byte[] dataPubA = savePublicKey(pairA.getPublic());
        byte[] dataPrvB = savePrivateKey(pairB.getPrivate());
        byte[] dataPubB = savePublicKey(pairB.getPublic());

        System.out.println("Keylength: " + dataPrvA.length * 8 + " bits");

        System.out.println("Alice Prv: " + bytesToHex(dataPrvA) + " length: " + dataPrvA.length);
        System.out.println("Alice Pub: " + bytesToHex(dataPubA) + " length: " + dataPubA.length);
        System.out.println("Bob Prv:   " + bytesToHex(dataPrvB) + " length: " + dataPrvB.length);
        System.out.println("Bob Pub:   " + bytesToHex(dataPubB) + " length: " + dataPubB.length);

        byte[] aliceSecret, bobSecret;

        aliceSecret = doECDH("Alice's secret: ", dataPrvA, dataPubB);
        bobSecret = doECDH("Bob's secret:   ", dataPrvB, dataPubA);

        if (MessageDigest.isEqual(aliceSecret, bobSecret)) {
            System.out.println("Secrets equal.");
        } else {
            System.out.println("Secrets not equal.");
        }

    }

    public KeyPair generateEcdhKeypair(String _curve) {

        KeyPair kp = null;

        try {
            kp = generateAsyncKeypair("ECDH", _curve, "BC").generateKeyPair();

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
}