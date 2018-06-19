package com.example.android.medjour.ui.journaling;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.medjour.R;
import com.example.android.medjour.utils.UiUtils;
import com.example.android.medjour.databinding.FragmentPreparationBinding;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreparationFragment extends Fragment {

    static long prepStartTime;
    FragmentPreparationBinding prepBinder;
    toMeditationCallback meditationCallback;

    public interface toMeditationCallback {
        void toMeditation(long preparationTime);
    }

    public PreparationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This ensures that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            meditationCallback = (toMeditationCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement toMeditationCallback");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        prepBinder = DataBindingUtil.inflate(inflater, R.layout.fragment_preparation, container,
                false);
        View root = prepBinder.getRoot();

        UiUtils.getTimeStamp(prepStartTime);

        //gradually change background color to indigo, in preparation for the meditation
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int endColor = ContextCompat.getColor(getActivity(), R.color.indigo);

            int startColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);
            UiUtils.changeBackground(root, startColor, endColor);
        }

        prepBinder.preparationStartBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long clickTime = Calendar.getInstance().getTimeInMillis();
                meditationCallback.toMeditation(clickTime - prepStartTime);
            }
        });
        return root;
    }


}
