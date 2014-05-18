package org.ninjacave.jarsplice.splicers;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.ninjacave.jarsplice.JarSplicePlusLauncher;
import org.ninjacave.jarsplice.Utils;


/**
 * Abstract splicer
 * 
 * @author TheNinjaCave
 */
public abstract class Splicer {
	
	
	protected Manifest getManifest(String mainClass, String vmArgs)
	{
		final Manifest manifest = new Manifest();
		final Attributes attribute = manifest.getMainAttributes();
		attribute.putValue("Manifest-Version", "1.0");
		attribute.putValue("Main-Class", JarSplicePlusLauncher.class.getName());
		attribute.putValue("Launcher-Main-Class", mainClass);
		attribute.putValue("Launcher-VM-Args", vmArgs);
		return manifest;
	}
	
	
	protected void addFilesFromJars(String[] jars, JarOutputStream out) throws IOException
	{
		for (final String jar : jars) {
			ZipFile jarFile = null;
			
			try {
				
				jarFile = new ZipFile(jar);
				
				for (final ZipEntry entry : new EnumerationIterator<ZipEntry>(jarFile.entries())) {
					
					if (entry.isDirectory()) continue;
					
					if (!entry.getName().toLowerCase().startsWith("meta-inf")) {
						
						if (!entry.getName().contains(JarSplicePlusLauncher.class.getSimpleName())) {
							
							InputStream in = null;
							try {
								
								in = jarFile.getInputStream(jarFile.getEntry(entry.getName()));
								
								out.putNextEntry(new ZipEntry(entry.getName()));
								
								Utils.copyStream(in, out);
								
							} finally {
								if (in != null) try {
									in.close();
								} catch (final IOException e) {}
								out.closeEntry();
							}
						}
					}
				}
				
			} finally {
				if (jarFile != null) try {
					jarFile.close();
				} catch (final IOException e) {}
			}
		}
	}
	
	
	protected void addNativesToJar(String[] natives, JarOutputStream out) throws IOException
	{
		for (final String native1 : natives) {
			
			if (shouldAddNativeToJar(native1)) {
				
				@SuppressWarnings("resource")
				InputStream in = null;
				
				try {
					
					in = new FileInputStream(native1);
					out.putNextEntry(new ZipEntry(getFileName(native1)));
					
					Utils.copyStream(in, out);
					
				} finally {
					if (in != null) try {
						in.close();
					} catch (final IOException e) {}
				}
				
				out.closeEntry();
			}
		}
	}
	
	
	protected boolean shouldAddNativeToJar(String native1)
	{
		return true;
	}
	
	
	protected void addJarLauncher(JarOutputStream out) throws IOException
	{
		InputStream in = null;
		try {
			
			final String launcherPath = JarSplicePlusLauncher.class.getName().replace('.', '/') + ".class";
			
			in = getClass().getResourceAsStream("/" + launcherPath);

			out.putNextEntry(new ZipEntry(launcherPath.substring(0, launcherPath.lastIndexOf('/'))));
			out.closeEntry();			

			out.putNextEntry(new ZipEntry(launcherPath));
			
			Utils.copyStream(in, out);
			
			out.closeEntry();
			
		} finally {
			if (in != null) try {
				in.close();
			} catch (final IOException e) {}
		}
	}
	
	
	protected String getFileName(String ref)
	{
		ref = ref.replace('\\', '/');
		return ref.substring(ref.lastIndexOf('/') + 1);
	}
	
	
	protected InputStream getResourceAsStream(String res)
	{
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(res);
	}


	protected void makeExecutable(String output)
	{
		File f = new File(output);
		f.setExecutable(true);
	}
}
