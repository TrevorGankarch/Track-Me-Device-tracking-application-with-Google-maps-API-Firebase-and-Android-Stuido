package com.cse.trackme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AddMembersActivity extends Activity {





    private ListView frndlist;
    private CustomAdapter friendsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listdialog);

        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);


        frndlist = findViewById(R.id.list_dialog);
        Button done = findViewById(R.id.bdone);

        displayFriends();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }





    public void displayFriends(){
        friendsAdapter = new CustomAdapter();
        frndlist.setAdapter(friendsAdapter);

    }

    public class CustomAdapter extends BaseAdapter {

        private Button bT;


        @Override
        public int getCount() {
            return MainActivity.Friends.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override

        public View getView(final int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.row_adduser,null);

            ImageView UserLogo = view.findViewById(R.id.ivaddmem);
            TextView UserName = view.findViewById(R.id.tvmemname);
            TextView UserPhone = view.findViewById(R.id.tvmemphone);
            bT = view.findViewById(R.id.btadd);

            bT.setText(" Add ");
            UserLogo.setImageResource(R.drawable.friendico);
            UserName.setText(MainActivity.Friends.get(i).getName());
            UserPhone.setText(MainActivity.Friends.get(i).getPhone());

            bT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AddMember(MainActivity.FriendIDs.get(i));

                }
            });


            return view;

        }
    }

    public void AddMember(String MemID){
        if(MainActivity.SOScontactsIDs.indexOf(MemID)==-1) {
            MainActivity.SOScontactsIDs.add(MemID);
            showMsg("Member Added");
        }else{
            showMsg("Member Already Added");
        }
    }



    public void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }





}
