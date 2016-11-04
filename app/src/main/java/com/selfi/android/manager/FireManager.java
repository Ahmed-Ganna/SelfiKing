package com.selfi.android.manager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.selfi.android.constants.FireConstants;

/**
 * Created by Ahmed on 01/08/2016.
 */

public class FireManager {

    public static DatabaseReference getCompetitionImagesNode(){
        return FirebaseDatabase.getInstance().getReference().child(FireConstants.COMPETITION_IMAGES_NODE);
    }

    public static DatabaseReference getCompetitionInfoNode(){
        return FirebaseDatabase.getInstance().getReference().child(FireConstants.COMPETITION_INFO_NODE);
    }

    public static DatabaseReference getUserUploadedImagesNode(){
        return FirebaseDatabase.getInstance().getReference().child(FireConstants.USER_IMAGES_UPLOADED_NODE);
    }

    public static DatabaseReference getImagesLikesNode(){
        return FirebaseDatabase.getInstance().getReference().child(FireConstants.IMAGE_LIKE_NODE);
    }

    public static DatabaseReference getCompetitionResultsNode(){
        return FirebaseDatabase.getInstance().getReference().child(FireConstants.COMPETITION_RESULTS_NODE);
    }

    public static DatabaseReference getUsersNode(){
        return FirebaseDatabase.getInstance().getReference().child(FireConstants.USERS_NODE);
    }

    public static DatabaseReference getExpiredImages(){
        return FirebaseDatabase.getInstance().getReference().child(FireConstants.EXPIRED_IMAGES_NODE);
    }

    public static FirebaseAuth getFirebaseAuth(){
        return FirebaseAuth.getInstance();
    }

    public static StorageReference getSelfiStorage() {
        return FirebaseStorage.getInstance().getReference().child(FireConstants.SELFI_STORAGE_FOLDER);
    }

    public static DatabaseReference getHashTagsNode() {
        return FirebaseDatabase.getInstance().getReference().child(FireConstants.HASHTAGS_NODE);
    }

    public static DatabaseReference getExpiredNode() {
        return FirebaseDatabase.getInstance().getReference().child(FireConstants.EXPIRED_STATUES_NODE);
    }
}
