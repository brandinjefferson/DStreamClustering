import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

public class DStreamTest {

	//Object[][] density_grid = new Object[8][8];
	public static int dimensions = 0; //The number of unique words found
	private static int timestamp; // t, the current time as an integer
	private static int gaptime = 100; // gap, displays the total 
	public static ArrayList<String> uniqueRecords; //I may have been using this incorrectly
												//Maybe this should be covered by the density grid
	public static Collection<Set<Set<Record>>> densitygrid; //Collection = S, First Set = Si/grids, Second Set = ji/partitions
	public static RedBlackTree gridlist;
	
	
	private static TwitterStream twitStream;
	private static String oauthconsumerkey = "XJzkH9T32B631ylqiuA";
	private static String oauthconsumersecret = "xpf1n34oCarn7sOYQXCPuMlAjKQtPtFaxeq2JnaxFg";
	private static String accesstoken = "712592654-zzA9hbH5RXAb555ee46KvUYtPVZS1hV4BPHsf5Sq";
	private static String accesstokensecret = "G0ZPtNF2DEpUgWHLEDMT8UYWGSs3KxDFlpdI0Uk";
	
	@Test
	public void test() {
		//fail("Not yet implemented");
		/*for (int i = 0;i<8;i++){
			for (int j=0;j<8;j++){
				density_grid[i][j] = new ArrayList<Record>();
			}
		}*/
		Scanner reader = new Scanner(System.in);
		System.out.print("Begin (0 = No, 1 = Yes): ");
		int choice = reader.nextInt();
		if (choice == 1){
			getTweets();
		}
		reader.close();
	}

	private void getTweets(){
		timestamp = 0;
		gridlist = new RedBlackTree();
		StatusListener listener = new StatusListener(){

			@Override
			public void onException(Exception arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStatus(Status status) {
				// TODO Auto-generated method stub
				//Think about putting these in different threads
				String[] tokens = tokenizeTweet(status);
				addDimensions(tokens);			//Add total dimensions from this tweet
				
				ArrayList<Record> currentRecords = convertDataRecords(tokens);
				updateConnections(currentRecords);
				
				
				timestamp+=1;
			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
				// TODO Auto-generated method stub
				
			}
			
		};
		twitStream = new TwitterStreamFactory().getInstance();
		twitStream.addListener(listener);
		twitStream.setOAuthConsumer(oauthconsumerkey, oauthconsumersecret);
		twitter4j.auth.AccessToken accessToken = new AccessToken(accesstoken, accesstokensecret);
		twitStream.setOAuthAccessToken(accessToken);
		twitStream.sample(); //Getting random tweets right now
	}
	
	private String[] tokenizeTweet(Status status){
		String text = status.getText();
		text = text.toLowerCase();
		text = text.replaceAll("[^\\p{L}\\p{N}]"," ");
		String[] temp = text.split(" ");
		return temp;
	}
	
	//Remember that if a record is already within the list, update its grid.
	private ArrayList<Record> convertDataRecords(String[] tokens){
		ArrayList<Record> tempArray = new ArrayList<Record>();
		for (String text : tokens){
			Record newRecord = new Record(text,timestamp);
			newRecord.initConnections(dimensions);
			if (!uniqueRecords.contains(text)){
				newRecord.setAsNew(true);
				uniqueRecords.add(text);
			}
			else{
				newRecord.setAsNew(false);
			}
			tempArray.add(newRecord);
		}
		return tempArray;
	}
	
	private void mapToGrid(Record rec){
		
	}
	
	//Should find which grid the record is in by comparing the words
	private void findInGrid(String rec){
		
	}
	
	private void addDimensions(String[] tokens){
		dimensions+=tokens.length;
	}
	
	//Should be in its own thread? This could take a while
	private void updateConnections(ArrayList<Record> tokens){
		int ct = 0;
		for (int i=0;i<tokens.size();i++){
			for (int j=0;j<uniqueRecords.size();j++){
				if (tokens.contains(new Record(uniqueRecords.get(j),timestamp))){
					Record temp = tokens.get(i);
					temp.addConnection(j);
					ct+=1;
				}
				if (ct==tokens.size()){
					ct=0;
					break;
				}
			}
		}
	}
	
}
