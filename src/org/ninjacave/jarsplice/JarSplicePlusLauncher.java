package org.ninjacave.jarsplice;


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
public class JarSplicePlusLauncher {
	
	private boolean jspVerbose;
	
	
	public JarSplicePlusLauncher(String[] cliArgs) throws Exception
	{
		final File file = getCodeSourceLocation();
		final String nativeDirectory = getNativeDirectory();
		final String mainClass = getMainClass(file);
		final String vmArgs = getVmArgs(file);
		final String javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		
		// parse launcher args
		for (final String s : cliArgs) {
			if (s.equalsIgnoreCase("--jspl:verbose")) {
				jspVerbose = true;
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
				if (arg.startsWith("--jspl:")) continue;
				arguments.add(arg);
			}
			
			if (jspVerbose) {
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
				if (jspVerbose) System.out.print("JSP> ");
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
		final JarFile jarFile = new JarFile(file, false);
		final Enumeration<?> entities = jarFile.entries();
		
		while (entities.hasMoreElements()) {
			final JarEntry entry = (JarEntry) entities.nextElement();
			
			if ((!entry.isDirectory()) && (entry.getName().indexOf('/') == -1)) {
				if (isNativeFile(entry.getName())) {
					final InputStream in = jarFile.getInputStream(jarFile.getEntry(entry.getName()));
					final OutputStream out = new FileOutputStream(nativeDirectory + File.separator + entry.getName());
					
					copyStream(in, out, 65535);
					
					in.close();
					out.close();
				}
			}
		}
		jarFile.close();
	}
	
	
	public boolean isNativeFile(String entryName)
	{
		final String osName = System.getProperty("os.name");
		final String name = entryName.toLowerCase();
		
		if (osName.startsWith("Win")) {
			if (name.endsWith(".dll")) return true;
		} else if (osName.startsWith("Linux")) {
			if (name.endsWith(".so")) return true;
		} else if (((osName.startsWith("Mac")) || (osName.startsWith("Darwin"))) && ((name.endsWith(".jnilib")) || (name.endsWith(".dylib")))) {
			return true;
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
			return new File(JarSplicePlusLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	private static long copyStream(InputStream in, OutputStream out, int buffer_size) throws IOException
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
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception
	{
		final JarSplicePlusLauncher fatJarLauncher = new JarSplicePlusLauncher(args);
	}
}
