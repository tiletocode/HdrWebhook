package com.hdr.whatap.webhook;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
	
	public static String formatDate(long timestamp, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
	    return formatter.format(timestamp);
	}
	
	
	public static long toTimestamp(String date, String format) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		Date lDate = formatter.parse(date);
		return lDate.getTime();
		
	}
	
	public static String replace(String str, String regex, Object replacement) {
		String result = str.replaceAll(regex, replacement.toString());
		
		return result;	
	}
}
