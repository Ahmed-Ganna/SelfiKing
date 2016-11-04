package com.selfi.android.manager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Ahmed on 01/07/2016.
 */
public class SessionManager {

    public static FirebaseUser getUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static boolean isAnonymous() {
        return FirebaseAuth.getInstance().getCurrentUser().isAnonymous();
    }
}
