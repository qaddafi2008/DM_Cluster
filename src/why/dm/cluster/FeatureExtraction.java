/**
 * Copyright (C) 2012 why
 */
package why.dm.cluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import why.dm.util.IntegerDouble;

/**
 * 遍历文件后提取词项和选择特征
 * 
 * @author qinhuiwang
 * @version $Rev$ $Date$
 */
public class FeatureExtraction {

	/******************* 以下为所有文档共有（包括训练文档和测试文档） ********************/

	// 所有文档集合
	private ArrayList<LinkedList<Document>> allDocuments = new ArrayList<LinkedList<Document>>();
	// Classify names. Also first-layer sub folder name.
	private ArrayList<String> classifyNames = new ArrayList<String>();
	// 所有term集合，包括名字和序号
	private HashMap<String, Integer> terms = new HashMap<String, Integer>();
	// Name string of all terms. Only for debug.
	private ArrayList<String> termIndices = new ArrayList<String>();
	// Index of termsIndices. Only for debug.
	private int currentTermIndex = 0;

	/******************* 以上为所有文档共有（包括训练文档和测试文档） ********************/

	/******************* 以下为测试文档相关 ********************/

	// 测试文档集合
	private LinkedList<Document> testDocuments = new LinkedList<Document>();
	// 测试文档比例（剩下的都是训练文档）
	private double testProportion = 0.01;

	/******************* 以上为测试文档相关 ********************/

	/******************* 以下为训练文档相关 ********************/

	// 训练文档特有特征（不包括测试文档）
	private Feature trainingFeature = new Feature();
	// IDF中的m系数
	public static final double IDF_M = 4.;

	/******************* 以上为训练文档相关 ********************/

	/******************* 以下为特征选择相关 ********************/

	// ECE选择的特征（即被选中的term）及其ECE值
	private TreeSet<IntegerDouble> eces = new TreeSet<IntegerDouble>();
	// 用特征选择算法所选择的特征
	private ArrayList<Integer> selectedFeatures = new ArrayList<Integer>();
	// 特征选择算法保留的特征数
	private int totalSelectedFeatures = 500;

	/******************* 以上为特征选择相关 ********************/

	/********************* 以下为调试相关 *********************/

	// 当前次数
	private int debugCount = 0;
	// 打开调试
	private boolean debugTrace = false;

	/********************* 以上为调试相关 *********************/

	public void clear() {
		allDocuments.clear();
		terms.clear();
		termIndices.clear();
		trainingFeature.clear();
		currentTermIndex = 0;
		testDocuments.clear();
		classifyNames.clear();
	}

