/*
 * This class will describe a grid as an object
 * It contains all of the methods required to edit grid
 * values and obtain those same values
 * */
import java.util.ArrayList;

public class Grid {

	//These variables function as the characteristic vector
	//and should be updated every time the grid is updated
	private double density;		//The density at the time of the last update
	private int tUpdated;		//Last time updated
	private int tRemoved;		//Last time removed from the grid list
	private GridType label;		//Current grid's type
	
	//The list of records within the given grid
	private ArrayList<Record> recordlist;
	
	private enum GridType {
		DENSE, TRANSITIVE, SPARSE;
	}
	
	
	//Use this only when a Grid hasn't been used before
	public Grid(int timestamp, int dimensions){
		tUpdated = timestamp;
		tRemoved = 0;
		density = 0.0;
		calculateType(dimensions);
	}
	
	public Grid(int timestamp,int dimensions, Record rec){
		tUpdated = timestamp;
		tRemoved = 0;
		recordlist.add(rec);
		density = rec.getDensityCoefficient();
		calculateType(dimensions);
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
	
	public void changeTimeRemoved(int curtime){
		this.tRemoved = curtime;
	}
	
}
