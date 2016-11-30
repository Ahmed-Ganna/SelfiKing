package com.selfi.android.backend.communication;

import android.net.Uri;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.selfi.android.backend.callbacks.SelfiListener;
import com.selfi.android.constants.FireConstants;
import com.selfi.android.data.model.SelfiImage;
import com.selfi.android.data.model.SelfiUser;
import com.selfi.android.manager.FireManager;
import com.selfi.android.manager.SessionManager;
import com.selfi.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ahmed on 01/08/2016.
 */

public class FireRequests {



    public static void obtainImages(final SelfiListener listener){
        FireManager.getCompetitionImagesNode()
                .orderByChild(FireConstants.LIKES_NODE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Logger.forDebug(dataSnapshot.toString());
                        if (dataSnapshot.exists()){
                            listener.onSuccess(getImagesFromSnap(dataSnapshot));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onError(databaseError.getDetails());
                    }
                });
    }



    public static void getSelfiUser(String id,final SelfiListener listener){
        FireManager.getUsersNode()
                .child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Logger.forDebug(dataSnapshot.toString());
                        if (dataSnapshot.exists()){
                            listener.onSuccess(dataSnapshot.getValue(SelfiUser.class));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onError(databaseError.getDetails());
                    }
                });
    }

    public static void getUserImages(final SelfiListener listener){
            FireManager.getCompetitionImagesNode()
                    .orderByChild(FireConstants.AUTHOR_ID)
                    .equalTo(SessionManager.getUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            listener.onSuccess(getImagesFromSnap(dataSnapshot));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            listener.onError(databaseError.getDetails());
                        }
                    });
    }

    private static ArrayList<SelfiImage> getImagesFromSnap(DataSnapshot dataSnapshot){
        ArrayList<SelfiImage> images = new ArrayList<>();
        if (dataSnapshot.exists()){
            for (DataSnapshot imageSnap : dataSnapshot.getChildren()){
                SelfiImage image = imageSnap.getValue(SelfiImage.class);
                image.setId(imageSnap.getKey());
                images.add(image);
                Logger.forDebug("Selfi retrieved : "+image.getId());
            }
        }
        return images;
    }
    public static void getAuthorInfo(String authorId,final SelfiListener listener){
        FireManager.getUsersNode()
                .child(authorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            listener.onSuccess(dataSnapshot.getValue(SelfiUser.class));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onError(databaseError.getDetails());
                    }
                });
    }




    public static void likeSelfi(String selfiId,boolean islike,SelfiListener listener){
        DatabaseReference child = FireManager.getImagesLikesNode()
                .child(selfiId)
                .child(SessionManager.getUser().getUid());
        if (islike){
            child.setValue(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Logger.forDebug("user - image linking created");
                    listener.onSuccess(true);
                }else {
                    listener.onError(task.getException().getMessage());
                }
            });
        }else {
            child.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Logger.forDebug("user - image linking removed");
                    listener.onSuccess(false);
                }else {
                    listener.onError(task.getException().getMessage());
                }
            });
        }
        FireManager.getCompetitionImagesNode()
                .child(selfiId)
                .child(FireConstants.LIKES_NODE)
                .runTransaction(new Transaction.Handler() { // update selfi likes after new like process
                    @Override
                    public Transaction.Result doTransaction(MutableData currentData) {
                        if (currentData.getValue()==null){
                            currentData.setValue(0);
                        }else {
                            Logger.forDebug("current likes "+currentData);
                            currentData.setValue(islike?((Double)currentData.getValue())+1:((Double)currentData.getValue())-1);
                        }

                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        Logger.forDebug(islike?"likes increased by 1":"likes decreased by 1");
                    }
                });
    }



    static void saveCustomUser(final SelfiUser user, final SelfiListener listener) {
        FireManager.getUsersNode().child(SessionManager.getUser().getUid()).setValue(user).addOnSuccessListener(aVoid -> {
            Logger.forDebug("Custom user saved successfully id : "+SessionManager.getUser().getUid());
            listener.onSuccess(user);
        }).addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public static void getCustomUser(String id,final SelfiListener listener) {
        FireManager.getUsersNode()
                .child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            listener.onSuccess(dataSnapshot.getValue(SelfiUser.class));
                        }else {
                            listener.onError(Logger.NOT_EXIST_ERROR);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onError(databaseError.getMessage());
                    }
                });
    }

    public static void uploadNewSelfi(Uri imageUri,Double color,String hashTag,boolean isHashExist, SelfiListener listener) {
        Logger.forDebug("uploadNewSelfi: uploading");
        FireManager.getSelfiStorage()
                .child(System.currentTimeMillis()+"_"+imageUri.getLastPathSegment())
                .putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                    String imgUrl = taskSnapshot.getMetadata().getDownloadUrl().toString();
                    FireManager.getCompetitionImagesNode()
                            .push()
                            .setValue(new SelfiImage(imgUrl,color,SessionManager.getUser().getUid(),hashTag))
                            .addOnSuccessListener(aVoid -> {
                                Logger.forDebug("uploadNewSelfi Success: image uploaded url: "+imgUrl);
                                listener.onSuccess(null);
                            }).addOnFailureListener(e -> {
                                Logger.forDebug("uploadNewSelfi: uploading failed");
                                listener.onError(e.getMessage());
                            });

                }).addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
        });
        if (!isHashExist){
            FireManager.getHashTagsNode()
                    .push()
                    .setValue(hashTag);
        }
    }

    public static void obtainHashtags(SelfiListener listener) {
        List<String> hashTags = new ArrayList<>();
        FireManager.getHashTagsNode()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Logger.forDebug(dataSnapshot.toString());
                            for (DataSnapshot hashTag : dataSnapshot.getChildren()){
                                hashTags.add((String) hashTag.getValue());
                            }
                        }
                        listener.onSuccess(hashTags);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onError(databaseError.getDetails());
                    }
                });
    }

    public static void getSelfiLikeStatus(String id,SelfiListener listener) {
        Logger.forDebug("like status for :"+id );
        FireManager.getImagesLikesNode()
                .child(id)
                .child(SessionManager.getUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Logger.forDebug(dataSnapshot.toString());
                        if (dataSnapshot.exists()){
                            listener.onSuccess(true);
                        }else {
                            listener.onSuccess(false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onError(databaseError.getMessage());
                    }
                });
    }

    public static void beginCompetition(SelfiListener listener) {
        setExpired(true);
    }

    public static void endCompetition(SelfiListener listener) {
        FireManager.getExpiredNode().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.getValue(Boolean.class)){
                    FireManager.getCompetitionImagesNode()
                            .orderByChild(FireConstants.LIKES_NODE)
                            .limitToLast(3)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    List<SelfiImage> selfiImages = getImagesFromSnap(dataSnapshot);
                                    Collections.sort(selfiImages);
                                    setResults(selfiImages);
                                    setExpired(true);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Logger.forDebug(databaseError.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private static void setExpired(boolean isExpired) {
        FireManager.getExpiredNode().setValue(isExpired);
    }

    private static void setResults(List<SelfiImage> selfiImages) {
        for (int i = 0; i < 3; i++) {
            FireManager.getCompetitionResultsNode()
                    .push().setValue(selfiImages.get(i));
        }
    }

    public static void getResultUsers(List<SelfiImage> results,SelfiListener listener) {
        List<SelfiUser> users = new ArrayList<>();
        for (SelfiImage selfiImage : results){
            FireRequests.getSelfiUser(selfiImage.getAuthorId(), new SelfiListener() {
                @Override
                public void onSuccess(Object user) {
                    users.add((SelfiUser) user);
                }

                @Override
                public void onError(String errDetail) {

                }
            });
        }
    }
}
