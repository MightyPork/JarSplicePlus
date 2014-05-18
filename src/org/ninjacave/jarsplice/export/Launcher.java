package org.ninjacave.jarsplice.export;


import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


/**
 * Launcher bundled with the fat jar.<br>
 * This class must be entirely independent on the rest of the project.
 * 
 * @author TheNinjaCave
 */
public class Launcher {
	
	private boolean jsplVerbose;
	
	
	public Launcher(String[] cliArgs) throws Exception
	{
		final File file = getCodeSourceLocation();
		final String nativeDirectory = getNativeDirectory();
		final String mainClass = getMainClass(file);
		final String vmArgs = getVmArgs(file);
		final String javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		
		// parse launcher args
		for (final String s : cliArgs) {
			if (s.equalsIgnoreCase("--jspl-verbose")) {
				jsplVerbose = true;
			}
		}
		
		try {
			extractNatives(file, nativeDirectory);
			
			final ArrayList<String> arguments = new ArrayList<String>();
			
			arguments.add(javaPath);
			
			for (final String s : vmArgs.split(" ")) {
				if (s.length() == 0) continue;
				arguments.add(s);
			}
			
			arguments.add("-cp");
			arguments.add(file.getAbsoluteFile().toString());
			arguments.add("-Djava.library.path=" + nativeDirectory);
			arguments.add(mainClass);
			
			for (final String arg : cliArgs) {
				if (arg.length() == 0) continue;
				if (arg.startsWith("--jspl")) continue;
				arguments.add(arg);
			}
			
			if (jsplVerbose) {
				System.out.print("== JarSplicePlus ==\n> ");
				for (final String arg : arguments) {
					if (arg.startsWith("-")) System.out.print("\n\t");
					System.out.print(arg + " ");
				}
				System.out.println();
			}
			
			final ProcessBuilder processBuilder = new ProcessBuilder(arguments);
			processBuilder.redirectErrorStream(true);
			final Process process = processBuilder.start();
			
			writeConsoleOutput(process);
			
			process.waitFor();
		} finally {
			deleteNativeDirectory(nativeDirectory);
		}
	}
	
	
	public void writeConsoleOutput(Process process) throws Exception
	{
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try {
			
			is = process.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				if (jsplVerbose) System.out.print("JSP> ");
				System.out.println(line);
			}
			
		} finally {
			if (is != null) try {
				is.close();
			} catch (final IOException e) {}
			
			if (isr != null) try {
				isr.close();
			} catch (final IOException e) {}
			
			if (br != null) try {
				br.close();
			} catch (final IOException e) {}
		}
	}
	
	
	public void extractNatives(File file, String nativeDirectory) throws Exception
	{
		JarFile jarFile = null;
		
		try {
			
			jarFile = new JarFile(file, false);
			
			final Enumeration<?> entities = jarFile.entries();
			
			while (entities.hasMoreElements()) {
				final JarEntry entry = (JarEntry) entities.nextElement();
				
				if ((!entry.isDirectory()) && (entry.getName().indexOf('/') == -1)) {
					if (isNativeFile(entry.getName())) {
						
						InputStream in = null;
						// false positive
						@SuppressWarnings("resource")
						OutputStream out = null;
						
						try {
							
							in = jarFile.getInputStream(jarFile.getEntry(entry.getName()));
							out = new FileOutputStream(nativeDirectory + File.separator + entry.getName());
							
							final byte[] buffer = new byte[65535];
							int n;
							while (-1 != (n = in.read(buffer))) {
								out.write(buffer, 0, n);
							}
							
						} finally {
							if (in != null) try {
								in.close();
							} catch (final IOException e) {}
							
							if (out != null) try {
								out.close();
							} catch (final IOException e) {}
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
	
	
	public boolean isNativeFile(String entryName)
	{
		final String osName = System.getProperty("os.name");
		final String name = entryName.toLowerCase();
		
		if (osName.startsWith("Win")) {
			return name.endsWith(".dll");
			
		} else if (osName.startsWith("Linux")) {
			return name.endsWith(".so");
			
		} else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
			return name.endsWith(".jnilib") || name.endsWith(".dylib");
		}
		
		return false;
	}
	
	
	public String getNativeDirectory()
	{
		String nativeDir = System.getProperty("deployment.user.cachedir");
		
		if ((nativeDir == null) || (System.getProperty("os.name").startsWith("Win"))) {
			nativeDir = System.getProperty("java.io.tmpdir");
		}
		
		nativeDir = nativeDir + File.separator + "natives" + new Random().nextInt();
		
		final File dir = new File(nativeDir);
		
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		return nativeDir;
	}
	
	
	public void deleteNativeDirectory(String directoryName)
	{
		final File directory = new File(directoryName);
		
		final File[] files = directory.listFiles();
		for (final File file : files) {
			file.delete();
		}
		
		directory.delete();
	}
	
	
	public String getMainClass(File file) throws Exception
	{
		final JarFile jarFile = new JarFile(file);
		final Manifest manifest = jarFile.getManifest();
		final Attributes attribute = manifest.getMainAttributes();
		
		return attribute.getValue("Launcher-Main-Class");
	}
	
	
	public String getVmArgs(File file) throws Exception
	{
		final JarFile jarFile = new JarFile(file);
		final Manifest manifest = jarFile.getManifest();
		final Attributes attribute = manifest.getMainAttributes();
		
		return attribute.getValue("Launcher-VM-Args");
	}
	
	
	public File getCodeSourceLocation()
	{
		try {
			return new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception
	{
		new Launcher(args);
	}
}
