package com.panjacreation.videocall.models;

import android.webkit.JavascriptInterface;

import com.panjacreation.videocall.activities.CallingActivity;

public class InterfaceJava {
    CallingActivity callingActivity;

    public InterfaceJava(CallingActivity callingActivity) {
        this.callingActivity = callingActivity;
    }

    @JavascriptInterface
    public void onPeerConnected(){
        callingActivity.onPeerConnected();
    }
}
