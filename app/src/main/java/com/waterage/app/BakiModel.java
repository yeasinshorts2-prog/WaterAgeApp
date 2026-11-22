package com.waterage.app;

public class BakiModel {
    public int id;
    public String title;
    public double amount;
    public String date;
    public boolean isPaid; // false = বাকি, true = জমা

    public BakiModel(int id, String title, double amount, String date, boolean isPaid) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.isPaid = isPaid;
    }
          }
