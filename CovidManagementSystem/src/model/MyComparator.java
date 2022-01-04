package model;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class MyComparator<T> implements Comparator<T> {

	@Override
	public int compare(T arg0, T arg1) {
		// TODO Auto-generated method stub
		Collator myCollator = Collator.getInstance(Locale.forLanguageTag("vi-VN"));
		return myCollator.compare(arg0.toString(), arg1.toString());
	}

}
