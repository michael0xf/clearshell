package com.mtk.shell;

import android.os.storage.StorageManager;
import android.widget.Toast;

import com.mtk.map.Array;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipList {
    private ClearShell.ZipAdapter fileAdapter;
    private List<File> files;
    List<File> dirs = new ArrayList<>();
    List<Integer> positions = new ArrayList<>();
    FSync fSync;
    ClearShell clearShell;
    ZipList.Comparator2 comparator = new ZipList.Comparator2();
    SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm EEE dd MMM yyyy");
    public final static int ABC_SORT = 0, SIZE_SORT = 1, MODIFY_SORT = 2;
    public ZipList(ClearShell clearShell, ClearShell.ZipAdapter fileAdapter, FSync fsync) {
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
            ZipList ZipList = new ZipList(files.get(0), abc);
            dirs = ZipList.dirs;
            positions = ZipList.positions;
        }*/

    }

    synchronized void showZipList() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //    synchronized (ZipList.this) {
                showZipList2();
            }


        }).start();
    }
    synchronized void showZipList2(){
        clearShell.log("show storage list");

        init(fileAdapter.abc);

        clearShell.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fileAdapter.backDirButtonOff();
                synchronized (ZipList.this) {
                    fileAdapter.showList(getStrings(dirs, files), 0);
                }
            }
        });

    }

    synchronized void showZipList(int i) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        init(fileAdapter.abc);
                        clearShell.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (ZipList.this) {
                                    fileAdapter.showList(getStrings(dirs, files), i);
                                }
                            }
                        });

                    }
                }
        ).start();
    }


    synchronized boolean openSomeDir(final File f) {

        if (f.isDirectory()) {
            openFile(f, true, null);
            return true;
        }else{
            if (!isZip(f))
                return false;
            if (!isOpened)
                return false;
            setOpened(false);
            new Thread( new Runnable() {
                @Override
                public void run() {
                    Zip zip = null;
                    try {
                        zip = new Zip(f.getAbsolutePath(), new ZipFile(f));
                    } catch (IOException e) {
                        clearShell.printError(e);
                    }
                    if (zip != null){
                        openFile2(zip, true, null, null);
                    }
                }
            }).start();
            return true;
        }

    }

    synchronized void openSomeDir2(final File f) {
        if (f.isDirectory()) {
            openFile2(f, true, null, null);
        }else{
            if (!isZip(f))
                return;
            Zip zip = null;
            try {
                zip = new Zip(f.getAbsolutePath(), new ZipFile(f));
            } catch (IOException e) {
                clearShell.printError(e);
            }
            if (zip != null){
                openFile2(zip, true, null, null);
            }
        }
    }

    public boolean isZip(File f){
        try {


            RandomAccessFile raf = new RandomAccessFile(f, "r");

            long n = raf.readInt();

            raf.close();

            if ((n == 0x504B0304)||(n == 0x504B0306)||(n == 0x504B0308)) {
                return true;
            }

        } catch (Exception e) {
        }
        return false;
    }


    synchronized void refresh(){
        if (!fileAdapter.closed) {
            File f = getCurrentDir();
            if (f == null) {
                showZipList();
            } else {
                if (f.exists()){
                    openFile(f, false, positions.get(positions.size() - 1));
                }else {
                    do{
                        f = f.getParentFile();
                        if (f == null) {
                            showZipList();
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
                showZipList2();
            } else {
                if (f.exists()){
                    openFile2(f, false, positions.get(positions.size() - 1), null);
                }else {
                    do{
                        f = f.getParentFile();
                        if (f == null) {
                            showZipList2();
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
        if (dirs.size() == 1) {
            dirs.remove(dirs.size() - 1);
            int  i = positions.remove(positions.size() - 1);
            init(fileAdapter.abc);
            clearShell.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fileAdapter.showList(getStrings(dirs, files), i);
                }
            });

            //showList(getZipList(), positions.remove(positions.size() - 1));
            return;
        }else if (dirs.size() == 0){
            showZipList();
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

    synchronized ArrayList<Object> init(ZipList prev){
        dirs.addAll(prev.dirs);
        positions.addAll(prev.positions);
        files = new ArrayList<>(prev.files.size());
        files.addAll(prev.files);
        return getStrings(dirs, files);

    }


    synchronized void openFileFromTree(int position) {
        if (!isOpened)
            return;
        setOpened(false);
        new Thread( new Runnable() {
            @Override
            public void run() {
                synchronized (ZipList.this) {
                    if (position < 0){
                        showZipList();
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

    boolean isDisk(File file){
        return file instanceof ZipList.Zip;
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
                        positions.add(0, i);
                        dirs.add(0, parent);
                        if (isDisk(parent))
                            return;
                        break;
                    }
                }
            } else {
                positions.add(0, 0);
                dirs.add(0, parent);
                if (isDisk(parent))
                    return;
            }
            file = parent;
            parent = parent.getParentFile();
        }
    }

    synchronized void initFromFile(File file, int abc) {
        dirs.clear();
        positions.clear();
        files = null;
        if (file == null) {
            clearShell.okDialog("Error", "File not found.");
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
            showZipList();

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
                final boolean b = dirs.size() > 0;
                 files = listFiles(f, fileAdapter.abc);
                ArrayList<Object> lst = getStrings(dirs, files);
                 clearShell.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (b)
                            fileAdapter.backDirButtonOn();
                        else
                            fileAdapter.backDirButtonOff();
                        synchronized (ZipList.this) {
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

    synchronized public File findFile(String s){
        if (dirs.size() > 0){
            File file =  dirs.get(0);
            if (file instanceof Zip){
                return ((Zip)file).findFile(s);
            }else{
                Toast.makeText(clearShell, "Error. ZipFile not found", Toast.LENGTH_LONG).show();
            }
        }
        return null;

    }


    synchronized public String getRootDir(){
        if (dirs.size() > 0){
            return dirs.get(0).getParent();
        }else{
            return null;
        }
    }

    synchronized public File getZip(){
        if (dirs.size() > 0){
            return dirs.get(0);
        }else{
            return null;
        }
    }
    synchronized public String getZipPath(){
        File f = getZip();
        if (f != null)
            return f.getAbsolutePath();
        return "";
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
        int p = base.getZipList().getLastPosition();
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
                long len1 = fSync.getZeroOrLen(o1);
                long len2 = fSync.getZeroOrLen(o2);
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



    public class Zip extends File {
        File[] files;
        HashMap<String, File> allFiles = new HashMap();
        File findFile(String path){
            return allFiles.get(path);
        }

        public Zip(String pathname, ZipFile zipFile) {
            super(pathname);


            Array<File> files = new Array(File.class, zipFile.size());
            HashMap<String, ZipList.Dir> map = new HashMap();
            try {
                ZipInputStream zin = new ZipInputStream(new FileInputStream(this));
                try {
                    ZipEntry ze;
                    while ((ze = zin.getNextEntry()) != null) {

                        String fullPath = ze.getName();
                        String parent = ZipList.getParent(fullPath);

                        File f;
                        if (ze.isDirectory()) {
                            ZipList.Dir dir = getParent(pathname, map, normalizePath(fullPath), this);
                            f = dir;
                        }else {
                            f = new ZipList.File2(pathname, zipFile, normalizePath(fullPath));
                            allFiles.put(f.getAbsolutePath(), f);
                        }

                        if (ClearShell.notEmpty(parent)) {
                            ZipList.Dir dir = getParent(pathname, map, parent, this);
                            dir.add(f);
                        }else {
                            files.addElement(f);
                        }
                    }
                }finally {
                    zin.close();
                }
            }catch (Exception e) {
                clearShell.printError(e);
            }
            this.files = files.cloneArray();
        }



        ZipList.Dir getParent(String pathname, HashMap<String, ZipList.Dir> map, String path, File zip) {
            ZipList.Dir dir = map.get(path);
            if (dir == null) {
                File parent;
                String parentPath = ZipList.getParent(path);
                if (parentPath.length() > 0){
                    parent = map.get(parentPath);
                }else
                    parent = zip;
                dir = new ZipList.Dir(pathname, path, parent);
                allFiles.put(dir.getAbsolutePath(), dir);
                map.put(path, dir);
            }
            return dir;
        }

        @Override
        public boolean isDirectory(){
            return true;
        }

        @Override
        public File[] listFiles(){

            return files;
        }
        @Override
        public boolean delete(){
            clearShell.okDialog("It's archive.", "Deleting files from the archive is not supported.");
            return false;
        }

    }

    interface Entry{
        void extract(File dir);
        String getName();
    }

    public class File2 extends File implements Entry{
//        private InputStream inputStream;
        ZipFile zipFile;
        String path, name;
        ZipEntry entry;
        synchronized public String getZipParentPath(){
            File f = getZip();
            if (f != null)
                return f.getParent();
            return "";
        }
        public File2(String pathname, ZipFile zipFile, String path) throws IOException {
            super(pathname);
            this.zipFile = zipFile;
            this.path = ZipList.getParent(path);
            this.name = ZipList.getName(path);
            String full = this.path + '/' + name;
            entry = zipFile.getEntry(full);

//            BIS bis = new BIS(zin);

/*            byte[] buf = new byte[(int)ze.getSize()];
            try {
                for (int c = zin.read(), i = 0; c != -1; c = zin.read(), i++) {
                    buf[i] = (byte)c;
                }
                zin.closeEntry();
            }
            finally {
            }*/

        }

        @Override
        public void extract(File dir){
            try {
                String s = dir.getAbsoluteFile() + "/" + getName();
                File f = new File(s);
                if (!f.exists()){
                    if (!f.createNewFile()){
                        Toast.makeText(clearShell, "Error. " + f.getAbsolutePath() + "Can't create file.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }else{
                    Toast.makeText(clearShell, "Error. " + f.getAbsolutePath() + "File already exists.", Toast.LENGTH_LONG).show();
                    return;
                }
                fSync.extract(zipFile.getInputStream(entry), f);
            } catch (IOException e) {
                clearShell.printError(e);
            }
        }


        public InputStream getInputStream() throws  IOException{
            String full = path + '/' + name;
            ZipEntry entry = zipFile.getEntry(full);
                return zipFile.getInputStream(entry);
        }

        @Override
        public String getAbsolutePath(){
            return path + '/' + name;
        }

        @Override
        public String getName(){
            return name;
        }

        @Override
        public String getParent(){
            return path;
        }


        @Override
        public String getPath(){
            return path + '/' + name;
        }

        @Override
        public String toString(){
            return path + '/' + name;
        }


        @Override
        public boolean delete(){
            clearShell.okDialog("It's archive.", "Deleting files from the archive is not supported.");
            return false;
        }

        @Override
        public long length(){
            return entry.getSize();
        }


        @Override
        public long lastModified(){
            return entry.getTime();
        }

    }

    public class Dir extends File implements Entry{
        String path, name;
        File parent;

        public Zip getZip(){
           if (parent instanceof Zip){
               return (Zip)parent;
           }
           return ((Dir)parent).getZip();
        }

        public File getParentFile(){
            return parent;
        }

        public Dir(String pathname, String full, File parent) {
            super(pathname);
            this.parent = parent;
            this.path = ZipList.getParent(full);
            this.name = ZipList.getName(full);
        }
        Array<File> buf = new Array(File.class);
        File[] files;

        @Override
        public void extract(File dir){
            String path = dir.getAbsolutePath() + "/" + getName();
            File f = new File(path);
            if (! f.exists()){
                f.mkdir();
            }else{
                if (!f.isDirectory()){
                    clearShell.okDialog("Error", f.getAbsolutePath() + " not a directory.");
                    return;
                }
            }
            for(File file: files){
                ((Entry)file).extract(f);
            }
        }


        public void add(File f) {
            buf.addElement(f);
        }

        @Override
        public File[] listFiles(){
            if ((files == null)||(files.length != buf.size())){
                files = buf.cloneArray();
            }
            return files;
        }

        @Override
        public String getAbsolutePath(){
            return path + '/' + name;
        }

        @Override
        public String getName(){
            return name;
        }

        @Override
        public String getParent(){
            return path;
        }


        @Override
        public String getPath(){
            return path + '/' + name;
        }

        @Override
        public String toString(){
            return path + '/' + name;
        }

        @Override
        public boolean isDirectory(){
            return true;
        }
    }
    public static String getParent(String path) {
        path = normalizePath(path);
        int i = path.lastIndexOf("/");
        if (i >= 0){
            return path.substring(0, i);
        }
        return "";

    }
    public static String normalizePath(final String path) {
        if (ClearShell.notEmpty(path)){
            if (path.charAt(path.length() - 1) == '/'){
                return path.substring(0, path.length() - 1);
            }
        }
        return path;

    }

    public static String getName(String path) {
        path = normalizePath(path);
        int i = path.lastIndexOf("/");
        if (i >= 0) {
            return path.substring(i + 1);
        }
        return "";

    }
    
}
