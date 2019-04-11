package in.digibuddies.vivian;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import de.hdodenhof.circleimageview.CircleImageView;
import in.digibuddies.vivian.R.layout.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class Chat extends AppCompatActivity {
    private ListView messagesContainer;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    private String localToken = "";
    private String conversationId = "";
    private String primaryToken = "";
    private String botName = "";
    String botResponse = "";
    String idd;
    String lastResponseMsgId0="";
    private int watermark=-1;
    private LinearLayout linerlayout;
    ProgressBar progressBar;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("message");
    AnimationDrawable anim;
    TextInputLayout inputLayout;
    String us_id;
    String conversationTokenInfo0,conversationTokenInfo;
    EditText input;
    ImageButton im;
    ArrayList<String> lastResponseMsgId = new ArrayList<>();

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        public void run() {
            pollBotResponses();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
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
        RelativeLayout rel = (RelativeLayout) findViewById(R.id.rel);
        anim = (AnimationDrawable) rel.getBackground();
        anim.setEnterFadeDuration(6000);
        anim.setExitFadeDuration(2000);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int avatar = preferences.getInt("avatar",1);
        if (avatar==2){
            CircleImageView av = (CircleImageView)findViewById(R.id.avatar);
            av.setImageResource(R.drawable.vivian2);
        }
        idd = getSharedPreferences(getPackageName()+"_preferences",MODE_PRIVATE).getString("userId","000");
        primaryToken = getMetaData(getBaseContext(),"botPrimaryToken");
        botName = getMetaData(getBaseContext(),"botName").toLowerCase();
        us_id=getSharedPreferences(getPackageName()+"_preferences",MODE_PRIVATE).getString("userId","000");
       runnable.run();
    }

    public void pollBotResponses()
    {   Log.d("dummy1","dum6");
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
                                Integer arrayLength = jsonObject.getJSONArray("activities").length();
                                String curMsgId = jsonObject.getJSONArray("activities").getJSONObject(arrayLength-1).get("id").toString();
                                    Message message = new Message(jsonObject.getJSONArray("activities").getJSONObject(arrayLength-1));
                                    if (message.from.equals(botName)) {
                                        if(!lastResponseMsgId0.equals(curMsgId)&&!message.text.equals("")) {
                                            AddResponseToChat(message.text);
                                            if (progressBar.getVisibility()==View.VISIBLE)
                                            {progressBar.setVisibility(View.GONE);
                                                linerlayout = (LinearLayout)findViewById(R.id.ll2);
                                                linerlayout.setVisibility(View.VISIBLE);
                                            }
                                            adapter.notifyDataSetChanged();
                                            lastResponseMsgId0 = curMsgId;
                                            inputLayout.setHint("Enter Text...");
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
        messagesContainer = (ListView) findViewById(R.id.messagescontainer);
        im = (ImageButton) findViewById(R.id.imageButton);
        input = (EditText)findViewById(R.id.input);
        inputLayout = (TextInputLayout)findViewById(R.id.til);
        progressBar = (ProgressBar)findViewById(R.id.progressBar2);
        progressBar.setIndeterminate(true);
        ImageButton back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        sayHelloToClient();
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = input.getText().toString();
                myRef.child(idd).push().setValue(messageText);
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(us_id);//dummy
                chatMessage.setMessage(messageText);
                Log.d("messagetext",messageText);
                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage.setMe(true);

                input.setText("");
                displayMessage(chatMessage);
                Observable.fromCallable(() -> {
                    sendMessageToBot(messageText);
                    return false;
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((result) -> {
                            handler.postDelayed(runnable, 1000);
                        });

                InputMethodManager imm = (InputMethodManager)Chat.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                }


            }
        });
    }

    //Get the bot response by polling a GET to directline API
    private String getBotResponse() {
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
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
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
        adapter = new ChatAdapter(Chat.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);
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
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    /*private class Connection extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... arg0) {
            String conversationTokenInfo = startConversation();*/
    private void sndmsg(String s){

        if (conversationId != "") {
            sendMessageToBot(s);
        }
        //return null;
    }
    //}

    @Override
    public void finish() {
        super.finish();
        onLeaveThisActivity();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (anim != null && !anim.isRunning())
            anim.start();
    }
    protected void onLeaveThisActivity() {
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}

