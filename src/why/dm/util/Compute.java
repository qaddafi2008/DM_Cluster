package why.dm.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import why.dm.cluster.Document;
import why.dm.cluster.kMeans.CenterPoint;

public class Compute {
	/**
	 * 将document对象转换为CenterPoint对象
	 * 
	 * @param doc
	 *            文档
	 * @param k
	 *            循环的值，作为中心点的类别
	 * @return 中心点
	 */
	public static CenterPoint transformIntoCP(Document doc, int k,
			boolean whetherUseIdfs, HashMap<Integer, Double> idfs) {
		HashMap<Integer, Integer> docHits = doc.getHits();
		Iterator<Integer> docKeyIterator = docHits.keySet().iterator();
		HashMap<Integer, Double> cpHits = new HashMap<Integer, Double>();
		// System.out.print("(");
		while (docKeyIterator.hasNext()) {
			Integer key = (Integer) docKeyIterator.next();
			int temp = docHits.get(key);
			if (whetherUseIdfs)
				cpHits.put(key, temp * idfs.get(key));
			else
				cpHits.put(key, temp + 0.0);
			// System.out.print(temp * idfs.get(key)+", ");
		}
		// System.out.println(")");
		System.out.println(k + ". " + doc.getPath());
		return new CenterPoint(cpHits, k);
	}

	/**
	 * 用各维度相减的差的平方和再开平方根作为文档的距离
	 * 
	 * @param cp1
	 *            中心点1
	 * @param cp2
	 *            中心点2
	 * @return 2个中心点的距离
	 */
	public static Double computeDistance(CenterPoint cp1, CenterPoint cp2) {

		HashMap<Integer, Double> hitsCP1 = cp1.getNewHits();
		HashMap<Integer, Double> hitsCP2 = cp2.getNewHits();
		HashMap<Integer, Double> differHits = new HashMap<Integer, Double>();// 维度差

		Double l;// temporarily store the value
		Double sum = 0.0;
		Iterator<Integer> hitsCP1Iterator = hitsCP1.keySet().iterator();

		while (hitsCP1Iterator.hasNext()) {// 遍历文档1，作为被减数
			Integer cp1Key = (Integer) hitsCP1Iterator.next();
			differHits.put(cp1Key, hitsCP1.get(cp1Key));
		}
		Iterator<Integer> hitsCP2Iterator = hitsCP2.keySet().iterator();
		while (hitsCP2Iterator.hasNext()) {// 遍历文档2，作为减数
			Integer cp2Key = (Integer) hitsCP2Iterator.next();
			if (differHits.containsKey(cp2Key))
				differHits.put(cp2Key,
						differHits.get(cp2Key) - hitsCP2.get(cp2Key));
			else
				differHits.put(cp2Key, hitsCP2.get(cp2Key));
		}
		Iterator<Integer> differHitsiIterator = differHits.keySet().iterator();
		while (differHitsiIterator.hasNext()) {// 取出所有的维度
			Integer key = (Integer) differHitsiIterator.next();
			l = differHits.get(key);
			sum += l * l;// 平方和
		}
		// 原来应该Math.sqrt(sum)，但为了提高计算速度，直接不开根号了
		return sum;
	}

	/**
	 * 计算文档与中心点的乘积
	 * 
	 * @param docHits
	 *            文档的维度集合
	 * @param cp
	 *            中心点
	 * @param whetherUseIdfs
	 *            是否使用idfs,true为是
	 * @param idfs
	 *            idfs的集合，如果不使用它，可以传入NULL
	 * @return 乘积
	 */
	public static Double computeProductWithCenterPoint(
			HashMap<Integer, Integer> docHits, CenterPoint cp,
			boolean whetherUseIdfs, HashMap<Integer, Double> idfs) {
		HashMap<Integer, Double> hitsd2 = cp.getNewHits();

		Integer j;// temporarily store the value
		Double k;
		Double sum = 0.0;
		Iterator<Integer> testDocHistIterator = docHits.keySet().iterator();
		while (testDocHistIterator.hasNext()) {
			Integer key = (Integer) testDocHistIterator.next();
			j = docHits.get(key);
			k = hitsd2.get(key);
			if (k != null) {
				if (whetherUseIdfs) {
					if (idfs.containsKey(key))
						sum += k * j * idfs.get(key);
					// else 如果不包含这个维度的IDFS，则置为0
					// sum += 0;
				} else
					sum += k * j;
			}
		}
		return sum;
	}

