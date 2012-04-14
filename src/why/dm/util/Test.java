package why.dm.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Test {
	public static void main(String[] args) {
		/*ArrayList<Integer> list = new ArrayList<Integer>(5);//初始容量
		list.add(9);
		list.add(8);
		list.add(7);
		list.add(6);
		list.add(5);
		list.add(4);
		list.add(3);
		list.add(2);
		list.add(1);
		for(int i=0;i<list.size();i++)
			System.out.println(list.get(i));
		System.out.println("========================");*/
		/*int j=9;
		while(j>0){
			if(5==j){
				System.out.println("do not print five");
				j--;
				continue;
			}
			System.out.println(j);
			j--;
		}
		System.out.println(80000000.0<Double.MAX_VALUE);*/
		HashSet<String> set1= new HashSet<String>();
		set1.add("ab");
		set1.add("bc");
		HashSet<String> set2 =new HashSet<String>(set1);
		System.out.println(set2.remove("ab"));
		for (String string : set1) {
			System.out.println(string);
		}
		System.out.println("----------");
		for (String string : set2) {
			System.out.println(string);
		}
		
	}
}
