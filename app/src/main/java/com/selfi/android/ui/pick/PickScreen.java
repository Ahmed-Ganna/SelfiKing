package com.selfi.android.ui.pick;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.adobe.creativesdk.aviary.AdobeImageIntent;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.selfi.android.R;
import com.selfi.android.backend.callbacks.SelfiListener;
import com.selfi.android.backend.communication.FireRequests;
import com.selfi.android.constants.NavConstants;
import com.selfi.android.manager.ScreenManager;
import com.selfi.android.manager.SessionManager;
import com.selfi.util.BaseActivity;
import com.selfi.util.ImageUtil;
import com.selfi.util.Logger;
import com.selfi.util.PermissionUtility;
import com.selfi.util.picasso.GrayscaleCircleTransform;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import me.crosswall.photo.pick.PickConfig;

import static com.selfi.android.constants.NavConstants.REQUEST_IMAGE_CAPTURE;
import static com.selfi.util.FileUtil.getOutputMediaUri;

public class PickScreen extends BaseActivity implements FABProgressListener {

    @BindView(R.id.menu_pick)FloatingActionMenu pickMenu;
    @BindView(R.id.fab_camera)FloatingActionButton cameraBtn;
    @BindView(R.id.fab_gallary)FloatingActionButton galleryBtn;
    @BindView(R.id.avatar_img)ImageView avatar;
    @BindView(R.id.editedImageView)ImageView editedImageView;
    @BindView(R.id.hashtag_edit_text)AutoCompleteTextView hashTagEd;
    @BindView(R.id.fabProgressCircle)FABProgressCircle uploadProgressFab;
    @BindView(R.id.loading)CircularProgressBar loader;
    @BindView(R.id.reset_btn)ImageView resetImg;
    private Uri editedImgUri;
    private int colorPallet;
    private boolean taskRunning;
    private List<String> hashTags;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);
        ButterKnife.bind(this);
        setupToolbar(false);
        prepareHashTags();
        updateUserDetails();
        uploadProgressFab.attachListener(this);
    }

    private void updateUserDetails() {
        if (!SessionManager.isAnonymous()){
            Picasso.with(this)
                    .load(SessionManager.getUser().getPhotoUrl())
                    .transform(new GrayscaleCircleTransform())
                    .placeholder(R.drawable.ic_profile)
                    .into(avatar);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case NavConstants.EDITOR_FOR_RESULT_REQUEST_CODE: // photo edited - aviary editor
                    editedImgUri = data.getParcelableExtra(AdobeImageIntent.EXTRA_OUTPUT_URI);
                    Logger.forDebug(editedImgUri.getPath());
                    setPalletColor();
                    updateUi();
                    break;

                case REQUEST_IMAGE_CAPTURE: // photo captured  - camera
                    new ImageUtil.CameraImageTask(this).execute(getOutputMediaUri().getPath());
                    break;

                case PickConfig.PICK_REQUEST_CODE: // photo picked  - gallery
                    String picked = data.getStringArrayListExtra(PickConfig.EXTRA_STRING_ARRAYLIST).get(0);
                    Logger.forDebug("picked path"+picked);
                    ScreenManager.launchImageEditorScreen(PickScreen.this,Uri.parse(picked));
                    break;

                case NavConstants.JOIN_NOW_REQUEST_CODE:

            }
        }
    }

    private void setPalletColor() {
        new Handler().post(() -> {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), editedImgUri);
                Palette.from( bitmap ).generate(palette -> {
                    int defaultColor = 0x000000;
                    if (palette.getVibrantSwatch()!=null){
                        colorPallet = palette.getVibrantColor(defaultColor);
                        return;

                    }else if (palette.getDarkVibrantSwatch()!=null){
                        colorPallet = palette.getDarkVibrantColor(defaultColor);
                        return;
                    }
                    else if (palette.getMutedSwatch()!=null){
                        colorPallet = palette.getMutedColor(defaultColor);
                        return;
                    }else if (palette.getDarkMutedSwatch()!=null){
                        colorPallet = palette.getDarkMutedColor(defaultColor);
                        return;
                    }
                    Logger.forDebug("pallet :"+ colorPallet);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private void updateUi() {
        editedImageView.setImageURI(editedImgUri);
        toggleFab(true);
    }

    @OnClick(R.id.fab_camera)void pickFromCamera(){
        pickMenu.close(true);
        boolean isPermissionGranted = PermissionUtility.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (isPermissionGranted)
        ScreenManager.launchCameraForPhoto(this,getOutputMediaUri());
    }
    @OnClick(R.id.fab_gallary)void pickFromGallery(){
        pickMenu.close(true);
        ScreenManager.launchPhotoPicker(this);
    }


    private void uploadImage() {
        String hashTag = hashTagEd.getText().toString();
        FireRequests.uploadNewSelfi(editedImgUri, (double) colorPallet, TextUtils.isEmpty(hashTag)?"Selfi":hashTag,isHashTagExist(hashTag), new SelfiListener() {
            @Override
            public void onSuccess(Object something) {
                hideFloatingProgress(false);
                resetImg();
                toggleFab(false);
                Toast.makeText(PickScreen.this, "Selfi uploaded successfully :)", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errDetail) {
                hideFloatingProgress(true);
                Toast.makeText(PickScreen.this, "Error occurred :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isHashTagExist(String hashTag) {
        return hashTags!=null&&hashTags.contains(hashTag);
    }


    private void prepareHashTags(){
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        FireRequests.obtainHashtags(new SelfiListener() {
            @Override
            public void onSuccess(Object hashTags) {
                PickScreen.this.hashTags = (List<String>) hashTags;
                hashTagEd.setAdapter(new ArrayAdapter<>(PickScreen.this, layoutItemId, (List<String>) hashTags));
            }

            @Override
            public void onError(String errDetail) {

            }
        });
    }

    @OnClick(R.id.upload_fab)void onUploadFabClick(){
        if (SessionManager.isAnonymous()){
            showJoinUsDialog();
        }else {
            if (!taskRunning) {
                uploadProgressFab.show();
                taskRunning = true;
                uploadImage();
            }
        }
    }

    @OnClick(R.id.reset_btn)void resetImg(){
        editedImageView.setImageResource(R.drawable.page_bg);
        toggleFab(false);
    }

    @Override
    public void onFABProgressAnimationEnd() {
        Snackbar.make(uploadProgressFab, "upload complete", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
    }

    private void hideFloatingProgress(boolean isEroor){
        taskRunning = false;
        if (isEroor){
            uploadProgressFab.hide();
        }else {
            uploadProgressFab.beginFinalAnimation();
        }
    }

    private void toggleFab(boolean isImageEdited){
        if (isImageEdited){
            uploadProgressFab.setVisibility(View.VISIBLE);
            pickMenu.setVisibility(View.GONE);
            resetImg.setVisibility(View.VISIBLE);
        }else {
            uploadProgressFab.setVisibility(View.GONE);
            pickMenu.setVisibility(View.VISIBLE);
            resetImg.setVisibility(View.GONE);
        }
    }

    private void showJoinUsDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Not Authorized")
                .setContentText("u must be logged in to like selfi !")
                .setConfirmText(getString(R.string.join_us))
                .setConfirmClickListener(sweetAlertDialog -> ScreenManager.launchProfileScreen(this,true))
                .show();
    }
}