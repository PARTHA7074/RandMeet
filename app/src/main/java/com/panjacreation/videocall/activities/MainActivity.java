package com.panjacreation.videocall.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.panjacreation.videocall.R;
import com.panjacreation.videocall.models.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {
    AppCompatButton findBtn;
    CircleImageView profileImg;
    TextView profileName;
    private AdView bannerAdView;
    InstallStateUpdatedListener listener;
    AppUpdateManager appUpdateManager;
    public static final int IN_APP_UPDATE_REQUEST_CODE = 109;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadAdd();
        inAppReview();
        inAppUpdate();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        profileName = findViewById(R.id.profile_name);
        try {
            profileName.setText(account.getDisplayName());
        } catch (Exception e) {
            e.printStackTrace();
        }


        profileImg = findViewById(R.id.profile_image);
        try {
            Glide.with(this).load(account.getPhotoUrl()).into(profileImg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        findBtn = findViewById(R.id.findBtn);
        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToConnectingActivityWithPermission();
            }
        });

    }

    private void loadAdd(){
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        bannerAdView = findViewById(R.id.bannerAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);

        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                super.onAdFailedToLoad(adError);
                bannerAdView.loadAd(adRequest);
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                super.onAdLoaded();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.

            }
        });

    }

    private void goToConnectingActivityWithPermission(){
        Dexter.withContext(getApplicationContext())
                .withPermissions(
                        Manifest.permission.RECORD_AUDIO
                        ,Manifest.permission.CAMERA
                        ,Manifest.permission.MODIFY_AUDIO_SETTINGS)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()){
                            Intent intent = new Intent(getApplicationContext(),ConnectingActivity.class);
                            startActivity(intent);
                        }
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()){
                            Toast.makeText(MainActivity.this, "You have denied some permission permanently, Please allow Camera and Microphone from app setting to continue", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();

                    }
                }).check();
    }

    private void inAppReview(){
        ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(MainActivity.this, reviewInfo);
                flow.addOnCompleteListener(taskFlow -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                });
            } else {
                // There was some problem, log or handle the error code.
                Log.e(TAG, "inAppReview: "+task.getException());

            }
        });
    }

    private void inAppUpdate(){

        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

        listener = state -> {
            // (Optional) Provide a download progress bar.
            if (state.installStatus() == InstallStatus.DOWNLOADING) {
                long bytesDownloaded = state.bytesDownloaded();
                long totalBytesToDownload = state.totalBytesToDownload();
                // Implement progress bar.
            }
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate();
            }


            // Log state or install the update.
        };

        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                appUpdateManager.registerListener(listener);

                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            this,
                            // Include a request code to later monitor this update request.
                            IN_APP_UPDATE_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate();
            }
        });
    }

    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.activity_main_layout),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.purple_200));
        snackbar.show();
    }

    public void onActivityResultInAppUpdate(int requestCode, int resultCode, Intent data) {
        if (requestCode == IN_APP_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        onActivityResultInAppUpdate(requestCode,resultCode,data);
    }

    @Override
    protected void onStop() {
        super.onStop();
        appUpdateManager.unregisterListener(listener);
    }


}