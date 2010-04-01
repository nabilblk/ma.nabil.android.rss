package ma.nabil.android.rss;

import java.net.MalformedURLException;
import java.net.URL;



import ma.nabil.android.rss.parser.ChannelRefresh;
import ma.nabil.android.rss.provider.RSSReader;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ChannelAdd extends Activity {
    /** Called when the activity is first created. */
	
	private static final String TAG = "ChannelAdd";
	
	public EditText mURLText;
	protected ProgressDialog mBusy;	
	final Handler mHandler = new Handler();
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_add);
        mURLText=(EditText)findViewById(R.id.url);
        Button add=(Button)findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addChannel();
				
			}
		});
        
    }
	private static URL getDefaultFavicon(String rssurl)
	{
		try
		{
			URL orig = new URL(rssurl);

			URL iconUrl = new URL(orig.getProtocol(), orig.getHost(),
			  orig.getPort(), "/favicon.ico");
			
			return iconUrl;
		}
		catch (MalformedURLException e)
		{
			/* This shouldn't happen since we've already validated. */
			Log.d(TAG, Log.getStackTraceString(e));
			return null;
		}
	}
    /**
     * method to verify and add the channel  
     */
    private void addChannel()
	{
    final String rssurl=mURLText.getText().toString();
    
    mBusy=ProgressDialog.show(ChannelAdd.this,
	  "Téléchargement", "Accés au feed XML...", true, false);
    Thread t = new Thread()
	{
		public void run()
		{
			try
			{
				ChannelRefresh refresh = new ChannelRefresh(getContentResolver());

				final long id = refresh.syncDB(null, -1, rssurl);
				
				if (id >= 0)
				{
					URL iconurl = getDefaultFavicon(rssurl);
					refresh.updateFavicon(id, iconurl);
				}

		    	mHandler.post(new Runnable() {
		    		public void run()
		    		{
		    			mBusy.dismiss();
		    			
		    			Uri uri = ContentUris.withAppendedId(RSSReader.Channels.CONTENT_URI, id);
		    			//setResult(RESULT_OK, uri.toString());
		    			finish();
		    		}
		    	});
			}
			catch(Exception e)
			{
				final String errmsg = e.getMessage();
				final String errmsgFull = e.toString();

	    		mHandler.post(new Runnable() {
	    			public void run()
	    			{
	    				mBusy.dismiss();

	    				String errstr = ((errmsgFull != null) ? errmsgFull : errmsg);
	    				
	    				new AlertDialog.Builder(ChannelAdd.this)
	    				.setTitle("Feed error")
	    				.setMessage("An error was encountered while accessing the feed: " + errstr)
	    				.setIcon(R.drawable.star_big_on)
	    				.setNeutralButton("Ok",
	    				new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog,
	    				int which) {
	    				}
	    				}).show();

	    			}
	    		});
			}			    	
		}
	};

	t.start();		
}

}