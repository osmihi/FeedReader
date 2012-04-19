package com.osmihi.feedReader;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FeedReaderActivity extends Activity {	
    TextView testAppText;
    Button testAppButton;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        testAppText = (TextView)findViewById(R.id.test_app_text);
        testAppButton = (Button)findViewById(R.id.test_app_button);
        
        testAppButton.setOnClickListener(new TestButtonListener());
    }
    
    public class TestButtonListener implements OnClickListener {
    	public void onClick(View v) {
        	testAppText.setText(testFeed());
        }	
    }
    
	public static String testFeed() {
		String output = "";
		
		String[] siteUrls = {
/*				"hacker news",
				"34590834059e0wgjri0wejge0ht5358th58ht2038ht08urhg0t883h40h8t08tt0h823t0h8h80",
				"geek news",
				"world news",
				"fox",
				"tech blog",
				"toys blog",
				"music",
				// ERROR 403 error from IMDB "the walking dead",
				// ERROR will usually time out "video games",
*/				
				// TODO remember to check for when there's no internet connection: UnkownHostException?
				// TODO Anything that finds an IMDB page gives an IOException!
				// TODO Must address the occasional SocketTimeoutException
				
				"http://www.slashdot.org/",
				//"http://www.wikipedia.org/",
				//"http://www.huffingtonpost.com/",
				"http://www.cnn.com/",
				"www.lifehacker.com",
				//"msnbc.com/",
				//"http://news.ycombinator.com",
		};
		
		ArrayList<Site> someSites = new ArrayList<Site>();
		
		for (int i = 0; i < siteUrls.length; i++) {
			try {
				someSites.add(new Site(siteUrls[i]));
				output += someSites.get(someSites.size() -1).toString() + "\n";
			} catch (FeedNotFoundException e) {
				output += "Feed not found.\n\n";
			}
		}
		
		output += "Done!\n";
		
		return output;
	}
}