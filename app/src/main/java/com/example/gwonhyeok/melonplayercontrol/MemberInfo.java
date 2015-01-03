package com.example.gwonhyeok.melonplayercontrol;

/**
 * Created by GwonHyeok on 15. 1. 4..
 */

public class MemberInfo {

    private static MemberInfo instance;
    private String keyCookie;

    public synchronized static MemberInfo getInstance() {
        if (instance == null) {
            instance = new MemberInfo();
        }
        return instance;
    }

    public String getKeyCookie() {
        return keyCookie;
    }

    public void setKeyCookie(String keyCookie) {
        this.keyCookie = keyCookie;
    }
}
