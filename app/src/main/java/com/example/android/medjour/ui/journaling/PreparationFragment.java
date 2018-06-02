package com.example.android.medjour.ui.journaling;


import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.medjour.R;
import com.example.android.medjour.databinding.FragmentPreparationBinding;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreparationFragment extends Fragment {

    long startTime;
    FragmentPreparationBinding prepBinder;
    StartButtonCallback startCallback;

    public interface StartButtonCallback {
        void onClick(long preparationTime);
    }

    public PreparationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            startCallback = (StartButtonCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement StartButtonCallback");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        prepBinder = DataBindingUtil.inflate(inflater, R.layout.fragment_preparation, container,
                false);
        View root = prepBinder.getRoot();

        getTimeStamp();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            changeBackground(root);
        }

        prepBinder.preparationStartBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long clickTime = Calendar.getInstance().getTimeInMillis();
                startCallback.onClick(clickTime - startTime);
            }
        });
        return root;
    }

    private void getTimeStamp() {
        startTime = Calendar.getInstance().getTimeInMillis();
    }

    private void changeBackground(View root) {
        int MAX_MINS = 5; //minutes
        long MAX_TIME = TimeUnit.MINUTES.toMillis(MAX_MINS);

        int endColor = ContextCompat.getColor(getActivity(), R.color.indigo);

        int startColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);

        ObjectAnimator colorFade = ObjectAnimator.ofObject(root, "backgroundColor",
                new ArgbEvaluator(), startColor, endColor);
        colorFade.setDuration(MAX_TIME);
        colorFade.start();
    }

}
