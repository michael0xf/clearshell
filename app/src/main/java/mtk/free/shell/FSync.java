package mtk.free.shell;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.mtk.map.Array;
import com.mtk.map.HTable;
import com.mtk.map.HashArray;
import com.mtk.map.LongArray;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

class FSync implements  Runnable{
    private FreeShell freeShell;
    private HTable<String, File> selected = new HTable<>(String.class, File.class, 32);
    private HashMap<String, String> exceptions = new HashMap(32);


    FSync(FreeShell freeShell){
        this.freeShell = freeShell;
    }
    synchronized String[] getSelected(){

        return selected.code().toArray();
    }

    synchronized File[] getSelectedFiles(){

        return selected.value().toArray();
    }


    synchronized boolean hasSelected(){
        return selected.size() > 0;
    }


    public synchronized void copy(FreeShell.FileAdapter fileAdapter) {
        File dir = fileAdapter.getStorageList().getCurrentDir();
        cc(dir, false, false, fileAdapter);
    }

    public synchronized void overwrite(FreeShell.FileAdapter fileAdapter) {
        File dir = fileAdapter.getStorageList().getCurrentDir();
        cc(dir, true, false, fileAdapter);
    }

    public synchronized void move(FreeShell.FileAdapter fileAdapter) {
        File dir = fileAdapter.getStorageList().getCurrentDir();
        cc(dir, false,true, fileAdapter);
    }


    public synchronized void moveOverwrite(FreeShell.FileAdapter fileAdapter) {
        File dir = fileAdapter.getStorageList().getCurrentDir();
        cc(dir, true, true, fileAdapter);
    }




        /*    for (File f:selected.values()) {
            if (f.isDirectory()) {
                sizes().put(f, folderSize(f));
            }*/


    synchronized boolean removeSelect(File file){
        if (file == null)
            return false;
        return removeSelect(file, file.getAbsolutePath());
    }

    synchronized boolean removeException(String path){
        String superParent = exceptions.remove(path);
        if (superParent != null) {
            HashSet<String> set = superParentException.get(superParent);
            if (set != null)
                set.remove(path);
            return true;
        }
        return false;
    }

