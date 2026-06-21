package mtk.free.shell;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.webkit.MimeTypeMap;

import com.mtk.map.IntArray;

import java.io.File;
import java.util.Random;

/**
 * Created by Jerry on 2/15/2018.
 */

public class AudioServiceBinder extends Binder {

    AudioServiceBinder(){
        System.out.println("new AudioServiceBinder");
    }

    // Media player that play audio.
    private MediaPlayer audioPlayer = null;


    int oldAudioPosition;

    public synchronized int getFilePosition(){
        if (list != null){
            return list.getPosition();
        }
        return 0;
    }
    public synchronized File getFile(){
        if (list != null){
            return list.getFile();
        }
        return null;
    }


    /*
            File dir = file.getParentFile();
        File[] files = dir.listFiles();
        if (files == null)
            return -1;
        if (files.length == 0)
            return -1;
        for(int i = 0; i < files.length; i++){
            File f = files[i];
            if (f.equals(file)){
                return i;
            }
        }
        if (oldFilePosition < files.length){
            return oldFilePosition;
        }
        return 0;

     */

    // Start play audio.
    public synchronized boolean next()
    {

        if(audioPlayer != null) {
            int d = 1000;//getDuration() / 100;
            int pos = getCurrentAudioPosition();
            if (pos + d > getDuration()){
                nextFile();
                return true;
            }

            audioPlayer.seekTo(pos + d);
        }else{
            int d = 1000;//getDuration() / 100;
            int pos = oldAudioPosition + d;
            if (pos > duration) {
                if (oldAudioPosition == duration)
                    return false;
                oldAudioPosition = duration;
            }else
                oldAudioPosition = pos;
        }
        return true;
    }

    // Start play audio.
    public synchronized void seekTo(int pos)
    {
        if(audioPlayer!=null) {
            audioPlayer.seekTo(pos);
        }
        oldAudioPosition = pos;
    }
    // Start play audio.
    public synchronized boolean prev()
    {
        if(audioPlayer!=null) {
            int d =  1000;
            int pos = getCurrentAudioPosition();
            if (pos - d < 0){
                prevFile();
                return true;
            }
            audioPlayer.seekTo(pos - d);
        }else{
            int d = 1000;//getDuration() / 100;
            int pos = oldAudioPosition - d;
            if (pos < 0) {
                if (oldAudioPosition == 0){
                    return false;
                }
                oldAudioPosition = 0;

            }else
                oldAudioPosition =  pos;
        }
        return true;
    }


