package com.selfi.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.selfi.android.manager.ScreenManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ahmed on 07/08/2016.
 */

public class ImageUtil {

    private static Uri decodeSampledBitmapFromUri(String path,
                                                  int reqWidth, int reqHeight) throws IOException {


        // First decode with inJustDecodeBounds=true to check dimensions
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(path),null, options);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        File destination = new File(ImagesUtil.getOutputMediaUri().getPath());
        FileOutputStream fo;
        fo = new FileOutputStream(destination);
        fo.write(bytes.toByteArray());
        fo.close();
        return Uri.fromFile(destination);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static class CameraImageTask extends AsyncTask<String, Void, Uri> {

        private Activity context;
        public CameraImageTask(Activity context) {
            this.context = context;
        }

        @Override
        protected Uri doInBackground(String... params) {
            Log.d("Imagetask", "doInBackground: Processing image");
            try {
                return decodeSampledBitmapFromUri(params[0],300, 100);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Uri uri) {
            ScreenManager.launchImageEditorScreen(context,uri);
        }
    }

    public static class BitmapTask extends AsyncTask<String,Void,Bitmap>{

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                // Log exception
                return null;
            }
        }
    }
}
