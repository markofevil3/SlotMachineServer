package com.yna.game.common;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;

public class TimeManager {
	static Logger logger = Logger.getLogger(TimeManager.class);
			
	static long offset = 0;
	
	public static void Init() {
		Timestamp now = null;
//		Connection dbConnection = DBManager.Get();
//		
//		try {
//			PreparedStatement statement = dbConnection.prepareStatement("select NOW() as now");
//			ResultSet resultSet = statement.executeQuery();
//			
//			if (resultSet.first()) {
//				now = resultSet.getTimestamp("now");
//			}
//			
//			resultSet.close();
//			statement.close();
//		} catch (Exception exception) {
//			now = null;
//			logger.info("Init:Exception:" + exception.toString());
//		}
//		
//		if (now != null) {
//			Calendar calendar = Calendar.getInstance();
//			long localTime = calendar.getTimeInMillis();
//			calendar.setTime(now);
//			offset = calendar.getTimeInMillis() - localTime; 
//		}
//		
//		DBManager.Return(dbConnection);
	}
	
	public static Calendar GetDBCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(calendar.getTimeInMillis() + offset); 
		return calendar;
	}
	
	public static long GetTimeInMillis() {
		return GetDBCalendar().getTimeInMillis();
	}
	
	public static int GetDBCurrentHour() {
		return GetDBCalendar().get(Calendar.HOUR_OF_DAY);
	}
	
	public static Timestamp GetCurrentDateTime() {
		return new Timestamp(GetDBCalendar().getTime().getTime());
	}
	
	public static String GetServerTimeString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(GetDBCalendar().getTime());
	}
	
	public static String GetServerDateString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(GetDBCalendar().getTime());
	}
	
	public static String GetYesterdayDateStringWithinFiveMins() {
		Calendar calendar = GetDBCalendar();
		int min = calendar.get(Calendar.MINUTE);
		min = min - (min % 5);
		calendar.set(Calendar.MINUTE, min);
		calendar.add(Calendar.DATE, -1);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_");
		return dateFormat.format(calendar.getTime());
	}
	
	public static String GetDateStringWithinFiveMins() {
		Calendar calendar = GetDBCalendar();
		int min = calendar.get(Calendar.MINUTE);
		min = min - (min % 5);
		calendar.set(Calendar.MINUTE, min);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_");
		return dateFormat.format(calendar.getTime());
	}
	
	public static String GetDateStringWithinTenMins() {
		Calendar calendar = GetDBCalendar();
		int min = calendar.get(Calendar.MINUTE);
		min = min - (min % 5) - 10;
		calendar.set(Calendar.MINUTE, min);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_");
		return dateFormat.format(calendar.getTime());
	}
	
	public static String GetDateStringWithinFiveBeforeNow(int numMinsBefore) {
		Calendar calendar = GetDBCalendar();
		int min = calendar.get(Calendar.MINUTE);
		min = min - numMinsBefore;
		min = min - (min % 5);
		calendar.set(Calendar.MINUTE, min);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_");
		return dateFormat.format(calendar.getTime());
	}
	
	public static String GetYesterdayDateString() {
		Calendar calendar = GetDBCalendar();
		calendar.add(Calendar.DATE, -1);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_");
		return dateFormat.format(calendar.getTime());
	}
	
	public static String GetDateString() {
		Calendar calendar = GetDBCalendar();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_");
		return dateFormat.format(calendar.getTime());
	}
	
	public static int GetNumMinutesToNextDay() {
		Calendar calendar = GetDBCalendar();
		return (24 - calendar.get(Calendar.HOUR_OF_DAY) - 1) * 60 + 60 -  calendar.get(Calendar.MINUTE);
	}
	
	public static long GetPreviousMondayMillis() {
		Calendar calendar = GetDBCalendar();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTimeInMillis();
	}
	
	public static long GetPreviousHourMillis() {
		Calendar calendar = GetDBCalendar();
		calendar.add(Calendar.HOUR, -1);
		calendar.set(Calendar.MINUTE, 0);
		return calendar.getTimeInMillis();
	}
	
	public static long GetTimeMillisFromString(String timeString) {
		if (timeString == null || timeString.equals("") || timeString.equals("null")) {
			return 0;
		}
		
		Calendar calendar = GetDBCalendar();
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		try {
			calendar.setTime(format.parse(timeString));
		} catch (ParseException exception) {
			logger.info("GetTimeMillisFromString:Exception:" + exception.toString());
		}
		
		return calendar.getTimeInMillis();
	}
	
	public static String TimeStringFromMillis(long millis) {
		if (millis == 0) {
			return "null";
		}
		
		String timeString = "";
		Calendar calendar = GetDBCalendar();
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		try {
			calendar.setTimeInMillis(millis);
			timeString = format.format(calendar.getTime());
		} catch (Exception exception) {
			logger.info("TimeStringFromMillis:Exception:" + exception.toString());
		}
		
		return timeString;
	}
}