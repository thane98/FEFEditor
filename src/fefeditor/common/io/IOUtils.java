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
}
