package mtk.free.shell;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.storage.StorageManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.mtk.map.Array;
import com.mtk.map.BaseRoot;
import com.mtk.map.HTable;
import com.mtk.map.IOTable;
import com.mtk.map.IVector;
import com.mtk.map.IntIntTable;
import com.mtk.map.LiveVector;
import com.mtk.map.Tag;
import com.mtk.map.HashArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import mtk.map.MapThreads;
import mtk.util.XParser;

import static android.view.Gravity.NO_GRAVITY;
import static android.view.View.VISIBLE;
import static mtk.free.shell.StorageList.stringCompare;

//need go to current file by mp3 button
//after close nto lighting while playing
//the change position button  make blue like other button
//find email while send email

public class FreeShell extends Activity implements SurfaceHolder.Callback{

/*
This application is simple, but covers 90% of regular actions with the device and can save a lot your time after your understand the simple philosophy of the innovative interface. The application allows you to quickest adding a new contact, make calls, send SMS to several numbers, send E-mail, also launch, grouping and settings other applications. The application contains the original high precision calculator, memorizing constants and allowing you to make comments calculations. Also, FreeShell contains the note book, FS-oriented mp3 player and the original file manager allowing faster and more convenient to work with files to those who understand it. You always have at hand the buttons of the camera and the "my location" button. Also you may run linux commands.
The application does not contain advertising and is focused on business people who do not like to be distracted and who are used to reading the message on himself initiative only.
*/

    Camera camera;
    SurfaceView svPreview;
    SurfaceHolder previewHolder;
    FreeShell freeShell = this;
    HashMap<String, TextAdapter> openedTextFile = new HashMap();
//    boolean flashState = false;
    final int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    private final boolean ADS = false;
    boolean isLoop = false, isRandom = false;
    public void save(PrintWriter writer) {
        //String f = getMyFileName("text");
        writer.println("<text>");
        String s = text().getText().toString();
        writer.println(encode(s));
        //fSync().writeFile(f, text().getText().toString());
        writer.println("</text>");

        if (firstScreen().getTextAdapter() != null){
            if (!firstScreen().getTextAdapter().closed) {
                writer.println("<edit>");
                String f = firstScreen.getCurrentTextFile();
                if (f != null)
                    writer.println("editFile=" + f);
                writer.println("line=" + firstScreen.edited.globalPosition);
                writer.println("index=" + firstScreen().getTextAdapter().listView().index);

                writer.println("</edit>");
                /*writer.println("textFileName=" + firstScreen().fileName);
                writer.println("textFilePath=" + firstScreen().path);*/
            }
        }
        if ((fSync != null) && fSync.hasSelected()) {
            writer.println("<selected>");
            String[] sel = fSync().getSelected();
            for(String path: sel){
                writer.println(path);
            }
            writer.println("</selected>");

        }
        writer.println("isRandom=" + isRandom);
        writer.println("isLoop=" + isLoop);

        if (audioServiceBinder != null){
            writer.println("<mp3>");
            int seekTo = audioServiceBinder.getCurrentAudioPosition();
            writer.println("seekTo=" + seekTo);
            writer.println("playing=" + playing);
            writer.println("mp3FilePosition=" + audioServiceBinder.getFilePosition());
            List<File> list = audioServiceBinder.getFileList();
            if (notEmpty(list)) {
                writer.println("<mp3Files>");
                for (File f : list) {
                    writer.println(f.getAbsolutePath());
                }
                writer.println("</mp3Files>");
            }
            writer.println("</mp3>");
        }

    }

    public void start(Tag tag){


        Tag mp3Tag = tag.getTagByLink("#mp3");
        if (mp3Tag != null) {
            int seekTo = mp3Tag.getIntByLink("#seekTo");
            int mp3FilePosition = mp3Tag.getIntByLink("#mp3FilePosition");
            final boolean oldPlaying = Boolean.parseBoolean(mp3Tag.getStringByLink("#playing"));

            Tag mp3Files = mp3Tag.getTagByLink("#mp3Files");
            br:if (mp3Files != null) {
                Array<String> sb = mp3Files.getText();
                if (sb == null){
                    break br;
                }
                Files files = new Files(sb.size());
                if ((sb != null) && (sb.size() > 0)) {
                    for (int i = 0; i < sb.size(); i++) {
                        String s = sb.elementAt(i);
                        File file = new File(s);
                        if (file.exists()){
                            if (!file.isDirectory())
                                files.add(file);
                        }
                    }
                    files.setPosition(mp3FilePosition);
                    audioAction = new Runnable() {
                        @Override
                        public void run() {
                            audioServiceBinder.setRandom(isRandom);
                            audioServiceBinder.setLoop(isLoop);
                            if (!audioServiceBinder.isPlaying()){
                                if (isConnectingToExistingService)
                                    playing = false;
                                else
                                    playing = oldPlaying;
                                if (playing){
                                    audioServiceBinder.start(files, seekTo);
                                }else{
                                    audioServiceBinder.ini(files, seekTo);
                                }
                            }else{
                                playing = true;
                            }
                            if (mp3Adapter != null){
                                mp3Adapter().setupPlayFile();
                            }
                            if (playing){
                                if (mp3Adapter != null){
                                    mp3Adapter.pause.setFlag(true);
                                    mp3Adapter.pause.button().invalidate();
                                }
                            }
                        }
                    };

                    if (audioServiceBinder != null) {
                        audioAction.run();
                        audioAction = null;
                    }


                }
            }

        }



    }

    public void load(Tag tag) {
        isRandom = Boolean.parseBoolean(tag.getStringByLink("#isRandom"));
        isLoop = Boolean.parseBoolean(tag.getStringByLink("#isLoop"));

        Tag tagText = tag.getTagByLink("#text");
        if (tagText != null) {
            Array<String> sb = tagText.getText();
            if (sb != null){
                for(int i = 0; i < sb.size(); i++){
                    String s = decode(sb.elementAt(i));
                    if (text().getText().length() > 0)
                        text().append("\n");
                    text().append(s);
                }
            }
        }
        Tag sel = tag.getTagByLink("#selected");
        if (sel!= null) {
            Array<String> sb = sel.getText();
            if ((sb != null)&&(sb.size() > 0)){
                int count = 0;
                for(int i = 0; i < sb.size(); i++) {
                    String s = sb.elementAt(i);
                    if (fSync().select(new File(s))) {
                        count++;
                    }
                }
                if (count > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FreeShell.this);
                    builder.setTitle("Warning")
                            .setMessage("Some selected files are found")
                            .setCancelable(true)
                            .setPositiveButton("View", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e) {
                                                Thread.yield();
                                            }
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    showSelected();
                                                }
                                            });

                                        }
                                    }).start();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }

        }


    }

    String variablesFileName(){
        return myDir() + "/lastVariables.var";
    }
    synchronized boolean saveVariables(){
        return saveVariables(variablesFileName());
    }
    synchronized HashMap<String, BigDecimal> loadVariables(){
        return loadVariables(variablesFileName());
    }
    synchronized boolean saveVariables(String name){
        if (!name.endsWith(".var")){
            name = name + ".var";
        }
        Set<Map.Entry<String, BigDecimal>> set = variables.entrySet();
        File f = new File(name);
        File parent = f.getParentFile();
        if (!parent.exists()){
            parent.mkdirs();
            refreshDirs(parent.getParent());
        }
        try {
            PrintWriter printWriter = new PrintWriter(f);
            for(Map.Entry entry: set){
                printWriter.println(entry.getKey().toString() + "=" + entry.getValue().toString());
            }
            printWriter.close();
        } catch (Exception e) {
            return false;
        }
        return true;

    }

    synchronized HashMap<String, BigDecimal> loadVariables(String name){
        HashMap<String, BigDecimal> ret = new HashMap();
        List<String> list = fileToArray(name);
        for (String s : list) {
            int i = s.indexOf('=');
            if (i > 0){
                try {
                    String let = s.substring(0, i).trim();
                    String val = s.substring(i + 1).trim();
                    ret.put(let, new BigDecimal(val));
                }catch (Throwable t){

                }

            }
        }
        return ret;
    }


    //will delete "(" ")" "fs" "app" because app by default and fs by menu. add "saveMtk"  "pause"  "stop"  - last two
    // with pictures. add search by all types


    interface Procedure {
        void procedure();
    }

    interface SetFile{
        void setFile(File f);
    }
    interface Function {
        boolean function();
    }

    /*
    <ScrollView
      xmlns:android="http://schemas.android.com/apk/res/android"
      android:layout_width="fill_parent"
      android:layout_height="fill_parent">
        <LinearLayout
          android:layout_width="fill_parent"
          android:layout_height="fill_parent">
            <!-- bla bla bla-->
        </LinearLayout>
    </ScrollView>
     */

    class Layout{
        int h, w;
        int x, y;
    }

    class CustomAction{


        RelativeLayout.LayoutParams params;
        View view;
        public String toString(){
            if (view != null) {
                if (view instanceof  TextView)
                    return ((TextView) view).getText().toString();
                return view.toString();
            }
            return "view == null";
        }

        void init(int w, int h, View view) {
            if (w == h){
                if (textButtonParams0 == null){
                    textButtonParams0 = new RelativeLayout.LayoutParams(w, h);
                }
                params = textButtonParams0;
            }else{
                if (textButtonParams1 == null){
                    textButtonParams1 = new RelativeLayout.LayoutParams(w, h);
                }
                params = textButtonParams1;
            }
            if ((params.width != w)||(params.height != h)) {
                params = new RelativeLayout.LayoutParams(w, h);
                params.addRule(RelativeLayout.CENTER_VERTICAL);
            }

            this.view = view;
        }


    }


    RelativeLayout.LayoutParams textButtonParams0;
    RelativeLayout.LayoutParams textButtonParams1;

    abstract class CustomButton extends CustomAction {

        CustomButton(){}

        public CustomButton(CharSequence text, Procedure procedure, int color, int background) {
            init(text, procedure, color, background);


        }
        Mp3Adapter.Toucher toucher;
        Boolean flag;
        int background;
        void setFlag(boolean v) {

            if ((flag == null)||(v != flag)) {
                background = v ? bOrange : bOff;
                button().setBackgroundColor(background);
                button().setTextColor(v ? cOrange : cOff);
                flag = v;
            }
        }

        public CustomButton(CharSequence text, Procedure procedure, boolean flag) {
            init(text, procedure, cOrange, bOrange);
            setFlag(flag);
        }

        int getWb(){
            return FreeShell.this.wb - (FreeShell.this.wb >> 2);
        }

        int normalize(CharSequence text){
            int w;
            br: if (text.length() < 4) {
                w = FreeShell.this.hb;
                if (text.toString().indexOf("<") >= 0)
                    break br;
                if (text.toString().indexOf(">") >= 0)
                    break br;

                if (text.length() < 3){
                    text = Html.fromHtml("<big><big>" + text +"</big></big>");
                }
            } else {
                w = getWb();
            }
            return w;
        }

        public void init(CharSequence text, Procedure procedure, int color, int background) {
            this.background = background;
            Button button = new Button(FreeShell.this);
            button.setAllCaps(false);
            button.setClickable(true);
            button.setTextAppearance(FreeShell.this, R.style.fontButton);
            int w = normalize(text);
            init(w, hb, button);
            button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (toucher != null){
                                toucher.resetActionUpFlag();
                                return;
                            }
                            button.setBackgroundColor(bPleaseWait);
                            button.setClickable(false);
                            button.invalidate();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            procedure.procedure();
                                            button.setBackgroundColor(CustomButton.this.background);
                                            button.setClickable(true);
                                            button.invalidate();
                                        }
                                    });

                                }
                            }).start();

                        }catch(Exception t){
                            printError(t);
                        }
                    }
                }

            );
            view = button;


            button.setBackgroundColor(background);
            button.setTextColor(color);

            button.setText(text);

        }

        void setOnTouchListener(Mp3Adapter.Toucher l){
            button().setOnTouchListener(l);
            l.setButton(this);
        }

        Button button() {
            return (Button) view;
        }

        public void pleaseWait(){
            Button button = button();
            button.setBackgroundColor(bPleaseWait);
            button.setClickable(false);
            button.invalidate();
        }

        public void pleaseEnd(){
            Button button  = button();
            button.setBackgroundColor(CustomButton.this.background);
            button.setClickable(true);
            button.invalidate();
        }

    }

    class ShellButton extends CustomButton {
        public ShellButton(){}

        public ShellButton(CharSequence text, Procedure procedure, int color, int background, int line,  ButtonPanel buttonPanel) {
            super(text, procedure, color, background);
            buttonPanel.addLine(this, line);
        }



        @Override
        int getWb(){
            return FreeShell.this.wb;
        }
        public ShellButton(CharSequence text, Procedure procedure, boolean flag, int line, ButtonPanel buttonPanel) {
            super(text, procedure, flag);
            buttonPanel.addLine(this, line);
        }


    }


    static int cGreen = Color.argb(240, 0, 16, 0);
    static int bGreen = Color.argb(96, 128, 255, 128);

    static int cGreen1 = Color.argb(240, 0, 16, 16);
    static int bGreen1 = Color.argb(96, 64, 255, 192);

    static int cBlue = Color.argb(240, 0, 0, 96);
    static int bBlue = Color.argb(96, 0, 192, 255);
    static int bPleaseWait = Color.argb(64, 128, 192, 255);
    static int cCyan = Color.argb(240, 0, 0, 96);
    static int bCyan = Color.argb(96, 64, 255, 255);
    static int cBlack = Color.argb(240, 255, 255, 255);
    static int bBlack = Color.argb(96, 0, 0, 0);
    static int cRose = cBlue;//Color.argb(240, 0, 32, 0);
    static int bRose = Color.argb(96, 255, 192, 255);
    static int cGray = Color.argb(240, 0, 0, 0);
    static int bGray = Color.argb(96, 128, 128, 128);

    static int cLight = Color.argb(240, 32, 32, 32);
    static int bLight = Color.argb(96, 192, 192, 192);
    static int cRed = Color.argb(240, 32, 0, 0);
    static int bRed = Color.argb(96, 255, 128, 128);
    static int cRed1 = Color.argb(240, 0, 0, 32);
    static int bRed1 = Color.argb(96, 255, 192, 192);

    static int cMangeta = Color.argb(240, 32, 0, 32);
    static int bMangeta = Color.argb(96, 255, 128, 255);

    static int cBrown = Color.argb(240, 16, 16, 0);
    static int bBrown = Color.argb(96, 192, 192, 96);

    static int cYellow = Color.argb(240, 16, 16, 0);
    static int bYellow = Color.argb(96, 255, 192, 128);
    static int bYellow2 = Color.argb(96, 255, 160, 216);

    static int cYellow1 = cGreen;
    static int bYellow1 = Color.argb(96, 255, 255, 192);

    static int cOrange = Color.argb(240, 48, 24, 0);
    static int bOrange = Color.argb(96, 255, 192, 0);


    int bSel = 0xFF000060;
    int cSel = 0xFFFFFF40;
    int bSel1 = Color.argb(96, 255, 255, 0x40);
    int cSel1 = Color.argb(240, 00, 00, 0x60);

    static int bOff = bGray;
    static int cOff = cGray;
    static int bError = bRed;
    static int cError = cRed;

    static int bText = bGray;
    static int cText = cGray;

    int c0 = Color.argb(240, 32, 32, 0);
    int b0 = Color.argb(96, 255, 255, 192);

    int max;
    public void init() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        DisplayMetrics dm = getResources().getDisplayMetrics();
        displayMetrics = dm;
        Button button = new Button(this);
        button.setTextAppearance(FreeShell.this, R.style.fontButton);
        button.setText("Hello world!");
        button.measure(
                makeMeasureSpec,

                makeMeasureSpec);
        hb = button.getMeasuredHeight() +  (hb >> 2);
        //hb = (hb << 1) - (hb >> 1) - (hb >> 2);
        wb = (hb << 1) - (hb >> 2);
        margin = hb >> 5;
        wb2 = wb >> 1;
        hb2 = hb >> 1;

        if (dm.heightPixels > dm.widthPixels) {
            max = dm.heightPixels + (dm.heightPixels>>1);
            listW = dm.widthPixels - hb;
        } else {
            max = dm.widthPixels + (dm.widthPixels>>1);
            listW = dm.heightPixels - hb;
        }
        myLayout();

        orientation = getResources().getConfiguration().orientation;

        firstScreen().init();

    }
    EditText text;


    static class ArithmeticException2 extends Exception{
        ArithmeticException2(String s){
            super (s);
        }
    }

    public BigDecimal eval2(final String str) throws ArithmeticException2 {
        return new Object() {
            int pos = -1;
            Character ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : null;
            }

            boolean eat(int charToEat) {
                while ((ch != null)&&(ch == ' ')) nextChar();
                if ((ch != null)&&(ch == charToEat)) {
                    nextChar();
                    return true;
                }
                return false;
            }

            boolean notEat(int charToEat) {
                int pos = this.pos;
                if (pos == str.length())
                    return false;
                char c = str.charAt(pos);
                while (c == ' '){
                    pos++;
                    c = str.charAt(pos);
                }
                return(c == charToEat);
            }

            BigDecimal parse() throws ArithmeticException2 {
                nextChar();
                BigDecimal x = parseExpression();
/*                StringBuilder sb = new StringBuilder("Unexpected symbol: ");
                sb.append(ch);*/
                if (pos < str.length()) throw new ArithmeticException2("Unexpected symbol: " + str.charAt(pos));
                return x;
            }


            BigDecimal parseExpression() throws ArithmeticException2 {
                BigDecimal x = parseTerm();
                for (;;) {
                    if (eat('+')) {
                        BigDecimal t =parseTerm();
                        x = x.add( t, MathContext.DECIMAL128); // addition
                    }else if (eat('-')) {
                        BigDecimal t =parseTerm();
                        x = x.subtract(t, MathContext.DECIMAL128); // subtraction
                    }else
                        return x;
                }
            }

            BigDecimal parseTerm() throws ArithmeticException2 {
                BigDecimal x = parseFactor();
                for (;;) {
                    if      (eat('*')) x = x.multiply(parseFactor(), MathContext.DECIMAL128);
                        //x *= parseFactor(); // multiplication
                    else if (eat('/')){
                        BigDecimal f = parseFactor();
                        BigDecimal f0 = f.setScale(round, RoundingMode.HALF_UP);
                        if (f0.compareTo(BigDecimal.ZERO) == 0){
                            throw new ArithmeticException2("Division by zero");
                        }
                        x = x.divide(f, MathContext.DECIMAL128);
                   }else if (eat('^')){
                        BigDecimal factor = parseFactor();
                        if (factor.doubleValue() == factor.intValue()){
                            x = x.pow(factor.intValue(), MathContext.DECIMAL128); // exponentiation
                        }else
                            x = BigDecimal.valueOf(Math.pow(x.doubleValue(), factor.doubleValue())); // exponentiation
                    }else return x;
                }
            }

            BigDecimal parseFactor() throws ArithmeticException2{
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return parseFactor().negate(MathContext.DECIMAL128); // unary minus
                BigDecimal x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch != null) && ((ch >= '0' && ch <= '9') || ch == '.')) { // numbers
                    while ((ch != null) && ((ch >= '0' && ch <= '9') || ch == '.')) nextChar();
                    x = BigDecimal.valueOf(Double.parseDouble(str.substring(startPos, this.pos)));
                } else if ((ch != null) && ((ch >= 'A' && ch <= 'Z')||(ch >= 'a' && ch <= 'z') || specLeft(ch))) { // functions
                    String name;
                    if (specLeft(ch)){
                        name = "" + ch;
                        nextChar();
                    }else {
                        while ((ch != null) && ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')))
                            nextChar();
                        name = str.substring(startPos, this.pos);
                    }
                    if (notEat('(')){
                        x = parseFactor();
                        if (name.equals("√")) {
                            x = BigDecimal.valueOf(Math.sqrt(x.doubleValue())); // exponentiation
                        }else if (name.equals("°")) {
                            x = BigDecimal.valueOf(Math.toDegrees(x.doubleValue())); // exponentiation
                        }else {

                            try {
                                Method method = Math.class.getMethod(name, double.class);
                                Object dd = method.invoke(null, x.doubleValue());
                                if (dd == null)
                                    throw new ArithmeticException2("Unknown function: " + name);
                                x = new BigDecimal(dd.toString());
                                //x = BigDecimal.valueOf();
                            } catch (NoSuchMethodException e) {
                                throw new ArithmeticException2("Unknown function: " + name);
                            } catch (InvocationTargetException e) {
                                throw new ArithmeticException2("Unknown function: " + name);
                            } catch (IllegalAccessException e) {
                                throw new ArithmeticException2("Unknown function: " + name);
                            }
                        }
                    }else{
                        BigDecimal d =  FreeShell.this.variables.get(name);
                        if (d == null){
                            throw new ArithmeticException2("Unknown variable: " + name);
                        }else
                            x = d;
                    }


                } else {
                    if (pos < str.length()) {
                        throw new ArithmeticException2("Unexpected symbol: " + str.charAt(pos));
                    }else {
                        throw new ArithmeticException2("Unexpected: " + ch);
                    }
                }


                if (eat('°'))
                    x = BigDecimal.valueOf(Math.toRadians(x.doubleValue())); // exponentiation
                return x;
            }

            private boolean specLeft(Character ch) {
                switch(ch){
                    case '√':
                    case '°':
                        return true;
                }
                return false;
            }

        }.parse();
    }
    HashMap<String, BigDecimal> variables = newVariables();
    HashMap<String, BigDecimal> newVariables(){
        variables = new HashMap();
        variables.put("pi", BigDecimal.valueOf(Math.PI));
        variables.put("e", BigDecimal.valueOf(Math.E));
        variables.put("c", new BigDecimal("299792458"));

        return variables;
    }



    /*private void recalculate(){
        String text = text().toString();
        String[] ss = text.split("\n");
        text().getText().clear();
        if (ss != null){
            for(String s: ss){
                if (!mayBeExpression(s)){
                    continue;
                }
                try{
                    int i = s.indexOf('=');
                    String let, right;
                    if (i > 0){
                        let = s.substring(0, i).trim();
                        right = s.substring(i + 1).trim();
                    }else{
                        let = null;
                        right = s;
                    }
                    BigDecimal d = eval2(right);
                    if (let != null){
                        variables.put(let, d);
                    }
                    right = d.toString();

                }catch (Throwable e){
                    text().getText().append(e.getMessage() + "\n");
                }
                text().getText().append(s + "\n");

                text().getText().append(s +  "\n");

            }
        }
    }*/

    boolean containBadSymbols(String s){
        if (s == null)
            return true;
        if (s.length() == 0)
            return true;
        for(int i =0;i <s.length(); i++){
            if (!Character.isLetter(s.charAt(i)))
                return true;
        }
        return false;
    }

    String val(BigDecimal bigDecimal){
        bigDecimal = bigDecimal.setScale(round, RoundingMode.HALF_UP);
        return String.valueOf(bigDecimal);
    }

    private void eval() {
        String s = getLine().trim();
        if (!notEmpty(s))
            return;

      /*  if (!mayBeExpression(s))
            return;*/
        try{

            BigDecimal leftDecimal;
            try{
                leftDecimal = eval2(s);
                appendString(" = " + val(leftDecimal));

                nextLine();
                return;
            }catch (Throwable t) {

            }
            leftDecimal = variables.get(s);
            if (leftDecimal != null){
                pressChar(" = " + val(leftDecimal));

                nextLine();
                return;
            }
            int i = s.indexOf('=');
            String leftString = null;
            String equals = " = ";
            String rightString = null;
            BigDecimal rightDecimal= null;
            br:
            if (i < 0){
                leftString = s;
                leftDecimal = eval2(leftString);
            }else {
                rightString = s.substring(i + 1).trim();
                if (notEmpty(rightString)) {
                    if (containBadSymbols(rightString)) {
                        rightDecimal = eval2(rightString);
                    } else {
                        rightDecimal = variables.get(rightString);
                    }
                }else {
                    equals = "";
                    rightString = null;
                }

                leftString = s.substring(0, i).trim();
                if (notEmpty(leftString)) {
                    if (containBadSymbols(leftString)) {
                        leftDecimal = eval2(leftString);
                    } else {
                        leftDecimal = variables.get(leftString);
                    }
                }else {
                    equals = "";
                    leftString = null;
                }

            }
            if (leftDecimal == null){
                if (rightDecimal != null){
                    if (leftString != null) {
                        variables.put(leftString, rightDecimal);
                        String val = val(rightDecimal);
                        Toast.makeText(FreeShell.this, leftString + " = " + val, Toast.LENGTH_LONG).show();
                      }else{
                        addString(val(rightDecimal));
                    }
                }else{
                    printUpperError("Both variables not found.");
                }
            }else{
                if (rightDecimal != null){
                    int b = val(leftDecimal).compareTo(val(rightDecimal));
                    if (b == 0){
                        appendLine(val(leftDecimal) + " = " + val(rightDecimal));
                    }else if (b < 0){
                        appendLine(val(leftDecimal) + " < " + val(rightDecimal));
                    }else{
                        appendLine(val(leftDecimal) + " > " + val(rightDecimal));
                    }

                }else{
                    if (rightString != null) {
                        variables.put(rightString, leftDecimal);
                        String val = val(leftDecimal);
                        Toast.makeText(FreeShell.this, rightString + " = " + val, Toast.LENGTH_LONG).show();

                        appendLine(rightString + " = " + val);
                    }else{
                        appendString(equals + val(leftDecimal));
                    }
                }
            }
            nextLine();
/*                            if (s.length() > 0) {
                            try {
                                d = new BigDecimal(s);
                                variables.put(let, d);
                                Toast.makeText(FreeShell.this, let + "=" + s, Toast.LENGTH_LONG).show();
                                nextLine();
                                return;
                            } catch (Throwable tt) {
                                break br;
                            }
                        } else
                            equals = "";
                    }
                }else
                    let = null;
            }else
                let = null;
            try {
                d = eval2(s);

                s = d.round(round).toEngineeringString();
                if (let != null) {
                    variables.put(let, d);
                    appendLine(s + equals + s);
                    return;

                }else{
                    pressChar(equals + s);
                }
            }catch (ArithmeticException2 e){
                printUpperError(e.getMessage());
                return;
            }*/

        }catch (Throwable e){
            printUpperError(e.getMessage());
        }

    }



    private void minus() {

        text().getText().append('-');
        text.invalidate();
    }
    private void plus() {

        text().getText().append('+');
        text.invalidate();
    }

    private void fullPoint() {
        text().getText().append('.');
        text.invalidate();
    }

    private void let() {
        text().getText().append('=');
        text.invalidate();
    }

    ShellButton video;

    void watchVideo() {

    }
    //AutoCompleteTextView

    public SurfaceView svPreview() {
        if (svPreview == null) {

            svPreview = new SF(this);
            baseLayout().addView(svPreview);

            previewHolder = svPreview.getHolder();
            previewHolder.addCallback(this);

        }
        return svPreview;
    }

    boolean isPreview = true;
    TextView pleaseWait;
    TextView pleaseWait(){
        if (pleaseWait == null) {
            pleaseWait = new TextView(FreeShell.this);
            pleaseWait.setTextColor(Color.WHITE);
            pleaseWait.setText(Html.fromHtml("<big><big>Please wait...</big></big>"));
            pleaseWait.setX(0);
            pleaseWait.setY(0);
        }
        return pleaseWait;
    }

    boolean bred = false;
    ScrollView vScroll;
    ScrollView vScroll(){
            if (vScroll == null){
                vScroll = new ScrollView(FreeShell.this);
                vScroll.setScrollbarFadingEnabled(false);
                baseLayout().addView(vScroll, ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT);
            }
            return vScroll;
        }



    RelativeLayout baseLayout;
    RelativeLayout baseLayout(){
        //return myLayout();
        if (baseLayout == null){
            baseLayout = new RelativeLayout(FreeShell.this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            baseLayout.setLayoutParams(params);
            baseLayout.setBackgroundColor(Color.TRANSPARENT);
            setContentView(baseLayout);
        }
        return baseLayout;
    }
    RelativeLayout myLayout;
    public RelativeLayout myLayout() {

        if (myLayout == null) {

            /*int w = displayMetrics.widthPixels;
            int h = displayMetrics.heightPixels;
            if (h > w){
                w = h;
            }
            w = w << 1;
            h = w;*/


      /*      vScroll = new ScrollView(this);

            hScroll = new HorizontalScrollView(this);
            vScroll.addView(hScroll);*/
            myLayout = new RelativeLayout(this);
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            myLayout.setLayoutParams(params);
            myLayout.setBackgroundColor(Color.TRANSPARENT);
            vScroll().addView(myLayout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            //baseLayout().addView(vScroll);
            //setContentView(myLayout);
        }
        return myLayout;
    }


    ShellButton captureButton;

    void contactAndCall() {
    }


    class Contact {
        String text = "";
        List<String> addr = new ArrayList(2);
    }


    Contact extractEmails() {
        return extractEmails(getWholeText());
    }

    public static Array<String> getEmailsForSend(StringBuffer buf) {
        String line = buf.toString();
        final String RE_MAIL = "([\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Za-z]{2,4})";
        Pattern p = Pattern.compile(RE_MAIL);
        Matcher m = p.matcher(line);
        IntIntTable t = new IntIntTable(8);
        Array<String> ret = new Array<String>(String.class, 1);
        while (m.find()) {
            String email = m.group(1);
            if (isValidEmailAddress(email)) {
                int start = m.start(1);
                int end = m.end(1);
                ret.addElement(email);
                t.put(start, end);
            }
        }
        int j = 0;
        if (t.code().size() > 0) {
            int start = t.code().elementAt(j);
            int end = t.value().elementAt(j);
            for (int i = 0; i < buf.length(); i++) {
                if ((i >= start) && (i < end)) {
                    continue;
                } else {
                    if (i >= end) {
                        j++;
                        if (j >= t.code().size()) {
                            break;
                        }
                        start = t.code().elementAt(j);
                        end = t.value().elementAt(j);
                    }
                    if ((i >= start) && (i < end)) {
                        continue;
                    }
                    int c = buf.charAt(i);
                    if ((c == ',') || (c == ' ') || (c == ';')) {
                        continue;
                    } else {
                        if (ret.size() >= j){
                            int size = ret.size();
                            ret.removeElementsFrom(j, size);
                            t.code().removeElementsFrom(j, size);
                            t.value().removeElementsFrom(j, size);
                            break;
                        }
                    }
                }

            }

            int i = t.size() - 1;
            if (i >= 0){
                end = t.value().elementAt(i);
                buf.delete(0, end);
            }
        }

        return ret;

    }

    Contact extractEmails(String s) {
        StringBuffer sb = new StringBuffer(s);
        Array<String> emails = getEmailsForSend(sb);
        Contact contact = new Contact();
        for(int i = 0; i < emails.size(); i++){
            String email = emails.elementAt(i);
            if (!contact.addr.contains(email)){
                contact.addr.add(email);
            }
        }

        contact.text = sb.toString();
        return contact;

    }

    public static boolean isValidEmailAddress(String target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();

    }

    Contact extractPhones() {
        return extractPhones(getWholeText());
    }

    protected boolean addPhone(List<String> phones, StringBuffer s, int begin, int end) {
        int len = end - begin;
        if (s.charAt(begin) == '+') {
            if (len < 5) {
                return false;
            }
        } else if (len < 3) {
            return false;
        }
        phones.add(s.substring(begin, end).trim());
        return true;
    }

    Contact extractPhones(String text) {
        Contact contact = new Contact();
        StringBuffer s = new StringBuffer(text);
        ArrayList<String> phones = new ArrayList<String>(1);
        contact.addr = phones;
        int start = 0;
        int begin = -1, i = 0;
        int c = 32;
        br:for (; i < s.length(); i++) {
            c = s.charAt(i);
            switch (c) {
                case '+':
                    if (begin < 0) {
                        begin = i;
                        break;
                    } else {
                        begin = -1;
                        break br;
                    }
                case '*':
                case '#':
                    if (begin < 0) {
                        begin = i;
                    }
                case '-':
                    break;
                default:
                    if (Character.isDigit(c)) {
                        if (begin < 0) {
                            begin = i;
                        }
                    } else {
                        if ((c == ',')||(c == ' ')||(c == ';')){
                            if (begin > -1) {
                                if (addPhone(phones, s, begin, i)) {
                                    start = i;
                                    begin = -1;
                                }else
                                    break br;
                            }
                            break;
                        }
                        break br;
                    }
            }

        }
        if (begin > -1) {
            if (addPhone(phones, s, begin, i))
                start = i + 1;
        }

        if (start < s.length()) {
            contact.text = s.substring(start);
        }

        return contact;

    }


    void call() {
        String phone = getWord();
        StringBuilder sb = new StringBuilder(phone.length());
        int i;
        if (phone.startsWith("+")){
            sb.append('+');
            i = 1;
        }else
            i=0;
        for(; i < phone.length(); i++){
            char c = phone.charAt(i);
            if (Character.isDigit(c))
                sb.append(c);
            switch(c){
                case '#':
                case '*':
                    sb.append(c);
            }
        }
        phone = sb.toString();
        if (notEmpty(phone)) {
            call(phone);
            return;
        }
        okDialog("Phone not found", "Please put any phone or move cursor to it in upper edit field.");

    }

    void call(/*Contact contact*/String phone) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CALL_PHONE },
                    PERMISSION_CALL_PHONE);
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL);

        intent.setData(Uri.parse("tel:" + phone));

        Intent chooser = Intent.createChooser(intent, "Call");
        this.startActivityForResult(chooser, 262144);
    }



    protected String get(List<String> list, int i) {
        if (list == null) {
            return null;
        }
        if (list.size() == 0) {
            return null;
        }
        if (i >= list.size()) {
            return null;
        }
        return list.get(i);
    }

    void sms() {
        try {
            Contact contact = extractPhones();
            int count = contact.addr.size();
            if (count == 0){
                AlertDialog.Builder builder = new AlertDialog.Builder(FreeShell.this);
                builder.setTitle("Phone not found")
                        .setMessage("You may enter any valid phone numbers in the begin of upper field and any text of message.")
                        .setCancelable(true)
                        .setPositiveButton("Send anyway", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendSms2(contact);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return;

            }

            sendSms2(contact);

        }catch (Throwable t){
            printError(t);
        }
    }

    void sendSms2(Contact contact){
        int count = contact.addr.size();
        if (count == 0) {
            sendSms("", contact.text);
        }else
        if (count == 1){
            sendSms(contact.addr.get(0), contact.text);
        }else{
            sendSms(contact.addr, contact.text);
        }


    }

    void sendSms(List<String> phones, String text){

                    try {
                        StringBuffer sb = new StringBuffer();
                        //ArrayList<Uri> uris = new ArrayList<>();
                        for (String phone : phones) {
                            //uris.add(Uri.parse("smsto:" + phone));
                            if (sb.length() > 0)
                                sb.append("; ");
                            sb.append(phone);
                        }

                        Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                        i.putExtra("address", sb.toString());
                        // here i can send message to emulator 5556,5558,5560
                        // you can change in real device
                        i.putExtra("sms_body", text);
                        //i.setType("text/plain");

                        i.setType("vnd.android-dir/mms-sms");

                        Intent chooser = Intent.createChooser(i, "Send messages");
                        startActivityForResult(chooser, 262144);
                        //startActivityForResultForResult(i, 500);
                    }catch (Throwable t){
                        printError(t);
                    }


/*                        if (uris.isEmpty()) {
                            return;
                        }
                        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        i.putExtra(Intent.EXTRA_TEXT, text);
                        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                        //startActivityForResultForResult(i, 500);
*/

    }

    //final static int PHOTO = 555, SMS = 777, VIDEO = 888;

/*    @Override
    public void startActivityForResultForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResultForResult(intent, requestCode, options);
//        if (requestCode==500) Toast.makeText(this,"Message Delivered Successfully",Toast.LENGTH_LONG).show();
    }*/

    void sendSms(String phone, String text){

                    Uri uri = Uri.parse("smsto:" + phone);
                    Intent it = new Intent(Intent.ACTION_SENDTO, uri);

                    it.putExtra("sms_body", text);
                    //it.setType("text/plain");
                    Intent chooser = Intent.createChooser(it, "Send message");
                    startActivityForResult(chooser, 262144);
                    //listView.invalidate();

    }

    @Override
    public void onResume() {
        printLog("onResume");
        super.onResume();
        if (photo != null){
            if (photo.isDirectory()){
                if (photo.exists()){
                    File[] l = photo.listFiles();
                    if (notEmpty(l)){
                        if (l.length != numberPhoto)
                            listPanel().showPhotoPanel(photo);
                    }
                }
            }
            photo = null;
        }
        firstScreen().checkCurrentDate();


    }



    class SF extends SurfaceView {
        SF(Context context) {
            super(context);
            previewHolder = getHolder();
            //previewHolder.
            // previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

    }

    //                    RectF rect = new RectF();
//                    rect.top = 0;
//                    rect.left = 0;
//                    rect.bottom = text.getHeight();fi
//                    rect.right = text.getWidth();
//                    canvas.drawRoundRect(rect, (float)1.0, (float)2.0, paint);
    int margin, wb, hb, wb2, hb2;

    class ButtonPanel{
        void lock(View close){
            for(int i = 0; i < lines.size(); i++){
                Array <CustomAction> actions = this.lines.value().elementAt(i);
                if (actions == null)
                    continue;

                for(int j = 0; j < actions.size(); j++){
                    CustomAction action = actions.elementAt(j);
                    if (action == null) {
                        continue;
                    }
                    View view = action.view;
                    if (view != close)
                        view.setClickable(false);
                }
            }
        }

        void unLock(){
            for(int i = 0; i < lines.size(); i++){
                Array <CustomAction> actions = this.lines.value().elementAt(i);
                if (actions == null)
                    continue;

                for(int j = 0; j < actions.size(); j++){
                    CustomAction action = actions.elementAt(j);
                    if (action == null) {
                        continue;
                    }
                    View view = action.view;
                        view.setClickable(true);
                }
            }
        }


        void normalize(){
        /*    actions = vLines;
            int all = 0;
            if (actions.size() == 0)
                return;
            for(int i = 0; i < actions.size(); i++){
                Array <CustomAction> actions2 = this.actions.value().elementAt(i);
                if (actions2 == null)
                    continue;
                int size = actions2.size();
                all += size;
            }
            int middle = all / actions.size();

            Array<CustomAction> buf = new Array<>(CustomAction.class, middle);
            for(int i = 0; i < actions.size(); i++){
                Array <CustomAction> actions2 = this.actions.value().elementAt(i);
                if (actions2 == null)
                    continue;
                int size = actions2.size();
                int d = size - middle;
                while (d > 1) {
                    buf.addElement(actions2.pop());
                    d--;
                }

                while ((buf.size() > 0)&&(d < 0)){
                    actions2.addElement(buf.pop());
                    d++;
                }
            }
            while(buf.size() > 0) {
                for (int i = 0; (buf.size() > 0) && (i < actions.size()); i++) {
                    Array<CustomAction> actions2 = this.actions.value().elementAt(i);
                    if (actions2 == null)
                        continue;
                    int size = actions2.size();
                    int d = size - middle;
                    while ((buf.size() > 0)&&(d < 0)) {
                        actions2.addElement(buf.pop());
                        d--;
                    }
                }
            }

*/
        }

        int scrollX = 0;

        IOTable<Array<CustomAction>> lines = new IOTable(Array.class,4);

        void addLine(CustomAction action, int line){
            if (fresh) {
                Array<CustomAction> list = lines.get(line);
                if (list == null) {
                    list = new Array<>(CustomAction.class, 6);
                    lines.put(line, list);
                }
                list.addElement(action);
            }else{

                int y = 0;
                if (line > 0){
                    y = (line * hb) + ((line - 1) * margin);
                }
                action.view.setY(y);
                action.view.setX(params.width + margin);
                int w = margin + action.params.width;
                this.right += w;
                params.width = params.width + w;
                hPanel.setMinimumWidth(params.width);

                hPanel.addView(action.view, action.params);
             //   hScroll().removeView(hPanel);
            //    hScroll.addView(hPanel, params);
                hPanel.invalidate();
                hScroll().invalidate();

            }
        }


        RelativeLayout.LayoutParams hscrollParams;
        HorizontalScrollView hScroll;
        HorizontalScrollView hScroll(){
            if (hScroll == null){
                hScroll = new HorizontalScrollView(FreeShell.this){
                    @Override
                    public boolean onInterceptTouchEvent(MotionEvent ev) {
                        return super.onInterceptTouchEvent(ev);
                    }

                };
                hScroll.setScrollbarFadingEnabled(false);
                hScroll.addView(hPanel());
            }
            return hScroll;
        }
        RelativeLayout hPanel;
        RelativeLayout hPanel(){
            if (hPanel == null){
                hPanel = new RelativeLayout(FreeShell.this);
            }
            return hPanel;
        }




        void close(RelativeLayout panel){
            panel.removeView(hScroll);
        }
        int right, hButtons;
        boolean fresh = true;
        RelativeLayout.LayoutParams params;
        int freshLayout(RelativeLayout panel, int w, int x0, int y0, int yOffset){
            if (!fresh)
                return reLayout(x0 ,y0);
            fresh = false;
            scrollX = hScroll().getScrollX();
            panel.removeView(hScroll);
            hScroll.removeView(hPanel);


            int y = yOffset;
            int x = 0;
            int right = 0;
            int hButtons = 0;

            for(int i = 0; i < lines.size(); i++){
                Array <CustomAction> actions = this.lines.value().elementAt(i);
                if (actions == null)
                    continue;
                if (hButtons < y + hb) {
                    hButtons = y + hb;
                }

                for(int j = 0; j < actions.size(); j++){
                    CustomAction action = actions.elementAt(j);
                    if (action == null) {
                        if (j < actions.size() - 1)
                            x += hb >> 1;
                        continue;
                    }
                    View view = action.view;
                    int wb = action.params.width;

                    if (x + wb > right) {
                        right = x + wb;
                    }
                    hPanel.removeView(view);


                    view.setX(x);
                    view.setY(y);

                    hPanel.addView(view, action.params);
                    if (j < actions.size() - 1)
                        x += wb + margin;
                }
                x = 0;
                y += hb + margin;
            }
            params = new RelativeLayout.LayoutParams(right, hButtons);
            this.right = right;
            hPanel.setLayoutParams(params);
            hScroll.addView(hPanel, params);

            hscrollParams = new RelativeLayout.LayoutParams(w, hButtons);
            hScroll().setX(x0);
            hScroll.setY(y0);

            panel.addView(hScroll(), hscrollParams);
            this.hButtons = hButtons;

            return hButtons;
        }

        int reLayout(int x0, int y0){
            scrollX = hScroll().getScrollX();
            hScroll().setX(x0);
            hScroll.setY(y0);
            hScroll.invalidate();
            return hButtons;
        }

    }

    class FirstScreen extends  ButtonPanel{
/*
CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null; // Usually back camera is at 0 position.
        try {
            cameraId = camManager.getCameraIdList()[0];
            camManager.setTorchMode(cameraId, true);   //Turn ON
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
 */
        //File upperText;
        void fresh(){
            if (hscrollParams != null) {
                myLayout().removeView(hScroll());

                //this.hPanel().setMinimumWidth(displayMetrics.widthPixels);
                this.hScroll().setMinimumWidth(displayMetrics.widthPixels);
                hscrollParams.width = displayMetrics.widthPixels;
                myLayout().addView(hScroll, hscrollParams);

                hScroll.invalidate();
                fresh = true;
            }
        }


        void hide(){
            if (hScroll != null)
                myLayout().removeView(hScroll());
            if (text != null)
                myLayout().removeView(text());
        }

    /*    RelativeLayout panel;
        RelativeLayout panel(){
            if (panel == null){
                panel = new RelativeLayout(FreeShell.this);
            }
            return panel;
        }*/
    void errorClearSave(){
        AlertDialog.Builder builder = new AlertDialog.Builder(FreeShell.this);
        builder.setTitle("Error")
                .setMessage("Can't save log. Do you wont clear?")
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try{
                            text().setText("");
                        }catch(Exception t){
                            printError(t);
                        }
                }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alert = builder.create();
        alert.show();


    }

    String getHistoryFileName(String path){
        return getUniqueName(path, getOnlyTime(), "txt");
    }
        String getHistoryFilePath(){
            String s = myHistoryDir() + "/" + getOnlyDate();
            File f = new File(s);
            if (!f.exists()){
                f.mkdirs();
            }
            return s;
        }

        String getFileName(){
            TextAdapter ta = getTextAdapter();
            if (ta == null)
                return fileName;
            else
                return ta.file.getName();
        }

        String getCurrentTextFile(){
            TextAdapter ta = getTextAdapter();
            if (ta == null)
                return filePath.getText().toString();
            else
                return ta.file.getAbsolutePath();
        }

        void removeTextAdapter(){
            if (edited != null) {
                edited = null;
                setDefaultFileName();
                mainReLayout();
            }

        }

        TextAdapter getTextAdapter(){
            if (edited != null) {
                TextAdapter textAdapter = ((TextAdapter) edited.adapter);
                if (textAdapter.closed) {
                    removeTextAdapter();
                    return null;
                }
                return textAdapter;
            }
            return null;
        }

        void saveText(){
            TextAdapter textAdapter = getTextAdapter();
            if (textAdapter != null){
                try {
                    textAdapter.changeLine(edited.globalPosition, text.getText().toString());
                    return;
                } catch (Exception e) {
                    error(e);
                    removeTextAdapter();
                    return;
                }
            }
            if (editedFile != null){
                if (editedFile instanceof ZipList.File2){
                    final ZipList.File2 zip = (ZipList.File2)editedFile;
                    firstScreen().checkEditedFile(new SetFile() {
                        @Override
                        public void setFile(File f) {
                            fSync().writeFile(f, text().getText().toString());
                            Toast.makeText(FreeShell.this, f + " was saved.", Toast.LENGTH_LONG).show();
                        }
                    }, zip.getZipParentPath(), firstScreen().getFileName(), text().getText().toString());
                    return;
                }
            }
            String f = getCurrentTextFile();
            if (f == null) {
                errorClearSave();
                return;
            }
            String s = text().getText().toString();
            if (s.length() > 0) {
                fSync().writeFile(f, text().getText().toString());
                Toast.makeText(FreeShell.this, f + " was saved.", Toast.LENGTH_LONG).show();
            }
        }
        void wholeClearText() {
            text().setText("");
            if (getTextAdapter() == null)
                setDefaultFileName();
            mainReLayout();
        }

        void saveClearText(){
            saveText();
            setDefaultFileName();
            wholeClearText();

           /* if (file == null) {
                errorClearSave();
            }else{
                text().setText("");
                okDialog("OK", "Log was saved in " + file.getAbsolutePath());
            }*/
        }
        /*void setEdited(File f){
            edited = f;
        }*/
        //File edited;
        /*void saveMtk(FileAdapter fileAdapter){
            if (edited == null)
                checkEditedFile(fileAdapter.getDir(), this::setEdited);
            else
                fSync().writeFile(edited, text().getText().toString());
        }*/
        void setEditFileName(String path, String name){

            fileName = name;
            this.path = path;
            refreshFilePath();
        }

        void checkEditedFile(SetFile setFile, String path, String s){
            String name = getWord();
            checkEditedFile(setFile, path, name, s);
        }

        void checkEditedFile(SetFile setFile, String p, String fileName, String s){
            if (fileName == null){
                fileName =  getOnlyTime() +  ".txt";
            }
            if (p == null){
                p = myHomeDir();
            }
            final String path = p;
            AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
            final EditText edittext = new EditText(FreeShell.this);
            edittext.setText(path + "/" + fileName);
            alert.setMessage("path/name");
            alert.setTitle("Save as");

            alert.setView(edittext);

            alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{

                        //What ever you want to do with the value
                        final String name = edittext.getText().toString();
                        File f = new File(name);
                        if (f.exists()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                            // Specify the dialog is not cancelable
                            builder.setCancelable(false).setTitle(name + " already exists. Overwrite?").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // what ever you want to do with No option.
                                }
                            }).setNeutralButton("Change name", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try{
                                        checkEditedFile(setFile, f.getParent(), f.getName(), s);
                                    }catch(Exception t){
                                        printError(t);
                                    }

                                    // what ever you want to do with No option.
                                }
                            }).setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    fSync().writeFile(f, s);
                                    setEditFileName(f.getParent(), f.getName());
                                    setFile.setFile(f);

                                    mainReLayout();
                                    // what ever you want to do with No option.
                                }
                            });


                            AlertDialog dialog2 = builder.create();
                            // Display the alert dialog on interface
                            dialog2.show();

                            return;
                        }
                        fSync().writeFile(f, s);

                        refreshDirs(f.getParent());
                       // setEditFileName(f.getParent(), f.getName());
                        setFile.setFile(f);
                        //listView.invalidate();
                    }catch(Exception t){
                        printError(t);
                    }

                }
            });
            alert.setNeutralButton("Clear name", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{
                        checkEditedFile(setFile, path, "", s);
                        return;
                    }catch(Exception t){
                        printError(t);
                    }

                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });
            alertShow(alert, edittext);

        }
        //राण में कूद पड़ी माँ काली
        ShellButton ads;
        void FirstScreen(){
            init();
        }
        Integer orientation = null;
        void init(){
            if (FreeShell.this.orientation == orientation){
                return;
            }
            orientation = FreeShell.this.orientation;
            fresh = true;
            super.fresh = true;
            lines.clear();
            this.hPanel().removeAllViews();
            int w = 0;
            //
            new ShellButton(Html.fromHtml("<big><big><big><b>⇦</b></big></big></big>"),
                    FreeShell.this::prevWindow,
                    cCyan, bCyan, 0, this){
                    int normalize(CharSequence text){
                        return hb;
                    }
                };
            w += hb;
            new ShellButton(Html.fromHtml("<big><big><big><b>⇨</b></big></big></big>"),
                    FreeShell.this::nextWindow,
                    cCyan, bCyan, 0, this){
                        int normalize(CharSequence text){
                            return hb;
                        }
                    };
            w += hb;

            if (ADS) {
                boolean isAds;
                if (ads != null) {
                    isAds = ads.flag;
                } else
                    isAds = false;
                ads = new ShellButton("Ads",
                        FreeShell.this::ads,
                        isAds, 0, this);
            }
            w += hb;

/*            new ShellButton(Html.fromHtml("restore panels"),
                    FreeShell.this::load,
                    cBlue, bSel1, 0, this);*/
            new ShellButton("Map",
                    FreeShell.this::location,
                    cRose, bYellow, 0, this);
            w += hb;
            new ShellButton("+123... Phone",
                    FreeShell.this::contacts,
                    cGreen,
                    bGreen, 0, this);
            w += wb;

            new ShellButton("New contact", freeShell::newContact,
                    cGreen1,
                    bGreen1, 0, this);
            w += wb;
            new ShellButton("Save & new",
                    this::saveClearText,
                    cOrange, bYellow1, 0, this);
            w += wb;

            int  i;
            if ((w + w + w) < (displayMetrics.widthPixels)){
                i = 0;
            }else{
                i = 1;
            }
            new ShellButton("Send SMS",
                    FreeShell.this::sms,
                    cGreen1,
                    bSel1, i, this);

            new ShellButton(Html.fromHtml("Send e-mail"),
                   FreeShell.this::email,
                    cGreen, bGreen, i, this);


            new ShellButton("Home",
                    FreeShell.this::home,
                    cBlue,
                    bYellow2, i, this);
            new ShellButton("App",
                    FreeShell.this::app,
                    cBlue,
                    bGreen1, i, this);

            new ShellButton("MP3",
                    FreeShell.this::mp3,
                    cBlue, bSel1,
                    i,  this);
            new ShellButton("Take photo",
                    FreeShell.this::takePhotoFirst,
                    cRose,
                    bRose, i, this);
            new ShellButton("FS", FreeShell.this::fs,
                    cCyan,
                    bCyan, i, this);
            new ShellButton("History",
                    FreeShell.this::history,
                    cOrange, bYellow1, i, this);
            new ShellButton("Rec video",
                    FreeShell.this::recVideo,
                    cRose,
                    bRose, i, this);

            new ShellButton("Selected",
                    FreeShell.this::showSelected,
                    cCyan,
                    bCyan, i, this);
            new ShellButton("Save text",
                    this::saveText,
                    cSel1, bSel1, i, this);
            new ShellButton("Run OS command",
                    FreeShell.this::su,
                    cCyan, bCyan, i, this);

            new ShellButton("Clear history",
                    FreeShell.this::clearHistory,
                    cMangeta,
                    bYellow, i, this);
            new ShellButton(Html.fromHtml("Remove line"),
                    FreeShell.this::removeLine,
                    cRed1, bRed1,  0, this);
            new ShellButton("New",
                    this::newDefaultFile,
                    cMangeta, bYellow, 0, this);
            new ShellButton("Clear text",
                    this::wholeClearText,
                    cRed, bRed, 0, this);

            new ShellButton("Copy to clipboard",
                    new Procedure() {
                        @Override
                        public void procedure() {
                            clip("edit field", getWholeText());
                        }
                    },
                    cGreen, bGreen, 0, this);
            new ShellButton("Paste clipboard",
                    new Procedure() {
                        @Override
                        public void procedure() {
                            ClipData clip;
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                            clip = clipboard.getPrimaryClip();
                            if(clip == null || clip.getItemCount() == 0)
                                return;
                            String t = clip.getItemAt(0).getText().toString();
                            pressChar(" " + t + " ");

                        }
                    },
                    cGreen, bGreen, 0, this);


            new ShellButton("Google",
                    FreeShell.this::google,
                    cRed,
                    bCyan, 0, this);

/*            new ShellButton(Html.fromHtml("Flashlight"),
                    this::flashLight,
                    flashState, 0, this);*/
            new ShellButton("Close all",
                    FreeShell.this::closeAll,
                    cRed,
                    bRed, 0, this);

            new ShellButton("About",
                    FreeShell.this::mainHelp,
                    cMangeta,
                    bYellow, 0, this);


        }

        private void newDefaultFile() {
            setDefaultFileName();
            wholeClearText();
        }
