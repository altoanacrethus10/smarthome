package com.example.smarthome.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {
    private static final String DATE_FORMAT = "dd MMM yyyy";
    private static final String DATE_TIME_FORMAT = "dd MMM yyyy, HH:mm";
    private static final String TIME_FORMAT = "HH:mm";
    private static final String API_DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * Format date to readable string
     */
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * Format date and time
     */
    public static String formatDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * Format time
     */
    public static String formatTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * Format date for API
     */
    public static String formatDateForApi(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * Get current date string
     */
    public static String getCurrentDate() {
        return formatDate(new Date());
    }
    
    /**
     * Get current date and time string
     */
    public static String getCurrentDateTime() {
        return formatDateTime(new Date());
    }
    
    /**
     * Generate receipt number with date
     */
    public static String generateReceiptNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String datePart = sdf.format(new Date());
        long timeMillis = System.currentTimeMillis() % 10000;
        return "RCP-" + datePart + "-" + String.format("%04d", timeMillis);
    }
    
    /**
     * Calculate days between two dates
     */
    public static long getDaysBetween(Date startDate, Date endDate) {
        long diffInMillis = endDate.getTime() - startDate.getTime();
        return diffInMillis / (24 * 60 * 60 * 1000);
    }
}
