package com.example.zenchat.models;

import android.webkit.JavascriptInterface;

import com.example.zenchat.activities.CallActivity;

public class interfaceJava {
    CallActivity callActivity;
    public interfaceJava (CallActivity callActivity){
        this.callActivity = callActivity;
    }
    @JavascriptInterface
    public void onPeerConnected(){
        callActivity.onPeerConnected();
    }
}
