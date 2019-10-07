package pvt.pvt;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatMain extends Activity {
    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://pvt2017.herokuapp.com/").addConverterFactory(GsonConverterFactory.create()).build();
    final HerokuInterfaceService herokuService = retrofit.create(HerokuInterfaceService.class);
    private ChatArrayAdapter chatArrayAdapter;
    private EditText chatText;
    private int eventId;
    private Toolbar toolbar;
    private String userName;
    private Long userFBId;
    private ListView list;
    private boolean typed;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_main);

        toolbar = (Toolbar) findViewById(R.id.toolchat);
        toolbar.setTitle("Aktivitetschatt");

        eventId = getIntent().getIntExtra("ID", 0);
        //Hämtar användarens FB-user-id:
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me", null, HttpMethod.GET, new GraphRequest.Callback() {
            public void onCompleted(GraphResponse response) {
                try {
                    JSONObject obj = response.getJSONObject();
                    userFBId = Long.parseLong(obj.getString("id"));
                    userName = obj.getString("name");
                } catch (JSONException e) {
                    Log.v("ChatMain", e.toString());
                }
            }
        }
        ).executeAsync();
        list = (ListView) findViewById(R.id.listview);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.chat_main);
        list.setAdapter(chatArrayAdapter);

        getChatMessages();
        handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                getNewMessages();
                handler.postDelayed(this, 2 * 1000);
            }
        }, 2 * 1000);

        chatText = (EditText) findViewById(R.id.chat_text);
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (validMessage(chatText.getText().toString()))
                        return sendChatMessage();
                }
                return false;
            }
        });
        Button sendButton = (Button) findViewById(R.id.btn);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validMessage(chatText.getText().toString()))
                    sendChatMessage();
            }
        });
//        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setAdapter(chatArrayAdapter);
    }

    private boolean validMessage(String msg) {
        msg = msg.trim();
        return !msg.equals("") || msg.length() != 0;
    }

    private void getChatMessages() {
        try {
            Call<ResponseBody> previousChat = herokuService.selectEventChat(eventId);
            previousChat.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONArray chatMessages = new JSONArray(response.body().string());

                        for (int i = 0; i < chatMessages.length(); i++) {
                            JSONObject obj = chatMessages.getJSONObject(i);
                            ChatMessage cm = new ChatMessage(obj.getString("name"), obj.getString("message"), obj.getString("date_time"));
                            chatArrayAdapter.add(cm);
                            chatArrayAdapter.notifyDataSetChanged();
                        }

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        } catch (ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void getNewMessages() {
        try {
            Call<ResponseBody> previousChat = herokuService.selectEventChat(eventId);
            previousChat.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONArray chatMessages = new JSONArray(response.body().string());
                        boolean messageAdded = false;

                        for (int i = 0; i < chatMessages.length(); i++) {

                            JSONObject obj = chatMessages.getJSONObject(i);
                            ChatMessage cm = new ChatMessage(obj.getString("name"), obj.getString("message"), obj.getString("date_time"));
                            if (chatArrayAdapter.getCount() > i) {
                                if (!chatArrayAdapter.getItem(i).isTheSameAs(cm)) {
                                    chatArrayAdapter.add(cm);
                                }
                            } else {
                                chatArrayAdapter.add(cm);
                            }
                        }
                        chatArrayAdapter.notifyDataSetChanged();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        } catch (ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
    }


    private boolean sendChatMessage() {
        try {
            chatArrayAdapter.notifyDataSetChanged();
            Date today = Calendar.getInstance().getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("hh.mm" + " - " + "MM/dd", Locale.GERMANY);
            String date = formatter.format(today);
            JSONObject chatToPost = new JSONObject();
            chatToPost.put("event_id", eventId);
            chatToPost.put("user_id", userFBId);
            chatToPost.put("message", chatText.getText().toString());
            Call<ResponseBody> postChat = herokuService.insertEventChat(chatToPost);
            postChat.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
            chatText.setText("");
            return true;
        } catch (JSONException e) {

        }

        return false;
    }

    @Override
    public void onBackPressed() {
        handler.removeMessages(0);
        super.onBackPressed();
    }

}
