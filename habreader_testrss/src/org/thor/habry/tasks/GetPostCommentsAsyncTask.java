package org.thor.habry.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.thor.habry.R;
import org.thor.habry.dto.Comment;
import org.thor.habry.dto.Message;
import org.thor.habry.messageparser.MessageParser;

import nu.xom.Document;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class GetPostCommentsAsyncTask extends AsyncTask<Message, Integer, Document> {
	
	ViewGroup mainLayout;
	Activity activity;
	Exception error;
	
	public GetPostCommentsAsyncTask(ViewGroup mainLayout, Activity activity) {
		this.mainLayout = mainLayout;
		this.activity = activity;
	}
	@Override
	protected void onPostExecute(Document result) {
		if (result != null) {									
			WebView webview = new WebView(activity);
			mainLayout.addView(webview);
			//activity.setContentView(webview);
			webview.getSettings().setJavaScriptEnabled(true);		
			webview.setWebViewClient(new WebViewClient() {
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
					Toast.makeText(activity, activity.getResources().getString(R.string.message_details_cannot_load_post) + " " +  description, Toast.LENGTH_SHORT).show();
				}
			});			
			webview.loadDataWithBaseURL("", result.toXML(), "text/html", "UTF-8", null);
			webview.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		}
	}
	
	@Override
	protected Document doInBackground(Message... feeds) {
		Message feedMessage = feeds[0];			
		Map<String, Object> documentParams = new HashMap<String, Object>(); 
		//documentParams.put("MAX_DISPLAY_WIDTH", Integer.valueOf(sizePoint.x));
		Document document = MessageParser.getInstance(documentParams).parseCommentsToDocument(feedMessage);		
		List<Comment> commentList = MessageParser.getInstance(documentParams).parseToComments(feedMessage);
		Log.d("habry", "comments!!!");
		if (commentList != null) {
			for (Comment comment : commentList) {
				Log.d("habry", "comment.getAuthor() = " + comment.getAuthor());
				Log.d("habry", "comment.getChild()= " + comment.getChildComments().size());
			}
		}
		return document;
	}	

}
