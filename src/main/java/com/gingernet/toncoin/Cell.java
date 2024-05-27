package com.gingernet.toncoin;


import com.alibaba.fastjson.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.i2p.crypto.eddsa.Utils.bytesToHex;
import static net.i2p.crypto.eddsa.Utils.hexToBytes;


public class Cell {

    static byte[] reachBocMagicPrefix = Hex.decode("B5EE9C72");
    static byte[] leanBocMagicPrefix = Hex.decode("68ff65f3");
    static byte[] leanBocMagicPrefixCRC = Hex.decode("acc3a728");

    protected BitString bits;
    protected List<Object> refs;
    protected boolean isExotic;

    public Cell() {
        this.bits = new BitString(1023);
        this.refs = new ArrayList<>();
        this.isExotic = false;
    }

    public byte[] getRepr() {
        List<Object> reprArray = new ArrayList<>();
        reprArray.add(this.getDataWithDescriptors());

        for (int i = 0; i < this.refs.size(); i++) {
            Cell c = (Cell) this.refs.get(i);
            reprArray.add(c.getMaxDepthAsArray());
        }
        for (int k = 0; k < this.refs.size(); k++) {
            Cell i = (Cell) this.refs.get(k);
            reprArray.add(i.hash());
        }
        byte[] x = new byte[]{};
        for (int k = 0; k < reprArray.size(); k++) {
            byte[] i = (byte[]) reprArray.get(k);
            x = Utils.concatBytes(x, i);
        }
        return x;
    }

    public byte[] hash() {
        byte[] repr = this.getRepr();
        MessageDigest digest = Utils.newDigest();
        digest.update(repr, 0, repr.length);
        return digest.digest();
    }

    public byte[] getMaxDepthAsArray() {
        int maxDepth = this.getMaxDepth();
        byte[] d = new byte[2];
        d[1] = (byte) (maxDepth % 256);
        d[0] = (byte) Math.floor(maxDepth / 256.0);
        return d;
    }

    public int getMaxDepth() {
        int maxDepth = 0;
        if (this.refs.size() > 0) {
            for (int k = 0; k < this.refs.size(); k++) {
                Cell c = (Cell) this.refs.get(k);
                if (c.getMaxDepth() > maxDepth) {
                    maxDepth = c.getMaxDepth();
                }
            }
            maxDepth = maxDepth + 1;
        }
        return maxDepth;
    }

    public byte[] getDataWithDescriptors() {
        byte[] d1 = this.getRefsDescriptor();
        byte[] d2 = this.getBitsDescriptor();
        byte[] tuBits = this.bits.getTopUppedArray();
        return Utils.concatBytes(Utils.concatBytes(d1, d2), tuBits);
    }

    public int getMaxLevel() {
        int maxLevel = 0;
        for (int i = 0; i < this.refs.size(); i++) {
            Cell c = (Cell) this.refs.get(i);
            if (c.getMaxLevel() > maxLevel) {
                maxLevel = c.getMaxLevel();
            }
        }
        return maxLevel;
    }

    public byte[] getRefsDescriptor() {
        byte[] d1 = new byte[1];
        int tmp = this.isExotic ? 1 : 0;
        d1[0] = (byte) (this.refs.size() + tmp * 8 + this.getMaxLevel() * 32);
        return d1;
    }

    public byte[] getBitsDescriptor() {
        byte[] d2 = new byte[1];
        d2[0] = (byte) (Math.ceil(this.bits.cursor / 8.0) + Math.floor(this.bits.cursor / 8.0));
        return d2;
    }

    public static List<Cell> fromBoc(byte[] serializedBoc) {
        return deserializeBoc(serializedBoc);
    }

