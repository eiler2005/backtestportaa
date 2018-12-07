package ru.backtesting.utils;

//Java program to calculate 
//Simple Moving Average 
import java.util.*; 

public class SimpleMovingAverage { 
   
	// queue used to store list so that we get the average 
	private final Queue<Double> dataset = new LinkedList<Double>(); 
	private final int period; 
	private double sum; 

	// constructor to initialize period 
	public SimpleMovingAverage(int period) 
	{	 
		this.period = period; 
	} 

	// function to add new data in the 
	// list and update the sum so that 
	// we get the new mean 
	public void addData(double num) 
	{ 
		sum += num; 
		dataset.add(num); 

		// Updating size so that length 
		// of data set should be equal 
		// to period as a normal mean has 
		if (dataset.size() > period) 
		{ 
         sum -= dataset.remove(); 
		} 
	} 

	// function to calculate mean 
	public double getMean() 
	{ 
		return sum / period; 
	} 

	public void addValues(List<Double> values) {
	     for (Double x : values) 
	    	 addData(x.doubleValue());
	}
	
	public static void main(String[] args) 
	{ 
     double[] input_data = { 1, 3, 5, 6, 8, 
                             12, 18, 21, 22, 25 }; 
     int per = 3; 
     SimpleMovingAverage obj = new SimpleMovingAverage(per); 
     for (double x : input_data) { 
         obj.addData(x); 
         System.out.println("New number added is " + 
                             x + ", SMA = " + obj.getMean()); 
     } 
	} 
}