    HashMap<String, HashSet<String>> superParentException = new HashMap<>(32);
    synchronized boolean removeSelect(File file, String path){
        if (selected == null)
            return false;
        if (file == null)
            return false;
        boolean b = selected.remove(path) != null;
        if (b) {
            HashSet<String> set = superParentException.remove(path);
            if (set != null)
                for(String s: set){
                    exceptions.remove(s);
                }
        }
            String superParent = isSelect2(file, path);
            if (superParent != null) {
                HashSet<String> set = superParentException.get(superParent);
                if (set == null) {
                    set = new HashSet();
                    superParentException.put(superParent, set);
                }
                set.add(path);
                exceptions.put(path, superParent);
            } else {
               if (!b)
                   return false;
            }
        //}

        invalidate(path);

        return true;
    }
    synchronized void invalidate(String path){

        HashSet<FreeShell.Item> fileItems = table.get(path);
        if ((fileItems == null)||(fileItems.size() == 0))
            return;
        final ArrayList<FreeShell.Item> list = new ArrayList<>(fileItems);
        freeShell.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        for(FreeShell.Item item: list){
                            if (item.isParent){
                                item.adapter.refresh();
                            }else{
                                File f = item.file;
                                if ((f != null)&&(f.exists())&& f.isDirectory()) {
                                    File[] files = f.listFiles();
                                    if (files != null){
                                        for (File file : files) {
                                            String p = file.getAbsolutePath();
                                            HashSet<FreeShell.Item> items;
                                            synchronized (FSync.this) {
                                                items = table.get(p);
                                            }
                                            if ((items != null) && (items.size() > 0)) {
                                                for (FreeShell.Item i : items) {
                                                    i.format();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            item.format();
                        }
                    }
                });

    }


    synchronized boolean select(FreeShell.Item fileItem){
        File file = fileItem.file;

        if (select(file)) {
            if (!file.isDirectory())
                return true;
            Long l = sizes().get(file);
            if (l == null)
                folderSize(file, fileItem);
            return true;
        }
        return false;
    }
    synchronized boolean select(File file){
        if (file == null)
            return false;
        if (!file.exists())
            return false;
        String path = file.getAbsolutePath();
        if (isSelect(file, path)) {
            freeShell.log("select conflict: " + path);
            return true;
        }

        if (exceptions.containsKey(path)) {
            removeException(path);
            if (!isSelect(file, path)) {
                selected.put(path, file);
            }
            invalidate(path);
            return true;

        }
        selected.put(path, file);
        invalidate(path);
        return true;
    }


    synchronized String isSelect2(File file, String path){
        if (exceptions.containsKey(path)) {
            return null;
        }
        File parent = file.getParentFile();
        Array<String> excessSelects = new Array<>(String.class, 1);
        while (parent != null) {
            String p = parent.getAbsolutePath();
            String ret = exceptions.get(p);
            if (ret != null) {
                if (excessSelects.size() > 0)
                    break;
                else
                    return null;
            }
            if (selected.containsKey(p)) {
                excessSelects.insertElementAt(p, 0);
            }
            parent = parent.getParentFile();
        }
        if (excessSelects.size() > 0) {
            String ret = excessSelects.removeElementAt(0);
            while (excessSelects.size() > 1) {
                String superParent = excessSelects.pop();
                selected.remove(superParent);
                HashSet<String> set = superParentException.get(superParent);
                if (set != null) {
                    HashSet<String> upper = superParentException.get(ret);
                    if (upper == null) {
                        upper = new HashSet();
                        superParentException.put(ret, upper);
                    }
                    for(String e: set){
                        upper.add(e);
                        exceptions.remove(e);
                        exceptions.put(e, ret);
                    }
                    superParentException.remove(superParent);
                }
            }

            return ret;
        }else
            return null;
    }
    synchronized boolean isSelect(FreeShell.Item fileItem){
        File f = fileItem.file;
        if (f == null)
            return false;
        return isSelect3(f, f.getAbsolutePath()) != null;

    }
    synchronized boolean isSelect(File file){
        return isSelect3(file, file.getAbsolutePath()) != null;
    }

    synchronized boolean isSelect(File file, String path){
        return isSelect3(file, path) != null;
    }
    synchronized String isSelect3(File file, String path){
        if (file == null)
            return null;
        if (selected.containsKey(path))
            return path;
        return isSelect2(file, path);
    }


    synchronized void changeSelect(File f, FreeShell.Item item){
        if (f == null)
            return;
        if (isSelect(f, f.getAbsolutePath()))
            removeSelect(f);
        else {
            select(item);
        }
    }


    class FolderCalculator implements Runnable{

          boolean run = true;
        File file;
        FreeShell.Item item;
        FolderCalculator(File file, FreeShell.Item fileItem){
            this.file = file;
            item = fileItem;
        }


        @Override
        public void run() {
            File directory = file;

            Array<File> arr = new Array(File.class, 32);
            arr.addElement(directory);
            int counter = 0;
            HashMap<File, Long> parents = new HashMap<>(64);
            while (run && (arr.size() > 0)) {
                counter++;
                File dir = arr.pop();
                while ((arr.size() > 0) && (!dir.exists())){
                    dir = arr.pop();
                }
                if (!dir.exists())
                    break;
                Long len = sizes().get(dir);
                if (len == null){
                    len = (long)0;
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {

                            if (file != null) {
                                if (file.isFile())
                                    len += file.length();
                                else
                                    arr.addElement(file);
                            }
                        }
                    }
                    parents.put(dir, len);
                }
                File parent = dir.getParentFile();
                Long lp = parents.get(parent);
                while (lp != null) {
                    lp += len;
                    parents.put(parent, lp);
                    parent = parent.getParentFile();
                    lp = parents.get(parent);
                }
                if (counter == 0x7f) {
                    if (!directory.exists())
                        break;
                    Long length = parents.get(directory);
                    if (length != null)
                        sizes().put(directory, length);
                    freeShell.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (item.adapter.closed){
                                        sizes().remove(directory);
                                        run = false;
                                    }else if (!isSelect(item)){
                                        sizes().remove(directory);
                                        run = false;
                                    }
                                    item.format();
                                    item.panel.invalidate();

                                }
                            });

                    counter = 0;
                }

            }

            final HashSet<FreeShell.Item> items = new HashSet(parents.size());
            for(Map.Entry<File, Long> entry: parents.entrySet()){
                File file = entry.getKey();
                HashSet<FreeShell.Item> fileItems = table.get(file);
                if (fileItems != null) {
                    for (FreeShell.Item fileItem : fileItems) {
                        items.add(fileItem);
                    }
                }
                sizes().put(entry.getKey(), entry.getValue());
            }
            items.add(item);
            freeShell.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            for(FreeShell.Item item: items){
                                item.format();
                            }
                        }
                    });

        }

    }


    public synchronized void folderSize(File file, FreeShell.Item fileItem) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new FolderCalculator(file, fileItem).run();
            }
        }).start();
