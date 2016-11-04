package com.selfi.util;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.selfi.android.R;

/**
 * Created by Ahmed on 05/08/2016.
 */

public class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;


    protected void setupToolbar(boolean hasBackBtn) {
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.drawable.ic_logo_actionbar);
        if (hasBackBtn){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }
}
