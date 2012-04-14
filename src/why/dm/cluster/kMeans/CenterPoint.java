/**
 * Copyright (C) 2012 why
 */
package why.dm.cluster.kMeans;

import java.util.HashMap;

public class CenterPoint {
	// 包括序号和计数
	private HashMap<Integer, Double> newHits = null;
	// 所属分类
	private int classify = -1;

	private Double length = null;

	public CenterPoint() {

	}

	public CenterPoint(HashMap<Integer, Double> hits, int classify) {
		this.newHits = hits;
		this.classify = classify;
	}

	public CenterPoint(HashMap<Integer, Double> hits, int classify,
			Double length) {
		this.newHits = hits;
		this.classify = classify;
		this.length = length;
	}

	/**
	 * @return the length
	 */
	public Double getLength() {
		return length;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(Double length) {
		this.length = length;
	}

	/**
	 * @return the newHits
	 */
	public HashMap<Integer, Double> getNewHits() {
		return newHits;
	}

	/**
	 * @param newHits
	 *            the newHits to set
	 */
	public void setNewHits(HashMap<Integer, Double> newHits) {
		this.newHits = newHits;
	}

	/**
	 * @return the classify
	 */
	public int getClassify() {
		return classify;
	}

	/**
	 * @param classify
	 *            the classify to set
	 */
	public void setClassify(int classify) {
		this.classify = classify;
	}

}