/*        void flashLight(){
            flashState = !flashState;
            CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId = null; // Usually back camera is at 0 position.
            try {
                cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, flashState);   //Turn ON
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }*/

        Item edited;
        void editItem(Item item, File file){
            this.edited = null;
            this.editedFile = null;

            setDefaultFileName();
            setLine(item, file);
            String s = item.toString();
            text().setText(s);
            this.edited = item;
            this.editedFile = file;
            text().setSelection(text().length(), text().length());
            text().requestFocus();

        }

        void setLine(Item item, File file){
            firstScreen().filePath(displayMetrics.widthPixels).setText(Html.fromHtml("Line " + item.globalPosition + ": <small>" + file.getAbsolutePath() + "</small>"));
            filePath.measure(
                    makeMeasureSpec,
                    makeMeasureSpec);
        }

        String fileName;
        String path;
        File editedFile;

        void editItem(TextAdapter textAdapter){
            if (getTextAdapter() == null){
                saveClearText();
            }
            editedFile = textAdapter.file;
            setEditFileName(editedFile.getParent(), editedFile.getName());
            String s = loadFile(editedFile);
            text().setText(s);
            text().requestFocus();
            mainReLayout();
        }


        boolean changes = true, afterChanges = false;
        public EditText text() {
            if (text == null) {

                text = new EditText(FreeShell.this);
                text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if ((hasFocus)&&(v==text)){
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(text, InputMethodManager.SHOW_IMPLICIT);

                        }
                    }
                });
                TextWatcher resizeListener = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                  /*      for(int i = 0; i < forFilter.size(); i++){
                            Adapter adapter = forFilter.elementAt(i);
                            if (adapter == null)
                                continue;
                            adapter.filter();
                        }*/
                        changes = true;

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        afterChanges = true;
                        changes = true;
                    }
                };
                text.setTextColor(cBlack);
                text.setBackgroundColor(bBlack);

                ViewTreeObserver vto = text.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (!changes)
                            return;
                        changes = false;
                        int v = text.getHeight();
                        if (textHeight == v)
                            return;
                        log("changeHeight");
                            //int d = v - textHeight;
                  //          changes = false;
                            textHeight = v;
                            //int y = (int) firstScreen().hScroll().getY();
                            int y = textHeight + margin;
                            filePath(displayMetrics.widthPixels).setY(y);
                            y += filePath.getMeasuredHeight();
                            firstScreen().hScroll().setY(y);
                            //y = listPanel().hScrollPanels().getScrollY();
                            y += firstScreen.hButtons + margin;
                            listPanel().hScrollPanels().setY(y);
                           // listPanel.panels.setY(y);
                            firstScreen.hScroll.invalidate();
                           // listPanel.paneflils.invalidate();
                            listPanel.hScrollPanels.invalidate();
                            if (afterChanges) {
                                int h = listPanel().getHeight() + margin +
                                        firstScreen().hPanel().getHeight() + margin +
                                        textHeight;
                                if (h > vScroll().getHeight()) {
                                    myLayout().setMinimumHeight(h);
                                    myLayout.invalidate();
                                }
                                afterChanges = false;
                            }
                            text.requestFocus();

                    }
                });
                text.addTextChangedListener(resizeListener);
            }
            return text;
        }
        int textHeight;
        boolean fresh = true;
        TextView filePath;
        RelativeLayout.LayoutParams filePathLayoutParams;
        TextView filePath(int w){
            if (filePath == null){
                filePath = new TextView(freeShell);
                filePath.setTextAppearance(FreeShell.this, R.style.font3);
                filePath.setBackgroundColor(bLight);
                filePath.setTextColor(cLight);
                if (this.path == null)
                    path = getHistoryFilePath();
                if (this.fileName == null)
                    fileName = getHistoryFileName(path);

              //  path += path;
                filePath.setText(path + '/' + fileName);
                filePathLayoutParams = new RelativeLayout.LayoutParams(w, RelativeLayout.LayoutParams.WRAP_CONTENT);
                filePath.setLayoutParams(filePathLayoutParams);

                filePath.measure(
                        makeMeasureSpec,
                        makeMeasureSpec);
                filePath.setClickable(true);
                filePath.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (edited != null){
                            Adapter adapter = edited.adapter;
                            adapter.show();
                        }else {
                            if (notEmpty(path)){
                                if (notEmpty(fileName)){
                                    File file = new File(path +"/" + fileName);
                                    if (file.exists()){
                                        if (openedTextFile.containsKey(file.getAbsolutePath())){
                                            openTextFile0(file, 0);
                                            return;
                                        }
                                    }
                                }
                                openAdapter(path);
                            }
                        }
                    }
                });

            }else {
                filePathLayoutParams.width = w;
                filePath.setWidth(w);
                filePath.measure(
                        makeMeasureSpec,
                        makeMeasureSpec);
            }
            return filePath;
        }
        int freshLayout(RelativeLayout panel, int w, int x0, int y0){
            if (!fresh)
                return reLayout(panel, w, x0, y0);
            fresh = false;
            text().setWidth(w);

            int th = textHeight;
            /*if (th == 0){
                th = hb;
            }*/
            if (text != null)
                panel.removeView(text);

            if (filePath != null)
                panel.removeView(filePath);

            if (th == 0){
                th = hb;
            }else{
                th += margin;
            }
            text.setX(x0);
            text.setY(y0);

            panel.addView(text);
            y0 += th;

            //setDefaultFileName();

            int ht = filePath(w).getMeasuredHeight();

            filePath.setX(x0);
            filePath.setY(y0);
            y0 += ht;

            panel.addView(filePath, filePathLayoutParams);


            int h = super.freshLayout(panel, w, x0 , y0, 0) + margin;

            return h + th + ht+ margin;
    }



        void checkCurrentDate(){
/*            if (edited != null){
                return;
            }
            if (notEmpty(getWholeText().trim())){
                return;
            }*/

        }

        private String setDefaultFileName() {
            editedFile = null;
            String path = getHistoryFilePath();
            edited = null;
            setEditFileName(path, getHistoryFileName(path));
            return path;
        }

        void clearEmptyHistory(){
            try {
                String sir = myHistoryDir();
                File f = new File(sir);
                if (f.exists()) {
                    File[] files = f.listFiles();
                    boolean b = false;
                    String cur = firstScreen().getCurrentTextFile();
                    File curDir = null;
                    if (notEmpty(cur)){
                        File fcur = new File(cur);
                        curDir = fcur.getParentFile();
                    }

                    for(File dir: files){
                        File[] d = dir.listFiles();
                        if (!notEmpty(d)) {
                            b = true;
                            if (!dir.equals(curDir))
                                dir.delete();
                        }
                    }
                    if (b){
                        refreshDirs(sir);
                    }
                }
            }catch (Throwable t){
                error(t);
            }

        }


        private void refreshFilePath() {
            TextView tw = filePath(displayMetrics.widthPixels);
            tw.setText(path + '/' + fileName );
            tw.measure(
                    makeMeasureSpec,
                    makeMeasureSpec);
            tw.invalidate();
        }

        int reLayout(RelativeLayout panel, int w, int x0, int y0){
            int th = textHeight;

            if (th == 0){
                th = hb;
            }else{
                th += margin;
            }
            text.setX(x0);
            text.setY(y0);
            y0 += th;
            int ht = filePath.getMeasuredHeight();
            y0 += ht;

            int h = super.reLayout(x0 , y0) + margin;

            return h + th + ht + margin;
        }
    }

    private void closeAll() {
        listPanel().closeAll();
        mainReLayout();
    }


    private void prevWindow() {
        int scrollX = listPanel().hScrollPanels().getScrollX();
        scrollX -= listW;
        listPanel().hScrollPanels().smoothScrollTo(scrollX, listPanel().hScrollPanels().getScrollY());
    }

    private void nextWindow() {
        int scrollX = listPanel().hScrollPanels().getScrollX();
        scrollX += listW;
        listPanel().hScrollPanels().smoothScrollTo(scrollX, listPanel().hScrollPanels().getScrollY());

    }

    private void location() {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps"));
        Intent chooser = Intent.createChooser(intent, "Map");
        startActivityForResult(chooser, 262144);
    }

    //FileAdapter home;
    private void home() {
        if (getAdapter(myHomeDir()) == null) {
            FileAdapter home = new FileAdapter();
            home.init(0);
            home.openSomeDir(homeDir);
            mainReLayout();
        }
    }

    private void history() {
        firstScreen().clearEmptyHistory();
        if (getAdapter(myHistoryDir()) == null) {
            FileAdapter home = new FileAdapter();
            home.init(0);
            home.openSomeDir(historyDir);
            mainReLayout();
        }
    }

    void newZipPanel(File file, DirAdapter dirAdapter){
        ZipAdapter home = new ZipAdapter();
        home.init(0);
        home.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.yield();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        home.openSomeDir(file, dirAdapter);
                    }
                });
            }
        }).start();

    }

    void openZipAdapter(String s){
        HashSet<ZipAdapter> homes = dirZipAdapter.get(s);
        ZipAdapter home;
        if ((homes != null)&&(homes.size() > 0)){
            home = homes.iterator().next();
            if (home != null){
                home.show();
            }
        }
        ZipAdapter zipAdapter = zipAdapter();
        zipAdapter.init(0);
        zipAdapter.openSomeDir(s);

    }
    ZipAdapter getZipAdapter(String s){
        HashSet<ZipAdapter> homes = dirZipAdapter.get(s);
        ZipAdapter home;
        if ((homes != null)&&(homes.size() > 0)){
            home = homes.iterator().next();
            if (home != null){
                home.show();
                return home;
            }
        };
        return null;

    }

    ZipAdapter getZipAdapter0(String s){
        HashSet<ZipAdapter> homes = dirZipAdapter.get(s);
        ZipAdapter home;
        if ((homes != null)&&(homes.size() > 0)){
            return homes.iterator().next();
        };
        return null;

    }


    void openAdapter(String s){
        HashSet<FileAdapter> homes = dirFileAdapter.get(s);
        FileAdapter home;
        if ((homes != null)&&(homes.size() > 0)){
            home = homes.iterator().next();
            if (home != null){
                home.show();
                return;
            }
        }
        FileAdapter fileAdapter = fileAdapter();
        fileAdapter.init(0);
        File f = new File(s);
        if (!f.exists()){
            f.mkdirs();
        }
        fileAdapter.openSomeDir(f);

    }
    FileAdapter getAdapter(String s){
        HashSet<FileAdapter> homes = dirFileAdapter.get(s);
        FileAdapter home;
        if ((homes != null)&&(homes.size() > 0)){
            home = homes.iterator().next();
            if (home != null){
                home.show();
                return home;
            }
        }
        return null;
    }


    FileAdapter getAdapter0(String s){
        HashSet<FileAdapter> homes = dirFileAdapter.get(s);
        FileAdapter home;
        if ((homes != null)&&(homes.size() > 0)){
            return homes.iterator().next();
        }
        return null;
    }

    FileAdapter openAdapter0(String s){
        FileAdapter fileAdapter = getAdapter0(s);
            if (fileAdapter != null){
                return fileAdapter;
            }
        fileAdapter = fileAdapter();
        fileAdapter.init(0);
        File f = new File(s);
        if (!f.exists()){
            f.mkdirs();
        }
        fileAdapter.openSomeDir(f);
        return fileAdapter;
    }

   /* class AutoFillContactAdapter  extends ArrayAdapter<String> implements AdapterView.OnItemClickListener{
        ShellContacts contacts;
        public AutoFillContactAdapter() {
            super(FreeShell.this,
                    android.R.layout.simple_list_item_1);

            contacts = findContacts("");
            this.addAll(contacts.buttons);

        }
        void initFromFile(AutoCompleteTextView textView){
            textView.setAdapter(this);
            textView.setOnItemClickListener(this);
        }

        void refresh(String s){

            s = s.trim();
            contacts = findContacts(s);
            clear();
            addAll(contacts.names);
            addAll(contacts.phones);

        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position < contacts.names.size()){
                text().setText(contacts.names.get(position));
            }else{
                text().setText(contacts.phones.get(position - contacts.names.size()));
            }

        }
    }
*/

    HashArray<FileAdapter> filePanels = new HashArray<FileAdapter>(FileAdapter.class,4){
        @Override
        public int hashCode(FileAdapter o){
            return o.listView.index;
        }
    };
    //HashArray<Adapter> forFilter = new HashArray(Adapter.class,2);
    //HashArray<FileAdapter> dstFiles = new HashArray<>(FileAdapter.class,2);


    abstract class Adapter /*extends ArrayAdapter<String>*/ implements Mtk{


         boolean closed = false;
        IOTable<Item> items = new IOTable<Item>(Item.class);


        ListView0 listView;
        ArrayList<Object> allItems;
        IntIntTable indexMap;
        ShellButton close, find, refresh;
        ButtonPanel buttonPanel = newButtonPanel();
        TextView info0 = new TextView(FreeShell.this);
        boolean b = false;


        public void lock(){
            lock(3);

        }
        public void lock(int num){
            if (isLocked()){
                if (progressBar().getMax() == num)
                    return;
            }
            removeProgress();
            progressBar().setMax(num);
            progressBar.setProgress(0);
            if (getCount() == 0){
                buttonPanel.hPanel().addView(progressBar, listW, hb);
            }else
                listView().addView(progressBar, listW, hb);
            if (isLocked())
                return;
            buttonPanel.hPanel().setBackgroundColor(bPleaseWait);
            buttonPanel.hPanel().invalidate();
            buttonPanel.lock(close.view);
            listView().lock2();
        }

        void removeProgress(){
            if (progressBar != null) {
                buttonPanel.hPanel().removeView(progressBar);
                listView().removeView(progressBar);
            }
        }
        public void unLock(){
            if (!isLocked())
                return;
            removeProgress();
            progress = false;
            buttonPanel.hPanel().setBackgroundColor(Color.TRANSPARENT);
            buttonPanel.hPanel().invalidate();
            buttonPanel.unLock();
            listView().unLock2();
        }


        public boolean isLocked(){
            return listView().lock;
        }


        void setDefaultText(){
            if (notEmpty(filteredWord))
                info0.setText("Filter: " + filteredWord);
            else
                info0.setText(getCount() + " items found.");
            info0.invalidate();
        }
        void goToWindow() {
            if (!loaded)
                return;
            int x = listView.index * listW;
            int y = listPanel().hScrollPanels().getScrollY();
            listPanel().hScrollPanels().postDelayed(new Runnable() {
                @Override
                public void run() {
                    listPanel().hScrollPanels().smoothScrollTo(x, y);
                }
            }, 500);
            checkVScroll();

        }

        ProgressBar progressBar;
        ProgressBar progressBar(){
            if (progressBar == null){
                progressBar = new ProgressBar(freeShell, null, android.R.attr.progressBarStyleSmall);
                progressBar.setBackgroundColor(Color.TRANSPARENT);
                progressBar.setX(0);
                progressBar.setY(0);
                progressBar.setVisibility(VISIBLE);
/*                progressBar.setX(x0);

                progressBar.setY(y0);
                progressBar.setBackgroundColor(Color.TRANSPARENT);
                //          seekBar.setDrawingCacheBackgroundColor(cMangeta);
                progressBar.setMax(w);


                this.hPanel.addView(progressBar, w, hb);
                return h;*/
            }
            return progressBar;
        }
        boolean progress = false;
        void progress(int num){
            if (!progress)
                progress = true;
            progressBar().setMax(getCount());
            progressBar.setProgress(num);
        }
        class ButtonPanel2 extends ButtonPanel {

            @Override
            int freshLayout(RelativeLayout panel, int w, int x0, int y0, int yOffset) {

                //durationInfo.setTextAppearance(FreeShell.this, R.style.font0);
                //x0 = 0;
                info0.setTextAppearance(FreeShell.this, R.style.fontButton);
                info0.setBackgroundColor(Color.TRANSPARENT);
                info0.setTextColor(cBlack);
                info0.measure(
                        makeMeasureSpec,
                        makeMeasureSpec);

                info0.setX(0);
                info0.setY(0);

                int ht = info0.getMeasuredHeight();

                int h = super.freshLayout(panel, w, x0, y0, ht);
                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(right, ht);
                info0.setScrollbarFadingEnabled(true);

                hButtons = h;

                hPanel.addView(info0, layout);
                if (params.height < h) {
                    panel.removeView(hScroll);
                    hScroll.removeView(hPanel);
                    params.height = h;
                    hscrollParams.height = h;
                    hScroll.addView(hPanel, params);
                    panel.addView(hScroll, hscrollParams);
                }


                return h;
            }

        }

        ButtonPanel newButtonPanel(){
            return new ButtonPanel2();
        }

        void refresh(){
            showFirst();
        }

        private void refreshByButton() {
            refresh();
            mainRelayout2();
        }


        void close(){
            clear();
            Adapter.this.close(listPanel().panels());
        }

        void postCreate(){
            buttonPanel.addLine(null, 0);
            close =
                    new ShellButton("\u274CClose",
                            this::close,
                            cRed,
                            bRed, 0, buttonPanel);

            buttonPanel.normalize();
            lock();
        }

        int getCount(){
            if (current == null)
                return 0;
            return current.size();
        }

        ListView0 listView(){
            if (listView == null)
                listView = new ListView0();
            return listView;
        }

        @Override
        public void save(PrintWriter writer) {
            writer.println("position=" + listView.getSelectedItemPosition());
            if (notEmpty(filteredWord))
                writer.println("filtering=" + filteredWord);
            writer.println("scrollX=" + buttonPanel.hScroll().getScrollX());
            writer.println("information=" + decode(info0.getText().toString()));
        }


        @Override
        public void start(Tag tag){
            listView().loadedPosition = tag.getIntByLink("#position");
        }




        @Override
        public boolean load(Tag tag) {

            filteredWord = tag.getStringByLink("#filtering");
            if (notEmpty(filteredWord)) {
                find.setFlag(true);
            }else
                find.setFlag(false);

            final int x = tag.getIntByLink("#scrollX");
            buttonPanel.hScroll().postDelayed(new Runnable() {
                @Override
                public void run() {
                    buttonPanel.hScroll().smoothScrollTo(x, 0);
                }
            }, 1000);
            String info = tag.getStringByLink("#information");
            if (notEmpty(info)){
                info0.setText(info);
            }
            return true;
        }

        abstract void showFirst();

        void show(){
            if (listPanel().list.contains(listView)) {
                goToWindow();
                mainReLayout();
                return;
            }
            /*if (closed) {
                closed = false;
                restore();
            }else*/
            if (allItems == null)
                showFirst();
            mainReLayout();
        }

        void setAll(ArrayList<Object> list) {
            clear();


            allItems = list;
            if (indexMap == null)
                indexMap = new IntIntTable(allItems.size());
            else
                indexMap.clear();

            if (notEmpty(filteredWord)) {
                filter();
            } else {
                setCurrent(allItems);
            }
            setDefaultText();

            //listView.relayoutList();
            //mainReLayout();
        }


        void changeFiltering() {

            if (notEmpty(filteredWord)) {
                filteredWord = "";
                find.setFlag(false);
                find.view.invalidate();
                setCurrent(allItems);

                setDefaultText();
                mainReLayout();
            }else{
                find.setFlag(true);

                find.view.invalidate();
                findInName(getWord());
            }
        }

        String filteredWord = "";
        void filter() {
            if (!notEmpty(filteredWord)){
                findInName(getWord());
            }
            String s = filteredWord;
            indexMap.clear();

            if (notEmpty(s)) {
                List<Object> list = new ArrayList<>(allItems.size());
                for (int i = 0, j = 0; i < allItems.size(); i++) {
                    Object o = allItems.get(i);
                    String c = allItems.get(i).toString();
                    if (c.toLowerCase().indexOf(s) >= 0) {
                        indexMap.put(j, i);
                        j++;
                        list.add(o);
                    }
                }
                setCurrent(list);
            } else {
                setCurrent(allItems);

            }
            listView.invalidate();
        }


        void findInName(String name){
            AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
            final EditText edittext = new EditText(FreeShell.this);
            edittext.setText(name);
            alert.setMessage("Find by name");
            alert.setTitle("Text to find:");

            alert.setView(edittext);

            alert.setPositiveButton("Find", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    filteredWord = edittext.getText().toString();
                    setDefaultText();
                        filter();
                        mainReLayout();

                }
            });
            alert.setNeutralButton("Clear text", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    findInName("");
                    return;
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });

            alertShow(alert, edittext);

        }





        float x() {
              return listView.getX();
        }

        float y() {
            return listView.getY() + listView.scrollY;
        }
        public void create0(int i0, int i1){
            //super(FreeShell.this, layout);
            listView();
            refresh =
                    new ShellButton("\u27F3",
                            this::refreshByButton,
                            cCyan, bCyan, i0, buttonPanel);

            find =
                    new ShellButton("Filter",
                            this::changeFiltering,
                            notEmpty(filteredWord), i1, buttonPanel);

        }



        public void init(int index1) {
            freeShell.log("init " + index1 + " " + this.getName());
            if (listView().v == null) {
                listView.setBackgroundColor(Color.TRANSPARENT);
                listView.setFocusable(true);
                listView.setAdapter(this);
            }else {
                if (listView.index == index1){
                    goToWindow();
                }

                if ((listView.index >= 0 )&&(listView.index < index1))
                    index1--;
                listPanel().list.removeElementAt(listView.index);

            }

            if (index1 < 0){
                log("listView.index < 0");
                index1 = 0;
            }

            listPanel().list.insertElementAt(listView, index1);

            if (!loaded)
                return;
            checkVScroll();
            goToWindow();

        /*    if (index1 == 0){
                listPanel.hScrollPanels().scrollTo(0, 0);
            }*/
        }





        void close(RelativeLayout panel) {
            closed = true;
            clear();
            buttonPanel.close(panel);
            panel.removeView(listView);
            listView.remove();
            myLayout().invalidate();
            mainReLayout();
            System.gc();

        }

        void checkVScroll(){
            vScroll().postDelayed(new Runnable() {
                @Override
                public void run() {
                    /*int x = (listView.index - 1) * listW;
                   listPanel().hScrollPanels().smoothScrollTo(x, listPanel().hScrollPanels().getScrollY());*/



                    int pos = listView().getSelectedItemPosition();
                    if (pos > getCount()){
                        pos = getCount() - 1;
                        listView.setSelection(pos);
                    }

                    int newY;
                    int bottom;
                    if (getCount() == 0){
                        newY = (int)firstScreen().hScroll().getY()- hb;
                        bottom = (int)listView().getY() + listPanel().y;
                    }else {
                        Item item = null;
                        try {
                            item = getView(pos);
                            newY = (int) item.panel.getY();
                            newY += listView().getY();
                            newY += listPanel().y;
                            bottom = newY + item.panel.getMeasuredHeight();

                        } catch (Exception e) {
                            error(e);
                            return;
                        }
                    }
                    int oldY = vScroll.getScrollY();
                    if ((newY < oldY) || (bottom > oldY + getDisplayHeight())){
                        newY -= (getDisplayHeight()>>1);
                        if (newY < 0)
                            newY = 0;
                        vScroll().smoothScrollTo(vScroll.getScrollX(), newY);
                    }

                    //vScroll().scrollTo(vScroll.getScrollX(), newY + getS);
                    //int oldY = vScroll.getScrollY();
                    //if (oldY )
                    //newY += firstScreen().hButtons + margin;
                  /*  int oldY = vScroll.getScrollY();

                        int bottom = listView.getBottom();
                        bottom += listView().getY() + hb;
                        if (oldY + getDisplayHeight() > bottom){
                            newY = bottom - getDisplayHeight();
                            if (newY < 0){
                                newY = 0;
                            }

                        }
        //           vScroll().scrollTo(vScroll.getScrollX(), newY);*/


                }
            }, 100);
        }

        void mainRelayout2(){
          //  goToWindow();
            if (!loaded)
                return;
            mainReLayout();
            checkVScroll();
            //listView().allHeight();

           /* int y = listPanel().y;
            y += (int)y();

            if (y < getDisplayHeight() - hb){

                y = vScroll.getScrollY();
            }
            int index = listView.index;
            int x = index * listW;*/

            //mainReLayout(x, y, this, listView.getSelectedItemPosition());


        }

        void showList(ArrayList<Object> list, Integer pos) {
            setAll(list);
            if (pos != null)
                listView.setSelection(pos);
            if (pos != null)
                mainRelayout2();
        }



        int freshLayout(int x0, int y0, int w, RelativeLayout panel, RelativeLayout.LayoutParams paramsPanel) {
            return buttonPanel.freshLayout(panel, w, x0, y0, 0) + margin;
        }

        int reLayout(int x0, int y0) {
            return buttonPanel.reLayout(x0, y0) + margin;
        }

        public abstract Item getView(int position, int p0);


        List<Object> current;
        void setCurrent(List<Object> all){
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    items.value().elementAt(i).remove();
                }
            }
            if (listView != null) {
                listView.allHeight = null;
                listView.removeAllViews();
            }
            current = all;
        }
        void clear(){
            setCurrent(null);
            if (items != null)
                items.clear();
        }

        int getCurrentPosition(int globalPosition) throws Exception{
            if (notEmpty(filteredWord)) {
                int pos0 = indexMap.get(globalPosition);
                if (pos0 < 0){
                    throw new Exception("Internal error #1");
                }
                return pos0;
            }else
                return globalPosition;

        }

        //@Override
        public Item getView(int position) throws Exception {
            int pos0 = getCurrentPosition(position);
            Item item = items.get(pos0);
            if (item == null) {
                item = getView(pos0, position);
                if (item != null) {
                    items.put(pos0, item);
                }else{

                }
            }
            if (item == null)
                return null;
            return item;
        }



    }
    private String getUniqueName(String path, String name, String ext) {
        String n = path + '/' + name + '.' + ext;
        File f = new File(n);
        int counter = 0;
        while (f.exists()){
            counter++;
            n = path + '/' + name + '-' + counter + '.' + ext;
            if (counter > 99){
                onBackPressed();
                printError("counter > 100");
                return null;
            }
            f = new File(n);
        }
        if (counter == 0)
            return name + '.' + ext;
        else
            return name + '-' + counter + '.' + ext;
    }


    private File getUniqueName(String path, String ext) {
        String n = path + '.' + ext;
        File f = new File(n);
        int counter = 1;
        while (f.exists()){
            n = path + counter + '.' + ext;
            counter++;
            if (counter > 99){
                onBackPressed();
                printError("counter > 100");
                return null;
            }
            f = new File(n);
        }
        return f;
    }


    FSync fSync;
    FSync fSync(){
        if (fSync == null){
            fSync = new FSync(this);
            new Thread(fSync).start();
        }
        return fSync;
    }

    HashMap<String, HashSet<FileAdapter>> dirFileAdapter = new HashMap<>();


    class FindAdapter extends FileAdapter{

        File base;

        public void setBase(File sources) {
            base = sources;
        }

        @Override
        void close() {
            if (base != null){
                File b = base;
                super.close();
                openAdapter(b.getAbsolutePath());
            }else
                super.close();
        }

        void backDir() {
            if (storageList.dirsSize() == 0) {
                if (base != null){
                    openSomeDir(base);
                    base = null;

                }
            }
            super.backDir();
        }
        void openFile(File f, boolean addParent, Integer position){
            base = null;
            super.openFile(f, addParent, position);
        }
        void openFile(File f, boolean addParent, Integer position, Runnable run) {
            base = null;
            super.openFile(f, addParent, position, run);
        }

        @Override
        public void save(PrintWriter writer) {
            super.save(writer);
            if (base != null){
                writer.println("base=" + base.getAbsolutePath());
                writer.println("<files>");
                ArrayList<File> files = storageList.getFiles();
                if (files != null) {
                    for (File file : files) {
                        String path = file.getAbsolutePath();
                        writer.println(path);
                    }
                }
                writer.println("</files>");

            }
        }

        @Override
        public void start(Tag tag) {
            String base = tag.getStringByLink("#base");
            if (notEmpty(base)) {
                File fBase = new File(base);
                if (fBase.exists()) {
                    Tag files = tag.getTagByLink("#files");
                    if (files != null) {
                        Vector<File> v = new Vector();
                        Array<String> sb = files.getText();
                        if ((sb != null) && (sb.size() > 0)) {
                            for (int i = 0; i < sb.size(); i++) {
                                String s = sb.elementAt(i);
                                File f = new File(s);
                                if (f.exists()) {
                                    v.add(f);
                                }
                            }
                        }
                        if (v.size() > 0){
                            init(v, fBase);
                            return;
                        }else {
                            openSomeDir(fBase);
                            return;
                        }
                    }
                }
            }
            super.start(tag);
        }

        @Override
        public String getName(){
            return "findAdapter";
        }

        public void init(Vector<File> result, File base) {
            File[] f = new File[result.size()];
            result.toArray(f);
            showList(f, " files found from " + base.getAbsolutePath());
            setBase(base);
            backDirButtonOn();
        }
    }

    public FindAdapter findAdapter(){
        return new FindAdapter();
    }

    abstract class DirAdapter extends Adapter{
        abstract File getCurrentDir();
        abstract String getDir();
        abstract boolean canSaveFile();
        abstract public List<File> getCurrents();
        abstract public int getAbc();
        abstract public File getBaseZip();
    }

    class FileAdapter extends DirAdapter{

        StorageList storageList = new StorageList(FreeShell.this, this, fSync());
        HTable<String, FileItem> itemsByPath = new HTable(String.class, FileItem.class, 32);
         int abc = StorageList.ABC_SORT;
        ShellButton zip, copy, overwrite, move, moveOverwrite, del, newDir, newPanel, backDirButton, abcButton, sizeButton, timeButton, sudo;
        public File getBaseZip(){
            return null;

        }

        @Override
        public void unLock(){
            getStorageList().setOpened(true);
            super.unLock();
        }
        public int getAbc(){
            return abc;
        }
        public boolean canSaveFile(){
            return true;
        }
        void init(int index, int abc){
            //this.foundFileListener = listener;
            init(index);
            this.abc = abc;
            listView().setSelection(0);

        }

        Files getFileList(File file){
            ArrayList<File> files = storageList.getFiles();
            if (isEmpty(files))
                return new Files(0);
            int size = files.size();
            Files ret = new Files(size);
            for(int i = 0; i < files.size(); i++){
                try {
                    File f = files.get(i);
                    if (f != null) {
                        if (!f.isDirectory()) {
                            if (AudioServiceBinder.isAudioExtension(getFileExt(f.getAbsolutePath()))) {
                                if (f.equals(file)) {
                                    ret.setPosition(ret.size());
                                }
                                ret.add(f);
                            }

                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            return ret;
        }


        void setDefaultText(){
            if (notEmpty(filteredWord)){
                info0.setText("Name filter: " + filteredWord);
            }else {
                String dir = getDir();
                if (notEmpty(dir)) {
                    info0.setText(dir);
                }else
                    info0.setText("");
            }
            info0.invalidate();
        }

        public List<File> getCurrents(){
            if (notEmpty(filteredWord)) {
                ArrayList<File> ret = new ArrayList<>(current.size());
                int offset = storageList.dirsSize();
                for (int i = 0; i < current.size(); i++) {
                    int pos = indexMap.get(i);
                    if (pos >= offset){
                        ret.add(storageList.getFile(pos - offset));
                    }
                }
                return ret;
            }else{
                return storageList.getFiles();
            }
        };

        @Override
        void close(){
            if (set != null){
                set.remove(this);
            }
            super.close();
        }

        HashSet<FileAdapter> set;
        String setPath;
        @Override
        void setAll(ArrayList<Object> all){
            if (set != null) {
                set.remove(this);
                if (set.size() == 0){
                    dirFileAdapter.remove(setPath);
                }
            }
            super.setAll(all);
            String dir = getDir();
            if (!notEmpty(dir)){
                dir = "";
            }
            HashSet<FileAdapter> l = dirFileAdapter.get(dir);
            if (l == null) {
                l = new HashSet();
                dirFileAdapter.put(dir, l);
            }
            set = l;
            setPath = dir;
            l.add(this);
        }

        @Override
        void showList(ArrayList<Object> list, Integer pos) {
            try {
                setDefaultText();
                super.showList(list, pos);
                if (getStorageList().dirsSize() == 1){
                    backDirButtonOff();
                }
            }finally {
                getStorageList().setOpened(true);
            }
        }
        void beginOfAction(int actions) {
            lock(actions);
        }

        void endOfAction() {
            File f = getCurrentDir();
            if (f == null)
                return;
            if (!f.exists()){
                close();
                return;
            }
            File[] files = f.listFiles();
            if (notEmpty(files)){
                if (files.length != storageList.filesSize()){
                    refresh();
                }
            }else{
                if (storageList.filesSize() != 0){
                    refresh();
                }
            }
            unLock();
        }

        @Override
        public void refresh(){
            getStorageList().refresh();
        }


        void openFile(File f, boolean addParent, Integer position){
            lock();
            storageList.openFile(f, addParent, position, null);
        }
        void openFile(File f, boolean addParent, Integer position, Runnable run){
            lock();
            storageList.openFile(f, addParent, position, run);
        }


        void backDirButtonOn(){
            backDirButton.setFlag(true);
            backDirButton.view.invalidate();

        }

        void backDirButtonOff(){
            backDirButton.setFlag(false);
            backDirButton.view.invalidate();

        }


        StorageManager getStorageManager() {
            return (StorageManager) FreeShell.this.getSystemService(Context.STORAGE_SERVICE);
        }

        void printError(String s){
            FreeShell.this.printError(s);
        }

        public void reLayout(){
            freeShell.myLayout.invalidate();
        }

        StorageList getStorageList(){
            return storageList;
        }


        String getDir(){
            File dir = storageList.getCurrentDir();
            if (dir != null)
                return dir.getAbsolutePath();
            return null;
        }

        File getCurrentDir(){
            return storageList.getCurrentDir();
        }

        void openSomeDir(File dir){
            if (dir.exists())
                storageList.openSomeDir(dir);
            else
                storageList.showStorageList();
        }

        public FileItem getFileItem(int position) throws Exception {
            return (FileItem) super.getView(position);
        }

        @Override
        void clear(){
            File f = getCurrentDir();
            if (f != null) {
                String p = f.getAbsolutePath();
                HashSet<FileAdapter> l = dirFileAdapter.get(p);
                if (l != null) {
                    l.remove(this);
                }
            }

            itemsByPath.clear();
            super.clear();
        }

/*        public void su() {
                if (notEmpty(s)){
                    sudo2(s + "/" + command);
                }else
                    sudo2(command);
        }*/
        @Override
        ListView0 listView(){
            if (listView == null){
                listView = new ListView0(){
                    @Override
                    public void setIndex(IVector v, int val){
                        filePanels.removeElement(FileAdapter.this);
                        super.setIndex(v, val);
                        filePanels.put(FileAdapter.this);
                    }
                    @Override
                    public void remove(IVector v){
                        filePanels.removeElement(FileAdapter.this);
                        super.remove(v);
                    }
                    @Override
                    public void remove(){
                        filePanels.removeElement(FileAdapter.this);
                        super.remove();
                    }
                };

            }
            return listView;
        }
        void move(){
            if (fSync().hasSelected()) {
                    fSync().move(FileAdapter.this);
            }else{
                okDialog("Not found", "You can select a file by clicking on its number.");
            }

        }
        void moveOverwrite(){
            if (fSync().hasSelected()) {
                fSync().moveOverwrite(FileAdapter.this);
            }else{
                okDialog("Not found", "You can select a file by clicking on its number.");
            }

        }
        void copy(){
            if (fSync().hasSelected()) {
                fSync().copy(FileAdapter.this);
            }else{
                okDialog("Not found", "You can select a file by clicking on its number.");
            }

        }
        void overwrite(){
            if (fSync().hasSelected()) {
                fSync().overwrite(FileAdapter.this);
            }else{
                okDialog("Not found", "You can select a file by clicking on its number.");
            }

        }

        void del(){
            if (fSync().hasSelected()) {
                fSync().del(FileAdapter.this);
            }else{
                okDialog("Not found", "You can select a file by clicking on its number.");
            }

        }

        public FileAdapter(FileAdapter prev, int index) {
            create0(0, 0);
            ini();
            init(index);
            abc = prev.abc;
            showList(storageList.init(prev.storageList), prev.listView().getSelectedItemPosition());
        }
      /*  public void init(int index, File base) {
            getStorageList().setBase(base);
            backDirButtonOn();
            init(index);
        }*/
        public void showList(File[] files, String s) {
            showList(storageList.init(files), 0);
            info0.setText(s);
            info0.invalidate();
            unLock();
        }


        public FileAdapter() {
            create0(0, 0);
            ini();

        }

        void setFile(File f){
            firstScreen().setEditFileName(f.getParent(), f.getName());
        }

        void save_as(){
           // firstScreen().setEditFileName(getDir(), firstScreen().fileName);
            firstScreen().checkEditedFile(this::setFile, getDir(), firstScreen().getFileName(), text().getText().toString());
        }

/*        void save(){
            String path = firstScreen().getCurrentTextFile();
            if (notEmpty(path)){
                File f = new File(path);
                boolean refresh = false;
                if (!f.exists()){
                    refresh = true;
                }
                fSync().writeFile(path, text().getText().toString());
                if (refresh)
                    refreshDirs(f.getParent());
            }
            save_as();
        }
        void newFile(){
            String word = getWord();
            newFile(word);
        }
        void newFile(String name){
            AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
            final EditText edittext = new EditText(FreeShell.this);
            edittext.setText(getDir() + "/" + name);
            alert.setMessage("path/name");
            alert.setTitle("New/edit file");

            alert.setView(edittext);

            alert.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{

                        //What ever you want to do with the value
                        final String name = edittext.getText().toString();
                        File f = new File(name);
                        if (!f.exists()){
                            fSync().writeFile(f, getWholeText());
                        }
                        refreshDirs(f.getParent());
                        if (f.exists()){
                            openTextFile(FileAdapter.this, f, listView().index);
                        }
                    }catch(Exception t){
                        FreeShell.this.printError(t);
                    }

                }
            });
            alert.setNeutralButton("Clear text", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{
                        newFile("");
                        return;
                    }catch(Exception t){
                        FreeShell.this.printError(t);
                    }

                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });

            alertShow(alert, edittext);

        }*/
        public void ini(){
            new ShellButton("Save text here",
                    this::save_as,
                    cGreen, bGreen, 0, buttonPanel);
/*            new ShellButton("Save text",
                    this::save,
                    cGreen, bGreen, 0, buttonPanel);*/

/*            new ShellButton("New file",
                    this::newFile,
                    cBlue,
                    bGreen, 0, buttonPanel);*/



            buttonPanel.addLine(null, 0);
            zip =
                    new ShellButton("Zip selected", this::zip, cYellow, bYellow, 1, buttonPanel);
            new ShellButton("Find subtext", this::findSubtext, cOrange, bOrange, 1, buttonPanel);

            new ShellButton("Find filename", this::findFileName, cCyan, bCyan, 1, buttonPanel);

            copy =
                    new ShellButton("Paste selected", this::copy, cSel1, bSel1, 1, buttonPanel);
            overwrite =
                    new ShellButton("Overwrite", this::overwrite, cSel1, bSel1, 1, buttonPanel);
            move =
                    new ShellButton("Move selected", this::move, cRose, bRose, 1, buttonPanel);
            moveOverwrite =
                    new ShellButton("Move overwrite", this::moveOverwrite, cRose, bMangeta, 1, buttonPanel);
            del = new ShellButton("Delete files", this::del, cRed, bRed, 1, buttonPanel);


            backDirButton = new ShellButton("\u21D1",
                    this::backDir,
                    false, 2, buttonPanel){
                @Override
                void setFlag(boolean v){
                    if ((flag == null)||(v != flag)) {
                        background = v ? bLight : bOff;
                        button().setBackgroundColor(background);
                        button().setTextColor(v ? cMangeta : cOff);
                        flag = v;
                    }
                }

            };
            newDir = new ShellButton("New dir", this::newDir, cCyan, bCyan, 2, buttonPanel);
            newPanel =
                    new ShellButton("Clone panel",
                            new Procedure() {
                                @Override
                                public void procedure() {

                                    listPanel.newFilePanel(FileAdapter.this, listView.index + 1);
                                }
                            },
                            cRose,
                            bRose, 2, buttonPanel);

            //new ShellButton("new file", this::newFile, cGreen1, bGreen1, 2, buttonPanel);


            abcButton =
                    new ShellButton("Abc sort", this::abcSort, abc == StorageList.ABC_SORT, 2, buttonPanel);
            timeButton =
                    new ShellButton("Time sort", this::timeSort,abc == StorageList.MODIFY_SORT, 2, buttonPanel);

            sizeButton =
                    new ShellButton("Len sort", this::sizeSort
,abc == StorageList.SIZE_SORT, 2, buttonPanel);

            new ShellButton("Save panels here", this::saveMtkAs, cGreen, bGreen, 2, buttonPanel);

            new ShellButton("Save variables here",
                    this::saveVariablesAs,
                    cGreen1,
                    bGreen1, 2, buttonPanel);


/*            new ShellButton("help",
                    FreeShell.this::fileHelp,
                    cYellow,
                    bYellow, 2, buttonPanel);*/



            postCreate();
            /*show = new ShellButton("",
                    this::show,
                    cRose,
                    bRose, 0, firstScreen());*/
        }

        private void zip() {
            zip(this);
        }

        private void printPath(String s) {
            pressChar(" " + s);
        }
        private void copyPath(String s) {
            clip("path", s);
        }


        private void saveMtkAs() {
            FreeShell.this.saveMtkAs(this);

        }




        private void findFileName() {
            String word = getWord();

     /*       if ((foundFileListener != null) && (foundFileListener instanceof  FindNameListener)){
                ((FindNameListener)foundFileListener).start(word);
            }else {*/
                FindSubtextListener listener = new FindNameListener(this, word);
                listener.start(word);
            //}
        }

        //FSync.FoundFileListener foundFileListener;
        private void findSubtext() {
            String word = getWord();
/*
            if ((foundFileListener != null) && (foundFileListener instanceof  FindSubtextListener)){
                ((FindSubtextListener)foundFileListener).start(word);
            }else {*/
                FindSubtextListener listener = new FindSubtextListener(this, word);
                listener.start(word);
            //}
        }



        private void zip(FileAdapter adapter){
            if (!fSync().hasSelected()){
                okDialog("Not found", "You can select a file by clicking on its number.");
                return;
            }
            String s = "";
            boolean ok = false;
            String[] ss = fSync().getSelected();
            if (notEmpty(ss)){
                File f = new File(ss[0]);
                s = f.getName();
                int i = s.indexOf('.');
                if (i >= 0){
                    s = s.substring(i);
                    if (ss.length == 1){
                        ok = true;
                    }
                }
            }
            if (!ok) {
                String word = getWord();
                if (notEmpty(word)) {
                    s = word;
                }
            }
            if (!s.endsWith(".zip")){
                s = s + ".zip";
            }
            zip(s, adapter);

        }


        private void zip(String name, final FileAdapter adapter) {
            final String path = getDir();
            if (path == null){
                return;
            }
            AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
            final EditText edittext = new EditText(FreeShell.this);
            edittext.setText(path + "/" + name );
            alert.setMessage("To zip:");
            alert.setTitle("Create new zip file");

            alert.setView(edittext);

            alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{
                        //What ever you want to do with the value
                        String name0 = edittext.getText().toString();
                        if (!name0.endsWith(".zip")){
                            name0 = name0 + ".zip";
                        }
                        final String name2 = name0;
                        File newfile = new File(name2);
                        if (newfile.exists()){

                            AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                            // Specify the dialog is not cancelable
                            builder.setCancelable(false).setTitle(name2 + " already exists. Your can create zip with new name only.").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // what ever you want to do with No option.
                                }
                            }).setNeutralButton("Change name", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    zip("", adapter);
                                    // what ever you want to do with No option.
                                }
                            });
                            ;


                            AlertDialog dialog2 = builder.create();
                            // Display the alert dialog on interface
                            dialog2.show();

                            return;
                        }
                        try {
                            fSync().createZip(newfile, adapter);
                        } catch (Exception e) {
                            FreeShell.this.printError(e);
                        }
                        String currentDir = getDir();
                        refreshDirs(currentDir);

                        //listView.invalidate();
                    }catch(Exception tt){
                        FreeShell.this.printError(tt);
                    }

                }
            });
            alert.setNeutralButton("Clear text", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try {
                        zip("", adapter);
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }

                    return;
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });

            alertShow(alert, edittext);


        }



        void newDir(){
            newDir(getWord());
        }

        void newDir(String nameDir){
            String path = getDir();
            if (path == null){
                return;
            }
            AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
            final EditText edittext = new EditText(FreeShell.this);
            edittext.setText(nameDir);
            alert.setMessage(path);
            alert.setTitle("Create new directory");

            alert.setView(edittext);

            alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{
                        //What ever you want to do with the value
                        final String nameDir2 = edittext.getText().toString();
                        File newfile = new File(path + "/" + nameDir2);
                        if (newfile.exists()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                            // Specify the dialog is not cancelable
                            builder.setCancelable(false).setTitle(nameDir2 + " already exists.").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // what ever you want to do with No option.
                                }
                            }).setNeutralButton("Change name", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    newDir(nameDir2);
                                    // what ever you want to do with No option.
                                }
                            });
                            ;


                            AlertDialog dialog2 = builder.create();
                            // Display the alert dialog on interface
                            dialog2.show();

                            return;
                        }

                        String currentDir = getDir();
                        newfile.mkdirs();
                        //getStorageList().refresh();
                        openFile(newfile, true, null);
                        refreshDirs(currentDir);

                        //listView.invalidate();
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }

                }
            });
            alert.setNeutralButton("Clear text", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    newDir("");
                    return;
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });

            alertShow(alert, edittext);


        }

        void rename(FileItem fileItem){
            File f = fileItem.file;
            if (f == null)
                return;
            String path = f.getParent();
            AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
            final EditText edittext = new EditText(FreeShell.this);
            edittext.setText(f.getName());
            alert.setMessage(path);
            alert.setTitle("Rename");

            alert.setView(edittext);

            alert.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{
                        //What ever you want to do with the value
                        final String name2 = edittext.getText().toString();
                        File newfile = new File(path + "/" + name2);
                        if (newfile.exists()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                            // Specify the dialog is not cancelable
                            builder.setCancelable(false).setTitle(name2 + " already exists.").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // what ever you want to do with No option.
                                }
                            }).setNeutralButton("Change name", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try{
                                        rename(fileItem);
                                    }catch (Exception e){
                                        FreeShell.this.printError(e);
                                    }
                                    // what ever you want to do with No option.
                                }
                            });
                            ;


                            AlertDialog dialog2 = builder.create();
                            // Display the alert dialog on interface
                            dialog2.show();

                            return;
                        }

                        f.renameTo(newfile);
                        String currentDir = getDir();
                        refreshDirs(currentDir);

                        //listView.invalidate();
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }

                }
            });
            alert.setNeutralButton("Clear text", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{
                        newDir("");
                        return;
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });

            alertShow(alert, edittext);


        }

        private void sizeSort() {

            abc = StorageList.SIZE_SORT;
            sizeButton.setFlag(true);
            timeButton.setFlag(false);
            abcButton.setFlag(false);

            storageList.refresh();
            //listView.invalidateViews();

        }


        private void abcSort() {
            abc = StorageList.ABC_SORT;
            sizeButton.setFlag(false);
            timeButton.setFlag(false);
            abcButton.setFlag(true);
            storageList.refresh();
            //listView.invalidateViews();
        }
        private void timeSort() {
                    abc = StorageList.MODIFY_SORT;
                    sizeButton.setFlag(false);
                    timeButton.setFlag(true);
                    abcButton.setFlag(false);
                    storageList.refresh();
                    //listView.invalidateViews();
        }

        @Override
        void showFirst() {
            init(0);
            storageList.showStorageList();
        }


        void close(RelativeLayout panel){
        //    setIsDst(false);
            //firstScreen().lines.get(0).removeElement(show);
            filePanels.removeElement(this);
            super.close(panel);
        }

     /*   void setIsDst(boolean v){

            isDst = v;
            if (isDst){
                dstFiles.addElement(FileAdapter.this);
            }else{
                dstFiles.removeElement(FileAdapter.this);
            }
            dst.setFlag(isDst);

        }*/


        @Override
        public Item getView(final int position, final int filteredPosition) {
            final File f = storageList.getFile(position);
            FileItem fileItem = new FileItem(position);
            final boolean isParent = position < storageList.dirsSize();
            fileItem.init('?', f, isParent, this, b);

            b= !b;
            if (f != null) {
                itemsByPath.put(f.getAbsolutePath(), fileItem);
            }
            return fileItem;

        }





        void backDir() {
            if (!backDirButton.flag)
                return;
            lock();
            storageList.backDir();

        }



        void menu(final FileItem fileItem) {
            if (fileItem.file == null)
                return;
            showPopupMenu(fileItem);
        }


        private void showPopupMenu(final FileItem fileItem) {
            if (fileItem.file.isDirectory()){
                showFolderMenu(fileItem);
            }else{
                showFileMenu(fileItem);
            }
        }
        private void showFolderMenu(final FileItem fileItem) {
            PopupMenu popupMenu = new PopupMenu(freeShell, fileItem.settingsButton);
            popupMenu.inflate(R.menu.foldermenu);

            popupMenu
                    .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.rename:
                                    rename(fileItem);
                                    return true;
                                case R.id.copyPath:
                                    copyPath(fileItem.file.getAbsolutePath());
                                    return true;
                                case R.id.printPath:
                                    printPath(fileItem.file.getAbsolutePath());
                                    return true;

                                case R.id.delete:
                                    fSync().del(fileItem.file);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

            popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    Toast.makeText(getApplicationContext(), "onDismiss",
                            Toast.LENGTH_SHORT).show();
                }
            });
            popupMenu.show();

        }
        private void showFileMenu(final FileItem fileItem) {
            PopupMenu popupMenu = new PopupMenu(freeShell, fileItem.settingsButton);
            popupMenu.inflate(R.menu.popupmenu);

            popupMenu
                    .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.rename:
                                    rename(fileItem);
                                    return true;
                                case R.id.edit:
                                    openTextFile(fileItem.fileAdapter(), fileItem.file, listView().index);
                                    return true;
                                case R.id.sysOpen:
                                    openFileIntent(fileItem.file);
                                case R.id.copyPath:
                                    copyPath(fileItem.file.getAbsolutePath());
                                    return true;
                                case R.id.printPath:
                                    printPath(fileItem.file.getAbsolutePath());
                                    return true;
                                case R.id.delete:
                                    fSync().del(fileItem.file);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

            popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    Toast.makeText(getApplicationContext(), "onDismiss",
                            Toast.LENGTH_SHORT).show();
                }
            });
            popupMenu.show();

        }

        /*
                    PopupMenu popupMenu = new PopupMenu(freeShell, fileItem.selectButton);
            popupMenu.inflate(R.menu.popupmenu);

            popupMenu
                    .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.rename:
                                    rename(fileItem);
                                    return true;
                                case R.id.edit:
                                    openTextFile(fileItem.file, listView().index);
                                    return true;
                                case R.id.sysOpen:
                                    openFileIntent(fileItem.file);
                                    return true;
                                case R.id.delete:
                                    fSync().del(fileItem.file);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

            popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    Toast.makeText(getApplicationContext(), "onDismiss",
                            Toast.LENGTH_SHORT).show();
                }
            });
            popupMenu.show();

         */

        @Override
        public void save(PrintWriter writer) {
            super.save(writer);
            writer.println("sort=" + abc);
            File file = storageList.getCurrentDir();
            String dir;
            if (file != null) {
                dir = file.getAbsolutePath();
                writer.println("dir=" + dir);
            }
        }

        @Override
        public boolean load(Tag tag) {
            abc = tag.getIntByLink("#sort");
            if (abc == StorageList.ABC_SORT){
                abcSort();
            }else if (abc == StorageList.SIZE_SORT){
                sizeSort();
            }else if (abc == StorageList.MODIFY_SORT){
                timeSort();
            }
            super.load(tag);
            String f = tag.getStringByLink("#dir");
            if (notEmpty(f)) {
                openSomeDir(new File(f));
            }else {
                storageList.showStorageList();
            }
            return true;
        }

        @Override
        public void start(Tag tag) {
            super.start(tag);
        }

        @Override
        public String getName() {
            return "fileAdapter";
        }



        private void saveVariablesAs(){
            saveVariablesAs(getWord());
        }
        private void saveVariablesAs(String name){

            AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
            final EditText edittext = new EditText(FreeShell.this);
            edittext.setText(getDir()  + "/" + name);
            alert.setMessage("path/name");
            alert.setTitle("Save as");

            alert.setView(edittext);

            alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{

                        //What ever you want to do with the value
                        final String name = edittext.getText().toString();
                        File f = new File(name);
                        if (f.exists()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                            // Specify the dialog is not cancelable
                            builder.setCancelable(false).setTitle(name + " already exists. Overwrite?").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // what ever you want to do with No option.
                                }
                            }).setNeutralButton("Change name", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        saveVariablesAs("");
                                    } catch (Exception t) {
                                        FreeShell.this.printError(t);
                                    }

                                    // what ever you want to do with No option.
                                }
                            }).setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    saveVariables(name);
                                    Toast.makeText(freeShell, name + " was saved.", Toast.LENGTH_LONG).show();
                                    // what ever you want to do with No option.
                                }
                            });


                            AlertDialog dialog2 = builder.create();
                            // Display the alert dialog on interface
                            dialog2.show();

                            return;
                        }
                        saveVariables(name);
                        Toast.makeText(freeShell, name + " was saved.", Toast.LENGTH_LONG).show();
                        refreshDirs(f.getParent());
                    }catch(Exception t){
                        FreeShell.this.printError(t);
                    }

                }
            });
            alert.setNeutralButton("Clear file name", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{
                        saveVariablesAs("");
                        return;
                    }catch(Exception t){
                        FreeShell.this.printError(t);
                    }

                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });

            alertShow(alert, edittext);

        }
    }



    IOTable<ListView0> xTable= new IOTable<ListView0>(ListView0.class, 16);

    class ListView0 extends RelativeLayout implements LiveVector.Element{
        Integer allHeight;
        Adapter adapter;
        IVector v;
        int index = -1;
        int wButtons, hButtons, xButtons, yButtons;
        boolean fresh = true;
        int position = 0;
        RelativeLayout.LayoutParams layoutParams;
        boolean lock = false;
        Integer loadedPosition;

        void lock2(){
            lock = true;
            setBackgroundColor(bPleaseWait);
            invalidate();
        }

        void unLock2(){
            lock = false;
            setBackgroundColor(Color.TRANSPARENT);
            invalidate();
        }

        int getSelectedItemPosition(){
            return position;
        }
        void setSelection(int pos){
            if (loadedPosition != null){
                if (pos < getCount()) {
                    position = loadedPosition;
                    loadedPosition = null;
                    return;
                }
                loadedPosition = null;
            }
            if (pos < getCount())
                position = pos;
        }
        void setAdapter(Adapter adapter){
            this.adapter = adapter;
        }
        Adapter getAdapter(){
            return adapter;
        }

        public int getCount(){
            return adapter.getCount();
        }


        public ListView0() {
            super(FreeShell.this);
        }


        int counter = 0;
        int scrollY;
        int allHeight(){
            if (allHeight == null){
                super.removeAllViews();
                /*if (adapter instanceof FileAdapter)
                    printLog("start: " + ((FileAdapter)adapter).getDir());*/
                counter = 0;
                allHeight = 0;
                scrollY = 0;
            }

            if (counter < getCount()){
                int totalHeight = allHeight;
                Adapter adapter = getAdapter();
                final ArrayList<Item> arr = new ArrayList(32);
                int b = Thread.currentThread().getPriority();
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                for (int i = 0; (i < getCount()) && (counter < getCount()); i++) {
                    Item item = null;
                    try {
                        item = adapter.getView(counter);
                    } catch (Exception e) {
                        error(e);
                    }
                    counter++;
                    if (counter >> 5 << 5 == 0){
                        adapter.progress(counter);
                    }
                    if (item == null)
                        continue;
                    if (item.loadIcon != null)
                        arr.add(item);
/*                    if (item.loadIcon != null)
                        item.loadIcon.run();*/
                    View mView = item.panel;
                    mView.measure(
                            makeMeasureSpec,

                            makeMeasureSpec);
                    mView.setX(0);
                    mView.setY(totalHeight);
                    item.panelLayout.height = mView.getMeasuredHeight();

                    mView.setMinimumHeight(item.panelLayout.height);
                    addView(mView, item.panelLayout);
                    if (item.file != null)
                        fSync().newFileItem(item.file.getAbsolutePath(), item);

                    if (i == position)
                        scrollY = totalHeight;
                    int h = mView.getMeasuredHeight();//mView.getHeight();//fileItem.panelLayout.height;
                    totalHeight += h + margin;//mView.getMeasuredHeight();
                }
                    totalHeight = totalHeight + (max >> 2);
                allHeight = totalHeight;
                Thread.currentThread().setPriority(b);
                getAdapter().unLock();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(Item item: arr) {
                            if (item != null) {
                                if (item.loadIcon != null)
                                    item.loadIcon.run();
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    Thread.yield();
                                }
                            }
                        }
                    }
                }).start();

                //setX(xButtons);
                //setY(yButtons);
                //listPanel().panels().addView(this);


                super.invalidate();

            }

            if (getCount() > counter){
                adapter.progress(counter);
                float d = getCount() / counter;
                return (int)(d * allHeight);
            }else{
                return allHeight;
            }

        }
        @Override
        public void setIndex(IVector v, int val) {
            if ((this.v != null)&&(this.v != v))
                remove();
            this.index = val;
            this.v = v;

        }

        @Override
        public void remove(IVector v) {
            if (this.v == v) {
                index = -1;
            }
        }

        @Override
        public void remove() {
            if (v != null){
                v.removeElementAt(index);
            }
        }
        Adapter getMyAdapter(){
            return (Adapter) getAdapter();
        }

        void stop(){
            if ((allHeight == null)/*||(allHeight == 0)*/)
                return;
            adapter.unLock();
           /* if (adapter instanceof FileAdapter) {
                printLog("stop: " + ((FileAdapter)adapter).getDir());
            }*/
/*            new Thread(new Runnable() {
                @Override
                public void run() {
                    int counter2 = 0;
                    for(int i = 0; i < getCount(); i++){
                        final int  ii = i;
                        freeShell.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Item item = adapter.getView(ii);
                                if (item != null){
                                    if (item.loadIcon != null)
                                        item.loadIcon.run();
                                }

                            }
                        });
                    }
                }
            }).start();*/
        }

        RelativeLayout.LayoutParams  relayoutList(int x, int y, int w, RelativeLayout panel, RelativeLayout.LayoutParams paramsPanel){
            //super.removeAllViews();
            //allHeight = null;
            //mainReLayout();
            //listPanel().panels().removeView(this);
            int all = allHeight();
            int h = listPanel().getHeight();
            if (h + hButtons < all){
                listPanel().setH(all);
            }

            //setMinimumHeight(all);
            //if (layoutParams == null)
                layoutParams = new RelativeLayout.LayoutParams(listW, all);
            /*else{
                layoutParams.height = all;
            }*/
            setLayoutParams(layoutParams);
            invalidate();
            if (allHeight != null) {
                if (counter < getCount()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                Thread.yield();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (allHeight != null) {
                                        if (counter < getCount()) {
                                            mainReLayout();
                                        }
                                    }else{
                                        stop();

                                    }
                                }
                            });
                        }
                    }).start();;
                }else{
                    stop();

                }
            }

        /*    if (allHeight != null) {
                if (counter < getCount()) {
                    this.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            final Runnable run = this;

                            if (allHeight == null)
                                return;
                          //  listPanel().panels().removeView(ListView0.this);
                            int all = allHeight();
                            ListView0.this.setMinimumHeight(all);
                            ListView0.this.layoutParams.height = all;

                            ListView0.this.requestLayout();
                        //    listPanel().panels().addView(ListView0.this);

                            // layoutParams = new RelativeLayout.LayoutParams(listW, all);
                            //setLayoutParams(layoutParams);
                            int h0 = all + (int) getY();
                            int h = listPanel().panels().getHeight();
                            if (h0 > h) {
                              //  listPanel().hScrollPanels().removeView(listPanel().panels);
                                listPanel().panels().setMinimumHeight(h0);
                                listPanel().panelsParams(listPanel().panelsParams.width, h0);
//                                listPanel().panelsParams.height = h0;
                                listPanel().panels().requestLayout();
                             //   listPanel().hScrollPanels().addView(listPanel().panels);
                                //  RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(listPanel().panels().getWidth(), h + hButtons);
                                //  listPanel().panels().setLayoutParams(layoutParams);

                                //listPanel().panels().invalidate();
                            } else
                                h0 = h;

                            int h1 = (int)listPanel().panels().getY() + h0;
                            h = listPanel().hScrollPanels().getHeight();
                            if (h1 > h) {
                         //      myLayout.removeView(listPanel().hScrollPanels());
                                listPanel().hScrollPanels().setMinimumHeight(h1);
                                listPanel().hScrollPanels().requestLayout();
                         //       myLayout.addView(listPanel().hScrollPanels());
                                //listPanel().hScrollPanels().invalidate();
                            }else
                                h1 = h;

                            h = myLayout().getHeight();
                            int h2 = h1 + (int) listPanel().hScrollPanels().getY();
                            if (h2 > h) {
                        //        vScroll().removeView(myLayout);
                                myLayout.setMinimumHeight(h2);
                                myLayout.requestLayout();
                    //            vScroll().addView(myLayout);
                            }
                            baseLayout.requestLayout();
                            if (allHeight != null) {
                                if (counter < getCount()) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                Thread.yield();
                                            }
                                            runOnUiThread(run);
                                        }
                                    }).start();
                                    //ListView0.this.postDelayed(run, 300);
                                }else{
                                    if (adapter instanceof FileAdapter) {
                                        printLog("stop: " + ((FileAdapter)adapter).getDir());
                                    }
                                }
                                //reLayout(x, y, w, panel, paramsPanel);
                            }
                        }


                    }, 300);

                }
            }*/
            //listPanel().panels().addView(this, layoutParams);
            //invalidateViews();
            return layoutParams;
        }
        RelativeLayout panel;
        int freshLayout(final int x, final int y, final int w, final RelativeLayout panel, RelativeLayout.LayoutParams paramsPanel){
            if (!fresh)
                return reLayout(x, y, w, panel, paramsPanel);
            fresh = false;
            final int pos = getSelectedItemPosition();
            this.panel = panel;
            //lock();
            panel.removeView(this);
            //panel.removeView(this);
            Adapter listAdapter = getAdapter();
            xButtons = x;

            xTable.put(x, this);
            yButtons = y;
            wButtons = w;

            int d = listAdapter.freshLayout(x, y, w, panel, paramsPanel);
            hButtons = d;
            setX(x);
            setY(d);



            layoutParams = relayoutList( x, y, w, panel, paramsPanel);
            this.setLayoutParams(layoutParams);
/*            layoutParams.width = w;
            setMinimumWidth(w);
            setMinimumHeight();*/
            int allHeight;
            if (this.allHeight == null)
                allHeight = 0;
            else
                allHeight = this.allHeight;

            int hPanel = hButtons + allHeight;

            if (paramsPanel.height < hPanel + y) {
                paramsPanel.height = hPanel + y;
                panel.setLayoutParams(paramsPanel);
            }
            //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, hPanel);
            setSelection(pos);
            panel.invalidate();
            invalidate();
            panel.addView(this, layoutParams);
            //unLock();

            //panel.addView(this, layoutParams);
            //panel.invalidate();
            //this.invalidateViews();
            return hPanel;
        }



        int reLayout(int x, int y, int w, RelativeLayout panel, RelativeLayout.LayoutParams paramsPanel){
            //lock();
            panel.removeView(this);
            int pos = getSelectedItemPosition();
            Adapter listAdapter = getAdapter();
            xButtons = x;
            xTable.put(x, this);
            yButtons = y;
            wButtons = w;

            int d = listAdapter.reLayout(x, y);
            hButtons = d;
            setX(x);
            setY(d);
            setMinimumWidth(w);



            int hPanel = hButtons + relayoutList(x, y, w, panel, paramsPanel).height;

            if (paramsPanel.height < hPanel + y) {
                paramsPanel.height = hPanel + y;
                //panel.setLayoutParams(paramsPanel);
                panel.setMinimumHeight(hPanel + y);
                panel.invalidate();
            }
            setSelection(pos);
            panel.addView(this, layoutParams);
//            unLock();
            invalidate();
            return hPanel;
        }
    }
    class ListPanel extends Layout{

        FileAdapter photoPanel;
        FileAdapter photoPanel(){
            if (photoPanel == null){
                photoPanel = new FileAdapter(){

                    @Override
                    void close(){
                        photoPanel = null;
                        super.close();
                    }
                };
                photoPanel.init(0);
            }
            return photoPanel;
        }
        void showPhotoPanel(File videoDir){
            if (videoDir.exists()) {
                //photoPanel().timeSort();
                photoPanel().openSomeDir(videoDir);
            }
            int x = (int)photoPanel().x();
            int y = (int)photoPanel.y();
            listPanel().hScrollPanels().postDelayed(new Runnable() {
                @Override
                public void run() {
                    listPanel().hScrollPanels().smoothScrollTo(x, y);
                }
            }, 1000);
            myLayout().invalidate();

        }


        void hide(){
            if (hScrollPanels != null)
                myLayout().removeView(hScrollPanels);
        }

        void newFilePanel(FileAdapter prev, int index1){

            FileAdapter currentFiles = new FileAdapter(prev, index1);

            FreeShell.this.mainReLayout();
            myLayout().invalidate();
        }

        void newZipPanel(ZipAdapter prev, int index1){

            ZipAdapter currentFiles = new ZipAdapter(prev, index1);

            FreeShell.this.mainReLayout();
            myLayout().invalidate();
        }


        void fresh(){
            fresh = true;
            /*for(int i = 0; i < list.size(); i++){
                list.elementAt(i).fresh = true;
            }*/

        }
        IVector<ListView0> list = new LiveVector(ListView0.class, 4);
        HorizontalScrollView hScrollPanels;
        HorizontalScrollView hScrollPanels(){
            if (hScrollPanels == null){
                hScrollPanels = new HorizontalScrollView(FreeShell.this){

                    @Override
                    public void scrollTo(int x, int y){

                       // vScroll().scrollTo(0, y + listPanel().y);
                        //super.scrollTo(x, 0);
                        super.scrollTo(x, y);
                    }


                    @Override
                    public boolean onInterceptTouchEvent(MotionEvent ev) {
                       // vScroll().scrollTo(x, y);

                        //int dy = vScroll.getScrollY();

                       // int yPanel = listPanel().y;

                        int y = (int)ev.getY();
                    //    y += vScroll.getScrollY();
/*                        if (y < hb)
                            return false;*/
                        int x = (int)ev.getX();
                        x += hScrollPanels.getScrollX();
                        int i = xTable.code().findMiddle(x);
                        if (i > 0){
                            ListView0 listView0 = xTable.value().elementAt(i - 1);
                            int xp = listView0.xButtons;
                            if (x >= xp){
                                if (x < xp + listView0.wButtons){
                                    if (listView0.getMyAdapter().buttonPanel.right > listView0.wButtons) {
                                        int bottom = listView0.hButtons;
                                        if (y < bottom)
                                            return false;
                                    }
                                }
                            }
                        }

                        boolean ret = super.onInterceptTouchEvent(ev);
                   /*     ev.offsetLocation(0, -getY());
                        vScroll.onInterceptTouchEvent(ev);*/


                        return ret;

                    }


                };
                hScrollPanels.setScrollbarFadingEnabled(false);
            }
            return hScrollPanels;
        }

        RelativeLayout panels;
        RelativeLayout panels(){
            if (panels == null){
                panels = new RelativeLayout(FreeShell.this);
            }
            return panels;
        }
        int getSize(){
            return list.size();
        }


       /* RelativeLayout.LayoutParams hScrollPanelsParams;
        RelativeLayout.LayoutParams hScrollPanelsParams(int w, int h){
            if (hScrollPanelsParams == null){
                hScrollPanelsParams = new RelativeLayout.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, h);
                hScrollPanels().setLayoutParams(hScrollPanelsParams);
            }
            if ((hScrollPanelsParams.width != w)||(hScrollPanelsParams.height != h)) {
                hScrollPanelsParams.width = ScrollView.LayoutParams.MATCH_PARENT;
                hScrollPanelsParams.height = h;
                hScrollPanels.invalidate();
            }
            return hScrollPanelsParams;

        }*/

        RelativeLayout.LayoutParams panelsParams;
        RelativeLayout.LayoutParams panelsParams(int w, int h){
            if (panelsParams == null){
                panelsParams = new RelativeLayout.LayoutParams(w, h);
                panels().setLayoutParams(panelsParams);
            }
            if ((panelsParams.width != w)||(panelsParams.height != h)) {
                panelsParams.width = w;
                panelsParams.height = h;
                panels.invalidate();
            }
            return panelsParams;
        }
        int getHeight(){
            return h;
        }

        void setH(int h){
            this.h = h;
                //hScrollPanelsParams(displayMetrics.widthPixels, h);
                panelsParams(getAllWidth(), h);
        }

        int getAllWidth(){
            return (listW + margin) * getSize();
        }
        boolean fresh = true;
        int freshLayout(RelativeLayout panel, int w, int x0, int y0) {
            if (!fresh)
                return reLayout(panel, w, x0, y0);
            fresh = false;
            this.w = w;
            x = x0;
            y = y0;


            panel.removeView(hScrollPanels());
            hScrollPanels.removeView(panels());

            for(int i = 0; i < list.size(); i++){
                panels().removeView(list.elementAt(i));
            }


            hScrollPanels.setX(x0);
            hScrollPanels.setY(y0);

     /*       for(ListView listView: buttons.getBuf()) {
                if (listView == null)
                    break;

            }*/

            int hPanel = 0;
            int px = 0;
            int allw = getAllWidth();
            panelsParams(allw, hPanel);
            if (getSize() == 0) {
                h = 0;
                return 0;
            }
            int wPanel = listW;
            for(int i = 0; i < list.size(); i++){
                int b = list.elementAt(i).freshLayout(px, 0, wPanel, panels, panelsParams);
                if (b > hPanel)
                    hPanel = b;
                px = px + wPanel + margin;
            }
            h = hPanel;
            hScrollPanels.addView(panels, panelsParams);
            //hScrollPanelsParams(w, hPanel);
            panel.addView(hScrollPanels, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
/*            int x = 0;
            for(int i = 0; i < list.size(); i++){
                ListView0 listView = list.elementAt(i);
                listView.setX(x);
                x += listW;
                listView.setY(listView.hButtons);
                RelativeLayout.LayoutParams layoutParams =  new RelativeLayout.LayoutParams(listW, 200);
                panels().addView(listView, layoutParams);
            }*/
            return hPanel;
        }

        int reLayout(RelativeLayout panel, int w, int x0, int y0) {
            this.w = w;
            x = x0;
            y = y0;
            if (getSize() == 0) {
                h = 0;
                return 0;
            }

            panel.removeView(hScrollPanels());
            hScrollPanels.removeView(panels());

            for(int i = 0; i < list.size(); i++){
                panels().removeView(list.elementAt(i));
            }


            hScrollPanels.setX(x0);
            hScrollPanels.setY(y0);

     /*       for(ListView listView: buttons.getBuf()) {
                if (listView == null)
                    break;

            }*/

            int hPanel = 0;
            int px = 0;
            int allw = getAllWidth();
            panelsParams(allw, hPanel);
            int wPanel = listW;
            for(int i = 0; i < list.size(); i++){
                if (list.elementAt(i) == null){
                    printError("list.elementAt(i) == null");
                    continue;
                }
                int b = list.elementAt(i).freshLayout(px, 0, wPanel, panels, panelsParams);
                if (b > hPanel)
                    hPanel = b;
                px = px + wPanel + margin;
            }
            h = hPanel;
            hScrollPanels.addView(panels, panelsParams);
            panel.addView(hScrollPanels, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            return hPanel;
        }

        public void closeAll() {

            while(list.size() > 0){
                if (list.lastElement() == null){
                    printError("list.lastElement() == null");
                    continue;
                }
                list.elementAt(list.size() - 1).adapter.close();
            }
        }


/*        int reLayout(RelativeLayout.LayoutParams paramsPanel, RelativeLayout panel, int w, int x0, int y0) {

            this.w = w;
            x = x0;
            y = y0;
            if (getSize() == 0) {
                h = 0;
                return 0;
            }
            int hPanel = 0;
            int px = 0;
            int allw = getSize();
            panelsParams(allw, hPanel);
            int wPanel = listW;
            for(int i = 0; i < list.size(); i++){
                int parity = list.elementAt(i).freshLayout(px, 0, wPanel, panels, panelsParams);
                if (parity > hPanel)
                    hPanel = parity;
                px = px + wPanel + margin;
            }
            h = hPanel;

            hScrollPanelsParams(w, hPanel);
            if (paramsPanel.height < hPanel + y0) {
                paramsPanel.height = hPanel + y0;
                panel.setLayoutParams(paramsPanel);
            }
            return hPanel;
        }
*/
    }


    ContactAdapter contactAdapter;

    public ContactAdapter contactAdapter(){
        if (contactAdapter == null){
            contactAdapter = new ContactAdapter();
        }
        return contactAdapter;
    }

    class ItemContact{
        String name;
        String contact;
        Uri uri;
        String id;

        public String toString(){
            return name + " " + contact;
        }

    }

    class ContactAdapter extends Adapter{
        HashMap<Item, Integer> selectedText = new HashMap();

        private void showVariables() {
            saveVariables();
            openTextFile0(new File(variablesFileName()), listView.index + 1);
        }

        @Override
        public String getName() {
            return "contactAdapter";
        }


        @Override
        public void init(int index){

            super.init(index);
            mainReLayout();
        }

        @Override
        void refresh(){
            findContacts();
            refresh0();
        }
        void refresh0(){
            Collections.sort(allContacts.emails, comparator);
            Collections.sort(allContacts.phones, comparator);
            ArrayList list;
            if (isPhones())
                list = allContacts.phones;
            else
                list = allContacts.emails;
            this.setAll(list);
            mainReLayout();
        }


        boolean isPhones(){
            return isPhones;
        }

        boolean isPhones = true;


        ShellButton showEmails;
        void switchButtons(){
            if (isPhones) {
                showEmails.button().setText("To e-mail list");
                showEmails.button().invalidate();
            }else{
                showEmails.button().setText("To phone list");
                showEmails.button().invalidate();
            }

        }
        private void switchList() {
            isPhones = !isPhones;
            switchButtons();
            if (isPhones) {
                this.setAll(allContacts.phones);
            }else{
                this.setAll(allContacts.emails);
            }
            mainReLayout();
        }

        public void addButtons() {

            new ShellButton("1",
                    () -> pressChar("1"),
                    cGreen,
                    bGreen, 0, buttonPanel);

            new ShellButton("2",
                    () -> pressChar("2"),
                    cGreen1,
                    bGreen1, 0, buttonPanel);

            new ShellButton("3",
                    () -> pressChar("3"),
                    cGreen,
                    bGreen, 0, buttonPanel);
            new ShellButton(Html.fromHtml("<big><b>&larr;</b></big>"),
                    FreeShell.this::left,
                    cYellow1,
                    bYellow1, 0, buttonPanel);
            new ShellButton(Html.fromHtml("<big><b>&rarr;</b></big>"),
                    FreeShell.this::right,
                    cYellow1,
                    bYellow1, 0, buttonPanel);
            new ShellButton("4",
                    () -> pressChar("4"),
                    cGreen1,
                    bGreen1, 1, buttonPanel);

            new ShellButton("5",
                    () -> pressChar("5"),
                    cGreen,
                    bGreen, 1, buttonPanel);

            new ShellButton("6",
                    () -> pressChar("6"),
                    cGreen1,
                    bGreen1, 1, buttonPanel);


            new ShellButton("7",
                    () -> pressChar("7"),
                    cGreen,
                    bGreen, 2, buttonPanel);

            new ShellButton("8",
                    () -> pressChar("8"),
                    cGreen1,
                    bGreen1, 2, buttonPanel);

            new ShellButton("9",
                    () -> pressChar("9"),
                    cGreen,
                    bGreen, 2, buttonPanel);


            new ShellButton("*",
                    () -> pressChar("*"),
                    cGreen1,
                    bGreen1, 3, buttonPanel);

            new ShellButton("0",
                    () -> pressChar("0"),
                    cGreen1,
                    bGreen1, 3, buttonPanel);

            new ShellButton("#",
                    () -> pressChar("#"),
                    cGreen1,
                    bGreen1, 3, buttonPanel);


            new ShellButton("-",
                    () -> pressChar("-"),
                    cGreen1,
                    bGreen1, 1,  buttonPanel);
            new ShellButton("+",
                    () -> pressChar("+"),
                    cGreen,
                    bGreen, 1,  buttonPanel);
            new ShellButton("\u00B0",
                    () -> pressChar("\u00B0"),
                    cGreen1,
                    bGreen1, 1, buttonPanel);


            new ShellButton("(",
                    () -> pressChar("("),
                    cGreen1,
                    bGreen1, 2, buttonPanel);
            new ShellButton(")",
                    () -> pressChar(")"),
                    cGreen,
                    bGreen, 2, buttonPanel);

            new ShellButton(".",
                    () -> pressChar("."),
                    cGreen,
                    bGreen, 3, buttonPanel);
            new ShellButton(Html.fromHtml("<big>=</big>"),
                    () -> pressChar("="),
                    cGreen,
                    bGreen, 3, buttonPanel);

            new ShellButton("^",
                    () -> pressChar("^"),
                    cGreen,
                    bGreen, 3, buttonPanel);
            new ShellButton("\u221A",
                    () -> pressChar("\u221A("),
                    cGreen1,
                    bGreen1, 3, buttonPanel);

            new ShellButton("Show variables",
                    this::showVariables,
                    cGreen,
                    bGreen, 3, buttonPanel);
            new ShellButton("Clear variables",
                    FreeShell.this::forgetVariables,
                    cRed,
                    bRed, 3, buttonPanel);


            new ShellButton("/",
                    () -> pressChar("/"),
                    cGreen,
                    bGreen, 0, buttonPanel);

            new ShellButton("Backspace",
                    FreeShell.this::del,
                    cOrange,
                    bOrange, 0, buttonPanel);

            postCreate();
            new ShellButton(Html.fromHtml("<big>Call</big>"),
                    FreeShell.this::call,
                    cRose, bRose, 4, buttonPanel);

            showEmails = new ShellButton(isPhones?"To e-mail list":"To phone list",
                    this::switchList,
                    cBlue, bSel1, 4, buttonPanel);
            new ShellButton(Html.fromHtml("Eval line"),
                    FreeShell.this::eval,
                    cGreen,
                    bGreen, 4, buttonPanel);

            new ShellButton(" ",
                    () -> pressChar(" "),
                    cGreen1,
                    bGreen1, 4, buttonPanel);
            new ShellButton("\u21A9",
                    () -> pressChar("\n"),
                    cOrange,
                    bOrange, 4, buttonPanel);

            create0(4, 4);



            new ShellButton(Html.fromHtml("sin"),
                    FreeShell.this::sin,
                    cCyan,
                    bCyan, 1, buttonPanel);
            new ShellButton(Html.fromHtml("cos"),
                    FreeShell.this::cos,
                    cCyan,
                    bCyan, 1, buttonPanel);
            new ShellButton(Html.fromHtml("tan"),
                    FreeShell.this::tg,
                    cCyan,
                    bCyan, 1, buttonPanel);


            new ShellButton("to\u00B0",
                    FreeShell.this::degree,
                    cYellow1,
                    bYellow1, 2, buttonPanel);


            new ShellButton("pi",
                    FreeShell.this::pi,
                    cGreen1,
                    bGreen1, 2, buttonPanel);




            new ShellButton("e",
                    FreeShell.this::e,
                    cGreen,
                    bGreen, 2, buttonPanel);
            new ShellButton("c",
                    FreeShell.this::c,
                    cYellow,
                    bYellow, 2, buttonPanel);

            bRound2 = new ShellButton(Html.fromHtml("Round2"),
                    FreeShell.this::round2,
                    round == 2, 1, buttonPanel);
            bRound20 = new ShellButton(Html.fromHtml("Round20"),
                    FreeShell.this::round10,
                    round == 20, 2, buttonPanel);

/*            new ShellButton("help",
                    FreeShell.this::contactHelp,
                    cYellow,
                    bYellow, 4, buttonPanel);*/

            //buttons.add(del);
        }

        public ContactAdapter() {
            addButtons();
            findContacts();
        }


        @Override
        void showFirst() {
            init(0);
        }

        @Override
        void close(RelativeLayout panel){
            clearGlobal();

            super.close(panel);
        }

        public void clearGlobal() {
            contactAdapter = null;
        }
        Random r01 = new Random();
        @Override
        public Item getView(int position, int filteredPosition) {
            Item item = new Item(position){
                @Override
                boolean isSelect(){
                    return selectedText.containsKey(this);
                }

                @Override
                void remove(){
                    selectedText.remove(this);
                    super.remove();
                }

            };
            if (allItems.size() <= position){
                return null;
            }
            if (current.size() <= filteredPosition){
                return null;
            }
            ItemContact itemContact = (ItemContact)allItems.get(position);
            Uri mSelectedContactUri = itemContact.uri;
            String s = current.get(filteredPosition).toString();

            View.OnClickListener open = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        String phone = itemContact.contact;
                        phone = phone.replace(' ','-');
                        pressChar(" " + phone + " ");
                        FreeShell.this.text().requestFocus();
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }
                }
            };
            View.OnClickListener select = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if (!selectedText.containsKey(item)){
                            selectedText.put(item, position);
                        }else{
                            selectedText.remove(item);
                        }
                        item.format();
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }
                }
            };
            View.OnClickListener settings;
            if (mSelectedContactUri !=null) {
                settings = new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        try{
                            Intent editIntent = new Intent(Intent.ACTION_EDIT);
                            /*
                             * Sets the contact URI to edit, and the data type that the
                             * Intent must match
                             */
                            editIntent.setDataAndType(mSelectedContactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                            FreeShell.this.startActivityForResult(editIntent, 262144);
                            refresh();
                            //listView.invalidateViews();
                        }catch (Exception e){
                            FreeShell.this.printError(e);
                        }

                    }
                };
            }else{
                settings = null;
            }

            Runnable loadIcon = new Runnable() {
                String id = itemContact.id;
                @Override
                public void run() {
                    try {

                        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));

                        if (inputStream != null) {
                            Bitmap b = BitmapFactory.decodeStream(inputStream);
                            BitmapDrawable photo = new BitmapDrawable(getResources(), b);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    item.setIcon(photo);
                                }
                            });
                            try {
                                inputStream.close();
                            }catch (Throwable t){

                            }

                        }

                    } catch (Throwable e) {
                    }

                }
            };


            if (b) {
                item.init(r01.nextBoolean()?'☺':'☻', this, null, listW, open, settings, select, loadIcon, null);
                item.setNormalColors(cGreen, bGreen);
            }else{
                item.init(r01.nextBoolean()?'☻':'☺', this, null, listW, open, settings, select, loadIcon, null);
                item.setNormalColors(cGreen1, bGreen1);
            }
            item.textView.setText(s);
            b = !b;
            return item;
        }

        @Override
        public void save(PrintWriter writer){
            super.save(writer);
            writer.println("isPhones=" + isPhones);
        }

        @Override
        public boolean load(Tag tag){
            isPhones = Boolean.parseBoolean("#isPhones");

            return super.load(tag);
        }


        @Override
        public void start(Tag tag) {
            switchButtons();
        }

        /*int freshLayout(int x, int y, int w, int h, RelativeLayout panel){

            int wb = (w / buttons.size()) - margin;
            for(ShellButton ShellButton : buttons){

                panel.removeView(ShellButton.view);
                ShellButton.view.setX(x);
                ShellButton.view.setY(0);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(wb, hb);
                panel.addView(ShellButton.view, params);
                x = incX(x, wb);
            }
            return hb + margin;
        }*/
    }

    private void enter() {
        pressChar("\n");
    }

    private void sin() {
        pressChar("sin(");
    }
    private void cos() {
        pressChar("cos(");
    }
    private void tg() {
        pressChar("tan(");
    }
    private void pi() {
        pressChar("pi");
    }
    private void e() {
        pressChar("e");
    }
    private void c() {
        pressChar("c");
    }


    private void rad() {
        pressChar("toRadians(");
    }
    private void degree() {
        pressChar("\u00B0(");
    }

    ShellButton bRound20;
    ShellButton bRound2;
    MathContext mRound2 = new MathContext(2, RoundingMode.HALF_UP);;
    MathContext mRound20 = new MathContext(20, RoundingMode.HALF_UP);
    MathContext mRound = mRound2;
    int round = 2;
    private void round2() {
        bRound2.setFlag(true);
        bRound20.setFlag(false);
        round = 2;
        mRound = mRound2;
        eval();
    }
    private void round10() {
        round = 20;
        mRound = mRound20;
        bRound2.setFlag(false);
        bRound20.setFlag(true);
        eval();
    }

    private void forgetVariables() {
        variables = newVariables();
        saveVariables();
    }



    FirstScreen firstScreen;
    FirstScreen firstScreen(){
        if (firstScreen == null){
            firstScreen = new FirstScreen();
        }
        return firstScreen;
    }

    ListPanel listPanel;
    ListPanel listPanel(){
        if (listPanel == null){
            listPanel = new ListPanel();
        }
        return listPanel;
    }

    int listW;
   // void mainReLayout(int scrollX, int scrollY, Adapter adapter, int pos) {

        /*if (scrollY < getDisplayHeight() - hb){

            scrollY = vScroll.getScrollY();
        }*/
        //vScroll.scrollTo(scrollX, scrollY);

     //   int x = scrollX, y = scrollY;
