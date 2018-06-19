package com.example.android.medjour.ui.journaling;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.medjour.R;
import com.example.android.medjour.databinding.FragmentReviewBinding;
import com.example.android.medjour.utils.UiUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewFragment extends Fragment {

    FragmentReviewBinding reviewBinding;

    public ReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        reviewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_review, container,
                false);
        View root = reviewBinding.getRoot();

        //gradually change background color from indigo back to background color,
        // as a slow "release" from the meditation state
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int endColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);

            int startColor = ContextCompat.getColor(getActivity(), R.color.indigo);
            UiUtils.changeBackground(root, startColor, endColor);
        }

        return root;
    }

}
