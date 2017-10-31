package com.rpereira.killtheapp.layout;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rpereira.killtheapp.R;
import com.rpereira.killtheapp.model.Process;

/**
 * ProcessFragment
 * @author rpereira
 * @since 07/02/2017
 */
public class ProcessFragment extends Fragment {

    private Process process;

    public ProcessFragment() {
        // Required empty public constructor
    }

    public static ProcessFragment newInstance(Process process) {
        ProcessFragment fragment = new ProcessFragment();
        fragment.process = process;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_process, container, false);

        ImageView imageView = (ImageView) view.findViewById(R.id.item_icon);
        imageView.setImageDrawable(this.process.getIcon());

        return view;
    }

}
