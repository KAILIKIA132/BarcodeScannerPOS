package com.arodi.powergas.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ExpenditureModel implements Parcelable {
    public  String expense_id;
    public  String date;
    public  String company_id;
    public  String reference;
    public  String amount;
    public String approved;
    public  String note;
    public  String status;

    public ExpenditureModel(String expense_id, String date, String company_id, String reference, String amount, String approved, String note, String status) {
        this.expense_id = expense_id;
        this.date = date;
        this.company_id = company_id;
        this.reference = reference;
        this.amount = amount;
        this.approved = approved;
        this.note = note;
        this.status = status;
    }

    protected ExpenditureModel(Parcel in) {
        expense_id = in.readString();
        date = in.readString();
        company_id = in.readString();
        reference = in.readString();
        amount = in.readString();
        approved = in.readString();
        note = in.readString();
        status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(expense_id);
        dest.writeString(date);
        dest.writeString(company_id);
        dest.writeString(reference);
        dest.writeString(amount);
        dest.writeString(approved);
        dest.writeString(note);
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExpenditureModel> CREATOR = new Creator<ExpenditureModel>() {
        @Override
        public ExpenditureModel createFromParcel(Parcel in) {
            return new ExpenditureModel(in);
        }

        @Override
        public ExpenditureModel[] newArray(int size) {
            return new ExpenditureModel[size];
        }
    };

    public String getExpense_id() {
        return expense_id;
    }

    public String getDate() {
        return date;
    }

    public String getCompany_id() {
        return company_id;
    }

    public String getReference() {
        return reference;
    }

    public String getAmount() {
        return amount;
    }

    public String getApproved() {
        return approved;
    }

    public String getNote() {
        return note;
    }

    public String getStatus() {
        return status;
    }
}