//        mainReLayout();
  /*      myLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                vScroll.scrollTo(x, y);
                listPanel.hScrollPanels.scrollTo(x, y);
            }
        }, 1000);*/
   /*     int ih;
        if (adapter.getCount() > 0) {
            ih = adapter.getView(pos).h();
        }else
            ih = 0;
        int oldScrollY = vScroll.getScrollY();
        if (oldScrollY < scrollY && oldScrollY + getDisplayHeight() > scrollY + ih) {
            return;
        }

        int h =  paramsPanel.height;


        if (h - scrollY < getDisplayHeight()){

            scrollY = h - getDisplayHeight();
        }

        if (scrollY < 0)
            scrollY = 0;
        final int y = scrollY;
        vScroll.postDelayed(new Runnable() {
            @Override
            public void run() {
                        vScroll.scrollTo(scrollX, y);
                        vScroll.invalidate();
            }
        }, 300);

        //new Thread().start();

    }*/
   boolean loaded = false;
   void mainReLayout() {
    if (!loaded)
        return;
     /*   int x0 = vScroll().getScrollX();;
        int y0 = vScroll.getScrollY();;*/
        vScroll().removeView(myLayout());
        //myLayout();
//        paramsPanel.width = superWidth;


        int w = displayMetrics.widthPixels;
        int y = firstScreen().freshLayout(myLayout, w, 0, 0);

        //int scrollY = vScroll.getScrollY();
        //hPanel += scrollY;

        listPanel().freshLayout(myLayout, w, 0, y);
       // myLayout.setLayoutParams(paramsPanel);
       int h0 = listPanel.panelsParams.height;
       int y0 = listPanel().y;
        int h = y0 + h0 + hb + hb;
                //listPanel.panelsParams(displayMetrics.widthPixels, displayMetrics.heightPixels).height + hb + hb;
        if (h < max)
            h = max;
        vScroll.addView(myLayout, RelativeLayout.LayoutParams.MATCH_PARENT, h/*paramsPanel*/);
       // vScroll.scrollTo(x0, y0);
        myLayout().invalidate();
    }


    //InterstitialAd mInterstitialAd;
    Integer orientation;
    DisplayMetrics displayMetrics;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        printLog("onCreate");
        try{
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            super.onCreate(savedInstanceState);
            orientation = getResources().getConfiguration().orientation;
            checkPermissions();

        }catch (Throwable e){
            freeShell.printError(e);
        }

        /*text();
        listPanel().showFirst();*/
        //        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 50);

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void bindAudioService() {
        if (audioServiceBinder == null) {
            if (!isMyServiceRunning(AudioService.class)){
                Intent intent = new Intent(FreeShell.this, AudioService.class);
                startService(intent);
            }else
                isConnectingToExistingService = true;
            Intent intent = new Intent(FreeShell.this, AudioService.class);
            // Below code will invoke serviceConnection's onServiceConnected method.
            bindService(intent, serviceConnection, Context.BIND_ADJUST_WITH_ACTIVITY);

        }
    }
     Runnable audioAction;
    private  AudioServiceBinder audioServiceBinder = null;
    // This service connection object is the bridge between activity and background service.
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // Cast and assign background service's onBind method returned iBander object.
            audioServiceBinder = (AudioServiceBinder) iBinder;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (audioAction != null) {
                        audioAction.run();
                        audioAction = null;
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (camera == null)
            return;
        try {
            camera.setPreviewDisplay(previewHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            Camera.Parameters cameraParameters = camera.getParameters();
            //set color efects to none
            cameraParameters.setColorEffect(Camera.Parameters.EFFECT_MONO);

            //set antibanding to none
            if (cameraParameters.getAntibanding() != null) {
                cameraParameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
            }

            // set white ballance
            if (cameraParameters.getWhiteBalance() != null) {
                cameraParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            }

            //cameraParameters.setExposureCompensation(Camera.Parameters.EX);


            //set flash
/*            if (cameraParameters.getFlashMode() != null) {
                cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }*/

            //set zoom
            if (cameraParameters.isZoomSupported()) {
                cameraParameters.setZoom(0);
            }


            //set focus mode
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            camera.setParameters(cameraParameters);

            //takePhoto();
        } catch (Throwable e) {
        }
    }

    String getOnlyTime(){
        Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
        String s = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(now);
        s = s.replace(":", "h");
        return s + "m";
    }

    String getOnlyDate(){
        Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
        return new SimpleDateFormat("yyyy MMM dd EEE", Locale.ENGLISH).format(now);
    }


    String getTime(){
        return getOnlyDate() + " " + getOnlyTime();
    }
    void deleteEmptySubfolders(String sir,  File curDir){
        try {
            File f = new File(sir);
            if (f.exists()) {
                File[] files = f.listFiles();
                boolean b = false;

                for(File dir: files){
                    File[] d = dir.listFiles();
                    if (!notEmpty(d)) {
                        b = true;
                        if (!dir.equals(curDir))
                            dir.delete();
                    }
                }
                if (b){
                    refreshDirs(sir);
                }
            }
        }catch (Throwable t){
            error(t);
        }

    }



    File newVideoDir(){
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        File CameraDirectory = new File(dir);
        String s = getOnlyDate();
        File newfile = new File(CameraDirectory.getAbsolutePath() + "/" + s);

        videoDir = newfile;
        if (!newfile.exists())
            newfile.mkdirs();
        deleteEmptySubfolders(dir, videoDir);
        return videoDir;
    }

    File videoDir;

    private void recVideo() {
        File dir = newVideoDir();
        File f = getUniqueName(dir.getAbsolutePath() + "/" + getOnlyTime(), "mp4");
        final Uri uri = Uri.fromFile(f);

        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        //cameraIntent.addCategory(Intent.CATEGORY_DEFAULT);
        cameraIntent.putExtra(MediaStore.EXTRA_SHOW_ACTION_ICONS, true);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        photo = dir;
        numberPhoto = 0;
        File[] l =  photo.listFiles();
        if (notEmpty(l)){
            numberPhoto  = l.length;
        }

        startActivityForResult(cameraIntent, 262144);

    }

    File photo = null;
    int numberPhoto = 0;
    void takePhotoFirst() {
        File dir = newVideoDir();
        File f = getUniqueName(dir.getAbsolutePath() + "/" + getOnlyTime(), "jpg");
        final Uri uri = Uri.fromFile(f);

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        //cameraIntent.addCategory(Intent.CATEGORY_DEFAULT);
        cameraIntent.putExtra(MediaStore.EXTRA_SHOW_ACTION_ICONS, true);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        photo = dir;
        numberPhoto = 0;
        File[] l =  photo.listFiles();
        if (notEmpty(l)){
            numberPhoto  = l.length;
        }
        //startActivityForResultForResult(cameraIntent, PHOTO);
        startActivityForResult(cameraIntent, 262144);


    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        printLog("surfaceChanged");
        if (camera == null)
            return;

        try {
                    //Log.i(TAG, "starting preview: " + started);
                    // ....
                    int rotation = getWindowManager().getDefaultDisplay().getRotation();
                    int degrees = 0;
                    switch (rotation) {
                        case Surface.ROTATION_0:
                            degrees = 0;
                            break;
                        // Natural orientation
                        case Surface.ROTATION_90:
                            degrees = 90;
                            break;
                        // Landscape left
                        case Surface.ROTATION_180:
                            degrees = 180;
                            break;// Upside down
                        case Surface.ROTATION_270:
                            degrees = 270;
                            break;// Landscape right
                    }
                    this.camera.setDisplayOrientation(degrees + 90);

            camera.startPreview();
            camera.autoFocus(null);
        }catch (Throwable t){
            error(t);
        }

    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
        printLog("surfaceDestroy");
    if (camera == null)
        return;
        try{
            camera.stopPreview();

        }catch (Throwable e){

        }
        try {
            camera.release();
        }catch (Throwable e){

        }
            camera = null;

    }


    void printMessage(String message) {
        okDialog("Information ", message);
        log(message);

    }
/*
Активировать вибратор:

$ echo 100 > /sys/devices/virtual/timed_output/vibrator/enable

Включить фонарик:

$ echo 1 > /sys/devices/platform/flashlight/leds/flashlight/brightness


 */
    void printBelow(String message){
        FreeShell.this.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Editable edit = text().getText();
                        int end = text().getSelectionEnd();
                        char c;
                        while (end < text.length()) {
                            c = edit.charAt(end);
                            if (c == '\n') {
                                break;
                            }
                            end++;
                        }
                        String sp = message;

                        if (end < text.length())
                            text().getText().insert(end, sp + '\n');
                        else
                            text().getText().insert(end, '\n' + sp);
                        text.setSelection(text().getText().length(), text().getText().length());
                        text().invalidate();
                    };
                }
        );
    }

    private void printUpperMessage0(String message) {

        Editable edit = text().getText();
        int start = text().getSelectionStart();
        char c;
        while(start > 0) {
            start--;
            c = edit.charAt(start);
            if (c == '\n') {
                break;
            }
        }
        String sp = message;

        if (start > 0)
            text().getText().insert(start, '\n' + sp);
        else
            text().getText().insert(start, sp + '\n');
        text().invalidate();
    }
    void printUpperError(Exception e) {
        error(e);
        printUpperError(e.getMessage());
    }
    void printUpperError(String message) {
        FreeShell.this.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        printUpperMessage0(message);
                    }

                });
    }
    String formatErrorMessage(String message){

        /*static int cOrange = Color.argb(240, 48, 24, 0);
        static int bOrange = Color.argb(96, 255, 192, 0);
        static int cGreen = Color.argb(240, 0, 16, 0);
        static int bGreen = Color.argb(96, 128, 255, 128);
        static int cRed = Color.argb(240, 32, 0, 0);
        static int bRed = Color.argb(96, 255, 64, 64);*/

        return "<font color=red>Error </font><font color=#ffaf00>" + message + "</font>";
    }
    String formatMessage(String message){
        return "<font color=green>" + message + "</font>";
    }

