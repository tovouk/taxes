package com.josehinojo.algoliademo;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Account implements Parcelable {
    private String firstName;
    private String lastName;
    private String name;
    private String email;
    private String phone;
    private String streetAddress;
    private String city;
    private String state;
    private String address;
    private int socialSecurity;
    private double moneyOwed;

    public Account(){

    }

    public Account(String firstName,String lastName,String email,String phone, String streetAddress,
                   String city, String state, int socialSecurity,double moneyOwed){
        this.firstName = firstName;
        this.lastName = lastName;
        this.name = firstName + " " + lastName;
        this.email = email;
        this.phone = phone;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        address = streetAddress + " " + city + "," + state;
        this.socialSecurity = socialSecurity;
        this.moneyOwed = moneyOwed;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setName(){
        this.name = firstName + " " + lastName;
    }

    public void setName(String first, String last){
        this.name = first + " " + last;
    }

    public String getName(){
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setAddress(){
        this.address = streetAddress + " " + city +"," + state;
    }

    public void setAddress(String street, String city,String state){
        this.address = street + " " + city + "," + state;
    }

    public String getAddress(){
        return address;
    }

    public int getSocialSecurity() {
        return socialSecurity;
    }

    public void setSocialSecurity(int socialSecurity) {
        this.socialSecurity = socialSecurity;
    }

    public double getMoneyOwed() {
        return moneyOwed;
    }

    public void setMoneyOwed(double moneyOwed) {
        this.moneyOwed = moneyOwed;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    private Account(Parcel in){
        this();
        firstName = in.readString();
        lastName = in.readString();
        name = in.readString();
        email = in.readString();
        phone = in.readString();
        streetAddress = in.readString();
        city = in.readString();
        state = in.readString();
        address = in.readString();
        socialSecurity = in.readInt();
        moneyOwed = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(streetAddress);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(address);
        dest.writeInt(socialSecurity);
        dest.writeDouble(moneyOwed);
    }

    @NonNull
    @Override
    public String toString() {

        return firstName + " " + lastName+ " : Money Owed $" + moneyOwed;
    }

    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>(){

        @Override
        public Account createFromParcel(Parcel source) {
            return new Account(source);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
}
