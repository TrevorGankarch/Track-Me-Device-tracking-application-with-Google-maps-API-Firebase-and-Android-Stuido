package com.cse.trackme;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cse.trackme.ObjectClasses.User;
import com.cse.trackme.ObjectClasses.UserLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity  implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback{

    private static final int MAXIMUM_TRACKING_LIMIT = 15;
    private static final int MY_LOCATION_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private int defaultZoom = 15;
    public static boolean NumberVerified=false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;

    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference mRootRef;
    private DatabaseReference mUsersRef;
    private DatabaseReference mUserRef;

    private User tempuser; // Temp user for fetching friends
    public static User CurrentUser;
    public static String UID;
    private LatLng MyLatLng;

    private RelativeLayout maplayout;
    private RelativeLayout frndmngrlayout;
    private RelativeLayout locatelayout;
    private LinearLayout soslayout;
    private LinearLayout welcomelayout;
    private boolean mapvisible=true;

    private LocationManager locationManager;
    private LocationListener listener;

    private CardView cdmylocation;
    private CardView cdfriends;
    private CardView cdlocate;
    private CardView cdsos;

    private ListView locatelist;

    private SeekBar ZoomBar;


    //------------------------------------------------ Friend Manager

    private ListView frndlist;
    private ProgressBar pbrfrnd;
    private TextView tvTracking;
    private  TextView tvreqnum;
    private CardView cdtracknum;
    private  CardView cdreq;
    private  CardView cdblock;
    private  CardView cdaddfrnd;
    private  CardView cdblockfrnd;
    private LinearLayout nofrndsview;
    private TextView tvnofrnds;
    public static ArrayList<User> Friends = new ArrayList<User>();
    public static ArrayList<User> Requests = new ArrayList<User>();
    public static ArrayList<String> SOScontactsIDs = new ArrayList<String>();
    public static ArrayList<String> RequestIDs =null;
    public static ArrayList<String> FriendIDs =null;
    private FMCustomAdapter friendsAdapter;
    private RMCustomAdapter requestAdapter;
    private LOCCustomAdapter locationAdapter;


    private int TempID;

    private ArrayList<UserLocation> LocationArray = new ArrayList<UserLocation>();
    private ArrayList<UserLocation> TempLocationArray;
    private ArrayList<User> TempFriendsArray;
    private ArrayList<User> TempFriendsArray2;
    private ArrayList<User> TempRequestArray;
    private ArrayList<Integer> TrackingArray = new ArrayList<Integer>();
    //private  HashMap<Integer,Marker> MarkersMap = new HashMap<Integer, Marker>();


    //------------------------------------------------

    //------------------------------------------------SOS Layout
    private CardView cdsendsos;
    private CardView cdsossettings;
    //-------------------------------------------------



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getSupportActionBar().hide();

        //-----------------------------------------------------------Main Layout
        maplayout = findViewById(R.id.maplayout);
        frndmngrlayout = findViewById(R.id.frndmngrlayout);
        locatelayout = findViewById(R.id.locatelayout);
        soslayout = findViewById(R.id.soslayout);
        welcomelayout= findViewById(R.id.welcomelayout);
        locatelayout.setVisibility(View.GONE);
        welcomelayout.setVisibility(View.VISIBLE);
        cdmylocation = findViewById(R.id.cdme);
        cdfriends = findViewById(R.id.cdfrnd);
        cdlocate = findViewById(R.id.cdlocate);
        cdsos = findViewById(R.id.cdsos);
        locatelist= findViewById(R.id.list_);
        ZoomBar= findViewById(R.id.seek);
        //--------------------------------------------------------------

        //------------------------------------------------ Friend Manager
        frndlist = findViewById(R.id._frndlist);
        pbrfrnd= findViewById(R.id.pbr);
        tvTracking= findViewById(R.id.tvtracking);
        tvreqnum= findViewById(R.id.tvreqnum);
        cdtracknum= findViewById(R.id.cdtrack);
        cdreq= findViewById(R.id.cdreq);
        cdblock= findViewById(R.id.cdloc);
        cdaddfrnd = findViewById(R.id.cdaddfrnd);
        cdblockfrnd = findViewById(R.id.cdblock);
        nofrndsview= findViewById(R.id.viewnofriend);
        tvnofrnds= findViewById(R.id.tvnofrnd);

        //------------------------------------------------

        //------------------------------------------------SOS Layout
        cdsendsos = findViewById(R.id.cdsendsos);
        cdsossettings = findViewById(R.id.cdsettings);

        //-------------------------------------------------

        //------------------------------------------------ User Database Access

        ZoomBar.setProgress(5);
        ZoomBar.incrementProgressBy(5);
        ZoomBar.setMax(10);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        mRootRef = db.getReference();
        mUsersRef =mRootRef.child("Users");
        mUserRef=mUsersRef.child(UID);
        mUserRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CurrentUser = dataSnapshot.getValue(User.class);
                //Log.d("CurrentUser", CurrentUser.getName());
                if (CurrentUser!=null) {
                    tvreqnum.setText(Integer.toString(CurrentUser.getRequestCount()));
                    loadFriends();
                    loadRequests();
                    refreshLocateArray();
                    locatelayout.setVisibility(View.VISIBLE);
                }else{
                    //Failed to load data at start
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
        //------------------------------------------------------------------


        //------------------------------------------------------------------------------------- MAP Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                /**
                if(MarkersMap.get(100)!=null){
                    MarkersMap.remove(100);
                }
                 **/

                MyLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                //MarkersMap.put(100,addMyMarker(me,"You are here")); //Adding markers method 1 - keep markers in hashmap and change position
                refreshMArkers();
                refreshLocateArray(); //Method 1,2 both add new markers again or change position

                UserLocation userlocation = new UserLocation(location.getLatitude(),location.getLongitude());
                FirebaseDatabase.getInstance().getReference("UserLocations")
                        .child(LoginActivity.UID)
                        .setValue(userlocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        //------------------------------------------------------------------------------------------


        //-----------------------------------------------------------------Updating Data



        //-----------------------------------------------------------------


        loadSOSContactIDs();



        //---------------------------------------------------------------- Buttons
        cdmylocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMyLocation();

            }
        });

        cdfriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFriends();
                welcomelayout.setVisibility(View.GONE);
                animateMap();
            }
        });

        cdlocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                welcomelayout.setVisibility(View.GONE);

                if(!mapvisible) {
                    animateMap();
                    loadLocation();
                }else {
                    frndmngrlayout.setVisibility(View.GONE);
                    soslayout.setVisibility(View.GONE);
                    locatelayout.setVisibility(View.VISIBLE);
                    loadLocation();
                }
                getMyLocation();
            }
        });

        cdsos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                welcomelayout.setVisibility(View.GONE);
                if (SOScontactsIDs.size()==0) {
                    Intent intent = new Intent(MainActivity.this, SOSActivity.class);
                    startActivity(intent);
                }else{
                    if(!mapvisible) {
                        animateMap2();

                    }else {
                        locatelayout.setVisibility(View.GONE);
                        frndmngrlayout.setVisibility(View.GONE);
                        soslayout.setVisibility(View.VISIBLE);
                    }

                }

            }
        });
        //=================
        cdsendsos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSOS();
            }
        });
        cdsossettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SOSActivity.class);
                startActivity(intent);
            }
        });
        //=================

        //+++++++++++
        cdaddfrnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(NumberVerified) {
                    if(FriendIDs!=null) {
                        Intent intent = new Intent(getApplicationContext(), SendRequestActivity.class);
                        startActivity(intent);
                    }else{
                        showMsg("Please wait few seconds until loading friend's data..");
                    }
                }else{
                    showMsg("Please verify your phone number to add friends");
                }
            }
        });

        cdreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadRequests();

            }
        });

        ZoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                defaultZoom=progress+10;
                mMap.animateCamera( CameraUpdateFactory.zoomTo( defaultZoom ) );

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //+++++++++++


        //--------------------------------------------------------------------







    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                // Permission was denied. Display an error message.
            }
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.

            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);
            getMyLocation();

        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
            getMyLocation();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();

        } else {
            // Show rationale and request permission.
            enableMyLocation();

        }

    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public boolean onMyLocationButtonClick() {

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    public void getMyLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                LatLng me = new LatLng(location.getLatitude(), location.getLongitude());
                                addMyMarker(me,"You are here");
                                moveCamera(me,defaultZoom);
                            }
                        }
                    });
            locationManager.requestLocationUpdates("gps", 5000, 0, listener);
        } else {
            // Show rationale and request permission.
            enableMyLocation();

        }

    }

    //=============================================================================================
    //====================== ++ Locate Friends++ ==================================================


    public void loadLocation(){

        locatelist.setAdapter(null);

        int friendcount =0;
        if(CurrentUser!=null) {
            FriendIDs = CurrentUser.getAllFriends();
            friendcount = CurrentUser.getFriendCount();
            //showMsg(Integer.toString(CurrentUser.getFriendCount()));
        }else{
            showMsg("Data not loaded. Check your connection");
        }
        if (friendcount>0 && Friends.size()!=friendcount ) {
            pbrfrnd.setVisibility(View.VISIBLE);
            TempFriendsArray = new ArrayList<User>();
            for (int i = 0; i < friendcount; i++) {
                mUserRef =mUsersRef.child(FriendIDs.get(i));
                mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        TempFriendsArray.add(dataSnapshot.getValue(User.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //System.out.println("The read failed: " + databaseError.getCode());
                    }

                });
            }
            Friends=TempFriendsArray;
            displayLocations();


        }if(Friends.size()==friendcount){
            displayLocations();

        }else{
            nofrndsview.setVisibility(View.VISIBLE);
            tvnofrnds.setText("No Friends Here");
        }


    }  // Adding friends to Track Friends List

    public void displayLocations(){
        pbrfrnd.setVisibility(View.GONE);
        nofrndsview.setVisibility(View.GONE);
        locationAdapter = new LOCCustomAdapter();
        locatelist.setAdapter(locationAdapter);
        locatelist.setVisibility(View.VISIBLE);
    }

    public class LOCCustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return Friends.size();
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

            view = getLayoutInflater().inflate(R.layout.row_locateuser,null);

            ImageView Logo = view.findViewById(R.id.ivloc);
            final TextView Name = view.findViewById(R.id.tvlocname);
            TextView Phone = view.findViewById(R.id.tvlocphone);
            CardView Loc = view.findViewById(R.id.cdloc);
            final CardView eye = view.findViewById(R.id.cdeye);


            Logo.setImageResource(R.drawable.friendico);
            Name.setText(Friends.get(i).getName());
            Phone.setText(Friends.get(i).getPhone());

            Loc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    locateFriend(i);

                    if(eye.getVisibility()==View.VISIBLE){
                        eye.setVisibility(View.GONE);

                    }else{
                        if(i<LocationArray.size()) {
                            if(LocationArray.get(i)!=null ) {
                                if(TrackingArray.indexOf(i)!=-1) {
                                    //showMsg(TrackingArray.toString());
                                    //showMsg(LocationArray.toString());
                                    eye.setVisibility(View.VISIBLE);
                                    showMsg("Tracking..");
                                }else{
                                    eye.setVisibility(View.GONE);
                                    showMsg("Tracking Stopped");
                                }
                            }else{
                                showMsg("Location not available");
                            }
                        }
                    }
                }
            });

            eye.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(LocationArray.size()>i) {
                        if (LocationArray.get(i) != null) {
                            watchFriend(i);
                        } else {
                            eye.setVisibility(View.GONE);
                            showMsg("Location not available");
                        }
                    }

                }
            });



            return view;

        }


    }

    public void watchFriend(int id){
        LatLng latLng = new LatLng(LocationArray.get(id).getLatitude(), LocationArray.get(id).getLongitude());
        moveCamera(latLng,defaultZoom);
    }

    public void refreshLocateArray(){
        int friendcount=0;

        if(CurrentUser!=null) {
            friendcount = CurrentUser.getFriendCount();
        }

        //LocationArray.clear();


        TempLocationArray = new ArrayList<UserLocation>();

        for (int i = 0; i < friendcount; i++) {

            mRootRef.child("UserLocations").child(FriendIDs.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    TempLocationArray.add(dataSnapshot.getValue(UserLocation.class));

                    //showMsg(Integer.toString(LocationArray.size()));


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //System.out.println("The read failed: " + databaseError.getCode());
                }




            });
        }
        LocationArray=TempLocationArray;
        if(LocationArray.size()==friendcount) {
            refreshMArkers();
        }




    }
    public void locateFriend(int id){
        //This id is not Firebase id..This is i inside customadapter class




        if(TrackingArray.indexOf(id)==-1){


            //showMsg(Integer.toString(LocationArray.size()));



            if(LocationArray.size()>id) {
                TrackingArray.add(id);
                //MarkersMap.put(id, addMarker(latLng, Friends.get(id).getName() + " is here")); // Method 1
                if (LocationArray.get(id)!=null){
                    LatLng latLng = new LatLng(LocationArray.get(id).getLatitude(), LocationArray.get(id).getLongitude());
                    addMarker(latLng,Friends.get(id).getName()+" is here");
                }
            }else{
                showMsg("Location not available");
            }
        }else{
            TrackingArray.remove(id);
            refreshMArkers();
            /** Method 1
            if(MarkersMap.get(id)!=null) {
                MarkersMap.remove(id).remove();
            }
             **/
        }

    }
    public  void refreshMArkers(){

        if(mMap!=null){
            mMap.clear();
            if(MyLatLng!=null) {
                addMyMarker(MyLatLng, "You are here");
            }else{
                showMsg("Please turn on Location");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

            /**  //Method 1  - changing positions

            for (Map.Entry<Integer, Marker> entry : MarkersMap.entrySet()) {
                if(entry.getKey()<MAXIMUM_TRACKING_LIMIT && LocationArray.size()>=TrackingArray.size()) {
                    if(LocationArray.size()>entry.getKey()) {
                        LatLng latLng = new LatLng(LocationArray.get(entry.getKey()).getLatitude(), LocationArray.get(entry.getKey()).getLongitude());
                        entry.getValue().setPosition(latLng);
                    }else{
                        //err
                    }
                }

            }
             **/

            //Method 2 - Adding new markers


            for(int TrackId : TrackingArray){
                if(LocationArray.size()>TrackId){
                    if (LocationArray.get(TrackId)!=null){
                        LatLng latLng = new LatLng(LocationArray.get(TrackId).getLatitude(), LocationArray.get(TrackId).getLongitude());
                        addMarker(latLng,Friends.get(TrackId).getName()+" is here");
                    }
                }
            }
        }else{
            showMsg("Map is not available");
        }
    }

    //=============================================================================================




    //==============================================================================================
    //======================= ++FRIEND MANAGER CODE++ ==============================================

    public void loadFriends(){
        frndlist.setAdapter(null);

        int friendcount =0;
        if(CurrentUser!=null) {
            FriendIDs = CurrentUser.getAllFriends();
            friendcount = CurrentUser.getFriendCount();
            //showMsg(Integer.toString(CurrentUser.getFriendCount()));
        }else{
            showMsg("Data not loaded. Check your connection");
        }
        if (friendcount>0 && Friends.size()!=friendcount ) {
            pbrfrnd.setVisibility(View.VISIBLE);
            TempFriendsArray2 = new ArrayList<User>();
            for (int i = 0; i < friendcount; i++) {
                mUserRef =mUsersRef.child(FriendIDs.get(i));
                mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        TempFriendsArray2.add(dataSnapshot.getValue(User.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //System.out.println("The read failed: " + databaseError.getCode());
                    }


                });
                Friends=TempFriendsArray2;


            }
            displayFriends();


        }else if(Friends.size()==friendcount){
            displayFriends();

        }else{
            nofrndsview.setVisibility(View.VISIBLE);
            tvnofrnds.setText("No Friends Here");
        }


    }

    public void displayFriends(){
        pbrfrnd.setVisibility(View.GONE);
        nofrndsview.setVisibility(View.GONE);
        friendsAdapter = new FMCustomAdapter();
        frndlist.setAdapter(friendsAdapter);

    }

    public class FMCustomAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return Friends.size();
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
            view = getLayoutInflater().inflate(R.layout.row_friend,null);

            ImageView Logo = view.findViewById(R.id.ivfrnd);
            TextView Name = view.findViewById(R.id.tvfrndname);
            TextView Phone = view.findViewById(R.id.tvfrndphone);

            Logo.setImageResource(R.drawable.friendico);
            Name.setText(Friends.get(i).getName());
            Phone.setText(Friends.get(i).getPhone());

            return view;

        }
    }

    //=========================================================================================

    public void loadRequests(){
        frndlist.setAdapter(null);

        int requestcount =0;
        if(CurrentUser!=null) {
            RequestIDs = CurrentUser.getAllRequests();
            requestcount = CurrentUser.getRequestCount();
        }else{
            showMsg("Data not loaded. Check your connection");
        }




        if (requestcount>0 && Requests.size()!=requestcount ) {
            pbrfrnd.setVisibility(View.VISIBLE);
            TempRequestArray = new ArrayList<User>();
            for (int i = 0; i < requestcount; i++) {
                mUserRef =mUsersRef.child(RequestIDs.get(i));
                mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        TempRequestArray.add(dataSnapshot.getValue(User.class));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //System.out.println("The read failed: " + databaseError.getCode());
                    }


                });
            }
            Requests=TempRequestArray;

            displayRequests();
        }else if(Requests.size()==requestcount){
            displayRequests();
        }else{
            nofrndsview.setVisibility(View.VISIBLE);
            tvnofrnds.setText("No Requests Here");
        }
    }

    public void displayRequests(){
        pbrfrnd.setVisibility(View.GONE);
        nofrndsview.setVisibility(View.GONE);
        requestAdapter = new RMCustomAdapter();
        frndlist.setAdapter(requestAdapter);
    }

    public class RMCustomAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return Requests.size();
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
            view = getLayoutInflater().inflate(R.layout.row_requests,null);

            ImageView Logo = view.findViewById(R.id.ivreq);
            TextView Name = view.findViewById(R.id.tvreqname);
            TextView Phone = view.findViewById(R.id.tvreqphone);
            CardView Conf = view.findViewById(R.id.cdconf);
            CardView Del = view.findViewById(R.id.cddel);

            Logo.setImageResource(R.drawable.friendico);

            Name.setText(Requests.get(i).getName());
            Phone.setText(Requests.get(i).getPhone());
            Conf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmFrindReq(i);
                }
            });
            Del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteFrindReq(i);
                }
            });


            return view;

        }
    }

    public void confirmFrindReq(int id){
        User temp = CurrentUser;
        temp.addFriend(RequestIDs.get(id));
        User tempFriend = Requests.get(id);
        String FriendID = RequestIDs.get(id);
        Requests.remove(id);
        RequestIDs.remove(id);
        temp.setRequests(android.text.TextUtils.join(",", RequestIDs));
        FirebaseDatabase.getInstance().getReference("Users")
                .child(UID)
                .setValue(temp).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {



                }else{
                    //Error occurred while adding data
                    showMsg(task.getException().getMessage());

                }

            }
        });

        tempFriend.addFriend(UID);
        FirebaseDatabase.getInstance().getReference("Users")
                .child(FriendID)
                .setValue(tempFriend).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    showMsg("Friend Added");


                }else{
                    //Error occurred while adding data
                    showMsg(task.getException().getMessage());

                }

            }
        });




    }

    public void deleteFrindReq(int id){

        User temp = CurrentUser;
        User tempFriend = Requests.get(id);
        String FriendID = RequestIDs.get(id);
        Requests.remove(id);
        RequestIDs.remove(id);
        temp.setRequests(android.text.TextUtils.join(",", RequestIDs));
        FirebaseDatabase.getInstance().getReference("Users")
                .child(UID)
                .setValue(temp).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {

                }else{
                    //Error occurred while adding data
                    showMsg(task.getException().getMessage());

                }

            }
        });

        ArrayList<String> sentReqs = tempFriend.getAllSentRequests();
        sentReqs.remove(UID);
        tempFriend.setSentrequests(android.text.TextUtils.join(",", sentReqs));

        FirebaseDatabase.getInstance().getReference("Users")
                .child(FriendID)
                .setValue(tempFriend).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    showMsg("Request deleted");


                }else{
                    //Error occurred while adding data
                    showMsg(task.getException().getMessage());

                }

            }
        });


    }


    //==============================================================================================


    //==============================================================================================
    //============================= ++ SOS ++ ======================================================

    private void loadSOSContactIDs(){


        mRootRef.child("SOS").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                if(data!=null) {
                    if (data.length() != 0) {
                        SOScontactsIDs = new ArrayList<String>(Arrays.asList(data.split(",")));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //System.out.println("The read failed: " + databaseError.getCode());
            }


        });

    }

    private void sendSOS(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);
            sendSOSSMS();
        }else{
            sendSOSSMS();
        }

    }

    private void sendSOSSMS(){
        if(MyLatLng!=null) {
            String link = "http://www.google.com/maps/place/" + MyLatLng.latitude + "," + MyLatLng.longitude;

            for (int i = 0; i < SOScontactsIDs.size(); i++) {
                User friend = MainActivity.Friends.get(MainActivity.FriendIDs.indexOf(MainActivity.SOScontactsIDs.get(i)));
                String phoneNo = friend.getPhone();
                String msg = "Hey " + friend.getName() + " I am here in emergency situation: " + link;
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, msg, null, null);
                    Toast.makeText(getApplicationContext(), "Message Sent",
                            Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.getMessage(),
                            Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
            }
        }else{
            showMsg("Your location not available");
        }
    }


    //==============================================================================================



    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    public void animateMap(){
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        //float mapHeight = displayMetrics.heightPixels / displayMetrics.density;
        float mapHeight = displayMetrics.heightPixels / 2;
        ValueAnimator slideAnimator;
        if (mapvisible) {
            slideAnimator = ValueAnimator
                    .ofInt(Math.round(mapHeight), 0)
                    .setDuration(300);
            mapvisible=false;
            locatelayout.setVisibility(View.GONE);
            soslayout.setVisibility(View.GONE);
            frndmngrlayout.setVisibility(View.VISIBLE);


        }else{
            slideAnimator = ValueAnimator
                    .ofInt(0, Math.round(mapHeight))
                    .setDuration(300);
            mapvisible=true;
            locatelayout.setVisibility(View.VISIBLE);
            frndmngrlayout.setVisibility(View.GONE);
        }



// we want to manually handle how each tick is handled so add a
// listener
        slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // get the value the interpolator is at
                Integer value = (Integer) animation.getAnimatedValue();
                // I'm going to set the layout's height 1:1 to the tick
                maplayout.getLayoutParams().height = value.intValue();
                // force all layouts to see which ones are affected by
                // this layouts height change
                maplayout.requestLayout();
            }
        });