/*    void printMessage(String message, String title) {
        text().getText().append(title + ":" + message);

       new ShellButton(message, () -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(FreeShell.this);
            builder.setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setNegativeButton("OK",
                            (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        },
                cGray,
                bGray);
        mainReLayout();
    }*/

    void openPhotos() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 262144);
    }

    class AllContacts{
        ArrayList phones;
        ArrayList emails;
    }

    AllContacts allContacts = new AllContacts();

    void findContacts() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final AllContacts allContacts = findContacts0();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FreeShell.this.allContacts = allContacts;
                        if (contactAdapter != null)
                            contactAdapter.refresh0();
                    }
                });
            }
        }).start();
    }
/*    String[] names = {"Mikhail Kravchenko", "Ivan Kravchenko", "Stepan Kravchenko", "Alexey Kravchenko"};
    String[] phones = {"+380 67 385 36 87", "+380-63-485-3687", "+380673863687",  "*111#"};
*/
    AllContacts findContacts0(){
        AllContacts ret = new AllContacts();
        ret.phones = new ArrayList<>();
        ret.emails = new ArrayList<>();
  /*      for(int i = 0; i < names.length; i++) {
            ItemContact itemContact = new ItemContact();
            itemContact.id = i + "";
            itemContact.name = names[i];
            itemContact.uri = null;
            itemContact.contact = phones[i];
            ret.phones.add(itemContact);
        }*/

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        // String name, number = "";
        int count = cur.getCount();
        cur.moveToFirst();

        if (count > 0) {
            do{
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Uri mSelectedContactUri = null;
                try {
                    int mLookupKeyIndex = cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
                    // Gets the lookup key value
                    String mCurrentLookupKey = cur.getString(mLookupKeyIndex);
                    mSelectedContactUri =
                            ContactsContract.Contacts.getLookupUri(Long.parseLong(id), mCurrentLookupKey);
                } catch (Throwable t) {

                }
                boolean add = false;
                ArrayList<String> phones = getPhoneNumbers(cr, id);
                for(String phone: phones) {
                    ItemContact itemContact = new ItemContact();
                    itemContact.id = id;
                    itemContact.name = name;
                    itemContact.uri = mSelectedContactUri;
                    itemContact.contact = phone;
                    ret.phones.add(itemContact);
                    add = true;
                }
                ArrayList<String> emails = getEmailAddresses(cr, id);
                for(String email: emails) {
                    ItemContact itemContact = new ItemContact();
                    itemContact.id = id;
                    itemContact.name = name;
                    itemContact.uri = mSelectedContactUri;
                    itemContact.contact = email;
                    ret.emails.add(itemContact);
                    add = true;
                }
                if (!add){
                    ItemContact itemContact = new ItemContact();
                    itemContact.id = id;
                    itemContact.name = name;
                    itemContact.uri = mSelectedContactUri;
                    itemContact.contact = "empty";
                    ret.emails.add(itemContact);
                    ret.phones.add(itemContact);

                }


            }while (cur.moveToNext());
        }
        return ret;


    }

    public ArrayList<String> getPhoneNumbers(ContentResolver cr, String id) {
        ArrayList<String> phones = new ArrayList<String>();

        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                new String[]{id}, null);
        while (pCur.moveToNext()) {
            String s = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phones.add(s);
        }
        pCur.close();
        return(phones);
    }

    public ArrayList<String> getEmailAddresses(ContentResolver cr, String id) {
        ArrayList<String> emails = new ArrayList<String>();

        Cursor emailCur = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{id}, null);
        while (emailCur.moveToNext()) {
            // This would allow you get several email addresses
            String s = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            emails.add(s);
        }
        emailCur.close();
        return (emails);
    }

