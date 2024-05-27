package com.gingernet.toncoin;

public class validAddressTest {

    public static void main(String[] args) {
        System.out.println(Address.validAddress("EQC7LzQRKFN6_dtbcLiZGetLThfgr8PbnT1OlLCqgB6Fb1g4"));
        System.out.println(Address.validAddress("0:bb2f341128537afddb5b70b89919eb4b4e17e0afc3db9d3d4e94b0aa801e856f"));
        System.out.println(Address.validAddress("EQC7LzQRKFN6_dtbcLiZGetLThfgr8PbnT1OlLCqgB6Fb1g45"));

        // It is a normal address on the block browser, but the verification will fail
        System.out.println(Address.validAddress("EQBbLhCaUDKhAK7hYKd1EAXuKqrXaMClYO5mo2n3JiRRymq_"));
        System.out.println(Address.validAddress("12312312"));
    }

}