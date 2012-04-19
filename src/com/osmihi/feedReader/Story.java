package com.osmihi.feedReader;

import java.net.MalformedURLException;
import java.net.URL;
//fkgkfkfkfkfkf
public class Story {
	private String title;
	private String description;
	private URL link;
	
	Story(String t, String d, String l) {
		title = t;
		description = d;
		try {
			link = new URL(l);
		} catch (MalformedURLException e) {e.printStackTrace();}
	}
	
	// Get methods
	
	public String getTitle() {return title;}
	
	public String getDescription() {return description;}
	
	public URL getLink() {return link;}
	
	// Set methods
	
	public void setTitle(String title) {this.title = title;}
	
	public void setDescription(String description) {this.description = description;}
	
	public void setLink(URL link) {this.link = link;}
	
	public String toString() {
		String outputString = "";
		
		outputString += getTitle() + "\n";
		outputString += getLink() + "\n";
		outputString += getDescription() + "\n";
		
		return outputString;
	}
	
}
