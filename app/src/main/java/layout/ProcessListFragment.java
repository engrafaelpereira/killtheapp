package layout;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.rpereira.killtheapp.R;
import com.example.rpereira.killtheapp.adapter.ProcessToIconTextAdapter;
import com.example.rpereira.killtheapp.model.Process;
import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by rpereira on 16/01/17.
 *
 */
public class ProcessListFragment extends Fragment {

    private static final String TAG = ProcessListFragment.class.getSimpleName();

    private ListView listView;
    private FloatingActionButton addButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_process_list, container, false);

        this.addButton = (FloatingActionButton) view.findViewById(R.id.refreshButton);
        setRefreshButtonAction();

        this.listView = (ListView) view.findViewById(R.id.listView);
        setViewListAction();
        listApps(false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        listApps(false);
    }

    private void setViewListAction() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int position, long id) {
                final Process process = (Process) listView.getItemAtPosition(position);
                setKillProcessAlertDialog(view, process);
            }
        });
    }

    private void setKillProcessAlertDialog(final View view, final Process process) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Alert")
                .setMessage("Do you really want to kill " + process.getName() + "?")
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.i(TAG, process.getPid() + ":" + process.getPackageName());
                        if (killProcess(process)) {
                            Snackbar.make(view, "Kill the app: " + process.getName(), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            listApps(true);
                        } else {
                            Snackbar.make(view, "Impossible to kill: " + process.getName(), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void setRefreshButtonAction() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                listApps(true);
            }
        });
    }

    private void animateList() {
        AnimatorSet flip = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(),
                R.animator.card_flip_full);
        flip.setTarget(listView);
        flip.start();
    }

    private void listApps(final boolean animate) {
        if (animate) {
            animateList();
        }

        List<AndroidAppProcess> runningAppProcesses = AndroidProcesses.getRunningAppProcesses();
        final PackageManager pm = getActivity().getPackageManager();

        List<Process> processes = new ArrayList<>();
        for (AndroidAppProcess runningProcess : runningAppProcesses) {
            Drawable icon = getAppIcon(pm, runningProcess);
            String name = getProcessName(pm, runningProcess);
            processes.add(new Process(runningProcess.pid, name,
                    runningProcess.getPackageName(), icon));
        }
        Collections.sort(processes, Collections.<Process>reverseOrder());

        ListAdapter adapter = new ProcessToIconTextAdapter(getActivity(), processes);
        listView.setAdapter(adapter);
    }

    private String getProcessName(PackageManager pm, AndroidAppProcess runningProcess) {
        try {
            CharSequence label = pm.getApplicationLabel(pm.getApplicationInfo(runningProcess.getPackageName(), PackageManager.GET_META_DATA));
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
        int pid = process.getPid();
        String packageName = process.getPackageName();

        android.os.Process.killProcess(pid);

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
}
