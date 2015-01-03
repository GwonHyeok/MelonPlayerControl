package com.example.gwonhyeok.melonplayercontrol;

import com.hyeok.melon.SearchData;

/**
 * Created by GwonHyeok on 15. 1. 4..
 */
public class indexSearchData extends SearchData {
    private int id;

    public indexSearchData(String SongName, String SID, String Albumart, String Singer) {
        super(SongName, SID, Albumart, Singer);
    }

    public indexSearchData(int id, String SongName, String SID, String Albumart, String Singer) {
        super(SongName, SID, Albumart, Singer);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
