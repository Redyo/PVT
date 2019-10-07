package pvt.pvt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Profile extends Fragment {
    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://pvt2017.herokuapp.com/").addConverterFactory(GsonConverterFactory.create()).build();
    final HerokuInterfaceService herokuService = retrofit.create(HerokuInterfaceService.class);
    MyReceiver r;
    private ImageView imageView;
    private View specialView;
    private ExpandableListAdapterForProfile listAdapter;
    private ExpandableListView expListView;
    private Button editProfile;
    private List<String> headers = new ArrayList<>();
    private Person me;
    private TextView personsNameHeader;
    private TextView mountainOfLikes;
    private String firstName;
    private String lastName;
    private long userID;
    private ImageView likeImage;
    private Map<String, String> names = new HashMap<>();
    private CharSequence[] items;
    private boolean gettingUserInfo, gettingChildren, gettingFavLocations, gettingCreatedEvents, gettingUserLikes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        headers.add("Mina skapade aktiviteter");
        headers.add("Mina barn");
        headers.add("Mina favoritplatser");

        me = new Person("", "", 0);

        //Hämtar användarens FB-user-id:
        try {
            new RetrievePosts().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void getUserInfo() {
        if (!gettingUserInfo) {
            gettingUserInfo = true;
            Call<ResponseBody> userInfo = herokuService.getUser(userID);
            userInfo.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful()) {
                            JSONArray personInfo = new JSONArray(response.body().string());
                            JSONObject personObject = personInfo.getJSONObject(0);
                            firstName = personObject.get("first_name").toString();
                            lastName = personObject.get("last_name").toString();
                            me.setFirstName(firstName);
                            me.setLastName(lastName);
                            personsNameHeader.setText(me.getName());
                        } else {
                            Toast.makeText(getContext(), "serverfel: " + response.message(), Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException | JSONException e) {
                        Toast.makeText(getContext(), "fel: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    } finally {
                        gettingUserInfo = false;
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.v("Profile, onFailure", "Unable to connect to heroku");
                }
            });
        }
    }

    private void getUserChildren() {
        if (!gettingChildren) {
            gettingChildren = true;
            Call<ResponseBody> userChildrenInfo = herokuService.getUserChildren(userID);
            userChildrenInfo.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONArray personInfo = new JSONArray(response.body().string());
                        for (int i = 0; i < personInfo.length(); i++) {
                            JSONObject obj = personInfo.getJSONObject(i);
                            me.addChild(new Child(obj.getInt("child_id"), obj.getInt("age")));
                            updateView();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    } finally {
                        gettingChildren = false;
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.v("Profile, onFailure", "Unable to connect to heroku");
                }
            });
        }
    }

    private void getUserFavLocations() {
        if (!gettingFavLocations) {
            gettingFavLocations = true;
            Call<ResponseBody> userLocationInfo = herokuService.getUserLocations(userID);
            userLocationInfo.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONArray personInfo = new JSONArray(response.body().string());
                        for (int i = 0; i < personInfo.length(); i++) {
                            JSONObject obj = personInfo.getJSONObject(i);
                            me.addFavPlace(new Place(obj.get("name").toString(), Integer.parseInt(obj.get("location_id").toString()), obj.get("location_type").toString()));
                            updateView();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    } finally {
                        gettingFavLocations = false;
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.v("Profile, onFailure", "Unable to connect to heroku");
                }
            });
        }
    }

    private void getUserCreatedEvents() {
        if (!gettingCreatedEvents) {
            gettingCreatedEvents = true;
            Call<ResponseBody> userEventInfo = herokuService.selectEventsCreatedByUser(userID);
            userEventInfo.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONArray personInfo = new JSONArray(response.body().string());
                        for (int i = 0; i < personInfo.length(); i++) {
                            JSONObject obj = personInfo.getJSONObject(i);
                            me.addMyEvent(new Event(Integer.parseInt(obj.get("event_id").toString()), obj.get("name_short").toString(), obj.get("date").toString(), obj.get("start_time").toString(), obj.get("end_time").toString(), obj.get("type_name").toString(), obj.get("description").toString()));
                            updateView();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    } finally {
                        gettingCreatedEvents = false;
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.v("Profile, onFailure", "Unable to connect to heroku");
                }
            });
        }
    }

    private void getUserLikes() {
        if (!gettingUserLikes) {
            gettingUserLikes = true;
            Call<ResponseBody> userLikesInfo = herokuService.getAmountOfLikes(userID);
            userLikesInfo.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONArray personInfo = new JSONArray(response.body().string());
                        for (int i = 0; i < personInfo.length(); i++) {
                            JSONObject obj = personInfo.getJSONObject(i);
                            me.setLikes(Integer.parseInt(obj.get("likes").toString()));
                            mountainOfLikes.setText(obj.get("likes").toString());
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    } finally {
                        gettingUserLikes = false;
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.v("Profile, onFailure", "Unable to connect to heroku");
                }
            });
        }
    }

    private class RetrievePosts extends AsyncTask<String, String, String> {
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
                                userID = Long.parseLong(obj.get("id").toString());
                                me.setFacebookID(userID);
                            } catch (JSONException e) {
                                Log.v("Profile, RetrievePosts", "JSONException");
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "posts.limit(20)");
            postsRequest.setParameters(parameters);
            postsRequest.executeAndWait();
            return "" + userID;
        }
    }

    public void refresh() {
        listAdapter = new ExpandableListAdapterForProfile(getContext(), headers, me);
        expListView = (ExpandableListView) specialView.findViewById(R.id.lvExp);
        expListView.setAdapter(listAdapter);
        editProfile.setText("REDIGERA");
        headers.clear();
        headers.add("Mina skapade aktiviteter");
        headers.add("Mina barn");
        headers.add("Mina favoritplatser");
        me = new Person("", "", 0);
        try {
            new RetrievePosts().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        getUserInfo();
        getUserChildren();
        getUserFavLocations();
        getUserCreatedEvents();
        getUserLikes();

    }

    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(r);
    }

    public void onResume() {
        super.onResume();
        r = new MyReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(r,
                new IntentFilter("TAG_REFRESH"));
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Profile.this.refresh();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_screen, container, false);
        specialView = view;
        imageView = (ImageView) view.findViewById(R.id.image);

        editProfile = (Button) view.findViewById(R.id.edit_button);
        listAdapter = new ExpandableListAdapterForProfile(getContext(), headers, me);
        editProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listAdapter.showBtns(!listAdapter.isBtnsVisible());
                editProfile.setText(listAdapter.isBtnsVisible() ? R.string.text_done : R.string.text_edit);
            }
        });
        //Changes thread policy to prevent fatal exception in: android.os.StrictMode$AndroidBlockGuardPolicy.onNetwork(StrictMode.java:1303).
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me", null, HttpMethod.GET, new GraphRequest.Callback() {
            public void onCompleted(GraphResponse response) {
                try {
                    JSONObject obj = response.getJSONObject();
                    URL fbPictureUrl = new URL("https://graph.facebook.com/" + obj.getString("id") + "/picture?type=large");
                    InputStream inputStream = (InputStream) fbPictureUrl.getContent();
                    Bitmap fbUserImage = (BitmapFactory.decodeStream(inputStream));
                    imageView.setImageBitmap(fbUserImage);
                } catch (JSONException | IOException e) {
                    Log.v("Profile, RetrievePosts", "JSONException");
                }
            }
        }).executeAsync();

        likeImage = (ImageView) view.findViewById(R.id.imageView);
        likeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                names = new HashMap<>();
                Call<ResponseBody> likersInfo = herokuService.getLikers(userID);
                likersInfo.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            JSONArray personInfo = new JSONArray(response.body().string());
                            for (int i = 0; i < personInfo.length(); i++) {
                                JSONObject obj = personInfo.getJSONObject(i);
                                String name = obj.get("first_name").toString() + " " + obj.get("last_name").toString();
                                String id = obj.get("user_id").toString();
                                names.put(id, name);
                            }
                            Collection<String> keys = names.values();       //ger namnen
                            items = keys.toArray(new CharSequence[names.size()]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Användare som rekommenderar dig:");
                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    for (Map.Entry<String, String> entry : names.entrySet()) {
                                        if (entry.getValue().equals(items[item].toString())) {
                                            Intent intent = new Intent(getContext(), OtherProfile.class);
                                            Long id = Long.parseLong(entry.getKey());
                                            intent.putExtra("ID", id);
                                            startActivity(intent);
                                        }
                                    }
                                }
                            });

                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.v("Profile, onFailure", "Unable to connect to heroku");
                    }
                });
            }
        });


        personsNameHeader = (TextView) view.findViewById(R.id.profile_name);

        mountainOfLikes = (TextView) view.findViewById(R.id.amount_of_likes);
        mountainOfLikes.setText("0");
        updateView();
        return view;
    }

    public void updateView() {
        listAdapter = new ExpandableListAdapterForProfile(getContext(), headers, me);
        expListView = (ExpandableListView) specialView.findViewById(R.id.lvExp);
        expListView.setAdapter(listAdapter);
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (headers.get(groupPosition).equals("Mina barn")) {
                    Toast.makeText(
                            Profile.this.getContext(), headers.get(groupPosition) + " : " +
                                    me.getChildren().get(childPosition), Toast.LENGTH_SHORT).show();
                    return false;
                } else if (headers.get(groupPosition).equals("Mina skapade aktiviteter")) {
                    Intent intent = new Intent(getContext(), EventActivity.class);
                    ArrayList<Event> myEvents = me.getMyEvents();
                    intent.putExtra("ID", myEvents.get(childPosition).getEventId());
                    startActivity(intent);
                    return false;
                } else if (headers.get(groupPosition).equals("Mina favoritplatser")) {
                    Intent intent = new Intent(getContext(), PlaceActivity.class);
                    ArrayList<Place> myPlaces = me.getFavPlaces();
                    intent.putExtra("ID", myPlaces.get(childPosition).getId());
                    startActivity(intent);
                    return false;

                }
                return false;
            }
        });
        for (int i = 0; i < headers.size(); i++) {
            expListView.expandGroup(i);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem logout = menu.add("Logga ut");
        MenuItem deleteAccount = menu.add("Ta bort konto");

        logout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(getContext(), LoginScreen.class);
                startActivity(intent); //TODO
                return true;
            }
        });

        deleteAccount.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Vill du verkligen radera ditt konto?");
                builder.setCancelable(false);
                builder.setPositiveButton("Ja",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    JSONObject obj = new JSONObject();
                                    obj.put("user_id", userID);
                                    Call<ResponseBody> makeCall = herokuService.deleteAccount(obj);
                                    makeCall.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            LoginManager.getInstance().logOut();
                                            Intent intent = new Intent(getContext(), LoginScreen.class);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        }
                                    });
                                } catch (JSONException e) {
                                    Log.v("ExpListAd, JSONEx", e.toString());
                                }

                            }
                        });
                builder.setNegativeButton("Nej",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

}
