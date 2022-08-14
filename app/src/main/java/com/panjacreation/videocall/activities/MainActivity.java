package com.panjacreation.videocall.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
        });

    }
}