package org.thor.habry.feedparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.thor.habry.dto.Message;
import org.thor.habry.feedprovider.ContentProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;


public class FeedXmlParser {
	// We don't use namespaces
	private static final String ns = null;

	public List<Message> parse( ContentProvider contentProvider ) throws XmlPullParserException, IOException {
		XmlPullParser parser = contentProvider.getContentParser();		
		try {		
			int eventType = parser.getEventType();
			List<Message> entries = new ArrayList<Message>();
		    while (eventType != XmlPullParser.END_DOCUMENT) {
		    	switch (eventType){
	            case XmlPullParser.START_TAG:
	                String name = parser.getName(); 
	                if (FeedTagsEnum.ITEM.toString().equalsIgnoreCase(name)) {
	                	Message message = readEntry(parser);
	                	message.setType(contentProvider.getMessageType());
	                	entries.add(message);
	                }
	                break;
		    	}
		    	eventType = parser.next();
		    }
		    return entries;
		} finally {
			contentProvider.flashResources();
		}
	}

	private List<Message> readFeed(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		List<Message> entries = new ArrayList<Message>();

		
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (FeedTagsEnum.ITEM.toString().equalsIgnoreCase(name)) {
				entries.add(readEntry(parser));
			} else {
				skip(parser);
			}
		}
		return entries;
	}

	// Parses the contents of an entry. If it encounters a title, summary, or
	// link tag, hands them off
	// to their respective "read" methods for processing. Otherwise, skips the
	// tag.
	private Message readEntry(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		//parser.require(XmlPullParser.START_TAG, ns, FeedTags.ITEM.toString());
		String title = null;
		String link = null;
		String description = null;
		String author = null;
		List<String> categories = new ArrayList<String>();
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (FeedTagsEnum.TITLE.toString().equalsIgnoreCase(name)) {
				title = readTitle(parser);
			} else if (FeedTagsEnum.DESCRIPTION.toString().equalsIgnoreCase(name)) {
				description = readDescription(parser);
			} else if (FeedTagsEnum.AUTHOR.toString().equalsIgnoreCase(name)) {
				author = readAuthor(parser);	
			} else if (FeedTagsEnum.CATEGORY.toString().equalsIgnoreCase(name)) {
				categories.add(readOneCategory(parser));
			} else if (FeedTagsEnum.LINK.toString().equalsIgnoreCase(name)) {
				link = readURL(parser);
			} else {			
				skip(parser);
			}
		}
		Message message = new Message(title, description, author);
		message.getCategories().addAll(categories);
		message.setLink(link);
		return message;
	}

	// Processes title tags in the feed.
	private String readTitle(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		return readTextValueOfTag(parser, FeedTagsEnum.TITLE);
	}
	
	// Processes title tags in the feed.
	private String readURL(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		return readTextValueOfTag(parser, FeedTagsEnum.LINK);
	}
	
	// Processes title tags in the feed.
	private String readOneCategory(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		return readTextValueOfTag(parser, FeedTagsEnum.CATEGORY);
	}

	// Processes title tags in the feed.
	private String readDescription(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		return readTextValueOfTag(parser, FeedTagsEnum.DESCRIPTION);
	}
	
	private String readAuthor(XmlPullParser parser) throws IOException,	XmlPullParserException {		
		return readTextValueOfTag(parser, FeedTagsEnum.AUTHOR);
	}
	
	private String readTextValueOfTag(XmlPullParser parser, FeedTagsEnum feedTag) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, feedTag.toString());
		String title = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, feedTag.toString());
		return title;
	}

	/*
	 * // Processes link tags in the feed. private String readLink(XmlPullParser
	 * parser) throws IOException, XmlPullParserException { String link = "";
	 * parser.require(XmlPullParser.START_TAG, ns, "link"); String tag =
	 * parser.getName(); String relType = parser.getAttributeValue(null, "rel");
	 * if (tag.equals("link")) { if (relType.equals("alternate")){ link =
	 * parser.getAttributeValue(null, "href"); parser.nextTag(); } }
	 * parser.require(XmlPullParser.END_TAG, ns, "link"); return link; }
	 * 
	 * // Processes summary tags in the feed. private String
	 * readSummary(XmlPullParser parser) throws IOException,
	 * XmlPullParserException { parser.require(XmlPullParser.START_TAG, ns,
	 * "summary"); String summary = readText(parser);
	 * parser.require(XmlPullParser.END_TAG, ns, "summary"); return summary; }
	 */

	// For the tags title and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		//if (parser.getEventType() != XmlPullParser.START_TAG) {
			//throw new IllegalStateException();
		//}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {			
			case XmlPullParser.END_TAG: {
				depth--;				
			}
				break;
			case XmlPullParser.START_TAG: {
				depth++; 
			}
				break;
			}
		}
	}
}
