package com.selfi.android.ui.selfi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.like.LikeButton;
import com.like.OnLikeListener;
import com.selfi.android.R;
import com.selfi.android.backend.callbacks.SelfiListener;
import com.selfi.android.backend.communication.FireRequests;
import com.selfi.android.constants.NavConstants;
import com.selfi.android.data.model.SelfiImage;
import com.selfi.android.data.model.SelfiUser;
import com.selfi.android.manager.ScreenManager;
import com.selfi.android.manager.SessionManager;
import com.selfi.util.BaseActivity;
import com.selfi.util.Logger;
import com.selfi.util.ShareUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class SelfiPreviewScreen extends BaseActivity implements OnLikeListener {


    @BindView(R.id.image)ImageView image;
    @BindView(R.id.likes)TextView likesCount;
    @BindView(R.id.report_bar)View reportBar;
    @BindView(R.id.send_report)Button sendReport;
    @BindView(R.id.result_bar)TextView resultBar;
    @BindView(R.id.author_image)ImageView authorImg;
    @BindView(R.id.tool_bar)Toolbar toolbar;
    @BindView(R.id.like_btn)LikeButton likeBtn;
    private Animation slideInBottom,slideInTop,slideOutBottom,slideOutTop;
    private String reason;
    private SelfiImage selfiImage;
    private SelfiUser author;
    private boolean isLiked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfi_preview);
        ButterKnife.bind(this);
        setupAnimations();
        setupToolbar(true);
        likeBtn.setOnLikeListener(this);
        selfiImage = getIntent().getParcelableExtra(NavConstants.SELFIPREVIEW);
        updateUi();
        if (!SessionManager.isAnonymous()) {
            getSelfiStatus();
        }
    }

    private void getSelfiStatus() {
        FireRequests.getSelfiLikeStatus(selfiImage.getId(), new SelfiListener() {
            @Override
            public void onSuccess(Object isLikedOnLine) {
                isLiked = (boolean)isLikedOnLine;
                likeBtn.setLiked(isLiked);
            }

            @Override
            public void onError(String errDetail) {

            }
        });
    }

    private void updateUi() {
        Logger.forDebug("Image id :"+selfiImage.getId()+"  "+selfiImage.getTag());
        getSupportActionBar().setTitle(TextUtils.isEmpty(selfiImage.getTag())?"#Selfi_king":"#"+selfiImage.getTag());
        final Animation alphaIn = AnimationUtils.loadAnimation(this, R.anim.alpha_in);
        alphaIn.setDuration(1500);
        alphaIn.setFillEnabled(true);
        alphaIn.setFillAfter(true);
        Picasso.with(this).load(selfiImage.getImage()).into(this.image, new Callback() {
            public void onSuccess() {
                image.startAnimation(alphaIn);
            }

            public void onError() {
                Logger.forError("picasoo error");
            }
        });

        likesCount.setText(String.valueOf((int)selfiImage.getLikes()));
        FireRequests.getCustomUser(selfiImage.getAuthorId(), new SelfiListener() {
            @Override
            public void onSuccess(Object user) {
                author= (SelfiUser) user;
                Picasso.with(SelfiPreviewScreen.this)
                        .load(author.getProfileImg())
                        .into(authorImg);
            }

            @Override
            public void onError(String errDetail) {
                Logger.forError(errDetail);
            }
        });
    }


    private void setupAnimations() {
        this.slideInTop = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
        this.slideInTop.setFillAfter(true);
        this.slideOutTop = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);
        this.slideOutTop.setFillAfter(false);
        this.slideInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        this.slideInBottom.setFillAfter(true);
        this.slideOutBottom = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
        this.slideOutBottom.setFillAfter(false);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        menu.findItem(R.id.send_report).setVisible(true);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send_report :
                showReportBar();
                return true;
            case R.id.share :
                createShareIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void createShareIntent() {
        startActivity(ShareUtils.getInstance(this).shareImage(this.image));
    }

    public void onBackPressed() {
        if (this.reportBar.getVisibility() == View.VISIBLE) {
            hideReportBar();
            return;
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.aviary_slide_in_left, R.anim.aviary_slide_out_right);
    }

    private void showReportBar() {
        this.slideInTop.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                SelfiPreviewScreen.this.reportBar.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.reportBar.startAnimation(this.slideInTop);
    }

    private void hideReportBar() {
        this.slideOutTop.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                SelfiPreviewScreen.this.reportBar.setVisibility(View.INVISIBLE);
                SelfiPreviewScreen.this.sendReport.setClickable(true);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.reportBar.startAnimation(this.slideOutTop);
    }

    @OnClick({R.id.send_report})
    public void onReportButtonClick() {
        if (TextUtils.isEmpty(this.reason)) {
            this.reason = getResources().getString(R.string.report_option_1);
        }
       hideReportBar();
        // TODO: 05/08/2016 firebase stuff
      //showResultBar(status, R.string.report_status_success, R.string.report_status_failure);
    }



    private void showResultBar(final boolean status, final int successText, final int failureText) {
        this.slideInTop.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                SelfiPreviewScreen.this.resultBar.setText(status ? getText(successText) : getText(failureText));
            }

            public void onAnimationEnd(Animation animation) {
                resultBar.setVisibility(0);
                new Handler().postDelayed(() -> hideResultBar(), 1000);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.resultBar.startAnimation(this.slideInTop);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.option_1 :
                if (checked) {
                    this.reason = getResources().getString(R.string.report_option_1);
                    return;
                }
                return;
            case R.id.option_2 :
                if (checked) {
                    this.reason = getResources().getString(R.string.report_option_2);
                    return;
                }
                return;
            default:
        }
    }

    private void hideResultBar() {
        this.slideOutTop.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                resultBar.setVisibility(View.INVISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.resultBar.startAnimation(this.slideOutTop);
    }

    @Override
    public void liked(LikeButton likeButton) {
        if (SessionManager.isAnonymous()){
            likeButton.setLiked(false);
            showJoinUsDialog();
            return;
        }
        FireRequests.likeSelfi(selfiImage.getId(), true, new SelfiListener() {
            @Override
            public void onSuccess(Object something) {
                likeButton.setLiked((Boolean) something);
                likesCount.setText(((Boolean) something)?""+(Integer.valueOf(likesCount.getText().toString())+1):
                        ""+(Integer.valueOf(likesCount.getText().toString())-1));
            }

            @Override
            public void onError(String errDetail) {

            }
        });
    }

    private void showJoinUsDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Not Authorized")
                .setContentText("You must be logged in to like selfi !")
                .setConfirmText(getString(R.string.join_us))
                .setConfirmClickListener(sweetAlertDialog -> {
                    ScreenManager.launchProfileScreen(this,true);
                    sweetAlertDialog.cancel();
                })
                .show();
    }

    @Override
    public void unLiked(LikeButton likeButton) {
        FireRequests.likeSelfi(selfiImage.getId(), false, new SelfiListener() {
            @Override
            public void onSuccess(Object something) {
                likeButton.setLiked((Boolean) something);
                likesCount.setText(((Boolean) something)?""+(Integer.valueOf(likesCount.getText().toString())+1):
                        ""+(Integer.valueOf(likesCount.getText().toString())-1));
            }

            @Override
            public void onError(String errDetail) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case NavConstants.JOIN_NOW_REQUEST_CODE:
                    likeBtn.setLiked(true);
                    Logger.forDebug("user returned from profile screen as logged in");
                    break;

            }
        }
    }

    @OnClick(R.id.share)void shareSelfi() {
       /* Bitmap icon = null;
        try {
            icon = new ImageUtil.BitmapTask().execute(selfiImage.getImage()).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TITLE,"Make your awesome selfi with #Selfi_king!");
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
        startActivity(Intent.createChooser(share, "Share Image"));*/
        createShareIntent();
    }

}
