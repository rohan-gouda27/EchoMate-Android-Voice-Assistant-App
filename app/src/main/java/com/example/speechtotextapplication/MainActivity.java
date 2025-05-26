package com.example.speechtotextapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SpeechRecognizer recognizer;
    private Button btnRecord;
    private TextView textView;
    private TextToSpeech tts;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        requestAudioPermission();
        initTextToSpeech();
    }
private void initTextToSpeech(){
        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            if(tts.getEngines().isEmpty()){
                Toast.makeText(MainActivity.this, "Engine is Not Availbale", Toast.LENGTH_SHORT).show();
            }else {
                String s=wishme();
                tts.setSpeechRate(0.99f);
                speak(s + "  How can i help you ");
            }
            }
        });
}

    private String wishme() {
        String s = " ";
        Calendar c = Calendar.getInstance();
        int time = c.get(Calendar.HOUR_OF_DAY);
        if (time < 12) {
            s = "Good Morning Sir";
        } else if (time < 16) {
            s = "Good Afternoon Sir";
        } else if (time < 20) {
            s="Good Evening Sir";
        }else{
            s="Good Night Sir";
        }
        return s;
    }

    private void speak(String msg) {
        tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void response(String msg){
        String msgs=msg.toLowerCase(Locale.ROOT);
        if(msgs.contains("hi")){
            speak("Hello sir how can i help you");
        }
        if(msgs.contains("time")){
            Date date=new Date();
            String time= DateUtils.formatDateTime(this,date.getTime(),DateUtils.FORMAT_SHOW_TIME);
            speak(time);
        }
        if(msgs.contains("date")){
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dt=new SimpleDateFormat("dd/MM/yyyy");
            Calendar cal=Calendar.getInstance();
            String todays_Date=dt.format(cal.getTime());
            speak("the date is"+todays_Date);
        }

        if(msgs.contains("google")){
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
            startActivity(intent);
        }
        if(msgs.contains("youtube")){
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"));
            startActivity(intent);
        }
        if(msgs.contains("search")){
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("https://in.search.yahoo.com/search?fr=mcafee&type=E210IN885G0&p="+msgs.replace("search"," ")));
            startActivity(intent);
        }
        if(msgs.contains("remember")){
            speak("Okay sir i will remember that for you..");
            writeToFile(msgs.replace("Remember To"," "));
        }
        if(msgs.contains("know")){
          String data=readFromFile();
          speak("Yes sir you told me to remember that"+data);
        }
        if(msgs.contains("play")){
            play();
        }
        if(msgs.contains("pause")){
            pause();
        }
        if(msgs.contains("stop")){
            stop();
        }
    }

    private void stop() {
      stopPlayer();
    }

    private void pause() {
        if(player!=null){
            player.pause();
        }
    }

    private void play() {
        if(player==null){
            player=MediaPlayer.create(this,R.raw.song);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer();
                }
            });
        }
        player.start();
    }

    private void stopPlayer() {
        if(player!=null){
            player.release();
            player=null;
            Toast.makeText(this, "Media Player Released", Toast.LENGTH_SHORT).show();
        }
    }

    private String readFromFile() {
        String ret=" ";
        try {
            InputStream inputStream=openFileInput("data.txt");
            if(inputStream!=null){
                InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
                BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                String receivestr="";
                StringBuilder stringBuilder=new StringBuilder();
                while ((receivestr=bufferedReader.readLine())!=null){
                    stringBuilder.append("\n").append(receivestr);
                }
                inputStream.close();
                ret=stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e){
            Log.e("Exception","File Not Found "+e.toString());
        }
        catch (IOException e){
            Log.e("Exception","Cannot Read File "+e.toString());
        }
        return ret;
    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(openFileOutput("data.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }catch (IOException e){
            Log.e("Exception","File write Failed"+e.toString());
        }
    }

    private void initializeViews() {
        textView = findViewById(R.id.textView);
        btnRecord = findViewById(R.id.btnRecord);

        if (textView == null || btnRecord == null) {
            Log.e(TAG, "UI components not found. Check layout IDs.");
            Toast.makeText(this, "UI error. Check layout IDs.", Toast.LENGTH_LONG).show();
        } else {
            btnRecord.setEnabled(false);
        }
    }

    private void requestAudioPermission() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Toast.makeText(MainActivity.this, "Audio permission granted", Toast.LENGTH_SHORT).show();
                        initializeSpeechRecognizer();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "Permission denied. Cannot use speech input.", Toast.LENGTH_LONG).show();
                        btnRecord.setEnabled(false);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void initializeSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition not available.", Toast.LENGTH_LONG).show();
            return;
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                updateTextView("Listening...");
            }

            @Override public void onBeginningOfSpeech() {}

            @Override public void onRmsChanged(float rmsdB) {}

            @Override public void onBufferReceived(byte[] buffer) {}

            @Override public void onEndOfSpeech() {
                updateTextView("Processing...");
            }

            @Override
            public void onError(int error) {
                String message;
                switch (error) {
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        message = "No speech could be recognized";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        message = "Network error";
                        break;
                    case SpeechRecognizer.ERROR_AUDIO:
                        message = "Audio recording error";
                        break;
                    default:
                        message = "Unknown error: " + error;
                }
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onResults(Bundle results) {
                ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (result != null && !result.isEmpty()) {
                    updateTextView(result.get(0));
                } else {
                    updateTextView("No result.");
                }
                btnRecord.setEnabled(true);
                response(result.get(0));
            }

            @Override public void onPartialResults(Bundle partialResults) {}

            @Override public void onEvent(int eventType, Bundle params) {}
        });

        btnRecord.setEnabled(true);
    }

    public void startRecording(View view) {
        if (recognizer == null) {
            Toast.makeText(this, "Speech recognizer not initialized.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);

        try {
            recognizer.startListening(intent);
            btnRecord.setEnabled(false);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "startRecording: " + e.getMessage());
        }
    }

    private void updateTextView(String message) {
        if (textView != null) {
            textView.setText(message);
        }
    }

    private String getErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO: return "Audio recording error.";
            case SpeechRecognizer.ERROR_CLIENT: return "Client side error.";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Insufficient permissions.";
            case SpeechRecognizer.ERROR_NETWORK: return "Network error.";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Network timeout.";
            case SpeechRecognizer.ERROR_NO_MATCH: return "No speech match.";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Recognizer is busy.";
            case SpeechRecognizer.ERROR_SERVER: return "Server error.";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "No speech input.";
            default: return "Unknown error: " + error;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }
    }
}
