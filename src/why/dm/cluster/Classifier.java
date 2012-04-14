/**
 * Copyright (C) 2012 why
 */
package why.dm.cluster;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

/**
 * The Classifier abstract class
 * 
 * @author hector
 * @version $Rev$ $Date$
 */
public abstract class Classifier {
	protected FeatureExtraction featureExtraction;
	protected boolean debugTrace = true;
	protected String debugFileName;
	private PrintStream originPs = System.out;

	// 结果矩阵,其中resultMatrix[1][3]，表示预测结果为类1，实际类别为类3的文档数
	// 二维数组各维的长度为DIMENSION+2,多出的2行2列分别用来存放每行每列的统计和以及所占比例
	// 如resultMatrix[0][DIMENSION]，表示第一行的总和，即预测结果为类0的文档总数
	// resultMatrix[0][DIMENSION+1],表示预测结果为类0的文档数占所有文档数的比例，省略了%
	// 如resultMatrix[DIMENSION][0]，表示第一列的总和，即实际类别为类0的文档总数
	// resultMatrix[DIMENSION+1][0],表示实际类别为类0的文档数占所有文档数的比例，省略了%
	private int DIMENSION = 20;
	private int[][] resultMatrix = null;
	private double[][] resultRatioMatrix = null;
	
	protected static final String OUTPUT_CHART_PATH = "output/chart/";
	protected static final String OUTPUT_DATA_PATH = "output/data/";
	private static final String ERROR_RESULT_MATRIX_NOT_INIT = "ERROR: You have to init resultMaxtix!";

	public abstract void clear();

