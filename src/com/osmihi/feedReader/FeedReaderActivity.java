package com.osmihi.feedReader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FeedReaderActivity extends Activity {	
    TextView labelSearch;
    Button searchButton;
    Button clearButton;
    EditText siteAddName;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        labelSearch = (TextView)findViewById(R.id.labelSearch);
        siteAddName = (EditText)findViewById(R.id.site_add_name);
        searchButton = (Button)findViewById(R.id.search_button);
        clearButton = (Button)findViewById(R.id.clear_button);

        searchButton.setOnClickListener(new searchButtonListener());
        clearButton.setOnClickListener(new clearButtonListener());
    }
    
    public class searchButtonListener implements OnClickListener {
    	public void onClick(View v) {
    		String siteName = siteAddName.getText().toString();

    		siteAddName.setText(testFeed(siteName));
        }
    }
    public class clearButtonListener implements OnClickListener {
    	public void onClick(View v) {
    		siteAddName.setText("");
        }
    }

    
	public static String testFeed(String site) {
				// ERROR 403 error from IMDB "the walking dead",
				// ERROR will usually time out "video games",
				// TODO remember to check for when there's no internet connection: UnkownHostException?
				// TODO Anything that finds an IMDB page gives an IOException!
				// TODO Must address the occasional SocketTimeoutException
		return Site.makeSite(site);
	}
}