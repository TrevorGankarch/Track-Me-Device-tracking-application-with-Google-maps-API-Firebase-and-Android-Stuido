package com.cse.trackme;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cse.trackme.ObjectClasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText etname;
    private EditText etemail;
    private EditText etphoneno;
    private EditText etpassword;
    private Button btReg;
    private TextView tverrname,tverrmail,tverrphone,tverrpassword;
    private String name;
    private String mail;
    private String phone;
    private String password;
    private ProgressBar pbr;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        this.getSupportActionBar().hide();

        etname = findViewById(R.id.etname);
        etemail = findViewById(R.id.etmail);
        etphoneno = findViewById(R.id.etphone);
        etpassword = findViewById(R.id.etpassword);
        tverrname = findViewById(R.id.tvnameerr);
        tverrmail = findViewById(R.id.tvmailerr);
        tverrphone = findViewById(R.id.tvphoneerr);
        tverrpassword = findViewById(R.id.tvpassworderr);
        btReg = findViewById(R.id.bReg);
        pbr = findViewById(R.id.pbrreg);

        mAuth =FirebaseAuth.getInstance();

        etname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etname.setBackgroundResource(R.drawable.edittext);
            }
        });

        etemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etemail.setBackgroundResource(R.drawable.edittext);
            }
        });
        etphoneno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etphoneno.setBackgroundResource(R.drawable.edittext);
            }
        });
        etpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etpassword.setBackgroundResource(R.drawable.edittext);
            }
        });


        btReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name=etname.getText().toString();
                mail=etemail.getText().toString();
                phone=etphoneno.getText().toString();
                password=etpassword.getText().toString();
                etname.setBackgroundResource(R.drawable.edittext);
                etemail.setBackgroundResource(R.drawable.edittext);
                etphoneno.setBackgroundResource(R.drawable.edittext);
                etpassword.setBackgroundResource(R.drawable.edittext);
                validate();
            }
        });
    }

    public void validate(){
        boolean Valid=true;
        tverrname.setText("");
        tverrmail.setText("");
        tverrphone.setText("");
        tverrpassword.setText("");

        if (name.equals("")){
            tverrname.setText("First Name is required field");
            etname.setBackgroundResource(R.drawable.edittextred);
            Valid=false;
        }
        if (mail.equals("")){
            tverrmail.setText("Email is required field");
            etemail.setBackgroundResource(R.drawable.edittextred);
            Valid=false;
        }else if(!mail.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")){
            tverrmail.setText("Invalid Email format");
            etemail.setBackgroundResource(R.drawable.edittextred);
            Valid=false;
        }
        if (phone.equals("")){
            tverrphone.setText("Phone number is required field");
            etphoneno.setBackgroundResource(R.drawable.edittextred);
            Valid=false;
        }else if(!phone.matches("^\\+(?:[0-9] ?){6,14}[0-9]$")){
            tverrphone.setText("Check phone number format. ex:+94123456789");
            etphoneno.setBackgroundResource(R.drawable.edittextred);
            Valid=false;
        }
        if (password.equals("")){
            tverrpassword.setText("PassWord is required field");
            etpassword.setBackgroundResource(R.drawable.edittextred);
            Valid=false;
        }else if(password.length()<6){
            tverrpassword.setText("PassWord must contains at a least 6 letters");
            etpassword.setBackgroundResource(R.drawable.edittextred);
            Valid=false;
        }
        if (Valid){
           createUser();
        }

    }

    public void clearFields(){
        etname.setText("");
        etemail.setText("");
        etphoneno.setText("");
        etpassword.setText("");
    }

    public void createUser(){
        btReg.setVisibility(View.GONE);
        pbr.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(mail,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            //Account created.. Need to store additional data

                            User user = new User(name,mail,phone);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        btReg.setVisibility(View.VISIBLE);
                                        pbr.setVisibility(View.GONE);
                                        clearFields();
                                        showMsg("Successfully Registered");
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);

                                    }else{
                                        //Error occurred while adding data
                                        showMsg("Account created but "+task.getException().getMessage());
                                        btReg.setVisibility(View.VISIBLE);
                                        pbr.setVisibility(View.GONE);
                                    }

                                }
                            });
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        btReg.setVisibility(View.VISIBLE);
                                        pbr.setVisibility(View.GONE);
                                        clearFields();
                                        showMsg("Successfully Registered");
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);

                                    }else{
                                        //Error occurred while adding data
                                        showMsg("Account created but "+task.getException().getMessage());
                                        btReg.setVisibility(View.VISIBLE);
                                        pbr.setVisibility(View.GONE);
                                    }

                                }
                            });


                        }else{
                            showMsg(task.getException().getMessage());
                            btReg.setVisibility(View.VISIBLE);
                            pbr.setVisibility(View.GONE);
                        }
                    }
                });
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    //Method for checking internet availability
    public boolean isNetworkAvailable(Context ctx)
    {
        ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting() && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected();
    }

    // Method for display toast message
    public void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}


