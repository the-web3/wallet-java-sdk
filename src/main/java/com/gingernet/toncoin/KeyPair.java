package com.gingernet.toncoin;


import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.SignatureException;


/**
 * Holds a keypair.
 */
public class KeyPair {

    private static final EdDSANamedCurveSpec ed25519 = EdDSANamedCurveTable.ED_25519_CURVE_SPEC;

    private final EdDSAPublicKey mPublicKey;
    private final EdDSAPrivateKey mPrivateKey;

    /**
     * Creates a new KeyPair without a private key. Useful to simply verify a signature from a
     * given public address.
     *
     * @param publicKey
     */
    public KeyPair(EdDSAPublicKey publicKey) {
        this(publicKey, null);
    }

    /**
     * Creates a new KeyPair from the given public and private keys.
     *
     * @param publicKey
     * @param privateKey
     */
    public KeyPair(EdDSAPublicKey publicKey, EdDSAPrivateKey privateKey) {
        mPublicKey = publicKey;
        mPrivateKey = privateKey;
    }


    /**
     * Creates a new keypair from a raw 32 byte secret seed.
     *
     * @param seed The 32 byte secret seed.
     * @return {@link KeyPair}
     */
    public static KeyPair fromSecretSeed(byte[] seed) {
        EdDSAPrivateKeySpec privKeySpec = new EdDSAPrivateKeySpec(seed, ed25519);
        EdDSAPublicKeySpec publicKeySpec = new EdDSAPublicKeySpec(privKeySpec.getA().toByteArray(), ed25519);
        return new KeyPair(new EdDSAPublicKey(publicKeySpec), new EdDSAPrivateKey(privKeySpec));
    }


    /**
     * Creates a new keypair from a 32 byte address.
     *
     * @param publicKey The 32 byte public key.
     * @return {@link KeyPair}
     */
    public static KeyPair fromPublicKey(byte[] publicKey) {
        EdDSAPublicKeySpec publicKeySpec = new EdDSAPublicKeySpec(publicKey, ed25519);
        return new KeyPair(new EdDSAPublicKey(publicKeySpec));
    }


    /**
     * Generates a random keypair.
     *
     * @return a random keypair.
     */
    public static KeyPair random() {
        java.security.KeyPair keypair = new KeyPairGenerator().generateKeyPair();
        return new KeyPair((EdDSAPublicKey) keypair.getPublic(), (EdDSAPrivateKey) keypair.getPrivate());
    }

    public byte[] getPublicKey() {
        return mPublicKey.getAbyte();
    }

    /**
     * 返回私钥
     *
     * @return
     */
    public byte[] getPrivateKey() {
        return mPrivateKey.getSeed();
    }


    /**
     * Sign the provided data with the keypair's private key.
     *
     * @param data The data to sign.
     * @return signed bytes, null if the private key for this keypair is null.
     */
    public byte[] sign(byte[] data) {
        if (mPrivateKey == null) {
            throw new RuntimeException("KeyPair does not contain secret key. Use KeyPair.fromSecretSeed method to create a new KeyPair with a secret key.");
        }
        try {
            Signature sgr = new EdDSAEngine(MessageDigest.getInstance("SHA-512"));
            sgr.initSign(mPrivateKey);
            sgr.update(data);
            return sgr.sign();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verify the provided data and signature match this keypair's public key.
     *
     * @param data      The data that was signed.
     * @param signature The signature.
     * @return True if they match, false otherwise.
     * @throws RuntimeException
     */
    public boolean verify(byte[] data, byte[] signature) {
        try {
            Signature sgr = new EdDSAEngine(MessageDigest.getInstance("SHA-512"));
            sgr.initVerify(mPublicKey);
            sgr.update(data);
            return sgr.verify(signature);
        } catch (SignatureException e) {
            return false;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}