package com.specialops.tttstudent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.specialops.tttstudent.managers.UserManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private UserManager userManager = UserManager.getInstance();

    private FirebaseDatabase tttRealTime;
    private DatabaseReference tttRealTimeRef;
    private FirebaseFirestore tttFireStore;
    private FirebaseUser tttUser;
    private String userRoute;
    private String uid;
    private String refAddress;
    private Map<String,LatLng> busLocations = new HashMap<String,LatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!userManager.isCurrentUserLogged()){
            startSignInActivity();
        }
        else{

            //Acquiring user id
            tttUser = FirebaseAuth.getInstance().getCurrentUser();
            uid = tttUser.getUid();
        }
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 100);


        if(userManager.isCurrentUserLogged())
        {
            //Acquiring Preferred Route
            tttFireStore = FirebaseFirestore.getInstance();
            DocumentReference docRef = tttFireStore.collection("users").document(uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            userRoute = document.get("route").toString();
                            //Acquiring Locations
                            tttRealTime = FirebaseDatabase.getInstance("https://noble-radio-299516-default-rtdb.europe-west1.firebasedatabase.app/");
                            refAddress = "journeys/routes/"+userRoute;
                            tttRealTimeRef = tttRealTime.getReference(refAddress);

                            //Listening for LatLng changes
                            ValueEventListener tttRealTimeListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if(snapshot.exists())
                                    {
                                        Log.e("Analyze",""+snapshot.child("sharingStatus"));
                                        busLocations.clear();
//                        for(DataSnapshot childSnapShot : snapshot.getChildren())
//                        {

                                        boolean shareStatus = snapshot.child("sharingStatus").getValue(Boolean.class);
                                        if(shareStatus)
                                        {
                                            double latitude = snapshot.child("latitude").getValue(Double.class);
                                            double longitude = snapshot.child("longitude").getValue(Double.class);
                                            LatLng currentLoc = new LatLng(latitude,longitude);
                                            busLocations.put(userRoute+" Driver",currentLoc);
                                            Log.e("check",""+busLocations.containsValue(currentLoc));

                                        }
                                        else{
                                            Toast noDriver = Toast.makeText(getApplicationContext(),"No driver active on "+userRoute,Toast.LENGTH_SHORT);
                                            noDriver.show();
                                        }
//                        }


                                        FragmentManager fragManager = getSupportFragmentManager();
                                        FragmentTransaction fragSwitch = fragManager.beginTransaction();
                                        FragmentContainerView fragContainer = findViewById(R.id.fragmentContainerView);
                                        fragContainer.removeAllViews();
                                        fragSwitch.replace(R.id.fragmentContainerView, new StudentMapFragment(busLocations));
                                        fragSwitch.commit();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Authorization Error","Database Inaccessible");
                                }
                            };
                            tttRealTimeRef.addValueEventListener(tttRealTimeListener);
                        } else {
                            Log.d("Failed", "No such document");
                        }
                    } else {
                        Log.d("Failed 2", "get failed with ", task.getException());
                    }
                }
            });



        }


    }

    private void startSignInActivity(){
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers =
                Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build()
                );

        // Launch the activity
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
//                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false, true)
//                        .setLogo(R.drawable.ic_launcher_foreground)
                        .build(),
                RC_SIGN_IN);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        this.handleResponseAfterSignIn(requestCode,resultCode,data);
    }

    private void showSnackBar(String message){
        Toast ma;
        ma = Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT);
        ma.show();
    }


    public void handleResponseAfterSignIn(int requestCode,int resultCode,Intent data){
        IdpResponse response = IdpResponse.fromResultIntent(data);
        if(requestCode == RC_SIGN_IN){
            //success
            if(resultCode == RESULT_OK){
                userManager.createUser();
                showSnackBar(getString(R.string.connection_succeed));
            } else {
                //ERRORS
                if(response == null){
                    showSnackBar(getString(R.string.error_authentication_canceled));
                } else if( response.getError() !=null){
                    if(response.getError().getErrorCode() == ErrorCodes.NO_NETWORK){
                        showSnackBar(getString(R.string.error_no_internet));
                    } else if( response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR){
                        showSnackBar(getString(R.string.error_unknown_error));
                    }
                }
            }
        }
    }
}