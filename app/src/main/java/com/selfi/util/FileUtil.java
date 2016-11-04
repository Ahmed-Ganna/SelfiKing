package com.selfi.util;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Ahmed on 09/08/2016.
 */

public class FileUtil {

    private static final String FILE_NAME = "king.jpg";
    private static final String IMAGE_DIRECTORY_NAME = "selfiking";

    public static Uri getOutputMediaUri() {
        File mediaStorageDir = getImageFolder();
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator+FILE_NAME));
    }

    private static File getImageFolder(){
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);
    }
}
