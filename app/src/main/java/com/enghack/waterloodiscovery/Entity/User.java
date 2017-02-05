package com.enghack.waterloodiscovery.Entity;

import java.io.Serializable;

/**
 * Created by ruins7 on 2016-09-25.
 */

public class User implements Serializable {

    private int uid;
    private String username;
    private String password;
    private int utype;

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

    public int getUtype() {
        return utype;
    }

    public void setUtype(int utype) {
        this.utype = utype;
    }
}