/*    ArrayList findContacts(ContactAdapter contactAdapter){
        ArrayList<ContactAdapter.ItemContact> ret = new ArrayList<>();

        Cursor c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
       // String name, number = "";
        int count = c.getCount();
        c.moveToFirst();

        for (int i = 0; i < count; i++) {
            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

            {
                Cursor pCur = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id},
                        null);
                Uri mSelectedContactUri = null;
                try {
                    int mLookupKeyIndex = c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
                    // Gets the lookup key value
                    String mCurrentLookupKey = c.getString(mLookupKeyIndex);
                    mSelectedContactUri =
                            ContactsContract.Contacts.getLookupUri(Long.parseLong(id), mCurrentLookupKey);
                }catch (Throwable t){

                }

                while (pCur.moveToNext()) {

                    //String number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));

                        String number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String email = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));




                    ContactAdapter.ItemContact itemContact = contactAdapter.newItemContact();
                        itemContact.id = id;
                        itemContact.name = name;
                        itemContact.email = email;
                        itemContact.phone = number;
                        itemContact.uri = mSelectedContactUri;
                        ret.add(itemContact);
                    //contacts.add(name, email, photo, mSelectedContactUri);


                }
            }
            c.moveToNext();
        }
        return ret;
    }*/

    HashMap<String, Intent> starts = new HashMap<>();


    Intent getIntentByExt(String ext) {
        if ((ext == null) || (ext.length() == 0)) {
            return null;
        }
        if (!starts.containsKey(ext)) {
            MimeTypeMap myMime = MimeTypeMap.getSingleton();

            String mimeType = myMime.getMimeTypeFromExtension(ext);
            if ((mimeType != null) && (mimeType.length() > 0)) {
                Intent newIntent = new Intent(Intent.ACTION_VIEW);

                newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                newIntent.setDataAndType(null, mimeType);
                starts.put(ext, newIntent);
            } else {
                starts.put(ext, null);
            }
        }
        return starts.get(ext);


    }

    //calculate expresson!!!

    public static String getFileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        int index = url.lastIndexOf(".");
        if (index < 0) {
            return "";
        } else {
            String ext = url.substring(index + 1);
            if (ext.length() > 0) {
                if (ext.indexOf("%") > -1) {
                    ext = ext.substring(0, ext.indexOf("%"));
                }
                if (ext.indexOf("/") > -1) {
                    ext = ext.substring(0, ext.indexOf("/"));
                }
            }
            return ext.toLowerCase();

        }
    }

    public static String getFileName(String url) {
        int index = url.lastIndexOf(".");
        if (index < 0) {
            return url;
        } else {
            return url.substring(0, index);
        }
    }




/*
    LinearLayout row;
    LinearLayout mainLayout;

    public LinearLayout mainLayout() {

        if (mainLayout == null) {
            mainLayout = new LinearLayout(this);
        }
        return mainLayout;
/*
            for (int i = 0; i < 3; i++) {
                LinearLayout row = new LinearLayout(this);
                row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                for (int j = 0; j < 4; j++) {
                    ShellButton btnTag = new ShellButton(this);
                    btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    btnTag.setText("ShellButton " + (j + 1 + (i * 4)));
                    btnTag.setId(j + 1 + (i * 4));
                    row.addView(btnTag);
                    btnTag.getRight();
                }

                layout.addView(row);
                setContentView(myLayout);
            }
        }
        return myLayout;*/

//
    //messages().relativeLayout.addView(action.view);


    private void del() {
        int start = text().getSelectionStart();
        int end = text().getSelectionEnd();

        if (start == 0){
            if (end == 0)
                return;
        }else{
            if (end == start)
                start--;
        }
        text().getText().delete(start, end);
        text.invalidate();
    }
    private void del2() {
        int start = text().getSelectionStart();
        int end = text().getSelectionEnd();

        if (end == text().length()){
            if (start == text().length())
                return;
        }else{
            if (end == start) {
                end++;
            }
        }
        text().getText().delete(start, end);
        text.invalidate();
    }

    void pressChar(String c){
        int start = text().getSelectionStart();
        int end = text().getSelectionEnd();
        text().getText().replace(start, end, c);
        text.invalidate();
    }
    void left(){
        int end = text().getSelectionEnd();
        int start = text().getSelectionStart();
        if (start > 0) {
            if (start == end)
                end--;
            start--;
        }
        text.setSelection(start, end);

    }


    void right(){
        int end = text().getSelectionEnd();
        int start = text().getSelectionStart();
        if (start != end){
            if (end < text().length()) {
                end++;
                text.setSelection(start, end);
                return;
            }
        }else{
            if (start < text().length()) {
                if (start == end)
                    end++;
                start++;
                text.setSelection(start, end);
            }
        }
    }


    String getWholeText(){

        return firstScreen().text().getText().toString().trim();

    }
    String getLine(){
        Editable edit = text().getText();
        int start = text().getSelectionStart();
        int end = text().getSelectionEnd();
        char c;
        while(start > 0) {
            start--;
            c = edit.charAt(start);
            if (c == '\n')
                break;
        }
        while(end < edit.length()) {
            c = edit.charAt(end);
            if (c == '\n')
                break;
            end++;
        }

        return edit.subSequence(start, end).toString().trim();
    }
    void nextLine(){

        Editable edit = text().getText();
        int start = text().getSelectionStart();
        char c;
        while(start < edit.length()) {
            c = edit.charAt(start);
            if (c == '\n') {
                start++;
                break;
            }
            start++;
        }
        text().setSelection(start, start);
        if (start == edit.length()){
            if (start > 0){
                if (edit.charAt(start-1) != '\n' ){
                    pressChar("\n");
                }
            }
        }
        text.invalidate();

    }
    void appendString(String s){

        Editable edit = text().getText();
        int start = text().getSelectionStart();
        char c;
        while(start < edit.length()) {
            c = edit.charAt(start);
            if (c == '\n') {
                break;
            }
            start++;
        }
        text().getText().replace(start, start, s);
        text.invalidate();
    }

    void removeLine(){

        Editable edit = text().getText();
        int start = text().getSelectionStart();
        int end = text().getSelectionEnd();
        char c;
        while(start > 0) {
            start--;
            c = edit.charAt(start);
            if (c == '\n')
                break;
        }
        while(end < edit.length()) {
            c = edit.charAt(end);
            if (c == '\n')
                break;
            end++;
        }
        text().getText().delete(start, end);
        text.invalidate();
    }

    void addString(String s){

        Editable edit = text().getText();
        int start = text().getSelectionStart();
        char c;
        while(start > 0) {
            if (start < edit.length()) {
                c = edit.charAt(start);
                if (c == '\n') {
                    start++;
                    break;
                }
            }
            start--;
        }
        text().getText().replace(start, start, s);
        text.invalidate();

    }


    void appendLine(CharSequence s){

        Editable edit = text().getText();
        int start = text().getSelectionStart();
        char c;
        while(start < edit.length()) {
            c = edit.charAt(start);
            if (c == '\n') {
                start++;
                break;
            }
            start++;
        }

        text().setSelection(start, start);
        if (start == edit.length()){
            if (start > 0){

                if (edit.charAt(start-1) != '\n' ){
                    pressChar("\n");
                    start++;
                }
            }
        }

        if (start < text().getText().length())
            text().getText().replace(start, start, s + "\n");
        else
            text().getText().replace(start, start, s);
        text.invalidate();

    }


    String getWord(){
        Editable edit = text().getText();
        int start = text().getSelectionStart();
        int end = text().getSelectionEnd();
        char c;
        while(start > 0) {
            start--;
            c = edit.charAt(start);
            if ((c == ' ')||(c == '\n'))
                break;
        }
        while(end < edit.length()) {
            c = edit.charAt(end);
            if ((c == ' ')||(c == '\n'))
                break;
            end++;
        }

        String s =  edit.subSequence(start, end).toString().trim();
        if (notEmpty(s))
            return s;
        s = getLine();
        if (notEmpty(s)) {
            String[] ss = s.split(" ");
            if (notEmpty(ss)) {
                for (int i = ss.length - 1; i >= 0; i--){
                    s = ss[i].trim();
                    if (notEmpty(s)){
                        return s;
                    }
                }
            }
        }
        return "";
    }

    String getLastLine(){
        String s = text().getText().toString();
        int i =  s.lastIndexOf("\n");
        if (i == -1) {
            return s.trim();
        }
        return s.substring(i).trim();
    }


    public FileAdapter fileAdapter(){
        return new FileAdapter();
    }

    void fs() {
        FileAdapter currentFiles = fileAdapter();
        currentFiles.showFirst();
        mainReLayout();
        myLayout().invalidate();
    }


    void menu() {

    }

    void app() {
        appAdapter().show();
        mainReLayout();
        myLayout().invalidate();
    }

    void contacts() {
       contactAdapter().show();
       mainReLayout();
       myLayout().invalidate();
    }

    void extendedMenu() {

    }

    void mc() {

    }

    void rec() {

    }

    public static String readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString("UTF-8");
    }


    public void closeSilently(Object... xs) {
        // Note: on Android API levels prior to 19 Socket does not implement Closeable
        for (Object x : xs) {
            if (x != null) {
                try {
                    if (x instanceof Closeable) {
                        ((Closeable) x).close();
                    } else if (x instanceof Socket) {
                        ((Socket) x).close();
                    } else if (x instanceof DatagramSocket) {
                        ((DatagramSocket) x).close();
                    } else {
                        throw new RuntimeException("cannot close " + x);
                    }
                } catch (Throwable e) {
                    error(e);
                }
            }
        }
    }


    int getDisplayHeight(){
        Rect r = new Rect();
        Window w = getWindow();
        w.getDecorView().getWindowVisibleDisplayFrame(r);

        return displayMetrics.heightPixels - r.top;
    }


    EditText text(){
        return firstScreen().text();
    }

    void mp3(){

        mp3Adapter().show();
        myLayout().invalidate();
    }



    Mp3Adapter mp3Adapter;

    public Mp3Adapter mp3Adapter(){
        if (mp3Adapter == null){
            mp3Adapter = new Mp3Adapter();
        }
        return mp3Adapter;
    }
    class Mp3Adapter extends FileAdapter{

        ShellButton prevFile, prev, pause, next, nextFile, random, repeat;



        TextView durationInfo = new TextView(FreeShell.this);
        TextView timer = new TextView(FreeShell.this);
        TextView info = new TextView(FreeShell.this);
        int timerWidth;

//        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

        int skipedSeek = -1;


        SeekBar seekBar = newSeekBar();

        SeekBar newSeekBar(){
            SeekBar seekBar = new SeekBar(FreeShell.this);
            seekBar.setMax(listW);
            return  seekBar;
        }
        void checkDir(){
            if (audioServiceBinder != null){
                File f = audioServiceBinder.getFile();
                if (f != null){
                    if (f.exists()){
                        File dir = getCurrentDir();
                        File parent = f.getParentFile();
                        if (!f.getParentFile().equals(dir)){
                            openSomeDir(parent);
                        }
                    }
                }
            }

        }

        @Override
        void show(){
            super.show();
            checkDir();
        }
        class ButtonPanel3 extends ButtonPanel2 {
            int freshLayout(RelativeLayout panel, int w, int x0, int y0, int yOffset) {
                int h = super.freshLayout(panel, w, x0, y0, yOffset);

                x0 = 0;
                y0 = h;
                durationInfo.setTextAppearance(FreeShell.this, R.style.fontButton);
                durationInfo.setBackgroundColor(Color.TRANSPARENT);
                durationInfo.setTextColor(cBlack);
                timer.setTextAppearance(FreeShell.this, R.style.fontButton);
                timer.setBackgroundColor(Color.TRANSPARENT);
                timer.setTextColor(cBlack);
  //              timer.setTextColor(cGray);
                info.setTextAppearance(FreeShell.this, R.style.fontButton);
                info.setBackgroundColor(Color.TRANSPARENT);
                info.setTextColor(cBlack);
    //            info.setTextColor(cGray);

                timer.setText("00:00:00");
                timer.measure(
                        makeMeasureSpec,
                        makeMeasureSpec);

                int dw = timer.getMeasuredWidth();
                timerWidth = dw;
                int ht = timer.getMeasuredHeight();
                timer.setX(0);
                timer.setY(y0 + ht);


                this.hPanel.addView(timer, dw, ht);
                //int x = x0 + dw;
                durationInfo.setText("00:00:00");
                durationInfo.setX(0);
                durationInfo.setY(y0);
                this.hPanel.addView(durationInfo, dw, ht);



                info.setX(dw);
                info.setY(y0);

                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(right, ht);
                info.setScrollbarFadingEnabled(true);
                this.hPanel.addView(info, layout);

                y0 += ht;
                skipedSeek = 0;
                seekBar.setX(x0);

                seekBar.setY(y0);
                seekBar.setBackgroundColor(Color.TRANSPARENT);
      //          seekBar.setDrawingCacheBackgroundColor(cMangeta);
//                skipedSeek = 100;



                this.hPanel.addView(seekBar, w, hb);
                h = h + ht + hb;
                hButtons = h;

                if (params.height < h) {
                    panel.removeView(hScroll);
                    hScroll.removeView(hPanel);
                    params.height = h;
                    hscrollParams.height = h;
                    hScroll.addView(hPanel, params);
                    panel.addView(hScroll, hscrollParams);
                }


                return h;
            }

        }

        @Override
        ButtonPanel newButtonPanel(){
            return new ButtonPanel3();
        }

        @Override
        void postCreate(){

        }

        void postCreate2(){
            super.postCreate();
        }


        public Mp3Adapter() {
            prevFile = new ShellButton(Html.fromHtml("<big><b>&lt;&lt;</big></b>"), this::mp3PrevFile, cGreen, bGreen, 4, buttonPanel);
            prev =  new ShellButton(Html.fromHtml("<big><b>&lt;</big></b>"), this::nop, cGreen, bGreen, 4, buttonPanel);
            prev.setOnTouchListener(new Toucher(this::mp3Prev));
            pause = new ShellButton("", this::mp3Play, playing, 4, buttonPanel);
            next = new ShellButton(Html.fromHtml("<big><b>&gt;</big></b>"), this::nop, cRose, bRose, 4, buttonPanel);
            next.setOnTouchListener(new Toucher(this::mp3Next));
            nextFile = new ShellButton(Html.fromHtml("<big><b>&gt;&gt;</big></b>"), this::mp3NextFile, cRose, bRose, 4, buttonPanel);
            random  = new ShellButton("RND", this::random, isRandom, 4, buttonPanel);
            repeat  = new ShellButton("Loop", this::repeat, isLoop, 4, buttonPanel);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (audioServiceBinder == null)
                        return;
                    if (progress == skipedSeek)
                        return;
                    double step = (double) audioServiceBinder.getDuration() / (double) seekBar.getMax();
                    audioServiceBinder.seekTo((int) (step * progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    /*if (!playing)
                        mp3Play(true);*/

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    /*if (playing)
                        mp3Play(false);*/
                }
            });


            postCreate2();

            t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!closed) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.yield();
                        }
                        pauseUpdate();
                    }
                }
            });
            t.start();
        }

        Thread t;

        private void random() {
            isRandom = !isRandom;
            random.setFlag(isRandom);
            if (audioServiceBinder != null){
                audioServiceBinder.setRandom(isRandom);
            }
        }

        private void repeat() {
            isLoop = !isLoop;
            repeat.setFlag(isLoop);
            if (audioServiceBinder != null){
                audioServiceBinder.setLoop(isLoop);
            }

        }

        private void mp3PrevFile() {
            if (audioServiceBinder != null){
                if (!audioServiceBinder.prevFile()){
                    mp3Play(false);
                }else{
                    truePlaying();
                }
            }
        }

        private void mp3NextFile() {
            if (audioServiceBinder != null){
                if (!audioServiceBinder.nextFile()){
                    mp3Play(false);
                }else{
                    truePlaying();
                }
            }
        }



        @Override
        public String getName(){
            return "mp3Adapter";

        }

        void nop(){

        }


        @Override
        public void init(int index){
            super.init(index);
        }


        class Toucher implements View.OnTouchListener{
             boolean actionUpFlag = false;
            Runnable run;
            Function function;
            CustomButton button;
            Toucher(Function function){
                this.function = function;
            }
            void setButton(CustomButton b){
                button = b;
            }

            void resetActionUpFlag(){
                actionUpFlag = false;
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    actionUpFlag = true;
                    button.pleaseWait();

                    if (run == null) {
                        run = () -> {

                            while (actionUpFlag) {

                                FreeShell.this.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    if (!function.function()) {
                                                        actionUpFlag = false;
                                                    }
                                                }catch (Throwable t){
                                                    actionUpFlag = false;
                                                }
                                            }
                                        }
                                );
                                Thread.yield();
                                try{
                                    Thread.sleep(100);
                                }catch (Exception e){

                                }
                            }
                            run = null;
                        };
                        new Thread(run).start();
                    }
                }else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    actionUpFlag = false;
                    button.pleaseEnd();
                }

                return true;
            }

        }

        @Override
        void showFirst(){
            init(0);
            File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            openFile(f, true, null);
        }





        /*ProgressBar backgroundAudioProgress;
        ProgressBar backgroundAudioProgress(){
            if (backgroundAudioProgress == null){
                backgroundAudioProgress = new ProgressBar(this);
            }
            return backgroundAudioProgress;
        }*/
        // Bind background service with caller activity. Then this activity can use
        // background service's AudioServiceBinder instance to invoke related methods.

        void setupPlayFile(){
            if (audioServiceBinder != null){
                File file = audioServiceBinder.getFile();
                if (file != null){
                    updateAll(oldFile);
                    if ((oldFile != null)&& oldFile.getParentFile().equals(file.getParentFile())){
                    }else{
                        audioDirs.clear();
                        File audioDir = file;
                        while (true) {
                            audioDir = audioDir.getParentFile();
                            if (audioDir == null)
                                break;
                            audioDirs.add(audioDir.getAbsolutePath());
                        }
                    }
                    oldFile = file;
                    updateAll(file);
                    int dd = audioServiceBinder.getTotalAudioDuration();
                    String s = getTime(dd);
                    durationInfo.setText(s);

                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(file.getAbsolutePath());

                    String title =
                            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String name =
                            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    if (title == null){
                        title = file.getName();
                    }
                    if (name == null){
                        name = "";
                    }
                    info.setText(' ' + name.trim() + ' ' + title.trim());
                    if (!playing) {
                        //skipedSeek = audioServiceBinder.getCurrentAudioPosition();
                        final int currProgressM = audioServiceBinder.getAudioProgress(seekBar.getMax());
                        skipedSeek = currProgressM;
                        seekBar.setProgress(currProgressM);
                    }

                }
            }

        }


        void truePlaying(){
            pause.setFlag(true);
            pause.button().invalidate();
            playing = true;
        }

        boolean mp3Play(boolean v){

            try {

                if (v){
                    if (audioServiceBinder != null) {
                        if (audioServiceBinder.start()) {
                            truePlaying();
                            setupPlayFile();
                            return true;
                        }
                    }
                }else{
                    playing = false;
                    pause.setFlag(false);
                    if (audioServiceBinder != null){
                        audioServiceBinder.stop();
                    }
                }

            }catch (Throwable e){
                printError(e.getMessage());
            }
            if ((audioServiceBinder != null)&&(audioServiceBinder.getFile() != null)) {
                int currProgress = audioServiceBinder.getAudioProgress(100);
                pause.button().setText(currProgress + "%");
            }else{
                pause.button().setText("");
            }
            pause.button().invalidate();
            return false;
        }

        boolean mp3Prev() {
            boolean ret = audioServiceBinder.prev();
            pauseUpdate();
            return ret;
        }

        boolean mp3Next() {
            boolean ret = audioServiceBinder.next();
            pauseUpdate();
            return ret;
        }


/*        void mp3PrevDir() {
            stop();
            currentPosition = storageList.dirs.size();
            provider().prev();
        }

        void mp3NextDir() {
            stop();
            currentPosition = storageList.size();
            provider().prev();
        }*/

        void stop(){
            try{
                if (audioServiceBinder != null)
                    audioServiceBinder.stop();
            }catch (Throwable t){

            }
            playing = false;

            pause.setFlag(false);
        }

        void mp3Play() {
            mp3Play(!playing);
        }


        File oldFile = null;
        void pauseUpdate(){
    /*        if (!playing) {
                if (audioServiceBinder != null){
                    if (audioServiceBinder.isPlaying())
                        audioServiceBinder.stop();
                }
                return;
            }*/
/*            if ((audioServiceBinder == null)||(audioServiceBinder.getFile() == null)|| (!audioServiceBinder.isPlaying())) {
                pause.setFlag(false);

            }*/

            if (audioServiceBinder == null)
                return;
            final File current = audioServiceBinder.getFile();
            if (current == null)
                return;
            final int currProgress100 = audioServiceBinder.getAudioProgress(100);
            final int dd = audioServiceBinder.getCurrentAudioPosition();
            final int currProgressW = audioServiceBinder.getAudioProgress(listW - timerWidth);
            final int currProgressM = audioServiceBinder.getAudioProgress(seekBar.getMax());
           // boolean playing2 = audioServiceBinder.isPlaying();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*if (!playing)
                        return;
                    if (!playing2) {
                        mp3NextFile();
                        return;
                    }*/
                    if (current != oldFile) {
                        setupPlayFile();
                    }

                    // Calculate the percentage.


                    pause.button().setText(currProgress100 + "%");
                    pause.button().invalidate();

                    String s = getTime(dd);
                    timer.setText(s);

                    timer.setX(currProgressW);
           /*dd = audioServiceBinder.getTotalAudioDuration() - dd;
            s = timeFormatter.format(dd);
            durationInfo.setText(s);*/



                    skipedSeek = currProgressM;
                    seekBar.setProgress(currProgressM);
                   /* int total = audioServiceBinder.getTotalAudioDuration();

                    if (dd >= total) {
                        mp3NextFile();
                    }*/

                }
            });
        }


        void close(RelativeLayout panel){
            mp3Adapter = null;
            super.close(panel);
        }

    }


    class App{
        Intent intent;
        Drawable logo;
        Object label;
        String pack;
        HashMap<Intent, Integer> map;
        HashMap<String, Intent> packageIndex;
        HashMap<String, Integer> nameIndex;
        public String toString(){
            return label.toString();
        }
    }
    class Apps{

        Apps(int size){
            apps = new ArrayList<>(size);
            map = new HashMap<>(size);
            packageIndex = new HashMap<>(size);
            nameIndex = new HashMap<>(size);
        }
        ArrayList<Object> apps;
        App getApp(int index){
            return (App)apps.get(index);
        }

        HashMap<Intent, App> map;
        HashMap<String, Intent> packageIndex;
        HashMap<String, App> nameIndex;

    }
    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
                : false;
    }

    public static Drawable getAppIcon(PackageManager mPackageManager, ApplicationInfo applicationInfo) {
            return mPackageManager.getApplicationIcon(applicationInfo);
    }


    public static Drawable getAppIcon(PackageManager mPackageManager, String packageName) {

        try {
            return mPackageManager.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    Apps allApps;
    Apps allApps(){
        if (allApps == null){

         final PackageManager pm = getPackageManager();
            List<ApplicationInfo> appages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            Apps ret = new Apps(appages.size());
            try {
                for (ApplicationInfo packageInfo : appages) {
                    try {
                        Intent old = ret.packageIndex.get(packageInfo.packageName);
                        if (old == null) {
                            Intent intent = pm.getLaunchIntentForPackage(packageInfo.packageName);

                            if (intent != null) {
                                Drawable logo = getAppIcon(pm, packageInfo);
                                if (logo != null) {
                                    App app = new App();
                                    ret.apps.add(app);
                                    ret.map.put(intent, app);
                                    ret.nameIndex.put(packageInfo.packageName, app);
                                    ret.packageIndex.put(packageInfo.packageName, intent);
                                    app.logo = logo;
                                    app.label = pm.getApplicationLabel(packageInfo).toString();
                                    app.intent = intent;
                                    app.pack = packageInfo.packageName;
                                }
                            }
                        }
                    } catch (Throwable t) {

                    }
                }
            }catch (Throwable t){

            }
            try{
                List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
                for (PackageInfo packageInfo : packages) {
                    try{
                        Intent old = ret.packageIndex.get(packageInfo.packageName);
                        if (old == null) {
                            Intent intent = pm.getLaunchIntentForPackage(packageInfo.packageName);

                            if (intent != null) {
                                Drawable icon = getAppIcon(pm, packageInfo.applicationInfo);
                                /*if (appIcon != null)*/
                                {
                                       String appName = pm.getApplicationLabel(
                                            packageInfo.applicationInfo).toString();


                                    App app = new App();
                                    ret.apps.add(app);
                                    ret.map.put(intent, app);
                                    ret.nameIndex.put(packageInfo.packageName, app);
                                    ret.packageIndex.put(packageInfo.packageName, intent);
                                    app.logo = icon;
                                    app.label = appName;
                                    app.intent = intent;
                                    app.pack = packageInfo.packageName;
                                }
                            }
                        }
                    }catch (Throwable t){

                    }
                }

            }catch (Throwable t){

            }

            try{
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> pkgAppsList = pm.queryIntentActivities( mainIntent, 0);
                for(ResolveInfo resolveInfo: pkgAppsList){
                    try{

                        Drawable logo = resolveInfo.loadIcon(pm);
                            String pname = resolveInfo.activityInfo.applicationInfo.packageName;
                            Intent old = ret.packageIndex.get(pname);
                            if (old == null) {
                                Intent intent = pm.getLaunchIntentForPackage(pname);
                                if (intent != null) {
                                    CharSequence cs = resolveInfo.loadLabel(pm);
                                    App app = new App();
                                    ret.apps.add(app);
                                    ret.map.put(intent, app);
                                    ret.nameIndex.put(pname, app);
                                    ret.packageIndex.put(pname, intent);
                                    app.logo = logo;
                                    app.label = cs.toString();
                                    app.intent = intent;
                                    app.pack = pname;
                                }
                            }

                            String cname = resolveInfo.activityInfo.applicationInfo.className;
                            String appName = pname + "." + cname;
                            old = ret.packageIndex.get(appName);
                            if (old == null) {

                                Intent intent = pm.getLaunchIntentForPackage(appName);
                                if (intent != null) {
                                    CharSequence cs = resolveInfo.loadLabel(pm);
                                    App app = new App();
                                    ret.apps.add(app);
                                    ret.map.put(intent, app);
                                    ret.nameIndex.put(appName, app);
                                    ret.packageIndex.put(appName, intent);
                                    app.logo = logo;
                                    app.label = cs.toString();
                                    app.intent = intent;
                                    app.pack = appName;

                                }
                            }
                        }catch (Throwable t){

                        }
                    }
            }catch (Throwable t){

            }
            allApps = ret;
            Collections.sort(allApps.apps, comparator);

            return ret;
        }
        return  allApps;
    }

    AppAdapter appAdapter;
    public AppAdapter appAdapter(){
        if (appAdapter == null){
            appAdapter = new AppAdapter();
        }
        return appAdapter;

    }
    class AppAdapter extends Adapter{
        void close(RelativeLayout panel){
            appAdapter = null;
            super.close(panel);
        }

        @Override
        public void refresh() {
            allApps = null;
            setAll(allApps().apps);
            mainReLayout();
        }

        @Override
        public void start(Tag tag) {
        }

        @Override
        public void init(int index){
            super.init(index);
            allApps = null;
            setAll(allApps().apps);
        }


        @Override
        public String getName(){
            return "appAdapter";
        }



        public AppAdapter() {
            create0(0, 0);
            postCreate();

        }
        boolean b = true;

        @Override
        void showFirst() {
            init(0);
        }

        @Override
        public Item getView(final int position, int filteredPosition) {
            final Item item = new Item(position);
            final App app = allApps.getApp(position);
            View.OnClickListener open = new View.OnClickListener() {
                @Override
                public void onClick (View v){
                    try{
                        Intent intent = app.intent;

                        FreeShell.this.startActivityForResult(intent, 262144);
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }

                }
            };

            View.OnClickListener settings = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        String packageName = app.pack;//filtered.get(position);
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null));
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        FreeShell.this.startActivityForResult(intent, 262144);
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }
                }
            };
            String s = current.get(filteredPosition).toString();
            if (!(app ==  current.get(filteredPosition))){
                printError("(!(app ==  current.get(filteredPosition))");
            }
            String packageName = app.pack;//filtered.get(position);
            String fileName = myAppDir() + "/" + normalizeFileName(s) + ".link";
            final File file = new File(fileName);
            if (!file.exists()) {
                fSync().writeFile0(file, "packageName=" + packageName);
            }

            View.OnClickListener select = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if ((file != null)&&(file.exists())) {
                            item.changeSelection();
                        }
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }
                }
            };


            if (b) {

                item.init('?',this, file, listW, open, settings, select, null, null);
                item.setNormalColors(cGreen, bGreen);

            }else{
                item.init('?', this, file, listW, open, settings, select, null, null);
                item.setNormalColors(cGreen1, bGreen1);
            }
            Drawable logo = app.logo;
            if (logo != null) {

                item.setIcon(logo);
                /*logo.setBounds(0, 0, logo.getIntrinsicWidth(), logo.getIntrinsicHeight());
                ImageSpan span = new ImageSpan(logo, ImageSpan.ALIGN_BASELINE);
                SpannableString spanned = new SpannableString("  " + s);
                spanned.setSpan(span, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                fileItem.textView.setText(spanned);*/
                item.textView.setText(s);
            }else
                item.textView.setText(s);
            b = !b;
            return item;
        }


    }


    String myDir() {
        return getApplicationInfo().dataDir;
    }
    File appDir;
    String myAppDir() {
        if (appDir != null){
            return appDir.getAbsolutePath();
        }
        try {
            String myDir = myDir() + "/app";
            appDir = new File(myDir);
            if (!appDir.exists()) {
                appDir.mkdir();
            } else {
                if (!appDir.isDirectory()) {
                    appDir.delete();
                }
                appDir.mkdir();
            }
            return appDir.getAbsolutePath();
        }catch (Throwable t){
            printError(t);
        }

        return "";
    }
    File historyDir;
    String myHistoryDir() {

        if (historyDir != null){
            return historyDir.getAbsolutePath();
        }
        try {
            String myDir = myDir() + "/history";
            historyDir = new File(myDir);
            if (!historyDir.exists()) {
                historyDir.mkdir();
            } else {
                if (!historyDir.isDirectory()) {
                    historyDir.delete();
                }
                historyDir.mkdir();
            }
            return historyDir.getAbsolutePath();
        }catch (Throwable t){
            printError(t);
        }

        return "";


    }

    File homeDir;
    String myHelpDir() {
        String s = myHomeDir() + "/help";
        File f = new File(s);
        if (!f.exists()){
            f.mkdir();
        }
        return s;
    }
    String myHomeDir() {

        if (homeDir != null){
            return homeDir.getAbsolutePath();
        }
        try {
            String myDir = myDir() + "/home";
            homeDir = new File(myDir);
            if (!homeDir.exists()) {
                homeDir.mkdir();
            } else {
                if (!homeDir.isDirectory()) {
                    homeDir.delete();
                }
                homeDir.mkdir();
            }
            return homeDir.getAbsolutePath();
        }catch (Throwable t){
            printError(t);
        }

        return "";


    }
    synchronized void saveMtk(){
        saveVariables();
        saveMtk(lastStateFileName());
    }

    synchronized void saveMtk(String name){
        if (!loaded)
            return;
        if (!name.endsWith(".shell")){
            name = name + ".shell";
        }
        PrintWriter writer = null;
        try {
            File file = new File(name);
            File parent = file.getParentFile();
            if (!parent.exists()){
                parent.mkdirs();
                refreshDirs(parent.getParent());
            }
            writer = new PrintWriter(file, "UTF-8");
        } catch (Exception e) {
            printError("can't create '" + name + "': " + e.getMessage());
            return;
        }
        writer.println("<root>");
        save(writer);
        for(int i = 0; i < listPanel().list.size(); i++){
            Mtk mtk = (Mtk)listPanel().list.elementAt(i).getAdapter();
            if (!mtk.isLocked()) {
                writer.println("<Mtk name='" + mtk.getName() + "'>");
                mtk.save(writer);
                writer.println("</Mtk>");
            }
        }
        writer.println("vScrollY="+ vScroll().getScrollY());
        writer.println("hScrollX="+ listPanel().hScrollPanels().getScrollX());
        writer.println("</root>");
        writer.close();
        //closeLog();
    }
    String getMyFileName(String name){
        return myDir() + '/' + name;
    }

    boolean run = true;
    MapThreads mapThreads;
    public MapThreads mapThreads(){
        if (mapThreads == null )
            mapThreads = new MapThreads();
        return mapThreads;
    }

    String lastStateFileName(){
        return getMyFileName("lastState.shell");
    }

    class Entry{
        Mtk mtk;
        Tag tag;
    }

    synchronized void load(){
        loaded = false;
        variables = loadVariables();
        String fn = lastStateFileName();
        File xml = new File(fn);
        loadMtk(xml);
    }
    boolean firstLoad = true;
    synchronized void loadMtk(File xml){
        loaded = false;
        printLog("load");
        if(xml.exists()) {
            listPanel().closeAll();
            run = true;
            XParser xParser = mapThreads().openXmlFile(xml.getAbsolutePath(), root -> {
                try {
                    run = false;
                    FreeShell.this.runOnUiThread(
                            new Runnable() {
                                public void run() {
                                    int p = Thread.currentThread().getPriority();
                                    int x = 0, y = 0;
                                    try{
                                        if (root == null)
                                        {
                                            mainReLayout();
                                            return;
                                        }
                                        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                                        load(root);
                                        ArrayList<Entry> list = new ArrayList();
                                        Tag tag = root.getTagByLink("#Mtk");
                                        while (tag != null)
                                        {
                                            String name = tag.getLocalStringByName("name");
                                            if (notEmpty(name)) {
                                                try {
                                                    Method m = FreeShell.this.getClass().getMethod(name);
                                                    if (m != null) {
                                                        Mtk mtk = (Mtk) m.invoke(FreeShell.this);
                                                        if (mtk != null) {
                                                            mtk.init(listPanel().list.size());
                                                            mtk.load(tag);
                                                            Entry entry = new Entry();
                                                            entry.mtk = mtk;
                                                            entry.tag = tag;
                                                            list.add(entry);
                                                        }
                                                    }

                                                } catch (Throwable e) {
                                                    printError(e.getMessage());
                                                }


                                            }
                                            tag = tag.getNextTag();
                                        }
                                        for(Entry e: list){
                                            e.mtk.start(e.tag);
                                        }
                                        FreeShell.this.start(root);

                                        y = root.getIntByLink("#vScrollY");
                                        x = root.getIntByLink("#hScrollX");
                                        BaseRoot baseRoot = root.getBaseRoot();
                                        baseRoot.deleteBody();


                                    }finally{
                                        firstLoad = false;
                                        Thread.currentThread().setPriority(p);
                                        final int x0 = x;
                                        final int y0 = y;
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Thread.sleep(100);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        loaded = true;
                                                        mainReLayout();
                                                        vScroll().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                vScroll.smoothScrollTo(x0, y0);
                                                                ;
                                                            }
                                                        }, 1000);
                                                        listPanel().hScrollPanels().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                listPanel().hScrollPanels().smoothScrollTo(x0, y0);
                                                                ;
                                                            }
                                                        }, 1000);
                                                    }
                                                });
                                            }
                                        }).start();
                                    }

                                }
                            });
                }catch (Throwable t){
                    printError(t);
                }


            }).getXParserImpl();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    while (run)
                        xParser.getParser().run();

                }
            }).start();


        }else {
            loaded = true;
            if (listPanel().list.size() == 0){
                contacts();
                saveMtk();
            }else
                printError(xml.getAbsolutePath() + " not found.");
            mainReLayout();
        }


    }

    /*MapThreads mapThreads;
    MapThreads mapThreads(){
        if (mapThreads == null){
            mapThreads = new MapThreads();
            mapThreads.getFileThread().start();
        }
        return mapThreads;
    }*/

    @Override
    public void onStop() {
  //      saveMtk();
        super.onStop();

    }


    @Override
    public void onPause() {
        printLog("onPause");
        saveMtk();
        try{
            super.onPause();
        }catch (Exception e){
            freeShell.printError(e);
        }

    }

    @Override
    protected void onDestroy() {
        try{
            // Unbound background audio service when activity is destroyed.
            listPanel().closeAll();
            printLog("onDestroy");
            unBoundAudioService();
            closeLog();
            super.onDestroy();
        }catch (Exception e){
            freeShell.printError(e);
        }

    }



    private void unBoundAudioService() {
        if (audioServiceBinder != null) {
            try {
                unbindService(serviceConnection);
            }catch (Throwable e){

            }
        }
    }
    String getStringArrayRepresentation(String name, Set<String> set){
        if ((set == null)||(set.size() == 0))
            return "";
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(String s: set){
            jsonArray.put(s);
        }
        try {
            jsonObject.put(name, jsonArray);
            return jsonObject.toString();
        } catch (JSONException e) {
            return "";
        }
    }



    static boolean notEmpty(String s){
        return (s != null) && (s.length() > 0);
    }
    static boolean notEmpty(String[] ss){
        return (ss != null) && (ss.length > 0);
    }

    //SpannableString spanned;
    RelativeLayout.LayoutParams buttonParams;
    RelativeLayout.LayoutParams buttonParams(){
        if (buttonParams == null){
            //RelativeLayout.LayoutParams.MATCH_PARENT
            buttonParams = new RelativeLayout.LayoutParams(hb, hb);
            buttonParams.addRule(RelativeLayout.CENTER_VERTICAL);

        }
        return buttonParams;
    }

