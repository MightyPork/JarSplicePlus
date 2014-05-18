package org.ninjacave.jarsplice.splicers;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.ninjacave.jarsplice.Utils;


/**
 * Windows exe splicer
 * 
 * @author TheNinjaCave
 */
public class WinExeSplicer extends Splicer {
	
	private static final String stubFile = "res/stub.exe";
	
	
	public void createExe(String[] jars, String[] natives, String output, String mainClass, String vmArgs) throws Exception
	{
		FileOutputStream fos = null;
		InputStream is = null;
		JarOutputStream jos = null;
		
		try {
			
			fos = new FileOutputStream(output);
			is = getResourceAsStream(this.stubFile);
			
			Utils.copyStream(is, fos);
			
			fos.flush();
			
			final Manifest manifest = getManifest(mainClass, vmArgs);
			jos = new JarOutputStream(fos, manifest);
			
			addFilesFromJars(jars, jos);
			addNativesToJar(natives, jos);
			addJarLauncher(jos);
			makeExecutable(output);
			
		} finally {
			if (fos != null) try {
				fos.close();
			} catch (final IOException e) {}
			
			if (jos != null) try {
				jos.close();
			} catch (final IOException e) {}
			
			if (is != null) try {
				is.close();
			} catch (final IOException e) {}
		}
	}
	
	
	@Override
	protected boolean shouldAddNativeToJar(String native1)
	{
		return native1.toLowerCase().endsWith(".dll");
	}
}
