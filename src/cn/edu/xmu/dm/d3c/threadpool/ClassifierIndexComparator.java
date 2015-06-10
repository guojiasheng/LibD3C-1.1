package cn.edu.xmu.dm.d3c.threadpool;


import java.util.Comparator;

public class ClassifierIndexComparator implements Comparator {

		
		public int compare(Object o1, Object o2) {
			//
			ClassifierIndex one = (ClassifierIndex)o1;
			//
			ClassifierIndex theother = (ClassifierIndex)o2;
			//
			if(one.getIndex() < theother.getIndex()){
				//
				return -1;
			}else if(one.getIndex() > theother.getIndex()){
				//
				return 1;
			}
			//
			return 0;
		}
	}
