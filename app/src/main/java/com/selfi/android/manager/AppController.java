package com.selfi.android.manager;

import android.app.Application;
import android.content.Intent;

import com.adobe.creativesdk.aviary.AdobeImageIntent;
import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;
import com.adobe.creativesdk.foundation.auth.IAdobeAuthClientCredentials;
import com.facebook.FacebookSdk;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by ganna on 24-06-2015.
 */
public class AppController extends Application implements IAdobeAuthClientCredentials {

    private static final String CREATIVE_SDK_CLIENT_ID = "5c794530ac9248d3b49f8c0f7f77ba87";
    private static final String CREATIVE_SDK_CLIENT_SECRET = "3218656a-1c01-4be5-8a18-f906cd5176c9";


    @Override
    public void onCreate() {
        super.onCreate();
//        for image editor content synchronization
        Intent cdsIntent = AdobeImageIntent.createCdsInitIntent(getBaseContext(), "CDS");
        startService(cdsIntent);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AdobeCSDKFoundation.initializeCSDKFoundation(getApplicationContext());

        // to enable offline disk cache
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


    }

    @Override
    public String getClientID() {
        return CREATIVE_SDK_CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return CREATIVE_SDK_CLIENT_SECRET;
    }
}
