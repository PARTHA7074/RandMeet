package com.panjacreation.videocall.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.panjacreation.videocall.R;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConnectingActivity extends AppCompatActivity {
    CircleImageView profileImg;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String userName;
    boolean isOk = false;
    boolean destroyed = false;
    boolean nextActivity = false;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting);

        loadAdd();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);


        profileImg = findViewById(R.id.profile_image2);
        try {
            Glide.with(this).load(account.getPhotoUrl()).into(profileImg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        userName = auth.getUid();

        database.getReference().child("users").orderByChild("status").equalTo(0).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getChildrenCount()>0 ){
                            isOk = true;
                            for (DataSnapshot childSnap : snapshot.getChildren()) {
                                database.getReference().child("users").child(Objects.requireNonNull(childSnap.getKey()))
                                        .child("incoming").setValue(userName);
                                database.getReference().child("users").child(childSnap.getKey())
                                        .child("status").setValue(1);

                                Intent intent = new Intent(getApplicationContext(),CallingActivity.class);
                                nextActivity = true;
                                String incoming = childSnap.child("incoming").getValue(String.class);
                                String createdBy = childSnap.child("createdBy").getValue(String.class);
                                boolean isAvailable = childSnap.child("isAvailable").getValue(Boolean.class);
                                intent.putExtra("username",userName);
                                intent.putExtra("incoming",incoming);
                                intent.putExtra("createdBy",createdBy);
                                intent.putExtra("isAvailable",isAvailable);
                                startActivity(intent);
                                finish();
                            }
                        }
                        else{
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("incoming",userName);
                            map.put("createdBy",userName);
                            map.put("isAvailable",true);
                            map.put("status",0);

                            database.getReference().child("users").child(userName)
                                    .setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference().child("users").child(userName)
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.child("status").exists()){
                                                                if (snapshot.child("status").getValue(Integer.class) == 1){
                                                                    if (isOk)
                                                                        return;
                                                                    isOk = true;
                                                                    Intent intent = new Intent(getApplicationContext(),CallingActivity.class);
                                                                    nextActivity = true;
                                                                    String incoming = snapshot.child("incoming").getValue(String.class);
                                                                    String createdBy = snapshot.child("createdBy").getValue(String.class);
                                                                    boolean isAvailable = Boolean.TRUE.equals(snapshot.child("isAvailable").getValue(Boolean.class));
                                                                    intent.putExtra("username",userName);
                                                                    intent.putExtra("incoming",incoming);
                                                                    intent.putExtra("createdBy",createdBy);
                                                                    intent.putExtra("isAvailable",isAvailable);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mInterstitialAd != null) {
            mInterstitialAd.show(ConnectingActivity.this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
            destroyed = true;
            database.getReference().child("users").child(userName).setValue(null);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!destroyed && !nextActivity) {
            database.getReference().child("users").child(userName).setValue(null);
            finish();
        }
    }

    private void loadAdd(){
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(TAG, "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad dismissed fullscreen content.");
                                mInterstitialAd = null;
                                destroyed = true;
                                finish();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show fullscreen content.");
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(TAG, "Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad showed fullscreen content.");
                                database.getReference().child("users").child(userName).setValue(null);
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                        loadAdd();
                    }
                });
    }

}