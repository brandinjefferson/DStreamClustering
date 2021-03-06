import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

public class DStreamTest {

	
	public static boolean activeStream = false;
	
	public static volatile int dimensions = 0; //The number of unique words found
	private static volatile int timestamp = 0; // t, the current time as an integer
	private static int timeplaceholder=0;
	private static volatile int gaptime = 100; // gap, displays the total 
	public static volatile HashMap<String, Integer> wordCount = new HashMap<>(); //A word count vector - keeps track of the # of times a word has appeared
	public static HashMap<String, Record> recordsList = new HashMap<>(); //Keeps one copy of each record for every grid to use in updates
	public static volatile Thread newthread;
	public static ArrayList<LinkedList<Grid>> clusterlist = new ArrayList<>();
	public static int totalgridct = 0;
	public static volatile boolean clusteringactive = false;
	public static ConcurrentLinkedQueue<String[]> buffer = new ConcurrentLinkedQueue<String[]>();
	public static Object timeObj = new Object();
	public static Object bufferObj = new Object();
	public static Object gridObj = new Object();
	public static Object clusterObj = new Object();
	public static Object wordCtObj = new Object();
	
	//First List = S, Second List = Si/grids, Third List = ji/partitions
	//public static LinkedList<LinkedList<LinkedList<Record>>> densitygrid = new LinkedList<LinkedList<LinkedList<Record>>>(); 
	public static volatile RedBlackTree gridlist;
	
	
	private static TwitterStream twitStream;
	private static String oauthconsumerkey = "XJzkH9T32B631ylqiuA";
	private static String oauthconsumersecret = "xpf1n34oCarn7sOYQXCPuMlAjKQtPtFaxeq2JnaxFg";
	private static String accesstoken = "712592654-zzA9hbH5RXAb555ee46KvUYtPVZS1hV4BPHsf5Sq";
	private static String accesstokensecret = "G0ZPtNF2DEpUgWHLEDMT8UYWGSs3KxDFlpdI0Uk";
	
	@Test
	public void test() {
		mainApp();
	}
	
