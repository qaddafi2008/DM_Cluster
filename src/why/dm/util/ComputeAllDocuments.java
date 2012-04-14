package why.dm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import why.dm.cluster.Document;
import why.dm.cluster.kMeans.CenterPoint;

public class ComputeAllDocuments {
	/**
	 * 随机选择K个点作为中心点
	 * 
	 * @param k
	 * @param featureExtraction
	 * @return
	 */
	public static LinkedList<CenterPoint> selectCenterPoints(int k,
			LinkedList<Document> testDocs, boolean whetherUseIdfs,
			HashMap<Integer, Double> idfs) {
		if (0 == k) {
			System.err.println("The value of K in k-means can't be setted 0!");
			throw new ArithmeticException();
		}

		int dist = testDocs.size() / k;
		LinkedList<CenterPoint> cps = new LinkedList<CenterPoint>();
		int index = 0;
		for (int i = 0; i < k; i++) {
			cps.add(Compute.transformIntoCP(testDocs.get(index), i,
					whetherUseIdfs, idfs));
			index += dist;
		}
		return cps;
	}

	/**
	 * 利用计算出的中心点集合预测出测试文档属于哪个类(采用向量积计算相似度)
	 * 
	 * @param d
	 *            文档
	 * @param cps
	 *            中心点集合
	 * @return 所属类的序号/索引
	 */
	public static int findClassifyByCenterPoints(Document d,
			LinkedList<CenterPoint> cps, boolean whetherUseIdfs,
			HashMap<Integer, Double> idfs) {
		Double max = Double.NEGATIVE_INFINITY;
		// Double min = Double.MAX_VALUE;
		Double s;
		int classify = -2;
		HashMap<Integer, Integer> docHists = d.getHits();
		Iterator<CenterPoint> cpsIterator = cps.iterator();

		while (cpsIterator.hasNext()) {
			CenterPoint cp = (CenterPoint) cpsIterator.next();
			s = Compute.computeProductWithCenterPoint(docHists, cp,
					whetherUseIdfs, idfs);//
			// 前提是要对中心点进行归一化（computeCenterPointByAverage()中），计算比较准确
			if (s > max) {
				max = s;
				classify = cp.getClassify();
			}
		}
		return classify;

	}

	public static ArrayList<LinkedList<Document>> clusterAllDocuments(int k,
			LinkedList<Document> testDocs, LinkedList<CenterPoint> cps,
			boolean whetherUseIdfs, HashMap<Integer, Double> idfs) {
		ArrayList<LinkedList<Document>> allDocumentsByNewClassify = new ArrayList<LinkedList<Document>>();
		for (int i = 0; i < k; i++)
			allDocumentsByNewClassify.add(new LinkedList<Document>());
		Iterator<Document> testDocIterator = testDocs.iterator();
		int temp = 0;
		while (testDocIterator.hasNext()) {
			temp++;
			Document document = (Document) testDocIterator.next();
			int clusterClassify = findClassifyByCenterPoints(document, cps,
					whetherUseIdfs, idfs);// 计算所属类别
			document.setGuessClassify(clusterClassify);// 对预测类别赋值
			allDocumentsByNewClassify.get(clusterClassify).add(document);// 按类别进行添加归类
		}
		System.out.println("clusterAllDocuments :" + temp);
		return allDocumentsByNewClassify;
	}

	/**
	 * 利用直接取平均值的方法来计算所有的中心点
	 * 
	 * @param classifyDocs
	 *            按类分的文档集合
	 * @param isUnification
	 *            是否要对中心点进行归一化，true为是
	 * @param whetherUseIdfs
	 *            是否使用idfs,true为是
	 * @param idfs
	 *            idfs的集合，如果不使用它，可以传入NULL
	 * @return 中心点集合
	 */
	public static LinkedList<CenterPoint> computeAllCenterPointsByAverage(
			ArrayList<LinkedList<Document>> classifyDocs,
			boolean isUnification, boolean whetherUseIdfs,
			HashMap<Integer, Double> idfs) {
		LinkedList<CenterPoint> centerPoints = new LinkedList<CenterPoint>();
		CenterPoint cp;
		for (int i = 0; i < classifyDocs.size(); i++) {
			cp = Compute.computeCenterPointByAverage(classifyDocs.get(i),
					isUnification, whetherUseIdfs, idfs);
			centerPoints.add(cp);
		}
		return centerPoints;
	}

