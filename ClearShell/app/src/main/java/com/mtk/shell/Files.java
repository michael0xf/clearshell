package com.mtk.shell;


import java.io.File;
import java.util.ArrayList;

public class Files extends ArrayList<File>{

    Files(int count){
        super(count);
    }
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }


    int position = 0;

    public File getFile(){
        if (position < size()){
            return get(position);
        }
        return null;
    }
}