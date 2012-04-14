/**
 * Copyright (C) 2012 why
 */
package why.dm.util;


/**
 * The integer double class
 * 
 * @author hector
 * @version $Rev$ $Date$
 */
public class IntegerDouble implements Comparable<IntegerDouble> {

	private double doubleValue;
	private int intValue;

	public IntegerDouble() {
		this(0, 0.);
	}

	public IntegerDouble(int intValue, double doubleValue) {
		this.intValue = intValue;
		this.doubleValue = doubleValue;
	}

	public int compareTo(IntegerDouble o) {
		// return doubleValue > o.doubleValue ? 1 : (doubleValue ==
		// o.doubleValue ? 0 : -1);
		return doubleValue <= o.doubleValue ? 1 : -1;
	}

	public double getDoubleValue() {
		return doubleValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setDoubleValue(double value) {
		doubleValue = value;
	}

	public void setIntValue(int value) {
		intValue = value;
	}
	
	public static void main(String[] args) {
		String aString;
		//if(aString!=null & aString.length())
			//System.out.println();
        //System.out.print(args[1]+args[2]+args[3]);
        
    }
}