	public static Double computeDifferenceByDistance(
			LinkedList<CenterPoint> currentCenterPoints,
			LinkedList<CenterPoint> nextCenterPoints) {
		Double sum = 0.0;
		int sizeOfCurrentCPs = currentCenterPoints.size();
		if (nextCenterPoints.size() != sizeOfCurrentCPs) {
			System.err
					.println("Error: the size of nextCenterPoints is not equal to currentCenterPoints");
			return -1.0;
		}
		Iterator<CenterPoint> currentIterator = currentCenterPoints.iterator();
		Iterator<CenterPoint> nextIterator = nextCenterPoints.iterator();
		while (nextIterator.hasNext()) {
			CenterPoint nextCenterPoint = (CenterPoint) nextIterator.next();
			CenterPoint currentCenterPoint = (CenterPoint) currentIterator
					.next();
			sum += Compute.computeDistance(nextCenterPoint, currentCenterPoint);
		}
		System.out.println("sum :" + sum);
		return sum;
	}
	
	/**
	 * 利用计算出的中心点集合预算出测试文档与中心点的最小相似度（类似于距离中的最远距离）
	 * 
	 * @param d
	 *            文档
	 * @param cps
	 *            中心点集合
	 * @return 所属类的序号/索引
	 */
	public static Double findMinSimByCenterPoints(Document d,
			LinkedList<CenterPoint> cps, boolean whetherUseIdfs,
			HashMap<Integer, Double> idfs) {
		Double min = Double.MAX_VALUE;
		
		Double s;
		int classify = -2;
		HashMap<Integer, Integer> docHists = d.getHits();
		Iterator<CenterPoint> cpsIterator = cps.iterator();

		while (cpsIterator.hasNext()) {
			CenterPoint cp = (CenterPoint) cpsIterator.next();
			s = Compute.computeProductWithCenterPoint(docHists, cp,
					whetherUseIdfs, idfs);//
			// 前提是要对中心点进行归一化（computeCenterPointByAverage()中），计算比较准确
			if (s < min) {
				min = s;
				classify = cp.getClassify();//暂没用到
			}
		}
		return min;

	}
	
	/**
	 * 把旧的中心点加上新增的文档重新计算新的中心点
	 * @param cp 要继续重新计算的中心点
	 * @param sizeOfThisCluster 中心点所在聚类的大小（即，文档数）
	 * @param doc 新增加到中心点所在聚类的文档
	 */
	public static void transformOldCpToNewCp(CenterPoint cp,int sizeOfThisCluster,Document doc){
		HashMap<Integer, Double> cpHits = cp.getNewHits();
		HashMap<Integer, Integer> docHits= doc.getHits();
		HashMap<Integer, Double> newCpHits = new HashMap<Integer, Double>();
		int newSizeOfThisCluster = sizeOfThisCluster+1;//新增加一个元素
		
		Iterator<Integer> cpHitsIterator = cpHits.keySet().iterator();
		while (cpHitsIterator.hasNext()) {
			Integer cpKey = (Integer) cpHitsIterator.next();
			newCpHits.put(cpKey, cpHits.get(cpKey)*sizeOfThisCluster);
		}
		
		Iterator<Integer> docHitsIterator= docHits.keySet().iterator();
		while (docHitsIterator.hasNext()) {
			Integer docKey = (Integer) docHitsIterator.next();
			if(newCpHits.containsKey(docKey))
				newCpHits.put(docKey, (newCpHits.get(docKey)+docHits.get(docKey))/newSizeOfThisCluster);
			else 
				newCpHits.put(docKey, (docHits.get(docKey)+0.0)/newSizeOfThisCluster);
			
		}
		System.out.println("size of cp:"+cpHits.size()+"; doc: "+docHits.size());
		cp.setNewHits(newCpHits);
	}
}
