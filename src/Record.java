import java.util.Collections;
import java.util.ArrayList;
import twitter4j.Status;

public class Record extends Object {
	private String word;
	private Double densityCoefficient;
	private int timestamp;
	public ArrayList<Integer> connections;	//Counts number of times word has shown up with the current record
											//Use uniqueRecords as reference for indexes
	private boolean newrecord;				//Set to true if this record is new, false if it has been seen before
	
	public Record(String _word, int time){
		this.word = _word;
		this.timestamp = time;
		this.connections = new ArrayList<Integer>();
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
	
	public void initConnections(int dimensions){
		for (int i=0;i<dimensions;i++){
			if (i==dimensions-1){
				this.connections.add(1);
			}
			else{
				this.connections.add(0);
			}
		}
	}
	
	public void addConnection(int index){
		/*for (Record e : this.connections){
			if (e.getWord() == rec.getWord()){
				System.out.println(e.getWord() + " is already connected to " + rec.getWord());
				return;
			}
		}
		this.connections.add(rec);*/
		int a = this.connections.get(index);
		a+=1;
		this.connections.remove(index);
		this.connections.add(index, a);
	}
	
	@Override
	public boolean equals(Object other){
		if (other == this) return true;
		
		if (!(other instanceof Record)) return false;
		
		Record r = (Record) other;
		return r.word.equals(this.word);
	}
	
}
