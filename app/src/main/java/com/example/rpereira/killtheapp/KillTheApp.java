package com.example.rpereira.killtheapp;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.rpereira.killtheapp.adapter.ProcessToIconTextAdapter;
import com.example.rpereira.killtheapp.model.Process;
import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class KillTheApp extends AppCompatActivity {

    private static final String TAG = KillTheApp.class.getSimpleName();

    private Context context;
    private ActivityManager activityManager;
    private ListView listView;
    private FloatingActionButton addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getBaseContext();
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_app);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.addButton = (FloatingActionButton) findViewById(R.id.addButton);
        setAddButtonAction();

        this.listView = (ListView) findViewById(R.id.listView);
        setViewListAction();
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
        new AlertDialog.Builder(KillTheApp.this)
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
                        }
                        listApps();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up addButton, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listApps();
    }

    private void setAddButtonAction() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listApps();
            }
        });
    }

    private void listApps() {
        List<AndroidAppProcess> runningAppProcesses = AndroidProcesses.getRunningAppProcesses();
        final PackageManager pm = getPackageManager();

        List<Process> processes = new ArrayList<>();
        for (AndroidAppProcess runningProcess : runningAppProcesses) {
            Drawable icon = getAppIcon(pm, runningProcess);
            String name = getProcessName(pm, runningProcess);
            processes.add(new Process(runningProcess.pid, name,
                    runningProcess.getPackageName(), icon));
        }
        Collections.sort(processes, Collections.<Process>reverseOrder());

        ListAdapter adapter = new ProcessToIconTextAdapter(context, processes);
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
        activityManager.killBackgroundProcesses(packageName);
        boolean result = !isPackageRunning(packageName);

        Log.i(TAG, "Killed process: " + packageName + "; result: " + result);
        return result;
    }
}