    public static List<Cell> deserializeBoc(byte[] serializedBoc) {
        JSONObject header = parseBocHeader(serializedBoc);
        byte[] cells_data = header.getBytes("cells_data");
        int cells_num = header.getIntValue("cells_num");
        int size_bytes = header.getIntValue("size_bytes");
        byte[] root_list = header.getBytes("root_list");

        List<Cell> cells_array = new ArrayList<>();
        for (int ci = 0; ci < cells_num; ci++) {
            JSONObject dd = deserializeCellData(cells_data, size_bytes);
            cells_data = dd.getBytes("residue");
            Cell cell = dd.getObject("cell", Cell.class);
            cells_array.add(cell);
        }
        for (int ci = cells_num - 1; ci >= 0; ci--) {
            Cell c = cells_array.get(ci);
            for (int ri = 0; ri < c.refs.size(); ri++) {
                int r = (int) c.refs.get(ri);
                if (r < ci) {
                    throw new IllegalArgumentException("Topological order is broken");
                }
                c.refs.set(ri, cells_array.get(r));
            }
        }
        List<Cell> root_cells = new ArrayList<>();
        for (byte i : root_list) {
            root_cells.add(cells_array.get(i));
        }
        return root_cells;
    }

    public static JSONObject deserializeCellData(byte[] cellData, int referenceIndexSize) {
        if (cellData.length < 2) {
            throw new IllegalArgumentException("Not enough bytes to encode cell descriptors");
        }
        int d1 = cellData[0] & 0xff;
        int d2 = cellData[1] & 0xff;
        cellData = Utils.byteSlice(cellData, 2);

        int level = (int) Math.floor(d1 / 32.0);
        int isExotic = d1 & 8;
        int refNum = d1 % 8;
        int dataBytesize = (int) Math.ceil(d2 / 2.0);
        boolean fullfilledBytes = (d2 % 2 == 0);
        Cell cell = new Cell();
        cell.isExotic = isExotic > 0;
        if (cellData.length < dataBytesize + referenceIndexSize * refNum) {
            throw new IllegalArgumentException("Not enough bytes to encode cell data");
        }

        cell.bits.setTopUppedArray(Utils.byteSliceRange(cellData, 0, dataBytesize), fullfilledBytes);
        cellData = Utils.byteSlice(cellData, dataBytesize);
        for (int r = 0; r < refNum; r++) {
            cell.refs.add(Utils.readNBytesUIntFromArray(referenceIndexSize, cellData));
            cellData = Utils.byteSlice(cellData, referenceIndexSize);
        }
        JSONObject object = new JSONObject();
        object.put("cell", cell);
        object.put("residue", cellData);
        return object;
    }

    public void writeCell(Cell anotherCell) {
        this.bits.writeBitString(anotherCell.bits);
        this.refs.addAll(anotherCell.refs);
    }

