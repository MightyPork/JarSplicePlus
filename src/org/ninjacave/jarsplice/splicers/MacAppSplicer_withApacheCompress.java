//package org.ninjacave.jarsplice.splicers;
//
//
//import java.io.*;
//import java.util.jar.JarOutputStream;
//import java.util.jar.Manifest;
//
//import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
//import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
//import org.ninjacave.jarsplice.JatSplicePlusLauncher;
//import org.ninjacave.jarsplice.Utils;
//
//
///**
// * Mac splicer
// * 
// * @author TheNinjaCave
// */
//public class MacAppSplicer_withApacheCompress extends Splicer {
//	
//	private void addZipEntry(String input, ZipArchiveOutputStream os, String name, boolean executableFile) throws Exception
//	{
//		InputStream is = null;
//		
//		try {
//			is = getResourceAsStream(input);
//			
//			final ZipArchiveEntry zae = new ZipArchiveEntry(name);
//			if (executableFile) zae.setUnixMode(33261);
//			else zae.setUnixMode(33188);
//			os.putArchiveEntry(zae);
//			Utils.copyStream(is, os);
//			os.closeArchiveEntry();
//			
//		} finally {
//			if (is != null) try {
//				is.close();
//			} catch (final IOException e) {}
//		}
//	}
//	
//	
//	private void addZipFolder(ZipArchiveOutputStream os, String folderName) throws Exception
//	{
//		final ZipArchiveEntry zae = new ZipArchiveEntry(folderName);
//		zae.setUnixMode(16877);
//		os.putArchiveEntry(zae);
//		os.closeArchiveEntry();
//	}
//	
//	
//	private void addFileAsZipEntry(File inputFile, ZipArchiveOutputStream os, String name) throws Exception
//	{
//		InputStream is = null;
//		
//		try {
//			is = new FileInputStream(inputFile);
//			
//			final ZipArchiveEntry zae = new ZipArchiveEntry(name);
//			zae.setUnixMode(33188);
//			os.putArchiveEntry(zae);
//			Utils.copyStream(is, os);
//			os.closeArchiveEntry();
//			
//		} finally {
//			if (is != null) try {
//				is.close();
//			} catch (final IOException e) {}
//		}
//	}
//	
//	
//	public void createAppBundle(String[] jars, String[] natives, String output, String mainClass, String vmArgs, String bundleName, String icon)
//			throws Exception
//	{
//		
//		final File tmpJarFile = new File(output + ".tmp");
//		
//		FileOutputStream fos = null;
//		ZipArchiveOutputStream zaos = null;
//		PrintStream ps = null;
//		
//		try {
//			fos = new FileOutputStream(output);
//			zaos = new ZipArchiveOutputStream(fos);
//			
//			final String appName = bundleName + ".app/";
//			
//			addZipFolder(zaos, appName);
//			addZipFolder(zaos, appName + "Contents/");
//			addZipFolder(zaos, appName + "Contents/MacOS/");
//			addZipFolder(zaos, appName + "Contents/Resources/");
//			addZipFolder(zaos, appName + "Contents/Resources/Java/");
//			
//			addZipEntry("res/Contents/PkgInfo", zaos, appName + "Contents/PkgInfo", false);
//			addZipEntry("res/Contents/MacOS/JavaApplicationStub", zaos, appName + "Contents/MacOS/JavaApplicationStub", true);
//			addZipEntry("res/Contents/MacOS/mac_launch_fd.sh", zaos, appName + "Contents/MacOS/mac_launch_fd.sh", true);
//			
//			File iconFile = null;
//			
//			if (icon.length() != 0) {
//				iconFile = new File(icon);
//				
//				if ((!iconFile.exists()) || (!iconFile.isFile())) {
//					throw new Exception("Icon file not found at: " + icon);
//				}
//				
//				addFileAsZipEntry(iconFile, zaos, appName + "Contents/Resources/" + iconFile.getName());
//			}
//			
//			createTmpJar(jars, natives, tmpJarFile, mainClass, vmArgs);
//			addFileAsZipEntry(tmpJarFile, zaos, appName + "Contents/Resources/Java/app.jar");
//			
//			final ZipArchiveEntry zae = new ZipArchiveEntry(appName + "Contents/Info.plist");
//			zae.setUnixMode(33188);
//			zaos.putArchiveEntry(zae);
//			
//			ps = new PrintStream(zaos);
//			final String iconFileName = iconFile != null ? iconFile.getName() : null;
//			writePlistFile(ps, bundleName, iconFileName);
//			ps.flush();
//			
//			zaos.closeArchiveEntry();
//		} finally {
//			
//			if (ps != null) try {
//				ps.close();
//			} catch (final Exception e) {}
//			
//			if (zaos != null) try {
//				zaos.close();
//			} catch (final Exception e) {}
//			
//			if (fos != null) try {
//				fos.close();
//			} catch (final Exception e) {}
//			
//			tmpJarFile.delete();
//		}
//	}
//	
//	
//	private void createTmpJar(String[] jars, String[] natives, File tmpJarFile, String mainClass, String vmArgs) throws Exception
//	{
//		FileOutputStream fos = null;
//		JarOutputStream jos = null;
//		final Manifest manifest = getManifest(mainClass, vmArgs);
//		try {
//			fos = new FileOutputStream(tmpJarFile);
//			jos = new JarOutputStream(fos, manifest);
//			
//			addFilesFromJars(jars, jos);
//			addNativesToJar(natives, jos);
//			addJarLauncher(jos);
//		} finally {
//			if (jos != null) try {
//				jos.close();
//			} catch (final IOException e) {}
//			
//			if (fos != null) try {
//				fos.close();
//			} catch (final Exception e) {}
//		}
//	}
//	
//	
//	@Override
//	protected boolean shouldAddNativeToJar(String native1)
//	{
//		return native1.endsWith(".jnilib") || native1.endsWith(".dylib");
//	}
//	
//	
//	private void writePlistFile(PrintStream pos, String bundleName, String iconFile)
//	{
//		pos.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//		pos.println("<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">");
//		pos.println("<plist version=\"1.0\">");
//		pos.println("<dict>");
//		
//		writeKeyString(pos, "CFBundleAllowMixedLocalizations", "true");
//		writeKeyString(pos, "CFBundleDevelopmentRegion", "English");
//		writeKeyString(pos, "CFBundleExecutable", "JavaApplicationStub");
//		writeKeyString(pos, "CFBundleGetInfoString", bundleName + " 1.0.0");
//		if (iconFile != null) writeKeyString(pos, "CFBundleIconFile", iconFile);
//		writeKeyString(pos, "CFBundleInfoDictionaryVersion", "6.0");
//		writeKeyString(pos, "CFBundleName", bundleName);
//		writeKeyString(pos, "CFBundlePackageType", "APPL");
//		writeKeyString(pos, "CFBundleShortVersionString", "1.0.0");
//		writeKeyString(pos, "CFBundleSignature", "????");
//		writeKeyString(pos, "CFBundleVersion", "10.2");
//		
//		pos.println("<key>Java</key>");
//		pos.println("<dict>");
//		pos.println("<key>ClassPath</key>");
//		pos.println("<array>");
//		pos.println("<string>$JAVAROOT/app.jar</string>");
//		pos.println("</array>");
//		pos.println("<key>JVMVersion</key>");
//		pos.println("<string>1.5+</string>");
//		pos.println("<key>MainClass</key>");
//		pos.println("<string>" + JatSplicePlusLauncher.class.getName() + "</string>");
//		pos.println("<key>WorkingDirectory</key>");
//		pos.println("<string>$APP_PACKAGE/Contents/Resources/Java</string>");
//		pos.println("<key>Properties</key>");
//		pos.println("<dict>");
//		writeKeyString(pos, "apple.laf.useScreenMenuBar", "true");
//		pos.println("</dict>");
//		pos.println("</dict>");
//		
//		pos.println("</dict>");
//		pos.println("</plist>");
//	}
//	
//	
//	private void writeKeyString(PrintStream pos, String key, String string)
//	{
//		pos.println("<key>" + key + "</key>");
//		pos.println("<string>" + string + "</string>");
//	}
//}
