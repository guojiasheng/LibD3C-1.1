package cn.edu.xmu.dm.d3c.utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;

/**
 * desc:从配置文件中读取分类器的配置，并初始化分类器
 * <code>InitClassifiers</code>
 * @author chenwq (chenwq@stu.xmu.edu.cn)
 * @version 1.0 2012/04/10
 */
public class InitClassifiers {
	public static String[] classifiersName;
	public static ArrayList<String> classifiersOption=new ArrayList<String>();
	static public Classifier[] init(
			String filePath,
			List<String> nameOfClassifiers, 
			List<String> pathOfClassifiers,
			List<String> parameterOfCV) {
		Logger logger = Logger.getLogger(InitClassifiers.class);

		Classifier[] cfsArray = {new weka.classifiers.trees.J48()};
		try {
			File f = new File(filePath);
			SAXReader reader = new SAXReader();
			Document doc;

			doc = reader.read(f);

			Element root = doc.getRootElement();
			Element foo;
			List<Classifier> lst = new ArrayList<Classifier>();
			for (Iterator iter = root.elementIterator("classifier"); iter
					.hasNext();) {
				foo = (Element) iter.next();

				String classifierName = foo.attributeValue("name").trim();
				String classifierPath = foo.element("parameter")
						.elementText("class").trim();
				String option = foo.element("parameter").elementText("options")
						.trim();

				logger.info("classifierName:" + classifierName);
				logger.info("options:" + option);

				//System.out.println("classifierName:" + classifierName);
				//System.out.println("options:" + option);
				String[] options = weka.core.Utils.splitOptions(option);
				
				Classifier cfs = null;
				if(!classifierName.startsWith("IB") || classifierName.equals("IB1")){
					cfs = AbstractClassifier.forName(classifierPath, options);
					lst.add(cfs);
					
					nameOfClassifiers.add(classifierName);
					//
					pathOfClassifiers.add(classifierPath);
					
					parameterOfCV.add("");
					classifiersOption.add(option);
				}else if(classifierName.startsWith("IB")){
					IBk ibCfs = (weka.classifiers.lazy.IBk)Class.forName(classifierPath).newInstance();
					String other = foo.element("parameter").elementText("other")
							.trim();
					ibCfs.setKNN(Integer.parseInt(other));
					lst.add(ibCfs);
					nameOfClassifiers.add(classifierName);
					//
					pathOfClassifiers.add(classifierPath);
					parameterOfCV.add("");
					classifiersOption.add(option);
				}
				
				System.out.println(cfs);
				
			}

			cfsArray = lst.toArray(new Classifier[lst.size()]);
			classifiersName = nameOfClassifiers.toArray(new String[nameOfClassifiers.size()]);
			
			return cfsArray;
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static void main(String[] args) {
		//InitClassifiers.init("config/classifiers.xml");
	}
}