//        freeShell.mapThreads().getFileThread().putRunnable(new FolderCalculator(fileItem));
    }

    FileSizeTable sizes;
    synchronized FileSizeTable sizes(){
        if (sizes == null){
            sizes = new FileSizeTable();
        }
        return sizes;
    }

    public class FileSizeTable {


        public FileSizeTable(){
            value0 = new LongArray(1024);
            value1 = new LongArray(1024);
            code = new HashArray(String.class, 1024);
        }

        LongArray value0;
        LongArray value1;
        HashArray code;
        public synchronized void remove(File key){
                int index = code.removeElement(key.getAbsolutePath());
                value0.removeElementAt(index);
                value1.removeElementAt(index);
        }


        public synchronized int put(File key, long size)
        {
                int index = code.put(key.getAbsolutePath());
                value0.insertElementAt(size, index);
                value1.insertElementAt(key.lastModified(), index);
                return index;
        }

        public synchronized Long get(File key){
                int index = code.indexOfObject(key.getAbsolutePath());
                if (index >= 0) {
                    long v = value1.elementAt(index);
                    if (v == key.lastModified())
                        return value0.elementAt(index);
                    else {
                        code.removeElementAt(index);
                        value0.removeElementAt(index);
                        value1.removeElementAt(index);
                        return null;
                    }

                }
                return null;
        }

    }
    String getParentParent(File f){
        if (f != null){
            f = f.getParentFile();
            if (f != null)
                return f.getParent();
        }
        return null;
    }

    boolean writeFile(File file, String text) {
        final int ret = writeFile0(file,text);
        if (ret >= 0) {
            freeShell.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ret == 1){
                        freeShell.refreshDirs(getParentParent(file));
                    }
                    Toast.makeText(freeShell, file.getName() + " was saved.", Toast.LENGTH_LONG).show();
                }
            });
            return true;
        }
        return false;
    }

    synchronized int writeFile0(File file,String text){
        try {
            int ret = 0;
            File parent = file.getParentFile();
            if (!parent.exists()){
                parent.mkdirs();
                ret = 1;
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(text.getBytes());
            fileOutputStream.close();

            return ret;
        } catch (Throwable e) {
            freeShell.printError(e.getMessage());
        }
        return -1;

    }


    synchronized File writeFile(String name,String text){
        File f = new File(name);

        if (writeFile(f, text))
            return f;
        else
            return null;
    }

    synchronized void arrayToFile(String name, String[] collection){
        File f = new File(name);
        try {
            PrintWriter printWriter = new PrintWriter(f);
            for(String s: collection){
                if ( s != null)
                    printWriter.println(s);
            }
            try{
                printWriter.close();
            }catch (Throwable t) {
            }
        } catch (IOException e) {
            freeShell.printError(e.getMessage());
            return;
        }

    }
    long getZeroOrLen(File o1) {
        if (o1.isDirectory()) {
            if (sizes == null)
                return 0;
            Long len1 = sizes.get(o1);
            if (len1 == null)
                return 0;
            return len1;
        } else
            return o1.length();
    }
    interface  Action{
        public void run(Map<String, File> newFolders) throws Exception;
        public File getSource();
    }


        Stack<Action> actions  = new Stack();
        HashMap<String, HashSet<FreeShell.Item>> table = new HashMap<>();
        HTable<String, File> filesToDelete  = new HTable(String.class, File.class, 32);
        Array<File> dirsToDelete = new Array(File.class, 32);
        synchronized void newFileItem(String f, FreeShell.Item fileItem){
            HashSet<FreeShell.Item> list = table.get(f);
            if (list == null){
                list = new HashSet();
                table.put(f, list);
            }
            list.add(fileItem);
        }
        synchronized void removeFileItem(String f, FreeShell.Item fileItem){
            HashSet<FreeShell.Item> list = table.get(f);
            if (list == null){
                return;
            }
            if (!list.remove(fileItem))
            {
                int dtop = 1;
            }
            if (list.size() == 0)
                table.remove(f);
        }

        void invalidPlace(FreeShell.Adapter adapter){
            freeShell.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    adapter.unLock();
                    AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                    // Specify the dialog is not cancelable
                    builder.setCancelable(false).setTitle("This is invalid place!").setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });


                    AlertDialog dialog = builder.create();
                    // Display the alert dialog on interface
                    dialog.show();


                }
            });
        }


    public synchronized void cc(final File dir, final boolean overwrite, final boolean delete, final FreeShell.Adapter adapter) {

        if (!dir.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);

            builder.setTitle("Error")
                    .setMessage("The dir not exists.")
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }
        AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
        String action;
        if (delete)
             action = "move";
        else
            action = "copy";

        if (overwrite)
            action = action + " and overwrite old files.";
        builder.setTitle("Are you sure?")
                .setMessage("Do you want " + action + " selected files to " + dir.getAbsolutePath() + "?")
                .setCancelable(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try{

                            synchronized (FSync.this) {
                                cc2(dir, overwrite, delete, adapter);
                            }

                        }catch (Exception e){
                            freeShell.printError(e);
                        }

                    };

                });

        AlertDialog alert = builder.create();
        alert.show();


    }



    public synchronized void cc2(final File dir, final boolean overwrite, final boolean delete, FreeShell.Adapter adapter) {
            if (dir == null){
                invalidPlace(adapter);
                return;
            }
            try {
                pause = true;
                String destPath = dir.getAbsolutePath();
                File[] selected = this.selected.value().toArray();
                for (File src : selected) {
                    if (isSelect2(src, src.getAbsolutePath()) == null) {
                        String newName = destPath + '/' + src.getName();
                        File dest = new File(newName);
                        if (delete) {
                            if (overwrite) {
                                putAction(new MoveOverwriteAction(src, dest));
                            } else {
                                putAction(new MoveAction(src, dest));
                            }
                        } else {
                            if (overwrite) {
                                putAction(new OverwriteAction(src, dest));
                            } else {
                                putAction(new CopyAction(src, dest));
                            }
                        }
                    }
                }
            }finally{
                pause = false;
            }
        }


         boolean pause = false;
         boolean skipAll = false;
        HashSet<File> errors = new HashSet(1);
