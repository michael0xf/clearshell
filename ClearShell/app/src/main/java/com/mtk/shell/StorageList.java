package com.mtk.shell;

import android.os.Environment;
import android.os.storage.StorageManager;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

class StorageList {

    private ClearShell.FileAdapter fileAdapter;
    private List<File> files;
    List<File> dirs = new ArrayList<>();
    List<Integer> positions = new ArrayList<>();
    FSync fSync;
    ClearShell clearShell;
    Comparator2 comparator = new Comparator2();
    SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm EEE dd MMM yyyy", Locale.ENGLISH);
    public final static int ABC_SORT = 0, SIZE_SORT = 1, MODIFY_SORT = 2;
    public StorageList(ClearShell clearShell, ClearShell.FileAdapter fileAdapter, FSync fsync) {
        this.clearShell = clearShell;
        this.fileAdapter = fileAdapter;
        this.fSync = fsync;
    }


    public ArrayList<File> getFiles(){
        if (files == null)
            return new ArrayList(0);
        else
            return new ArrayList<>(files);
    }

    synchronized int filesSize(){
        return files.size();
    }


    synchronized void init(int abc) {

                initFromFile(null, abc);

        /*files = getStorageFileList(abc);
        if ((files != null)&&(files.size() > 0)){
            StorageList storageList = new StorageList(files.get(0), abc);
            dirs = storageList.dirs;
            positions = storageList.positions;
        }*/

    }