	/**
	 * 词法分析、去除停用词、用Porter Stemming Algorithm进行词根还原
	 */
	private void doStem() {
		char[] w = new char[501];
		Stemmer s = new Stemmer();
		s.readStopword();
		for (int classify = 0; allDocuments.size() > classify; ++classify) {

			Iterator<Document> iterator = allDocuments.get(classify).iterator();
			while (iterator.hasNext()) {
				Document currentDocument = iterator.next();
				if (debugTrace) {
					System.out.print(currentDocument.getPath() + ": ");
				}
				try {
					FileInputStream in = new FileInputStream(
							currentDocument.getPath());

					try {
						while (true)

						{
							int ch = in.read();
							if (Character.isLetter((char) ch)) {
								int j = 0;
								while (true) {
									ch = Character.toLowerCase((char) ch);
									w[j] = (char) ch;
									if (j < 500)
										++j;
									ch = in.read();
									if (!Character.isLetter((char) ch)) {
										/* to test add(char ch) */
										for (int c = 0; c < j; c++)
											s.add(w[c]);

										/* or, to test add(char[] w, int j) */
										/* s.add(w, j); */

										if (s.isStopword()) {
											s.resetIndex();
										} else {
											s.stem();

											String u;

											/* and now, to test toString() : */
											u = s.toString();

											/*
											 * to test getResultBuffer(),
											 * getResultLength() :
											 */
											/*
											 * u = new
											 * String(s.getResultBuffer(), 0,
											 * s.getResultLength());
											 */

											if (debugTrace) {
												System.out.print(u);
											}

											// 加入全局索引
											Integer index = null;
											if (null == (index = terms.get(u))) {
												terms.put(u, currentTermIndex);
												index = currentTermIndex;
												termIndices.add(u);
												++currentTermIndex;
											}
											/*
											 * // 加入分类索引 HashMap<Integer,
											 * Integer> currentClassifyHits =
											 * classifyHits .get(currentDocument
											 * .getClassify()); if
											 * (currentClassifyHits
											 * .containsKey(index)) { Integer
											 * original = currentClassifyHits
											 * .get(index);
											 * currentClassifyHits.put(index,
											 * original + 1); } else {
											 * currentClassifyHits.put(index,
											 * 1); }
											 */
											// 加入文档索引
											currentDocument.insertTerm(index);

										}
										break;
									}
								}
							}
							if (ch < 0)
								break;
							// System.out.print((char) ch);
							if (debugTrace) {
								System.out.print(',');
							}
						}
					} catch (IOException e) {
						// System.out.println("error reading " +
						// currentDocument.getPath());
						e.printStackTrace();
						break;
					} finally {
						try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					// documents.push(currentDocument);
				} catch (FileNotFoundException e) {
					// System.out.println("file " + currentDocument.getPath() +
					// " not found");
					e.printStackTrace();
					break;
				}
				if (debugTrace) {
					System.out.println('.');
				} else {
					if (0 == debugCount % 100) {
						System.out.print(String.valueOf(debugCount) + " ");
					}
					if (0 == debugCount % 2000) {
						System.out.println();
					}
					++debugCount;
				}
			}

		}
	}

	/**
	 * ECE(Expected Cross Entropy)
	 */
	private double ece(int term) {

		// Calculate P(tk)
		double pt = 0.;
		int totalHit = trainingFeature.getTotalHit();
		int termHit = 0;
		if (trainingFeature.getHits().containsKey(term)) {
			termHit = trainingFeature.getHits().get(term);
		}
		if (0 == totalHit) {
			throw new ArithmeticException();
		}
		if (0 != termHit && 0 != totalHit) {
			pt = (double) termHit / (double) totalHit;
		}

		// Calculate sigma( P(ci | tk) log( P(ci | tk) / P(ci) ) )
		ArrayList<LinkedList<Document>> classifyDocuments = trainingFeature
				.getClassifyDocuments();
		double sigma = 0.;
		for (int classify = 0; trainingFeature.getClassifyHits().size() > classify; ++classify) {
			// Calculate P(ci | tk)
			double pct = 0.;
			if (trainingFeature.getClassifyHits().get(classify)
					.containsKey(term)) {
				pct = trainingFeature.getClassifyHits().get(classify).get(term);
				pct /= termHit;
			}

			// Calculate sigma
			if (0 != pct) {
				double pc = (double) classifyDocuments.get(classify).size()
						/ (double) trainingFeature.getDocuments().size();
				double log = pct / pc;
				sigma += pct * Math.log(log);
			}
		}

		// Return the result
		return pt * sigma;
	}

	/**
	 * @return the classifyNames
	 */
	public ArrayList<String> getClassifyNames() {
		return classifyNames;
	}

	/**
	 * @return the selectedFeatures
	 */
	public ArrayList<Integer> getSelectedFeatures() {
		return selectedFeatures;
	}

	/**
	 * @return the termIndices
	 */
	public ArrayList<String> getTermIndices() {
		return termIndices;
	}

	/**
	 * @return the terms
	 */
	public HashMap<String, Integer> getTerms() {
		return terms;
	}

	/**
	 * @return the testDocuments
	 */
	public LinkedList<Document> getTestDocuments() {
		return testDocuments;
	}

	/**
	 * @return the trainingFeature
	 */
	public Feature getTrainingFeature() {
		return trainingFeature;
	}

	/**
	 * 递归获取目录中的文件
	 * 
	 * @param dirPath
	 * @param classify
	 */
	private void readDocumentList(String dirPath, int classify) {
		File dirFile = new File(dirPath);
		File[] files = dirFile.listFiles();

		if (null == files)
			return;

		for (int i = 0; files.length > i; ++i) {
			if (files[i].isDirectory())
				readDocumentList(files[i].getAbsolutePath(), classify);
			else {
				String fileNameStr = files[i].getAbsolutePath();
				// System.out.println(fNameStr);
				Document document = new Document();
				document.setPath(fileNameStr);
				document.setClassify(classify);
				allDocuments.get(classify).add(document);
				// documents().add(document);
				// classifyDocuments.get(classify).add(document);
			}
		}
	}

	/**
	 * 读取指定目录下的文件
	 * 
	 * @param dirPath
	 */
	public void readFiles(String dirPath) {
		readTypesAndFilePaths(dirPath);
		doStem();
	}

	/**
	 * 根据目录的路径，得到其目录下所有文件路径+名称，以及第一次子目录的文件
	 * 
	 * @param dirPath
	 */
	private void readTypesAndFilePaths(String dirPath) {
		File dirFile = new File(dirPath);
		File[] files = dirFile.listFiles();

		if (files == null)
			return;
		// documents.clear();
		clear();
		for (int i = 0; i < files.length; ++i) {
			if (files[i].isDirectory()) {
				allDocuments.add(new LinkedList<Document>());
				readDocumentList(files[i].getAbsolutePath(), i);
				classifyNames.add(files[i].getName());
			}
		}
	}

	/**
	 * Feature selection
	 */
	public void selectFeature() {
		int currentTerm = 0;
		Iterator<String> iterator = terms.keySet().iterator();
		while (iterator.hasNext()) {
			Integer i = terms.get(iterator.next());
			eces.add(new IntegerDouble(i, ece(i)));
			// if (0 == currentTerm % 10000) {
			// System.out.print(currentTerm + " ");
			// }
			// if (0 == currentTerm % 200000) {
			// System.out.println();
			// }
			++currentTerm;
		}

		// Copy to selectedFeatures
		int copied = 0;
		Iterator<IntegerDouble> iterator2 = eces.iterator();
		while (iterator2.hasNext()) {
			if (totalSelectedFeatures < copied)
				break;
			selectedFeatures.add(iterator2.next().getIntValue());
			++copied;
		}
	}
	
	public void selectTestDocuments() {
		selectTestDocuments(0);
	}
	
	/**
	 * 选择测试集和训练集
	 * 
	 * @param testPart
	 *            现在选择第几份
	 *            -1表示全部都是训练集
	 */
	public void selectTestDocuments(int testPart) {
		// boolean testFull = false;
		testDocuments.clear();
		trainingFeature.clear();
		HashMap<Integer, Integer> dfs = trainingFeature.getDfs();
		HashMap<Integer, Double> idfs = trainingFeature.getIdfs();
		int totalHit = 0;
		for (int classify = 0; allDocuments.size() > classify; ++classify) {
			ArrayList<Integer> classifyTotalHits = trainingFeature
					.getClassifyTotalHits();
			ArrayList<HashMap<Integer, Integer>> classifyHits = trainingFeature
					.getClassifyHits();
			ArrayList<LinkedList<Document>> classifyDocuments = trainingFeature
					.getClassifyDocuments();
			LinkedList<Document> documents = trainingFeature.getDocuments();
			classifyTotalHits.add(0);
			classifyHits.add(new HashMap<Integer, Integer>());
			classifyDocuments.add(new LinkedList<Document>());
			int processed = 0;
			LinkedList<Document> currentClassifyDocuments = allDocuments
					.get(classify);
			Iterator<Document> documentIterator = currentClassifyDocuments
					.iterator();
			Document currentDocument;
			while (documentIterator.hasNext()) {
				currentDocument = documentIterator.next();
				int partSize = (int) (currentClassifyDocuments.size() * testProportion);
				int partBegin = partSize * testPart;
				// testPart == -1表示全部都是训练集
				if (0 < testPart && partBegin <= processed && partBegin + partSize > processed) {
					testDocuments.add(currentDocument);
				} else {
					classifyDocuments.get(classify).add(currentDocument);
					documents.add(currentDocument);

					/******************* 以下为统计训练集数据 ********************/

					// term在每个文档中的df只计算一次
					//boolean firstDf = true;
					HashSet<Integer> addedTerm = new HashSet<>();
					// 将项加入分类索引
					Iterator<Integer> hitIterator = currentDocument.getHits()
							.keySet().iterator();
					while (hitIterator.hasNext()) {
						Integer currentKey = hitIterator.next();
						Integer currentCount = currentDocument.getHits().get(
								currentKey);

						// Total hit
						totalHit += currentCount;

						// Hits
						HashMap<Integer, Integer> hits = trainingFeature
								.getHits();
						Integer originalHits = hits.get(currentKey);
						if (null == originalHits) {
							hits.put(currentKey, currentCount);
						} else {
							hits.put(currentKey, originalHits + currentCount);
						}

						// Classify hits
						HashMap<Integer, Integer> currentClassifyHits = classifyHits
								.get(classify);
						Integer originalClassifyHits = currentClassifyHits
								.get(currentKey);
						if (null == originalClassifyHits) {
							currentClassifyHits.put(currentKey, currentCount);
						} else {
							currentClassifyHits.put(currentKey,
									originalClassifyHits + currentCount);
						}

						// Classify total hits
						Integer originalClassifyTotalHits = classifyTotalHits
								.get(classify);
						classifyTotalHits.set(classify,
								originalClassifyTotalHits + currentCount);
						
						// DF
						if (!addedTerm.contains(currentKey)) {
							Integer value = dfs.get(currentKey);
							if (null != value) {
								dfs.put(currentKey, value + 1);
							} else {
								dfs.put(currentKey, 1);
							}
							addedTerm.add(currentKey);
							//firstDf = false;
						}
					}

					/******************* 以上为统计训练集数据 ********************/

				}
				++processed;

			}
		}
		//System.out.println("IDF:");
		int current = 0;
		Iterator<Integer> iterator = dfs.keySet().iterator();
		while (iterator.hasNext()) {
			Integer key = iterator.next();
			Integer df = dfs.get(key);
			double idf = Math.log(1 + IDF_M / (double) df);
			idfs.put(key, idf);
			//System.out.print("<" + key + ":" + idf + ">");
			//if (0 == current % 5) {
			//	System.out.println();
			//}
			++current;
		}
		System.out.println();
		trainingFeature.setTotalHit(totalHit);
	}

	/**
	 * @param selectedFeatures
	 *            the selectedFeatures to set
	 */
	public void setSelectedFeatures(ArrayList<Integer> selectedFeatures) {
		this.selectedFeatures = selectedFeatures;
	}

	/**
	 * @param args
	 */
	/*
	 * public static void main(String[] args) {
	 * getTypesAndFilePaths("bin/newgroups"); for (int i = 0; i <
	 * firstSubDirNames.size(); i++) {
	 * System.out.println(firstSubDirNames.get(i)); }
	 * System.out.println("Total files: "+filePaths.size()); }
	 */

	public void setTestProportion(double value) {
		testProportion = value;
	}

	public void traceDocument() {
		for (int classify = 0; allDocuments.size() > classify; ++classify) {
			LinkedList<Document> currentClassifyDocuments = allDocuments
					.get(classify);
			Iterator<Document> documentIterator = currentClassifyDocuments
					.iterator();
			while (documentIterator.hasNext()) {
				documentIterator.next().trace(classifyNames, termIndices);
			}
		}
	}

	public void traceEce() {
		int currentTerm = 0;
		Iterator<IntegerDouble> iterator = eces.iterator();
		while (iterator.hasNext() && 1000 > currentTerm) {
			IntegerDouble id = iterator.next();
			System.out.println("Ece " + currentTerm++ + ": "
					+ termIndices.get(id.getIntValue()) + "("
					+ id.getIntValue() + "), " + id.getDoubleValue() + ". ");
		}
	}

	public void traceTerm() {
		int currentTerm = 0;
		Iterator<String> iterator = terms.keySet().iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			if (500 > terms.get(s)) {// 0 == currentTerm % 50) {
				System.out.print(s + ": " + terms.get(s) + ". ");
			}
			if (0 == currentTerm % 10000) {
				System.out.println();
			}
			++currentTerm;
		}
	}
}
