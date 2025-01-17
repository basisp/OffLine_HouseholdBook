package com.example.offline_householdbook.db;

public class FinancialRecord {
    // Construct
    public FinancialRecord(String date, String categoryName, int amount, String memo) {
        this.date = date;
        this.categoryName = categoryName;
        this.amount = amount;
        this.memo = memo;
    }
    // _id 있는 버전
    public FinancialRecord(int _id, String date, String categoryName, int amount, String memo) {
        this._id = _id;
        this.date = date;
        this.categoryName = categoryName;
        this.amount = amount;
        this.memo = memo;
    }

    // Field
    private int _id;
    private String date;    // 날짜
    private String categoryName;
    private int amount;
    private String memo;

    // Getter
    public int get_id() { return _id; }

    public String getDate() {
        return date;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getAmount() {
        return amount;
    }

    public String getMemo() {
        return memo;
    }


    // Method
    public String toString() {
        return "날짜: " + date + " 카테고리: " + categoryName + " 금액: " + amount + " 메모: " + memo;
    }
}


