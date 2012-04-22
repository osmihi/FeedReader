package com.osmihi.feedReader;

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
		boolean urlOK = false;
		
		while (!urlOK) {
			try {
				mainUrl = new URL(m);
				urlOK = true;
			} catch (MalformedURLException e) {
				try {
					m = findUrl(m);
				} catch (SearchFailException e1) {
					throw new FeedNotFoundException("Failure during search: " + e1.toString());
				}
			}
		}

		try {
			feedUrl = findFeed(mainUrl);
			verifyFeed();
		} catch (FeedNotFoundException e) {
			throw e;
		}

		// do this later, from outside, esp. not in the constructor
		//makeStories();
	}

	private String findUrl(String m) throws SearchFailException {
		String searchUrl = "";
		String newUrlStr = "";
		
		// URL encode the string so we can insert it into the search query URL
		try {
			m = java.net.URLEncoder.encode(m, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// No need to worry since we explicitly provide a valid encoding above
		} finally {
			searchUrl = "http://api.duckduckgo.com/?q=" + m + "&format=xml";
		}
		
		try {
			// Get some search results from DuckDuckGo in XML format
			Document doc = Jsoup.connect(searchUrl).get();
			
			// Locate the tag where the first result's URL is stored
			Elements results = doc.getElementsByTag("FirstURL");
			
			if (results.size() != 0) {
				Element result = results.get(0);
				newUrlStr = result.html();
			} else {
				throw new SearchFailException("No results from DuckDuckGo");
			}
		} catch (IOException e) {
			throw new SearchFailException("Problem connecting to DuckDuckGo");
		} catch (Exception e) {
			throw new SearchFailException("Some other search failure");
		}
		
		return newUrlStr;
	}
	
	private URL findFeed(URL mainUrl) throws FeedNotFoundException {
		try {
			// Jump through the saved file to find the feed url
			Document doc = Jsoup.connect(mainUrl.toString()).get();
			
			// Search for links that match rss or atom in the type attribute
			Elements feedLinks = doc.select("link[type*=rss],link[type*=atom]");
			
			// TODO Let user choose which RSS to follow? Right now, default to first one.
			Element theFeed = feedLinks.get(0);
			return new URL(theFeed.attr("href"));
			
		} catch (IndexOutOfBoundsException e) {
			throw new FeedNotFoundException("Mysterious ArrayList IndexOutOfBoundsException!");
		} catch (MalformedURLException e) {
			try {// Second attempt to find a feed link. (just adds rss to end of url)
				return new URL(mainUrl + "rss");
			} catch (MalformedURLException e1) {
				throw new FeedNotFoundException("Unable to find feed for " + mainUrl.toString());
			}
		} catch (IOException e) {
			throw new FeedNotFoundException("Problem connecting to " + mainUrl.toString());
		}
	}

	private void verifyFeed() throws FeedNotFoundException {
		SyndFeedInput input = new SyndFeedInput();
		try {
			feed = input.build(new XmlReader(feedUrl));
		} catch (IllegalArgumentException e) {
			throw new FeedNotFoundException("IllegalArgumentException reading feed");
		} catch (FeedException e) {
			throw new FeedNotFoundException("FeedException reading feed");
		} catch (IOException e) {
			throw new FeedNotFoundException("IOException reading feed");
		}
	}
	
	private void makeStories() {
		List<SyndEntryImpl> entries = feed.getEntries();

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
/*		outputString += "Stories: \n";
		
		for (int i = 0; i < storyList.size(); i++) {
			outputString += storyList.get(i).toString() + "\n";
		}
*/		return outputString;
	}

	public static String makeSite(String siteStr) {
		try {
			Site aSite = new Site(siteStr);
			return aSite.toString();
		} catch (FeedNotFoundException e) {
			return e.toString();
		}
	}	
}

class SearchFailException extends Exception {
	private static final long serialVersionUID = 1L;
	String msg = "Error performing web search.";
	
	SearchFailException() {}
	
	SearchFailException(String u) {msg += " (" + u + ")";}
	
	public String toString() {return msg;}
}

class FeedNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;
	String msg = "Error finding feed.";
	
	FeedNotFoundException() {}
	
	FeedNotFoundException(String u) {msg += " (" + u + ")";}
	
	public String toString() {return msg;}
}
