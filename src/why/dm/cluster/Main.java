/**
 * Copyright (C) 2012 why
 */
package why.dm.cluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.jfree.data.category.DefaultCategoryDataset;

import why.dm.cluster.kMeans.KMeans;

/**
 * The Main class
 * 
 * @author hector
 * @version $Rev$ $Date$
 */
public final class Main {

	/**
	 * Entry function
	 * 
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Extract feacture
		FeatureExtraction featureExtraction = new FeatureExtraction();
		System.out.println("Read Files:");
		Date begin = new Date();System.out.println("begin: "+begin.toLocaleString());
		featureExtraction.readFiles("D:\\EProgramFiles\\DMClassify\\DM\\runtime\\newgroups");
		cluster(featureExtraction);
		System.out.println("the size of the testDocs: "+featureExtraction.getTestDocuments().size());
		Date end = new Date();System.out.println("end: "+end.toLocaleString());
		printTime(end.getTime() - begin.getTime());
		
		Date begin2 = new Date();
		System.out.println(begin2.toLocaleString());
		KMeans kMeans= new KMeans(10,featureExtraction);
		kMeans.setDebugFileName("kmeans");
		kMeans.clustering();
		Date end2 = new Date();
		System.out.println(end2.toLocaleString());
		printTime(end2.getTime() - begin2.getTime());
		printTime(end2.getTime() - begin.getTime());
		// classification(featureExtraction);
	}

	private static void cluster(FeatureExtraction featureExtraction) {
		//Date begin = new Date();

		//int totalTestPart = 1;
		//featureExtraction.setTestProportion(1. / totalTestPart);
		//featureExtraction.selectTestDocuments();
		featureExtraction.selectTestDocuments(-1); // -1表示全部都是训练集
		//Date end = new Date();
		//System.out.println(end.toLocaleString());
		// Show time difference
		//printTime(end.getTime() - begin.getTime());
		//System.out.print(featureExtraction.getTestDocuments().size());
		System.out.print("training docs: "+featureExtraction.getTrainingFeature().getDocuments()
				.size());
	}

	private static void classification(FeatureExtraction featureExtraction) {
		Date begin = new Date();
		int totalTestPart = 10;
		featureExtraction.setTestProportion(1. / totalTestPart);

		for (int testPart = 0; totalTestPart > testPart; ++testPart) {
			String testPartString = String.valueOf(testPart);
			System.out.println();
			System.out.println();
			System.out.println("Calculating round " + testPartString + "...");

			featureExtraction.selectTestDocuments(testPart);
			// System.out.println(featureExtraction.getTestDocuments().size());
			// System.out.println(featureExtraction.getTrainingFeature().getDocuments().size());
			// System.out.println(featureExtraction.getTestDocuments().get(0).getPath());
			// System.out.println();
			// System.out.println("Select Features:");
			featureExtraction.selectFeature();
			// System.out.println();
			// featureExtraction.traceTerm();
			System.out.println("the size of testDocs: "+featureExtraction.getTestDocuments().size());

			// Naive Bayes classification
			/*System.out.println("Naive Bayes...");
			nativeBayes.clear();
			nativeBayes.setDebugFileName("native_bayes_" + testPartString);
			nativeBayes.setFeatureExtraction(featureExtraction);
			nativeBayes.train();
			nativeBayes.test();*/

			// BP ANN classification System.out.println("BP ANN...");
			// bpAnn.clear();
			// bpAnn.setDebugFileName("bp_ann_" + testPartString);
			// bpAnn.setFeatureExtraction(featureExtraction);
			// bpAnn.train();
			// bpAnn.test();

			// System.out.println("All of the doc size"+featureExtraction.getTerms().size()+"; all of train doc size:"+featureExtraction.getTrainingFeature().getHits().size());
			// Center point cosine classification
		/*	System.out.println("Center point cosine...");
			centerPointCos.clear();
			centerPointCos.setDebugFileName("center_point_cos_"
					+ testPartString);
			centerPointCos.setFeatureExtraction(featureExtraction);
			centerPointCos.train();
			centerPointCos.test();*/

			// Center point product and unification classification
			/*System.out.println("Center point product and unification...");
			centerPointProductAndUnification.clear();
			centerPointProductAndUnification
					.setDebugFileName("center_point_product_and_unification_"
							+ testPartString);
			centerPointProductAndUnification
					.setFeatureExtraction(featureExtraction);
			centerPointProductAndUnification.train();
			centerPointProductAndUnification.test();*/

			// KNN classification
			/*System.out.println("KNN...");
			knn.clear();
			knn.setDebugFileName("knn_" + testPartString);
			knn.setFeatureExtraction(featureExtraction);
			knn.train();
			knn.test();*/

		}

		Date end = new Date();
		/*
		 * // Chart DefaultCategoryDataset dataset =
		 * toDataset(featureExtraction); // --chart-------------- JFreeChart
		 * jfreechart = ChartFactory.createBarChart( "Naive Bayes Chart",
		 * "Classification", "Value", dataset, PlotOrientation.VERTICAL, false,
		 * true, false); jfreechart.setBackgroundPaint(Color.white);
		 * CategoryPlot categoryplot = (CategoryPlot) jfreechart.getPlot();
		 * categoryplot.setBackgroundPaint(new Color(238, 238, 255));
		 * categoryplot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT); //
		 * --------------------------------------- try { // Save image
		 * java.util.Date d = new java.util.Date(); String time =
		 * String.valueOf(d.getTime()); ChartUtilities.saveChartAsPNG(new
		 * File("runtime/naivebayes" + time + ".png"), jfreechart, 2048, 768);
		 * // ChartUtilities.saveChartAsJPEG(new File("trace/BarChart.jpg"), //
		 * chart, 368, 278); } catch (Exception e) { e.printStackTrace(); //
		 * System.err.println("Problem occurred creating chart!" // +
		 * e.getMessage()); }
		 */
		// Show time spent
		printTime(end.getTime() - begin.getTime());
	}
	
	private static void printTime(long l) {
		System.out.println("long: " + l);
		long day = l / (24 * 60 * 60 * 1000);
		long hour = (l / (60 * 60 * 1000) - day * 24);
		long minute = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
		long second = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
		System.out.println();
		System.out.println("Used time: " + day + "d " + hour + "h " + minute
				+ "m " + second + "s.");
	}

	public static DefaultCategoryDataset toDataset(
			FeatureExtraction featureExtraction) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		int currentDocument = 0;
		Iterator<Document> iterator = featureExtraction.getTestDocuments()
				.iterator();
		while (iterator.hasNext()) {
			++currentDocument;
			Document document = iterator.next();
			ArrayList<Double> values = document.getClassifyValues();
			for (int index = 0; featureExtraction.getTrainingFeature()
					.getClassifyHits().size() > index; ++index) {
				dataset.addValue(-values.get(index), String.valueOf(index),
						currentDocument + "" + index);// String.valueOf(currentRound++));
			}
		}
		return dataset;
	}

}
