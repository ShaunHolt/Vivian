package in.digibuddies.vivian;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import in.digibuddies.vivian.tts.Synthesizer;
import in.digibuddies.vivian.tts.Voice;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,ISpeechRecognitionServerEvents {

    private Synthesizer m_syn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    private String localToken = "";
    private String conversationId = "";
    private String primaryToken = "";
    private String botName = "";
    String botResponse = "";
    String idd;
    Voice v;
    Uri[] uris = new Uri[3];
    NavigationView navigationView;
    Boolean flag=false;
    private String lastResponseMsgId0="";
    public static int abc=0;
    SharedPreferences preferences;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("message");
    AnimationDrawable anim;
    TextToSpeech textToSpeech;
    String us_id;
    Uri urihi,uriidl,urithink,randomUri,urilisten;
    VideoView videoView,videoView2;
    SharedPreferences.Editor editor;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    String conversationTokenInfo0,conversationTokenInfo;
    MicrophoneRecognitionClient i_mic;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    FloatingActionButton imic;
    TextView hint;
    //keep the last Response MsgId, to check if the last response is already printed or not

    ArrayList<String> lastResponseMsgId = new ArrayList<>();
    private AudioManager myAudioManager;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        public void run() {
            pollBotResponses();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean welcomeScreenShown = preferences.getBoolean("firsttime", false);
        if (!welcomeScreenShown){
            editor = preferences.edit();
            Intent intro = new Intent(this, Help.class);
            startActivity(intro);
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.choosedialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            CircleImageView av1 = (CircleImageView)dialog.findViewById(R.id.avatar);
            CircleImageView av2 = (CircleImageView)dialog.findViewById(R.id.avatar2);
            av1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editor.putInt("avatar",1);
                    editor.apply();
                    finish();
                    startActivity(new Intent(MainActivity.this,MainActivity.class));
                }
            });
            av2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editor.putInt("avatar",2);
                    editor.apply();
                    finish();
                    startActivity(new Intent(MainActivity.this,MainActivity.class));
                }
            });
            editor.putString("userId", String.valueOf(new Random().nextInt(100000)+ Calendar.getInstance().getTime().hashCode()));
            editor.putBoolean("firsttime", true);
            editor.apply();

        }

        String subscriptionKey = "215cf609151e49f5975a8f4f24bf2ef6";
        int av = preferences.getInt("avatar",0);
        if (av==1){
            urihi = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vhi);
            uriidl = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.breathewithlook);
            uris[0] = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vspeak);
            uris[1] = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vspeak1hand);
            uris[2] = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vspeak2hand);
            randomUri = uris[new Random().nextInt(uris.length)];
            urithink = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vthink);
            urilisten = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vlisten);
            v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-CA, HeatherRUS)", Voice.Gender.Female, true);
            m_syn = new Synthesizer(subscriptionKey,videoView);
            m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
            m_syn.SetVoice(v, null);

            Observable.fromCallable(() -> {
                m_syn.SpeakToAudio("Hey there...");
                return false;
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((result) -> {
                    });
        }
        else if (av==2){
            urihi = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mvhi);
            uriidl = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mvidle);
            uris[0] = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mvspeak0);
            uris[1] = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mvspeak1);
            uris[2] = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mvspeak2);
            randomUri = uris[new Random().nextInt(uris.length)];
            urithink = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mvthink);
            urilisten = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mvlisten);
            v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, BenjaminRUS)", Voice.Gender.Male, true);
            m_syn = new Synthesizer(subscriptionKey,videoView);
            m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
            m_syn.SetVoice(v, null);

            Observable.fromCallable(() -> {
                m_syn.SpeakToAudio("Hey there...");
                return false;
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((result) -> {
                    });
        }

        Observable.fromCallable(() -> {
            conversationTokenInfo0 = startConversation();


            return false;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    conversationTokenInfo=conversationTokenInfo0;
                    JSONObject jsonObject = null;

                    if (conversationTokenInfo != "") {
                        try {
                            jsonObject = new JSONObject(conversationTokenInfo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //send message to bot and get the response using the api conversations/{conversationid}/activities
                    if (jsonObject != null) {
                        try {
                            conversationId = jsonObject.get("conversationId").toString();
                            localToken = jsonObject.get("token").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d("converstaion","started "+conversationTokenInfo);
                });
        initControls();
        idd = getSharedPreferences(getPackageName()+"_preferences",MODE_PRIVATE).getString("userId","000");
        videoView = (VideoView) findViewById(R.id.model);

        Observable.fromCallable(() -> {
            i_mic = SpeechRecognitionServiceFactory.createMicrophoneClient(this,
                    SpeechRecognitionMode.LongDictation,
                    "en-IN",
                    this,
                    subscriptionKey);
            return false;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                });
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });
        textToSpeech.setPitch((float) 1);
        textToSpeech.setSpeechRate(1);
        videoView.stopPlayback();
        videoView.setVideoURI(urihi);
        videoView.start();



        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.setVideoURI(uriidl);
                videoView.start();
            }
        });
        primaryToken = getMetaData(getBaseContext(),"botPrimaryToken");
        botName = getMetaData(getBaseContext(),"botName").toLowerCase();
        us_id=getSharedPreferences(getPackageName()+"_preferences",MODE_PRIVATE).getString("userId","000");
        runnable.run();
        ImageButton imageButton = (ImageButton)findViewById(R.id.gotochat);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,Chat.class);
                startActivity(intent);
            }
        });
