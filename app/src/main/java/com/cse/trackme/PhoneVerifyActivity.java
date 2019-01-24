package com.cse.trackme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class PhoneVerifyActivity extends AppCompatActivity {

    private TextView verimsg;
    private EditText etcode;
    private Button btVeri;
    private ProgressBar pbr;
    private  TextView skip;



    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference mRootRef;
    private DatabaseReference mUsersRef;
    private DatabaseReference mUserRef;

    private  SharedPreferences.Editor editor;
    private boolean phoneverified;

    private String phone;
    private String codeSent;
    private String codeEntered;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phoneverification);
        this.getSupportActionBar().hide();

        TextView regLink = findViewById(R.id.signup);
        verimsg = findViewById(R.id.tvverimsg);
        etcode = findViewById(R.id.etcode);
        pbr = findViewById(R.id.pbrveri);
        btVeri = findViewById(R.id.bVeri);
        skip = findViewById(R.id.tvskip);

        editor = getSharedPreferences("phonePrefs", MODE_PRIVATE).edit();

        SharedPreferences prefs = getSharedPreferences("phonePrefs", MODE_PRIVATE);
        phoneverified = prefs.getBoolean("verified", false);

        if (phoneverified){
            //No need to verify again
            MainActivity.NumberVerified=true;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
        }


        mAuth = FirebaseAuth.getInstance();

        db = FirebaseDatabase.getInstance();
        mRootRef = db.getReference();
        mUsersRef =mRootRef.child("Users");
        mUserRef = mUsersRef.child(LoginActivity.UID);

        mUserRef.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        //showMsg(user.getPhone());
                        phone =user.getPhone();
                        verimsg.setText("OPT has been sent to " + phone +  " Please enter it below");
                        if (phoneverified){
                            //No need to verify again
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        }else{
                            if(phone!=null && phone!="") {
                                verifyPhone();
                            }else{
                                //Error
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
            });









        btVeri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pbr.setVisibility(View.VISIBLE);
                btVeri.setVisibility(View.GONE);
                codeEntered=etcode.getText().toString();
                //showMsg(codeEntered);
                if(codeEntered.isEmpty()){
                    showMsg("Enter the verification code");
                    pbr.setVisibility(View.GONE);
                    btVeri.setVisibility(View.VISIBLE);
                }else if (codeSent!=null){
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, codeEntered);
                    mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseDatabase.getInstance().getReference("Contacts")
                                        .child(phone)
                                        .setValue(LoginActivity.UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {

                                            showMsg("Phone number verified..");


                                        }else{
                                            //Error occurred while adding data
                                            showMsg("Phone number verified..but" + task.getException().getMessage());
                                        }

                                    }
                                });

                                MainActivity.NumberVerified=true;
                                editor.putBoolean("verified", true);
                                editor.apply();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                pbr.setVisibility(View.GONE);
                                btVeri.setVisibility(View.VISIBLE);
                                finish();
                            }else{
                                showMsg("Verification failed check the code again.");
                                pbr.setVisibility(View.GONE);
                                btVeri.setVisibility(View.VISIBLE);
                            }

                        }
                    });




                }else {
                    showMsg("Please try phone number verification later");
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    pbr.setVisibility(View.GONE);
                    btVeri.setVisibility(View.VISIBLE);
                    finish();
                }
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pbr.setVisibility(View.VISIBLE);
                btVeri.setVisibility(View.GONE);
                skip.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
                pbr.setVisibility(View.GONE);
                btVeri.setVisibility(View.VISIBLE);
                skip.setVisibility(View.VISIBLE);

            }
        });


    }





    public void verifyPhone(){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks




    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.


            finishVerify();
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.


            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
            }

            // Show a message and update the UI
            // ...
        }

        @Override
        public void onCodeSent(String verificationId,
                               PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.


            // Save verification ID and resending token so we can use them later

            codeSent =verificationId;
            //showMsg(codeSent);

            // ...
        }
    };

    public void finishVerify(){

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
