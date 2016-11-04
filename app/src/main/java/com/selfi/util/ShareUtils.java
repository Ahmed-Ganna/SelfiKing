package com.selfi.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class ShareUtils {

    public static ShareUtils instance;
    private Context context;

    public static ShareUtils getInstance(Context context) {
        if (instance == null) {
            instance = new ShareUtils(context);
        }
        return instance;
    }

    private ShareUtils(Context context) {
        this.context = context;
    }

    public Intent shareImage(ImageView image) {
        Intent shareIntent = new Intent("android.intent.action.SEND");
        shareIntent.setType("image/jpg");
        shareIntent.setData(getLocalBitmapUri(image));
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "#Selfi_king");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Make your awesome selfi with us");
        return shareIntent;
    }

    private Uri getLocalBitmapUri(ImageView imageView) {
        if (!(imageView.getDrawable() instanceof BitmapDrawable)) {
            return null;
        }
        Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Uri bmpUri = null;
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(CompressFormat.PNG, 90, out);
            out.close();
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return bmpUri;
        }
    }

}
