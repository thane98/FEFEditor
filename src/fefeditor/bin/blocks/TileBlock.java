package fefeditor.bin.blocks;

import fefeditor.common.io.BinFile;
import feflib.utils.ByteUtils;

public class TileBlock {
    private String tid;
    private String mTid;
    private byte[] unknown;
    private byte placementNumber;
    private byte[] changeIds;
    private byte unknownByte;
    private byte defenseBonus;
    private byte avoidBonus;
    private byte healingBonus;
    private byte[] unknownTwo;
    private String effect;

    public TileBlock() {

    }

    public TileBlock(TileBlock t) {
        tid = t.getTid();
        mTid = t.getmTid();
        unknown = t.getUnknown();
        placementNumber = t.getPlacementNumber();
        changeIds = t.getChangeIds();
        unknownByte = t.getUnknownByte();
        defenseBonus = t.getDefenseBonus();
        avoidBonus = t.getAvoidBonus();
        healingBonus = t.getHealingBonus();
        unknownTwo = t.getUnknownTwo();
        effect = t.getEffect();
    }

    public TileBlock(BinFile file) {
        try {
            tid = file.readStringFromPointer();
            mTid = file.readStringFromPointer();
            unknown = file.readByteArray(4);
            placementNumber = file.readByte();
            changeIds = file.readByteArray(3);
            unknownByte = file.readByte();
            defenseBonus = file.readByte();
            avoidBonus = file.readByte();
            healingBonus = file.readByte();
            unknownTwo = file.readByteArray(0x10);
            effect = file.readStringFromPointer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public byte[] getBytes(int[] pointers) {
        if (pointers.length < 3)
            return null;
        byte[] bytes = new byte[0x28];
        System.arraycopy(ByteUtils.toByteArray(pointers[0]), 0, bytes, 0, 4);
        System.arraycopy(ByteUtils.toByteArray(pointers[1]), 0, bytes, 0x4, 4);
        System.arraycopy(unknown, 0, bytes, 0x8, 4);
        bytes[0xC] = placementNumber;
        System.arraycopy(changeIds, 0, bytes, 0xD, 3);
        bytes[0x10] = unknownByte;
        bytes[0x11] = defenseBonus;
        bytes[0x12] = avoidBonus;
        bytes[0x13] = healingBonus;
        System.arraycopy(unknownTwo, 0, bytes, 0x14, 0x10);
        System.arraycopy(ByteUtils.toByteArray(pointers[2]), 0, bytes, 0x24, 4);
        return bytes;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getmTid() {
        return mTid;
    }

    public void setmTid(String mTid) {
        this.mTid = mTid;
    }

    public byte[] getUnknown() {
        return unknown;
    }

    public void setUnknown(byte[] unknown) {
        this.unknown = unknown;
    }

    public byte getPlacementNumber() {
        return placementNumber;
    }

    public void setPlacementNumber(byte placementNumber) {
        this.placementNumber = placementNumber;
    }

    public byte[] getChangeIds() {
        return changeIds;
    }

    public void setChangeIds(byte[] changeIds) {
        this.changeIds = changeIds;
    }

    public byte getUnknownByte() {
        return unknownByte;
    }

    public void setUnknownByte(byte unknownByte) {
        this.unknownByte = unknownByte;
    }

    public byte getDefenseBonus() {
        return defenseBonus;
    }

    public void setDefenseBonus(byte defenseBonus) {
        this.defenseBonus = defenseBonus;
    }

    public byte getAvoidBonus() {
        return avoidBonus;
    }

    public void setAvoidBonus(byte avoidBonus) {
        this.avoidBonus = avoidBonus;
    }

    public byte getHealingBonus() {
        return healingBonus;
    }

    public void setHealingBonus(byte healingBonus) {
        this.healingBonus = healingBonus;
    }

    public byte[] getUnknownTwo() {
        return unknownTwo;
    }

    public void setUnknownTwo(byte[] unknownTwo) {
        this.unknownTwo = unknownTwo;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }
}
