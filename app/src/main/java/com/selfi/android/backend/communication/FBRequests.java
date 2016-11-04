package com.selfi.android.backend.communication;

import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.selfi.android.backend.callbacks.SelfiListener;
import com.selfi.android.constants.FBConstants;
import com.selfi.android.constants.FireConstants;
import com.selfi.android.data.model.SelfiUser;
import com.selfi.android.manager.FireManager;
import com.selfi.android.manager.SessionManager;
import com.selfi.util.Logger;

import org.json.JSONException;

/**
 * Created by Ahmed on 05/08/2016.
 */
public class FBRequests {

    //    To solve firebase FB email problem
    public static void saveNewUser(final SelfiListener userSavedListener) {

        FireManager.getUsersNode()
                .child(SessionManager.getUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Logger.forDebug(dataSnapshot.toString());
                        if (!dataSnapshot.exists()){// User not exist !
                            Logger.forDebug("User not exist , saving new custom user .....");
                            getFBDetailsAndSaveUser(userSavedListener);
                        }else {
                            userSavedListener.onSuccess(dataSnapshot.getValue(SelfiUser.class));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private static void getFBDetailsAndSaveUser(final SelfiListener userSavedListener) {
        Logger.forDebug("getFBDetailsAndSaveUser BEGIN");
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), (jObject, response) -> {
            String email = SessionManager.getUser().getEmail();
            String imgUrl  = null;
            String fbId = AccessToken.getCurrentAccessToken().getUserId();
            String cover=null;
            String name = jObject.optString("name");
            try {
                email = AccessToken.getCurrentAccessToken().getUserId()+"@facebook.com";
                if (jObject.has(FBConstants.EMAIL)) {
                    email=jObject.getString(FBConstants.EMAIL);
                }
                imgUrl = (FBConstants.GRAPH_URL + "/" + fbId + "/" + "picture?height=" + FBConstants.IMG_PARAMS_WIDTH + "&width=" + FBConstants.IMG_PARAMS_HEIGHT);
                SessionManager.getUser().updateEmail(email);
                cover = jObject.getJSONObject(FBConstants.COVER).optString(FBConstants.COVER_SOURCE);
            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                SelfiUser selfiUser = new SelfiUser(fbId,name,email,imgUrl,cover,FireConstants.INITIAL_POINTS);
                FireRequests.saveCustomUser(selfiUser,userSavedListener);
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString(FBConstants.FIELDS_SYMBOL, FBConstants.ME_FIELDS);
        request.setParameters(parameters);
        request.executeAsync();
    }
}
