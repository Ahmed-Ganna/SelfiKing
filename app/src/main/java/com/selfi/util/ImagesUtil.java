package com.selfi.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Ahmed on 08/08/2016.
 */

public class ImagesUtil {
    private static final String FILE_NAME = "king.jpg";
    private static final String IMAGE_DIRECTORY_NAME = "selfiking";


    public static Uri getOutputMediaUri() {

        File mediaStorageDir = getImageFile();

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator+FILE_NAME)) ;
    }

    private static File getImageFile(){
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);
    }

    private void compressCapturedImage() {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(getOutputMediaUri().getPath(),
                    options);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

            File  destination = getImageFile();
            FileOutputStream fo;
                destination.createNewFile();
                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();
                Log.d("Logger", "compressCaptureImage: "+destination.getPath());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
