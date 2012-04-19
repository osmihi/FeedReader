package com.osmihi.feedReader;

// TODO Do we even use jdom??

import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndContent;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntryImpl;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.XmlReader;

public class Site {
	private URL mainUrl;
	private URL feedUrl;
	private ArrayList<Story> storyList = new ArrayList<Story>();
	SyndFeed feed = null;
	
	private final int MAX_SEARCHES = 6;
	private final int STORIES_PER_PAGE = 9;
	
	Site(String m) throws FeedNotFoundException {
		int att = 0;
		
		try {
			System.out.println("Trying " + m + "...");
			mainUrl = new URL(m);
			findFeed();
			if (!verifyFeed()) {throw new MalformedURLException();}
			
		} catch (MalformedURLException e) {
			try {
				System.out.println("Trying " + findUrl(m));
				mainUrl = new URL(findUrl(m));
			} catch (MalformedURLException e2) {
				throw new FeedNotFoundException();
			}
			findFeed();
			att++;
		}
		
		if (verifyFeed()) {
			makeStories();
		} else {
			throw new FeedNotFoundException(mainUrl.toString());
		}
			
	}
	
	private void findFeed() {
		// Jump through the saved file to find the feed url
		Document doc = null;
		
		try {
			doc = Jsoup.connect(mainUrl.toString()).get();
		} catch (SocketTimeoutException s) {
			s.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Search for links that match rss or atom in the type attribute
		Elements feedLinks = doc.select("link[type*=rss],link[type*=atom]");
		
		// TODO Let user choose which RSS to follow? Right now, default to first one.

		try {
			Element theFeed = feedLinks.get(0);
			feedUrl = new URL(theFeed.attr("href"));
		} catch (Exception e) {
			// Second attempt to find a feed link.
			try {
				feedUrl = new URL(mainUrl + "rss");
			} catch (Exception e1) {}
		}
	}

	private boolean verifyFeed() {
		SyndFeedInput input = new SyndFeedInput();
		try {
			feed = input.build(new XmlReader(feedUrl));
			return true;
		} catch (IOException e1) {
			return false;
		} catch (IllegalArgumentException e1) {
			return false;
		} catch (FeedException e1) {
			return false;
		}  
	}
	
	private String findUrl(String m) {
		//Since using the duckduckgo api only allows 1 search result, the 'attempt' argument isn't needed
		//TODO Find new api for full search results
		String newM = "";
		String newUrlStr = "";
		
		try {
			newM = java.net.URLEncoder.encode(m, "UTF-8");
		} catch (UnsupportedEncodingException e) {}
		String searchUrl = "http://api.duckduckgo.com/?q=" + newM + "&format=xml";
		/*BJbjhgfvhgvhgvghvhgvhggcfgcgfcgfc
		gfcghvgghvhghvjgh
		*/
		Document doc = null;
		try {
			doc = Jsoup.connect(searchUrl).get();
		} catch (IOException e) {e.printStackTrace();}
		
		// we had to use the DOM getElementsByTag thing below (rather than CSS selector)
		// in order to get around the fact that the namespace in bing result was making it tough to grab the element
		Elements results = doc.getElementsByTag("AbstractURL");
		
		if (results.size() != 0) {
			Element result = results.get(0);
			newUrlStr = result.html();
		}
		
		return newUrlStr;
	}
	
	private void makeStories() {
		List<SyndEntryImpl> entries = feed.getEntries();
		
		//System.out.println("Entries for " + feedUrl.toString() + ": " + entries.size());
		
		for (int i = 0; i < entries.size() && i < STORIES_PER_PAGE; i++) {
				String aTitle = "";
				String aDesc = "";
				String aLink = "";
				SyndContent aContent = null;
			
				SyndEntryImpl aStory = (SyndEntryImpl)(entries.get(i));
				
				aTitle = aStory.getTitle();
				aContent = aStory.getDescription();
				if (aContent != null) {
					aDesc = aContent.getValue();
					// TODO removing tags from description here. Move to elsewhere so we can use info contained in the html in description.
					aDesc = Jsoup.parse(aDesc).text();
				}
				aLink = aStory.getLink();
				
				Story storyObj = new Story(aTitle, aDesc, aLink);
				storyList.add(storyObj);
		}
		
	}
	
	public URL getMainUrl() {return mainUrl;}
	
	public URL getFeedUrl() {return feedUrl;}
	
	public ArrayList<Story> getStoryList() {return storyList;}
	
	public String toString() {
		String outputString = "";
		
		outputString += "Main URL: " + mainUrl.toString() + "\n";
		outputString += "Feed URL: " + feedUrl.toString() + "\n";
		outputString += "Stories: \n";
		
		for (int i = 0; i < storyList.size(); i++) {
			outputString += storyList.get(i).toString() + "\n";
		}
		return outputString;
	}
	
}

class FeedNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;
	String mUrl = "";
	FeedNotFoundException() {}
	
	FeedNotFoundException(String u) {mUrl = u;}
	
	public String getMainUrl() {return mUrl;}
}
