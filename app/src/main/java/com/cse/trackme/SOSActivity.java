package com.cse.trackme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cse.trackme.ObjectClasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SOSActivity extends AppCompatActivity {


    private EditText ETmsg;
    private ListView SOSList;
    private Button btAddmem;
    private CheckBox ckSOS;
    private Button btSave;
    private ProgressBar pbr;

    private CustomAdapter SOSAdapter;




    private String msgInput;
    private String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);
        this.getSupportActionBar().setTitle("Track Me | Emergency SMS Settings ");

        ETmsg= findViewById(R.id.etsosmessage);
        SOSList= findViewById(R.id.list_sos);
        btAddmem= findViewById(R.id.baddmem);
        ckSOS= findViewById(R.id.ckmsg);
        btSave= findViewById(R.id.bsavesos);
        pbr = findViewById(R.id.pbrsos);




        displayFriends();


        btAddmem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SOSActivity.this, AddMembersActivity.class);
                startActivity(i);
            }
        });



        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btSave.setVisibility(View.GONE);
                pbr.setVisibility(View.VISIBLE);
                msgInput = ETmsg.getText().toString();

                /**

                if(msgInput.indexOf("<location>")!=-1){

                }else{
                    showMsg("Error : <location> tag is required");
                }
                 **/
                if(MainActivity.SOScontactsIDs.size()>0) {
                    String SOSContacts = android.text.TextUtils.join(",", MainActivity.SOScontactsIDs);

                            FirebaseDatabase.getInstance().getReference("SOS")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(SOSContacts).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        btSave.setVisibility(View.VISIBLE);
                                        pbr.setVisibility(View.GONE);

                                        showMsg("Successfully Saved");
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);

                                    } else {
                                        //Error occurred while adding data
                                        showMsg(task.getException().getMessage());
                                        btSave.setVisibility(View.VISIBLE);
                                        pbr.setVisibility(View.GONE);
                                    }

                                }
                            });
                }else{
                    showMsg("Nothing to save");
                }


            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        displayFriends();
    }



    public void displayFriends(){

        SOSAdapter = new CustomAdapter();
        SOSList.setAdapter(SOSAdapter);

        ListAdapter listadp = SOSList.getAdapter();
        if (listadp != null) {
            int totalHeight = 0;
            for (int i = 0; i < listadp.getCount(); i++) {
                View listItem = listadp.getView(i, null, SOSList);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = SOSList.getLayoutParams();
            params.height = totalHeight + (SOSList.getDividerHeight() * (listadp.getCount() - 1));
            SOSList.setLayoutParams(params);
            SOSList.requestLayout();
        }

    }

    public class CustomAdapter extends BaseAdapter {
        private Button bT;


        @Override
        public int getCount() {
            return MainActivity.SOScontactsIDs.size();
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

            bT.setText(" Remove ");
            UserLogo.setImageResource(R.drawable.friendico);
            User friend = MainActivity.Friends.get(MainActivity.FriendIDs.indexOf(MainActivity.SOScontactsIDs.get(i)));
            UserName.setText(friend.getName());
            UserPhone.setText(friend.getPhone());

            bT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeMember(i);

                }
            });


            return view;

        }
    }

    private void removeMember(int id){
        try {
            MainActivity.SOScontactsIDs.remove(id);
            displayFriends();
        }catch (IndexOutOfBoundsException e){

        }
    }


    // Method for display toast message
    public void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

}


