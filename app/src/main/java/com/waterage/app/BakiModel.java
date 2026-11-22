package com.waterage.app;

public class BakiModel {
    public String key;
    public String title;
    public double amount;
    public String date;
    public boolean isPaid; // false = বাকি, true = জমা

    // Firebase থেকে ডেটা পড়ার জন্য এই খালি কনস্ট্রাক্টরটি জরুরি
    public BakiModel() {
    }

    public BakiModel(String key, String title, double amount, String date, boolean isPaid) {
        this.key = key;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.isPaid = isPaid;
    }
}
