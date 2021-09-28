package com.cmanghan.myservice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;


public class Transaction implements Comparable<Transaction>{
    private String payer;
    private int points;
    private Date dateTimestamp;



    public Transaction(String payer, int points, String timestamp) throws ParseException {
        this.payer = payer;
        this.points = points;
       this.dateTimestamp = convertEnteredTimestamp(timestamp);
    }

    public String getPayer() {
        return payer;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    public Date getDateTimestamp() {
        return dateTimestamp;
    }

    public Date convertEnteredTimestamp(String timestamp) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        //assumes all dates are in PST
        sdf.setTimeZone(TimeZone.getTimeZone("PST"));
        Date convertedDate = sdf.parse(timestamp);
        return convertedDate;
    }

    @Override
    public int compareTo(Transaction o) {
        return getDateTimestamp().compareTo(o.getDateTimestamp());
    }
}
