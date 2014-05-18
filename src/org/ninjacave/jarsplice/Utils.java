package org.ninjacave.jarsplice;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Utils class for JarSplicePlus
 * 
 * @author MightyPork
 */
public class Utils {
	
	/**
	 * Copy an input stream to an output stream.
	 * 
	 * @param in input stream
	 * @param out output stream
	 * @param buffer_size size of the buffer array (bytes)
	 * @return total number of copied bytes
	 * @throws IOException
	 */
	public static long copyStream(InputStream in, OutputStream out, int buffer_size) throws IOException
	{
		final byte[] buffer = new byte[buffer_size];
		int n = 0;
		long count = 0L;
		while (-1 != (n = in.read(buffer))) {
			out.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
	
	
	public static long copyStream(InputStream in, OutputStream out) throws IOException
	{
		return copyStream(in, out, 8192);
	}
}
