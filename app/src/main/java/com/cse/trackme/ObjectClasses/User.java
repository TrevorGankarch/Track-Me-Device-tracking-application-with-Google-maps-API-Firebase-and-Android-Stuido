package com.cse.trackme.ObjectClasses;


import java.util.ArrayList;
import java.util.Arrays;

public class User {
    public String name;
    public  String email;
    public String phone;
    public String friends;
    public String requests;
    public String sentrequests;

    public  User(){

    }


    public User(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.friends = "";
        this.requests="";
        this.sentrequests="";
    }

    public String getPhone(){
        return this.phone;
    }

    public String getEmail(){
        return this.email;
    }

    public String getName(){
        return this.name;
    }

    public void setPhone(String p){this.phone=p;}
    public void setEmail(String em){this.email=em;}
    public void setName(String n){this.name=n;}
    public void setFriends(String fr){this.friends=fr;}
    public void setRequests(String req){this.requests=req;}
    public void setSentrequests(String sentReq){this.sentrequests=sentReq;}

    public ArrayList<String> getAllFriends(){
        if(friends.length()!=0) {
            return new ArrayList<String>(Arrays.asList(friends.split(",")));
        }else{
            return new ArrayList<String>();
        }
    }

    public int getFriendCount(){
        if(friends.length()!=0) {
            return new ArrayList<String>(Arrays.asList(friends.split(","))).size();
        }else{
            return 0;
        }
    }

    public void addFriend(String id){
        if(friends.length()==0){
            friends+=id;
        }else{
            friends+=(","+id);
        }
    }

    public ArrayList<String> getAllRequests(){
        if(requests.length()!=0) {
            return new ArrayList<String>(Arrays.asList(requests.split(",")));
        }else{
            return new ArrayList<String>();
        }
    }

    public int getRequestCount(){
        if(requests.length()!=0) {
            return new ArrayList<String>(Arrays.asList(requests.split(","))).size();
        }else{
            return 0;
        }
    }

    public void addRequest(String id){
        if(requests.length()==0){
            requests+=id;
        }else{
            requests+=(","+id);
        }
    }


    public ArrayList<String> getAllSentRequests(){
        if(sentrequests.length()!=0) {
            return new ArrayList<String>(Arrays.asList(sentrequests.split(",")));
        }else{
            return new ArrayList<String>();
        }
    }

    public int getSentRequestCount(){
        if(sentrequests.length()!=0) {
            return new ArrayList<String>(Arrays.asList(sentrequests.split(","))).size();
        }else{
            return 0;
        }
    }

    public void addSentRequest(String id){
        if(sentrequests.length()==0){
            sentrequests+=id;
        }else{
            sentrequests+=(","+id);
        }
    }

}
