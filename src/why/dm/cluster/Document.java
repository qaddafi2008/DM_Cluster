/**
 * Copyright (C) 2012 why
 */
package why.dm.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

//import com.sun.org.apache.bcel.internal.generic.NEW;

import why.dm.util.DocumentDouble;

/**
 * The Document class
 * 
 * @author hector
 * @version $Rev$ $Date$
 */
public class Document {
	// 所属分类
	private int classify = -1;
	// 猜测的所属分类
	private int guessClassify = -1;
	// 相对于每个类别的相似度
	private ArrayList<Double> classifyValues = new ArrayList<>();

	// Only for debug.
	private LinkedList<Integer> hitIndices = new LinkedList<>();

	// <term序号, term在本文档中的命中次数>
	private HashMap<Integer, Integer> hits = new HashMap<>();
	// 文件路径
	private String path;
	// the distances between this doc with the rest docs 该文档与其他文档的距离，以及其他文档的指针
	private Set<DocumentDouble> documentDistances = null;
	// the length of doc
	private Double length = -1.0;
	// 文档与其所属类的相似度(加权相似度)
	private Double sim = -1.0;

	public void addClassifyValue(double value) {
		classifyValues.add(value);
	}

	/**
	 * @return the classify
	 */
	public int getClassify() {
		return classify;
	}

	/**
	 * @return the classifyValues
	 */
	public ArrayList<Double> getClassifyValues() {
		return classifyValues;
	}

	/**
	 * @return the docDIs
	 */
	public Set<DocumentDouble> getDocumentDistances() {
		return documentDistances;
	}

	/**
	 * @return the guessClassify
	 */
	public int getGuessClassify() {
		return guessClassify;
	}

	/**
	 * @return the hits
	 */
	public HashMap<Integer, Integer> getHits() {
		return hits;
	}

	/**
	 * @return the length
	 */
	public Double getLength() {
		return length;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the sim
	 */
	public Double getSim() {
		return sim;
	}

	public void insertTerm(Integer index) {
		hitIndices.push(index);
		if (hits.containsKey(index)) {
			Integer count = hits.remove(index);
			hits.put(index, ++count);
		} else {
			hits.put(index, 1);
		}
	}

	/**
	 * @param classify
	 *            the classify to set
	 */
	public void setClassify(int classify) {
		this.classify = classify;
	}

	/**
	 * @param docDIs
	 *            the docDIs to set
	 */
	public void setDocumentDistances(Set<DocumentDouble> docDIs) {
		this.documentDistances = docDIs;
	}

	/**
	 * @param guessClassify
	 *            the guessClassify to set
	 */
	public void setGuessClassify(int guessClassify) {
		this.guessClassify = guessClassify;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(Double length) {
		this.length = length;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @param sim
	 *            the sim to set
	 */
	public void setSim(Double sim) {
		this.sim = sim;
	}

	public void trace() {
		System.out.print("Class " + classify + ">> ");
		Iterator<Integer> iterator = hitIndices.descendingIterator();
		while (iterator.hasNext()) {
			Integer index = iterator.next();
			System.out.print(index + ": " + hits.get(index) + "; ");
		}
		System.out.println();
	}

	public void trace(ArrayList<String> classifyNames,
			ArrayList<String> termNames) {
		String str = classifyNames.get(classify);
		int lastIndex = path.lastIndexOf('\\');
		int lastIndex2 = path.lastIndexOf('/');
		if (lastIndex2 > lastIndex)
			lastIndex = lastIndex2;
		System.out.print(str + "(" + classify + ") "
				+ path.substring(lastIndex + 1) + ">> ");
		Iterator<Integer> iterator = hitIndices.descendingIterator();
		while (iterator.hasNext()) {
			Integer index = iterator.next();
			System.out.print(index + "(" + termNames.get(index) + "): "
					+ hits.get(index) + "; ");
		}
		System.out.println();
	}

}
