package com.selfi.android.ui.selfi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.selfi.android.R;
import com.selfi.android.backend.callbacks.SelfiListener;
import com.selfi.android.backend.communication.FireRequests;
import com.selfi.android.data.model.SelfiImage;
import com.selfi.android.manager.FireManager;
import com.selfi.android.manager.ScreenManager;
import com.selfi.android.ui.selfi.adapters.SelfiesListAdapter;
import com.selfi.util.BaseActivity;
import com.selfi.util.ConnectivityUtils;
import com.selfi.util.Logger;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class SelfiScreen extends BaseActivity implements AdapterView.OnItemClickListener, FirebaseAuth.AuthStateListener {


    @BindView(R.id.profile_btn)ImageButton myProfileBtn;
    @BindView(R.id.pick_photo)ImageButton pickPhotoBtn;
    @BindView(R.id.notification_btn)ImageButton notifyBtn;
    @BindView(R.id.grid_view)StaggeredGridView gridView;
    @BindView(R.id.loading)CircularProgressBar loader;
    @BindView(R.id.no_internet_image)ImageView noInternetImage;
    private SelfiesListAdapter selfiAdapter;
    private static final String TAG = SelfiScreen.class.getSimpleName();
    private Snackbar snackbarNoInternet;
    @BindView(R.id.snack_bar) CoordinatorLayout snackbarLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfi);
        ButterKnife.bind(this);
        setupToolbar(false);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        setUpAnimation();
        setUpGridView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FireManager.getFirebaseAuth().addAuthStateListener(this);
    }

    private void setUpGridView() {
        this.gridView.setOnItemClickListener(this);
        selfiAdapter = new SelfiesListAdapter(SelfiScreen.this);
        gridView.setAdapter(selfiAdapter);
    }

    private void setUpAnimation() {
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        pulse.setRepeatMode(-1);
        pulse.setRepeatCount(-1);
        this.pickPhotoBtn.startAnimation(pulse);
        this.snackbarNoInternet = Snackbar.make(this.snackbarLayout, R.string.snackbar_no_internet, Snackbar.LENGTH_INDEFINITE);
    }


    private void obtainImages(){
        loader.setVisibility(View.VISIBLE);
        FireRequests.obtainImages(new SelfiListener() {
            @Override
            public void onSuccess(Object newImages) {
                Collections.reverse((ArrayList<SelfiImage>) newImages);
                selfiAdapter.update((ArrayList<SelfiImage>) newImages);
                loader.setVisibility(View.GONE);
            }

            @Override
            public void onError(String errDetail) {
                Logger.forError(errDetail);
                loader.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScreenManager.launchSelfiPreviewScreen(SelfiScreen.this,selfiAdapter.getItem(position));
    }

    @OnClick(R.id.profile_btn)void toProfileScreen(){
        ScreenManager.launchProfileScreen(this,false);
    }



    @OnClick(R.id.pick_photo)void launchPickScreen(){
        ScreenManager.launchPicScreen(this);
    }


    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityUtils.isNetworkAvailable(context)) {
                Logger.forDebug("Network:onReceive:ON");
                SelfiScreen.this.notifyBtn.setEnabled(true);
                SelfiScreen.this.myProfileBtn.setEnabled(true);
                SelfiScreen.this.noInternetImage.setVisibility(View.GONE);
                SelfiScreen.this.snackbarNoInternet.dismiss();
                SelfiScreen.this.gridView.setVisibility(View.VISIBLE);
                return;
            }
            Logger.forDebug("Network:onReceive:OFF");
            SelfiScreen.this.notifyBtn.setEnabled(false);
            SelfiScreen.this.myProfileBtn.setEnabled(false);
            SelfiScreen.this.gridView.setVisibility(View.GONE);
            SelfiScreen.this.loader.setVisibility(View.GONE);
            SelfiScreen.this.noInternetImage.setVisibility(View.VISIBLE);
            SelfiScreen.this.snackbarNoInternet = Snackbar.make(SelfiScreen.this.snackbarLayout, R.string.snackbar_no_internet, Snackbar.LENGTH_INDEFINITE);
            SelfiScreen.this.snackbarNoInternet.show();
        }
    };

    protected void onResume() {
        super.onResume();
        registerReceiver(this.networkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }


    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(this.networkChangeReceiver);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void signInAnonymously(){
        loader.setVisibility(View.VISIBLE);
        FireManager.getFirebaseAuth().signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "signInAnonymously", task.getException());
                    }else {

                    }
                });
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user!=null){
            Logger.forDebug("user logged in");
            obtainImages();
        }else {
            Logger.forDebug("user logged out");
            signInAnonymously();
        }
    }

    @OnClick(R.id.notification_btn)void openNotifications(){
        Toast.makeText(this, "Notification feature will be available soon", Toast.LENGTH_SHORT).show();
    }
}
