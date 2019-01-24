package com.cse.trackme;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {


    public static String UID = "";

    private EditText etmail;
    private EditText etpassword;
    private Button btLogin;
    private String mail;
    private String password;
    private ProgressBar pbr;
    private CheckBox ckremembr;
    private FirebaseAuth mAuth;

    private  SharedPreferences.Editor editor;
    private boolean rememberPref;
    private String email;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.getSupportActionBar().hide();

        TextView regLink = findViewById(R.id.signup);
        etmail = findViewById(R.id.et_mail);
        etpassword = findViewById(R.id.et_password);
        btLogin = findViewById(R.id.bLogin);
        pbr = findViewById(R.id.pbrlog);
        ckremembr= findViewById(R.id.ckrem);
        mAuth = FirebaseAuth.getInstance();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.

            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);

        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);
        }

        editor = getSharedPreferences("phonePrefs", MODE_PRIVATE).edit();

        SharedPreferences prefs = getSharedPreferences("remPrefs", MODE_PRIVATE);
        rememberPref = prefs.getBoolean("remember", false);
        email = prefs.getString("email","");

        if(rememberPref){
            etmail.setText(email);
        }



        regLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mail=etmail.getText().toString();
                password=etpassword.getText().toString();

                if (mail.equals("") || password.equals("")){
                    showMsg("Please re check your inputs");
                }else{
                    login();
                }
            }
        });
    }

    public void login(){
        if(ckremembr.isChecked()){
            editor.putBoolean("remember", true);
            editor.putString("email",mail);
            editor.apply();

        }else{
            editor.putBoolean("remember", false);
            editor.putString("email","");
            editor.apply();

        }

        if(isNetworkAvailable(this)) {
            btLogin.setVisibility(View.GONE);
            pbr.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(mail,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                UID=mAuth.getCurrentUser().getUid();
                                MainActivity.UID=UID;
                                btLogin.setVisibility(View.VISIBLE);
                                pbr.setVisibility(View.GONE);
                                endLogin();

                            }else{
                                //Error occurred
                                showMsg(task.getException().getMessage());
                                btLogin.setVisibility(View.VISIBLE);
                                pbr.setVisibility(View.GONE);
                            }

                        }
                    });

        }
        else{
            showMsg("Please check your internet connection");
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    public void endLogin(){
        Intent intent = new Intent(getApplicationContext(), PhoneVerifyActivity.class);
        startActivity(intent);

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
