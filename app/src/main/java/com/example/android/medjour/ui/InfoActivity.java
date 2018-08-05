package com.example.android.medjour.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.android.medjour.R;
import com.example.android.medjour.databinding.ActivityInfoBinding;
import com.example.android.medjour.utils.JournalUtils;

public class InfoActivity extends AppCompatActivity {

    private ActivityInfoBinding infoBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infoBinding = DataBindingUtil.setContentView(this, R.layout.activity_info);

        String displayText;
        String appBarTitle;
        if (JournalUtils.getSharedPrefBoo(this, JournalUtils.BOO_STUDENT)) {
            displayText = getString(R.string.guideline_text);
            appBarTitle = getString(R.string.menu_guidelines);
        } else {
            displayText = getString(R.string.info_text);
            appBarTitle = getString(R.string.menu_information);
        }
        infoBinding.infoTv.setText(displayText);

        setSupportActionBar(infoBinding.infoAppbar.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(appBarTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
