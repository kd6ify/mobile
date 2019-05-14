package com.futureconcepts.ax.sync;

public class Base64
{
    protected static String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    static public byte[] decode(String encodedData)
    {
    	byte[] data = new byte[encodedData.length() * 3 / 4];
    	int dc = 0;
    	int[] buffer = new int[4];
    	int bc = 0;
    	for (byte c : encodedData.getBytes())
    	{
    		if (c == '=')
    		{
    			buffer[bc++] = -1;
    		}
    		else
    		{
    			int i = ALPHABET.indexOf(c);
    			if (i >= 0)
    			{
    				buffer[bc++] = i;
    			}
    		}
    		if (bc == 4)
    		{
    			if (buffer[0] != -1 && buffer[1] != -1) 
    			{
    				data[dc++] = (byte) ((buffer[0] << 2) | (buffer[1] >> 4));
    				if (buffer[2] != -1) 
    				{
    					data[dc++] = (byte) (((buffer[1] << 4) & 0xf0) | (buffer[2] >> 2));
    					if (buffer[3] != -1) 
    					{
    						data[dc++] = (byte) (((buffer[2] << 6) & 0xc0) | buffer[3]);
    					}
    				}
    			}
    			bc = 0;
    		}
    	}
    	if (dc != data.length)
    	{
    		byte[] tmp = new byte[dc];
    		System.arraycopy(data, 0, tmp, 0, dc);
    		data = tmp;
    	}
    	return data;
    }
}
