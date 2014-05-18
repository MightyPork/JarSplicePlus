package org.ninjacave.jarsplice.gui;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.ninjacave.jarsplice.splicers.MacAppSplicer;


/**
 * Gui "export for mac" panel
 * 
 * @author TheNinjaCave
 */
public class ExportMacAppPanel extends JPanel implements ActionListener {
	
	JFileChooser fileChooser;
	JFileChooser iconChooser;
	JButton macAppButton;
	JButton iconButton;
	JarSpliceFrame jarSplice;
	MacAppSplicer macAppSplicer = new MacAppSplicer();
	JTextField nameTextField;
	JTextField iconTextField;
	
	
	public ExportMacAppPanel(JarSpliceFrame jarSplice)
	{
		this.jarSplice = jarSplice;
		
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);
		
		this.fileChooser = getFileChooser();
		this.iconChooser = getIconChooser();
		
		setLayout(new BorderLayout(5, 20));
		
		final TitledBorder border = BorderFactory.createTitledBorder("Create OS X APP Bundle");
		border.setTitleJustification(2);
		setBorder(border);
		
		add(createDescriptionPanel(), "First");
		
		final JPanel panel1 = new JPanel(new BorderLayout());
		panel1.add(createNamePanel(), "First");
		add(panel1, "Center");
		
		final JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(createIconPanel(), "First");
		panel1.add(panel2, "Center");
		
		final JPanel panel3 = new JPanel();
		panel3.add(new JLabel(), "First");
		panel3.add(createButtonPanel(), "Center");
		panel2.add(panel3, "Center");
	}
	
	
	private JPanel createDescriptionPanel()
	{
		final JPanel descriptionPanel = new JPanel();
		final JLabel label = new JLabel();
		label.setText(String.format(
				"<html><div style=\"width:%dpx;\">%s</div><html>",
				new Object[] {
						Integer.valueOf(300),
						"This is an optional step and will create an OS X APP Bundle. If there are native files then only the Mac native files (*.jnilib and *.dylib) will be added to the APP Bundle." }));
		
		descriptionPanel.add(label);
		
		return descriptionPanel;
	}
	
	
	private JPanel createButtonPanel()
	{
		final JPanel buttonPanel = new JPanel();
		this.macAppButton = new JButton("Create OS X APP Bundle");
		this.macAppButton.addActionListener(this);
		buttonPanel.add(this.macAppButton);
		
		return buttonPanel;
	}
	
	
	public JPanel createNamePanel()
	{
		final JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new FlowLayout(1, 0, 0));
		
		final JPanel pathPanel = new JPanel();
		this.nameTextField = new JTextField("");
		this.nameTextField.setDocument(new JTextFieldLimit(32));
		this.nameTextField.setPreferredSize(new Dimension(380, 30));
		this.nameTextField.setMinimumSize(new Dimension(380, 30));
		this.nameTextField.setMaximumSize(new Dimension(380, 30));
		pathPanel.add(this.nameTextField);
		
		selectPanel.add(pathPanel);
		
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 20));
		final TitledBorder border1 = BorderFactory.createTitledBorder("Set APP Bundle Name");
		panel.setBorder(border1);
		
		final JPanel descriptionPanel = new JPanel();
		final JLabel label = new JLabel();
		label.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", new Object[] { Integer.valueOf(300), "Set the name of the APP Bundle." }));
		
		descriptionPanel.add(label);
		
		panel.add(selectPanel, "Center");
		panel.add(descriptionPanel, "First");
		panel.add(new JLabel(), "Last");
		
		return panel;
	}
	
	
	public JPanel createIconPanel()
	{
		final JPanel selectPanel = new JPanel();
		
		selectPanel.setLayout(new FlowLayout(1, 0, 0));
		
		final JPanel pathPanel = new JPanel();
		this.iconTextField = new JTextField("");
		this.iconTextField.setPreferredSize(new Dimension(280, 30));
		this.iconTextField.setMinimumSize(new Dimension(280, 30));
		this.iconTextField.setMaximumSize(new Dimension(280, 30));
		pathPanel.add(this.iconTextField);
		
		final JPanel buttonPanel = new JPanel();
		this.iconButton = new JButton("Select Icon");
		this.iconButton.addActionListener(this);
		buttonPanel.add(this.iconButton);
		
		selectPanel.add(pathPanel);
		selectPanel.add(buttonPanel);
		
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 20));
		final TitledBorder border1 = BorderFactory.createTitledBorder("Set APP Bundle Icon");
		panel.setBorder(border1);
		
		final JPanel descriptionPanel = new JPanel();
		final JLabel label = new JLabel();
		label.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", new Object[] { Integer.valueOf(300),
				"Select the icon the app bundle will use. This should be in the Apple Icon Image format (*.icns)." }));
		
		descriptionPanel.add(label);
		
		panel.add(selectPanel, "Center");
		panel.add(descriptionPanel, "First");
		panel.add(new JLabel(), "Last");
		
		return panel;
	}
	
	
	public String getOutputFile(File file)
	{
		String outputFile = file.getAbsolutePath();
		
		if (!outputFile.endsWith(".zip")) {
			outputFile = outputFile + ".zip";
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
				return filename.endsWith(".zip");
			}
			
			
			@Override
			public String getDescription()
			{
				return "*.zip";
			}
		};
		this.fileChooser.setFileFilter(filter);
		
		return this.fileChooser;
	}
	
	
	private JFileChooser getIconChooser()
	{
		this.iconChooser = new JFileChooser();
		
		this.iconChooser.setAcceptAllFileFilterUsed(false);
		
		final FileFilter filter = new FileFilter() {
			
			@Override
			public boolean accept(File file)
			{
				if (file.isDirectory()) return true;
				final String filename = file.getName();
				return filename.endsWith(".icns");
			}
			
			
			@Override
			public String getDescription()
			{
				return "*.icns";
			}
		};
		this.iconChooser.setFileFilter(filter);
		
		return this.iconChooser;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.macAppButton) {
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
					this.macAppSplicer.createAppBundle(sources, natives, output, mainClass, vmArgs, this.nameTextField.getText(), this.iconTextField.getText());
					
					JOptionPane.showMessageDialog(this, "APP Bundle Successfully Created.", "Success", -1);
				} catch (final Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, "APP Bundle creation failed due to the following exception:\n" + ex.getMessage(), "Failed", 0);
				}
				
				System.out.println("File Saved as " + output);
			}
		} else if (e.getSource() == this.iconButton) {
			this.iconChooser.setCurrentDirectory(this.jarSplice.lastDirectory);
			final int value = this.iconChooser.showDialog(this, "Add");
			this.jarSplice.lastDirectory = this.iconChooser.getCurrentDirectory();
			
			if (value == 0) {
				final File iconFile = this.iconChooser.getSelectedFile();
				try {
					this.iconTextField.setText(iconFile.getCanonicalPath());
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public class JTextFieldLimit extends PlainDocument {
		
		private final int limit;
		
		
		JTextFieldLimit(int limit)
		{
			this.limit = limit;
		}
		
		
		@Override
		public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
		{
			if (str == null) {
				return;
			}
			if (getLength() + str.length() <= this.limit) super.insertString(offset, str, attr);
		}
	}
}
