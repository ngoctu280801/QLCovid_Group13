package model;

import java.awt.event.KeyEvent;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Utils {
	public static String changeDateFormatter(String date, String format, SimpleDateFormat sdf){
		SimpleDateFormat f = new SimpleDateFormat(format);
		String res = null;
		try {
			res = f.format(sdf.parse(date));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	public static  boolean notSkipCheck(KeyEvent e){
		int code = e.getKeyCode();
		if(		code == KeyEvent.VK_LEFT || 
				code == KeyEvent.VK_UP || 
				code == KeyEvent.VK_DOWN || 
				code == KeyEvent.VK_RIGHT){
			return false;
		}
		return true;
	}
	
	public static String validateNum(StringBuilder s){
		for(int i = 0; i < s.length(); i++){
			if(!Character.isDigit(s.charAt(i))){
				s.deleteCharAt(i);
				i--;
			}
		}
		StringBuilder num = new StringBuilder(new BigInteger(s.toString()) + "");
		for(int i = num.length() - 3; i > 0; i-=3){
			num.insert(i, ',');
		}
		return num.toString();
	}
}
