package why.dm.cluster;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class Cluster {
	protected FeatureExtraction featureExtraction;
	protected String debugFileName;
	protected static final String OUTPUT_CHART_PATH = "output/chart/";
	protected static final String OUTPUT_DATA_PATH = "output/data/";
	private PrintStream originPs = System.out;
	// 结果矩阵,其中resultMatrix[1][3]，表示预测结果为类1，实际类别为类3的文档数
	// 二维数组第一维的长度为聚出来的聚类数，第二维的长度为DIMENSION+1,
	// 如resultMatrix[i][DIMENSION]，表示第i行的总和，即预测结果为类i的文档总数
	
	private int DIMENSION = 20;
	private int[][] resultMatrix = null;
	private static final String ERROR_RESULT_MATRIX_NOT_INIT = "ERROR: You have to init resultMaxtix!";
	
	
	public abstract void clustering();
	/**
	 * @return the debugFileName
	 */
	public String getDebugFileName() {
		return debugFileName;
	}
	/**
	 * @param debugFileName the debugFileName to set
	 */
	public void setDebugFileName(String debugFileName) {
		this.debugFileName = debugFileName;
	}
	/**
	 * 初始化结果矩阵
	 * @param numOfPredictedCluster 预测的类别数
	 */
	public void initResultMatrix(int numOfPredictedCluster) {
		resultMatrix = new int[numOfPredictedCluster][];
		for (int i = 0; i < numOfPredictedCluster; i++) {
			resultMatrix[i] = new int[DIMENSION + 1];
			for (int j = 0; j < DIMENSION + 1; j++)
				resultMatrix[i][j] = 0;
		}
	}
	/**
	 * 累加统计结果矩阵每个元素的值
	 * 
	 * @param allDocumentsByNewClassify 所有分完类后的文档
	 */
	public void accumulateResultMatrix(ArrayList<LinkedList<Document>> allDocumentsByNewClassify) {
		if (resultMatrix == null) {
			System.err.println(ERROR_RESULT_MATRIX_NOT_INIT);
			return;
		}
		int realClassify;//guessClassify,
		for(int i=0;i<allDocumentsByNewClassify.size();i++){
			LinkedList<Document> docsOfOneClassify = allDocumentsByNewClassify.get(i);
			resultMatrix[i][DIMENSION]=docsOfOneClassify.size();
			Iterator<Document> docsOfOneClassifyIterator =docsOfOneClassify.iterator();
			while (docsOfOneClassifyIterator.hasNext()) {
				Document document = (Document) docsOfOneClassifyIterator.next();
				//guessClassify=document.getGuessClassify();
				realClassify=document.getClassify();
				resultMatrix[i][realClassify] += 1;//利用构造list时按类别顺序构造的优势，i即预测的类别
			}
			
			//System.out.println("~~~~~~i= "+i+"; guessclassify="+guessClassify);
		}	
	}
	

	/**
	 * 输出结果矩阵
	 */
	public void outputResultMaxtrix(int numOfPredictedCluster) {
		if (resultMatrix == null) {
			System.err.println(ERROR_RESULT_MATRIX_NOT_INIT);
			return;
		}
		System.out.print("\t");
		for (int i = 0; i < DIMENSION; i++)
			// 打印矩阵第一行
			System.out.print("rc" + i + "\t");
		System.out.print("Total");
		System.out.println();
		for (int i = 0; i < numOfPredictedCluster; i++) {
			System.out.print("fc" + i + "\t");

			for (int j = 0; j < DIMENSION + 1; j++) {
				int num = resultMatrix[i][j];
				System.out.print(num + "\t");
			}
			System.out.println();
		}
	}
	
	/**
	 *  利用结果矩阵计算熵
	 * @param numOfPredictedCluster 要聚成的聚类数，即预测的聚类数
	 * @param numOfAllDocuments 所有的文档数
	 * @return
	 */
	public Double calculateEntropy(int numOfPredictedCluster,int numOfAllDocuments){
		Double entropy =0.0;
		Double e_eachPredictedCluster;
		Double testDouble;
		for(int i=0;i<numOfPredictedCluster;i++){
			e_eachPredictedCluster=0.0;
			testDouble=0.0;
			int numOfOneClusterDOCS = resultMatrix[i][DIMENSION];
			for(int j=0;j<DIMENSION;j++){
				Double tempDouble;
				if(resultMatrix[i][j]!=0){
					tempDouble = (resultMatrix[i][j]+0.0)/numOfOneClusterDOCS;
					testDouble += tempDouble;
					e_eachPredictedCluster += tempDouble*Math.log(tempDouble);//计算每一项的和
				}
			}
			e_eachPredictedCluster = e_eachPredictedCluster*(-1);
			entropy += (numOfOneClusterDOCS+0.0)/numOfAllDocuments*e_eachPredictedCluster;
			System.out.println("testDouble: "+testDouble+"---e_eachPredictedCluster--"+e_eachPredictedCluster+"--entropy---"+entropy);
		}
		return entropy;
	}

	/**
	 * 判断文件夹是否存在，如果不存在则新建
	 * 
	 * @param chartPath
	 */
	private void createPathIfNotExist(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
			// log.info("CHART_PATH="+CHART_PATH+"create.");
		}
	}

	public void redirectToNewOutput(String fileName) {
		createPathIfNotExist(OUTPUT_DATA_PATH);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(OUTPUT_DATA_PATH + fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		PrintStream newPs = new PrintStream(bos, false);
		System.setOut(newPs);
	}

	public void redirectToOldOutput() {
		System.out.flush();
		System.setOut(originPs);
	}
}
