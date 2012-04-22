package com.osmihi.feedReader;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FeedReaderActivity extends Activity {	
    TextView testAppText;
    Button testAppButton;
    EditText siteAddName;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        testAppText = (TextView)findViewById(R.id.test_app_text);
        siteAddName = (EditText)findViewById(R.id.site_add_name);
        testAppButton = (Button)findViewById(R.id.test_app_button);

        testAppButton.setOnClickListener(new TestButtonListener());
    }
    
    public class TestButtonListener implements OnClickListener {
    	public void onClick(View v) {
    		String siteName = siteAddName.getText().toString();

    		testAppText.setText(testFeed(siteName));
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