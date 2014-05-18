package org.ninjacave.jarsplice.splicers;


import java.io.PrintStream;


class MacPlist {
	
	final PrintStream pos;
	private int arrays = 0;
	private int dicts = 0;
	private int plists = 0;
	
	
	MacPlist(PrintStream pos)
	{
		this.pos = pos;
	}
	
	
	void begin()
	{
		if (plists != 0) throw new RuntimeException("Plist already open.");
		
		pos.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pos.println("<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">");
		
		pos.println("<plist version=\"1.0\">");
		plists++;
		
		dictOpen(null);
	}
	
	
	void end()
	{
		if (plists != 1) throw new RuntimeException("Plist not open.");
		
		dictClose();
		pos.println("</plist>");
		plists--;
		
		if (plists != 0) throw new RuntimeException("Plist not closed.");
		if (arrays != 0) throw new RuntimeException("Plist array(s) not closed.");
		if (dicts != 0) throw new RuntimeException("Plist dict(s) not closed.");
	}
	
	
	void key(String key)
	{
		pos.println("<key>" + key + "</key>");
	}
	
	
	void string(String string)
	{
		pos.println("<string>" + string + "</string>");
	}
	
	
	void keyString(String key, String string)
	{
		key(key);
		string(string);
	}
	
	
	void dictOpen(String key)
	{
		if (key != null) key(key);
		pos.println("<dict>");
		dicts++;
	}
	
	
	void dictClose()
	{
		if (dicts <= 0) throw new RuntimeException("Plist dict(s) not open, cant close.");
		pos.println("</dict>");
		dicts--;
	}
	
	
	void arrayOpen(String key)
	{
		if (key != null) key(key);
		pos.println("<array>");
		arrays++;
	}
	
	
	void arrayClose()
	{
		if (arrays <= 0) throw new RuntimeException("Plist dict(s) not open, cant close.");
		pos.println("</array>");
		arrays--;
	}
}
