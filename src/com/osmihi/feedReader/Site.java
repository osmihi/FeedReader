package com.osmihi.feedReader;

//import SearchFailException;

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
	private MainUrl mu;
	private FeedUrl fu;
	
	private ArrayList<Story> storyList = new ArrayList<Story>();
	
	// NOTE: Maximum # site / feed searches are found in the classes MainUrl and FeedUrl. Currently hard coded, we should change that.
	private final int STORIES_PER_PAGE = 9;			// note: should be set with reference to a value stored in an xml file.
	private final String BLEKKO_AUTH = "dfc0cc0f";
	
	Site(String m) throws FeedNotFoundException {
		try {
			mu = new MainUrl(m);
		} catch (OverCountException e) {
			throw new FeedNotFoundException(e.toString());
		}
		
		boolean feedFound = false;
		
		while(!feedFound) {
			try {
				fu = new FeedUrl(mu.getUrl());
				feedFound = true;
			} catch (OverCountException e) {
				try {
					mu.next();			
				} catch (OverCountException e1) {
					throw new FeedNotFoundException(e1.toString());
				}
			}
		}
		
		
	}

	// INNER CLASSES
	
	private interface Iterable {
		public void next() throws OverCountException;
	}
	
	private abstract class SiteUrl implements Iterable {
		protected URL url;
		protected int count;
		protected final int maxCount;
		
		SiteUrl(int maxCountNum) throws OverCountException {
			count = -1;
			maxCount = maxCountNum; // set maximum number of iterations
		}
		
		public void next() throws OverCountException {
			count++;
			if (count <= maxCount) {
				attempt();
			} else {throw new OverCountException("(in SiteUrl)");}
		}
		
		protected abstract void attempt();
		
		public URL getUrl() {return url;}
		public int getMaxCount() {return maxCount;}
	}
	
	private class MainUrl extends SiteUrl {
		// When created, an instance of this class must either have set its url to a valid Url, or throw an OverCountException.
		// its public next() method will try to find the next search result (and next and next) until a valid url is given.
		// an instance of this class represents only a valid url that has the potential to host feeds. it doesn't validate anything feed-related!
		
		private String possibleUrl;
		private boolean urlOK = false;
		private ArrayList<String> resultList = new ArrayList<String>();
		
		public MainUrl(String inStr) throws OverCountException {
			super(5); // init counter and set max number of sites to try for a given search term
			possibleUrl = inStr;
			
			while (!urlOK) {
				try {
					init();
					urlOK = true;
				} catch (MalformedURLException e) {
					next();
				}
			}
		}
		
		protected void init() throws MalformedURLException {
			url = new URL(possibleUrl);
		}
		
		protected void attempt() {
			String searchTerm = "";
			String searchUrl = "";
			
			if (count == 0) {
				// Get web search results via Blekko
				// First, escape the term and assemble the search URL
				try {
					searchTerm = java.net.URLEncoder.encode(possibleUrl, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// No need to worry since we explicitly provide a valid encoding above
				} finally {
					searchUrl = "http://blekko.com/?q=" + searchTerm + "+/rss&auth=" + BLEKKO_AUTH;
				}
				
				// Next, get the search results
				try {
					// Get some search results from Blekko
					SyndFeedInput input = new SyndFeedInput();
					SyndFeed searchResults = input.build(new XmlReader(new URL(searchUrl)));
					List<SyndEntryImpl> entries = searchResults.getEntries();		
					
					for (int i = 0; i < entries.size(); i++) {
						SyndEntryImpl aHit = (SyndEntryImpl)(entries.get(i));
						resultList.add(aHit.getLink());
					}
				} catch (FeedException e) {
//					throw new SearchFailException("Problem connecting to Blekko " + searchUrl);
				} catch (IOException e) {
//					throw new SearchFailException("Problem connecting to Blekko " + searchUrl);
				}	
			}
			
			// Finally, set possibleUrl to next Blekko result
			if (resultList.size() != 0) {
				boolean thisUrlOk = false;
				while (!thisUrlOk) { // TODO potential infinite loop problem?
				possibleUrl = resultList.get(count);
					try {
						init();
						thisUrlOk = true;
					} catch (MalformedURLException e) {}
				}
			}
		}
	}
	
	private class FeedUrl extends SiteUrl {
		private URL urlToCheck;
		private boolean urlOK = false;
		private boolean feedOK = false;
		private URL possibleFeed;
		private ArrayList<String> feedList = new ArrayList<String>();
		private SyndFeed feed;
		
		public FeedUrl(URL inUrl) throws OverCountException {
			super(5); 		// init counter and set max number of feed links to try for a given site
			urlToCheck = inUrl;
			try {
				init();
			} catch (OverCountException e) {throw e;}
			
			while (!feedOK) {
				if (feedList.size() == 0 || count >= feedList.size()-1) {throw new OverCountException("No valid feeds found for URL " + urlToCheck.toString());}
				next();
			}
			
			url = possibleFeed;
		}
		
		protected void init() throws OverCountException {
			// parse contents of urlToCheck and create queue of possible feeds
			try{
			// Look through the mainUrl's page to find the feed url
			Document doc = Jsoup.connect(urlToCheck.toString()).get();
			
			// Search for links that match rss or atom in the type attribute
			//Elements feedLinks = doc.select("link[type*=rss],link[type*=atom]");
			Elements feedLinks = doc.select("[type*=rss],[href*=rss],[type*=atom],[href=*atom]");
			
			for (Element e : feedLinks) {
				feedList.add(e.attr("href"));
			}
			} catch (IOException e) {
				throw new OverCountException("Could not connect to " + urlToCheck.toString());
			}
		}
		
		private boolean verifyUrl(){
			// ensures that the next feed candidate is a valid URL by creating a new URL object from it
			try {
				possibleFeed = new URL(feedList.get(count));
				return true;
			} catch (MalformedURLException e) {
				return false;
			}
		}
		
		private boolean verifyFeed() {
			// return true or false depending on if possibleFeed has a valid feed at it.
			SyndFeedInput input = new SyndFeedInput();
			try {
				XmlReader xr = new XmlReader(possibleFeed);
				feed = input.build(xr);
				return true;
			} catch (Exception e) {
				return false;
			}
			
		}
		
		protected void attempt() {
			if (verifyUrl()) {
				feedOK = verifyFeed();
			}
		}
		
		public SyndFeed getFeed() {return feed;}

	} // END FEEDURL CLASS
	
	private String makeStories() {
		SyndFeed feed = fu.getFeed();
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
		String str = "";
		for (Story s : storyList) {
			str += s.toString();
		}
		return str;
	 } // END MAKESTORIES METHOD
	
	

	public String getMainUrl() {
		return mu.getUrl().toString();
	}

	public String getFeedUrl() {
		return fu.getUrl().toString();
		
	}
	
	public String toString() {
		String str = "";
		str += "Main URL: " + getMainUrl() + "\n";
		str += "Feed URL: " + getFeedUrl() + "\n";
		return str;
	}
	
	public static String makeSite(String q) {
		try{
			Site s = new Site(q);
			return s.toString();
		} catch (FeedNotFoundException e) {
			return e.toString();
		}
		
	}
	
	class OverCountException extends Exception {
		private static final long serialVersionUID = 1L;
		String msg = "Count exceeded.";
		
		OverCountException() {}
		
		OverCountException(String u) {msg += " (" + u + ")";}
		
		public String toString() {return msg;}
	}
	
}	// END OF SITE CLASS

class SearchFailException extends Exception {
	private static final long serialVersionUID = 1L;
	String msg = "Unspecified error performing web search.";
	
	SearchFailException() {}
	
	SearchFailException(String u) {msg += " (" + u + ")";}
	
	public String toString() {return msg;}
}

//////////////////////////////////////////////////////////////////

class FeedNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;
	String msg = "Error finding feed.";
	
	FeedNotFoundException() {}
	
	FeedNotFoundException(String u) {msg += " (" + u + ")";}
	
	public String toString() {return msg;}
}
