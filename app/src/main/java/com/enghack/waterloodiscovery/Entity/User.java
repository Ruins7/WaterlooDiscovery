package com.enghack.waterloodiscovery.Entity;

import java.io.Serializable;

/**
 * Created by ruins7 on 2016-09-25.
 */

public class User implements Serializable {

    private int uid;
    private String username;
    private String password;
    private int task_id;
    private boolean progress;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public boolean isProgress() {
        return progress;
    }

    public void setProgress(boolean progress) {
        this.progress = progress;
    }
}