//main thread
        @Override
        public void run(){
            while(true) {
                boolean start = false;
                if (actions.size() > 0) {
                    start = true;
                    HashMap<String, File> newFolders = new HashMap<String, File>();
                    freeShell.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            freeShell.beginOfAction(actions.size());
                        }
                    });

                    while (actions.size() > 0) {
                        while (pause) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.yield();
                            }
                        }
                        if (actions.size() > 0) {
                            Action runnable = actions.pop();
                            String path = runnable.getSource().getAbsolutePath();
                            try {
                                errors.remove(path);
                                runnable.run(newFolders);
                            } catch (Throwable t) {
                                errors.add(runnable.getSource());
                                if (!skipAll)
                                    error(t, runnable);
                            }
                        }
                    }
                }
                if (filesToDelete.size() > 0) {
                    freeShell.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            freeShell.progress(1);
                        }
                    });

                    while (filesToDelete.size() > 0) {
                        while (pause) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.yield();
                            }
                        }
                        String path = filesToDelete.code().pop();
                        File f = filesToDelete.value().pop();
                        try {
                            if (f.isDirectory()) {
                                File[] files = f.listFiles();
                                if ((files != null) && (files.length > 0)) {
                                    for (File file : files) {
                                        String p = file.getAbsolutePath();
                                        synchronized (FSync.this) {
                                            if (!exceptions.containsKey(p)) {
                                                filesToDelete.put(p, file);
                                            } else {
                                                removeException(p);
                                            }
                                        }
                                    }
                                }
                                dirsToDelete.addElement(f);
                            } else {
                                errors.remove(path);
                                f.delete();
                                freeShell.printLog("File " + path + " was deleted");
                            }
                        } catch (Throwable t) {
                            errors.add(f);
                            if (!skipAll)
                                error(t, new DelAction(f));
                        }
                    }
                    freeShell.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            freeShell.progress(2);
                        }
                    });
                }

                if (dirsToDelete.size() > 0) {
                    while (dirsToDelete.size() > 0) {
                        while (pause) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.yield();
                            }
                        }


                        File f = dirsToDelete.pop();
                        if (!f.exists())
                            continue;
                        String path = f.getAbsolutePath();
                        try {

                            File[] files = f.listFiles();
                            if ((files == null) || (files.length == 0)) {
                                errors.remove(path);
                                f.delete();
                                //freeShell.printLog("Directory " + f.getAbsolutePath() + " was deleted");
                            }
                        } catch (Throwable t) {
                            errors.add(f);
                            if (!skipAll)
                                error(t, new DelAction(f));
                        }
                    }
                    freeShell.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            freeShell.progress(3);
                        }
                    });

                }
                while(zips.size() > 0){
                    try {
                        zips.pop().close();
                    } catch (Throwable e) {
                        freeShell.printError(e);
                    }
                }
                int size = errors.size();
                if (size > 0){
                    StringBuilder sb = new StringBuilder("Error list.");
                    for(File f: errors){
                        sb.append(f.getAbsolutePath() + "\n");
                    }
                    freeShell.showLog(sb);
                    errors.clear();
                }

                if (start) {
                    start= false;
                    freeShell.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(freeShell, "Ok", Toast.LENGTH_LONG).show();
                            freeShell.endOfAction();
                        }
                    });
                    skipAll = false;

                }
                do {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.yield();
                    }
                }while (pause);


            }

        }


        synchronized void putAction(final Action run){

            actions.add(run);
        }

        class DelAction implements Action{
            File file;
            DelAction(File file){
                this.file = file;
            }
            @Override
            public void run(Map<String, File> newFolders) throws Exception {
                String p = file.getAbsolutePath();
                removeSelect2(p);
                filesToDelete.put(p, file);
            }

            @Override
            public File getSource() {
                return file;
            }
        }

        class CopyAction implements Action{
            File source, dest;
            CopyAction(File source, File dest){
                this.source = source;
                this.dest = dest;
            }
            public File getSource(){
                return source;
            }
            @Override
            public String toString(){
                return "Copy " + source.getAbsolutePath() + " to " + dest.getAbsolutePath();
            }

            boolean newAction(File src, File dst) {
                String p = src.getAbsolutePath();
                synchronized (FSync.this) {
                    if (!exceptions.containsKey(p)) {
                        putAction(new CopyAction(src, dst));
                        return true;
                    } else {
                        removeException(p);
                        return false;
                    }
                }
            }

            synchronized void complete(File f, boolean hasNextAction){
                String p = f.getAbsolutePath();
                removeSelect2(p);
                HashSet<FreeShell.Item> list = table.get(p);;
                if (list != null) {
                    for(final FreeShell.Item fileItem: list) {
                        freeShell.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    fileItem.format();
                                }
                            }
                        );
                    }
                }
            }

            boolean isOverwrite(){
                return false;
            }

            InputStream getInputStream(File source) throws IOException {
                if (source instanceof  ZipList.File2)
                    return ((ZipList.File2)source).getInputStream();
                else
                    return new FileInputStream(source);
            }

            public void run(Map<String, File> newFolders) throws Exception{


                if (!source.exists()) {
                    sourceNotFound();
                    return;

                }

                if (!source.canRead()){
                    sourceNotRead();
                    return;
                }

                if (source.isDirectory()){
                    copyFolder(newFolders);
                    return;
                }

                if (dest.exists()){
                    if (!isOverwrite()){
                        destinationAlreadyExists();
                        return;
                    }
                    freeShell.printLog(dest.getAbsolutePath() + " was overwritten");
                }else
                    freeShell.printLog(dest.getAbsolutePath() + " was create");

                InputStream is = null;
                OutputStream os = null;
                try {
                    long len = source.length();
                    byte[] buffer;
                    if (len > 0xffff)
                        buffer = new byte[0xffff];
                    else
                        buffer = new byte[(int)len];
                    is = getInputStream(source);
                    os = new FileOutputStream(dest);

                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    complete(source, true);
                }finally{
                    try {
                        is.close();
                    }catch (IOException e){
                        freeShell.log(e);
                    }
                    try {
                        os.close();
                    }catch (IOException e){
                        freeShell.log(e);
                    }

                }
            }

            public void copyFolder(Map<String, File> newFiles) {
                File[] files = source.listFiles();
                if (dest.exists()){
                    if (!dest.isDirectory()) {
                        if (isOverwrite()) {
                            dest.delete();
                            freeShell.printLog("File " + dest.getAbsolutePath() + " was deleted");
                            dest.mkdir();
                            newFiles.put(dest.getAbsolutePath(), dest);
                        } else {
                            new Exception("Can't create dir " + dest.getAbsolutePath());
                        }
                    }
                }else {
                    dest.mkdir();
                    newFiles.put(dest.getAbsolutePath(), dest);
                    freeShell.printLog(dest.getAbsolutePath() + " was created");
                }
                boolean newAction = true;
                for (File file : files) {
                    if (file != null) {
                        if (!newFiles.containsKey(file.getAbsolutePath())) {
                            File newDest = new File(dest.getAbsolutePath() + '/' + file.getName());
                            if (file.isDirectory())
                                newFiles.put(newDest.getAbsolutePath(), newDest);
                            if (!newAction(file, newDest))
                                newAction = false;
                        }
                    }
                }
                complete(source, newAction);
            }


            private void sourceNotRead() throws Exception {
                throw new Exception("Source not read: " + source.getAbsolutePath());
            }


            private void destinationAlreadyExists() throws Exception {
                throw new Exception("Destination already exists: " + dest.getAbsolutePath());
            }

            void sourceNotFound() throws Exception {
                throw new Exception("Source not found: " + source.getAbsolutePath());
            }


        }


        class OverwriteAction extends CopyAction{
            OverwriteAction(File source, File dest) {
                super(source, dest);
            }
            boolean newAction(File src, File dst) {
                String p = src.getAbsolutePath();
                synchronized (FSync.this) {
                    if (!exceptions.containsKey(p)) {
                        putAction(new OverwriteAction(src, dst));
                        return true;
                    } else {
                        removeException(p);
                        return false;
                    }
                }
            }

            boolean isOverwrite(){
                return true;
            }
            @Override
            public String toString(){
                return "Overwrite " + source.getAbsolutePath() + " to " + dest.getAbsolutePath();
            }
        }
        class MoveAction extends CopyAction{
            MoveAction(File source, File dest) {
                super(source, dest);
            }
            boolean newAction(File src, File dst) {
                String p = src.getAbsolutePath();
                synchronized (FSync.this) {
                    if (!exceptions.containsKey(p)) {
                        putAction(new MoveAction(src, dst));
                        return true;
                    } else {
                        removeException(p);
                        return false;
                    }
                }

            }

            @Override
            synchronized void complete(File f, boolean hasNextAction){
                if (!hasNextAction)
                    return;
                String p = f.getAbsolutePath();
                removeSelect2(p);
                synchronized (FSync.this) {
                    if (!exceptions.containsKey(p)) {
                        if (f.isDirectory()) {
                            dirsToDelete.addElement(f);
                        } else {
                            filesToDelete.put(p, f);
                        }
                    } else {
                        removeException(p);
                    }
                }
            }
            @Override
            public String toString(){
                return "Move " + source.getAbsolutePath() + " to " + dest.getAbsolutePath();
            }
        }

        class MoveOverwriteAction extends MoveAction{
            MoveOverwriteAction(File source, File dest) {
                super(source, dest);
            }
            boolean newAction(File src, File dst) {
                String p = src.getAbsolutePath();
                synchronized (FSync.this) {
                    if (!exceptions.containsKey(p)) {
                        putAction(new MoveOverwriteAction(src, dst));
                        return true;
                    } else {
                        removeException(p);
                        return false;
                    }
                }

            }


            boolean isOverwrite(){
                return true;
            }
            @Override
            public String toString(){
                return "Move&overwrite " + source.getAbsolutePath() + " to " + dest.getAbsolutePath();
            }

        }

        void  error(final Throwable t, Action action){
            pause = true;
            freeShell.runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);

                    builder.setTitle("Error: " + t.getMessage())
                            .setMessage("Do you want to continue?")
                            .setCancelable(true)
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try{
                                        synchronized (FSync.this) {
                                            actions.clear();
                                            dirsToDelete.clear();
                                            filesToDelete.clear();
                                            pause = false;
                                        }
                                    }catch (Exception e){
                                        freeShell.printError(e);
                                    }
                                }
                            }).setNeutralButton("Repeat", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try{
                                        synchronized (FSync.this) {
                                            if (action != null) {
                                                putAction(action);
                                            }
                                            pause = false;
                                        }
                                    }catch (Exception e){
                                        freeShell.printError(e);
                                    }
                                }
                            })

                            .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);

                                    builder.setTitle("Error: " + t.getMessage())
                                            .setMessage("Please choice next action")
                                            .setCancelable(true)
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try{
                                                        synchronized (FSync.this) {
                                                            actions.clear();
                                                            dirsToDelete.clear();
                                                            filesToDelete.clear();
                                                            pause = false;
                                                        }
                                                    }catch (Exception e){
                                                        freeShell.printError(e);
                                                    }
                                                }
                                            })
                                            .setNeutralButton("Skip", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    synchronized (FSync.this) {
                                                        pause = false;
                                                    }

                                                };

                                            })
                                            .setPositiveButton("Skip All", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    synchronized (FSync.this) {
                                                        skipAll = true;
                                                        pause = false;
                                                    }

                                                };

                                            });

                                    AlertDialog alert = builder.create();
                                    alert.show();

                                };

                            });

                    AlertDialog alert = builder.create();
                    alert.show();



                }
            });

        }

        synchronized void removeSelect2(String p){
            if (selected.remove(p) != null){
                superParentException.remove(p);
                invalidate(p);
            }

        }

    public synchronized void del(File f){

        if (!f.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);

            builder.setTitle("Error")
                    .setMessage("The file not exists.")
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }
        AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);

        builder.setTitle("Are you sure?")
                .setMessage("Do you want to delete " + f.getName() + " from " + f.getParent() + "?")
                .setCancelable(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try{

                                synchronized (FSync.this) {
                                    try {
                                        pause = true;
                                        putAction(new DelAction(f));
                                    }finally {
                                        pause = false;
                                    }
                                }

                        }catch (Exception e){
                            freeShell.printError(e);
                        }

                    };

                });

        AlertDialog alert = builder.create();
        alert.show();


    }

    public synchronized void del(FreeShell.FileAdapter fileAdapter){
        final ArrayList<File> arrayList = new ArrayList(selected.size());
        File dir = fileAdapter.getCurrentDir();

        File[] files = dir.listFiles();
        for(File file: files){
            if (isSelect(file)) {
                arrayList.add(file);
            }
        }
/*        for(File file: arrayList){
            removeSelect(file);
        }*/

        //Traveler traveler = new Traveler();
        //traveler.del(arrayList);

        if (arrayList.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);

            builder.setTitle("Error")
                    .setMessage("Have no selected files in current folder '" + dir.getAbsolutePath() + "'")
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }
        String path = fileAdapter.getStorageList().getDir();
        AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);

        builder.setTitle("Are you sure?")
                .setMessage("Do you want to delete " + arrayList.size() + " files from " + path + "?")
                .setCancelable(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try{

                                synchronized (FSync.this) {
                                    try {
                                        pause = true;
                                        for (File file : arrayList) {
                                            putAction(new DelAction(file));
                                        }
                                    }finally {
                                        pause = false;
                                    }
                                }

                        }catch (Exception e){
                            freeShell.printError(e);
                        }

                    };

                    });

        AlertDialog alert = builder.create();
        alert.show();



    }

