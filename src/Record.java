import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import twitter4j.Status;

import java.util.HashMap;
import java.util.Iterator;

public class Record extends Object {
	private String word;
	private Double densityCoefficient;
	private int timestamp;
	public HashMap<String,Integer> connections;	//Counts number of times word has shown up with the current record
											//Use wordCount as reference for indexes
											//Considered the partitions for a space (each word is a space)
	private boolean newrecord;				//Set to true if this record is new, false if it has been seen before
	
	public Record(String _word, int time){
		this.word = _word;
		this.timestamp = time;
		this.connections = new HashMap<String,Integer>();
		this.densityCoefficient = 1.0;
	}
	
	public void updateRecord(int curTimeStamp){
		calculateDensityCoefficient(curTimeStamp);
		updateTimestamp(curTimeStamp);
	}
	
	
	//Calculates the density coefficient for the current record
	//using its timestamp and a given value (lambda) as the decay factor
	// -----!-------
	//lambda = decay factor; 0 < lambda < 1
	public void calculateDensityCoefficient(int curTimeStamp){
		//Let lambda = 0.7 for arbitrary reasons
		double deltaTime = curTimeStamp - timestamp;
		this.densityCoefficient = Math.pow(0.7, deltaTime);
	}
	
	public Double getDensityCoefficient(){
		return this.densityCoefficient;
	}
	
	public String getWord(){
		return this.word;
	}
	
	public void setAsNew(boolean choice){
		this.newrecord = choice;
	}
	
	public boolean isNewRecord(){
		return this.newrecord;
	}
	
	public void updateTimestamp(int newTime){
		this.timestamp = newTime;
	}
	
	//Removing items requires an iterator
	//*******
	//Initializes the connection vector of a single record by making all counts = 0 except those with
	//a token as a key
	// listOfRecords - the word count vector/recordsList in DStreamTest
	// tokens - list of words that occurred with the current word
	public void initConnections(HashMap<String,Integer> listOfRecords){
		this.connections = listOfRecords;
	}
	
	//********
	//Places a new connection on an old record
	public void addConnection(Record e){
		this.connections.put(e.getWord(), 1);
	}
	
	//
	public void updateConnections(Record[] e){
		for (int i=0;i<e.length;i++){
			this.connections.put(e[i].getWord(), connections.get(e[i].getWord())+1);
		}
	}
	//Looks for a record with the given key within the connections hashmap
	//If it exists, return true
	public boolean findRecord(String key){
		return this.connections.containsKey(key);
	}
	
	public int getWordCount(String key){
		return this.connections.get(key);
	}
	
	@Override
	public boolean equals(Object other){
		if (other == this) return true;
		
		if (!(other instanceof Record)) return false;
		
		Record r = (Record) other;
		return r.word.equals(this.word);
	}
	
}
