package org.ninjacave.jarsplice.gui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.ninjacave.jarsplice.splicers.Splicer;


/**
 * Gui "export a jar" panel
 * 
 * @author TheNinjaCave
 */
public class CreatePanel extends JPanel implements ActionListener {
	
	JFileChooser fileChooser;
	JButton createButton;
	JarSpliceFrame jarSplice;
	Splicer splicer = new Splicer();
	
	
	public CreatePanel(JarSpliceFrame jarSplice)
	{
		this.jarSplice = jarSplice;
		
		this.fileChooser = new JFileChooser() {
			
			@Override
			public void approveSelection()
			{
				final File f = getSelectedFile();
				if ((f.exists()) && (getDialogType() == 1)) {
					final int result = JOptionPane.showConfirmDialog(this, "The file already exists. Do you want to overwrite it?", "Confirm Replace", 0);
					switch (result) {
						case 0:
							super.approveSelection();
							return;
						case 1:
							return;
						case 2:
							return;
					}
				}
				super.approveSelection();
			}
		};
		this.fileChooser.setAcceptAllFileFilterUsed(false);
		
		final FileFilter filter = new FileFilter() {
			
			@Override
			public boolean accept(File file)
			{
				if (file.isDirectory()) return true;
				final String filename = file.getName();
				return filename.endsWith(".jar");
			}
			
			
			@Override
			public String getDescription()
			{
				return "*.jar";
			}
		};
		this.fileChooser.setFileFilter(filter);
		
		final TitledBorder border = BorderFactory.createTitledBorder("Create Fat Jar");
		border.setTitleJustification(2);
		setBorder(border);
		
		final JPanel buttonPanel = new JPanel();
		
		this.createButton = new JButton("Create Fat Jar");
		this.createButton.addActionListener(this);
		buttonPanel.add(this.createButton);
		
		add(buttonPanel);
	}
	
	
	public String getOutputFile(File file)
	{
		String outputFile = file.getAbsolutePath();
		
		if (!outputFile.endsWith(".jar")) {
			outputFile = outputFile + ".jar";
		}
		
		return outputFile;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.createButton) {
			this.fileChooser.setCurrentDirectory(this.jarSplice.lastDirectory);
			final int value = this.fileChooser.showSaveDialog(this);
			this.jarSplice.lastDirectory = this.fileChooser.getCurrentDirectory();
			
			if (value == 0) {
				final String[] jars = this.jarSplice.getJarsList();
				final String[] natives = this.jarSplice.getNativesList();
				final String output = getOutputFile(this.fileChooser.getSelectedFile());
				final String mainClass = this.jarSplice.getMainClass();
				final String vmArgs = this.jarSplice.getVmArgs();
				try {
					this.splicer.createFatJar(jars, natives, output, mainClass, vmArgs);
					
					JOptionPane.showMessageDialog(this, "Fat Jar Successfully Created.", "Success", -1);
				} catch (final Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(
							this,
							"Jar creation failed due to the following exception:\n" + ex.getClass().getSimpleName() + "\n - " + ex.getMessage(),
							"Failed",
							0);
				}
				
				System.out.println("File Saved as " + output);
			}
		}
	}
}
