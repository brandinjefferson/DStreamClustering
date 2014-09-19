/*
 * This class will describe a grid as an object
 * It contains all of the methods required to edit grid
 * values and obtain those same values
 * */
import java.util.ArrayList;


public class Grid implements Comparable<Grid> {

	//These variables function as the characteristic vector
	//and should be updated every time the grid is updated
	private double density;		//The density at the time of the last update
	private int tUpdated;		//Last time updated
	private int tRemoved;		//Last time removed from the grid list
	private GridType label;		//Current grid's type
	private int cluster;		//The cluster this grid belongs to - 0 means it's not in a cluster
	
	//The list of records within the given grid
	//Act like a pointer
	private ArrayList<Record> partitionSets;
	
	public enum GridType {
		DENSE, TRANSITIVE, SPARSE;
	}
	
	
	//Use this only when a Grid hasn't been used before
	public Grid(int timestamp, int dimensions,ArrayList<Record> list){
		tUpdated = timestamp;
		tRemoved = 0;
		for (Record e : list){
			density+=e.getDensityCoefficient();
		}
		partitionSets = list;
		calculateType(dimensions);
		cluster = 0;
	}
	
	//Should be updateCharacteristicVector
	public void updateCharVector(int timestamp,int dimensions){
		tUpdated = timestamp;
		calculateDensity(timestamp);
		calculateType(dimensions);
	}
	
	//Return 0 if equal
	@Override
	public int compareTo(Grid g) {
		if (partitionSets.size() != g.partitionSets.size()) return -1;
		for (int i=0;i<partitionSets.size();i++){
			String one = partitionSets.get(i).getWord();
			String two = g.partitionSets.get(i).getWord();
			if (!one.equalsIgnoreCase(two)) return -1;
		}
		return 0;
	}
	
	
	//This should only be 
	public void calculateDensity(int curtime){
		double tdelta = curtime - tUpdated;
		double curDensity = Math.pow(0.7, tdelta);
		this.density = (curDensity * this.density) + 1;
	}
	
	//Calculates the Grid Type for label
	//The two values given are arbitrary and can be changed
	//as the user sees fit
	//Only call this after the density has been updated
	//Assume N is the number of dimensions for now
	public void calculateType(int dimensions){
		double highDensityThreshold = 2.5/(dimensions * (1-0.7));
		double lowDensityThreshold = 0.6/(dimensions * (1-0.7));
		if (this.density > highDensityThreshold){
			this.label = GridType.DENSE;
		}
		else if (this.density < lowDensityThreshold){
			this.label = GridType.SPARSE;
		}
		else this.label = GridType.TRANSITIVE;
	}
	
	public GridType getLabel(){
		return this.label;
	}
	
	public void changeTimeRemoved(int curtime){
		this.tRemoved = curtime;
	}
	
	public void setCluster(int cluster){
		this.cluster = cluster;
	}
	
	public int getCluster(){
		return this.cluster;
	}

	/**
	 * Returns true if the current grid and g have at least one record that is the same.
	 * @param g A grid to compare to
	 * @return
	 */
	public boolean findPartitions(Grid g){
		for (Record e : g.partitionSets){
			for (Record t : partitionSets){
				if (e.equals(t)) return true;
			}
		}
		return false;
	}
	
}