/*    Spanned selSettingsHtml = Html.fromHtml("<font color=#FFFF40><b>...</b></font>");
    Spanned settingsHtml = Html.fromHtml("<font color=#000060><b>...</b></font>");*/
    final static int blue = 0x00000060;


    class Item {
        boolean isParent = false;
        Adapter adapter;
        File file;
        TextView textView;
        TextView selectButton;
        TextView settingsButton;
        View openButton0;
        //Integer y;
        RelativeLayout panel;
        RelativeLayout parent;
        RelativeLayout.LayoutParams panelLayout;
        View icon;
        Runnable loadIcon;
        View.OnClickListener open;
       // int hb;

        int globalPosition;


        Item(int globalPosition){
            setGlobalPosition(globalPosition);
        }

        public void setGlobalPosition(int i) {
                selSettingsHtml = Html.fromHtml("<font color=#FFFF40><b>" + i + "</b></font>");
                settingsHtml = Html.fromHtml("<font color=#000060><b>" + i + "</b></font>");
                this.globalPosition = i;
        }


        void setIcon(Drawable d){
            ImageView icon = new ImageView(FreeShell.this);
            icon.setImageDrawable(d);
            icon.setX(iconX);
            icon.setY(0);

            panel.addView(icon, buttonParams());
            if (openButton0 != null) {
                panel.removeView(openButton0);
                openButton0 = null;
            }
            icon.setClickable(true);
            icon.setOnClickListener(open);
            this.icon = icon;
        }
        boolean isSelect(){
            return fSync().isSelect(this);
        }

        void format(){
            if (isSelect()){
                //openButton0.setTextColor(0xFFFFFF40);
                panel.setBackgroundColor(bSel);
                textView.setTextColor(cSel);
                settingsButton.setTextColor(cSel);
                selectButton.setTextColor(cSel);
            }else{
                //panel.setBackgroundColor(Color.TRANSPARENT);
                setNormalColors();
                //textView.setTextAppearance(FreeShell.this, R.style.font0);
                // openButton0.setTextColor(0xD4000000);//0xD40FFFFF

            }

                panel.invalidate();

        }
        void changeSelection(){
            try{
                fSync().changeSelect(file, Item.this);
                if (FreeShell.this.selected != null){
                    selected.refresh();
                    mainReLayout();
                }
            }catch (Exception e){
                FreeShell.this.printError(e);
            }

/*            if (isSelect())
                fSync().removeSelect(file, file.getAbsolutePath());
            else
                fSync().select(this);*/
            //format();

        }

        public String toString(){
            return textView.getText().toString();
        }

        void remove(){
           if (file != null)
                fSync().removeFileItem(file.getAbsolutePath(), this);;
            if (parent != null)
                parent.removeView(panel);
        }



        void setText(SpannableString spanned) {
            textView.setText(spanned);
            textView.invalidate();
            panel.invalidate();
        }



        int h(){
            return panelLayout.height;
        }


        RelativeLayout.LayoutParams reLayout(RelativeLayout parent, int x, int y) {
            remove();
            this.parent = parent;
//            this.x = x;
            panel.setX(x);
            panel.setY(y);
            parent.addView(panel, panelLayout);
            return panelLayout;
        }
        Spanned selSettingsHtml, settingsHtml;

        int cColor, bColor;
        void setNormalColors(int color, int background){
            cColor = color;
            bColor= background;
            setNormalColors();
        }
        void setNormalColors(){
            textView.setTextColor(cColor);
            //selectButton.setTextColor(blue);
            if (selectButton != null) {
                selectButton.setTextColor(0xc0101000);
                selectButton.setText(settingsHtml);
            }
            settingsButton.setTextColor(0xc0101000);
            //openButton0.setTextColor(color);
            panel.setBackgroundColor(bColor);

        }
        void setText(String text){
            Spanned sp = Html.fromHtml(text);
            textView.setText(sp);
        }
        int w;
        int iconX;
        public void init(Character defSymbol, Adapter adapter, File file, int w, View.OnClickListener open, View.OnClickListener settings, View.OnClickListener selectProcedure,
                         Runnable loadIcon,
                         TextView textView) {
            this.loadIcon = loadIcon;
            this.adapter = adapter;
            this.file = file;
            this.open = open;
            this.w = w;
            if (textView == null) {
                textView = new TextView(FreeShell.this);
            }
            this.textView = textView;
            textView.setClickable(true);
            textView.setOnClickListener(open);
            textView.setGravity(NO_GRAVITY);

          //  this.hb = FreeShell.this.hb;


            int textW = w - hb;
            int textX = hb;
            if (defSymbol != null) {
                textW -= hb;
                textX += hb;
            }
            textView.setTextAppearance(FreeShell.this, R.style.font0);
            if (settings != null) {
                textW -= hb;
                selectButton = new Button(FreeShell.this);
                selectButton.setClickable(true);
                selectButton.setTextAppearance(FreeShell.this, R.style.fontSelButton);
                selectButton.setText(settingsHtml);

                selectButton.setLayoutParams(buttonParams());

                /*selectButton.measure(
                        makeMeasureSpec,

                        makeMeasureSpec);*/
                selectButton.setOnClickListener(selectProcedure);
            }

            Button openButton = null;
            if (defSymbol != null) {
                openButton = new Button(FreeShell.this);
                openButton.setOnClickListener(open);
                openButton.setClickable(true);
                openButton.setTextAppearance(FreeShell.this, R.style.fontSelButton);
                this.openButton0 = openButton;
                String ext;
                if (file != null)
                    ext = getFileExt(file.getName());
                else
                    ext = "";
                if ((file != null) && AudioServiceBinder.isAudioExtension(ext)) {
                    openButton.setText(Html.fromHtml("<big><b>&#x1d11e;</b></big>"));
                } else if ((file != null) && (ext.equals("var"))) {
                    openButton.setText(Html.fromHtml("<big><b>&#x1D6D8;</b></big>"));
                } else if ((file != null) && (isText(ext))) {
                    openButton.setText(Html.fromHtml("<big><b>&#x1f4d6;</b></big>"));
                }else if ((file != null) && (ext.equals("zip"))) {
                    openButton.setText(Html.fromHtml("<big><b>&#x1f5dc;</b></big>"));
                } else
                    openButton.setText(Html.fromHtml("<big><b>" + defSymbol + "</b></big>"));
                openButton.setBackgroundColor(Color.TRANSPARENT);
                openButton.setLayoutParams(buttonParams());
            }

            //
            //openButton0.setGravity(NO_GRAVITY);
            /*openButton0.setWidth(hb);
            openButton0.setHeight(hb - (hb >> 1));*/
            //openButton0.setGravity(Gravity.CENTER);


/*            openButton0.measure(
                    makeMeasureSpec,

                    makeMeasureSpec);*/

            if (selectProcedure == null){
                selectProcedure = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{
                            changeSelection();
                        }catch (Exception e){
                            FreeShell.this.printError(e);
                        }
                    }
                };
            }

            Button settingsButton = new Button(FreeShell.this);
            settingsButton.setOnClickListener(settings);
            settingsButton.setClickable(true);
            settingsButton.setTextAppearance(FreeShell.this, R.style.fontSelButton);
            this.settingsButton = settingsButton;

            settingsButton.setText(Html.fromHtml("<big><b>...</b></big>"));
            settingsButton.setBackgroundColor(Color.TRANSPARENT);
            //openButton0.setGravity(NO_GRAVITY);
            /*openButton0.setWidth(hb);
            openButton0.setHeight(hb - (hb >> 1));*/
            //openButton0.setGravity(Gravity.CENTER);

            settingsButton.setLayoutParams(buttonParams());
            settingsButton.setX(w - hb);
            settingsButton.setY(0);


            panelLayout = new RelativeLayout.LayoutParams(w, RelativeLayout.LayoutParams.WRAP_CONTENT);
            panel = new RelativeLayout(FreeShell.this);
            panel.setLayoutParams(panelLayout);
            panel.setClickable(true);
            panel.setOnClickListener(open);


            textView.setX(textX);
            textView.setY(0);
            RelativeLayout.LayoutParams textLayout = new RelativeLayout.LayoutParams(textW, RelativeLayout.LayoutParams.WRAP_CONTENT);
            textLayout.addRule(RelativeLayout.CENTER_VERTICAL);

            textView.setLayoutParams(textLayout);
            panel.addView(textView, textLayout);



            if (this.selectButton != null) {
                this.selectButton.setX(0);
                panel.addView(this.selectButton);
                iconX = hb;
                if (openButton != null)
                    openButton.setX(iconX);

            }else {

                iconX = 0;
                if (openButton != null)
                    openButton.setX(iconX);
            }
            if (openButton != null)
                panel.addView(openButton, buttonParams);
            panel.addView(settingsButton);

            textView.setBackgroundColor(Color.TRANSPARENT);
            if (this.selectButton != null)
                this.selectButton.setBackgroundColor(Color.TRANSPARENT);
            //openButton0.setBackgroundColor(Color.TRANSPARENT);
            panel.setBackgroundColor(Color.TRANSPARENT);
        }

    }
    class FileItem extends Item {
        FileItem(int position) {
            super(position);
        }

        FileAdapter fileAdapter(){
            return (FileAdapter)adapter;
        }


        void open(){
            openFileIntent(file);
        }

        void firstOpen(){
            if (file == null)
                return;
            if (adapter.isLocked())
                return;
            try {
                fileAdapter().listView().setSelection(fileAdapter().getCurrentPosition(globalPosition));
            } catch (Exception e) {
                log(e);
            }
            if (file.isDirectory()) {
                if (fileAdapter().getStorageList().dirsSize() == 1){
                    //fileAdapter.openFile(file, false, null);
                    fileAdapter().openSomeDir(file);
                }else
                    fileAdapter().openFile(file, true, globalPosition);
                return;
            }
            if (FreeShell.this.selected != null) {
                if (fileAdapter() == FreeShell.this.selected) {
                    fileAdapter().openSomeDir(file.getParentFile());
                    return;
                }
            }
            String ext = getFileExt(file.getName());
            if (ext != null) {
                br0:if (ext.equals("link")) {
                    String name = loadLine(file.getAbsoluteFile());
                    if (name == null)
                        break br0;
                    if (name.startsWith("packageName=")) {
                        name = name.substring("packageName=".length());

                    } else
                        break br0;
                    Intent intent = allApps().packageIndex.get(name);
                    if (intent != null) {
                        FreeShell.this.startActivityForResult(intent, 262144);
                        return;
                    }
                    break br0;
                }
            }
            if (AudioServiceBinder.isAudioExtension(ext)){
                if (audioServiceBinder != null){
                    audioServiceBinder.ini(fileAdapter().getFileList(file), 0);
                }
                Mp3Adapter mp3Adapter = mp3Adapter();
                if (adapter != mp3Adapter) {
                    mp3Adapter.openSomeDir(file.getParentFile());
                    mp3Adapter.show();
                }
                mp3Adapter.mp3Play(true);
                return;
            }else if(ext.toLowerCase().equals("zip")){
                unzip(file, (FileAdapter) adapter);
                return;
            }/*else if (ext.equals("sh")) {
                sh(file, adapter.listView().index);
            } */else if (isText(ext)){
                /*int pos = fileAdapter().listView().getSelectedItemPosition();
                String dir = fileAdapter().getDir();*/
                openTextFile(fileAdapter(), file, fileAdapter().listView.index + 1);
                //fileAdapter().close();

              // textAdapter.mainRelayout2();

                //textAdapter().init(pos, dir, file, index);
                //mainReLayout();


                //textAdapter().init(file, fileAdapter().listView.index + 1);
                //previewTextPanel().openFile(FileItem.this);
                return;
            }else if (ext.equals("shell")) {
                loadMtkDialog(file);
                return;
            } else if (ext.equals("var")) {
                loadVarDialog(file);
                return;
            }
            open();

        }


        boolean isSelect() {

            if (file == null)
                return false;
            return fSync().isSelect(this);

        }
        boolean parity;
        public void init(Character defSymbol, File f, boolean isParent, FileAdapter fileAdapter, boolean b) {
            this.file = f;
            this.parity = b;
            this.isParent = isParent;
            this.adapter = fileAdapter;
            int abc = fileAdapter.abc;

            View.OnClickListener select = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeSelection();
                }
            };

            View.OnClickListener open;
            if (isParent) {

                open = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{
                            if (adapter.isLocked())
                                return;
                            fileAdapter.lock();
                            fileAdapter.getStorageList().openFileFromTree(globalPosition);
                        }catch (Exception e){
                            FreeShell.this.printError(e);
                        }

                    }
                };


            } else {
                open = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                            firstOpen();
                    }
                };
            }

            View.OnClickListener settings = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        fileAdapter.menu(FileItem.this);
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }
                }
            };
            /*Float scale = null;
            if (isParent){
                int parentsAmount = fileAdapter.dirs.size();
                if (parentsAmount > 0) {
                    scale = (float) 0.6;
                    if (position > 0) {
                        float d = (float) 0.4 / (parentsAmount - position);
                        scale += d;
                        if (scale >= 1)
                            scale = null;
                    }
                }
            }*/

            Runnable rloadIcon = null;

            br:if ((f != null)&&(!f.isDirectory())) {




                String t = file.getName();
                String ext = getFileExt(t);
                final File ff = file;
                if (ext != null) {
                    br0:
                    if (ext.equals("link")) {
                        String name = loadLine(file.getAbsoluteFile());
                        if (name == null)
                            break br0;
                        if (name.startsWith("packageName=")) {
                            name = name.substring("packageName=".length());


                        } else break br0;
                        App app = allApps().nameIndex.get(name);
                        Drawable logo = app.logo;
                        rloadIcon = new Runnable() {
                            @Override
                            public void run() {
                                if (logo != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setIcon(logo);
                                        }
                                    });
                                }

                            }
                        };
                        break br;
                    }else if (ext.equals("shell")){
                        rloadIcon = new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    Intent i = allApps().packageIndex.get("mtk.free.shell");
                                    if (i != null) {
                                        if (allApps.map != null) {
                                            App app = allApps.map.get(i);
                                            if (app != null) {
                                                Drawable logo = app.logo;
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        FileItem.this.setIcon(logo);
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }catch (Exception e){

                                }
                            }
                        };

                    }else if (AudioServiceBinder.isAudioExtension(ext)){

                    }else if (ext.equals("var")){

                    }else if (ext.equals("txt")){

                    }else if (ext.equals("zip")){

                    } else if (isImageFile(file.getAbsolutePath())){
                        final int THUMBSIZE = hb;
                        rloadIcon = new Runnable() {
                            @Override
                            public void run() {
                                // if (isImageExt(ext)) {

                                try {
                                    Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(ff.getAbsolutePath()),
                                            THUMBSIZE, THUMBSIZE);
                                    if (bitmap != null) {
                                        final Drawable  logo = new BitmapDrawable(getResources(), bitmap);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                FileItem.this.setIcon(logo);
                                            }
                                        });
                                    }
                                } catch (Throwable e) {
                                    log(e);
                                }
                                //}

                            }
                        };
                    }else if (isVideoFile(file.getAbsolutePath())) {
                        rloadIcon = new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                                if (bitmap != null) {
                                    final Drawable  logo = new BitmapDrawable(getResources(), bitmap);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            FileItem.this.setIcon(logo);
                                        }
                                    });
                                }
                            }
                        };


                    }else
                    {
                        rloadIcon = new Runnable() {
                            @Override
                            public void run() {
                                Drawable logo = getIconByExt(ext);
                                if (logo != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            FileItem.this.setIcon(logo);
                                        }
                                    });
                                }
                            }
                        };

                    }

                }
            }


            super.init(defSymbol, fileAdapter, file, listW, open, settings, select, rloadIcon, null);
            if (file == null){
                panel.setMinimumHeight(hb);
                if (openButton0 != null)
                    panel.removeView(openButton0);
                if (selectButton != null)
                    panel.removeView(selectButton);
            }
            format();


        }


        @Override
        void format() {
            if (file != null){
                if (!file.exists()){
                    file = null;
                }
            }
            boolean isSelect = isSelect();
            if (isSelect) {
                //openButton0.setTextColor(0xFFFFFF40);
                panel.setBackgroundColor(0xFF000060);
                textView.setTextColor(0xFFFFFF40);
                if (selectButton != null)
                    selectButton.setText(selSettingsHtml);
                if (openButton0 != null){
                    if (openButton0 instanceof TextView){
                        ((TextView) openButton0).setTextColor(0xFFFFFF40);
                    }

                }

            } else {
                if (selectButton != null)
                    selectButton.setText(settingsHtml);
                if (openButton0 != null){
                    if (openButton0 instanceof TextView){
                        ((TextView) openButton0).setTextColor(0xc0101000);
                    }
                }
                // openButton0.setTextColor(0xD4000000);//0xD40FFFFF

            }

            if (file == null) {
                if (globalPosition == 0)
                    textView.setText("*** LIST OF ROOTS ***");
                panel.setBackgroundColor(bLight);
                textView.setTextColor(cMangeta);
                if (openButton0 != null)
                    openButton0.setOnClickListener(null);
                return;
            }
            if (file.isDirectory()) {
                setDirColors(isSelect, isParent);
            }else
                paintFileItem(isSelect);

                panel.invalidate();
        }


        void setDirColors(boolean isSelect, boolean isParent) {
            boolean b = false;
            if (file == null)
                return;
            if (audioDirs.contains(file.getAbsolutePath())) {
                b = true;
            }
            TextView icon;
            if (this.icon == null) {
                icon = new TextView(FreeShell.this);
                if (openButton0 != null){
                    panel.removeView(openButton0);
                    openButton0 = null;
                }

                icon.setBackgroundColor(Color.TRANSPARENT);

                icon.setTextAppearance(FreeShell.this, R.style.fontSelButton);

                icon.setClickable(true);
                icon.setOnClickListener(this.open);
                icon.setGravity(Gravity.CENTER);
                icon.setX(iconX);
                icon.setY(0);

                this.icon = icon;
                panel.addView(icon, hb, hb);
                if (isParent) {
                    //&lt;parent&gt;
                    icon.setText(Html.fromHtml("<center>&lt;parent&gt</center>"));
                } else {
                    icon.setText(Html.fromHtml("<center>&lt;dir&gt</center>"));
                }
                this.icon = icon;
            } else {
                icon = (TextView) this.icon;
            }

            Long len = fSync().sizes().get(file);
            StringBuilder src = formatFile(b, len);
            String s = src.toString();
            textView.setText(Html.fromHtml(s));
            if (!isSelect) {
                icon.setTextColor(0xc0101000);
                if (isParent) {
                    panel.setBackgroundColor(bLight);
                    textView.setTextColor(cMangeta);

                } else {
                    panel.setBackgroundColor(bCyan);
                    textView.setTextColor(cCyan);
                }
            } else {
                icon.setTextColor(0xFFFFFF40);
            }



        }


        String formatLen(Long len) {
            if (len == null)
                return "";
            String sl = Long.toString(len);
            StringBuilder sb = new StringBuilder(sl.length() + sl.length() / 3);
            for (int i = sl.length() - 1, c = 0; i >= 0; i--) {
                sb.insert(0, sl.charAt(i));
                if (c == 2) {
                    if (i > 0)
                        sb.insert(0, '.');
                    c = 0;
                } else
                    c++;
            }
            sl = sb.toString();
            return sl;
        }

        StringBuilder formatFile(boolean b, Long len) {
            File f = file;
            String t = f.getName();
            StringBuilder src = new StringBuilder();

            String sl = formatLen(len);
           // src.append("<big>");
            int abc = fileAdapter().abc;
            if (abc == StorageList.ABC_SORT) {
                src.append(t);
                src.append(' ');
                src.append("<font color=#C0101000><small><small>");
                src.append(sl);
                src.append(' ');
                src.append(fileAdapter().getStorageList().getTimes(f.lastModified()));
                src.append("</small></small></font>");
            } else if (abc == StorageList.SIZE_SORT) {
                src.append(sl);
                src.append(' ');
                src.append(t);
                src.append(' ');
                src.append("<font color=#C0101000><small><small>");
                src.append(fileAdapter().getStorageList().getTimes(f.lastModified()));
                src.append("</small></small></font>");
            } else if (abc == StorageList.MODIFY_SORT) {
                src.append(fileAdapter().getStorageList().getTimes(f.lastModified()));
                src.append(' ');
                src.append(t);
                src.append(' ');
                src.append("<font color=#C0101000><small><small>");
                src.append(sl);
                src.append("</small></small></font>");
            }
        //    src.append("</big>");
            if (b) {
                src.insert(0, "<b>");
                src.append("</b>");
            }
            return src;
        }


        void paintFileItem(boolean isSelect) {
            File f = file;
            if (f == null)
                return;
            boolean b = false;
            if (audioServiceBinder != null) {
                if (f.equals(audioServiceBinder.getFile())) {
                    b = true;
                }
            }
            StringBuilder src = formatFile(b, f.length());
            String s = src.toString();
            SpannableString spanned = new SpannableString(Html.fromHtml(s));
            textView.setText(spanned);
            if (!isSelect) {
                if (this.parity) {
                    panel.setBackgroundColor(bGreen);
                }else{
                    panel.setBackgroundColor(bGreen1);
                }
                textView.setTextColor(cBlue);
            }


        }
    }

    //Spanned sel = Html.fromHtml("<b>+</b>");




/*            @Override
            public void onItemClick(int position) {
                File file;
                if (position < dirs.size()) {
                    openFileFromTree(position);
                } else {

                }
            }*/


    /*void loadFile(String name, EditText text){
        try {

            File f = new File(name);
            BufferedReader parity = new BufferedReader(new FileReader(f));
            String readLine;
            while ((readLine = parity.readLine()) != null) {

                text.append(readLine);
                char c = readLine.charAt(0);
                if (readLine.endsWith("\n"))
                    text.append("\n");
            }
            try {
                parity.close();
            }catch (Throwable t) {
            }
        } catch (IOException e) {
            printError(e.getMessage());
        }
    }*/
    String loadFile(File file){



        if (!file.exists())
            return "";
        if (!file.canRead())
            return "";

        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader b;
            if (file instanceof ZipList.File2 )
                b = new BufferedReader(new InputStreamReader(((ZipList.File2) file).getInputStream()));
            else
                b = new BufferedReader(new FileReader(file));

            String readLine;
            while ((readLine = b.readLine()) != null) {
                if (stringBuilder.length() > 0)
                    stringBuilder.append('\n');
                stringBuilder.append(readLine);
            }
            try {
                b.close();
            }catch (Throwable t) {
            }
        } catch (IOException e) {
            printError(e.getMessage());
        }
        return stringBuilder.toString();

    }


    String loadLine(File file){
        if (!file.exists())
            return "";
        if (!file.canRead())
            return "";

        try {
            BufferedReader b;
            if (file instanceof ZipList.File2 )
                b = new BufferedReader(new InputStreamReader(((ZipList.File2) file).getInputStream()));
            else
                b = new BufferedReader(new FileReader(file));

            String readLine = b.readLine();
            try {
                b.close();
            }catch (Throwable t) {
            }
            return readLine;
        } catch (IOException e) {
            printError(e.getMessage());
        }
        return null;

    }

    String loadFile(String name){
            File f = new File(name);
            return loadFile(f);
    }

    List<String> fileToArray(String name){
        try {

            File f = new File(name);
            ArrayList<String> arr = new ArrayList<>(5);
            if (!f.exists())
                return arr;
            BufferedReader b;
            if (f instanceof ZipList.File2 )
                b = new BufferedReader(new InputStreamReader(((ZipList.File2) f).getInputStream()));
            else
                b = new BufferedReader(new FileReader(f));


            String readLine;
            while ((readLine = b.readLine()) != null) {
                arr.add(readLine);
            }
            try {
                b.close();
            }catch (Throwable t) {
            }
            return arr;
        } catch (IOException e) {
            printError(e.getMessage());
        }
        return null;
    }

/*
           // View sv = super.getView(position, convertView, parent);

            LayoutInflater inflater= FreeShell.this.getLayoutInflater();
            View view=inflater.inflate(R.layout.mylist, null,true);

            ImageView imageView = (ImageView) view.findViewById(R.id.icon);


            TextView textView = (TextView) view.findViewById(R.id.Itemname);
            String s = this.getItem(position);
            textView.setText(s);

            if (position < dirs.size()) {
                textView.setBackgroundColor(bRose);
                textView.setTextColor(cRose);
                imageView.setImageResource(R.drawable.parent);
                return view;

            }
            File f = files[position - dirs.size()];
            if (f == null){
                textView.setTextColor(cGray);
                textView.setBackgroundColor(bGray);
                return view;
            }
            if (f.isDirectory()) {
                setDirColors(f, textView);

                imageView.setImageResource(R.drawable.dir);
                return view;
            }

            imageView.setImageResource(R.drawable.file);
            paintFileItem(s, f, textView);
            return view;
 */
/*
public static long folderSize(File directory) {
    long length = 0;
    for (File file : directory.listFiles()) {
        if (file.isFile())
            length += file.length();
        else
            length += folderSize(file);
    }
    return length;
}
 */


    final static String[] PERMISSIONS = new String[] {
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE


    };
    boolean started = false;
    void checkPermissions(){
        int i = 0;
        for(String permission: PERMISSIONS){
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { permission },
                        i);
                return;
            }
            i++;
        }
/*        CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeUnavailable(String cameraId) {
                super.onTorchModeUnavailable(cameraId);
            }

            @Override
            public void onTorchModeChanged(String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                flashState = enabled;
            }
        };
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        manager.registerTorchCallback(torchCallback, null);// (callback, handler)*/
        started = true;
        loadProperties();
        svPreview();
        bindAudioService();
        init();
        load();
    }

    int PERMISSION_CALL_PHONE = PERMISSIONS.length;
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode < PERMISSIONS.length) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissions();
                return;
            }else{
                System.exit(-1);
            }
        }
        if (requestCode == PERMISSION_CALL_PHONE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                call();
            }else{

            }
        }

    }
    void okDialog(String title, String m){
        FreeShell.this.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {

                        AlertDialog.Builder builder = new AlertDialog.Builder(FreeShell.this);
                        builder.setTitle(title)
                                .setMessage(m)
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        printLog("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation != FreeShell.this.orientation) {
            FreeShell.this.orientation = orientation;
            DisplayMetrics dm = getResources().getDisplayMetrics();
            displayMetrics = dm;
            if (!started)
                return;
            firstScreen().init();
            //text().setWidth(dm.widthPixels);
            firstScreen().filePath(dm.widthPixels);
            //text().invalidate();

            firstScreen().fresh();
            //listPanel().fresh();
            mainReLayout();
            /*if (preview != null)
                preview.relayout();*/
            baseLayout().invalidate();
        }



        // int orientation = getResources().getConfiguration().orientation;
       // mOrientationListener.onOrientationChanged(orientation);

    }

    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.contains("video");
    }

    boolean isText(String ext) {
        if ((ext == null)||(ext.length()==0))
            return true;
        return sortedTxtFormats.contains(ext.toLowerCase());
    }

    String propertiesFileName(){
        return myDir() + "/freeShell.mtk";
    }


/*    String[] imageformats = new String[]{
        "jpg", "png", "jpeg", "gif", "bmp"};
    HashArray<String> sortedImgFormats;*/
    String[] textformats = new String[]{
    "help", "mtk", "log", "html", "xml", "css", "svg", "json", "properties", "me",
    "java", "pas", "c", "cpp", "h", "cs", "js", "py", "rb", "pl", "php", "sh","txt", "htm", "html", "xml", "sql",
    "ini", "cfg", "bat"};
    HashArray<String> sortedTxtFormats;

    HashArray<String> initFormats(String[] arr){
        HashArray<String> ret = new HashArray<>(String.class, arr.length);
        for(String s: arr){
            ret.put(s);
        }
        return ret;
    }


    String arrayToSimpleString(String[] a){
        StringBuilder sb  = new StringBuilder();
        for(String s: a){
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(s);
        }
        return sb.toString();
    }

    void loadDef(){
        //sortedImgFormats = initFormats(imageformats);
        sortedTxtFormats = initFormats(textformats);
    }
    private HashArray<String> initArrayFromString(String ss) {
        String[] arr = ss.split(",");
        if (arr.length == 0)
            return new HashArray<>();
        HashArray<String> ret = new HashArray<>(String.class, arr.length);
        for(String s: arr){
            ret.addElement(s.trim());
        }
        return ret;
    }

    void loadProperties(){
        createHelp();
        File f = new File(propertiesFileName());
        if (!f.exists()){
            loadDef();
            FileWriter fileWriter = null; //Set true for append mode
            try {
                fileWriter = new FileWriter(f, false);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                printWriter.println("<root>");
                //printWriter.println("imgFormats=" + arrayToSimpleString(imageformats));
                printWriter.println("txtFormats=" + arrayToSimpleString(textformats));
                printWriter.println("</root>");
                try {
                    printWriter.close();
                }catch (Exception e) {
                }
                try{
                    fileWriter.close();
                }catch (IOException e) {
                    log(e);
                }
            } catch (IOException e) {
                log(e);
            }

        }else{
            XParser xParser = mapThreads().openXmlFile(f.getAbsolutePath(), root -> {
                try {
                    run = false;
                    FreeShell.this.runOnUiThread(
                            new Runnable() {
                                public void run() {
                                    if (root == null){
                                        loadDef();
                                        return;
                                    }
/*                                    String s = root.getStringByLink("#imgFormats");
                                    if (notEmpty(s)){
                                        sortedImgFormats = initArrayFromString(s);
                                    }else {
                                        sortedImgFormats = initFormats(imgformats);
                                    }*/
                                    String s = root.getStringByLink("#txtFormats");
                                    if (notEmpty(s)){
                                        sortedTxtFormats = initArrayFromString(s);
                                    }else{
                                        sortedTxtFormats = initFormats(textformats);
                                    }



                                }

                            });
                }catch (Throwable t){
                    loadDef();
                    printError(t);
                }


            }).getXParserImpl();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (run)
                        xParser.getParser().run();

                }
            }).start();

        }

    }





    /*File historyDir;
    String myHistoryDir() {

        if (homeDir != null){
            return homeDir.getAbsolutePath();
        }
        try {
            String myDir = myDir() + "/home";
            homeDir = new File(myDir);
            if (!homeDir.exists()) {
                homeDir.mkdir();
            } else {
                if (!homeDir.isDirectory()) {
                    homeDir.delete();
                }
                homeDir.mkdir();
            }
            return homeDir.getAbsolutePath();
        }catch (Throwable t){
            printError(t);
        }

        return "";


    }*/


    final static String RESERVED = "|\\?*<\":>+[]/'";
    String normalizeFileName(String str){
        StringBuffer s = new StringBuffer(str);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c < 31) || (c == 127) || (RESERVED.indexOf(c) >= 0)) {
                String cc = "#" + (int)c;
                s.insert(i, cc);
                i += cc.length();
            }
        }
        return s.toString();
    }


    void beginOfAction(int actions){
        for(int i = 0; i < filePanels.size(); i++){
            FileAdapter fileAdapter = filePanels.elementAt(i);
            fileAdapter.beginOfAction(actions);
        }
    }
    void progress(int num){
        for(int i = 0; i < filePanels.size(); i++){
            FileAdapter fileAdapter = filePanels.elementAt(i);
            fileAdapter.progress(num);
        }
    }
    void endOfAction() {
        FileAdapter[] array = filePanels.toArray();
        for(FileAdapter fileAdapter: array){
            fileAdapter.endOfAction();
        }
        if (selected != null)
            selected.refresh();

    }

    void google() {
        google(getWord());
    }

    void google(String line) {
        Intent i = null;
        try {
            i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.google.com/search?as_epq=" + URLEncoder.encode(line, "utf-8")));
        } catch (UnsupportedEncodingException e) {
            i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.google.com/search?as_epq=" + URLEncoder.encode(line)));
        }
        Intent chooser = Intent.createChooser(i, "Call");
        startActivityForResult(chooser, 262144);
    }


    public TextAdapter textAdapter(){
        return new TextAdapter();
    }

    TextAdapter openTextFile0(File file, int index){
        TextAdapter adapter = openedTextFile.get(file.getAbsolutePath());
        if (adapter == null){
            TextAdapter textAdapter = textAdapter();
            textAdapter.init(null, file, index);
        }else{
            adapter.show();
        }
        return adapter;
    }

    TextAdapter openTextFile(DirAdapter dirAdapter, File file, int index){
        TextAdapter adapter = openedTextFile.get(file.getAbsolutePath());
        if (adapter == null){
            TextAdapter textAdapter = textAdapter();
            textAdapter.init(dirAdapter, file, index);
        }else{
            adapter.show();
        }
        return adapter;
    }

    class TextAdapter extends Adapter{
        File file;
        HashMap<Item, Integer> selectedText = new HashMap();

        @Override
        public void setDefaultText(){
            if (notEmpty(filteredWord))
                info0.setText("Filter: " + filteredWord);
            else {
                if (file != null)
                    info0.setText(file.getAbsolutePath());
                else {
                    log("Empty file");
                    info0.setText("Empty file");
                }
            }
            info0.invalidate();
        }

        @Override
        public void save(PrintWriter writer){
            if (fileAdapter != null){
                File zip = fileAdapter.getBaseZip();
                if (zip != null) {
                    writer.println("zip=" + zip.getAbsolutePath());
                }
            }
            if (file != null) {
                writer.println("file=" + file.getAbsolutePath());
                writer.println("lastModified=" + file.lastModified());
            }
            if (fileAdapter != null) {
                if (!fileAdapter.closed)
                    writer.println("filePath=" + fileAdapter.getDir());
            }
            super.save(writer);

        }
        String loadedFile;
        String loadedZip;
        @Override
        public boolean load(Tag tag){
            String f = tag.getStringByLink("#zip");
            if (notEmpty(f)) {
                loadedZip = f;
            }
            f = tag.getStringByLink("#file");
            if (notEmpty(f)) {
                loadedFile = f;
            }
            return super.load(tag);

        }

        Runnable setEditedLine;

        File createFile(){
            if (notEmpty(loadedFile)){
                if (notEmpty(loadedZip)){
                    ZipAdapter zipAdapter = getZipAdapter(loadedZip);
                    if (zipAdapter == null){
                        openZipAdapter(loadedZip);
                        zipAdapter = getZipAdapter(loadedZip);
                    }
                    if (zipAdapter != null){
                        TextAdapter.this.fileAdapter = zipAdapter;
                        return zipAdapter.getStorageList().findFile(loadedFile);
                    }
                }else{
                    return new File(loadedFile);
                }
            }
            return null;
        }

        @Override
        public void start(Tag tag) {
            if (!notEmpty(loadedFile))
                return;
            file = createFile();
            if (file == null){
                close();
                return;
            }
            Tag editTag = tag.getTagByLink("@edit");
            if (editTag != null) {
                String editFile = editTag.getStringByLink("#editFile");
                if (notEmpty(editFile)) {

                    int line = editTag.getIntByLink("#line");
                    int index = editTag.getIntByLink("#index");
                    if (listView().index == index) {

                        if (editFile.equals(loadedFile)) {
                            String f = tag.getStringByLink("#lastModified");
                            if (notEmpty(f)) {
                                setEditedLine = new Runnable() {
                                    @Override
                                    public void run() {

                                        try {

                                            if (file.lastModified() == Long.parseLong(f)) {
                                                listView().allHeight();
                                                if (line >= items.size()) {
                                                    errLoadDialog(line, editFile);;
                                                    return;
                                                }
                                                //firstScreen().editItem(items.get(line), file);
                                                Item item = (Item) items.get(line);
                                                firstScreen().edited = item;
                                                firstScreen().editedFile = file;
                                                firstScreen().setLine(item, file);
//                                                firstScreen().setEditFileName(file.getParent(), file.getName());
                                                if (notEmpty(loadedZip)){
                                                    TextAdapter.this.fileAdapter = getZipAdapter0(file.getParent());
                                                }else{
                                                    TextAdapter.this.fileAdapter = getAdapter0(file.getParent());
                                                }
                                                if (loaded)
                                                    mainReLayout();
                                                return;
                                            }else{
                                                okDialog("Warning", file.getAbsolutePath() + " has been changed, the above you see the old not associated text.");
                                            }
                                        } catch (Throwable t) {
                                            printError(t);
                                        }

                                    }
                                };
                            }
                        }

                    }
                }
            }

            init(fileAdapter, file, listView.index);




        }

        void errLoadDialog(int line, String name){
            okDialog("Warning", "While the last session you edited " +
                    "line #"
                    + line + " from file: "
                    + name + " You can see your text in edit field from, but now " +
                    "it is not associated because the file was modified. " +
                    "You can tap 'Copy to clipboard' button, select that " +
                    "line again from file and paste edited text.");

        }

        private void showPopupMenu(final Item mainItem) {
            PopupMenu popupMenu = new PopupMenu(freeShell, mainItem.settingsButton);
            popupMenu.inflate(R.menu.textmenu);

            popupMenu
                    .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            ClipData clip;
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                            switch (item.getItemId()) {
                                case R.id.copy:
                                    clip("Line " + mainItem.globalPosition + " " + file.getAbsolutePath(), mainItem.toString());
                                    return true;
                                case R.id.cut:
                                    clip("Line " + mainItem.globalPosition + " " + file.getAbsolutePath(), mainItem.toString());
                                case R.id.delete:
                                    try {
                                        removeItem(mainItem);
                                    } catch (Exception e) {
                                        printError(e);
                                    }
                                    showList(allItems, mainItem.globalPosition);
                                    save();
                                    return true;
                                case R.id.google:
                                    FreeShell.this.google(mainItem.toString());
                                    return true;
                                case R.id.pasteBefore:
                                    clip = clipboard.getPrimaryClip();
                                    if(clip == null || clip.getItemCount() == 0)
                                        return true;
                                    String t = clip.getItemAt(0).getText().toString();
                                    allItems.add(mainItem.globalPosition, t);
                                    showList(allItems, mainItem.globalPosition);
                                    save();
                                    return true;
                                case R.id.pasteAfter:
                                    clip = clipboard.getPrimaryClip();
                                    if(clip == null || clip.getItemCount() == 0)
                                        return true;
                                    t = clip.getItemAt(0).getText().toString();
                                    allItems.add(mainItem.globalPosition + 1, t);
                                    showList(allItems, mainItem.globalPosition);
                                    save();
                                    return true;
                                case R.id.newBefore:
                                    allItems.add(mainItem.globalPosition, getLine());
                                    showList(allItems, mainItem.globalPosition);
                                    save();
                                    return true;
                                case R.id.newAfter:
                                    allItems.add(mainItem.globalPosition + 1, getLine());
                                    showList(allItems, mainItem.globalPosition);
                                    save();
                                    return true;
                                default:

                                    return false;
                            }
                        }
                    });

            popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    Toast.makeText(getApplicationContext(), "onDismiss",
                            Toast.LENGTH_SHORT).show();
                }
            });
            popupMenu.show();
        }

        private void removeItem(Item mainItem) throws Exception {
            selectedText.remove(mainItem);
            mainItem.remove();
            allItems.remove(mainItem.globalPosition);
            if (current != allItems) {
                int pos = getCurrentPosition(mainItem.globalPosition);
                current.remove(pos);
            }
        }


        String getWholeText(){
            StringBuffer stringBuffer = new StringBuffer("");
            for(Object s: allItems){
                if (stringBuffer.length() > 0)
                    stringBuffer.append('\n');
                stringBuffer.append(s.toString());
            }
            return stringBuffer.toString();
        }

        void changeLine(int line, String text) throws Exception {
            Item item = items.get(line);
            View mView = item.panel;
            allItems.set(line, text);

            int h0 = mView.getMeasuredHeight();
            item.textView.setText(text);
            item.format();
            mView.measure(
                    makeMeasureSpec,

                    makeMeasureSpec);
            int h1 = mView.getMeasuredHeight();
            save();
            if (h0 != h1){
                    mainReLayout();
                /*listView.allHeight = null;
                listView.relayoutList();
                listView.panel.invalidate();*/
            }
        }

        void save(){
            final String s = getWholeText();
            if (file instanceof ZipList.File2){
                final ZipList.File2 zip = (ZipList.File2)file;
                firstScreen().checkEditedFile(new SetFile() {
                    @Override
                    public void setFile(File f) {
                        fSync().writeFile(f, s);
                        Toast.makeText(FreeShell.this, f + " was saved.", Toast.LENGTH_LONG).show();
                        int index = TextAdapter.this.listView().index;
                        TextAdapter.this.close();
                        openTextFile0(f, index);
                    }
                }, zip.getZipParentPath(), firstScreen().getFileName(), text().getText().toString());
                return;
            }
            fSync().writeFile(file, s);
            checkVariables();
        }
        void checkVariables(){
            if (file.getAbsolutePath().equals(variablesFileName())) {
                variables = loadVariables();
            }
        }
        void setFile(File f){
            file = f;
            if (isEdit()){
                firstScreen().setEditFileName(file.getParent(), file.getName());

                mainReLayout();
            }
            checkVariables();
        }

        void save_as(){
            String s = getWholeText();
            if (fileAdapter != null) {
                File zip = fileAdapter.getBaseZip();
                if (zip != null){
                    firstScreen().checkEditedFile(this::setFile, zip.getParent(), getWord(), s);
                    return;
                }
            }
            firstScreen().checkEditedFile(this::setFile, file.getParent(), getWord(), s);
        }


        void del(){
            fSync().del(file);
            close();
        }



        void editAll(){

            firstScreen().editItem(this);
        }


        void removeSelected(){
            Item items[] = new Item[selectedText.size()];
            selectedText.keySet().toArray(items);
            for(Item item: items){
                try {
                    selectedText.remove(item);
                    removeItem(item);
                } catch (Exception e) {
                    printError(e);
                }

            }
            save();
            refresh();

        }

        void clipboardSelected(){
            Item items[] = new Item[selectedText.size()];
            selectedText.keySet().toArray(items);
            StringBuilder sb = new StringBuilder();
            for(Item item: items){
                sb.append(item.toString() + '\n');
                selectedText.remove(item);
                item.format();
            }
            clip("Lines from" + file.getName(), sb.toString());
        }
        void printSelected(){
            Item items[] = new Item[selectedText.size()];
            selectedText.keySet().toArray(items);
            for(Item item: items){
                pressChar(item.toString() + '\n');
                selectedText.remove(item);
                item.format();
            }
        }





        TextAdapter(){
            create0(0, 0);
          /*  new ShellButton(Html.fromHtml("saveMtk"),
                    this::saveMtk,
                    cGreen, bGreen, 0, buttonPanel);*/
            new ShellButton(Html.fromHtml("Save file as"),
                    this::save_as,
                    cGreen, bGreen, 1, buttonPanel);
            new ShellButton(Html.fromHtml("Edit all text"),
                    this::editAll,
                    cCyan, bCyan, 1, buttonPanel);
            new ShellButton(Html.fromHtml("Clipboard all"),
                    this::copyAll,
                    cGreen1, bGreen1, 1, buttonPanel);
            new ShellButton(Html.fromHtml("Clipboard selected"),
                    this::clipboardSelected,
                    cSel1, bSel1, 0, buttonPanel);
            new ShellButton(Html.fromHtml("Remove selected"),
                    this::removeSelected,
                    cSel1, bSel1, 0, buttonPanel);
            new ShellButton(Html.fromHtml("Remove selected"),
                    this::removeSelected,
                    cSel1, bSel1, 0, buttonPanel);

            new ShellButton(Html.fromHtml("<small>Print selected text</small>"),
                    this::printSelected,
                    cSel1, bSel1, 1, buttonPanel);


            new ShellButton(Html.fromHtml("Delete file"),
                    this::del,
                    cRed, bRed, 0, buttonPanel);

            postCreate();
        }

        private void copyAll() {
            clip(file.getAbsolutePath(), loadFile(file));
        }

        @Override
        public Item getView(int position, int filteredPosition) {
            Item item = new Item(position){
                @Override
                boolean isSelect(){
                    return selectedText.containsKey(this);
                }

                @Override
                void remove(){
                    selectedText.remove(this);
                    super.remove();
                }

            };
            String s = current.get(filteredPosition).toString();

            View.OnClickListener open = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        firstScreen().editItem(item, file);
                        mainReLayout();
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }
//                    item.textView.requestFocus();
                }
            };
            View.OnClickListener settings;

                settings = new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        try{
                            showPopupMenu(item);
                        }catch (Exception e){
                            FreeShell.this.printError(e);
                        }
                    }
                };

            View.OnClickListener select = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if (!selectedText.containsKey(item)){
                            selectedText.put(item, position);
                        }else{
                            selectedText.remove(item);
                        }
                        item.format();
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }
                }
            };

                item.init(null, this, null, listW, open, settings, select, null, null);
                item.setNormalColors(cBlue, bSel1);
                item.textView.setText(s);
            return item;
        }

