package model;

import java.util.Comparator;

public class DateComparator<T> implements Comparator<T> {
	@Override
	public int compare(T o1, T o2) {
		// o1, o2 in dd/MM/yyyy format or dd/MM/yyyy hh:MM:ss format
		String date1 = changeFormat(o1.toString());
		String date2 = changeFormat(o2.toString());
		return date1.compareTo(date2);
		
	}

	private String changeFormat(String date){
		String d1 = date.substring(6, 10) + date.substring(3, 5) + date.substring(0, 2); 
		if(date.length() > 10){
			d1 += date.substring(10);
		}
		return d1;
	}
	
}
