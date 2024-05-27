package com.gingernet.toncoin;

import java.util.Date;


public class WalletV3Contract extends WalletContract {

    public WalletV3Contract(String pubkey) {
        super(pubkey);
    }

    public WalletV3Contract() {
    }

    @Override
    public Cell createSigningMessage(long seqno) {
        Cell message = new Cell();
        message.bits.writeUint(this.walletId, 32);
        if (seqno == 0) {
            for (int i = 0; i < 32; i++) {
                message.bits.writeBit(true);
            }
        } else {
            Date date = getDate();
            long timestamp = (long) Math.floor(date.getTime() / 1e3);
            message.bits.writeUint(timestamp + 600, 32);
        }
        message.bits.writeUint(seqno, 32);
        return message;
    }

    @Override
    public Cell createDataCell() {
        Cell cell = new Cell();
        cell.bits.writeUint(0, 32);
        cell.bits.writeUint(this.walletId, 32);
        cell.bits.writeBytes(this.pubkey);
        return cell;
    }
}