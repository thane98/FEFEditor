package fefeditor.common.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IOUtils 
{
	public static void copyFolder(File src, File dest) throws IOException 
	{
		if(src.isDirectory())
		{
			if(!dest.exists())
				dest.mkdir();

			String files[] = src.list();

			for (String file : files) 
			{
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				copyFolder(srcFile,destFile);
			}

		} 
		else 
		{
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			
			while ((length = in.read(buffer)) > 0){
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
		}
	}
	
	public static int search(byte[] target, byte[] input) {
        Object[] targetB = new Byte[target.length];
        int x = 0;
        while (x < target.length) {
            targetB[x] = target[x];
            ++x;
        }
        int idx = -1;
        ArrayDeque<Byte> q = new ArrayDeque<>(input.length);
        int i = 0;
        while (i < input.length) {
            if (q.size() == targetB.length) {
                Object[] cur = q.toArray(new Byte[0]);
                if (Arrays.equals(cur, targetB)) {
                    idx = i - targetB.length;
                    break;
                }
                q.pop();
                q.addLast(input[i]);
            } else {
                q.addLast(input[i]);
            }
            ++i;
        }
        return idx;
    }
	
	public static byte[] reverseEndian(byte[] input) 
    {
        int i = 0;
        while (i < input.length / 2) {
            byte temp = input[i];
            input[i] = input[input.length - i - 1];
            input[input.length - i - 1] = temp;
            ++i;
        }
        return input;
    }
	
	public static ArrayList<Byte> toByteList(byte[] input)
    {
    	ArrayList<Byte> result = new ArrayList<>();
        for (byte anInput : input) result.add(anInput);
    	return result;
    }
	
	public static byte[] leIntToByteArray(int i) 
    {
        final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }
	
	public static byte[] byteListToArray(List<Byte> byteList) 
    {
        byte[] byteArray = new byte[byteList.size()];
        int index = 0;
        while (index < byteList.size()) {
            byteArray[index] = byteList.get(index);
            ++index;
        }
        return byteArray;
    }
	
	public static byte[] getSearchBytes(String input) throws UnsupportedEncodingException
	{
		ArrayList<Byte> bytes = new ArrayList<>();
		bytes.add((byte) 0);
		for(Byte b : input.getBytes("shift-jis"))
			bytes.add(b);
		bytes.add((byte) 0);
		
		byte[] temp = new byte[bytes.size()];
		for(int x = 0; x < bytes.size(); x++)
			temp[x] = bytes.get(x);
		return temp;
	}
}
