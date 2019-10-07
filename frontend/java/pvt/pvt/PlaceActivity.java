package pvt.pvt;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlaceActivity extends AppCompatActivity implements OnMapReadyCallback {
    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://pvt2017.herokuapp.com/").addConverterFactory(GsonConverterFactory.create()).build();
    final HerokuInterfaceService herokuService = retrofit.create(HerokuInterfaceService.class);
    private GoogleMap mMap;
    private ExpandableListAdapterForPlaceActivity listAdapter;
    private ExpandableListView expListView;
    private Toolbar toolbar;
    static PlaceActivity placeActivity;

    private int placeId;
    private int placeType;
    private String placeName;
    private String placeDescription;
    private long myUserID;
    private boolean favorite;

    private ImageButton favBtn;
    private ImageView placeIcon;
    private Button addEventBtn;
    private Button viewMapBtn;
    private SupportMapFragment mapFragment;

    private List<String> headers = new ArrayList<>();
    private HashMap<String, List<Event>> events = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_activity);

        Bundle extras = getIntent().getExtras();
        placeId = extras.getInt("ID");
        placeActivity = this;
        favorite = false; // favorite is false until proven otherwise

        final TextView emptyMessage = (TextView) findViewById(R.id.emptyText);
        favBtn = (ImageButton) findViewById(R.id.favPlaceBtn);
        toolbar = (Toolbar) findViewById(R.id.toolpark);
        placeIcon = (ImageView) findViewById(R.id.type);
        addEventBtn = (Button) findViewById(R.id.addEventBtn);
        viewMapBtn = (Button) findViewById(R.id.viewMapBtn);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        listAdapter = new ExpandableListAdapterForPlaceActivity(PlaceActivity.this, headers, events);
        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        expListView.setAdapter(listAdapter);
        setExpandableListView();

        setSupportActionBar(toolbar);
        mapFragment.getMapAsync(this);

        initiateButtons();
        getUserFbId();
        getLocationEvents();
        setFavouriteBtn();
    }

    private void setFavouriteBtn() {
        Call<ResponseBody> checkIfFavourite = herokuService.checkIfFavourite3(myUserID, placeId);
        checkIfFavourite.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if(!response.body().string().equals("[]")) {
                        favorite = true;
                        favBtn.setImageResource(R.drawable.ic_star_filled);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v("Profile, onFailure", "Unable to connect to heroku");
            }
        });
    }

    private void getLocationEvents() {
        Call<ResponseBody> findEvents = herokuService.selectEventsByLocation(placeId);
        findEvents.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray temp = new JSONArray(response.body().string());
                    if (temp.length() > 0) {
                        for (int i = 0; i < temp.length(); i++) {
                            JSONObject obj = temp.getJSONObject(i);
                            Event e = new Event(Integer.parseInt(obj.get("event_id").toString()), obj.get("name").toString(),
                                    obj.get("date").toString(), obj.get("start_time").toString(),
                                    obj.get("end_time").toString(), obj.get("type_name").toString(), obj.get("description").toString(),
                                    Integer.parseInt(obj.get("noOfAttendees").toString()));
                            addEvent(e);
                            listAdapter = new ExpandableListAdapterForPlaceActivity(PlaceActivity.this, headers, events);
                            expListView.setAdapter(listAdapter);
                            setExpandableListView();
                        }
                    } else {
                        TextView empty = (TextView) findViewById(R.id.emptyText);
                        empty.setText("Nya vänner väntar runt hörnet!\n\nSchemalägg en aktivitet på denna plats\ngenom att klicka på knappen ovanför.");
                    }
                } catch (IOException | JSONException e) {
                    Log.v("PlaceActivity", "Exception in OnResponse");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v("PlaceActivity", "OnFailure");
            }
        });
    }

    private void getUserFbId() {
        try {
            new PlaceActivity.RetrieveUserFBId().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void initiateButtons() {
        addEventBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newEvent();
            }
        });

        viewMapBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openGoogleMapsApp();
            }
        });

        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("user_id", myUserID);
                    obj.put("location_id", placeId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!favorite) {
                    Call<ResponseBody> makeCall = herokuService.createFavoriteLocation(obj);
                    makeCall.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Toast.makeText(PlaceActivity.this, "Plats markerad som favorit.", Toast.LENGTH_LONG).show();
                            favBtn.setImageResource(R.drawable.ic_star_filled);
                            favorite = true;
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(PlaceActivity.this, "Error: HTTP-response failure.", Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    Call<ResponseBody> makeCall = herokuService.deleteFavoriteLocation(obj);
                    makeCall.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Toast.makeText(PlaceActivity.this, "Plats borttagen som favorit.", Toast.LENGTH_LONG).show();
                            favBtn.setImageResource(R.drawable.ic_star_empty);
                            favorite = false;
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(PlaceActivity.this, "Error: HTTP-response failure.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void initiateIcon() {
        placeIcon.setFocusable(false);

        switch (placeType) {
            case 3:
                placeIcon.setImageResource(R.drawable.skol1);
                break;
            case 2:
                placeIcon.setImageResource(R.drawable.sim1);
                break;
            default:
            case 1:
                placeIcon.setImageResource(R.drawable.sand1);
                break;
        }
    }

    public static PlaceActivity getInstance(){
        return placeActivity;
    }

    public void restart(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void setExpandableListView() {
        // setOnChildClickListener listener for child row click
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Event e = (Event) listAdapter.getChild(groupPosition, childPosition);
                Log.v("Test", events.get(headers.get(groupPosition)).get(childPosition).getEventId() + "");
                goToEvent(events.get(headers.get(groupPosition)).get(childPosition).getEventId());

                return true;
            }
        });

        for (int i = 0; i < headers.size(); i++) {
            expListView.expandGroup(i);
        }
    }


    private String generateUriString() {
        String uri = "google.navigation:q=";
        uri += placeName + "&mode=w";
        return uri;
    }

    private void openGoogleMapsApp() {
        Uri gmmIntentUri = Uri.parse(generateUriString());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void goToEvent(int eventId) {
        Intent intent = new Intent(this, EventActivity.class);
        intent.putExtra("ID", eventId);
        startActivity(intent);
    }

    private void newEvent() {
        Intent intent = new Intent(this, CreateEventActivity.class);
        intent.putExtra("ID", placeId);
        intent.putExtra("parkName", placeName);
        startActivity(intent);
    }

    public boolean checkIfHeaderExists(String header) {
        for (String head : headers) {
            if (head.equals(header)) {
                return true;
            }
        }
        return false;
    }

    public void addEvent(Event e) {
        String header = e.getDate();
        if (events.get(header) != null) {
            if (!checkIfHeaderExists(header)) {
                headers.add(header);
                ArrayList<Event> list = new ArrayList<>();
                list.addAll(events.get(header));
                list.add(e);
                events.put(header, list);
            } else {
                ArrayList<Event> list = new ArrayList<>();
                list.addAll(events.get(header));
                list.add(e);
                events.put(header, list);
            }
        } else {
            headers.add(header);
            ArrayList<Event> list = new ArrayList<>();
            list.add(e);
            events.put(header, list);
        }
        Collections.sort(headers);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        Call<ResponseBody> locationCallback = herokuService.getLocation(placeId);
        locationCallback.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray temp = new JSONArray(response.body().string());
                    JSONObject obj = temp.getJSONObject(0);
                    placeName = obj.getString("name_short");
                    placeType = obj.getInt("location_type");
                    placeDescription = obj.getString("description");
                    toolbar.setTitle(placeName);
                    initiateIcon();
                    mMap = googleMap;
                    CoordinateConverter cordi = new CoordinateConverter();
                    double[] coords = cordi.grid_to_geodetic(obj.getInt("position_x"), obj.getInt("position_y"));
                    LatLng park = new LatLng(coords[0], coords[1]);
                    mMap.addMarker(new MarkerOptions().position(park).title(placeName));
                    float zoomLevel = 14.0f;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(park, zoomLevel));
                } catch (IOException | JSONException e) {
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });


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

    public void showPlaceDescription() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Platsbeskrivning:\n\n" + placeDescription)
                .setNegativeButton("Stäng", null).show();
    }

    public void buttonOnClickDescription(final View v) {
        showPlaceDescription();
    }
}