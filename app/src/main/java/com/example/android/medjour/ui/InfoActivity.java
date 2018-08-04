package com.example.android.medjour.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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
        if(JournalUtils.getSharedPrefBoo(this, JournalUtils.BOO_STUDENT)) {
            displayText = getString(R.string.guideline_text);
        } else {
            displayText = getString(R.string.info_text);
        }
        infoBinding.infoTv.setText(displayText);

    }
}
