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
											//Use uniqueRecords as reference for indexes
	private boolean newrecord;				//Set to true if this record is new, false if it has been seen before
	
	public Record(String _word, int time){
		this.word = _word;
		this.timestamp = time;
		this.connections = new HashMap<String,Integer>();
		this.densityCoefficient = 1.0;
	}
	
	//Calculates the density coefficient for the current record
	//using its timestamp and a given value (lambda) as the decay factor
	// -----!-------
	//Call this only when the grid containing the record is updated
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
	// listOfRecords - the word count vector/uniqueRecords in DStreamTest
	// tokens - list of words that occurred with the current word
	public void initConnections(HashMap<String,Integer> listOfRecords, String [] tokens){
		this.connections = listOfRecords;
		for (Map.Entry<String, Integer> it : listOfRecords.entrySet()){
			this.connections.replace(it.getKey(), 0);
		}
		for (int i=0;i<tokens.length;i++){
			this.connections.replace(tokens[i],1);
		}
	}
	
	//********
	//Places a new connection on an old record
	
	public void addConnection(String key){
		this.connections.put(key, 1);
	}
	
	//Used for updating records that already exist but have just been encountered again
	// tokens - an array of the words that occurred with the record
	public void updateConnection(String [] tokens){
		for (int i=0;i<tokens.length;i++){
			if (findRecord(tokens[i])){
				int t = this.connections.get(tokens[i])+1;
				this.connections.replace(tokens[i],t);
			}
			else {
				this.connections.put(tokens[i], 1);
			}
		}
	}
	//Looks for a record with the given key within the connections hashmap
	//If it exists, return true
	public boolean findRecord(String key){
		return this.connections.containsKey(key);
	}
	@Override
	public boolean equals(Object other){
		if (other == this) return true;
		
		if (!(other instanceof Record)) return false;
		
		Record r = (Record) other;
		return r.word.equals(this.word);
	}
	
}
