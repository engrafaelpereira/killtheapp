package com.rpereira.killtheapp.layout;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.rpereira.killtheapp.R;
import com.rpereira.killtheapp.activity.SettingsActivity;
import com.rpereira.killtheapp.adapter.ProcessListRecycleViewAdapter;
import com.rpereira.killtheapp.model.Process;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * ProcessListFragment
 *
 * @author rpereira
 * @since 16/01/17.
 *
 */
public class ProcessListFragment extends Fragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = ProcessListFragment.class.getSimpleName();

    private RecyclerView processListRecyclerView;
    private ProcessListRecycleViewAdapter processListRecyclerViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isListAll = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_process_list, container, false);

        initRefreshButton(view);
        initSwipeRefreshLayout(view);
        initProcessListRecyclerView(view);
        initSharedPreferences();

        return view;
    }

    private void initSharedPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPref.registerOnSharedPreferenceChangeListener(this);
        this.isListAll = sharedPref.getBoolean(SettingsActivity.KEY_LIST_ALL, true);
    }

    private void initProcessListRecyclerView(View view) {
        this.processListRecyclerView = (RecyclerView) view.findViewById(R.id.processListRecyclerView);
        this.processListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.processListRecyclerViewAdapter = new ProcessListRecycleViewAdapter(setViewListAction());
        this.processListRecyclerView.setAdapter(processListRecyclerViewAdapter);
    }

    private void initSwipeRefreshLayout(View view) {
        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listProcess();
            }
        });
    }

    private void initRefreshButton(View view) {
        ImageButton refreshButton = (ImageButton) view.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                listProcess();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        listProcess();
    }

    private View.OnClickListener setViewListAction() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = processListRecyclerView.getChildLayoutPosition(v);
                Process process = processListRecyclerViewAdapter.get(position);
                setKillProcessAlertDialog(processListRecyclerView, process);
            }
        };
    }

    private void setKillProcessAlertDialog(final View view, final Process process) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Alert")
                .setMessage("Do you really want to kill " + process.getName() + "?\n[pid: " + process.getPid() + "]")
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.i(TAG, process.getPid() + ":" + process.getPackageName());
                        if (killProcess(process)) {
                            Snackbar.make(view, "Kill the app: " + process.getName(), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            listProcess();
                        } else {
                            Snackbar.make(view, "Impossible to kill: " + process.getName(), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void listProcess() {
        List<AndroidAppProcess> runningAppProcesses = AndroidProcesses.getRunningAppProcesses();
        final PackageManager pm = getActivity().getPackageManager();

        List<Process> newProcessList = new ArrayList<>();
        try {
            for (AndroidAppProcess runningProcess : runningAppProcesses) {
                Drawable icon = getAppIcon(pm, runningProcess);
                if (isListAll || icon != null) {
                    String name = getProcessName(pm, runningProcess);
                    newProcessList.add(new Process(runningProcess.stat().getPid(),
                            runningProcess.stat().ppid(), name,
                            runningProcess.getPackageName(), icon));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Fail to get the stat", e);
        }
        this.processListRecyclerViewAdapter.updateList(newProcessList);
        this.swipeRefreshLayout.setRefreshing(false);
    }

    private String getProcessName(PackageManager pm, AndroidAppProcess runningProcess) {
        try {
            CharSequence label = pm.getApplicationLabel(
                    pm.getApplicationInfo(runningProcess.getPackageName(), PackageManager.GET_META_DATA));
            return label.toString();
        } catch (PackageManager.NameNotFoundException e) {
            return runningProcess.name;
        }
    }

    private boolean isPackageRunning(String packageName) {
        if (packageName == null) {
            throw new InvalidParameterException();
        }

        List<AndroidAppProcess> runningAppProcesses = AndroidProcesses.getRunningAppProcesses();
        for (AndroidAppProcess process : runningAppProcesses) {
            if (packageName.equals(process.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private boolean killProcess(Process process) {
        android.os.Process.killProcess(process.getPid());
        android.os.Process.killProcess(process.getParentPid());

        String packageName = process.getPackageName();
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(packageName);
        boolean result = !isPackageRunning(packageName);

        Log.i(TAG, "Killed process: " + packageName + "; result: " + result);
        return result;
    }

    @Nullable
    private Drawable getAppIcon(PackageManager pm, AndroidAppProcess u) {
        Drawable icon = null;
        try {
            icon = pm.getApplicationIcon(u.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "Icon not found. " + u.getPackageName());
        }
        return icon;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(SettingsActivity.KEY_LIST_ALL)) {
            isListAll = sharedPreferences.getBoolean(SettingsActivity.KEY_LIST_ALL, true);
        }
    }

}
