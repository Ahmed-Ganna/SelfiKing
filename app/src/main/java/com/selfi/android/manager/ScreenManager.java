package com.selfi.android.manager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.adobe.creativesdk.aviary.AdobeImageIntent;
import com.selfi.android.R;
import com.selfi.android.constants.NavConstants;
import com.selfi.android.data.model.SelfiImage;
import com.selfi.android.ui.pick.PickScreen;
import com.selfi.android.ui.profile.ProfileScreen;
import com.selfi.android.ui.selfi.SelfiPreviewScreen;
import com.selfi.android.ui.selfi.SelfiScreen;

import me.crosswall.photo.pick.PickConfig;

/**
 * Created by Ahmed on 31/07/2016.
 */

public class ScreenManager {

    public static void launchSeliScreenWithExtra(String compType, Activity context){
        Intent i = new Intent(context, SelfiScreen.class);
        i.putExtra(NavConstants.COMPETITION_EXTRA,compType);
        context.startActivity(i);
    }

    public static void launchProfileScreen(Activity activity,boolean isJoinNow) {
        Intent i = new Intent(activity, ProfileScreen.class);
        i.putExtra(NavConstants.SIGNUP_NOW,isJoinNow);
        if (isJoinNow){
            activity.startActivityForResult(i,NavConstants.JOIN_NOW_REQUEST_CODE);
        }else {
            activity.startActivity(i);
        }
        activity.overridePendingTransition(R.anim.aviary_slide_in_right, R.anim.aviary_slide_out_left);
    }


    public static void launchSelfiPreviewScreen(Activity activity, SelfiImage selfiImage) {
        Intent i = new Intent(activity, SelfiPreviewScreen.class);
        i.putExtra(NavConstants.SELFIPREVIEW,selfiImage);
        activity.startActivity(i);
        activity.overridePendingTransition(R.anim.aviary_slide_in_right, R.anim.aviary_slide_out_left);
    }

    public static void launchImageEditorScreen(Activity activity, Uri imageUri) {
        Intent imageEditorIntent = new AdobeImageIntent.Builder(activity)
                .setData(imageUri)
                .withAccentColor(activity.getResources().getColor(R.color.colorAccent))
                .withOutputFormat(Bitmap.CompressFormat.JPEG)
                .withOutputQuality(90)
                .build();

        activity.startActivityForResult(imageEditorIntent, NavConstants.EDITOR_FOR_RESULT_REQUEST_CODE);
    }

    public static void launchCameraForPhoto(Activity activity,Uri outputFileUri) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(takePictureIntent, NavConstants.REQUEST_IMAGE_CAPTURE);
        }
    }

    public static void launchPhotoPicker(Activity activity) {
        new PickConfig.Builder(activity)
                .pickMode(PickConfig.MODE_SINGLE_PICK)
                //.maxPickSize(30)
                .spanCount(3)
                .toolbarColor(R.color.colorPrimary)
                .build();
    }

    public static void launchPicScreen(Activity activity) {
        activity.startActivity(new Intent(activity, PickScreen.class));
    }
}
