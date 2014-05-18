package org.ninjacave.jarsplice.splicers;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;


/**
 * Shell script splicer (for Linux)
 * 
 * @author TheNinjaCave
 */
public class ShellScriptSplicer extends Splicer {
	
	String[] batchFile = { "#!/bin/sh", "FNAME=\"`readlink -f \"$0\"`\"", "java -jar \"$FNAME\"", "exit 0", "" };
	
	
	@Override
	public void createFatJar(String[] jars, String[] natives, String output, String mainClass, String vmArgs) throws Exception
	{
		FileOutputStream fos = null;
		PrintStream pos = null;
		JarOutputStream jos = null;
		
		try {
			
			fos = new FileOutputStream(output);
			pos = new PrintStream(fos);
			
			for (final String element : this.batchFile) {
				pos.println(element);
			}
			
			pos.flush();
			fos.flush();
			
			final Manifest manifest = getManifest(mainClass, vmArgs);
			jos = new JarOutputStream(fos, manifest);
			
			addFilesFromJars(jars, jos);
			addNativesToJar(natives, jos);
			addJarLauncher(jos);
			
		} finally {
			if (jos != null) try {
				jos.close();
			} catch (final IOException e) {}
			
			if (fos != null) try {
				fos.close();
			} catch (final IOException e) {}
			
			if (pos != null) try {
				pos.close();
			} catch (final Exception e) {}
		}
	}
	
	
	@Override
	protected boolean shouldAddNativeToJar(String native1)
	{
		return native1.toLowerCase().endsWith(".so");
	}
}
