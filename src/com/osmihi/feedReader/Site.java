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
	private URL mainUrl;	// this will go away
	private URL feedUrl;	// this will go away
	
	private MainUrl mu;
	private FeedUrl fu;
	
	private ArrayList<Story> storyList = new ArrayList<Story>();
	SyndFeed feed = null;	// this seems fishy
	
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
			count = 0;
			maxCount = maxCountNum; // set maximum number of iterations
		}
		
		public void next() throws OverCountException {
			count++;
			if (count <= maxCount) {
				attempt();
			} else {throw new OverCountException();}
		}
		
		protected abstract void attempt();
		
		public URL getUrl() {return url;}
		public int getMaxCount() {return maxCount;}
	}
	
	private class MainUrl extends SiteUrl {
		// When created, an instance of this class must either have set its url to a valid Url, or throw an OverCountException.
		// its public next() method will find try the next search result (and next and next) until a valid url is given.
		// an instance of this class represents only a valid url that has the potential to host feeds. it doesn't validate anything feed-related!
		
		private String possibleUrl;
		private boolean urlOK = false;
		//private queue blekko results (stored as strings)
		
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
			if (count == 0) {
				// gather blekko results
			}
			// set possibleUrl to next blekko result
		}

	}
	
	private class FeedUrl extends SiteUrl {
		
		private URL urlToCheck;
		private boolean urlOK = false;
		private boolean feedOK = false;
		private URL possibleFeed;
		// private queue of possible feeds (stored as strings)
		
		public FeedUrl(URL inUrl) throws OverCountException {
			super(5); // init counter and set max number of feed links to try for a given site
			urlToCheck = inUrl;
			init();
			
			while (!feedOK) {
				next();
			}
		}
		
		protected void init() {
			// parse contents of urlToCheck and create queue of possible feeds 
		}
		
		private void tryUrl() throws MalformedURLException {
			// ensures that the next feed candidate is a valid URL by creating a new URL object from it
			// possibleFeed = new URL(queue[count]);
		}
		
		private boolean verifyFeed() {
			
			// return true or false depending on if possibleFeed has a valid feed at it.
			return false; // just placeholder atm so we don't get error warning f/ eclipse.
		}
		
		protected void attempt() {
			
			while (!urlOK) {
				try {
					tryUrl();
					urlOK = true;
				} catch (MalformedURLException e) {}
			}
			
			// ensure that the feed is indeed an actual feed.
			// feedOK = verifyFeed();
			
		}
	}
	
	class OverCountException extends Exception {
		private static final long serialVersionUID = 1L;
		String msg = "Count exceeded.";
		
		OverCountException() {}
		
		OverCountException(String u) {msg += " (" + u + ")";}
		
		public String toString() {return msg;}
	}
		
	// END INNER CLASSES


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