    public static JSONObject parseBocHeader(byte[] serializedBoc) {
        if (serializedBoc.length < 4 + 1) {
            throw new IllegalArgumentException("Not enough bytes for magic prefix");
        }
        byte[] inputData = serializedBoc;
        byte[] prefix = Utils.byteSliceRange(serializedBoc, 0, 4);
        serializedBoc = Utils.byteSliceRange(serializedBoc, 4, serializedBoc.length - 4);

        int has_idx = 0, hash_crc32 = 0, has_cache_bits = 0, flags = 0, size_bytes = 0;
        if (Utils.compareBytes(prefix, reachBocMagicPrefix)) {
            byte flags_byte = serializedBoc[0];
            has_idx = flags_byte & 128;
            hash_crc32 = flags_byte & 64;
            has_cache_bits = flags_byte & 32;
            flags = (flags_byte & 16) * 2 + (flags_byte & 8);
            size_bytes = flags_byte % 8;
        }
        if (Utils.compareBytes(prefix, leanBocMagicPrefix)) {
            has_idx = 1;
            hash_crc32 = 0;
            has_cache_bits = 0;
            flags = 0;
            size_bytes = serializedBoc[0];
        }
        if (Utils.compareBytes(prefix, leanBocMagicPrefixCRC)) {
            has_idx = 1;
            hash_crc32 = 1;
            has_cache_bits = 0;
            flags = 0;
            size_bytes = serializedBoc[0];
        }
        serializedBoc = Utils.byteSliceRange(serializedBoc, 1, serializedBoc.length - 1);

        if (serializedBoc.length < 1 + 5 * size_bytes) {
            throw new IllegalArgumentException("Not enough bytes for encoding cells counters");
        }
        byte offset_bytes = serializedBoc[0];
        serializedBoc = Utils.byteSliceRange(serializedBoc, 1, serializedBoc.length - 1);

        int cells_num = Utils.readNBytesUIntFromArray(size_bytes, serializedBoc);
        serializedBoc = Utils.byteSliceRange(serializedBoc, size_bytes, serializedBoc.length - size_bytes);
        int roots_num = Utils.readNBytesUIntFromArray(size_bytes, serializedBoc);

        serializedBoc = Utils.byteSliceRange(serializedBoc, size_bytes, serializedBoc.length - size_bytes);
        int absent_num = Utils.readNBytesUIntFromArray(size_bytes, serializedBoc);

        serializedBoc = Utils.byteSliceRange(serializedBoc, size_bytes, serializedBoc.length - size_bytes);
        int tot_cells_size = Utils.readNBytesUIntFromArray(offset_bytes, serializedBoc);

        serializedBoc = Utils.byteSliceRange(serializedBoc, offset_bytes, serializedBoc.length - offset_bytes);

        if (serializedBoc.length < roots_num * size_bytes) {
            throw new IllegalArgumentException("Not enough bytes for encoding root cells hashes");
        }

        byte[] root_list = new byte[roots_num];
        for (int c = 0; c < roots_num; c++) {
            int i = Utils.readNBytesUIntFromArray(size_bytes, serializedBoc);
            root_list[c] = (byte) i;
            serializedBoc = Utils.byteSliceRange(serializedBoc, size_bytes, serializedBoc.length - size_bytes);
        }
        boolean index = false;
        if (has_idx > 0) {
            List<Integer> index1 = new ArrayList<>();
            if (serializedBoc.length < offset_bytes * cells_num) {
                throw new IllegalArgumentException("Not enough bytes for index encoding");
            }
            for (int c = 0; c < cells_num; c++) {
                index1.add(Utils.readNBytesUIntFromArray(offset_bytes, serializedBoc));
                serializedBoc = Utils.byteSliceRange(serializedBoc, offset_bytes, serializedBoc.length - offset_bytes);
            }
        }
        if (serializedBoc.length < tot_cells_size) {
            throw new IllegalArgumentException("Not enough bytes for cells data");
        }
        byte[] cells_data = Utils.byteSliceRange(serializedBoc, 0, tot_cells_size);
        serializedBoc = Utils.byteSliceRange(serializedBoc, tot_cells_size, serializedBoc.length - tot_cells_size);

        if (hash_crc32 > 0) {
            if (serializedBoc.length < 4) {
                throw new IllegalArgumentException("Not enough bytes for crc32c hashsum");
            }
            int length = inputData.length;
            byte[] input1 = Utils.byteSliceRange(inputData, 0, length - 4);
            byte[] input2 = Utils.byteSliceRange(serializedBoc, 0, 4);
            if (!Utils.compareBytes(Utils.crc32c(input1), input2)) {
                throw new IllegalArgumentException("Crc32c hashsum mismatch");
            }
            serializedBoc = Utils.byteSliceRange(serializedBoc, 4, serializedBoc.length - 4);
        }
        if (serializedBoc.length > 0) {
            throw new IllegalArgumentException("Too much bytes in BoC serialization");
        }

        JSONObject object = new JSONObject();
        object.put("has_idx", has_idx);
        object.put("hash_crc32", hash_crc32);
        object.put("has_cache_bits", has_cache_bits);
        object.put("flags", flags);
        object.put("size_bytes", size_bytes);
        object.put("offset_bytes", offset_bytes);
        object.put("cells_num", cells_num);
        object.put("roots_num", roots_num);
        object.put("absent_num", absent_num);
        object.put("tot_cells_size", tot_cells_size);
        object.put("root_list", root_list);
        object.put("index", index);
        object.put("cells_data", cells_data);
        return object;
    }