/*        Integer filePos;
        String filePath;*/
        DirAdapter fileAdapter;

        boolean isEdit(){
            if (firstScreen().edited != null)
                if (firstScreen().edited.adapter == this)
                    return true;
            return false;


        }

        @Override
        void close(){
            if ((fileAdapter == null)||(fileAdapter.closed)){
                if (file != null) {
                    try {
                        fileAdapter = getAdapter(file.getParent());
                    }catch (Throwable t){

                    }
                }
            }

            if (isEdit()){
                firstScreen.wholeClearText();
                firstScreen.setDefaultFileName();
            }
            int index = listView.index;
            if (file != null)
                openedTextFile.remove(file.getAbsolutePath());
            super.close();
            if (fileAdapter != null) {
                if (!fileAdapter.closed)
                    fileAdapter.show();
                /*FileAdapter fileAdapter = new FileAdapter();
                fileAdapter.init(index);
                fileAdapter.openSomeDir(new File(filePath));
                fileAdapter.listView.setSelection(filePos);*/

            }
            mainReLayout();

        }


        void init(DirAdapter fileAdapter, File file, int index){
            this.fileAdapter = fileAdapter;
            this.file = file;
            openedTextFile.put(file.getAbsolutePath(), this);
            init(index);
            new Thread(new Runnable() {
                @Override
                public void run() {

                    ArrayList<Object> ret = new ArrayList<>(32);
                    if (!file.exists())
                        return;
                    if (!file.canRead())
                        return;

                    try {
                        BufferedReader b;
                        if (file instanceof ZipList.File2 )
                            b = new BufferedReader(new InputStreamReader(((ZipList.File2) file).getInputStream()));
                        else
                            b = new BufferedReader(new FileReader(file));
                        String readLine;
                        while ((readLine = b.readLine()) != null) {
                            ret.add(readLine);
                        }
                        try {
                            b.close();
                        }catch (Throwable t) {
                        }
                        if (ret.size() == 0){
                            ret.add("");
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showList(ret, 0);
                                if (setEditedLine != null){
                                    setEditedLine.run();
                                    setEditedLine = null;
                                }

                            }
                        });

                    } catch (IOException e) {
                        printError(e.getMessage());
                    }


                }
            }).start();
        }

        @Override
        void showFirst() {
            init(0);
        }
        @Override
        void refresh(){

            init(fileAdapter, file, listView.index);
            mainReLayout();

        }



        @Override
        public String getName() {
            return "textAdapter";
        }

    }


    void openFileIntent(File f){
        if (f == null)
            return;
        if (f.isDirectory()) {
            openAdapter(f.getAbsolutePath());
            return;
        }
        String ext = getFileExt(f.getName());

        //Uri uri = Uri.fromFile(f);
        Intent intent = getIntentByExt(ext);
        if (intent == null) {
            return;
        }
        //Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        //intent.setDataAndType(uri, intent.getType());//"image/*"
        //}, cPhoto, bPhoto);*/
/*
String ext = getFileExt(f.getName());
Intent newIntent = getIntentByExt(ext);

newIntent.setData(Uri.fromFile(f));
newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/
        try {



            List<ResolveInfo> resInfoList = FreeShell.this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (f.exists()) {
                //                                Uri uri = Uri.fromFile(file);
                Uri uri = FileProvider.getUriForFile(FreeShell.this,
                        "mtk.free.shell", f);

                if (uri != null) {
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setDataAndType(uri, intent.getType());
                    try {

                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            FreeShell.this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                    } catch (Throwable t) {
                        printError(t);
                    }
                }
            }

            Intent chooser = Intent.createChooser(intent, "Open");
            startActivityForResult(chooser, 262144);
        } catch (ActivityNotFoundException e) {
            starts.put(ext, null);
            printError("No handler for this type of file: " + f);
        }

    }

    class Contact2 {
        String text;
        List<String> phones;
        List<String> emails;

        String getText(String s) {
            int i = text.indexOf(s);
            if (i < 0)
                return text;
            String ret = "";
            if (i > 0)
                ret = text.substring(i);
            int end = i + s.length();
            if (end < text.length())
                ret = ret + text.substring(end, text.length());
            return ret;
        }
    }
    Contact2 extractValues() {
        return extractValues(getWholeText());
    }
    public static List<String> getEmails(StringBuffer buf) {
        String line = buf.toString();
        final String RE_MAIL = "([\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Za-z]{2,4})";
        Pattern p = Pattern.compile(RE_MAIL);
        Matcher m = p.matcher(line);
        IntIntTable t = new IntIntTable(8);
        ArrayList<String> ret = new ArrayList<String>(1);
        while (m.find()) {
            String email = m.group(1);
            if (isValidEmailAddress(email)) {
                int start = m.start(1);
                int end = m.end(1);
                if (!ret.contains(email)) {
                    ret.add(email);
                    t.put(start, end);
                }
            }
        }
        for(int i = t.size() - 1; i >= 0; i--){
            int start = t.code().elementAt(i);
            int end = t.value().elementAt(i);
            buf.delete(start, end);

        }
        return ret;

    }

    Contact2 extractValues(String s) {
        Contact2 contact = new Contact2();
        StringBuffer withoutEmails = new StringBuffer(s);
        List<String> emails = getEmails(withoutEmails);//s
/*        for (int j = 0; j < emails.size(); j++) {
            String email = emails.get(j);
            int start = 0;
            while (true) {
                int i = withoutEmails.indexOf(email, start);
                if (i < 0)
                    break;

                if (i > 0) {
                    withoutEmails.delete(i, i + email.length());
                    start = i;
                }else {
                    start = i + email.length();
                }
            }

        }*/
        ArrayList<String> phones = new ArrayList<String>(1);
        StringBuffer text = new StringBuffer(withoutEmails.toString());
        int begin = -1, i = 0, offset = 0;
        for (; i < withoutEmails.length(); i++) {
            final char c = withoutEmails.charAt(i);
            switch (c) {
                case '+':
                    if (begin < 0) {
                        begin = i;
                    } else {
                        text = text.delete(begin - offset, i - offset);
                        offset += i - begin;
                        addPhone(phones, withoutEmails, begin, i);
                        begin = -1;
                    }
                    break;
                case '*':
                case '#':
                    if (begin < 0) {
                        begin = i;
                    }
                case '-':
                    break;
                default:
                    if (Character.isDigit(c)) {
                        if (begin < 0) {
                            begin = i;
                        }
                    } else {
                        if (begin > -1) {
                            text = text.delete(begin - offset, i - offset);
                            offset += i - begin;
                            addPhone(phones, withoutEmails, begin, i);
                            begin = -1;
                        }
                    }
        }

        }
        if (begin > -1) {
            text = text.delete(begin - offset, i - offset);
            addPhone(phones, withoutEmails, begin, i);
        }
        if (emails.size() > 0) {
            contact.emails = emails;
        }
        if (phones.size() > 0) {
            contact.phones = phones;
        }
        contact.text = text.toString();

        return contact;

    }

    void newContact0(){
        try {

            Contact2 contact = extractValues();
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            ArrayList<ContentValues> data = new ArrayList<ContentValues>();
            boolean parcelable = false;
            if (notEmpty(contact.phones)) {
                if (contact.phones.size() > 1) {
                    parcelable = true;
                    for (int i = 0; i < contact.phones.size(); i++) {
                        ContentValues row = new ContentValues();
                        row.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                        row.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.phones.get(i));
                        row.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
                        data.add(row);
                    }
                } else {
                    if (contact.phones.size() == 1)
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, contact.phones.get(0));
                }
            }
            if (notEmpty(contact.emails)) {
                if (contact.emails.size() > 1) {
                    parcelable = true;
                    for (int i = 0; i < contact.emails.size(); i++) {
                        ContentValues row = new ContentValues();
                        row.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
                        row.put(ContactsContract.CommonDataKinds.Email.ADDRESS, contact.emails.get(i));
                        row.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_OTHER);
                        data.add(row);
                    }
                } else {
                    if (contact.emails.size() == 1)
                        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, contact.emails.get(0));
                }
            }

            if (notEmpty(contact.text))
                intent.putExtra(ContactsContract.Intents.Insert.NAME, contact.text);

            if (parcelable)
                intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);


            freeShell.startActivityForResult(intent, 262144);
        }catch (Throwable t){
            printError(t.getMessage());
        }

    }
    void newContact(){
            if (!notEmpty(getWholeText().trim())){
                AlertDialog.Builder builder = new AlertDialog.Builder(FreeShell.this);
                builder.setTitle("Warning")
                        .setMessage("You could form new contact in edit field from any words and as many phone numbers and email addresses as you like.")
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setNeutralButton("Add empty contact", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                newContact0();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            }
            newContact0();
    }


    public  void unzip(File file, FileAdapter fileAdapter){
        String nameDir = getFileName(file.getName());
        unzip(nameDir, file, fileAdapter);
    }

    void unzip(File dir, File file, FileAdapter fileAdapter){
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            fSync().unzip(file, dir, fileAdapter);
        } catch (IOException e) {
            freeShell.printError(e);
        }

    }

    public  void unzip(final String nameDir, File file, FileAdapter fileAdapter){
        String path = file.getParent();
        AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
        final EditText edittext = new EditText(FreeShell.this);
        edittext.setText(path + "/" + nameDir);
        alert.setMessage("Please enter a file name");
        alert.setTitle("Unzip to new directory");

        alert.setView(edittext);

        alert.setPositiveButton("Unzip", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try{
                    //What ever you want to do with the value
                    final String nameDir2 = edittext.getText().toString();
                    File dir = new File(nameDir2);
                    if (dir.exists()) {
                        if (dir.isDirectory()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                            // Specify the dialog is not cancelable
                            builder.setCancelable(false).setTitle(nameDir2 + " already exists. Extract to existing files?").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // what ever you want to do with No option.
                                }
                            }).setNeutralButton("Change name", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    unzip(nameDir, file, fileAdapter);
                                    // what ever you want to do with No option.
                                }
                            }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    unzip(dir, file, fileAdapter);
                                    // what ever you want to do with No option.
                                }
                            });

                            AlertDialog dialog2 = builder.create();
                            // Display the alert dialog on interface
                            dialog2.show();

                            return;

                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                            // Specify the dialog is not cancelable
                            builder.setCancelable(false).setTitle(nameDir2 + " already exists.").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // what ever you want to do with No option.
                                }
                            }).setNeutralButton("Change name", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    unzip(nameDir, file, fileAdapter);
                                    // what ever you want to do with No option.
                                }
                            });
                            ;


                            AlertDialog dialog2 = builder.create();
                            // Display the alert dialog on interface
                            dialog2.show();

                            return;
                        }
                    }
                    unzip(dir, file, fileAdapter);

                    //listView.invalidate();
                }catch (Exception e){
                    FreeShell.this.printError(e);
                }

            }
        });
        alert.setNeutralButton("View archive", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.yield();
                            }
                            FreeShell.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    newZipPanel(file, fileAdapter);
                                }
                            });

                        }
                    }).start();

                    return;
                }catch(Exception t){
                    printError(t);
                }

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alertShow(alert, edittext);



    }



    String geErrorsFileName(String path){
        return getUniqueName(path, getOnlyTime(), "log");
    }
    String getErrorFilePath(){
        String s = myErrorsDir() + "/" + getOnlyDate();
        File f = new File(s);
        if (!f.exists()){
            f.mkdir();
        }
        return s;
    }
    String myErrorsDir() {

        try {
            String myDir = myHistoryDir() + "/errors";
            File errorsDir = new File(myDir);
            if (!errorsDir.exists()) {
                errorsDir.mkdir();
            } else {
                if (!errorsDir.isDirectory()) {
                    errorsDir.delete();
                }
                errorsDir.mkdir();
            }
            return errorsDir.getAbsolutePath();
        }catch (Throwable t){
            printError(t);
        }

        return "";


    }

    void showLog(StringBuilder s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                File f = errorFile();
                fSync().writeFile(f, s.toString());
                openTextFile(null, f, 0);

            }
        });
    }

    File errorFile(){
        String path = getErrorFilePath();
        String fileName = geErrorsFileName(path);
        File errorFile = new File(path + "/" + fileName);
        if (!errorFile.exists()) {
            try {
                errorFile.createNewFile();
            } catch (IOException e) {
                Log.e("FreeShell", "Error while create log", e);
                errorFile = null;
            }
        }
        return errorFile;
    }


    PrintWriter logWriter;
    synchronized PrintWriter log(){
        if (logWriter == null){
            try {
                if (logFile() == null)
                    return null;
                FileWriter fileWriter = new FileWriter(logFile, true); //Set true for append mode
                logWriter = new PrintWriter(fileWriter);
            } catch (Exception e) {
                printMessage("can't create errors.log file: " + e.getMessage());
            }
        }
        return logWriter;
    }


    File logFile;
    File logFile(){
        if (logFile == null) {
            String s = myDir() + "/FreeShell.log";
            logFile = new File(s);
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    Log.e("FreeShell", "Error while create log", e);
                    logFile = null;
                }
            }

        }
        return logFile;
    }

    void sendLog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
        final EditText edittext = new EditText(FreeShell.this);
        edittext.setText("Hi! ");
        edittext.setSelection(edittext.length());
        alert.setMessage("Please write your problem details");
        alert.setTitle("Send error log");

        alert.setView(edittext);

        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                try{
                    final String hello = edittext.getText().toString();
                    sendLog(hello);
                }catch (Exception e){
                    FreeShell.this.printError(e);
                }
            }
        });
        alert.setNeutralButton("Clear text", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                sendLog("Hi! ");
                return;
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alertShow(alert, edittext);

    }
    private static final int BUFFER_SIZE = 1024;
    public void zipDir(String zipFile, String... files) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        byte data[] = new byte[BUFFER_SIZE];
        try {
            for(String fileName: files) {
                File file = new File(fileName);
                if (!file.exists())
                    continue;;
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(file.getName());
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    origin.close();
                }
            }
        }
        finally {
            out.close();
        }
    }

    synchronized void closeLog(){
        try {
            if (logWriter != null) {
                logWriter.close();
                logWriter = null;
            }
        }catch (Throwable t){

        }
    }
    void sendLog(String hello){
        try {
            closeLog();
            String m = hello;
            if ((logFile() == null) || (!logFile.exists())) {
                m = m + " FreeShell.log not found in ";
            }
            String zipName = myDir() + "/FreeShellLog.zip";
            File zip = new File(zipName);
            if (zip.exists()){
                zip.delete();
            }
            zipDir(zip.getAbsolutePath(), logFile.getAbsolutePath(), lastStateFileName(), variablesFileName(), propertiesFileName());
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            // The intent does not have a URI, so declare the "text/plain" MIME type
            emailIntent.setType("text/plain; charset=utf-8");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"hubblebubble0x3@gmail.com"}); // recipients
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FreeShell log");
            emailIntent.putExtra(Intent.EXTRA_TEXT, hello);

            if ((zip != null) && (zip.exists())) {
                Uri uri = FileProvider.getUriForFile(FreeShell.this,
                        "mtk.free.shell", zip);
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                try {
                    List<ResolveInfo> resInfoList = FreeShell.this.getPackageManager().queryIntentActivities(emailIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        FreeShell.this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }catch (Throwable t){
                    printError(t);
                }
            }

            Intent chooser = Intent.createChooser(emailIntent, "send email");
            startActivityForResult(chooser, 262144);

        }catch (Exception e){
            okDialog("Errors while send error log", e.getMessage());
            error(e);
        }

// You can also attach multiple items by passing an ArrayList of Uris

    }

    synchronized void error(Throwable e) {
        Log.e("FreeShell", e.getMessage(), e);
        if (e instanceof OutOfMemoryError){
            System.gc();
        }
        try {
            PrintWriter logWriter = log();
            if (logWriter == null)
                return;
            String time = new SimpleDateFormat("yyyy MMM dd EEE HH:mm:ss", Locale.ENGLISH).format(new Date());
            logWriter.println(time + " error " + e.getMessage());
            e.printStackTrace(logWriter);
            //closeLog();
        }catch (Throwable t){
            Log.e("FreeShell", "Errors while write errors.log", t);
        }

    }


    public void printError(Throwable error){
        error(error);
        okDialog("Error", error.getMessage());

    }
    synchronized void printError(String message) {
        if (message != null)
            Log.e("FreeShell", message);
        try{
            PrintWriter logWriter = log();
            if (logWriter == null)
                return;
            String time = new SimpleDateFormat("yyyy MMM dd EEE HH:mm:ss", Locale.ENGLISH).format(new Date());
            logWriter.println(time + " error " + message);
            //closeLog();
            okDialog("Error", message);
        }catch (Throwable t){
            Log.e("FreeShell", "Errors while write errors.log", t);
        }

    }


    void log(Throwable t) {
        printError(t.getMessage());
    }


    synchronized void printLog(String message) {
        Log.i("FreeShell", message);
        try{
            PrintWriter logWriter = log();
            if (logWriter == null)
                return;
            String time = new SimpleDateFormat("yyyy MMM dd EEE HH:mm:ss", Locale.ENGLISH).format(new Date());
            logWriter.println(time + " info " + message);
            //closeLog();
        }catch (Throwable t){
            Log.e("FreeShell", "Errors while write errors.log", t);
        }
    }


    void log(String s) {
        printLog(s);
    }


    void email() {
        email(extractEmails());
    }


    void email(Contact contact) {
        try {
            int count = contact.addr.size();
/*            if (count == 0){

                AlertDialog.Builder builder = new AlertDialog.Builder(FreeShell.this);
                builder.setTitle("Address not found")
                        .setMessage("You may enter any any valid e-mails in upper field and if you want any text of message. Subject will be form from first two words. All selected files (not folders) will be attached.")
                        .setCancelable(true)
                        .setPositiveButton("Send anyway", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                email2(contact);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

                return;
            }*/
            email2(contact);
        }catch (Throwable t){
            printError(t);
        }
    }

    void email2(Contact contact){
        List<File> list = fSync().popSelectedListFroEmail();
        sendEmail(contact.addr.toArray(new String[contact.addr.size()]), contact.text, list);

    }

    boolean notEmpty(Object[] o) {
        return o != null && o.length > 0;
    }


    void sendEmail(String[] emails, String text, List<File> list){
        try{
            String title = "";
            String[] words = text.split(" ");

            if (notEmpty(words)){
                Array<String> ww = new Array(words, words.length);
                int count = 0;
                for(int i = 0; i <ww.size(); i++){
                    if (notEmpty(ww.elementAt(i).trim())){
                        title = title + " " + ww.elementAt(i);
                        count++;
                        if (count == 2)
                            break;
                    }
                }
            }
            if (title.isEmpty()){
                title = text;
                if (title.length() > 32){
                    title = title.substring(0, 32);
                }
            }
            Intent emailIntent;
            if (notEmpty(list) && (list.size() > 1))
                emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
             else
                emailIntent = new Intent(Intent.ACTION_SEND);
            // The intent does not have a URI, so declare the "text/plain" MIME type
            emailIntent.setType("text/plain; charset=utf-8");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, emails); // recipients
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            emailIntent.putExtra(Intent.EXTRA_TEXT, text);
            if (notEmpty(list)) {
                List<ResolveInfo> resInfoList = FreeShell.this.getPackageManager().queryIntentActivities(emailIntent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list.size() == 1) {
                    emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    File file = list.get(0);
                    if (file.exists()) {
                        //                                Uri uri = Uri.fromFile(file);
                        Uri uri = FileProvider.getUriForFile(FreeShell.this,
                                "mtk.free.shell", file);

                        if (uri != null) {
                            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            try {

                                for (ResolveInfo resolveInfo : resInfoList) {
                                    String packageName = resolveInfo.activityInfo.packageName;
                                    FreeShell.this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }
                            } catch (Throwable t) {
                                printError(t);
                            }
                        }
                    }
                }else{
                    ArrayList<Uri> uris = new ArrayList<Uri>();
                    for (File file : list) {
                        Uri uri = FileProvider.getUriForFile(FreeShell.this,
                                "mtk.free.shell", file);

                        if (uri != null) {
                            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            try {

                                for (ResolveInfo resolveInfo : resInfoList) {
                                    String packageName = resolveInfo.activityInfo.packageName;
                                    FreeShell.this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }
                            } catch (Throwable t) {
                                printError(t);
                            }
                        }
                        uris.add(uri);
                    }
                    emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

                    //convert from paths to Android friendly Parcelable Uri's

                }
            }
            Intent chooser = Intent.createChooser(emailIntent, "send email");
            startActivityForResult(chooser, 262144);
        }catch (Exception e){
            FreeShell.this.printError(e);
        }

    }
    private boolean notEmpty(List list) {
        return (list != null) && (list.size() > 0);
    }
    private boolean isEmpty(List list) {
        return (list == null) || (list.size() == 0);
    }

    private void help() {
        createHelp();
        openAdapter(myHelpDir());
    }

    private void clearLog() {
        try {
            if (logWriter != null) {
                logWriter.close();
                logWriter = null;
            }
            File logFile = new File(myDir() + "/FreeShell.log");
            if (logFile.exists()) {
                FreeShell.this.deleteFile(logFile);
            }
        }catch (Throwable t){
            error(t);
        }

    }

    private void clearHistory() {

        FreeShell.this.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        String m = "Clear all history?";
                        AlertDialog.Builder builder = new AlertDialog.Builder(FreeShell.this);
                        builder.setTitle("Clear history")
                                .setMessage(m)
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        clearLog();
                                        try {

                                            String sir = myHistoryDir();
                                            File f = new File(sir);
                                            if (f.exists()) {
                                                String path = firstScreen().getHistoryFilePath();
                                                f.delete();
                                                f = new File(path);
                                                if (!f.exists())
                                                    f.mkdirs();

                                            }
                                        }catch (Throwable t){
                                            error(t);
                                        }

                                        firstScreen().setDefaultFileName();
                                        mainReLayout();
                                    }
                                }).setNeutralButton("Manual removal", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearLog();
                                openAdapter(myHistoryDir());
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

        }


    void mainHelp(){
        FreeShell.this.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        String m = "(c)2018 Mikhail Kravchenko, mtkravchenko@gmail.com";
                        AlertDialog.Builder builder = new AlertDialog.Builder(FreeShell.this);
                        builder.setTitle("About FreeShell")
                                .setMessage(m)
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })                                .setNeutralButton("Read help", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                help();
                            }
                        })                                .setNegativeButton("Send log", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendLog();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

    }

