package why.dm.cluster.MBSAS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import why.dm.cluster.Cluster;
import why.dm.cluster.Document;
import why.dm.cluster.FeatureExtraction;
import why.dm.cluster.kMeans.CenterPoint;
import why.dm.util.Compute;
import why.dm.util.ComputeAllDocuments;

public class MBSAS extends Cluster {
	private Double THRESHOLD = 20.0;
	private int numOfPredictedCluster;
	
	public MBSAS(int numOfPredictedCluster, FeatureExtraction featureExtraction,Double THRESHOLD){
		this.numOfPredictedCluster = numOfPredictedCluster;
		this.featureExtraction = featureExtraction;
		this.THRESHOLD = THRESHOLD;
	}
	@Override
	public void clustering() {
		LinkedList<Document> allDocs = featureExtraction.getTrainingFeature().getDocuments();
		int sizeOfAllDocs = allDocs.size();
		LinkedList<CenterPoint> cps=new LinkedList<CenterPoint>();
		//1 决定聚类
		ArrayList<LinkedList<Document>> docsClusters=clusterDeterminationByDefault(0, allDocs, cps);
		//2 模式分类
		patternClassification(allDocs, cps, docsClusters);
		
		//3 计算结果矩阵，并计算熵
		redirectToNewOutput("test_" + debugFileName + ".txt");
		initResultMatrix(numOfPredictedCluster);
		accumulateResultMatrix(docsClusters);
		Double entropy = calculateEntropy(numOfPredictedCluster, sizeOfAllDocs);// 计算熵
		System.out.println("The entropy is: " + entropy);
		outputResultMaxtrix(numOfPredictedCluster);

		redirectToOldOutput();
		
	}

	/**
	 * 决定聚类：先聚出只含有一个元素的K个类
	 * @param randomIndex 随机取的第一个元素的索引号
	 * @param allDocs 所有的文档集合
	 * @param cps 由聚类计算得到的中心点集合
	 * @return 按类别排列的文档集合
	 */
	private ArrayList<LinkedList<Document>> clusterDeterminationByDefault(int randomIndex, LinkedList<Document> allDocs,LinkedList<CenterPoint> cps){
		ArrayList<LinkedList<Document>> docsClusters= new ArrayList<LinkedList<Document>>(numOfPredictedCluster);
		for(int k=0;k<numOfPredictedCluster;k++)
			docsClusters.add(new LinkedList<Document>());//初始化所有分类
		
		Document firstDocument=allDocs.get(randomIndex);
		cps.add(Compute.transformIntoCP(firstDocument, 0, false, null));//随机选择一个元素作为第一个聚类
		firstDocument.setGuessClassify(0);//设置预测的类为类0
		docsClusters.get(0).add(firstDocument);
		allDocs.remove(randomIndex);//从测试集合中删除已经选中的文档
		
		int i=1;
		Double minSim;//最小相似度
		Iterator<Document> allDocsIterator = allDocs.iterator();
		while (allDocsIterator.hasNext() && i<numOfPredictedCluster) {
			Document document = (Document) allDocsIterator.next();
			minSim=ComputeAllDocuments.findMinSimByCenterPoints(document, cps, true, featureExtraction.getTrainingFeature().getIdfs());//用idf计算
			if(minSim<THRESHOLD){
				cps.add(Compute.transformIntoCP(document, i, false, null));
				document.setGuessClassify(i);
				docsClusters.get(i).add(document);//新增类别
				allDocs.remove(document);//删除测试集中被选中的文档
				i++;
			}
		}
		if(i != numOfPredictedCluster){
			System.err.println("Only get "+i+" clusters, NOT ENOUGH!!");
			throw new ArithmeticException();
		}
		
		return docsClusters;
	}
	
	/**
	 * 模式分类:把其他文档归并到前面已经分好的K个只含有一个元素的聚类中
	 * @param allDocs 所有文档
	 * @param cps 所有中心点
	 * @param docsClusters 按类别排列的文档集合，计算结果将继续存储在此集合中
	 */
	private void patternClassification(LinkedList<Document> allDocs,LinkedList<CenterPoint> cps,ArrayList<LinkedList<Document>> docsClusters){
		Iterator<Document> allDocsIterator = allDocs.iterator();
		
		int guessClassify;
		while (allDocsIterator.hasNext()) {
			Document document = (Document) allDocsIterator.next();
			guessClassify = ComputeAllDocuments.findClassifyByCenterPoints(document, cps, true, featureExtraction.getTrainingFeature().getIdfs());
			//利用即将增加的文档，重新计算中心点
			LinkedList<Document> oneCluster = docsClusters.get(guessClassify);
			ComputeAllDocuments.transformOldCpToNewCp(cps.get(guessClassify), oneCluster.size(), document);
			document.setGuessClassify(guessClassify);//设置预测类别
			oneCluster.add(document);
			//allDocs.remove(document);//删除测试集中被选中的文档
		}
	}

}
