package org.ninjacave.jarsplice.gui;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.ninjacave.jarsplice.splicers.WinExeSplicer;


/**
 * Gui "export as windows exe" panel
 * 
 * @author TheNinjaCave
 */
public class ExportWinExePanel extends JPanel implements ActionListener {
	
	JFileChooser fileChooser;
	JButton winExeButton;
	JButton iconButton;
	JarSpliceFrame jarSplice;
	WinExeSplicer winExeSplicer = new WinExeSplicer();
	
	
	public ExportWinExePanel(JarSpliceFrame jarSplice)
	{
		this.jarSplice = jarSplice;
		
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);
		
		this.fileChooser = getFileChooser();
		
		setLayout(new BorderLayout(5, 20));
		
		final TitledBorder border = BorderFactory.createTitledBorder("Create EXE file for Windows");
		border.setTitleJustification(2);
		setBorder(border);
		
		add(createAppPanel(), "First");
		
		add(createButtonPanel(), "Center");
	}
	
	
	private JPanel createAppPanel()
	{
		final JPanel descriptionPanel = new JPanel();
		final JLabel label = new JLabel();
		label.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", new Object[] { Integer.valueOf(300),
				"This is an optional step and will create a Windows EXE File. " }));
		
		descriptionPanel.add(label);
		
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 20));
		
		panel.add(descriptionPanel, "First");
		
		return panel;
	}
	
	
	private JPanel createButtonPanel()
	{
		final JPanel buttonPanel = new JPanel();
		this.winExeButton = new JButton("Create Windows EXE file");
		this.winExeButton.addActionListener(this);
		buttonPanel.add(this.winExeButton);
		
		return buttonPanel;
	}
	
	
	public JPanel createIconPanel()
	{
		final JPanel selectPanel = new JPanel();
		
		selectPanel.setLayout(new FlowLayout(1, 0, 0));
		
		final JPanel pathPanel = new JPanel();
		final JTextField textField = new JTextField("image.png");
		textField.setPreferredSize(new Dimension(300, 30));
		textField.setMinimumSize(new Dimension(300, 30));
		textField.setMaximumSize(new Dimension(300, 30));
		
		pathPanel.add(textField);
		
		final JPanel buttonPanel = new JPanel();
		this.iconButton = new JButton("Select Icon");
		this.iconButton.addActionListener(this);
		buttonPanel.add(this.iconButton);
		
		selectPanel.add(pathPanel);
		selectPanel.add(buttonPanel);
		
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 20));
		final TitledBorder border1 = BorderFactory.createTitledBorder("Set Exe Icon");
		panel.setBorder(border1);
		
		final JPanel descriptionPanel = new JPanel();
		final JLabel label = new JLabel();
		label.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", new Object[] { Integer.valueOf(300),
				"Select the icon the exe will use. This should be in the*.png file format." }));
		
		descriptionPanel.add(label);
		
		panel.add(selectPanel, "First");
		panel.add(descriptionPanel, "Center");
		
		return panel;
	}
	
	
	public String getOutputFile(File file)
	{
		String outputFile = file.getAbsolutePath();
		
		if (!outputFile.endsWith(".exe")) {
			outputFile = outputFile + ".exe";
		}
		
		return outputFile;
	}
	
	
	private JFileChooser getFileChooser()
	{
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
				return filename.endsWith(".exe");
			}
			
			
			@Override
			public String getDescription()
			{
				return "*.exe";
			}
		};
		this.fileChooser.setFileFilter(filter);
		
		return this.fileChooser;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.winExeButton) {
			this.fileChooser.setCurrentDirectory(this.jarSplice.lastDirectory);
			final int value = this.fileChooser.showSaveDialog(this);
			this.jarSplice.lastDirectory = this.fileChooser.getCurrentDirectory();
			
			if (value == 0) {
				final String[] sources = this.jarSplice.getJarsList();
				final String[] natives = this.jarSplice.getNativesList();
				final String output = getOutputFile(this.fileChooser.getSelectedFile());
				final String mainClass = this.jarSplice.getMainClass();
				final String vmArgs = this.jarSplice.getVmArgs();
				try {
					this.winExeSplicer.createExe(sources, natives, output, mainClass, vmArgs);
					
					JOptionPane.showMessageDialog(this, "EXE Successfully Created.", "Success", -1);
				} catch (final Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, "EXE creation failed due to the following exception:\n" + ex.getMessage(), "Failed", 0);
				}
				
				System.out.println("File Saved as " + output);
			}
		}
	}
}