/*    private void fileHelp() {
        createHelp();
        okDialog("Help", "Your may start button home on upper panel, find 'file_manager.help' in file list and open it.");

    }


    private void contactHelp() {
        createHelp();
        okDialog("Help", "Your may click button 'home' on upper panel, find 'call_sms_calculator.help' in file list and open it.");
    }*/


    void createHelp(){
        String p = myHelpDir();
        String h00 = p + "/About all.txt";
        File f00 = new File(h00);
        if (!f00.exists()){
            fSync().writeFile0(f00, "The three general purpose this shell:\n" +
                    "1. The most quickest reaction for any unforeseen situation. " +
                    "The shell focused on working with short texts in the top edit field for any " +
                    "operations, such as calculating, sms, email, add contact or notice.  " +
                    "Difference functions  get word, line or whole text from this field.\n" +
                    "2. Simple access to complete control of operating system and possibility of " +
                    "grouping applications use all features which provided standard file system. " +
                            "You can create links to applications from the full list of applications " +
                            "and sort them according to your directories. Also, you can easily start "+
                            "playing mp3 files from your directory and open any files.\n" +

            "3. Extra safety surfing on the go. So many people often stumble and crashed " +
                    "into a pillar because their device block their field vision. " +
                    "Therefore, it is important for their to see the real world through " +
                    "the device.\n" +
                    "Calm from the permanently incoming messages by minimizing their uncontrolled " +
                    "showing. By running standard applications from in your 'home' or other folder " +
                    "you choose that you are interested in at the moment - messages from relatives or " +
                    "spam. According FreeShell philosopy,  by default you are not interested " +
                    "in anything because you are focused.\n" +
                    "Mikhai Kravchenko.");
        }
//                        if (name.equals("√(5*5)=5 °(pi)=180 sin(90°)=1 ) {
//                            x = BigDecimal.valueOf(Math.sqrt(x.doubleValue())); // exponentiation
//                        }else if (name.equals("°")) {
        String h0 = p + "/favorite apps.txt";
        File f0 = new File(h0);
        if (!f0.exists()){
            fSync().writeFile0(f0, "Your may select any application in 'app' window and paste here and to any directory too, for easy access. " +
                    "Also you can open any file from file manager in any associated application. If it's a text or music, you can open it in other " +
                    "application from left menuitem. Button '...' on the right side opens the application settings." );
        }
        String h1 = p + "/" + "file manager.txt";
        File f1 = new File(h1);
        if (!f1.exists()){
            fSync().writeFile0(f1, "In any file panel the parent directories placed at " +
                    "top in accordance with hierarchy, in this way current directory " +
                    "is last with \"<parent>\" label, after them you can see all directories, " +
                    "with label \"<dir>\", and then files. Sorting does only  inside that groups." +
                    "The shell is storing all selected paths and not forget them when you leave. " +
                    "Also, you can mark on and off any subdirectories and files inside selected directory. " +
                    "There are panel \"selected\" for inspection selected paths. When you restore panels " +
                    "by button \"restore state\" or by opening *.shell files after saved any state, the " +
                    "system warn about selected files.\n You may filter by subtext the items from any panel. " +
                    "For file panels you may also find files by subtext in it names for all subfolders " +
                    "and may find some text in texts files. The text file extensions there are in file " +
                    "freeShell.mtk in the parent directory for home.");

        }
        String h2 = p + "/call, sms, email and calculator.txt";
        File f2 = new File(h2);
        if (!f2.exists()){
            String s = "For call you should be input some phone in text field and tap button 'call' which in '+123...' panel." +
                    " You can put several phones and call just go cursor on phone, without selection" +
                    "Your phones and emails you can put in top text field from contact list below, by tap an item.\n" +
                    "The list of phones and emails you can find at the bottom of the '+123...' panel.\n" +
                    " For SMS you must put phones without internal spaces. Phone numbers are separated by ' ', ',' and ';' characters  " +
                    "and then message and tap 'Send SMS'. You can also enter any symbol except ' ', ',' and ';' at the beginning of the message after phones. " +
                    "You can send more then one SMS by time." +
                    "The same for emails. Subject of email form from two first word. Attachment - from " +
                    "selected files. " +
                    "\nThe message body begins when the first word that is not an address " +
                    "is encountered. Therefore, if you attempt send an email address to other email address without " +
                    "additional text, will be sent two messages without any text to both email addresses and both addressee " +
                    "get to know addresses each other :) \n";

            s += "Calculator on '+123...' panel allows you to create custom groups of any constants for different situations, " +
                    "constants are set as follows: 'x=1/2' and 'y=cos(x)'. " +
                    "Calculator in advance knows \"pi\", \"e\" and \"c\".\nIf you want change the exists " +
                    "constant, yor must click 'show variables' button. Also you can save variables into other file and load any *.var " +
                    "file with all variables.\n"+
                    "Cos, sin and the similar functions take the argument in radians.\nSo many " +
                    "functions available in the calculator: toDegrees, toRadians, sin, cos, tan, " +
                    "asin, acos, atan, sinh, cosh, tanh, round, exp, abs, " +
                    "etc from Math.java with single argument (information about Math.java you can easily search now " +
                    "by tap on the current line and move cursor in word 'Math.java' in top text field  " +
                    "and press 'Google' button from top panel.\n" +
                    "All angles are in radians, but you can use easiest converting to degree like this: °(pi)=180 sin(90°)=1. " +
                    "Also you can use short sqrt function like this: √(5*5)=5" +
                    " The calculations very much accuracy, because used BigDecimal  when it possible, but showing" +
                    "up to maximum 20 digit after point, that sets by button 'round20'." +
                    "\nYou can put '=' in expression, not only for assignment, because the calculator " +
                    "compare known values. For example, if you try calculate \"pi = 3.16\" " +
                    "expression, calculator " +
                    "print \"pi < 3.16\", the same result will be, if you try set pi = 3.16, " +
                    "so far as calculator not permit change the constants.";

            fSync().writeFile0(f2, s);


            String h3 = p + "/edit text.txt";
            File f3 = new File(h3);
            if (!f3.exists()){

                String s3 = "The shell is focused on working with short texts in the top edit field. " +
                        "Difference functions  get word, line or whole text from this field. " +
                        "Text files are edited in the same way. " +
                        "You can now click on any line of this file and " +
                        "the selected line will appear in the edit field " +
                        "from where you can save it. By the button '...' additional " +
                        "functions are available. Please note that saving to the " +
                        "file occurs immediately after the action. When you click " +
                        "'Clear & save' at the top, the string is saved in the " +
                        "history. All notes stored in 'history' folder. You can edit " +
                        "it by line and manipulate such as your want.\n"+
                        "Your can put any text in upper edit field and save " +
                        "it in new file, also you can open any text file and click on the any line " +
                        "to edit it in the top edit field. After edited you can save changes " +
                        "by 'save' button below edit field.";
                fSync().writeFile0(f3, s3);
            }

        }
    }
    Comparator3 comparator = new Comparator3();

    class Comparator3 implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            if (o1 == o2)
                return 0;
            if (o1 == null)
                return 1;
            if (o2 == null)
                return -1;
            return stringCompare(o1.toString(), o2.toString());
        }
    };



    private void ads() {
    }

    class SelectedAdapter extends FileAdapter{
        @Override
        public void ini(){
            postCreate();
        }

        @Override
        public void refresh(){
            File[] f = fSync().getSelectedFiles();
            showList(storageList.init(f), null);
        }

        @Override
        void close(){
            selected = null;
            super.close();
        }
        @Override
        void openFile(File f, boolean addParent, Integer position){
            if (f.isDirectory())
                openAdapter(f.getAbsolutePath());
            else
                openSomeDir(f.getParentFile());

        }
        @Override
        void openFile(File f, boolean addParent, Integer position, Runnable run){
            if (f.isDirectory())
                openAdapter(f.getAbsolutePath());
            else
                openSomeDir(f.getParentFile());
        }

        @Override
        void openSomeDir(File dir){
            openAdapter(dir.getAbsolutePath());
        }

    }

    SelectedAdapter selected;
    public void showSelected() {
        if (selected == null){
            selected = new SelectedAdapter();
        }
        File[] f = fSync().getSelectedFiles();
        selected.init(0);
        selected.showList(f, "Selected files:");

    }


    HashMap<String, String> extPack =new HashMap<>();

    Drawable getIconByExt(String ext){
            Intent intent = getIntentByExt(ext);
            if (intent != null) {
                String pn = intent.getPackage();
                /*if (notEmpty(pn)){
                    try {
                        Drawable appIcon = getPackageManager().getApplicationIcon(pn);
                    } catch (PackageManager.NameNotFoundException e) {
                        return null;
                    }
                }*/
                if (!notEmpty(pn)){
                    if (extPack.containsKey(ext)) {
                        pn = extPack.get(ext);
                    }else{
                        pn = getPackageName(intent);
                        extPack.put(ext, pn);
                    }
                }


                if (notEmpty(pn)) {
                    if (allApps().map != null) {
                        Intent i = allApps().packageIndex.get(pn);
                        if (i != null) {
                            App app = allApps.map.get(i);
                            return app.logo;
                        }
                    }
                }
            }
            return null;
    }

    String getPackageName(Intent intent){
        try {
            List<ResolveInfo> resInfoList = FreeShell.this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (notEmpty(packageName))
                    return packageName;
            }
        }catch (Throwable t){

        }
        return null;
    }


    void loadVarDialog(File file){
        AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
        alert.setMessage("Current variables was saved in " + variablesFileName());
        alert.setTitle("After should be load variables from " + file.getName());
        alert.setPositiveButton("Load", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                loadVariables(file.getAbsolutePath());
                Toast.makeText(FreeShell.this, "Variables " + file.getName() + " have been loaded.", Toast.LENGTH_LONG).show();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();




    }

    void loadMtkDialog(File file){
        saveMtk();
        AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
        alert.setMessage("Current panels was saved in " + lastStateFileName());
        alert.setTitle("Load state " + file.getName());
        alert.setPositiveButton("Load", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                loadMtk(file);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();


    }
    /*
            alert.setNeutralButton("Clear text", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try{
                    unzip("", file, fileAdapter);
                    return;
                }catch(Exception t){
                    printError(t);
                }

            }
        });

     */
    void saveMtkAs(FileAdapter fileAdapter){
        String name = getWord();
        saveMtkAs(fileAdapter, name);
    }
    /*

     */

    void saveMtkAs(FileAdapter fileAdapter, String name){


        String path = fileAdapter.getDir();
        if (path == null){
            return;
        }
        final String fullPath = path + '/' + name;
        AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
        final EditText edittext = new EditText(FreeShell.this);
        edittext.setText(fullPath);
        alert.setMessage("To " +path);
        alert.setTitle("Save panels");

        alert.setView(edittext);

        alert.setPositiveButton("save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try{
                    //What ever you want to do with the value
                    final String name = edittext.getText().toString();
                    File newfile = new File(name);
                    if (newfile.exists()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                        // Specify the dialog is not cancelable
                        builder.setCancelable(true).setTitle(name + " already exists.").
                                setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // what ever you want to do with No option.
                                    }
                                }).setNeutralButton("Change name", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                saveMtkAs(fileAdapter, name);
                                // what ever you want to do with No option.
                            }
                        }).setNeutralButton("Overwrite", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                saveMtk(name);
                                // what ever you want to do with No option.
                            }
                        });
                    ;


                        AlertDialog dialog2 = builder.create();
                        // Display the alert dialog on interface
                        dialog2.show();

                        return;
                    }
                    saveMtk(name);
                    refreshDirs(newfile.getParent());
                    //listView.invalidate();
                }catch (Exception e){
                    FreeShell.this.printError(e);
                }

            }
        });
        alert.setNeutralButton("Clear text", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                saveMtkAs(fileAdapter, "");
                return;
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alertShow(alert, edittext);


    }


    class FindSubtextListener implements FSync.FoundFileListener{
        Vector<File> result;
        List<File> fileList;
        String text;
        String path;
        FindAdapter findAdapter;
        DirAdapter sources;

        FindSubtextListener(DirAdapter src, String word){
            sources = src;
            fileList = src.getCurrents();
            this.path = src.getCurrentDir().getAbsolutePath();
            findAdapter = new FindAdapter();/* {
                @Override
                public void refresh() {
                    if (foundFileListener != null) {
                        FindSubtextListener.this.start(word);
                    }
                }
            };*/

            findAdapter.init(src.listView().index + 1, src.getAbc());
            findAdapter.info0.setText("Find: " + word);
            mainReLayout();
           // fileAdapter.show();

        }

        void find(){
            fSync().findInFiles(fileList, FindSubtextListener.this, text);
        }
        String getMessage0(){
            return "Text to find";
        }
        String getMessage1(){
            return "contains \"";
        }

        String getMessage(int countDir, int countFiles){
            int amount;
            if (((result == null)|| result.size() == 0))
                amount = 0;
            else
                amount = result.size();
            return countDir + countFiles +
                    " processed, " +
                    amount + " was found " + getMessage1() + text + "\"";
        }

/*            public void dialog2(){
                alert2 = new AlertDialog.Builder(FreeShell.this);
                alert2.setMessage(getMessage());
                alert2.setTitle(path);
                alert2.setPositiveButton("Show panel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            complete(result);
                        }catch (Exception e){
                            FreeShell.this.printError(e);
                        }
                    }
                });

                alert2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        needNext = false;
                        // what ever you want to do with No option.
                    }
                });

                alert2.show();

            }*/

         boolean needNext = true;
        @Override
        public void found(int countDir, int countFiles, Vector<File> result) {

            this.result = result;
            findAdapter.info0.setText(getMessage(countDir, countFiles));
            findAdapter.info0.invalidate();

        }

        @Override
        public synchronized void complete(int countDir, int countFiles, Vector<File> result) {
            needNext = false;
            if ((result == null) || (result.size() == 0)){
                findAdapter.close();
                sources.show();
                okDialog(text + " not found", countDir+" directories " + "and " + countFiles + " files were scanned" );
                return;
            }else{
                found(countDir, countFiles, result);
            }
            findAdapter.init(result,  sources.getCurrentDir());
          //  sources.close();
            findAdapter.mainRelayout2();
        }

        @Override
        public boolean needNext() {
            return needNext;
        }
        void start(String word){

            AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
            final EditText edittext = new EditText(FreeShell.this);
            edittext.setText(word);
            alert.setMessage(getMessage0());
            alert.setTitle("Find in files");

            alert.setView(edittext);

            alert.setPositiveButton("Find", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{
                        String str = edittext.getText().toString();
                        FindSubtextListener.this.text = str;
                        find();
                        //What ever you want to do with the value
                    }catch(Exception tt){
                        FreeShell.this.printError(tt);
                        findAdapter.close();
                    }

                }
            });
            alert.setNeutralButton("Clear text", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try {
                        start("");
                    }catch (Exception e){
                        FreeShell.this.printError(e);

                        findAdapter.close();
                    }

                    return;
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    findAdapter.close();
                }
            });

            alertShow(alert, edittext);


        }



    }

    class FindNameListener extends  FindSubtextListener{

        FindNameListener(DirAdapter src, String word) {
            super(src, word);
        }

        String getMessage0(){
            return "Text in file name";

        }
        String getMessage1(){
            return "with name contains \"";
        }

        @Override
        void find(){
            fSync().findInFileNames(fileList, FindNameListener.this, text);
        }

    }


    String decode(String s){
        try {
            return URLDecoder.decode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return URLDecoder.decode(s);
        }
    }


    String encode(String s){
        try {
            return URLEncoder.encode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return URLDecoder.decode(s);
        }
    }


    void deleteFile(File file){
        file.delete();
        Toast.makeText(freeShell, file.getName() + " was deleted.", Toast.LENGTH_LONG).show();

    }

    void refreshDirs(String currentDir){
        if (notEmpty(currentDir)) {
            HashSet<FileAdapter> l = dirFileAdapter.get(currentDir);
            if (l != null) {
                for (FileAdapter fa : l) {
                    if (!fa.closed) {
                        fa.getStorageList().refresh();
                    }
                }
            }
        }
    }

    public void su() {
        String args = getLine();
        su(args);
    }
    public void su(String args) {
        AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
        final EditText edittext = new EditText(FreeShell.this);
        edittext.setText(args);
        edittext.setSelection(edittext.length());
        alert.setMessage("Command:");
        alert.setTitle("Run shell command");

        alert.setView(edittext);

        alert.setPositiveButton("Run", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try{
                    //What ever you want to do with the value
                    final String text = edittext.getText().toString();
                    su2(text);
                }catch (Exception e){
                    FreeShell.this.printError(e);
                }

            }
        });
        alert.setNeutralButton("Clear text", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                su("");
                return;
            }
        });


        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alertShow(alert, edittext);
    }



    public void su2(String args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuilder res = new StringBuilder();
                DataOutputStream outputStream = null;

                InputStream response = null;
                InputStream errors = null;
                try

                {
                    //String command = getLine();

                    String[] commands = args.split(" ");

                    Process su = Runtime.getRuntime().exec(commands);



                    response = su.getInputStream();
                    String line;
                    //Входной поток может что-нибудь вернуть
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response));
                    while ((line = bufferedReader.readLine()) != null){
/*                        if ((res.length() == 0) && (line.length() > 0)) {
                            res.append('\n');
                        }*/
                        res.append(line + "\n");
                    }
/*                    errors = su.getErrorStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(errors));

                    while ((line = bufferedReader.readLine()) != null){
                        if ((res.length() > 0) && (line.length() > 0)) {
                            res.append('\n');
                        }
                        res.append(line);
                    }*/
                 /*   outputStream = new DataOutputStream(su.getOutputStream());
                    outputStream.writeBytes("exit\n");
                    outputStream.flush();*/
                    printBelow(res.toString());

                } catch(IOException e)
                {
                    printUpperError(e);
                } finally
                {
                    if (errors != null){
                        try {
                            errors.close();
                        }catch(Throwable t){

                        }
                    }

                    if (response != null){
                        try {
                            response.close();
                        }catch(Throwable t){

                        }
                    }
                    if (outputStream != null){
                        try {
                            outputStream.close();
                        }catch(Throwable t){

                        }
                    }
                    //closeSilently(outputStream, response);
                }


            }
        }).start();
    }

    void clip(String tag, String str){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(tag, str);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(FreeShell.this, str, Toast.LENGTH_LONG).show();
    }


    void alertShow(AlertDialog.Builder alert, EditText editText){
        alert.show();
        editText.setSelection(editText.length(), editText.length());

        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 500);
    }
    boolean playing = false;
    boolean isConnectingToExistingService = false;
    HashSet<String> audioDirs = new HashSet(3);


    void updateAll(File oldFile){
        if (oldFile != null){
            String path = oldFile.getAbsolutePath();

            for(int i = 0; i < filePanels.size() ;i++){
                FileAdapter fa = filePanels.elementAt(i);
                FileItem fi = fa.itemsByPath.get(path);
                if (fi != null) {
                    fi.format();
                    fi.panel.invalidate();
                }
            }

        }
    }


    public ZipAdapter zipAdapter(){
        return new ZipAdapter();
    }

    HashMap<String, HashSet<ZipAdapter>> dirZipAdapter = new HashMap<>();

    class ZipAdapter extends DirAdapter{
        ZipList storageList = new ZipList(FreeShell.this, this, fSync());
        HTable<String, ZipItem> itemsByPath = new HTable(String.class, ZipItem.class, 32);
        int abc = StorageList.ABC_SORT;
        ShellButton zip, newPanel, backDirButton, abcButton, sizeButton, timeButton;
        @Override
        public void unLock(){
            getStorageList().setOpened(true);
            super.unLock();
        }
        public boolean canSaveFile(){
            return false;
        }
        public File getBaseZip(){
            return storageList.getZip();

        }

        @Override
        void close(){

            String parent = storageList.getRootDir();
            DirAdapter fileAdapter = this.dirAdapter;
            if (set != null){
                set.remove(this);
            }
            super.close();

            if (fileAdapter != null){
                if (!fileAdapter.closed) {
                    fileAdapter.show();
                    return;
                }
            }
            if (notEmpty(parent)){
                fileAdapter = getAdapter0(parent);
                if (fileAdapter != null){
                    if (!fileAdapter.closed) {
                        fileAdapter.show();
                        return;
                    }
                }

            }


        }

        HashSet<ZipAdapter> set;
        @Override
        void setAll(ArrayList<Object> all){
            if (set != null)
                set.remove(this);
            super.setAll(all);
            String dir = getDir();
            if (!notEmpty(dir)){
                dir = "";
            }
            HashSet<ZipAdapter> l = dirZipAdapter.get(dir);
            if (l == null) {
                l = new HashSet();
                dirZipAdapter.put(dir, l);
            }
            set = l;
            l.add(this);
        }
        public int getAbc(){
            return abc;
        }

        void setDefaultText(){
            if (notEmpty(filteredWord)){
                info0.setText("Name filter: " + filteredWord);
            }else {
                String dir = getDir();
                if (notEmpty(dir)) {
                    info0.setText(dir);
                }else
                    info0.setText("");
            }
            info0.invalidate();
        }

        public List<File> getCurrents(){
            if (notEmpty(filteredWord)) {
                ArrayList<File> ret = new ArrayList<>(current.size());
                int offset = storageList.dirsSize();
                for (int i = 0; i < current.size(); i++) {
                    int pos = indexMap.get(i);
                    if (pos >= offset){
                        ret.add(storageList.getFile(pos - offset));
                    }
                }
                return ret;
            }else{
                return storageList.getFiles();
            }
        };


        @Override
        void showList(ArrayList<Object> list, Integer pos) {
            try {
                unLock();
                setDefaultText();
                super.showList(list, pos);
            }finally {
                getStorageList().setOpened(true);
            }
        }
        @Override
        public void refresh(){
            getStorageList().refresh();
        }


        void openFile(File f, boolean addParent, Integer position){
            lock();
            storageList.openFile(f, addParent, position, null);
        }
        void backDirButtonOn(){
            backDirButton.setFlag(true);
            backDirButton.view.invalidate();

        }

        void backDirButtonOff(){
            backDirButton.setFlag(false);
            backDirButton.view.invalidate();

        }


        StorageManager getStorageManager() {
            return (StorageManager) FreeShell.this.getSystemService(Context.STORAGE_SERVICE);
        }


        ZipList getStorageList(){
            return storageList;
        }


        String getDir(){
            File dir = storageList.getCurrentDir();
            if (dir != null)
                return dir.getAbsolutePath();
            return null;
        }

        File getCurrentDir(){
            return storageList.getCurrentDir();
        }

        DirAdapter dirAdapter;
        void openSomeDir(File dir, DirAdapter dirAdapter){
            lock();
            this.dirAdapter = dirAdapter;
            if (dir.exists()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.yield();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                storageList.openSomeDir(dir);
                            }
                        });
                    }
                }).start();
            }else {
                okDialog("Error", dir.getAbsolutePath() + " not found.");
                close();
            }
        }


        void openSomeDir(String dir){
            File f = storageList.findFile(dir);
            if (f != null){
                storageList.openSomeDir(f);
            }
        }

        @Override
        void clear(){
            itemsByPath.clear();
            super.clear();
        }


        public ZipAdapter(ZipAdapter prev, int index) {
            create0(0, 0);
            ini();
            init(index);
            abc = prev.abc;
            showList(storageList.init(prev.storageList), prev.listView().getSelectedItemPosition());
        }


        public ZipAdapter() {
            create0(0, 0);
            ini();

        }

        void setFile(File f){
            firstScreen().setEditFileName(f.getParent(), f.getName());

            mainReLayout();
        }

        public void ini(){


            buttonPanel.addLine(null, 0);
            newPanel =
                    new ShellButton("clone panel",
                            new Procedure() {
                                @Override
                                public void procedure() {

                                    listPanel.newZipPanel(ZipAdapter.this, listView.index + 1);
                                }
                            },
                            cRose,
                            bRose, 2, buttonPanel);

            backDirButton = new ShellButton("\u21D1",
                    this::backDir,
                    false, 2, buttonPanel){
                @Override
                void setFlag(boolean v){
                    if ((flag == null)||(v != flag)) {
                        background = v ? bLight : bOff;
                        button().setBackgroundColor(background);
                        button().setTextColor(v ? cMangeta : cOff);
                        flag = v;
                    }
                }

            };


            abcButton =
                    new ShellButton("Abc sort",
                            new Procedure() {
                                @Override
                                public void procedure() {
                                    abc = StorageList.ABC_SORT;
                                    sizeButton.setFlag(false);
                                    timeButton.setFlag(false);
                                    abcButton.setFlag(true);
                                    storageList.refresh();
                                    //listView.invalidateViews();
                                }
                            }, abc == StorageList.ABC_SORT, 2, buttonPanel);
            timeButton =
                    new ShellButton("Time sort", this::timeSort,abc == StorageList.MODIFY_SORT, 2, buttonPanel);

            sizeButton =
                    new ShellButton("Len sort",
                            new Procedure() {
                                @Override
                                public void procedure() {
                                    abc = StorageList.SIZE_SORT;
                                    sizeButton.setFlag(true);
                                    timeButton.setFlag(false);
                                    abcButton.setFlag(false);

                                    storageList.refresh();
                                    //listView.invalidateViews();
                                }
                            },abc == StorageList.SIZE_SORT, 2, buttonPanel);


            postCreate();
        }



        private void findFileName() {
            String word = getWord();

     /*       if ((foundFileListener != null) && (foundFileListener instanceof  FindNameListener)){
                ((FindNameListener)foundFileListener).start(word);
            }else {*/
            FindSubtextListener listener = new FindNameListener(this, word);
            listener.start(word);
            //}
        }

        //FSync.FoundFileListener foundFileListener;
        private void findSubtext() {
            String word = getWord();
/*
            if ((foundFileListener != null) && (foundFileListener instanceof  FindSubtextListener)){
                ((FindSubtextListener)foundFileListener).start(word);
            }else {*/
            FindSubtextListener listener = new FindSubtextListener(this, word);
            listener.start(word);
            //}
        }



        private void zip(){
            String s = "";
            boolean ok = false;
            String[] ss = fSync().getSelected();
            if (notEmpty(ss)){
                File f = new File(ss[0]);
                s = f.getName();
                int i = s.indexOf('.');
                if (i >= 0){
                    s = s.substring(i);
                    if (ss.length == 1){
                        ok = true;
                    }
                }
            }
            if (!ok) {
                String word = getWord();
                if (notEmpty(word)) {
                    s = word;
                }
            }
            if (!s.endsWith(".zip")){
                s = s + ".zip";
            }
            zip(s);

        }


        private void zip(String name) {
            final String path = getRootPath();
            if (path == null){
                return;
            }
            AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
            final EditText edittext = new EditText(FreeShell.this);
            edittext.setText(path + '/' + name);
            alert.setMessage("Please enter name");
            alert.setTitle("Create new zip file");

            alert.setView(edittext);

            alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{
                        //What ever you want to do with the value
                        String name0 = edittext.getText().toString();
                        if (!name0.endsWith(".zip")){
                            name0 = name0 + ".zip";
                        }
                        final String name2 = name0;
                        File newfile = new File(name2);
                        if (newfile.exists()){

                            AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                            // Specify the dialog is not cancelable
                            builder.setCancelable(false).setTitle(name2 + " already exists. Your can create zip with new name only.").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // what ever you want to do with No option.
                                }
                            }).setNeutralButton("Change name", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    zip("");
                                    // what ever you want to do with No option.
                                }
                            });
                            ;


                            AlertDialog dialog2 = builder.create();
                            // Display the alert dialog on interface
                            dialog2.show();

                            return;
                        }
                        try {
                            fSync().createZip(newfile, ZipAdapter.this);
                        } catch (Exception e) {
                            FreeShell.this.printError(e);
                        }
                        String currentDir = getDir();
                        refreshDirs(currentDir);

                        //listView.invalidate();
                    }catch(Exception tt){
                        FreeShell.this.printError(tt);
                    }

                }
            });
            alert.setNeutralButton("Clear text", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try {
                        zip("");
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }

                    return;
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });

            alertShow(alert, edittext);


        }





        private void timeSort() {
            abc = StorageList.MODIFY_SORT;
            sizeButton.setFlag(false);
            timeButton.setFlag(true);
            abcButton.setFlag(false);
            storageList.refresh();
            //listView.invalidateViews();
        }

        @Override
        void showFirst() {
            init(0);
            storageList.showZipList();
        }


        @Override
        public Item getView(final int position, final int filteredPosition) {
            final File f = storageList.getFile(position);
            ZipItem ZipItem = new ZipItem(position);
            final boolean isParent = position < storageList.dirsSize();
            ZipItem.init('?', f, isParent, this, b);

            b= !b;
            if (f != null) {
                itemsByPath.put(f.getAbsolutePath(), ZipItem);
            }
            return ZipItem;

        }





        void backDir() {
            if (storageList.dirsSize() < 2) {
                close();
                return;
            }
            lock();
            storageList.backDir();
            if (storageList.dirsSize() == 1){
                backDirButtonOff();
            }
        }



        void menu(final ZipItem zipItem) {
            if (zipItem.file == null)
                return;
            showPopupMenu(zipItem);
        }


        private void showPopupMenu(final ZipItem ZipItem) {
            if (ZipItem.file.isDirectory()){
                showFolderMenu(ZipItem);
            }else{
                showFileMenu(ZipItem);
            }
        }
        void extract(File file){
            if (file instanceof ZipList.Entry){
                extract((ZipList.Entry)file, this);
            }
        }



        String getRootPath(){
            return storageList.getRootDir();
        }

        void extract(File dir, ZipList.Entry arj){
            boolean a;
            if (!dir.exists()){
                dir.mkdirs();
                a = true;
            }else
                a = false;
            arj.extract(dir);
            if (a)
                refreshDirs(dir.getParent());
            else
                refreshDirs(dir.getAbsolutePath());
        }

        public  void extract(ZipList.Entry file, ZipAdapter fileAdapter){
            String path = fileAdapter.getRootPath();
            if (path == null){
                freeShell.okDialog("Error", "File not found");
                return;
            }
            AlertDialog.Builder alert = new AlertDialog.Builder(FreeShell.this);
            final EditText edittext = new EditText(FreeShell.this);
            final String nameDir = path + "/" + file.getName();
            edittext.setText(nameDir);
            alert.setMessage("Please enter a file name");
            alert.setTitle("Extract to new directory");

            alert.setView(edittext);

            alert.setPositiveButton("Extract", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{
                        //What ever you want to do with the value
                        final String nameDir2 = edittext.getText().toString();
                        File dir = new File(nameDir2);
                        if (dir.exists()){
                            if (dir.isDirectory()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                                // Specify the dialog is not cancelable
                                builder.setCancelable(false).setTitle(nameDir2 + " already exists. Extract to existing files?").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // what ever you want to do with No option.
                                    }
                                }).setNeutralButton("Change name", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        extract(file, fileAdapter);
                                        // what ever you want to do with No option.
                                    }
                                }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        extract(dir, file);
                                        // what ever you want to do with No option.
                                    }
                                });

                                AlertDialog dialog2 = builder.create();
                                // Display the alert dialog on interface
                                dialog2.show();

                                return;

                            }else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(freeShell);
                                // Specify the dialog is not cancelable
                                builder.setCancelable(false).setTitle(nameDir2 + " already exists.").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // what ever you want to do with No option.
                                    }
                                }).setNeutralButton("Change name", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        extract(file, fileAdapter);
                                        // what ever you want to do with No option.
                                    }
                                });
                                ;


                                AlertDialog dialog2 = builder.create();
                                // Display the alert dialog on interface
                                dialog2.show();

                                return;
                            }
                        }
                        extract(dir, file);
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }

                }
            });
            alert.setNeutralButton("Clear text", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try{
                        extract(file, fileAdapter);
                        return;
                    }catch(Exception t){
                        freeShell.printError(t);
                    }

                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });

            alertShow(alert, edittext);



        }




        private void showFolderMenu(final ZipItem ZipItem) {
            PopupMenu popupMenu = new PopupMenu(freeShell, ZipItem.settingsButton);
            popupMenu.inflate(R.menu.zipfoldermenu);

            popupMenu
                    .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.select:
                                    ZipItem.changeSelection();
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

            popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    Toast.makeText(getApplicationContext(), "onDismiss",
                            Toast.LENGTH_SHORT).show();
                }
            });
            popupMenu.show();

        }
        private void showFileMenu(final ZipItem zipItem) {
            PopupMenu popupMenu = new PopupMenu(freeShell, zipItem.settingsButton);
            popupMenu.inflate(R.menu.zipmenu);

            popupMenu
                    .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.view:
                                    openTextFile(zipItem.zipAdapter(), zipItem.file, listView().index);
                                    return true;
                                case R.id.extract:
                                    extract(zipItem.file);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

            popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    Toast.makeText(getApplicationContext(), "onDismiss",
                            Toast.LENGTH_SHORT).show();
                }
            });
            popupMenu.show();

        }


        @Override
        public void save(PrintWriter writer) {
            super.save(writer);

            writer.println("sort=" + abc);
            File file = storageList.getCurrentDir();
            String dir;
            if (file != null) {
                dir = file.getAbsolutePath();
                writer.println("dir=" + dir);
            }
            dir = storageList.getZipPath();
            if (notEmpty(dir)){
                writer.println("zip=" + dir);
            }

            if (dirAdapter != null){
                if (!dirAdapter.closed) {
                    writer.println("parent=" + dirAdapter.getDir());
                }
            }

        }

        boolean init(String f) {
            return storageList.openSomeDir(new File(f));
        }

        @Override
        public boolean load(Tag tag) {
            abc = tag.getIntByLink("#abc");
            super.load(tag);
            String f = tag.getStringByLink("#zip");
            if (notEmpty(f)) {
                if (!init(f)){
                    close();
                    return false;
                }
                f = tag.getStringByLink("#dir");
                if (notEmpty(f)) {
                    openSomeDir(f);
                }else {
                    close();
                }
                return true;
            }
            return false;
        }

        @Override
        public void start(Tag tag) {
            if (!closed){
                String f = tag.getStringByLink("#parent");
                if (notEmpty(f)) {
                    dirAdapter = getAdapter(f);
                }

            }
            super.start(tag);
        }

        @Override
        public String getName() {
            return "zipAdapter";
        }


    }



    class ZipItem extends Item {
        ZipItem(int position) {
            super(position);
        }

        ZipAdapter ZipAdapter(){
            return (ZipAdapter)adapter;
        }


        void firstOpen(DirAdapter dirAdapter){
            if (file == null)
                return;
            if (file.isDirectory()) {
                if (ZipAdapter().getStorageList().dirsSize() == 1){
                    //ZipAdapter.openFile(file, false, null);
                    ZipAdapter().openSomeDir(file, dirAdapter);
                }else
                    ZipAdapter().openFile(file, true, globalPosition);
                return;
            }
            String ext = getFileExt(file.getName());
            if (ext != null) {
                br0:if (ext.equals("link")) {
                    String name = loadLine(file.getAbsoluteFile());
                    if (name == null)
                        break br0;
                    if (name.startsWith("packageName=")) {
                        name = name.substring("packageName=".length());

                    } else
                        break br0;
                    Intent intent = allApps().packageIndex.get(name);
                    if (intent != null) {
                        FreeShell.this.startActivityForResult(intent, 262144);
                        return;
                    }
                    break br0;
                }
            }
            if(ext.toLowerCase().equals("zip")){
                unzip(file, null);
                return;
            }else if (isText(ext)){
                openTextFile(zipAdapter(), file, ZipAdapter().listView.index + 1);
                return;
            }else if (ext.equals("shell")) {
                loadMtkDialog(file);
                return;
            } else if (ext.equals("var")) {
                loadVarDialog(file);
                return;
            }
        }


        boolean isSelect() {

            if (file == null)
                return false;
            return fSync().isSelect(this);

        }

        ZipAdapter zipAdapter(){
            return (ZipAdapter) adapter;
        }
        boolean parity;
        public void init(Character defSymbol, File f, boolean isParent, ZipAdapter ZipAdapter, boolean b) {
            this.file = f;
            this.parity = b;
            this.isParent = isParent;
            this.adapter = ZipAdapter;
            int abc = ZipAdapter.abc;

            View.OnClickListener select = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        fSync().changeSelect(ZipItem.this.file, ZipItem.this);
                        if (FreeShell.this.selected != null){
                            selected.refresh();
                        }
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }
                   /*format();
                    panel.invalidate();
                    openButton0.invalidate();
                    textView.invalidate();*/
                }
            };

            View.OnClickListener open;
            if (isParent) {

                open = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{
                            if (ZipAdapter.isLocked())
                                return;
                            ZipAdapter.lock();
                            ZipAdapter.getStorageList().openFileFromTree(globalPosition);
                        }catch (Exception e){
                            FreeShell.this.printError(e);
                        }

                    }
                };


            } else {
                open = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firstOpen(zipAdapter().dirAdapter);
                    }
                };
            }

            View.OnClickListener settings = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        ZipAdapter.menu(ZipItem.this);
                    }catch (Exception e){
                        FreeShell.this.printError(e);
                    }
                }
            };

            Runnable rloadIcon = null;

            br:if ((f != null)&&(!f.isDirectory())) {




                String t = file.getName();
                String ext = getFileExt(t);
                final File ff = file;
                if (ext != null) {
                    br0:
                    if (ext.equals("link")) {
                        String name = loadLine(file.getAbsoluteFile());
                        if (name == null)
                            break br0;
                        if (name.startsWith("packageName=")) {
                            name = name.substring("packageName=".length());


                        } else break br0;
                        App app = allApps().nameIndex.get(name);
                        Drawable logo = app.logo;
                        rloadIcon = new Runnable() {
                            @Override
                            public void run() {
                                if (logo != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setIcon(logo);
                                        }
                                    });
                                }

                            }
                        };
                        break br;
                    }else if (ext.equals("shell")){
                        rloadIcon = new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    Intent i = allApps().packageIndex.get("mtk.free.shell");
                                    if (i != null) {
                                        App app = allApps.map.get(i);
                                        Drawable logo = app.logo;
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                ZipItem.this.setIcon(logo);
                                            }
                                        });
                                    }
                                }catch (Exception e){

                                }
                            }
                        };

                    }else if (AudioServiceBinder.isAudioExtension(ext)){

                    }else if (ext.equals("var")){

                    }else if (ext.equals("txt")){

                    }else if (ext.equals("zip")){

                    }else
                    {
                        rloadIcon = new Runnable() {
                            @Override
                            public void run() {
                                Drawable logo = getIconByExt(ext);
                                if (logo != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ZipItem.this.setIcon(logo);
                                        }
                                    });
                                }
                            }
                        };

                    }

                }
            }


            super.init(defSymbol, ZipAdapter, file, listW, open, settings, select, rloadIcon, null);
            if (file == null){
                panel.setMinimumHeight(hb);
                if (openButton0 != null)
                    panel.removeView(openButton0);
                if (selectButton != null)
                    panel.removeView(selectButton);
            }
            format();


        }


        @Override
        void format() {
            if (file != null){
                if (!file.exists()){
                    file = null;
                }
            }
            boolean isSelect = isSelect();
            if (isSelect) {
                //openButton0.setTextColor(0xFFFFFF40);
                panel.setBackgroundColor(0xFF000060);
                textView.setTextColor(0xFFFFFF40);
                if (selectButton != null)
                    selectButton.setText(selSettingsHtml);
                if (openButton0 != null){
                    if (openButton0 instanceof TextView){
                        ((TextView) openButton0).setTextColor(0xFFFFFF40);
                    }

                }

            } else {
                if (selectButton != null)
                    selectButton.setText(settingsHtml);
                if (openButton0 != null){
                    if (openButton0 instanceof TextView){
                        ((TextView) openButton0).setTextColor(0xc0101000);
                    }
                }
                // openButton0.setTextColor(0xD4000000);//0xD40FFFFF

            }

            if (file == null) {
                if (globalPosition == 0)
                    textView.setText("*** LIST OF ROOTS ***");
                panel.setBackgroundColor(bLight);
                textView.setTextColor(cMangeta);
                if (openButton0 != null)
                    openButton0.setOnClickListener(null);
                return;
            }
            if (file.isDirectory()) {
                setDirColors(isSelect, isParent);
            }else
                paintZipItem(isSelect);
            panel.invalidate();
        }


        void setDirColors(boolean isSelect, boolean isParent) {
            boolean b = false;
            if (file == null)
                return;
            if (audioDirs.contains(file.getAbsolutePath())) {
                b = true;
            }
            TextView icon;
            if (this.icon == null) {
                icon = new TextView(FreeShell.this);
                if (openButton0 != null){
                    panel.removeView(openButton0);
                    openButton0 = null;
                }

                icon.setBackgroundColor(Color.TRANSPARENT);

                icon.setTextAppearance(FreeShell.this, R.style.fontSelButton);

                icon.setClickable(true);
                icon.setOnClickListener(this.open);
                icon.setGravity(Gravity.CENTER);
                icon.setX(iconX);
                icon.setY(0);

                this.icon = icon;
                panel.addView(icon, hb, hb);
                if (isParent) {
                    //&lt;parent&gt;
                    icon.setText(Html.fromHtml("<center>&lt;parent&gt</center>"));
                } else {
                    icon.setText(Html.fromHtml("<center>&lt;dir&gt</center>"));
                }
                this.icon = icon;
            } else {
                icon = (TextView) this.icon;
            }

            Long len = fSync().sizes().get(file);
            StringBuilder src = formatFile(b, len);
            String s = src.toString();
            textView.setText(Html.fromHtml(s));
            if (!isSelect) {
                icon.setTextColor(0xc0101000);
                if (isParent) {
                    panel.setBackgroundColor(bLight);
                    textView.setTextColor(cMangeta);

                } else {
                    panel.setBackgroundColor(bCyan);
                    textView.setTextColor(cCyan);
                }
            } else {
                icon.setTextColor(0xFFFFFF40);
            }



        }


        String formatLen(Long len) {
            if (len == null)
                return "";
            String sl = Long.toString(len);
            StringBuilder sb = new StringBuilder(sl.length() + sl.length() / 3);
            for (int i = sl.length() - 1, c = 0; i >= 0; i--) {
                sb.insert(0, sl.charAt(i));
                if (c == 2) {
                    if (i > 0)
                        sb.insert(0, '.');
                    c = 0;
                } else
                    c++;
            }
            sl = sb.toString();
            return sl;
        }

        StringBuilder formatFile(boolean b, Long len) {
            File f = file;
            String t = f.getName();
            StringBuilder src = new StringBuilder();

            String sl = formatLen(len);
            // src.append("<big>");
            int abc = ZipAdapter().abc;
            if (abc == StorageList.ABC_SORT) {
                src.append(t);
                src.append(' ');
                src.append("<font color=#C0101000><small><small>");
                src.append(sl);
                src.append(' ');
                src.append(ZipAdapter().getStorageList().getTimes(f.lastModified()));
                src.append("</small></small></font>");
            } else if (abc == StorageList.SIZE_SORT) {
                src.append(sl);
                src.append(' ');
                src.append(t);
                src.append(' ');
                src.append("<font color=#C0101000><small><small>");
                src.append(ZipAdapter().getStorageList().getTimes(f.lastModified()));
                src.append("</small></small></font>");
            } else if (abc == StorageList.MODIFY_SORT) {
                src.append(ZipAdapter().getStorageList().getTimes(f.lastModified()));
                src.append(' ');
                src.append(t);
                src.append(' ');
                src.append("<font color=#C0101000><small><small>");
                src.append(sl);
                src.append("</small></small></font>");
            }
            //    src.append("</big>");
            if (b) {
                src.insert(0, "<b>");
                src.append("</b>");
            }
            return src;
        }


        void paintZipItem(boolean isSelect) {
            File f = file;
            if (f == null)
                return;
            boolean b = false;
            if (audioServiceBinder != null) {
                if (f.equals(audioServiceBinder.getFile())) {
                    b = true;
                }
            }
            StringBuilder src = formatFile(b, f.length());
            String s = src.toString();
            SpannableString spanned = new SpannableString(Html.fromHtml(s));
            textView.setText(spanned);
            if (!isSelect) {
                if (this.parity) {
                    panel.setBackgroundColor(bGreen);
                }else{
                    panel.setBackgroundColor(bGreen1);
                }
                textView.setTextColor(cBlue);
            }


        }
    }


    public String getTime(int l){
        long hours = l / 3600000;
        long rm = (l % 3600000);
        long min = rm / 60000;
        long s = (rm % 60000) / 1000;
        String sh = "" + hours;
        if (sh.length() == 1){
            sh = "0" + sh;
        }
        String sm = "" + min;
        if (sm.length() == 1){
            sm = "0" + sm;
        }
        String ss = "" + s;
        if (ss.length() == 1){
            ss = "0" + ss;
        }

        return sh + ":" + sm + ":" + ss;
    }



}
