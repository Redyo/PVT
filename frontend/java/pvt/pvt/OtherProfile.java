package pvt.pvt;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OtherProfile extends AppCompatActivity implements Serializable {
    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://pvt2017.herokuapp.com/").addConverterFactory(GsonConverterFactory.create()).build();
    final HerokuInterfaceService herokuService = retrofit.create(HerokuInterfaceService.class);

    private ExpandableListAdapterForOtherProfile listAdapter;
    private ExpandableListView expListView;
    private Person person;
    private TextView listLikes;
    private List<String> headers;
    private HashMap<String, List<Event>> events;
    private HashMap<String, List<Place>> places;
    private HashMap<String, List<Child>> children;
    private boolean favorite = false;
    private long myUserID;
    private long otherUserID;
    private String firstName;
    private String lastName;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_profile_screen);

        headers = new ArrayList<>();
        children = new HashMap<>();
        places = new HashMap<>();
        events = new HashMap<>();
        headers.add("Barn");

        otherUserID = getIntent().getLongExtra("ID", 0);

        listLikes = (TextView) findViewById(R.id.textView2);

        final Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar3);
        mActionBarToolbar.setTitle("Profil");
        setSupportActionBar(mActionBarToolbar);

        //hämta min fb info:
        try {
            new OtherProfile.RetrieveUserFBId().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        person = new Person("", "", 0);
        Call<ResponseBody> userInfo = herokuService.getUser(otherUserID);
        userInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray personInfo = new JSONArray(response.body().string());
                    JSONObject personObject = personInfo.getJSONObject(0);
                    firstName = personObject.get("first_name").toString();
                    lastName = personObject.get("last_name").toString();
                    person.setFirstName(firstName);
                    person.setLastName(lastName);
                    //info.setText(person.getName());
                    mActionBarToolbar.setTitle(person.getName());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v("Profile, onFailure", "Unable to connect to heroku");
            }
        });

        Call<ResponseBody> userChildrenInfo = herokuService.getUserChildren(otherUserID);
        userChildrenInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray personInfo = new JSONArray(response.body().string());
                    for (int i = 0; i < personInfo.length(); i++) {
                        JSONObject obj = personInfo.getJSONObject(i);
                        person.addChild(new Child(Integer.parseInt(obj.get("child_id").toString()), Integer.parseInt(obj.get("age").toString())));
                        children.put(headers.get(0), person.getChildren());
                    }

                    expListView.expandGroup(0);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v("Profile, onFailure", "Unable to connect to heroku");
            }
        });

        Call<ResponseBody> userLikesInfo = herokuService.getAmountOfLikes(otherUserID);
        userLikesInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray personInfo = new JSONArray(response.body().string());
                    for (int i = 0; i < personInfo.length(); i++) {
                        JSONObject obj = personInfo.getJSONObject(i);
                        person.setLikes(Integer.parseInt(obj.get("likes").toString()));
                        listLikes.setText(person.getLikes() + " rekommendationer");
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v("Profile, onFailure", "Unable to connect to heroku");
            }
        });

        final ImageButton likes = (ImageButton) findViewById(R.id.likeButton);

        Call<ResponseBody> myLikedUsers = herokuService.getLikedUsers(myUserID);
        myLikedUsers.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray likedUsers = new JSONArray(response.body().string());
                    for (int i = 0; i < likedUsers.length(); i++) {
                        JSONObject obj = likedUsers.getJSONObject(i);
                        if (obj.get("liked_id").toString().equals(String.valueOf(otherUserID))) {
                            favorite = true;
                            likes.setImageResource(R.drawable.ic_star_filled);
                        }

                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v("Profile, onFailure", "Unable to connect to heroku");
            }
        });

        likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favorite) {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("liker_id", myUserID);
                        obj.put("liked_id", otherUserID);
                        Call<ResponseBody> makeCall = herokuService.deleteLike(obj);
                        makeCall.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                            }
                        });
                        Toast.makeText(OtherProfile.this, "Du rekommenderar inte längre " + person.getFirstName(), Toast.LENGTH_LONG).show();
                        likes.setImageResource(R.drawable.ic_star_empty);
                        int likes = person.getLikes();
                        person.setLikes(--likes);
                        listLikes.setText("" + person.getLikes() + " rekommendationer");
                        favorite = false;
                    } catch (JSONException e) {

                    }
                } else {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("liker_id", myUserID);
                        obj.put("liked_id", otherUserID);
                        Call<ResponseBody> makeCall = herokuService.createLike(obj);
                        makeCall.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                Toast.makeText(OtherProfile.this, "Du rekommenderar nu " + person.getFirstName(), Toast.LENGTH_LONG).show();
                                likes.setImageResource(R.drawable.ic_star_filled);
                                int likes = person.getLikes();
                                person.setLikes(++likes);
                                listLikes.setText("" + person.getLikes() + " rekommendationer");
                                favorite = true;
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                            }
                        });

                    } catch (JSONException e) {

                    }
                }
            }
        });

        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        listAdapter = new ExpandableListAdapterForOtherProfile(this, headers, children, places, events);
        expListView.setAdapter(listAdapter);

        imageView = (ImageView) findViewById(R.id.image);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL fbPictureUrl = new URL("https://graph.facebook.com/" + String.valueOf(otherUserID) + "/picture?type=large");
            InputStream inputStream = (InputStream) fbPictureUrl.getContent();
            Bitmap fbUserImage = (BitmapFactory.decodeStream(inputStream));
            imageView.setImageBitmap(fbUserImage);
        } catch (IOException e) {
            Log.v("OtherProfile", "IOException");
        }
    }

    private class RetrieveUserFBId extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            GraphRequest postsRequest = new GraphRequest(AccessToken.getCurrentAccessToken(),
                    "/me",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            try {
                                JSONObject obj = response.getJSONObject();
                                myUserID = Long.parseLong(obj.get("id").toString());
                            } catch (JSONException e) {
                                Log.v("Profile, RetrievePosts", "JSONException");
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "posts.limit(20)");
            postsRequest.setParameters(parameters);
            postsRequest.executeAndWait();
            return "" + myUserID;
        }
    }

    public void reportPerson() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                Toast.makeText(OtherProfile.this, "Personen har anmälts.", Toast.LENGTH_LONG).show();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Vill du anmäla den här personen för olämpligt beteende?")
                .setPositiveButton("Ja", dialogClickListener)
                .setNegativeButton("Nej", null).show();
    }

    public void buttonOnClickReport(final View v) {
        reportPerson();
    }
}