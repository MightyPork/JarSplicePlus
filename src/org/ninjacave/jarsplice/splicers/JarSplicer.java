package org.ninjacave.jarsplice.splicers;


import java.io.FileOutputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;


public class JarSplicer extends Splicer {
	
	public void createFatJar(String[] jars, String[] natives, String output, String mainClass, String vmArgs) throws Exception
	{
		final Manifest manifest = getManifest(mainClass, vmArgs);
		
		final FileOutputStream fos = new FileOutputStream(output);
		final JarOutputStream jos = new JarOutputStream(fos, manifest);
		try {
			addFilesFromJars(jars, jos);
			addNativesToJar(natives, jos);
			addJarLauncher(jos);
			makeExecutable(output);
		} finally {
			jos.close();
			fos.close();
		}
	}
}
