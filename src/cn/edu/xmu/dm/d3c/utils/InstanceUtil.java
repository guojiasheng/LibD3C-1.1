package cn.edu.xmu.dm.d3c.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.meta.LibD3C;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils.DataSource;

public class InstanceUtil {
	//
	public String jarName;
	//
	public String jarPath;

	public String pathPrefix = "Model";

	
	public static boolean convertToArff(String file) throws IOException{
		BufferedReader InputBR = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
		String InputLine = InputBR.readLine();
		
		if(InputLine.contains("@relation") || InputLine.contains("@Relation")){
			return false;
		}
		
			
		String[] dataString;
		HashSet<String> lable = new <String>HashSet();
		ArrayList<String> lableValue = new ArrayList<String>();
		ArrayList<ArrayList<String>> value = new ArrayList<ArrayList<String>>();
		ArrayList<String> temp = null;
		int attributes=0;
		boolean firstLine=true;
		while(InputLine != null)
		{
			dataString = InputLine.split(" ");
			if(firstLine){
				attributes=dataString.length-1;
				firstLine=false;
			}
			lable.add(dataString[0]);
			lableValue.add(dataString[0]);
			temp = new ArrayList<String>();
			for(int i=1;i<dataString.length;i++){
				String v = dataString[i].substring(2, dataString[i].length());
				temp.add(v);
			}
			value.add(temp);
			InputLine = InputBR.readLine();
		}
		InputBR.close();
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("temp.arff"), false), "utf-8"));
		bufferedWriter.write("@relation temp.arff"+"\n");
		int i,j=0;
		for(i=0;i<attributes;i++){
			bufferedWriter.write("@attribute "+i+"  numeric"+"\n");
		}
		bufferedWriter.write("@attribute class {");
		String classAttribute="";
		for(String la: lable){
			classAttribute+=(la+",");
		}
		classAttribute=classAttribute.substring(0, classAttribute.length()-1);
		bufferedWriter.write(classAttribute+ "}"+"\n");
		
		bufferedWriter.write("@data"+"\n");
		for(i=0;i<value.size();i++){
			ArrayList<String> tempValue = value.get(i);
			for(j=0;j<tempValue.size();j++){
				bufferedWriter.write(tempValue.get(j)+",");
			}
			bufferedWriter.write(lableValue.get(i)+"\n");
		}
		bufferedWriter.close();
		return true;
	}
	
	//
	/*
	 * 通过文件名的字符串获得实例
	 */
	public static Instances getInstances(String filename) throws Exception {
		File file = new File(filename);
		return getInstances(file);
	}

	/*
	 * 通过File类获得实例
	 */
	public static Instances getInstances(File file) throws Exception {
		Instances inst = null;
		try {
			ArffLoader loader = new ArffLoader();
			loader.setFile(file);
			inst = loader.getDataSet();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		return inst;
	}

	/*
	 * 打印出得到的实例
	 */
	public static void printInstances(Instances ins) {
		for (int i = 0; i < ins.numInstances(); i++) {
			System.out.println(ins.instance(i));
		}
	}

	/*
	 * 获得jar包路径
	 */
	public void getJarPath(Class clazz) throws Exception {
		//
		String path = clazz.getProtectionDomain().getCodeSource().getLocation()
				.getFile();
		//
		path = java.net.URLDecoder.decode(path, "UTF-8");
		//
		java.io.File jarFile = new java.io.File(path);
		//
		this.jarName = jarFile.getName();
		//
		java.io.File parent = jarFile.getParentFile();
		//
		if (parent != null) {
			//
			this.jarPath = parent.getAbsolutePath();
		}
	}

	/*
	 * 
	 */
	public void createClassifyResultFile(int num,
			List<List<Integer>> classifyRightOrWrong) {
		int i, j;
		try {
			//
			// URL url=this.getClass().getResource("/");
			// String path=url.getFile();
			//
			// System.out.println(path);
			//
			getJarPath(InstanceUtil.class);

			String pathPrefix = "";

			File dir = new File(pathPrefix);
			if (!dir.isDirectory()) {
				dir.mkdir();
			}

			String tempPath = pathPrefix + "ClassifyResult.arff";
			//
			System.out.println(tempPath);
			//
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempPath));
			// BufferedWriter writer = new BufferedWriter(new
			// FileWriter("D:/qc/javawork/LibD3C/lib/ClassifyResult.arff"));
			//
			String content = new String();
			content = "@relation ClassifyResult";
			writer.write(content);
			writer.newLine();
			for (i = 1; i <= num; i++) {
				content = new String();
				content = "@attribute	" + "A" + i + "	" + "{0,1}";
				writer.write(content);
				writer.newLine();
			}
			content = new String();
			content = "@data";
			writer.write(content);
			//
			for (i = 0; i < classifyRightOrWrong.size(); i++) {
				writer.newLine();
				content = new String();
				for (j = 0; j < classifyRightOrWrong.get(i).size(); j++) {
					//
					if (j == 0) {
						content = classifyRightOrWrong.get(i).get(j).toString();
					} else {
						content = content + ","
								+ classifyRightOrWrong.get(i).get(j);
					}
				}
				// 自己设置一个类别，不过这个类别对结果没什么影响
				writer.write(content);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 获取当前时间
	public static String getCurrentTime() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	// 获得时间间隔
	public static long timeCompare(String t1, String t2) {
		//
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Calendar c1=Calendar.getInstance();
		// Calendar c2=Calendar.getInstance();

		// c1.setTime(formatter.parse(t1));
		// c2.setTime(formatter.parse(t2));
		Date d1 = new Date();
		Date d2 = new Date();

		try {
			d1 = formatter.parse(t1);
			d2 = formatter.parse(t2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long result = (d2.getTime() - d1.getTime()) / 1000;

		return result;

	}

	//
	public double findTheMaxNo(double[] distributeForInstance) {
		//
		int i;
		//
		double No;
		double max;
		//
		List<Double> temp = new ArrayList<Double>();

		for (i = 0; i < distributeForInstance.length; i++) {
			//
			temp.add(distributeForInstance[i]);
		}
		//
		max = Collections.max(temp);
		//
		No = temp.indexOf(max);
		//
		return No;
	}

	/**
	 * 保存分类器
	 * 
	 * @param trainPath
	 *            训练文件
	 * @param savePath
	 *            保存路径
	 * @throws Exception
	 */
	public void SaveModel(LibD3C c, Instances data) throws Exception {
		c.buildClassifier(data);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				"train.model"));
		oos.writeObject(c);
		oos.flush();
		oos.close();
	}

	/**
	 * 载入并测试文件
	 * 
	 * @param modelPath
	 * @param testPath
	 * @return
	 * @throws Exception
	 */
	public void LoadModel(String modelPath, String testPath,
			String resultFilePath) throws Exception {
		try {
			LibD3C c1 = new LibD3C();
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					modelPath));
			c1 = (LibD3C) ois.readObject();
			ois.close();

			DataSource source = new DataSource(testPath);
			Instances data = source.getDataSet();
			data.setClassIndex(data.numAttributes() - 1);

			BufferedWriter writer = new BufferedWriter(new FileWriter(
					resultFilePath));
			
			BufferedWriter writePro = new BufferedWriter(new FileWriter(
					".probility"));
			writer.write("predcition	" + "origin classs");
			writer.newLine();
			for (int j = 0; j < data.numInstances(); j++) {
				writePro.write(String.valueOf(c1.distributionForInstance(data.get(j))[1]));
				writePro.newLine();
				writer.write(String.valueOf(c1.classifyInstance(data.get(j)))
						+ ",");
				writer.write(String.valueOf(data.get(j).classValue()));
				writer.newLine();
			}
			writer.flush();
			writer.close();
			writePro.flush();
			writePro.close();

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

}
