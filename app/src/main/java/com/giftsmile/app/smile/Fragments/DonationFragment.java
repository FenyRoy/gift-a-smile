package com.giftsmile.app.smile.Fragments;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.giftsmile.app.smile.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class DonationFragment extends Fragment {

    public TextView textView;


    public DonationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_donations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textView=view.findViewById(R.id.pageblank);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LaunchWeb("http://gift-smile.herokuapp.com/");

            }
        });

    }

    private void LaunchWeb(String url) {

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
    }
}
