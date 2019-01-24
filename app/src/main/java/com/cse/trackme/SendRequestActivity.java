package com.cse.trackme;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cse.trackme.ObjectClasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SendRequestActivity extends Activity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference mRootRef;
    private DatabaseReference mContactsRef;
    private DatabaseReference mContactRef;
    private DatabaseReference mUsersRef;
    private DatabaseReference mUserRef;




    private EditText etPhone;
    private TextView tverr;
    private Button btSend;
    private ProgressBar pbr;

    private String phone;
    private String key;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newfriend);

        etPhone= findViewById(R.id.et_reqphone);
        tverr= findViewById(R.id.tvreqerr);
        btSend = findViewById(R.id.bsendreq);
        pbr= findViewById(R.id.pbrreq);
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);


        //------------------------------------------------ User Database Access
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        mRootRef = db.getReference();
        mContactsRef =mRootRef.child("Contacts");
        mUsersRef =mRootRef.child("Users");




        //------------------------------------------------------------------

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tverr.setText("");
                phone=etPhone.getText().toString();
                if(phone.equals("")){
                    tverr.setText("Enter your friend's phone no. ex: +94123456789");
                }else if(!phone.matches("^\\+(?:[0-9] ?){6,14}[0-9]$")){
                    tverr.setText("Check phone number format. ex:+94123456789");
                }
                else{
                    btSend.setVisibility(View.GONE);
                    pbr.setVisibility(View.VISIBLE);
                    sendRequest(phone);
                }
            }
        });


    }

    private void sendRequest(String phoneno){
        mContactRef =mContactsRef.child(phoneno);
        mContactRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                key = dataSnapshot.getValue(String.class);
                if(key==null){
                    showMsg("Request failed.. User not available or hasn't verified the phone number");
                    btSend.setVisibility(View.VISIBLE);
                    pbr.setVisibility(View.GONE);
                }else{
                    if(!(key.equals(MainActivity.UID))) {
                        if(MainActivity.CurrentUser.getAllSentRequests()!=null) {
                            if (MainActivity.CurrentUser.getAllSentRequests().indexOf(key) == -1) {
                                saveRequest();
                            } else {
                                showMsg("Request already sent");
                                btSend.setVisibility(View.VISIBLE);
                                pbr.setVisibility(View.GONE);
                            }
                        }else if(MainActivity.FriendIDs.indexOf(key)!=-1){
                            showMsg("Already friends..");

                        }else{
                            saveRequest();
                        }
                    }else{
                     showMsg("You can not send request to yourself");
                        btSend.setVisibility(View.VISIBLE);
                        pbr.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                btSend.setVisibility(View.VISIBLE);
                pbr.setVisibility(View.GONE);
            }
        });

    }

    public void saveRequest(){
        mUserRef =mUsersRef.child(key);
        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user!=null) {
                    user.addRequest(MainActivity.UID);
                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(key)
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateCurrentUser();
                            } else {
                                //Error occurred while adding data
                                showMsg(task.getException().getMessage());
                                btSend.setVisibility(View.VISIBLE);
                                pbr.setVisibility(View.GONE);
                            }

                        }
                    });
                }else{
                    showMsg("User is not available anymore");
                    btSend.setVisibility(View.VISIBLE);
                    pbr.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                btSend.setVisibility(View.VISIBLE);
                pbr.setVisibility(View.GONE);
            }
        });



    }

    private void updateCurrentUser(){
        mUserRef =mUsersRef.child(MainActivity.UID);
        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                user.addSentRequest(key);
                FirebaseDatabase.getInstance().getReference("Users")
                        .child(MainActivity.UID)
                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            btSend.setVisibility(View.VISIBLE);
                            pbr.setVisibility(View.GONE);
                            showMsg("Request Sent");
                            finish();
                        }else{
                            //Error occurred while adding data
                            showMsg(task.getException().getMessage());
                            btSend.setVisibility(View.VISIBLE);
                            pbr.setVisibility(View.GONE);
                        }

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                btSend.setVisibility(View.VISIBLE);
                pbr.setVisibility(View.GONE);
            }
        });
        finish();
    }


    // Method for display toast message
    public void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

}

