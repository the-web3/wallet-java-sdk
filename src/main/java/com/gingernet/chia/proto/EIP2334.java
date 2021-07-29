package com.gingernet.chia.proto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class EIP2334 {
    public static byte[] deriveMasterFromSeed(String seed, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        return deriveMasterFromRawSeed(BIP39.derievePrivkey(seed, password));
    }

    public static byte[] deriveMasterFromRawSeed(byte[] seed) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        return EIP2333.derive_master_SK(seed);
    }

    public static byte[] deriveChildFromPath(byte[] master, String path) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        String[] pathway = path.split("/");
        if(pathway.length != 5) return null;
        if(!pathway[0].equals("m")) return null;
        if(!pathway[1].equals("12381")) return null;
        long[] derivation_path = new long[4];
        derivation_path[0] = 12381;
        try {
            derivation_path[1] = Integer.parseInt(pathway[2]);
            derivation_path[2] = Integer.parseInt(pathway[3]);
            derivation_path[3] = Integer.parseInt(pathway[4]);
        } catch(NumberFormatException ex){
            ex.printStackTrace();
            return null;
        }
        byte[] child = master;
        for(int i = 0; i < 4; ++i){
            child = EIP2333.derive_child_SK(child, derivation_path[i]);
        }
        return child;
    }
}
