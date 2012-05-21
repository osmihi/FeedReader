package com.osmihi.feedReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity{
	
	TextView labelMain;
	Menu main_menu;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        labelMain = (TextView)findViewById(R.id.labelMain);
        
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        
	    	case R.id.searchFeed:
	        	Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
	            this.startActivity(searchIntent);
	            return true;
	            
	        case R.id.app_info:
	            //showHelp();
	        	//TODO make a print out of info option here
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
