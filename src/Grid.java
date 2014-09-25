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
	private GridType label;		//Current grid's type
	protected boolean labelchanged; //Set to true if the label was changed from its previous version
	private int cluster;		//The cluster this grid belongs to - 0 means it's not in a cluster
	
	//The list of records within the given grid
	//Act like a pointer
	private ArrayList<Record> partitionSets;
	
	public enum GridType {
		DENSE, TRANSITIVE, SPARSE;
	}
	
	public Grid(){
		
	}
	
	//Use this only when a Grid hasn't been used before
	public Grid(int timestamp, int dimensions,ArrayList<Record> list){
		tUpdated = timestamp;
		for (Record e : list){
			density+=e.getDensityCoefficient();
		}
		partitionSets = list;
		calculateType(dimensions);
		cluster = 0;
		label = GridType.DENSE;
		labelchanged = false;
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
		boolean found = false;
		if (partitionSets.size() != g.partitionSets.size()) return -1;
		for (int i=0;i<partitionSets.size();i++){
			for (Record t : g.partitionSets){
				if (t.equals(partitionSets.get(i))) {
					found = true;
					break;
				}
				if (!found) return -1;
			}
		}
		return 0;
	}
	
	@Override
	public boolean equals(Object other){
		if (other == null || this.getClass() != other.getClass()) return false;
		boolean found = false;
		Grid g = (Grid) other;
		if (partitionSets.size() != g.partitionSets.size()) return false;
		for (int i=0;i<partitionSets.size();i++){
			for (Record t : g.partitionSets){
				if (t.equals(partitionSets.get(i))) {
					found = true;
					break;
				}
				if (!found) return false;
			}
		}
		return true;
	}
	
	public int getTimeUpdated(){
		return this.tUpdated;
	}
	
	public double getDensity(){
		return this.density;
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
	//N is the total number of grids that can possibly exist
	public void calculateType(int gridct){
		double highDensityThreshold = 4/(gridct * (1-0.7));
		double lowDensityThreshold = 0.6/(gridct * (1-0.7));
		if (this.density > highDensityThreshold){
			if (this.label != Grid.GridType.DENSE && DStreamTest.clusteringactive) 
				this.labelchanged=true;
			this.label = GridType.DENSE;
		}
		else if (this.density < lowDensityThreshold){
			if (this.label != Grid.GridType.SPARSE && DStreamTest.clusteringactive) 
				this.labelchanged=true;
			this.label = GridType.SPARSE;
		}
		else{
			if (this.label != Grid.GridType.TRANSITIVE && DStreamTest.clusteringactive) 
				this.labelchanged=true;
			this.label = GridType.TRANSITIVE;
		}
	}
	
	public GridType getLabel(){
		return this.label;
	}
	
	public void resetLabelWatcher(){
		this.labelchanged = false;
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
	
	public Record getPartition(int index){
		return partitionSets.get(index);
	}
	
	public int size(){
		return partitionSets.size();
	}
	
	public ArrayList<Record> getAllPartitions(){
		return partitionSets;
	}
	
}
