package org.ninjacave.jarsplice.gui;


import java.io.File;

import javax.swing.JFrame;


/**
 * Main frame
 * 
 * @author TheNinjaCave
 */
public class JarSpliceFrame {
	
	IntroductionPanel introPanel = new IntroductionPanel();
	JarsPanel jarsPanel = new JarsPanel(this);
	NativesPanel nativesPanel = new NativesPanel(this);
	ClassPanel classPanel = new ClassPanel(this);
	ExportJarPanel createPanel = new ExportJarPanel(this);
	ExportShellScriptPanel shellScriptPanel = new ExportShellScriptPanel(this);
	ExportMacAppPanel macAppPanel = new ExportMacAppPanel(this);
	ExportWinExePanel exePanel = new ExportWinExePanel(this);
	public File lastDirectory;
	
	
	public JarSpliceFrame()
	{
		
		//TITLE MODIFIED AS TO STATE THAT JARSPLICEPLUS IS AN EXTENSION TO JARSPLICE
		
		final JFrame frame = new JFrame("JarSplicePlus - An Extension to JarSplice");
		
		final TabPane tabPane = new TabPane(this);
		
		tabPane.addTab("INTRODUCTION", this.introPanel, true);
		tabPane.addTab("1) ADD JARS", this.jarsPanel, true);
		tabPane.addTab("2) ADD NATIVES", this.nativesPanel, true);
		tabPane.addTab("3) MAIN CLASS", this.classPanel, true);
		tabPane.addTab("4) CREATE FAT JAR", this.createPanel, true);
		
		tabPane.addTab("EXTRA (LINUX .SH)", this.shellScriptPanel, false);
		tabPane.addTab("EXTRA (MAC .APP)", this.macAppPanel, false);
		tabPane.addTab("EXTRA (WINDOWS .EXE)", this.exePanel, false);
		
		frame.add(tabPane, "Center");
		
		frame.setSize(640, 480);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(3);
	}
	
	
	public String[] getJarsList()
	{
		return this.jarsPanel.getSelectedFiles();
	}
	
	
	public String[] getNativesList()
	{
		return this.nativesPanel.getSelectedFiles();
	}
	
	
	public String getMainClass()
	{
		return this.classPanel.getMainClass();
	}
	
	
	public String getVmArgs()
	{
		return this.classPanel.getVmArgs();
	}
}
