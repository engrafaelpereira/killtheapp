package com.example.rpereira.killtheapp.model;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Process
 *
 * @author rpereira
 * @since 17/11/16.
 */

public class Process implements Comparable<Process> {

    private final int pid;
    private final String name;
    private final String packageName;
    private final Drawable icon;

    public Process(int pid, String name, String packageName, Drawable icon) {

        this.pid = pid;
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
    }

    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Process process = (Process) o;

        return pid == process.pid && name.equals(process.name)
                && packageName.equals(process.packageName);
    }

    @Override
    public int hashCode() {
        int result = pid;
        result = 31 * result + name.hashCode();
        result = 31 * result + packageName.hashCode();
        result = 31 * result + (icon != null ? icon.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NonNull Process process) {
        if (this.pid < process.pid) {
            return -1;
        } else if (this.pid > process.pid) {
            return 1;
        }
        return 0;
    }
}
