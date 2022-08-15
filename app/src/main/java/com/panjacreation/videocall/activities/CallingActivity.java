package com.panjacreation.videocall.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.panjacreation.videocall.R;
import com.panjacreation.videocall.models.InterfaceJava;

import java.util.UUID;

public class CallingActivity extends AppCompatActivity {
    WebView webView;
    String uniqueId = "";
    FirebaseAuth auth;
    String username;
    String friendUsername;
    boolean isPeerConnected = false;
    DatabaseReference firebaseRef;
    boolean isAudio = true;
    boolean isVideo = true;
    String createdBy;
    boolean pageExit = false;

    Group control;

    ImageView endCallBtn,audioBtn,videoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webView = findViewById(R.id.webView);

        auth = FirebaseAuth.getInstance();
        firebaseRef = FirebaseDatabase.getInstance().getReference().child("users");

        username = getIntent().getStringExtra("username");
        String incoming = getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");

        /*friendUsername = "";
        if (incoming.equalsIgnoreCase(friendUsername)){
            friendUsername = incoming;
        }*/

        friendUsername = incoming;


        firebaseRef.child(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    goToMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        endCallBtn = findViewById(R.id.end_call);
        audioBtn = findViewById(R.id.audioBtn);
        videoBtn = findViewById(R.id.videoBtn);
        control = findViewById(R.id.crontrols);

        setUpWebView();

        audioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                callJavaScriptFunction("javascript:toggleAudio(\""+isAudio+"\")");
                if (isAudio){
                    audioBtn.setImageResource(R.drawable.btn_unmute_normal);
                }else{
                    audioBtn.setImageResource(R.drawable.btn_mute_normal);
                }
            }
        });

        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideo = !isVideo;
                callJavaScriptFunction("javascript:toggleVideo(\""+isVideo+"\")");
                if (isVideo){
                    videoBtn.setImageResource(R.drawable.btn_video_normal);
                }else{
                    videoBtn.setImageResource(R.drawable.btn_video_muted);
                }
            }
        });

        endCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMainActivity();
            }
        });

    }


    void setUpWebView(){
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.addJavascriptInterface(new InterfaceJava(this),"Android");
        loadVideoCall();
    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        webView.loadUrl(filePath);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });
    }

    void initializePeer() {
        uniqueId = getUniqueId();

        callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");

        if (createdBy.equalsIgnoreCase(username)){
            if (pageExit)
                return;
            firebaseRef.child(username).child("connId").setValue(uniqueId);
            firebaseRef.child(username).child("isAvailable").setValue(true);

            control.setVisibility(View.VISIBLE);
        }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendUsername = createdBy;
                    FirebaseDatabase.getInstance().getReference().child("users")
                            .child(friendUsername).child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null){
                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            },3000);
        }
    }

    public void onPeerConnected(){
        isPeerConnected = true;
    }

    void sendCallRequest(){
        if (!isPeerConnected){
            Toast.makeText(this, "You are not connected. Please check your internet", Toast.LENGTH_SHORT).show();
            return;
        }
        listenConnId();
    }

    void listenConnId(){
        firebaseRef.child(friendUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null){
                    return;
                }
                control.setVisibility(View.VISIBLE);
                String connId = snapshot.getValue(String.class);
                callJavaScriptFunction("javascript:startCall(\""+connId+"\")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    void  callJavaScriptFunction(String function){
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(function,null);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageExit = true;
        firebaseRef.child(createdBy).setValue(null);
        webView.loadUrl("file:///android_asset/nonexistent.html");
        finish();
    }

    void goToMainActivity(){
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finishAffinity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToMainActivity();
    }

}