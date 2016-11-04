package com.selfi.android.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.etsy.android.grid.StaggeredGridView;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.pkmmte.view.CircularImageView;
import com.selfi.android.R;
import com.selfi.android.backend.callbacks.SelfiListener;
import com.selfi.android.backend.communication.FBRequests;
import com.selfi.android.backend.communication.FireRequests;
import com.selfi.android.constants.FBConstants;
import com.selfi.android.constants.NavConstants;
import com.selfi.android.data.model.SelfiImage;
import com.selfi.android.data.model.SelfiUser;
import com.selfi.android.manager.FireManager;
import com.selfi.android.manager.SessionManager;
import com.selfi.android.ui.selfi.adapters.SelfiesListAdapter;
import com.selfi.util.BaseActivity;
import com.selfi.util.ConnectivityUtils;
import com.selfi.util.Logger;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class ProfileScreen extends BaseActivity implements AdapterView.OnItemClickListener {


    @BindView(R.id.tool_bar)Toolbar toolbar;
    @BindView(R.id.cover_image)ImageView coverImg;
    @BindView(R.id.profile_image)CircularImageView profileImg;
    @BindView(R.id.join_us_btn)Button joinBtn;
    @BindView(R.id.count_points)TextView pointsTv;
    @BindView(R.id.count_selfies)TextView mySelfiesCount;
    private CallbackManager mCallbackManager;
    @BindView(R.id.grid_view)StaggeredGridView gridView;
    @BindView(R.id.loading)CircularProgressBar loader;
    private SelfiUser selfiUser;
    private SelfiesListAdapter selfiAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        setupToolbar(true);
        initFBVars();
        setUpGridView();
        if (SessionManager.getUser()!=null){
            if (!SessionManager.isAnonymous()){
                joinBtn.setVisibility(View.GONE);
                updateUi();
                getSelfiUserInfo();
                obtainImages();
            }
        }
        if (getIntent().getBooleanExtra(NavConstants.SIGNUP_NOW,false)){
            signUpWithFacebook();
        }
    }

    @OnClick(R.id.join_us_btn)void signUpWithFacebook(){
        if (ConnectivityUtils.isNetworkAvailable(this)) {
            Logger.forDebug("logging by fb");
            LoginManager.getInstance().logInWithReadPermissions(this, FBConstants.permissions);
        }else {
            Snackbar.make(findViewById(R.id.activity_profile),"Check internet connection",Snackbar.LENGTH_SHORT).show();
        }
    }

    private void initFBVars() {
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Logger.forDebug("facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("FACE", "onCancel: ");
                hideLoading();
            }

            @Override
            public void onError(FacebookException error) {
                Logger.forDebug("facebook:onError :"+ error);
                hideLoading();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        showLoading();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        SessionManager.getUser().linkWithCredential(credential)
                .addOnSuccessListener(this, authResult -> {
                    Logger.forDebug("handleFacebookAccessToken : On success");
                    Logger.forDebug("saving user ..");
                    FBRequests.saveNewUser(new SelfiListener() {
                        @Override
                        public void onSuccess(Object something) {
                            ProfileScreen.this.selfiUser = (SelfiUser) something;
                            updateUi();
                            hideLoading();
                            joinBtn.setVisibility(View.GONE);
                            returnResult();
                        }

                        @Override
                        public void onError(String errDetail) {
                            hideLoading();
                            Logger.forDebug(errDetail);
                        }
                    });
                }).addOnFailureListener(this, e -> {
                    Logger.forDebug("handleFacebookAccessToken : On onFailure previous linked "+e.getMessage());
                    FireManager.getFirebaseAuth().signInWithCredential(credential).addOnSuccessListener(authResult -> {
                        Logger.forDebug("pre linked user signed again");
                        hideLoading();
                        returnResult();
                    });

                });
    }

    private void returnResult() {
        setResult(RESULT_OK);
        finish();
    }

    private void getSelfiUserInfo() {
        showLoading();
        FireRequests.getCustomUser(SessionManager.getUser().getUid(),new SelfiListener() {
            @Override
            public void onSuccess(Object something) {
                hideLoading();
                ProfileScreen.this.selfiUser=(SelfiUser) something;
                updateUi();
            }

            @Override
            public void onError(String errDetail) {
                Logger.forDebug(errDetail);
            }
        });

    }

    private void updateUi() {
        if (SessionManager.isAnonymous()){
            getSupportActionBar().setTitle(getString(R.string.app_name));
            return;
        }
        getSupportActionBar().setTitle(selfiUser!=null?selfiUser.getName():SessionManager.getUser().getDisplayName());
        pointsTv.setText(selfiUser!=null?""+selfiUser.getPoints():"0");

            Picasso.with(this)
                    .load(selfiUser!=null?selfiUser.getProfileImg():" s")
                    .placeholder(R.drawable.avatar_placeholder)
                    .into(profileImg);
            if (selfiUser!=null){
                Picasso.with(this)
                        .load(selfiUser.getCoverImg())
                        .placeholder(R.drawable.bg_profile_header)
                        .into(coverImg);
            }
    }

    private void hideLoading() {
        loader.setVisibility(View.GONE);
    }

    private void showLoading() {
        loader.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.aviary_slide_in_left, R.anim.aviary_slide_out_right);
    }

    private void obtainImages(){
        loader.setVisibility(View.VISIBLE);
        FireRequests.getUserImages(new SelfiListener() {
            @Override
            public void onSuccess(Object newImages) {
                selfiAdapter.update((ArrayList<SelfiImage>) newImages);
                mySelfiesCount.setText(""+((ArrayList<SelfiImage>) newImages).size());
                loader.setVisibility(View.GONE);
            }

            @Override
            public void onError(String errDetail) {
                Logger.forError(errDetail);
                loader.setVisibility(View.GONE);
            }
        });
    }

    private void setUpGridView() {
        this.gridView.setOnItemClickListener(this);
        selfiAdapter = new SelfiesListAdapter(this);
        gridView.setAdapter(selfiAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