	/**
	 * 累加统计结果矩阵每个元素的值
	 * 
	 * @param forecastClassify
	 *            预测出的类别序号
	 * @param realClassify
	 *            实际的类别序号
	 */
	public void accumulateResultMatrix(int forecastClassify, int realClassify) {
		if (resultMatrix == null) {
			System.err.println(ERROR_RESULT_MATRIX_NOT_INIT);
			return;
		}
		resultMatrix[forecastClassify][realClassify] += 1;
		resultMatrix[forecastClassify][DIMENSION] += 1; // 统计预测结果为类forecastClassify的文档总数
		resultMatrix[DIMENSION][realClassify] += 1; // 统计真实类别为类realClassify的文档总数
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

	public String createStackedBarChart(CategoryDataset dataset, String xName,
			String yName, String chartTitle, String charName) {
		// 1:得到 CategoryDataset

		// 2:JFreeChart对象
		JFreeChart chart = ChartFactory.createStackedBarChart(chartTitle, // 图表标题
				xName, // 目录轴的显示标签
				yName, // 数值轴的显示标签
				dataset, // 数据集
				PlotOrientation.VERTICAL, // 图表方向：水平、垂直
				true, // 是否显示图例(对于简单的柱状图必须是false)
				false, // 是否生成工具
				false // 是否生成URL链接
				);
		// 图例字体清晰
		chart.setTextAntiAlias(false);

		chart.setBackgroundPaint(Color.WHITE);

		// 2 ．2 主标题对象 主标题对象是 TextTitle 类型
		chart.setTitle(new TextTitle(chartTitle, new Font("黑体", Font.BOLD, 25)));
		// 2 ．2.1:设置中文
		// x,y轴坐标字体
		Font labelFont = new Font("SansSerif", Font.TRUETYPE_FONT, 12);

		// 2 ．3 Plot 对象 Plot 对象是图形的绘制结构对象
		CategoryPlot plot = chart.getCategoryPlot();

		// 设置横虚线可见
		plot.setRangeGridlinesVisible(true);
		// 虚线色彩
		plot.setRangeGridlinePaint(Color.gray);

		// 数据轴精度
		NumberAxis vn = (NumberAxis) plot.getRangeAxis();
		// 设置最大值是1
		vn.setUpperBound(1);
		// 设置数据轴坐标从0开始
		// vn.setAutoRangeIncludesZero(true);
		// 数据显示格式是百分比
		DecimalFormat df = new DecimalFormat("0.00%");
		vn.setNumberFormatOverride(df); // 数据轴数据标签的显示格式
		// DomainAxis （区域轴，相当于 x 轴）， RangeAxis （范围轴，相当于 y 轴）

		CategoryAxis domainAxis = plot.getDomainAxis();

		domainAxis.setLabelFont(labelFont); // 轴标题

		domainAxis.setTickLabelFont(labelFont); // 轴数值

		// x轴坐标太长，建议设置倾斜，如下两种方式选其一，两种效果相同
		// 倾斜（1）横轴上的 Lable 45度倾斜
		// domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		// 倾斜（2）Lable（Math.PI 3.0）度倾斜
		// domainAxis.setCategoryLabelPositions(CategoryLabelPositions
		// .createUpRotationLabelPositions(Math.PI / 3.0));

		domainAxis.setMaximumCategoryLabelWidthRatio(0.6f); // 横轴上的 Lable 是否完整显示

		plot.setDomainAxis(domainAxis);

		// y轴设置
		ValueAxis rangeAxis = plot.getRangeAxis();
		rangeAxis.setLabelFont(labelFont);
		rangeAxis.setTickLabelFont(labelFont);
		// 设置最高的一个 Item 与图片顶端的距离
		rangeAxis.setUpperMargin(0.15);
		// 设置最低的一个 Item 与图片底端的距离
		rangeAxis.setLowerMargin(0.15);
		plot.setRangeAxis(rangeAxis);

		// Renderer 对象是图形的绘制单元
		StackedBarRenderer renderer = new StackedBarRenderer();
		// 设置柱子宽度
		renderer.setMaximumBarWidth(0.05);
		// 设置柱子高度
		renderer.setMinimumBarLength(0.1);
		// 设置柱的边框颜色
		renderer.setBaseOutlinePaint(Color.BLACK);
		// 设置柱的边框可见
		renderer.setDrawBarOutline(true);

		// // 设置柱的颜色(可设定也可默认)
		renderer.setSeriesPaint(0, new Color(204, 255, 204));
		renderer.setSeriesPaint(1, new Color(255, 204, 153));

		// 设置每个地区所包含的平行柱的之间距离
		renderer.setItemMargin(0.4);

		plot.setRenderer(renderer);
		// 设置柱的透明度(如果是3D的必须设置才能达到立体效果，如果是2D的设置则使颜色变淡)
		// plot.setForegroundAlpha(0.65f);

		FileOutputStream fos_jpg = null;
		try {
			createPathIfNotExist(OUTPUT_CHART_PATH);
			String chartName = OUTPUT_CHART_PATH + charName;
			fos_jpg = new FileOutputStream(chartName);
			ChartUtilities.writeChartAsPNG(fos_jpg, chart, 1000, 600, true, 10);
			return chartName;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				fos_jpg.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 柱状图,折线图 数据集
	private CategoryDataset getBarData(double[][] data, String[] rowKeys,
			String[] columnKeys) {
		return DatasetUtilities
				.createCategoryDataset(rowKeys, columnKeys, data);
	}

	/**
	 * @return the debugFileName
	 */
	public String getDebugFileName() {
		return debugFileName;
	}

	/**
	 * @return the featureExtraction
	 */
	public FeatureExtraction getFeatureExtraction() {
		return featureExtraction;
	}

	/**
	 * 初始化结果矩阵
	 */
	public void initResultMatrix() {
		resultMatrix = new int[DIMENSION + 2][];
		for (int i = 0; i < DIMENSION + 2; i++) {
			resultMatrix[i] = new int[DIMENSION + 2];
			for (int j = 0; j < DIMENSION + 2; j++)
				resultMatrix[i][j] = 0;
		}
	}

	/**
	 * @return the debugTrace
	 */
	public boolean isDebugTrace() {
		return debugTrace;
	}

	/**
	 * 生成堆栈柱状图
	 */
	public void outputStackedBarChart(String fileName) {
		//double[][] data = new double[][] { { 0.21, 0.66, 0.23, 0.40, 0.26, 0.66, 0.23, 0.40, 0.26, 0.26, 0.66, 0.23, 0.40, 0.26, 0.26, 0.66, 0.23, 0.40, 0.26, 0.26 },
				//{ 0.25, 0.21, 0.10, 0.40, 0.16, 0.66, 0.23, 0.40, 0.26, 0.26, 0.66, 0.23, 0.40, 0.26, 0.26, 0.66, 0.23, 0.40, 0.26, 0.26 } };
		String[] rowKeys = { "0", "1", "2", "3", "4", "5", "6", "7",
				"8", "9", "10", "11", "12", "13", "14", "15", "16",
				"17", "18", "19" };
		String[] columnKeys = { "0", "1", "2", "3", "4", "5", "6", "7",
				"8", "9", "10", "11", "12", "13", "14", "15", "16",
				"17", "18", "19" };
		CategoryDataset dataset = getBarData(resultRatioMatrix, rowKeys, columnKeys);
		createStackedBarChart(dataset, "分类", "百分比", "统计", fileName + ".png");
	}

	/**
	 * 计算比例
	 */
	public void calculateResultMatrix() {
		// result matrix
		if (resultMatrix == null) {
			System.err.println(ERROR_RESULT_MATRIX_NOT_INIT);
			return;
		}
		for (int i = 0; i < DIMENSION; i++)
			// 统计总数
			resultMatrix[DIMENSION][DIMENSION] += resultMatrix[i][DIMENSION];
		for (int i = 0; i < DIMENSION; i++) {
			resultMatrix[i][DIMENSION + 1] = (int) Math.round(1000.0
					* resultMatrix[i][i] / resultMatrix[i][DIMENSION]);
			resultMatrix[DIMENSION + 1][i] = (int) Math.round(1000.0
					* resultMatrix[i][i] / resultMatrix[DIMENSION][i]);
		}
		int totalGuessRigth = 0;
		for (int i = 0; i < DIMENSION; i++)
			totalGuessRigth += resultMatrix[i][i];
		resultMatrix[DIMENSION][DIMENSION + 1] = (int) Math
				.round(totalGuessRigth * 1000
						/ resultMatrix[DIMENSION][DIMENSION]);
		// resultMatrix[DIMENSION + 1][DIMENSION] = 1000;
		// resultMatrix[DIMENSION + 1][DIMENSION + 1] = 1000;
		
		// result ratio matrix
		resultRatioMatrix = new double[DIMENSION][];
		for (int i = 0; i < DIMENSION; ++i) {
			resultRatioMatrix[i] = new double[DIMENSION];
			for (int j = 0; j < DIMENSION; ++j) {
				resultRatioMatrix[i][j] = (double) resultMatrix[i][j] / (double) resultMatrix[DIMENSION][j];
			}
		}
	}

	/**
	 * 输出结果矩阵
	 */
	public void outputResultMaxtrix() {
		if (resultMatrix == null) {
			System.err.println(ERROR_RESULT_MATRIX_NOT_INIT);
			return;
		}
		System.out.print("\t");
		for (int i = 0; i < DIMENSION; i++)
			// 打印矩阵第一行
			System.out.print("rc" + i + "\t");
		System.out.print("TOTAL\t正确率");
		System.out.println();
		for (int i = 0; i < DIMENSION + 2; i++) {
			if (i < DIMENSION)
				System.out.print("fc" + i + "\t");// 空2格
			else if (i == DIMENSION)
				System.out.print("TOTAL\t");
			else if (DIMENSION + 1 == i)
				System.out.print("召回率\t");

			for (int j = 0; j < DIMENSION + 2; j++) {
				int num = resultMatrix[i][j];
				if (DIMENSION + 1 == j || DIMENSION + 1 == i)
					System.out.print(num / 10.0 + "%\t");
				else {
					System.out.print(num + "\t");
				}

			}
			System.out.println();
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

	/**
	 * @param debugFileName
	 *            the debugFileName to set
	 */
	public void setDebugFileName(String debugFileName) {
		this.debugFileName = debugFileName;
	}

	/**
	 * @param debugTrace
	 *            the debugTrace to set
	 */
	public void setDebugTrace(boolean debugTrace) {
		this.debugTrace = debugTrace;
	}

	/**
	 * @param featureExtraction
	 *            the featureExtraction to set
	 */
	public void setFeatureExtraction(FeatureExtraction featureExtraction) {
		this.featureExtraction = featureExtraction;
	}

	public abstract void test();

	public abstract void trace();

	public abstract void train();

}