	/**
	 * 计算中心点：利用类中所有点的各维度和来直接取平均值
	 * 
	 * @param docList
	 *            一个类中的文档集
	 * @param isUnification
	 *            是否要对中心点进行归一化，true为是
	 * @param whetherUseIdfs
	 *            是否使用idfs,true为是
	 * @param idfs
	 *            idfs的集合，如果不使用它，可以传入NULL
	 * @return 计算得到的中心点
	 */
	public static CenterPoint computeCenterPointByAverage(
			LinkedList<Document> docList, boolean isUnification,
			boolean whetherUseIdfs, HashMap<Integer, Double> idfs) {

		// List<Double> dimenList = new ArrayList<Double>();
		HashMap<Integer, Double> cpHits = new HashMap<Integer, Double>();

		// 以下 统计总和
		Document doc = null;
		Iterator<Document> iterator = docList.iterator();
		while (iterator.hasNext()) {// 一个个取出文档
			doc = iterator.next();
			HashMap<Integer, Integer> docHits = doc.getHits();
			Iterator<Integer> keyiIterator = docHits.keySet().iterator();
			while (keyiIterator.hasNext()) {// 遍历当前文档的所有维度
				Integer key = (Integer) keyiIterator.next();
				// dimenList.set(key, dimenList.get(key) + docHits.get(key));
				if (cpHits.containsKey(key))
					cpHits.put(key, cpHits.get(key) + docHits.get(key));// 维度
																		// 和
				else
					cpHits.put(key, docHits.get(key) + 0.0);
			}
		}
		Double cpLength = 0.0;// 用于存储中心点长度，以便于归一中心点时使用
		// HashMap<Integer, Double> cpHits = new HashMap<Integer, Double>();
		int numOfDocs = docList.size();
		Iterator<Integer> cpKeyiIterator = cpHits.keySet().iterator();
		// System.out.println("(");
		while (cpKeyiIterator.hasNext()) {// 取平均值
			Integer cpkey = (Integer) cpKeyiIterator.next();
			Double tmp = cpHits.get(cpkey) / numOfDocs;
			if (whetherUseIdfs) {// 如果使用idfs，则执行下一句
				// System.out.println(cpkey + "---size of idfs: " +
				// idfs.size());
				tmp = tmp * idfs.get(cpkey);// 乘以相应的权重idfs
			}
			cpHits.put(cpkey, tmp);
			cpLength += tmp * tmp;
			// System.out.print(tmp+", ");
		}
		// System.out.println(")");
		cpLength = Math.sqrt(cpLength);
		if (isUnification)//
			cpHits = computeHitsByUnification(cpHits, cpLength);// 对中心点维度进行归一化
		int classify = -2;
		if (doc != null)
			classify = doc.getGuessClassify();// 取预测的类别作为新计算出来的中心点的类别
		return new CenterPoint(cpHits, classify, cpLength);
	}

	/**
	 * 对某个中心点的所有维度进行归一化
	 * 
	 * @param cpHits
	 *            维度集合
	 * @param cpLength
	 *            中心点的长度
	 */
	private static HashMap<Integer, Double> computeHitsByUnification(
			HashMap<Integer, Double> cpHits, Double cpLength) {
		Set<Integer> set = cpHits.keySet();
		Iterator<Integer> it = set.iterator();
		while (it.hasNext()) {
			Integer i = it.next();
			cpHits.put(i, cpHits.get(i) / cpLength);
		}
		return cpHits;
	}

