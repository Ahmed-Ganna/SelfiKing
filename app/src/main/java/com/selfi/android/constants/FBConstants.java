package com.selfi.android.constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ahmed on 05/08/2016.
 */
public class FBConstants {

    public static final List<String> permissions = new ArrayList<String>() {{
        add("public_profile");
        add("email");
    }};

    public static final String FIELDS_SYMBOL = "fields";
    public static final String ME_FIELDS = "email,cover,name";
    public static final String EMAIL = "email";
    public static final int IMG_PARAMS_WIDTH = 600;
    public static final String GRAPH_URL = "https://graph.facebook.com";
    public static final int IMG_PARAMS_HEIGHT = 600;
    public static final String COVER = "cover";
    public static final String COVER_SOURCE = "source";
}