	public void mainApp(){
		Scanner reader = new Scanner(System.in);
		System.out.print("Begin (0 = No, 1 = Yes): ");
		int choice = reader.nextInt();
		if (choice == 1){
			activeStream = true;
			getTweets();
			//Create new thread for online component
			//Create new thread for offline component
			newthread = new Thread(new Runnable(){
				public void run(){
					System.out.println("Made it to Clustering Thread.");
					while(true){
						if (timestamp==gaptime){
							clusteringactive=true;
							//System.out.println("Beginning initial clustering.");
							initialclustering();
						}
						else if (timestamp%gaptime == 0 && timestamp!=gaptime && timestamp > 0){
							clusteringactive=true;
							//Remove sporadic grids
							//System.out.println("Adjusting clusters");
							adjustclustering();
						}
						if (clusteringactive){ 
							clusteringactive=false;
							/*for (LinkedList<Grid> it : clusterlist){
								ListIterator<Grid> iterator = it.listIterator();
								while (iterator.hasNext()){
									Grid temp = iterator.next();
									System.out.print("Cluster " + temp.getCluster() + ": " + temp.printPartitions());
								}
							}*/
						}
					}
				}
			});
			newthread.setName("Offline Component");
			newthread.start();
		}
		
		new Thread("Eval"){
			@Override
			public void run(){
				while(true){
					evaltweets();
				}
			}
		}.start();
		
		while (choice==1){
			choice=reader.nextInt();
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
				String[] tokens = tokenizeTweet(status);
				synchronized (bufferObj){
					buffer.add(tokens);
				}
				synchronized (timeObj){
					//System.out.println(timestamp);
				}
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
	
	public void evaltweets(){
		boolean empty = false;
		synchronized(bufferObj){
			empty = buffer.isEmpty();
		}
		if (activeStream && !empty){
			String [] tokens;
			synchronized(bufferObj){
				tokens = buffer.poll(); 
			}
			addToListOfRecords(tokens);
			//Stops in one of these. Might be that they take too long, so
			//another thread would be needed
			Record[] currentRecords = convertDataRecords(tokens);
			mapping(currentRecords);
			synchronized (timeObj){
				if (clusteringactive) timeplaceholder++;
				else {
					if (timeplaceholder>0) {
						timestamp += timeplaceholder;
						timeplaceholder=0;
					}
					timestamp++;
					System.out.println(timestamp);
				}
			}
		}
	}
	
	private String[] tokenizeTweet(Status status){
		String text = status.getText();
		text = text.toLowerCase();
		text = text.replaceAll("[^\\p{L}\\p{N}]"," ");
		String[] temp = text.split(" ");
		return temp;
	}
	
	//Increase the count of the words or add a word to the word count vector accordingly
	private void addToListOfRecords(String[] tokens){
		for (String text : tokens){
			//If the word is already present, increment its count by 1
			synchronized(wordCtObj){
				if (wordCount.containsKey(text)){
					Integer t = wordCount.get(text)+1;
					wordCount.put(text, t);
					//System.out.println("Added word");
					//recordsList.get(text).updateRecord(timestamp);
				}
				//Otherwise add it with a count of 1
				else {
					wordCount.put(text, 1);
					Record temp = new Record(text,timestamp+timeplaceholder);
					recordsList.put(text, temp);
					//System.out.println("Added word");
				}
			}
		}
		addDimensions();
	}
	
	private Record[] convertDataRecords(String[] tokens){
		Record[] tempArray = new Record[tokens.length];
		for (int i=0;i<tokens.length;i++){
			Record temp = new Record(tokens[i],timestamp+timeplaceholder);
			temp.initConnections(wordCount);
			tempArray[i] = temp;
		}
		return tempArray;
	}
	
	private void addDimensions(){
		dimensions = wordCount.size();
		totalgridct = (int)Math.pow(2.0, (double)dimensions);	//# of grids possible = Powerset(S)
	}
	
	//Map a record to a grid
	public static void mapping(Record[] records){
		for (int i=0;i<records.length;i++){
			ArrayList<Record> array = new ArrayList<Record>();
			for (int j=0;j<records.length;j++){
				if (j!=i) array.add(records[j]);
			}
			Grid g = new Grid(timestamp+timeplaceholder, dimensions, array);
			synchronized (gridObj){
				if (!gridlist.find(g,timestamp+timeplaceholder,dimensions)){
					gridlist.add(g);
				}
			}
			//calculateGaptime();
		}
	}
	
	//This determines the gap time.
	// N is the total number of grids ever added
	// 4.0 is Cm, the same used to determine grid density
	// 0.6 is Cl, the same used to determine grid density
	public static void calculateGaptime(){
		double top = totalgridct - 4.0;
		double bot = totalgridct - 0.6;
		double fin = Math.max(bot/top, top/bot);	
		gaptime = (int)Math.floor(Math.log(fin) / Math.log(0.7));
	}
	
	public static void initialclustering(){
		try{
			//Update the density of all grids
			//Assign dense grids to clusters
			gridlist.initialInOrderVisit(timestamp,clusterlist,totalgridct);
			int m = gridlist.getCurrentCluster(),loopsWithoutChange=0;
			boolean changesPossible = true;
			
			synchronized (clusterObj){
				while (changesPossible){
					//foreach cluster c
					for (int i=0;i<m;i++){
						//foreach outside grid g of c
						ArrayList<Grid> outsiders = outsideGrids(clusterlist.get(i));
						for (int j=0;j<outsiders.size();j++){
							//foreach neighboring grid h of g
							ArrayList<Grid> neighbors = new ArrayList<Grid>();
							gridlist.neighborInOrderVisit(outsiders.get(j), neighbors);
							for (int u = 0;u<neighbors.size();u++){
								//if (g and h are strongly correlated) and (h is in cluster c*)
								Integer posdiff = determinePositionalDifference(outsiders.get(j), neighbors.get(u));
								double attraction1 = 0.0,attraction2=0.0;
								for (Record record : outsiders.get(j).getAllPartitions()){
									attraction1+=initialAttraction(record, neighbors.get(u), posdiff);
								}
								for (Record rec : neighbors.get(u).getAllPartitions()){
									attraction2+=initialAttraction(rec, outsiders.get(j), posdiff);
								}
								double val = (4.0/(totalgridct*totalgridct)*(1-0.7)); // |P| = |Powerset(S) X Powerset(S)|
								if (attraction1 > val && attraction2 > val){
									loopsWithoutChange = 0;
									if (clusterlist.get(outsiders.get(j).getCluster()).size() > 
										clusterlist.get(neighbors.get(u).getCluster()).size()){
										for (Grid grid : clusterlist.get(neighbors.get(u).getCluster())){
											grid.setCluster(outsiders.get(j).getCluster());
											//Leave original cluster there, just empty it
											clusterlist.get(outsiders.get(j).getCluster()).add(grid);
										}
									}
									else {
										//Next line is causing a ConcurrentModificationException
										for (Grid grid : clusterlist.get(outsiders.get(j).getCluster())){ 
											grid.setCluster(neighbors.get(u).getCluster());
											//Leave original cluster there, just empty it
											clusterlist.get(neighbors.get(u).getCluster()).add(grid);
										}
									}
								}
								else if (attraction1 > val && attraction2 > val 
										&& neighbors.get(u).getLabel()==Grid.GridType.TRANSITIVE){
									loopsWithoutChange = 0;
									neighbors.get(u).setCluster(outsiders.get(j).getCluster());
									clusterlist.get(outsiders.get(j).getCluster()).add(neighbors.get(u));
								}
								else {
									loopsWithoutChange++;
								}
							}
						}
					}
					if (loopsWithoutChange > 4) changesPossible= false;
				}
			}
			clusteringactive = false;
		} catch (Exception e){
			e.printStackTrace();
			System.out.println("Error in innerclustering.");
		}
		//newthread = null;		//Ends the thread once the clustering is finished.
	}
	
	private static ArrayList<Grid> outsideGrids(LinkedList<Grid> cluster){
		ArrayList<Grid> outsiders = new ArrayList<Grid>();
		ListIterator<Grid> it = cluster.listIterator();
		while (it.hasNext()){
			Grid agrid = it.next();
			if (agrid.getLabel() == Grid.GridType.TRANSITIVE){
				outsiders.add(it.next());
			}
			if (agrid.getLabel() == Grid.GridType.DENSE){
				//If it's dense, make sure it's not an inside grid
				// It's an inside grid if all neighbors are within the current cluster
				ArrayList<Grid> temp = new ArrayList<Grid>();
				gridlist.neighborInOrderVisit(agrid, temp); 
				//If the array isn't empty, at least one neighbor wasn't in the cluster
				if (temp.size()!=0) outsiders.add(agrid);
			}
		}
		
		return outsiders;
	}
	
	/**
	 * Finds the initial grid attraction. Note: Grid attraction is determined by figuring out 
	 * how attracted a grid's records are to another grid
	 * @param x - The grid to attain an attraction measurement for
	 * @param g - The neighboring grid being used in the comparison
	 * @return
	 */
	private static double initialAttraction(Record x,Grid g,Integer diff){
		double radius = 1.0,eps = 0.8,product = 1.0;
		int w = 1,center,curPos=0;
		synchronized(wordCtObj){
		
			for (Map.Entry<String, Integer> it : wordCount.entrySet()){		//Dimension count
				if (curPos==diff) w=-1;
				else w=1;
				center=0;
				
				for (int i=0;i<g.size();i++){								//Find the center
					if (g.getPartition(i).getWord().equals(it.getKey())){
						center = it.getValue();
					}
				}
				double val = Math.abs((x.getWordCount(it.getKey())-center));
				if (val < (radius - eps)){
					product = product * ((1+w)/2);
				}
				else{
					double delta = val/(2*eps),something = radius/eps;
					product = product * (0.5 + (w *(something-delta)));
				}
				curPos++;
			}
		}
		return product;
	}
	
	/**
	 * Determines where the two grids are different. There will be only one place.
	 * @param one - The primary grid (g)
	 * @param two - A neighboring grid of g (g*)
	 * @return
	 */
	private static Integer determinePositionalDifference(Grid one, Grid two){
		Integer index=0,j=0;
		synchronized (wordCtObj){
			for (Map.Entry<String, Integer> it : wordCount.entrySet()){
				for (int i=0;i<one.size();i++){
					if ((one.getPartition(i).findRecord(it.getKey()) && !two.getPartition(i).findRecord(it.getKey())) ||
							(!one.getPartition(i).findRecord(it.getKey()) && two.getPartition(i).findRecord(it.getKey())))
						index = j;
				}
				j++;
			}
		}
		return index;
	}
	
	public static void adjustclustering(){
		RedBlackTree gridlist1;
		synchronized (gridObj){
			gridlist1 = gridlist;
		}
		ArrayList<Grid> changes = new ArrayList<Grid>();
		gridlist1.adjustInOrderVisit(timestamp, changes, totalgridct);
		
		synchronized(clusterObj){
			for (Grid g : changes){
				if (g.getLabel() == Grid.GridType.SPARSE){
					clusterlist.get(g.getCluster()).remove(g);
					gridlist1.find(g, 0);
				}
				else if (g.getLabel() == Grid.GridType.DENSE){
					//find neighboring grid with the largest cluster
					ArrayList<Grid> neighbors = new ArrayList<Grid>();
					gridlist.neighborInOrderVisit(g, neighbors);
					Grid h = new Grid();
					int maxsize = 0;
					for (Grid temp : neighbors){
						int size = clusterlist.get(temp.getCluster()).size();
						if (size>maxsize){
							maxsize = size;
							h = temp;
						}
					}
					if (h.getLabel() == Grid.GridType.DENSE){
						if (g.getCluster() == 0){
							g.setCluster(h.getCluster());
							clusterlist.get(h.getCluster()).add(g);
						}
						else if (clusterlist.get(g.getCluster()).size()>maxsize){
							for (Grid wut : clusterlist.get(h.getCluster())){
								wut.setCluster(g.getCluster());
								
							}
						}
						else if (clusterlist.get(g.getCluster()).size()<=maxsize){
							for (Grid wut : clusterlist.get(g.getCluster())){
								wut.setCluster(h.getCluster());
								
							}
						}
					}
					else if (h.getLabel() == Grid.GridType.TRANSITIVE){
						int origcluster = g.getCluster();
						g.setCluster(h.getCluster());
						clusterlist.get(h.getCluster()).add(g);
						ArrayList<Grid> outsiders = outsideGrids(clusterlist.get(h.getCluster()));
						if (g.getCluster() == 0 && outsiders.contains(h)){
							clusterlist.get(origcluster).remove(g);
						}
						else if (g.getCluster() != 0 &&
								clusterlist.get(origcluster).size() >= clusterlist.get(h.getCluster()).size()){
							g.setCluster(origcluster);
							h.setCluster(origcluster);
							clusterlist.get(g.getCluster()).add(g);
							clusterlist.get(g.getCluster()).add(h);
							clusterlist.get(h.getCluster()).remove(g);
						}
					}
				}
				else if (g.getLabel() == Grid.GridType.TRANSITIVE){
					ArrayList<Grid> neighbors = new ArrayList<Grid>();
					gridlist1.neighborInOrderVisit(g, neighbors);
					int maxsize=0,largestcluster=0;
					for (Grid h : neighbors){
						if (h.getLabel() == Grid.GridType.DENSE){
							Integer posdiff = determinePositionalDifference(g, h);
							double val = (4.0/((totalgridct*totalgridct)*(1-0.7))); //Theta
							double attr1=0.0,attr2=0.0;
							for (Record x : g.getAllPartitions()){
								attr1+=initialAttraction(x, h, posdiff);
							}
							for (Record x : h.getAllPartitions()){
								attr2+=initialAttraction(x, g, posdiff);
							}
							if (attr1 > val && attr2 > val){
								if (clusterlist.get(h.getCluster()).size() > maxsize){
									maxsize = clusterlist.get(h.getCluster()).size();
									largestcluster = h.getCluster();
								}
							}
						}
					}
					clusterlist.get(g.getCluster()).remove(g);
					g.setCluster(largestcluster);
					clusterlist.get(largestcluster).add(g);
				}
			}
		}
		clusteringactive = false;
		//newthread = null;		//Ends the thread once the clustering is finished.
	}
	
}