//mike
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        String[] array = getResources().getStringArray(R.array.quotes);
        String randomStr = array[new Random().nextInt(array.length)];
        TextView quote = (TextView)header.findViewById(R.id.quote);
        quote.setText(randomStr);
        ImageButton menu = (ImageButton)findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.chat) {
            Intent intent=new Intent(MainActivity.this,Chat.class);
            startActivity(intent);

        }
        else if (id == R.id.hologram) {
            Intent intent=new Intent(MainActivity.this,Hologram.class);
            startActivity(intent);
        }
        else if (id == R.id.choose) {
            editor = preferences.edit();
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.choosedialog);
            dialog.show();
            CircleImageView av1 = (CircleImageView)dialog.findViewById(R.id.avatar);
            CircleImageView av2 = (CircleImageView)dialog.findViewById(R.id.avatar2);
            av1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editor.putInt("avatar",1);
                    editor.apply();
                    finish();
                    startActivity(new Intent(MainActivity.this,MainActivity.class));
                }
            });
            av2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editor.putInt("avatar",2);
                    editor.apply();
                    finish();
                    startActivity(new Intent(MainActivity.this,MainActivity.class));
                }
            });
        }
        else if (id == R.id.home) {
            //
        }
        else if (id == R.id.help) {
            Intent intent=new Intent(MainActivity.this,Help.class);
            startActivity(intent);
        }
        else if (id == R.id.aboutus) {
            Intent intent=new Intent(MainActivity.this,AboutUs.class);
            startActivity(intent);
        }
        else if (id == R.id.feedback ){
            Intent intent=new Intent(MainActivity.this,Feedback.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Hey check out this app. It can help you recover from depression and anxiety. Link: - ";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Vivian: Personal Psychotherapist");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    protected void onStartNewActivity() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        onStartNewActivity();
    }


    public void pollBotResponses()
    {   Log.d("dummy1","dum6");
        //Toast.makeText(getBaseContext(),
        //       "test",
        //     Toast.LENGTH_SHORT).show();
        String[] demo = getResources().getStringArray(R.array.demo);
        if(conversationId != "" && localToken != "") {
            Observable.fromCallable(() -> {
                botResponse = getBotResponse();
                return false;
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((result) -> {
                        if (botResponse != "") {
                            try {

                                JSONObject jsonObject = new JSONObject(botResponse);
                                int i = 0;
                                Integer arrayLength = jsonObject.getJSONArray("activities").length();
                                String curMsgId = jsonObject.getJSONArray("activities").getJSONObject(arrayLength-1).get("id").toString();
                                Message message = new Message(jsonObject.getJSONArray("activities").getJSONObject(arrayLength-1));
                                if (message.from.equals(botName)) {
                                    if(!lastResponseMsgId0.equals(curMsgId)&&!message.text.equals("")) {
                                        AddResponseToChat(message.text);
                                        adapter.notifyDataSetChanged();
                                        lastResponseMsgId0 = curMsgId;
                                        abc=0;
                                        videoView.setVideoURI(randomUri);
                                        Log.d("abcvalue0", String.valueOf(abc));
                                        hint.setText("Speaking...");
                                        videoView.pause();
                                        Observable.fromCallable(() -> {
                                            m_syn.SpeakToAudio(message.text);
                                            Log.d("abcvalue00", String.valueOf(abc));
                                            return false;
                                        })
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe((result1) -> {
                                                    while (true){
                                                        if (abc==1){
                                                            videoView.start();

                                                        }
                                                        else if (abc==2){
                                                            hint.setText("Tap to speak!");
                                                            imic.setClickable(true);
                                                            imic.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                                                            flag = false;
                                                            break;}
                                                    }

                                                });



                                        myRef.child(idd).push().setValue(message.text);

                                    }
                                }
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                        }else
                        {
                            Log.d("dummy1", "dum1");
                        }
                    });


        }

        handler.postDelayed(runnable, 1000*5);
    }


    private void initControls() {
        imic = (FloatingActionButton) findViewById(R.id.imic);
        hint = (TextView) findViewById(R.id.hint);
        myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        sayHelloToClient();
        imic.setRippleColor(Color.WHITE);
        imic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!flag){
                    flag = true;
                    imic.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                    imic.setImageResource(R.drawable.ic_close_black_24dp);
                    int MyVersion = Build.VERSION.SDK_INT;
                    if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        Observable.fromCallable(() -> {
                            if (!checkIfAlreadyhavePermission()) {
                                requestForSpecificPermission();
                            }
                            else  i_mic.startMicAndRecognition();
                            return false;
                        })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe((result) -> {
                                });
                    }
                    else {
                        i_mic.startMicAndRecognition();
                    }
                    Log.d("bings","click");
                }
                else {
                    flag=false;
                    i_mic.endMicAndRecognition();
                    hint.setText("Tap to speak!");
                    imic.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    imic.setImageResource(R.drawable.mic);
                }

            }

        });

    }

    //Get the bot response by polling a GET to directline API
    private String getBotResponse() {
        //Only for demo sake, otherwise the network work should be done over an asyns task
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        String UrlText = "https://directline.botframework.com/v3/directline/conversations/" + conversationId + "/activities";
        URL url = null;
        try {
            url = new URL(UrlText);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String responseValue = "";
        HttpURLConnection urlConnection = null;
        try {

            String basicAuth = "Bearer " + localToken;
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", basicAuth);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            int responseCode = urlConnection.getResponseCode(); //can call this instead of con.connect()
            if (responseCode >= 400 && responseCode <= 499) {
                throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
            }
            else {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                responseValue = readStream(in);

                Log.w("responseSendMsg ",responseValue);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }

        return responseValue;
    }

    //sends the message by making it an activity to the bot
    private void sendMessageToBot(String messageText) {
        //Only for demo sake, otherwise the network work should be done over an asyns task
        // StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        String UrlText = "https://directline.botframework.com/v3/directline/conversations/" + conversationId + "/activities";
        URL url = null;

        try {
            url = new URL(UrlText);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            String basicAuth = "Bearer " + localToken;

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type","message");
                jsonObject.put("text",messageText);
                jsonObject.put("from",(new JSONObject().put("id","user1")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String postData = jsonObject.toString();

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", basicAuth);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Content-Length", "" + postData.getBytes().length);
            OutputStream out = urlConnection.getOutputStream();
            out.write(postData.getBytes());

            int responseCode = urlConnection.getResponseCode(); //can call this instead of con.connect()
            if (responseCode >= 400 && responseCode <= 499) {
                throw new Exception("Bad authentication status: " + responseCode); //provide a more meaningful exception message
            }
            else {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String responseValue = readStream(in);
                Log.w("responseSendMsg ",responseValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }

    }


    //returns the conversationID
    private String startConversation()
    {
        //Only for demo sake, otherwise the network work should be done over an asyns task
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);
        Log.d("converstaion","started123");
        String UrlText = "https://directline.botframework.com/v3/directline/conversations";
        URL url = null;
        String responseValue = "";

        try {
            url = new URL(UrlText);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        primaryToken = getMetaData(getBaseContext(),"botPrimaryToken");
        try {
            String basicAuth = "Bearer "  + primaryToken;
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", basicAuth);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("converstaion","excep1");
        }
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            responseValue = readStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }

        return  responseValue;
    }

    //read the chat bot response
    private String readStream(InputStream in) {
        char[] buf = new char[2048];
        Reader r = null;
        try {
            r = new InputStreamReader(in, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder s = new StringBuilder();
        while (true) {
            int n = 0;
            try {
                n = r.read(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (n < 0)
                break;
            s.append(buf, 0, n);
        }

        Log.w("streamValue",s.toString());
        return s.toString();
    }


    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
    }


    private void sayHelloToClient() {

        chatHistory = new ArrayList<ChatMessage>();

        /*ChatMessage msg = new ChatMessage();
        msg.setId("1");
        msg.setMe(false);
        msg.setMessage("Hello");
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg);
*/
        adapter = new ChatAdapter(MainActivity.this, new ArrayList<ChatMessage>());
        /*
        for (int i = 0; i < chatHistory.size(); i++) {
            ChatMessage message = chatHistory.get(i);
            displayMessage(message);
        }*/
    }

    /*
    Add the bot response to chat window
     */
    private void AddResponseToChat(String botResponse)
    {
        ChatMessage message = new ChatMessage();
        //message.setId(2);
        message.setMe(false);
        message.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        message.setMessage(botResponse);
        displayMessage(message);
        Log.d("messagetextrespo",botResponse);
    }


    /*
    Get metadata from manifest file against a given key
     */
    public static String getMetaData(Context context, String name) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.getString(name);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("Metadata", "Unable to load meta-data: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPartialResponseReceived(String s) {

        Log.d("bings","click2");
    }

    @Override
    public void onFinalResponseReceived(RecognitionResult recognitionResult) {
        i_mic.endMicAndRecognition();
        if(recognitionResult.Results.length>0) {
            click(String.valueOf(recognitionResult.Results[0].DisplayText));
            hint.setText("Thinking...");
            imic.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            imic.setImageResource(R.drawable.mic);
            imic.setClickable(false);
            videoView.pause();
            videoView.setVideoURI(urithink);
            videoView.start();

        }
        Log.d("bings","click3");
    }

    @Override
    public void onIntentReceived(String s) {

    }

    @Override
    public void onError(int i, String s) {

        Log.d("bings", String.valueOf(i));
        i_mic.endMicAndRecognition();
    }

    @Override
    public void onAudioEvent(boolean b) {
        if (b){
            final MediaPlayer mp = MediaPlayer.create(this, R.raw.but);
            hint.setText("Listening...");
            videoView.pause();
            videoView.setVideoURI(urilisten);
            videoView.start();
            mp.start();}
        else {
            final MediaPlayer mp = MediaPlayer.create(this, R.raw.but3);
            mp.start();

        }
        Log.d("bings","click4");
    }

    private void sndmsg(String s){

        if (conversationId != "") {
            sendMessageToBot(s);
        }
        //return null;
    }
    //}

    @Override
    protected void onResume() {
        super.onResume();
        videoView.start();
        if (anim != null && !anim.isRunning())
            anim.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
        navigationView.setCheckedItem(R.id.home);
    }
    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 101);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    i_mic.startMicAndRecognition();
                } else {
                    //not granted
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void click(String m){
        myRef.child(idd).push().setValue(m);
        if (TextUtils.isEmpty(m)) {
            return;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(us_id);//dummy
        chatMessage.setMessage(m);
        Log.d("messagetext", m);
        chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatMessage.setMe(true);
        displayMessage(chatMessage);
        //new Connection().execute(messageText);
        Observable.fromCallable(() -> {
            sendMessageToBot(m);
            return false;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    handler.postDelayed(runnable, 1000);
                });
    }


}
