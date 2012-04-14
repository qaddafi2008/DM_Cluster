package why.dm.cluster.kMeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import why.dm.cluster.Cluster;
import why.dm.cluster.Document;
import why.dm.cluster.FeatureExtraction;
import why.dm.util.Compute;
import why.dm.util.ComputeAllDocuments;

public class KMeans extends Cluster {
	private ArrayList<LinkedList<Document>> allDocumentsByNewClassify;
	private int k = 0;// K-means中K的值，即最终聚为几个类
	private final static Double THRESHOLD = 0.3;// 阀值

	public KMeans(int k, FeatureExtraction featureExtraction) {
		this.k = k;
		this.featureExtraction = featureExtraction;
	}

	public void clustering() {

		Double distanceBetweenCurrentAndNextCps = 50.0;
		int loopTimes = 0;// 循环次数
		LinkedList<Document> testDocs = featureExtraction.getTrainingFeature()
				.getDocuments();// 前面为训练集的形式，后面为测试集的形式（不包括idf）：featureExtraction.getTestDocuments();
		HashMap<Integer, Double> idfsHashMap = featureExtraction
				.getTrainingFeature().getIdfs();
		//Compute.computeSimilarityBetweenAllDocuments(testDocs, true,
				//idfsHashMap);

		LinkedList<CenterPoint> currentCenterPoints = ComputeAllDocuments
				.selectCenterPoints(k, testDocs, false, null);
		LinkedList<CenterPoint> nextCenterPoints;
		while (distanceBetweenCurrentAndNextCps > THRESHOLD && loopTimes < 20) {
			allDocumentsByNewClassify = ComputeAllDocuments
					.clusterAllDocuments(k, testDocs, currentCenterPoints,
							true, idfsHashMap);// 对文档进行聚类
			nextCenterPoints = ComputeAllDocuments
					.computeAllCenterPointsByAverage(allDocumentsByNewClassify,
							false, false, null);// 计算新的中心点
			distanceBetweenCurrentAndNextCps = ComputeAllDocuments
					.computeDifferenceByDistance(currentCenterPoints,
							nextCenterPoints);// 计算2次中心点的差值
			currentCenterPoints = nextCenterPoints;
			loopTimes++;
			System.out.println("compute times: " + loopTimes);
		}
		redirectToNewOutput("test_" + debugFileName + ".txt");
		initResultMatrix(k);
		accumulateResultMatrix(allDocumentsByNewClassify);
		Double entropy = calculateEntropy(k, testDocs.size());// 计算熵
		System.out.println("The entropy is: " + entropy);
		outputResultMaxtrix(k);

		redirectToOldOutput();
	}

}