    public byte[] toBoc(boolean has_idx, boolean hash_crc32, boolean has_cache_bits, int flags) {
        Cell root_cell = this;
        List<Object> allcells = root_cell.treeWalk();
        List<Object> topologicalOrder = (List<Object>) allcells.get(0);
        Map<String, Integer> cellsIndex = (Map<String, Integer>) allcells.get(1);

        int cells_num = topologicalOrder.size();
        int s = Integer.toBinaryString(cells_num).length();
        int s_bytes = (int) Math.min(Math.ceil(s / 8.0), 1);
        int full_size = 0;

        List<Integer> sizeIndex = new ArrayList<>();
        for (Object cell_info : topologicalOrder) {
            sizeIndex.add(full_size);
            List<Object> list = (List<Object>) cell_info;
            Cell o = (Cell) list.get(1);
            full_size = full_size + o.bocSerializationSize(cellsIndex, s_bytes);
        }
        int offset_bits = Integer.toBinaryString(full_size).length(); // Minimal number of bits to offset/len (unused?)
        int offset_bytes = (int) Math.max(Math.ceil(offset_bits / 8.0), 1);

        BitString serialization = new BitString((1023 + 32 * 4 + 32 * 3) * topologicalOrder.size());
        serialization.writeBytes(reachBocMagicPrefix);
        serialization.writeBitArray(new boolean[]{has_idx, hash_crc32, has_cache_bits});
        serialization.writeUint(flags, 2);
        serialization.writeUint(s_bytes, 3);
        serialization.writeUint8(offset_bytes);
        serialization.writeUint(cells_num, s_bytes * 8);
        serialization.writeUint(1, s_bytes * 8);
        serialization.writeUint(0, s_bytes * 8);
        serialization.writeUint(full_size, offset_bytes * 8);
        serialization.writeUint(0, s_bytes * 8);
        if (has_idx) {
            final int[] index = {0};
            topologicalOrder.forEach(cell_data -> {
                serialization.writeUint(sizeIndex.get(index[0]), offset_bytes * 8);
                index[0]++;
            });

        }
        for (Object cell_info : topologicalOrder) {
            List<Object> list = (List<Object>) cell_info;
            Cell o = (Cell) list.get(1);
            byte[] refcell_ser = o.serializeForBoc(cellsIndex, s_bytes);
            serialization.writeBytes(refcell_ser);
        }

        byte[] ser_arr = serialization.getTopUppedArray();
        if (hash_crc32) {
            ser_arr = Utils.concatBytes(ser_arr, Utils.crc32c(ser_arr));
        }
        return ser_arr;
    }

    int bocSerializationSize(Map<String, Integer> cellsIndex, int refSize) {
        return (this.serializeForBoc(cellsIndex, refSize)).length;
    }

    public List<Object> treeWalk() {
        List<Object> cells = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        return treeWalk(this, cells, map);
    }

    public List<Object> treeWalk(Cell cell, List<Object> topologicalOrderArray, Map<String, Integer> indexHashmap) {
        List<Object> objects = new ArrayList<>();
        byte[] cellHash = cell.hash();
        if (indexHashmap.containsKey(bytesToHex(cellHash))) {
            objects.add(topologicalOrderArray);
            objects.add(indexHashmap);
            return objects;
        }
        indexHashmap.put(bytesToHex(cellHash), topologicalOrderArray.size());
        List<Object> t = new ArrayList<>();
        t.add(cellHash);
        t.add(cell);
        topologicalOrderArray.add(t);

        for (Object subCell : cell.refs) {
            Cell tmp = (Cell) subCell;
            List<Object> res = treeWalk(tmp, topologicalOrderArray, indexHashmap);
            topologicalOrderArray = (List<Object>) res.get(0);
            indexHashmap = (Map<String, Integer>) res.get(1);
        }
        objects.add(topologicalOrderArray);
        objects.add(indexHashmap);
        return objects;
    }

    public byte[] serializeForBoc(Map<String, Integer> cellsIndex, int refSize) {
        List<byte[]> reprArray = new ArrayList<>();
        reprArray.add(this.getDataWithDescriptors());
        if (this.isExplicitlyStoredHashes() > 0) {
            throw new Error("Cell hashes explicit storing is not implemented");
        }
        for (int k = 0; k < this.refs.size(); k++) {
            Cell i = (Cell) this.refs.get(k);
            byte[] refHash = i.hash();
            int refIndexInt = cellsIndex.get(bytesToHex(refHash));
            String refIndexHex = Integer.toHexString(refIndexInt);
            if (refIndexHex.length() % 2 == 1) {
                refIndexHex = "0" + refIndexHex;
            }
            byte[] reference = hexToBytes(refIndexHex);
            reprArray.add(reference);
        }
        byte[] x = {};
        for (int k = 0; k < reprArray.size(); k++) {
            byte[] i = reprArray.get(k);
            x = Utils.concatBytes(x, i);
        }
        return x;
    }

    private int isExplicitlyStoredHashes() {
        return 0;
    }
}