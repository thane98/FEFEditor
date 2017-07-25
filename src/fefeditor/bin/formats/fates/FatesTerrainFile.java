package fefeditor.bin.formats.fates;

import fefeditor.common.io.BinFile;
import feflib.utils.ByteUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FatesTerrainFile extends BinFile {
    private List<fefeditor.bin.blocks.TileBlock> tiles = new ArrayList<>();
    private String mapModel;

    private int mapSizeX;
    private int mapSizeY;
    private int borderSizeX;
    private int borderSizeY;
    private byte[][] map;

    public FatesTerrainFile(File file, String mode) throws IOException {
        super(file, mode);

        this.seek(0x20);
        int tableOffset = this.readLittleInt() + 0x20;
        int tableCount = this.readLittleInt();
        mapModel = this.readStringFromPointer();
        int gridOffset = this.readLittleInt() + 0x20;

        processFileTable(tableOffset, tableCount);
        processMap(gridOffset);
    }

    private void processFileTable(int offset, int tableCount) {
        try {
            this.seek(offset);
            for (int x = 0; x < tableCount; x++) {
                fefeditor.bin.blocks.TileBlock block = new fefeditor.bin.blocks.TileBlock(this);
                tiles.add(block);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void processMap(int offset) {
        map = new byte[32][32];

        try {
            this.seek(offset);
            mapSizeX = this.readLittleInt();
            mapSizeY = this.readLittleInt();
            borderSizeX = this.readLittleInt();
            borderSizeY = this.readLittleInt();
            this.seek(this.getFilePointer() + 8);
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++)
                    map[x][y] = this.readByte();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void compile() {
        try {
            byte[] pointerOne = new byte[(tiles.size() + 1) * 0xC]; // Header has 3 pointers, each tile has 3 three pointers.
            int dataLength = tiles.size() * 0x28 + 0x10 + 0x418; // Header + tile array + map.
            int labelStart = dataLength + pointerOne.length + tiles.size() * 0x8;

            // Build pointer one.
            System.arraycopy(ByteUtils.toByteArray(8), 0, pointerOne, 0x4, 4);
            System.arraycopy(ByteUtils.toByteArray(0xC), 0, pointerOne, 0x8, 4);
            for (int x = 0; x < tiles.size(); x++) {
                System.arraycopy(ByteUtils.toByteArray(0x10 + x * 0x28), 0, pointerOne, 0xC + x * 0xC, 4);
                System.arraycopy(ByteUtils.toByteArray(0x14 + x * 0x28), 0, pointerOne, 0x10 + x * 0xC, 4);
                System.arraycopy(ByteUtils.toByteArray(0x34 + x * 0x28), 0, pointerOne, 0x14 + x * 0xC, 4);
            }

            // Build labels.
            List<Byte> compiledLabels = new ArrayList<>();
            List<Byte> compiledPointerTwo = new ArrayList<>();
            HashMap<String, Integer> labelMap = new HashMap<>();

            // TID labels come first followed by map model, so we have to do them separately.
            for (int x = 0; x < tiles.size(); x++) {
                fefeditor.bin.blocks.TileBlock t = tiles.get(x);
                if (!labelMap.keySet().contains(t.getTid())) {
                    labelMap.put(t.getTid(), labelStart + compiledLabels.size());
                    for (byte b : ByteUtils.toByteArray(0x10 + x * 0x28))
                        compiledPointerTwo.add(b);
                    for (byte b : ByteUtils.toByteArray(compiledLabels.size()))
                        compiledPointerTwo.add(b);
                    for (byte b : t.getTid().getBytes("shift-jis"))
                        compiledLabels.add(b);
                    compiledLabels.add((byte) 0);
                }
            }

            int modelOffset = labelStart + compiledLabels.size();
            for (byte b : mapModel.getBytes("shift-jis"))
                compiledLabels.add(b);
            compiledLabels.add((byte) 0);

            for (fefeditor.bin.blocks.TileBlock t : tiles) {
                if (!labelMap.keySet().contains(t.getmTid())) {
                    labelMap.put(t.getmTid(), labelStart + compiledLabels.size());
                    for (byte b : t.getmTid().getBytes("shift-jis"))
                        compiledLabels.add(b);
                    compiledLabels.add((byte) 0);
                }
                if (!labelMap.keySet().contains(t.getEffect())) {
                    labelMap.put(t.getEffect(), labelStart + compiledLabels.size());
                    for (byte b : t.getEffect().getBytes("shift-jis"))
                        compiledLabels.add(b);
                    compiledLabels.add((byte) 0);
                }
            }

            this.setLength(0x30);
            this.seek(0x30);
            for (fefeditor.bin.blocks.TileBlock t : tiles) {
                int[] pointers = {labelMap.get(t.getTid()), labelMap.get(t.getmTid()), labelMap.get(t.getEffect())};
                this.writeByteArray(t.getBytes(pointers));
            }
            this.writeLittleInt(mapSizeX);
            this.writeLittleInt(mapSizeY);
            this.writeLittleInt(borderSizeX);
            this.writeLittleInt(borderSizeY);
            this.writeLittleInt(mapSizeX - borderSizeX);
            this.writeLittleInt(mapSizeY - borderSizeY);
            for (byte[] aMap : map) this.writeByteArray(aMap);
            this.writeByteArray(pointerOne);

            for (byte b : compiledPointerTwo)
                this.writeByte(b);
            for (byte b : compiledLabels)
                this.writeByte(b);

            this.seek(0x20);
            this.writeLittleInt(0x10);
            this.writeLittleInt(tiles.size());
            this.writeLittleInt(modelOffset);
            this.writeLittleInt(tiles.size() * 0x28 + 0x10);

            this.seek(0x0);
            this.writeLittleInt((int) this.length());
            this.writeLittleInt(dataLength);
            this.writeLittleInt(pointerOne.length / 4);
            this.writeLittleInt(compiledPointerTwo.size() / 8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setTile(int x, int y, byte value) {
        map[x][y] = value;
    }

    public List<fefeditor.bin.blocks.TileBlock> getTiles() {
        return tiles;
    }

    public String getMapModel() {
        return mapModel;
    }

    public void setMapModel(String mapModel) {
        this.mapModel = mapModel;
    }

    public int getMapSizeX() {
        return mapSizeX;
    }

    public void setMapSizeX(int mapSizeX) {
        this.mapSizeX = mapSizeX;
    }

    public int getMapSizeY() {
        return mapSizeY;
    }

    public void setMapSizeY(int mapSizeY) {
        this.mapSizeY = mapSizeY;
    }

    public int getBorderSizeX() {
        return borderSizeX;
    }

    public void setBorderSizeX(int borderSizeX) {
        this.borderSizeX = borderSizeX;
    }

    public int getBorderSizeY() {
        return borderSizeY;
    }

    public void setBorderSizeY(int borderSizeY) {
        this.borderSizeY = borderSizeY;
    }

    public byte[][] getMap() {
        return map;
    }
}
