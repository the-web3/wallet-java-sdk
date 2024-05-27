package com.gingernet.toncoin;

import com.alibaba.fastjson.JSONObject;

public class WalletContract extends Contract {

    public WalletContract(String pubkey) {
        super(pubkey);
    }

    public WalletContract() {
    }

    public JSONObject createTransferMessage(byte[] secretKey, Address address, long amount, long seqno,
                                            String payload, int sendMode, boolean dummySignature) {
        Cell payloadCell = new Cell();
        if (payload != null) {
            if (payload.length() > 0) {
                payloadCell.bits.writeUint(0, 32);
                payloadCell.bits.writeBytes(payload.getBytes());
            }
        }

        Cell orderHeader = Contract.createInternalMessageHeader(address, amount, true, false, false, null, false, 0, 0, 0, 0);
        Cell order = Contract.createCommonMsgInfo(orderHeader, null, payloadCell);
        Cell signingMessage = this.createSigningMessage(seqno);
        signingMessage.bits.writeUint8(sendMode);
        signingMessage.refs.add(order);

        Address selfAddress = this.getAddress();
        KeyPair keyPair = KeyPair.fromSecretSeed(secretKey);

        byte[] signature = dummySignature ? new byte[64] : keyPair.sign(signingMessage.hash());
        Cell body = new Cell();
        body.bits.writeBytes(signature);
        body.writeCell(signingMessage);

        Cell stateInit = null, code = null, data = null;
        if (seqno == 0) {
            JSONObject deploy = this.createStateInit();
            stateInit = deploy.getObject("stateInit", Cell.class);
            code = deploy.getObject("code", Cell.class);
            data = deploy.getObject("data", Cell.class);
        }

        Cell header = Contract.createExternalMessageHeader(selfAddress.toString(), null, 0);
        Cell resultMessage = Contract.createCommonMsgInfo(header, stateInit, body);

        JSONObject object = new JSONObject();
        object.put("address", selfAddress);
        object.put("message", resultMessage);
        object.put("body", body);
        object.put("signature", signature);
        object.put("signingMessage", signingMessage);
        object.put("stateInit", stateInit);
        object.put("code", code);
        object.put("data", data);
        return object;
    }

}