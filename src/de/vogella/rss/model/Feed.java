package de.vogella.rss.model;

import java.util.ArrayList;
import java.util.List;

import de.vogella.rss.FeedMessage;

public class Feed {
	final String title;
	final String link;
	final String description;
	final String language;
	final String copyright;
	final String pubDate;
	
	final List<FeedMessage> entries = new ArrayList<FeedMessage>();

	public Feed(String title, String link, String desc, String lang,
			String copy, String date){
		this.title = title;
		this.link = link;
		this.description = desc;
		this.language = lang;
		this.copyright = copy;
		this.pubDate = date;
	}
	
	public String getTitle() {
		return title;
	}

	public String getLink() {
		return link;
	}

	public String getDescription() {
		return description;
	}

	public String getLanguage() {
		return language;
	}

	public String getCopyright() {
		return copyright;
	}

	public String getPubDate() {
		return pubDate;
	}

	public List<FeedMessage> getMessages() {
		return entries;
	}
	
	public String toString() {
		return "Feed [copyright=" + copyright + ", description=" + description
		        + ", language=" + language + ", link=" + link + ", pubDate="
		        + pubDate + ", title=" + title + "]";
	}
	
}
