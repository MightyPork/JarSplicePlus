package org.ninjacave.jarsplice.gui;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;


/**
 * Gui "add jars" panel
 * 
 * @author TheNinjaCave
 */
public class JarsPanel extends JPanel implements ActionListener {
	
	JarSpliceFrame jarSplice;
	JFileChooser fileChooser;
	JList list;
	DefaultListModel listModel = new DefaultListModel();
	JButton addButton;
	JButton removeButton;
	File[] selectedFiles;
	
	
	public JarsPanel(JarSpliceFrame jarSplice)
	{
		this.jarSplice = jarSplice;
		
		this.fileChooser = new JFileChooser();
		this.fileChooser.setMultiSelectionEnabled(true);
		this.fileChooser.setAcceptAllFileFilterUsed(false);
		
		final FileFilter filter = new FileFilter() {
			
			@Override
			public boolean accept(File file)
			{
				if (file.isDirectory()) return true;
				final String filename = file.getName();
				return (filename.endsWith(".jar")) || (filename.endsWith(".zip"));
			}
			
			
			@Override
			public String getDescription()
			{
				return "*.jar, *.zip";
			}
		};
		this.fileChooser.setFileFilter(filter);
		
		setLayout(new BorderLayout(5, 5));
		
		this.list = new JList(this.listModel);
		add(this.list, "Center");
		
		final TitledBorder border = BorderFactory.createTitledBorder("Add Jars");
		border.setTitleJustification(2);
		setBorder(border);
		
		final JPanel buttonPanel = new JPanel();
		
		this.addButton = new JButton("Add Jar(s)");
		this.addButton.addActionListener(this);
		buttonPanel.add(this.addButton);
		
		this.removeButton = new JButton("Remove Jar(s)");
		this.removeButton.addActionListener(this);
		buttonPanel.add(this.removeButton);
		
		add(buttonPanel, "Last");
	}
	
	
	public String[] getSelectedFiles()
	{
		if (this.selectedFiles == null) return new String[0];
		
		final String[] files = new String[this.listModel.getSize()];
		
		for (int i = 0; i < files.length; i++) {
			files[i] = ((String) this.listModel.get(i));
		}
		
		return files;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.addButton) {
			this.fileChooser.setCurrentDirectory(this.jarSplice.lastDirectory);
			final int value = this.fileChooser.showDialog(this, "Add");
			this.jarSplice.lastDirectory = this.fileChooser.getCurrentDirectory();
			
			if (value == 0) {
				this.selectedFiles = this.fileChooser.getSelectedFiles();
				
				for (final File selectedFile : this.selectedFiles) {
					this.listModel.removeElement(selectedFile.getAbsolutePath());
					this.listModel.addElement(selectedFile.getAbsolutePath());
				}
			}
			
		} else if (e.getSource() == this.removeButton) {
			final Object[] selectedItems = this.list.getSelectedValues();
			for (final Object selectedItem : selectedItems)
				this.listModel.removeElement(selectedItem);
		}
	}
}