	/**
	 * 利用乘积计算所有文档之间的相似度
	 * 
	 * @param allDocs
	 *            所有文档
	 * @param whetherUseIdfs
	 *            是否使用IDF，true为是
	 * @param idfs
	 *            dif的集合，如果不是使用可以传递NULL
	 * @return 所有文档的相似度矩阵
	 */
	public static Double[][] computeSimilarityBetweenAllDocuments(
			LinkedList<Document> allDocs, boolean whetherUseIdfs,
			HashMap<Integer, Double> idfs) {
		int size = allDocs.size();
		Double[][] similarity = new Double[size][];// 初始化第一维度
		Iterator<Document> allDocsIterator = allDocs.iterator();
		int i = 0;
		HashMap<Integer, Integer> docHits;
		Iterator<Document> docTempIterator;
		while (allDocsIterator.hasNext()) {
			Document doc = (Document) allDocsIterator.next();
			docHits = doc.getHits();
			similarity[i] = new Double[size];
			similarity[i][i] = 0.0;// 自己与自己的相似度记为0
			docTempIterator=allDocs.iterator();
			for(int k=0;k<=i;k++)//下移i+1个指针
				docTempIterator.next();
			int j = i + 1;
			while(docTempIterator.hasNext()) {
				similarity[i][j] = computeDistanceByProduct(docHits,
						docTempIterator.next(), whetherUseIdfs, idfs);
				j++;
			}
			if (i > 0) {// 补足矩阵前面的元素
				for (int k = 0; k < i; k++)
					similarity[i][k] = similarity[k][i];
			}
			i++;
			docTempIterator=null;
			System.gc();
			if(0==i%20)
				System.out.print(i+" ");
			if(0==i%400)
				System.out.println();
		}
		Double d1 = 0.0, d2 = 100.0;
		for (int j = 0; j < size; j++)
			for (int j2 = 0; j2 < size; j2++) {
				Double tmp = similarity[j][j2];
				if (tmp > d1)
					d1 = tmp;
				if (tmp < d2)
					d2 = tmp;
				if (j == 0)
					System.out.print(tmp + ", ");
			}
		System.out.println("\nmax: " + d1 + ". min: " + d2);
		return similarity;
	}

	/**
	 * 相乘方式计算2个文档距离
	 * 
	 * @param hitsd1
	 *            文档1 weiduji
	 * @param d2
	 *            文档2
	 * @param whetherUseIdfs
	 *            是否使用idfs,true为是
	 * @param idfs
	 *            idfs的集合，如果不使用它，可以传入NULL
	 * @return 2个文档的距离
	 */
	public static Double computeDistanceByProduct(
			HashMap<Integer, Integer> hitsd1, Document d2,
			boolean whetherUseIdfs, HashMap<Integer, Double> idfs) {

		HashMap<Integer, Integer> hitsd2 = d2.getHits();

		Integer j, k;// temporarily store the value
		Double d;
		Double sum = 0.0;
		Iterator<Integer> iterator = hitsd1.keySet().iterator();
		while (iterator.hasNext()) {
			Integer key = iterator.next();
			j = hitsd1.get(key);
			if (null != (k = hitsd2.get(key))) {
				if (whetherUseIdfs) {// 使用IDFS
					d = idfs.get(key);
					sum += k * j * d;
				} else
					sum += k * j;
			}
		}
		iterator = null;
		System.gc();
		// System.out.println("  "+res);
		return sum;
	}
	

	/*public static void main(String[] args) {
		LinkedList<Integer> llIntegers = new LinkedList<Integer>();
		llIntegers.add(9);
		llIntegers.add(7);
		llIntegers.add(8);
		llIntegers.add(6);
		Iterator<Integer> iterator = llIntegers.iterator();
		Iterator<Integer> tmpIterator;
		while (iterator.hasNext()) {
			System.out.println((Integer) iterator.next());
			tmpIterator = llIntegers.iterator();
			while (tmpIterator.hasNext()) {
				System.out.print(tmpIterator.next()+" ");
				
			}
			System.out.println();
		}
	}*/

}