    // Stop play audio.
    public synchronized void stop()
    {
        try {
            if (audioPlayer != null) {
                MediaPlayer mediaPlayer = audioPlayer;
                audioPlayer = null;
                oldAudioPosition = mediaPlayer.getCurrentPosition();
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();


            }
        }catch (Throwable t){

        }
    }
    public synchronized  boolean ini(Files list, int seekTo){
        this.list = list;
        File file = list.getFile();
        if (file != null) {
            if (file.exists()) {
                if (!file.isDirectory()) {
                    setupDuration(file);
                    oldAudioPosition = seekTo;
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized  boolean start(Files list, int seekTo){
        this.list = list;
        File file = list.getFile();
        if (file != null) {
            if (file.exists()) {
                if (!file.isDirectory()) {
                    oldAudioPosition = seekTo;
                    return start();
                }
            }
        }
        return false;
    }

    MediaPlayer.OnCompletionListener onCompletionListener;
    MediaPlayer.OnCompletionListener onCompletionListener(){
        if (onCompletionListener == null) {
            onCompletionListener = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    nextFile();
                }
            };
        }
        return onCompletionListener;
    }

   /* MediaPlayer.OnSeekCompleteListener onSeekCompleteListener;
    MediaPlayer.OnSeekCompleteListener onSeekCompleteListener(){
        if (onSeekCompleteListener == null) {
            onSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    nextFile();
                }
            };
        }
        return onSeekCompleteListener;
    }*/


    int duration;
    public synchronized  void setupDuration(File file){
        if (file == null)
            return;
        try {

            if (audioPlayer != null) {
                stop();
            }
            audioPlayer = new MediaPlayer();
            //audioPlayer.setDisplay(null);


            audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            audioPlayer.setDataSource(file.getAbsolutePath());
            audioPlayer.setLooping(false);
            //audioPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
            audioPlayer.setOnCompletionListener(onCompletionListener());
       //     audioPlayer.setOnSeekCompleteListener(onSeekCompleteListener());
            audioPlayer.prepare();
            duration = audioPlayer.getDuration();
            stop();
        }catch (Throwable t){

        }

    }


    public synchronized  boolean start()
    {
        File file = list.getFile();
        if (file == null)
            return false;
        int seekTo = oldAudioPosition;
        if (seekTo < 0)
            seekTo = 0;
        try {

            if (audioPlayer!= null){
                stop();
            }
            audioPlayer = new MediaPlayer();
            //audioPlayer.setDisplay(null);


            audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            audioPlayer.setDataSource(file.getAbsolutePath());
            audioPlayer.setLooping(false);

            audioPlayer.prepare();
            audioPlayer.setOnCompletionListener(onCompletionListener());
            duration = audioPlayer.getDuration();
            if (oldAudioPosition >= duration){
                return nextFile();
            }
            if (seekTo != 0)
                if (audioPlayer.getDuration() > seekTo)
                    audioPlayer.seekTo(seekTo);
            audioPlayer.start();

            return true;
        }catch(Throwable ex)
        {
            ex.printStackTrace();
            return false;
        }

    }


    // Return current audio play position.
    public synchronized int getCurrentAudioPosition()
    {
        if(audioPlayer != null)
            return audioPlayer.getCurrentPosition();

        return oldAudioPosition;

    }

    // Return current audio play position.
    public synchronized int getDuration()
    {
        if(audioPlayer != null)
            return audioPlayer.getDuration();

        return duration;

    }
    // Return total audio file duration.
    public synchronized int getTotalAudioDuration()
    {
        if(audioPlayer != null){
            return audioPlayer.getDuration();
        }else
            return duration;
    }

    // Return current audio player progress value.
    public synchronized int getAudioProgress(int max) {
        int ret = 0;
        int currAudioPosition;
        int totalAudioDuration;

        if (isPlaying()) {
            currAudioPosition = getCurrentAudioPosition();
            totalAudioDuration = getTotalAudioDuration();
        }else{
            currAudioPosition = oldAudioPosition;
            totalAudioDuration = duration;
        }
        if (totalAudioDuration > 0) {
            ret = (int)((long)currAudioPosition * (long)max / (long)totalAudioDuration);
        }
        return ret;

    }



    public static boolean isAudioExtension(String ext) {
        ext = ext.toLowerCase();
        if (ext.equals("mp3")){
            return true;
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        if (type == null)
            return false;
        return type.toLowerCase().contains("audio");
    }

    boolean isRandom = false;
    public synchronized void setRandom(boolean b) {
        isRandom = b;
    }
    boolean isLoop = false;
    public synchronized void setLoop(boolean b) {
        isLoop = b;
    }
    public synchronized Files getFileList(){
        return list;
    }

    Files list;
    public synchronized boolean nextFile() {
        if (list == null){
            return false;
        }
        if (list.size() == 0){
            return false;
        }
        stop();
        if (isRandom){
            return random();

        }
        int start = list.getPosition();
        for(int i = start + 1; i < list.size(); i++){
            if (checkFile(i)) {
                return true;
            }
        }
        if (isLoop){
            for(int i = 0; i < start; i++){
                if (checkFile(i )) {
                    return true;
                }
            }
            if (checkFile(start))
                return true;
        }
        return false;
    }

    public synchronized boolean prevFile() {
        if (list == null){
            return false;
        }
        if (list.size() == 0){
            return false;
        }
        stop();

        if (isRandom){
            return random();

        }
        int start = list.getPosition();
        for(int i = start - 1; i >= 0; i--){
            if (checkFile(i)) {
                return true;
            }
        }
        if (isLoop){
            for(int i = start + 1; i < list.size(); i++){
                if (checkFile(i )) {
                    return true;
                }
            }
            if (checkFile(start))
                return true;
        }
        return false;
    }


    private synchronized boolean checkFile(int pos){
        File f = list.get(pos);
        if (f == null)
            return false;
        if (!f.exists())
            return false;
        if (f.isDirectory())
            return false;
        if (isAudioExtension(FreeShell.getFileExt(f.getName()))){
            oldAudioPosition = 0;
            list.setPosition(pos);
            start();
            return true;
        }
        return false;
    }

    Random random = new Random();
    private synchronized boolean random() {

        int next = random.nextInt(list.size());
        int start = list.getPosition();
        if (next != start) {
            if (checkFile(next)){
                return true;
            }
        }

        IntArray map = new IntArray(list.size());
        for(int i = 0; i < list.size(); i++){
            map.setElementAt(i, i);
        }

        while(map.size() > 0){
            next = random.nextInt(map.size());
            int i = map.elementAt(next);
            if (checkFile(i))
                return true;
            map.removeElementAt(next);
        }
        return false;
    }

    public synchronized boolean isPlaying(){
        return (audioPlayer!= null) && (audioPlayer.isPlaying());
    }


}