/*

    public long copy(File source, File dir, boolean overwrite, CopyInfo copyInfo, boolean delete) {
        try {
            if (!dir.isDirectory()) {
                freeShell.printError("Destination must be directory");
                return 0;
            }
            if (source.isDirectory()) {
                return copyFolder(source, dir, overwrite, copyInfo, delete);
            } else {
                if (source.exists())
                    copyInfo.amount++;
                else {
                    copyInfo.error++;
                    freeShell.printError("Not found: " + source.getAbsolutePath());
                    return 0;
                }

                File dst = new File(dir.getAbsolutePath() + '/' + source.getName());
                if (dst.exists()) {
                    if (overwrite) {
                        dst.delete();
                    } else {
                        copyInfo.error++;
                        freeShell.printError("File " + dst.getAbsolutePath() + " already exists");
                        return 0;
                    }
                }

                Long ret = copyFile(source, dst, delete);
                if (ret == null) {
                    copyInfo.error++;
                    return 0;
                } else {
                    copyInfo.copied++;
                    return ret;
                }
            }
        }catch (Throwable t){
            freeShell.printError(t.getMessage());
            copyInfo.error++;
            return  0;
        }
    }*/


    class ZipAction implements Action{
        File src;
        Zip zip;
        int node;
        ZipAction(File src, Zip dstZip, int node){
            this.src = src;
            this.zip = dstZip;
            this.node = node;
        }
        @Override
        public void run(Map<String, File> newFolders) throws Exception {
            zip.zip(src, node);
            removeSelect2(src.getAbsolutePath());
        }

        @Override
        public File getSource() {
            return src;
        }
    }


    class Zip{
        ZipOutputStream out;
        byte data[];

        Zip(File f) throws Exception {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(f.getAbsolutePath())));
            data = new byte[BUFFER_SIZE];
        }

        boolean newAction(File src, Zip zip, int node) {
            String p = src.getAbsolutePath();
            synchronized (FSync.this) {
                if (!exceptions.containsKey(p)) {
                    putAction(new ZipAction(src, zip, node));
                    return true;
                } else {
                    removeException(p);
                    return false;
                }
            }
        }
        void zip(File src, int node) throws Exception {
            String path = src.getAbsolutePath();
            path = path.substring(node);
            if (src.isDirectory()){
                ZipEntry entry = new ZipEntry(path + "/");
                out.putNextEntry(entry);
                File[] files = src.listFiles();
                for(File file: files){
                    newAction(file, this, node);
                }
                return;
            }

            FileInputStream fi = new FileInputStream(src);
            BufferedInputStream origin = new BufferedInputStream(fi, BUFFER_SIZE);
            try {
                ZipEntry entry = new ZipEntry(path);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                    out.write(data, 0, count);
                }
            }
            finally {
                origin.close();
            }

        }

        void close() throws IOException {
            out.close();
        }
    }

    public synchronized List<File> popSelectedListFroEmail(){
        File[] selected = this.selected.value().toArray();
        ArrayList<File> ret = new ArrayList(selected.length);
        boolean err = false;
        for (File src : selected) {
            if (src.isFile()){
                removeSelect(src);
                ret.add(src);
                Toast.makeText(freeShell, src.getName() + " was attached.", Toast.LENGTH_LONG).show();
            }else{
                err = true;
            }
        }
        if (err){
            freeShell.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            String title ="Warning";
                            String m = "Some selected directories were missed. Yur can attach only files.";

                            AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                            builder.setTitle(title)
                                    .setMessage(m)
                                    .setCancelable(true)
                                    .setPositiveButton("View", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            freeShell.showSelected();
                                        }
                                    }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    });

        }
        return ret;
    }

    static int BUFFER_SIZE = 65536;
    Stack<Zip> zips = new Stack();
    public synchronized  void createZip(File dstZip, final FreeShell.Adapter adapter) throws Exception {
        if ((dstZip == null) || dstZip.exists()){

            invalidPlace(adapter);
            return;
        }
        try {
            pause = true;
            Zip zip = new Zip(dstZip);
            zips.add(zip);
            File[] selected = this.selected.value().toArray();
            for (File src : selected) {
                if (isSelect2(src, src.getAbsolutePath()) == null) {
                    String parent = src.getParent();
                    int node;
                    if (parent == null)
                        node = 0;
                    else
                        node = parent.length();
                    putAction(new ZipAction(src, zip, node));
                }
            }

        }finally {
            pause = false;
            if (adapter != null){
                freeShell.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.unLock();
                    }
                });
            }
        }

    }

    public synchronized void extract(InputStream in, File dir){
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(dir.getAbsolutePath(), false);
        } catch (FileNotFoundException e) {
            freeShell.log(e);
        }
        try {
            for (int c = in.read(); c != -1; c = in.read()) {
                fout.write(c);
            }
            in.close();
        } catch (IOException e) {
            freeShell.log(e);
        } finally {
            try {
                fout.close();
            } catch (IOException e) {
                freeShell.log(e);
            }
        }

    }


    public synchronized void unzip(File zipFile, File location, FreeShell.FileAdapter adapter) throws IOException {
        adapter.lock();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //String location = FreeShell.getFileName(zipFile.getName());
                    if (location.exists() && (!location.isDirectory())){
                        invalidPlace(adapter);
                        return;
                    }
                    ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
                    try {
                        ZipEntry ze = null;
                        while ((ze = zin.getNextEntry()) != null) {
                            String path = location.getAbsolutePath() + '/' + ze.getName();

                            if (ze.isDirectory()) {
                                File unzipFile = new File(path);
                                if(!unzipFile.isDirectory()) {
                                    unzipFile.mkdirs();
                                }
                            }
                            else {
                                FileOutputStream fout = new FileOutputStream(path, false);
                                try {
                                    int avail = zin.available();
                                    while (avail > 1){
                                        byte[] buf = new byte[avail];
                                        zin.read(buf);
                                        fout.write(buf);
                                        avail = zin.available();
                                    }
                                    for (int c = zin.read(); c != -1; c = zin.read()) {
                                        fout.write(c);
                                    }
                                    zin.closeEntry();
                                }
                                finally {
                                    fout.close();
                                }
                            }
                        }
                    }
                    finally {
                        zin.close();
                        freeShell.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.unLock();
                            }
                        });
                    }
                    freeShell.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.openSomeDir(location);
                            invalidate(zipFile.getAbsolutePath());
                        }
                    });

                }
                catch (Exception e) {
                    freeShell.printError(e);

                }

            }
        }).start();
    }



    public interface FoundFileListener{
        void found(int countDir, int countFiles, Vector<File> result);
        void complete(int countDir, int countFiles, Vector<File> result);
        boolean needNext();
    }


    class FileFinder extends FileNameFinder{

        FileFinder(List<File> sources, FoundFileListener listener, String toFind) {
            super(sources, listener, toFind);
        }

        @Override
        boolean findInFile(File f){
            if (f.isDirectory())
                return false;
            String ext = FreeShell.getFileExt(f.getName());
            if (freeShell.isText(ext)){
                countFiles++;
                try {
                    BufferedReader b;
                    if (f instanceof ZipList.File2 )
                        b = new BufferedReader(new InputStreamReader(((ZipList.File2) f).getInputStream()));
                    else
                        b = new BufferedReader(new FileReader(f));
                    String readLine;
                    while ((readLine = b.readLine()) != null) {
                        if (readLine.indexOf(toFind) >= 0) {
                            return true;
                        }
                    }
                    try {
                        b.close();
                    } catch (Throwable t) {
                    }
                } catch (IOException e) {
                    //freeShell.log(e);
                }
            }
            return false;
        }

        void checkCounter(){
            if (counter == 8){
                counter = 0;
                freeShell.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (!listener.needNext()){
                                    return;
                                }
                                listener.found(countDir, countFiles, result);
                            }
                        });

            }
            counter++;
        }