    synchronized void showStorageList() {
        new Thread(new Runnable() {
            @Override
            public void run() {

            //    synchronized (StorageList.this) {
                showStorageList2();
            }


        }).start();
    }
    synchronized void showStorageList2(){
        clearShell.log("show storage list");

        init(fileAdapter.abc);

        clearShell.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (StorageList.this) {
                    fileAdapter.showList(getStrings(dirs, files), null);
                }
                fileAdapter.mainRelayout2();
            }
        });

    }

    synchronized void showStorageList(int i) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                            init(fileAdapter.abc);
                            clearShell.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    synchronized (StorageList.this) {
                                        fileAdapter.showList(getStrings(dirs, files), i);
                                    }
                                }
                            });

                    }
                }
        ).start();
    }


    synchronized void openSomeDir(final File f) {
        if (f.isDirectory()) {
            openFile(f, true, null);
        }
    }

    synchronized void openSomeDir2(final File f) {
        if (f.isDirectory()) {
            openFile2(f, true, null, null);
        }
    }


    synchronized void refresh(){
        if (!fileAdapter.closed) {
            File f = getCurrentDir();
            if (f == null) {
                showStorageList();
            } else {
                if (f.exists()){
                    openFile(f, false, positions.get(positions.size() - 1));
                }else {
                    do{
                        f = f.getParentFile();
                        if (f == null) {
                            showStorageList();
                            return;
                        }
                    }while (!f.exists());
                    openSomeDir(f);

                }

            }
        }
    }



    synchronized void refresh2(){
        if (!fileAdapter.closed) {
            File f = getCurrentDir();
            if (f == null) {
                showStorageList2();
            } else {
                if (f.exists()){
                    openFile2(f, false, positions.get(positions.size() - 1), null);
                }else {
                    do{
                        f = f.getParentFile();
                        if (f == null) {
                            showStorageList2();
                            return;
                        }
                    }while (!f.exists());
                    openSomeDir2(f);

                }

            }
        }
    }

    synchronized void backDir(){
        new Thread( new Runnable() {
            @Override
            public void run() {
                backDir2();
            }
        }).start();
    }

    private synchronized void backDir2(){
        if (!isOpened)
            return;
        setOpened(false);
        if (dirs.size() == 2) {
            dirs.remove(dirs.size() - 1);
            int  i = positions.remove(positions.size() - 1);
            init(fileAdapter.abc);
            clearShell.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fileAdapter.showList(getStrings(dirs, files), i);
                }
            });

            //showList(getStorageList(), positions.remove(positions.size() - 1));
            return;
        }else if (dirs.size() < 2){
                showStorageList();
        }else {
            dirs.remove(dirs.size() - 1);
            int pos = positions.remove(positions.size() - 1);
            openFile2(dirs.get(dirs.size() - 1), false, pos, null);
        }
    }
    synchronized ArrayList<Object> init(File[] ff){
        dirs.clear();
        positions.clear();
        files = new ArrayList(ff.length);
        for(File f: ff){
            files.add(f);
        }
        return getStrings(dirs, files);
    }

    synchronized ArrayList<Object> init(StorageList prev){
        if (prev == null){
            files = new ArrayList<>();
            return getStrings(dirs, files);
        }else {
            if (prev.dirs != null) {
                dirs.addAll(prev.dirs);
            }
            if (prev.positions != null) {
                positions.addAll(prev.positions);
            }
            if (prev.files != null) {
                files = new ArrayList<>(prev.files.size());
                files.addAll(prev.files);
            }else{
                files = new ArrayList<>();
            }
            return getStrings(dirs, files);
        }

    }


    synchronized void openFileFromTree(int position) {
        if (!isOpened)
            return;
        setOpened(false);
        new Thread( new Runnable() {
            @Override
            public void run() {
                synchronized (StorageList.this) {
                    if (position < 0){
                        showStorageList();
                        return;
                    }
                    int selectorPosition;
                    if (position >= positions.size())
                        selectorPosition = 0;
                    else
                        selectorPosition = positions.get(position);
                    File file;
                    if (position > dirs.size()) {
                        file = null;
                    } else if (position == dirs.size() - 1){
                        refresh2();
                        return;
                    }else{
                        file = dirs.get(position);
                        dirs = dirs.subList(0, position + 1);
                    }
                    if (position < positions.size())
                        positions = positions.subList(0, position + 1);
                    openFile2(file, false, selectorPosition, null);
                }

            }
        }).start();

    }

    synchronized int dirsSize(){

        return dirs.size();
    }

    synchronized int getLastPosition() {
        if (positions.size() == 0)
            return 0;
        return positions.get(positions.size() - 1);
    }

    synchronized void initParent(File file, int abc) {
        if (isDisk(file))
            return;
        File parent = file.getParentFile();
        while (parent != null) {
            List<File> files = listFiles(parent, abc);
            if ((files != null)&&(files.size() > 0)) {
                for (int i = 0; i < files.size(); i++) {
                    if (equalsFiles(files.get(i), file)) {
                        positions.add(1, i);
                        dirs.add(1, parent);
                        if (isDisk(parent))
                            return;
                        break;
                    }
                }
            } else {
                positions.add(1, 0);
                dirs.add(1, parent);
                if (isDisk(parent))
                    return;
            }
            file = parent;
            parent = parent.getParentFile();
        }
    }

    synchronized void initFromFile(File file, int abc) {
        dirs.clear();
        dirs.add(null);
        positions.clear();
        positions.add(0);
        files = null;
        File parent;
        if (file == null) {
            files = getStorageFileList(abc);
            return;
        } else {
            files = listFiles(file, abc);
            initParent(file, abc);
        }

    }
    synchronized File getCurrentDir(){
        if (dirs.size() > 0){
            return dirs.get(dirs.size() - 1);
        }
        return null;
    }
    synchronized void openFile(final File f, final boolean addParent, final Integer pos) {
        openFile(f, addParent, pos, null);
    }

     boolean isOpened = true;
    synchronized void setOpened(boolean v){
        isOpened = v;
    }
    synchronized void openFile(final File f, final boolean addParent, Integer position, Runnable run) {
        if (f == null){
            clearShell.log("Open null file");
        }else{
            clearShell.log("open file " + f.getAbsolutePath());
        }


        if (!isOpened)
            return;
        setOpened(false);
        new Thread( new Runnable() {
            @Override
            public void run() {
                openFile2(f, addParent, position, run);
            }
        }).start();


    }
    private synchronized void openFile2(final File f, final boolean addParent, Integer position, Runnable run) {
        if (f == null) {
            showStorageList();

            return;
        }
        try {
            if (f.isDirectory()) {
                if (addParent) {
                    if (position == null) {
                        initFromFile(f, fileAdapter.abc);
                        position = getLastPosition();
                    }
                    dirs.add(f);

                    positions.add(position);

                }
                final boolean b = dirs.size() > 1;
                files = listFiles(f, fileAdapter.abc);
                ArrayList<Object> lst = getStrings(dirs, files);
                clearShell.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (b)
                            fileAdapter.backDirButtonOn();
                        else
                            fileAdapter.backDirButtonOff();
                        synchronized (StorageList.this) {
                            int pos = 0;
                            if (dirs.size() > 0) {
                                pos = dirs.size() - 1;
                            }
                            fileAdapter.showList(lst, pos);
                        }
                        if (run != null)
                            run.run();

                    }
                });
                return;
            }
        }catch (Throwable t){
            clearShell.log(t);
        }

    }

    synchronized public String getDir(){
        if (dirs.size() == 0){
            return null;
        }
        return dirs.get(dirs.size() - 1).getAbsolutePath();
    }

    synchronized public int size(){
        if (files == null)
            return dirs.size();
        else
            return dirs.size() + files.size();
    }

    synchronized Integer prevPosition(Integer currentPosition, boolean repeat) {
        if (files.size() == 0)
            return null;
        if (currentPosition == null){
            return 0;
        }else
            currentPosition--;
        if (currentPosition < dirs.size()){
            if (repeat)
                return size() - 1;
            else
                return null;
        }
        return currentPosition;
    }

    synchronized Integer nextPosition(Integer currentPosition, boolean repeat) {
        if (files.size() == 0)
            return null;
        if (currentPosition == null){
            return dirs.size();
        }else
            currentPosition++;
        if (currentPosition >= size()){
            if (repeat)
                return dirs.size();
            else
                return null;
        }
        return currentPosition;
    }




    synchronized File getFile(int position){

        int dirLen;
        if (dirs != null) {
            dirLen = dirs.size();
            if (position < dirs.size()) {
                return dirs.get(position);
            }
        }else{
            dirLen = 0;
        }
        position = position - dirLen;
        if ((files != null)&&(files.size() > position )) {
            File f = files.get(position);
            return f;
        }
        return null;
    }




    static  boolean equalsFiles(File f0, File f1){
        if (f0 == f1)
            return true;
        if (f0 == null)
            return false;
        return f0.equals(f1);
    }

    synchronized List<File> listFiles(File f, int abc){

        File[] files;
        if (f != null)
            files = f.listFiles();
        else
            files = null;
        ArrayList ret;
        if (files == null){
            return new ArrayList(0);
            //ret = getStorageFileList(abc);
        }else {
            ret = new ArrayList(files.length);
            for (File file : files) {
                if (file != null) {
                    ret.add(file);
                }
            }
        }
        sort(ret, abc);
        return ret;
    }

    StorageManager getStorageManager() {
        return (StorageManager) fileAdapter.getStorageManager();
    }
    /*
           Use reflection for detecting all storages as android do it
           probably doesn't work with USB-OTG
           works only on API 19+
     */
    synchronized public ArrayList getAllPaths() {
        ArrayList<String> allPaths = new ArrayList<>();
        try {
            Class<?> storageVolumeClass = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = getStorageManager().getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClass.getMethod("getPath");
            Method getState = storageVolumeClass.getMethod("getState");
            Object[] getVolumeResult = (Object[])getVolumeList.invoke(getStorageManager());
            //Class c =  getVolumeResult.getClass();
            //printMessage(c.getName());
            //final int length = Array.getLength(getVolumeResult);

            for (int i = 0; i < getVolumeResult.length; ++i) {
                Object storageVolumeElem = getVolumeResult[i];
                String mountStatus = (String) getState.invoke(storageVolumeElem);
                if (mountStatus != null && mountStatus.equals("mounted")) {
                    String path = (String) getPath.invoke(storageVolumeElem);
                    if (path != null) {
                        allPaths.add(path);
                    }
                }
            }
        } catch (Throwable e) {
        }
        return allPaths;
    }

    synchronized boolean isDisk(File f){
        if (disks == null){
            disks = getDisks();
        }
        if (disks != null) {
            for (File file: disks){
                if (file.equals(f))
                    return true;
            }
        }
        return false;
    }
    List<File> disks;

    synchronized List<File> getDisks() {
        try {

            StorageManager storageManager = getStorageManager();

            String[] volumes = (String[]) storageManager.getClass()
                    .getMethod("getVolumePaths", new Class[0])
                    .invoke(storageManager, new Object[0]);
            ArrayList<String> additional = getAllPaths();
            for (String v : volumes) {
                if (!additional.contains(v)) {
                    additional.add(v);
                }
            }
            ArrayList<File> files = new ArrayList<>(additional.size());
            int i = 0;
            for (; i < additional.size(); i++) {
                File f = new File(additional.get(i));
                if (f != null)
                    files.add(f);
            }
            File f = new File(clearShell.myDir());
            if (f.exists())
                files.add(f);
            return files;
        }catch (Throwable t){
            return new ArrayList<>();
        }
    }


    synchronized ArrayList<File> getStorageFileList(int abc) {
        try {

            StorageManager storageManager = getStorageManager();

            String[] volumes = (String[]) storageManager.getClass()
                    .getMethod("getVolumePaths", new Class[0])
                    .invoke(storageManager, new Object[0]);
            ArrayList<String> additional = getAllPaths();
            for(String v: volumes){
                if (!additional.contains(v)){
                    additional.add(v);
                }
            }
            ArrayList<File> files = new ArrayList<>(additional.size() + 1);

            int i = 0;
            for (; i < additional.size(); i++) {
                File f = new File(additional.get(i));
                if (f != null)
                    files.add(f);
            }

            sort(files, abc);
            File my = new File(clearShell.myHistoryDir());
            files.add(0, my);
            my = new File(clearShell.myHomeDir());
            files.add(0, my);

            /*ArrayList<File> add1 = new ArrayList();
            for (File file : files) {
                File parent = file.getParentFile();
                while (parent != null) {
                    if (!add1.contains(parent)) {
                        add1.add(parent);
                        parent = parent.getParentFile();
                    }else
                        break;
                }
            }
            sort(add1, abc);
            files.addAll(add1);*/

            /*File f = new File("/");
            if (f.exists());
            files.add(0, f);*/

            String[] add0 = new String[]{
                    Environment.DIRECTORY_ALARMS,
                    Environment.DIRECTORY_DCIM,
                    Environment.DIRECTORY_DOCUMENTS,
                    Environment.DIRECTORY_DOWNLOADS,
                    Environment.DIRECTORY_MOVIES,
                    Environment.DIRECTORY_MUSIC,
                    Environment.DIRECTORY_NOTIFICATIONS,
                    Environment.DIRECTORY_PICTURES,
                    Environment.DIRECTORY_PODCASTS,
                    Environment.DIRECTORY_RINGTONES};

            ArrayList addl0 = new ArrayList(add0.length);
            for(String s: add0){
                final String dir = Environment.getExternalStoragePublicDirectory(s).toString();
                File f = new File(dir);
                if (f.exists())
                    addl0.add(f);
            }
            sort(addl0, abc);
            files.addAll(addl0);

            File parent = my.getParentFile();
            while(parent != null){
                my = parent;
                parent = my.getParentFile();
            }
            files.add(my);


            return files;
        } catch (Throwable e) {
            fileAdapter.printError(e.getMessage());
        }
        return null;
    }


    HashMap<Long, String> time = new HashMap<>(1024);
    synchronized String getTimes(long t){
        String ret = time.get(t);
        if (ret == null){
            ret = dateFormatter.format(t);
            if (time.size() == 1024)
                time.clear();
            time.put(t, ret);
        }
        return ret;
    }

    /*public void setBase(ClearShell.FileAdapter base) {
        dirs.clear();
        File dir = base.getCurrentDir();
        if (dir == null)
            return;
        int p = base.getStorageList().getLastPosition();
        dirs.add(dir);
        dirs.add(dir);
        positions.add(p);
        positions.add(p);
    }*/


    class Comparator2 implements Comparator<File> {
        int abc;
        Comparator init(int abc){
            this.abc = abc;
            return this;
        }
        @Override
        public int compare(File o1, File o2) {
            if (o1 == o2)
                return 0;
            if (o1 == null)
                return 1;
            if (o2 == null)
                return -1;
            if (o1.isDirectory() && (!o2.isDirectory()))
                return -1;
            if ((!o1.isDirectory()) && o2.isDirectory())
                return 1;
            if (abc == ABC_SORT) {
                return stringCompare(o1.getName(), o2.getName());
            }else if (abc == SIZE_SORT){
                long len1;
                long len2;
                if (o1.isDirectory())
                    len1 = fSync.getZeroOrLen(o1);
                else
                    len1 = o1.length();
                if (o2.isDirectory())
                    len2 = fSync.getZeroOrLen(o2);
                else
                    len2 = o2.length();
                if (len1 > len2)
                    return -1;
                else if (len1 < len2)
                    return 1;
                else
                    return 0;
            }else {
                if (o1.lastModified() > o2.lastModified())
                    return -1;
                if (o1.lastModified() < o2.lastModified())
                    return 1;
                return 0;
            }
        }
    };

    synchronized void sort(ArrayList<File> files, final int abc){
        Collections.sort(files, comparator.init(abc));
    }
    public static int stringCompare(String str1,
                                    String str2)
    {
        for (int i = 0; i < str1.length() &&
                i < str2.length(); i++) {
            if ((int)str1.charAt(i) ==
                    (int)str2.charAt(i)) {
                continue;
            }
            else {
                return (int)str1.charAt(i) -
                        (int)str2.charAt(i);
            }
        }

        if (str1.length() < str2.length()) {
            return (str1.length()-str2.length());
        }
        else if (str1.length() > str2.length()) {
            return (str1.length()-str2.length());
        }

        else {
            return 0;
        }
    }

    ArrayList<Object> getStrings(List<File> dirs, List<File> files) {
        ArrayList<Object> ret = new ArrayList();
        if (dirs != null) {
            for (File f : dirs) {
                if ((f == null) || (f.getName() == null) || (f.getName().length() == 0))
                    ret.add("*** LIST OF PARENTS ***");
                else
                    ret.add(f.getName());
            }
        }
        if (files != null) {
            for (File ff : files) {
                if (ff == null)
                    ret.add("?");
                else
                    ret.add(ff.getName());
            }
        }
        return ret;
    }



}