// create a new animationset
        AnimatorSet set = new AnimatorSet();
// since this is the only animation we are going to run we just use
// play
        set.play(slideAnimator);
// this is how you set the parabola which controls acceleration
        set.setInterpolator(new AccelerateDecelerateInterpolator());
// start the animation
        set.start();
    } //For Tracking Layout

    public void animateMap2(){
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        //float mapHeight = displayMetrics.heightPixels / displayMetrics.density;
        float mapHeight = displayMetrics.heightPixels / 2;
        ValueAnimator slideAnimator;
        if (mapvisible) {
            slideAnimator = ValueAnimator
                    .ofInt( Math.round(mapHeight), 0)
                    .setDuration(300);
            mapvisible=false;
            locatelayout.setVisibility(View.GONE);
            soslayout.setVisibility(View.GONE);
            frndmngrlayout.setVisibility(View.VISIBLE);
            loadFriends();

        }else{
            slideAnimator = ValueAnimator
                    .ofInt(0,  Math.round(mapHeight))
                    .setDuration(300);
            mapvisible=true;
            soslayout.setVisibility(View.VISIBLE);
            frndmngrlayout.setVisibility(View.GONE);
        }



// we want to manually handle how each tick is handled so add a
// listener
        slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // get the value the interpolator is at
                Integer value = (Integer) animation.getAnimatedValue();
                // I'm going to set the layout's height 1:1 to the tick
                maplayout.getLayoutParams().height = value.intValue();
                // force all layouts to see which ones are affected by
                // this layouts height change
                maplayout.requestLayout();
            }
        });

// create a new animationset
        AnimatorSet set = new AnimatorSet();
// since this is the only animation we are going to run we just use
// play
        set.play(slideAnimator);
// this is how you set the parabola which controls acceleration
        set.setInterpolator(new AccelerateDecelerateInterpolator());
// start the animation
        set.start();
    } //For SOS Layout

    public void moveCamera(LatLng latLng, int zoomSize){
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLng, zoomSize);
        mMap.animateCamera(location);
    }

    public Marker addMyMarker(LatLng latLng, String title){
        return mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.memaker)));

    }

    public Marker addMarker(LatLng latLng, String title){
        return mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.trackmaker)));

    }


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
