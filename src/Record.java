import java.util.Collections;
import java.util.ArrayList;
import twitter4j.Status;

public class Record {
	private String word;
	private Double densityCoefficient;
	private int timestamp;
	public ArrayList<Record> connections;
	
	public Record(String _word, int time){
		this.word = _word;
		this.timestamp = time;
		this.connections = new ArrayList<Record>();
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
	
	public void addConnection(Record rec){
		for (Record e : this.connections){
			if (e.getWord() == rec.getWord()){
				System.out.println(e.getWord() + " is already connected to " + rec.getWord());
				return;
			}
		}
		this.connections.add(rec);
		
	}
	
	
	
}
