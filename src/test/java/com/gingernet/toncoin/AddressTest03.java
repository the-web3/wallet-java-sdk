package com.gingernet.toncoin;

public class AddressTest03 {
    public static void main(String[] args) {
        String to = "0:bd8c4b8b2af1ce0af79616c616d92db79784492094ac7321143eec0bf6401718";
        String to01 = new Address(to).toAddressString(true, true, false, false);
        String to02 = new Address(to).toAddressString(true, false, false, false);
        String to03 = new Address(to).toAddressString(true, false, true, false);
        Address toAddress01 = new Address(to01);
        Address toAddress02 = new Address(to02);
        Address toAddress03 = new Address(to03);
        System.out.println(toAddress01);
        System.out.println(toAddress02);
        System.out.println(toAddress03);
    }
}