//        freeShell.mapThreads().getFileThread().putRunnable(new FolderCalculator(fileItem));
    }







    class FileNameFinder implements Runnable{

          boolean run = true;
        List<File> sources;
        FoundFileListener listener;
        String toFind;
        int countDir, countFiles, counter = 0;
        FileNameFinder(List<File> sources, FoundFileListener listener, String toFind){
            this.sources = sources;
            this.toFind = toFind;
            this.listener = listener;
        }
        Vector<File> result = new Vector<>();

        boolean findInFile(File f){
            countFiles++;
            return f.getName().toLowerCase().indexOf(toFind) >= 0;
        }

        void checkCounter(){
            if (counter == 0x100){
                counter = 0;
                freeShell.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (!listener.needNext()){
                                    return;
                                }
                                listener.found(countDir, countFiles, result);
                            }
                        });

            }
            counter++;
        }

        @Override
        public void run() {
            for(File f: sources){
                if (f.exists()){
                    if (findInFile(f)) {
                        result.add(f);
                    }

                    if (f.isDirectory()){
                        runDirectory(f);
                    }
                }
                checkCounter();
            }
            freeShell.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            listener.complete(countDir, countFiles, result);
                        }
                    });

        }


        public void runDirectory(File directory) {
            countDir++;
            Array<File> arr = new Array(File.class, 32);
            arr.addElement(directory);
            while (run && (arr.size() > 0)) {
                if (!listener.needNext()){
                    run = false;
                }
                File dir = arr.pop();
                while ((arr.size() > 0) && (!dir.exists())){
                    dir = arr.pop();
                }
                if (!dir.exists())
                    break;
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!listener.needNext()){
                            run = false;
                        }

                        if (file != null) {
                            if (file.isFile()) {
                                if (findInFile(file)) {
                                    result.add(file);
                                }
                            }else
                                arr.addElement(file);
                        }
                        checkCounter();
                    }
                }
            }
        }

    }
    public synchronized void findInFileNames(List<File> sources, FoundFileListener listener, String toFind) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new FileNameFinder(sources, listener, toFind).run();
            }
        }).start();
//        freeShell.mapThreads().getFileThread().putRunnable(new FolderCalculator(fileItem));
    }


    public synchronized void findInFiles(List<File> sources, FoundFileListener listener, String toFind) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new FileFinder(sources, listener, toFind).run();
            }
        }).start();
//        freeShell.mapThreads().getFileThread().putRunnable(new FolderCalculator(fileItem));
    }

}


/*
                freeShell.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                        // Specify the dialog is not cancelable
                        builder.setCancelable(false);

                        // Set a title for alert dialog
                        builder.setTitle("The file is already in the work, what you prefer?");
                        builder.setItems(new String[]{
                                old.toString(),
                                run.toString()
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    actions.put(path, old);
                                }else{
                                    actions.put(path, run);
                                }
                            }
                        });
                        builder.show();


                        // Set the negative/no button click listener
                        builder.setNegativeButton("Cancel all", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                actions.clear();
                            }
                        });


                        AlertDialog dialog = builder.create();
                        // Display the alert dialog on interface
                        dialog.show();

                    }
                });

 */