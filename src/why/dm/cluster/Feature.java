/**
 * Copyright (C) 2012 why
 */
package why.dm.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Feature class
 * 
 * @author hector
 * @version $Rev$ $Date$
 */
public class Feature {
	// 各类中包含的文档。第一维是分类索引。内容同documents一致。
	private ArrayList<LinkedList<Document>> classifyDocuments = new ArrayList<>();
	// 各类中包含的term命中次数。第一维是分类索引。HashMap中Key是term的全局索引，Value是类中命中总和次数
	private ArrayList<HashMap<Integer, Integer>> classifyHits = new ArrayList<>();
	// 所有文档
	private LinkedList<Document> documents = new LinkedList<>();
	// 各类中的term总命中次数，也就是把所有一个类中的所有classifyHits加起来。第一维是分类索引
	private ArrayList<Integer> classifyTotalHits = new ArrayList<>();
	// <term序号, term在训练集中的命中次数>
	private HashMap<Integer, Integer> hits = new HashMap<>();
	// <term序号, term的文档频率df>
	private HashMap<Integer, Integer> dfs = new HashMap<>();
	// <term序号, term的逆文档频率idf>
	private HashMap<Integer, Double> idfs = new HashMap<>();
	// 所有term命中总次数之和
	private int totalHit = 0;

	public void clear() {
		classifyDocuments.clear();
		classifyHits.clear();
		documents.clear();
		classifyTotalHits.clear();
		hits.clear();
	}

	/**
	 * @return the classifyDocuments
	 */
	public ArrayList<LinkedList<Document>> getClassifyDocuments() {
		return classifyDocuments;
	}

	/**
	 * @return the classifyHits
	 */
	public ArrayList<HashMap<Integer, Integer>> getClassifyHits() {
		return classifyHits;
	}

	/**
	 * @return the classifyTotalHits
	 */
	public ArrayList<Integer> getClassifyTotalHits() {
		return classifyTotalHits;
	}

	/**
	 * @return the dfs
	 */
	public HashMap<Integer, Integer> getDfs() {
		return dfs;
	}

	/**
	 * @return the documents
	 */
	public LinkedList<Document> getDocuments() {
		return documents;
	}

	/**
	 * @return the hits
	 */
	public HashMap<Integer, Integer> getHits() {
		return hits;
	}

	/**
	 * @return the idfs
	 */
	public HashMap<Integer, Double> getIdfs() {
		return idfs;
	}

	/**
	 * @return the totalHit
	 */
	public int getTotalHit() {
		return totalHit;
	}

	/**
	 * @param classifyDocuments
	 *            the classifyDocuments to set
	 */
	public void setClassifyDocuments(
			ArrayList<LinkedList<Document>> classifyDocuments) {
		this.classifyDocuments = classifyDocuments;
	}

	/**
	 * @param classifyHits
	 *            the classifyHits to set
	 */
	public void setClassifyHits(
			ArrayList<HashMap<Integer, Integer>> classifyHits) {
		this.classifyHits = classifyHits;
	}

	/**
	 * @param classifyTotalHits
	 *            the classifyTotalHits to set
	 */
	public void setClassifyTotalHits(ArrayList<Integer> classifyTotalHits) {
		this.classifyTotalHits = classifyTotalHits;
	}

	/**
	 * @param dfs
	 *            the dfs to set
	 */
	public void setDfs(HashMap<Integer, Integer> dfs) {
		this.dfs = dfs;
	}

	/**
	 * @param documents
	 *            the documents to set
	 */
	public void setDocuments(LinkedList<Document> documents) {
		this.documents = documents;
	}

	/**
	 * @param hits
	 *            the hits to set
	 */
	public void setHits(HashMap<Integer, Integer> hits) {
		this.hits = hits;
	}

	/**
	 * @param idfs
	 *            the idfs to set
	 */
	public void setIdfs(HashMap<Integer, Double> idfs) {
		this.idfs = idfs;
	}

	/**
	 * @param totalHit
	 *            the totalHit to set
	 */
	public void setTotalHit(int totalHit) {
		this.totalHit = totalHit;
	}

}
