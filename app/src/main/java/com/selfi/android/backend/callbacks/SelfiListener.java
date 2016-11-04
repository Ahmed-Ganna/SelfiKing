package com.selfi.android.backend.callbacks;

/**
 * Created by Ahmed on 01/08/2016.
 */

public interface SelfiListener {

    void onSuccess(Object something);
    void onError(String errDetail);
}
