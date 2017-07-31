package fefeditor.bin;

public class CharSupport
{
    public static final byte[] ROMANTIC = { 0x04, 0x09, 0x0E, 0x14 };
    public static final byte[] FAST_ROMANTIC = { 0x03, 0x07, 0x0C, 0x12 };
    public static final byte[] PLATONIC = { 0x04, 0x09, 0x0E, -1 };
    public static final byte[] FAST_PLATONIC = { 0x03, 0x07, 0x0C, -1 };

    private int charId;
    private int type;
    private byte[] museumBytes;

    public CharSupport()
    {
        museumBytes = new byte[4];
        charId = 0;
        type = 0;
    }

    public int getCharId() {
        return charId;
    }

    public void setCharId(int charId) {
        this.charId = charId;
    }

    public int getType()
    {
        return type;
    }

    public void setType(byte[] bytes)
    {
        if(bytes[0] == 0x04)
        {
            if(bytes[3] == -1)
                type = 2;
            else
                type = 0;
        }
        else
        {
            if(bytes[3] == -1)
                type = 3;
            else
                type = 1;
        }
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public byte[] getBytes()
    {
        if(type == 0)
            return ROMANTIC;
        else if(type == 1)
            return FAST_ROMANTIC;
        else if(type == 2)
            return PLATONIC;
        else
            return FAST_PLATONIC;
    }

    public byte[] getMuseumBytes() {
        return museumBytes;
    }

    public void setMuseumBytes(byte[] museumBytes) {
        this.museumBytes = museumBytes;
    }
}
