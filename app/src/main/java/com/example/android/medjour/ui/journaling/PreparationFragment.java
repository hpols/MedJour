package com.example.android.medjour.ui.journaling;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.medjour.R;
import com.example.android.medjour.databinding.FragmentPreparationBinding;
import com.example.android.medjour.utils.JournalUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreparationFragment extends Fragment {

    private long prepStartTime;
    private FragmentPreparationBinding prepBinder;
    private toMeditationCallback meditationCallback;
    private final String PREP_START_TIME = "preparation start time key";

    public interface toMeditationCallback {
        void toMeditation(long preparationTime, boolean prepTimeLimitReached);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(PREP_START_TIME)) {
            prepStartTime = savedInstanceState.getLong(PREP_START_TIME);
        }

        prepBinder.preparationStartBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long prepTime = JournalUtils.getNow() - prepStartTime;
                boolean prepTimeLimitReached =false;
                //we can only log a certain amount of minutes for review, as per the C.MI. regulations
                if (prepTime > JournalUtils.MAX_PREP_TIME) {
                    prepTime = JournalUtils.MAX_PREP_TIME;
                    prepTimeLimitReached = true;
                }
                meditationCallback.toMeditation(prepTime, prepTimeLimitReached);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        prepBinder = DataBindingUtil.inflate(inflater, R.layout.fragment_preparation, container,
                false);
        View root = prepBinder.getRoot();

        if(savedInstanceState == null) {
            prepStartTime = JournalUtils.getNow();
        }

        //gradually change background color to indigo, in preparation for the meditation
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int endColor = ContextCompat.getColor(getActivity(), R.color.indigo);

            int startColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);
            JournalUtils.changeBackground(root, startColor, endColor, JournalUtils.PREP_FLAG);
        }

        JournalUtils.setRingerMode(getActivity(), JournalUtils.NOT_SILENCE);
        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(PREP_START_TIME, prepStartTime);
    }
}
