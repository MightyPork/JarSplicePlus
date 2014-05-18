package org.ninjacave.jarsplice.splicers;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.ninjacave.jarsplice.export.Launcher;


/**
 * Mac splicer
 * 
 * @author TheNinjaCave
 */
public class MacAppSplicer extends Splicer {
	
	private final JarSplicer macJarSplicer = new JarSplicer() {
		
		@Override
		protected boolean shouldAddNativeToJar(String native1)
		{
			return MacAppSplicer.this.shouldAddNativeToJar(native1);
		}
	};
	
	
	public void createAppBundle(String[] jars, String[] natives, String output, String mainClass, String vmArgs, String bundleName, String icon)
			throws IOException
	{
		
		final File tmpJarFile = File.createTempFile("jarsplice_export", ".jar");
		
		FileOutputStream fos = null;
		ZipOutputStream zaos = null;
		PrintStream ps = null;
		
		try {
			fos = new FileOutputStream(output);
			zaos = new ZipOutputStream(fos);
			
			final String appName = bundleName + ".app/";
			
			addZipFolder(zaos, appName);
			addZipFolder(zaos, appName + "Contents/");
			addZipFolder(zaos, appName + "Contents/MacOS/");
			addZipFolder(zaos, appName + "Contents/Resources/");
			addZipFolder(zaos, appName + "Contents/Resources/Java/");
			
			addZipFile(zaos, "res/Contents/PkgInfo", appName + "Contents/PkgInfo");
			addZipFile(zaos, "res/Contents/MacOS/JavaApplicationStub", appName + "Contents/MacOS/JavaApplicationStub");
			addZipFile(zaos, "res/Contents/MacOS/mac_launch_fd.sh", appName + "Contents/MacOS/mac_launch_fd.sh");
			
			File iconFile = null;
			
			if (icon.length() != 0) {
				iconFile = new File(icon);
				
				if ((!iconFile.exists()) || (!iconFile.isFile())) {
					throw new IOException("Icon file not found at: " + icon);
				}
				
				addZipFile(zaos, iconFile, appName + "Contents/Resources/" + iconFile.getName());
			}
			
			createTmpJar(jars, natives, tmpJarFile, mainClass, vmArgs);
			addZipFile(zaos, tmpJarFile, appName + "Contents/Resources/Java/app.jar");
			
			final ZipEntry zae = new ZipEntry(appName + "Contents/Info.plist");
			zaos.putNextEntry(zae);
			
			ps = new PrintStream(zaos);
			final String iconFileName = iconFile != null ? iconFile.getName() : null;
			writePlistFile(ps, bundleName, iconFileName);
			ps.flush();
			
			zaos.closeEntry();
			makeExecutable(output);
			
		} finally {
			
			if (ps != null) try {
				ps.close();
			} catch (final Exception e) {}
			
			if (zaos != null) try {
				zaos.close();
			} catch (final Exception e) {}
			
			if (fos != null) try {
				fos.close();
			} catch (final Exception e) {}
			
			tmpJarFile.delete();
		}
	}
	
	
	private void createTmpJar(String[] jars, String[] natives, File tmpJarFile, String mainClass, String vmArgs) throws IOException
	{
		macJarSplicer.createFatJar(jars, natives, tmpJarFile.getPath(), mainClass, vmArgs);
	}
	
	
	@Override
	protected boolean shouldAddNativeToJar(String native1)
	{
		return native1.endsWith(".jnilib") || native1.endsWith(".dylib");
	}
	
	
	private void writePlistFile(PrintStream pos, String bundleName, String iconFile)
	{
		final MacPlist plist = new MacPlist(pos);
		//@formatter:off
		plist.begin();
			plist.keyString("CFBundleAllowMixedLocalizations", "true");
			plist.keyString("CFBundleDevelopmentRegion", "English");
			plist.keyString("CFBundleExecutable", "JavaApplicationStub");
			plist.keyString("CFBundleGetInfoString", bundleName + " 1.0.0");
			if (iconFile != null) plist.keyString("CFBundleIconFile", iconFile);
			plist.keyString("CFBundleInfoDictionaryVersion", "6.0");
			plist.keyString("CFBundleName", bundleName);
			plist.keyString("CFBundlePackageType", "APPL");
			plist.keyString("CFBundleShortVersionString", "1.0.0");
			plist.keyString("CFBundleSignature", "????");
			plist.keyString("CFBundleVersion", "10.2");
			
			plist.dictOpen("Java");				
				plist.arrayOpen("ClassPath");
				plist.string("$JAVAROOT/app.jar");
				plist.arrayClose();
				
				plist.keyString("JVMVersion", "1.6+");
				
				plist.keyString("MainClass", Launcher.class.getName());
				plist.keyString("WorkingDirectory", "$APP_PACKAGE/Contents/Resources/Java");
				
				plist.dictOpen("Properties");				
					plist.keyString("apple.laf.useScreenMenuBar", "true");					
				plist.dictClose();			
			plist.dictClose();		
		plist.end();
		//@formatter:on
	}
	
}
