package pvt.pvt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeView extends Fragment {
    MyReceiver r;
    private long userID;
    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://pvt2017.herokuapp.com/").addConverterFactory(GsonConverterFactory.create()).build();
    final HerokuInterfaceService herokuService = retrofit.create(HerokuInterfaceService.class);

    private ExpandableListView expListViewForBookedEvents;
    private ExpandableListView expListViewForFavPlaceEvents;
    private ExpandableListView expListViewForSthlmEvents;

    private ExpandableListAdapterForHomeView listAdapterForBookedEvents;
    private ExpandableListAdapterForHomeView listAdapterForFavPlaceEvents;
    private ExpandableListAdapterForHomeView listAdapterForSthlmEvents;

    private List<String> headersForBookedEvents;
    private List<String> headersForFavPlaceEvents;
    private List<String> headersForSthlmEvents;

    private HashMap<String, List<Event>> bookedEvents;
    private HashMap<String, List<Event>> favPlaceEvents;
    private HashMap<String, List<Event>> sthlmEvents;

    private View thisView;
    private TabLayout tabLayout;
    static HomeView homeView;
    private Call<ResponseBody> mainCaller;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.text_header);

        getUserFbId();
        homeView = this;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        thisView = inflater.inflate(R.layout.home_view, container, false);

        initiateTabs();
        return thisView;
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(r);
    }

    @Override
    public void onResume() {
        super.onResume();
        r = new MyReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(r,
                new IntentFilter("TAG_REFRESH"));
        refreshForTab(tabLayout.getSelectedTabPosition());
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            HomeView.this.refreshForTab(tabLayout.getSelectedTabPosition());
        }
    }

    public static HomeView getHomeView() {
        return homeView;
    }

    private void initiateTabs() {
        tabLayout = (TabLayout) thisView.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Favoriter"));
        tabLayout.addTab(tabLayout.newTab().setText("Bokade"));
        tabLayout.addTab(tabLayout.newTab().setText("Upptäck"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // avbryter pågående call (spamma-tab-buggen)
                mainCaller.cancel();

                // för errorMessage-funktionaliteten
                thisView.findViewById(R.id.emptyText).setVisibility(View.GONE);
                thisView.findViewById(R.id.lvExp).setVisibility(View.VISIBLE);

                refreshForTab(tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /**
     * @param tab 0 = favsTab, 1 = bookedTab, 2 = sthlmTab
     */
    private void refreshForTab(int tab) {
        switch (tab) {
            case 0:
                refreshForFavPlaceEvents();
                break;
            case 1:
                refreshForBookedEvents();
                break;
            case 2:
                refreshForSthlmEvents();
                break;
        }
    }

    public void refreshForSthlmEvents() {
        sthlmEvents = new HashMap<>();
        headersForSthlmEvents = new ArrayList<>();
        downloadEventsFromSthlm();

        expListViewForSthlmEvents = (ExpandableListView) thisView.findViewById(R.id.lvExp);
        listAdapterForSthlmEvents = new ExpandableListAdapterForHomeView(getContext(), headersForSthlmEvents, sthlmEvents, expListViewForSthlmEvents);
        expListViewForSthlmEvents.setAdapter(listAdapterForSthlmEvents);

        expListViewForSthlmEvents.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(getContext(), EventActivity.class);
                intent.putExtra("ID", sthlmEvents.get(headersForSthlmEvents.get(groupPosition)).get(childPosition).getEventId());
                startActivity(intent);
                return false;
            }
        });
    }

    public void refreshForFavPlaceEvents() {
        favPlaceEvents = new HashMap<>();
        headersForFavPlaceEvents = new ArrayList<>();
        downloadEventsFromFavPlaces();

        expListViewForFavPlaceEvents = (ExpandableListView) thisView.findViewById(R.id.lvExp);
        listAdapterForFavPlaceEvents = new ExpandableListAdapterForHomeView(getContext(), headersForFavPlaceEvents, favPlaceEvents, expListViewForFavPlaceEvents);
        expListViewForFavPlaceEvents.setAdapter(listAdapterForFavPlaceEvents);
        expListViewForFavPlaceEvents.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(getContext(), EventActivity.class);
                intent.putExtra("ID", favPlaceEvents.get(headersForFavPlaceEvents.get(groupPosition)).get(childPosition).getEventId());
                startActivity(intent);
                return false;

            }
        });
    }

    public void refreshForBookedEvents() {
        bookedEvents = new HashMap<>();
        headersForBookedEvents = new ArrayList<>();
        downloadBookedEvents();

        expListViewForBookedEvents = (ExpandableListView) thisView.findViewById(R.id.lvExp);
        listAdapterForBookedEvents = new ExpandableListAdapterForHomeView(getContext(), headersForBookedEvents, bookedEvents, expListViewForBookedEvents);
        expListViewForBookedEvents.setAdapter(listAdapterForBookedEvents);
        expListViewForBookedEvents.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(getContext(), EventActivity.class);
                intent.putExtra("ID", bookedEvents.get(headersForBookedEvents.get(groupPosition)).get(childPosition).getEventId());
                startActivity(intent);
                return false;

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
                                userID = Long.parseLong(obj.get("id").toString());
                            } catch (JSONException e) {
                                Log.v("Profile, RetrievePosts", "JSONException");
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "posts.limit(20)");
            postsRequest.setParameters(parameters);
            postsRequest.executeAndWait();
            Log.v("Start", " ID " + userID);
            return "" + userID;
        }
    }

    public void downloadBookedEvents() {
        //callIsOngoing = true;
        mainCaller = herokuService.selectEventsByUser(userID);
        mainCaller.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray locationsEvents = new JSONArray(response.body().string());
                    if (locationsEvents.length() > 0) {
                        for (int i = 0; i < locationsEvents.length(); i++) {
                            JSONObject obj = locationsEvents.getJSONObject(i);
                            addToActivityList(createEventFromJson(obj), headersForBookedEvents, bookedEvents, listAdapterForBookedEvents, expListViewForBookedEvents);
                        }
                        expandGroups(expListViewForBookedEvents, headersForBookedEvents.size());
                    } else {
                        switchOnEmptyMessage("Du har för tillfället inga\nPlayday-aktiviteter inbokade.");
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v("HomeView, onFailure", "Unable to connect to heroku");
            }
        });
    }

    public void downloadEventsFromFavPlaces() {
        mainCaller = herokuService.getUserLocationsEventsTest(userID);
        mainCaller.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray locationsEvents = new JSONArray(response.body().string());
                    if (locationsEvents.length() > 0) {
                        for (int i = 0; i < locationsEvents.length(); i++) {
                            JSONObject obj = locationsEvents.getJSONObject(i);
                            addToActivityList(createEventFromJson(obj), headersForFavPlaceEvents, favPlaceEvents, listAdapterForFavPlaceEvents, expListViewForFavPlaceEvents);
                        }
                        expandGroups(expListViewForFavPlaceEvents, headersForFavPlaceEvents.size());
                    } else {
                        switchOnEmptyMessage("Det finns inga kommande aktivteter\npå dina favoritplatser just nu.\n\nFavoritmarkera fler platser under \"Sök\"\neller välj \"Upptäck\" för att söka i hela\nStockholm!");
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v("FavPlace, onFailure", "Unable to connect to heroku");
            }
        });
    }

    private void downloadEventsFromSthlm() {
        mainCaller = herokuService.selectAllEvents();
        mainCaller.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray locationsEvents = new JSONArray(response.body().string());
                    if (locationsEvents.length() > 0) {
                        for (int i = 0; i < locationsEvents.length(); i++) {
                            JSONObject obj = locationsEvents.getJSONObject(i);
                            addToActivityList(createEventFromJson(obj), headersForSthlmEvents, sthlmEvents, listAdapterForSthlmEvents, expListViewForSthlmEvents);

                        }
                        expandGroups(expListViewForSthlmEvents, headersForSthlmEvents.size());
                    } else {
                        switchOnEmptyMessage("Oj, det finns inga kommande aktivteter\ni hela Stockholm just nu.\n\nBli den första att skapa ett!");
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
    }

    private void switchOnEmptyMessage(String s) {
        TextView emptyMessage = (TextView) thisView.findViewById(R.id.emptyText);
        thisView.findViewById(R.id.lvExp).setVisibility(View.GONE);
        emptyMessage.setVisibility(View.VISIBLE);
        emptyMessage.setText(s);
    }

    private Event createEventFromJson(JSONObject obj) {
        Event e = null;
        try {
            e = new Event(Integer.parseInt(obj.get("event_id").toString()), obj.get("name").toString(), obj.get("date").toString(), obj.get("start_time").toString(), obj.get("end_time").toString(), obj.get("type_name").toString(), obj.get("description").toString(), Integer.parseInt(obj.get("noOfAttendees").toString()), obj.getString("children"));
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return e;
    }

    private void addToActivityList(Event e, List<String> headers,
                                  HashMap<String, List<Event>> events,
                                  ExpandableListAdapterForHomeView listadapter,
                                  ExpandableListView listview) {

        String header = e.getNeatDate();
        List<Event> dayEvents = events.get(header);

        if (dayEvents != null) {
            if (!headers.contains(header)) {
                headers.add(header);
            }
            dayEvents.add(e);
        } else {
            headers.add(header);
            dayEvents = new ArrayList<>();
            dayEvents.add(e);
            events.put(header, dayEvents);
        }

        //Collections.sort(headers);
        //sortAfterStartTime(dayEvents);

        for(int i = 0; i < headers.size(); i++){
            listview.expandGroup(i);
        }
        listadapter.notifyDataSetChanged();
    }

    private void expandGroups(ExpandableListView listView, int headersAmt) {
        for (int i = 0; i < headersAmt; i++) {
            listView.expandGroup(i);
        }
    }

    private void sortAfterStartTime(List<Event> events) {
        Collections.sort(events, new Comparator<Event>() {
            public int compare(Event e1, Event e2) {
                return e1.getStartTime().compareToIgnoreCase(e2.getStartTime());
            }
        });

    }

    private void getUserFbId() {
        try {
            new HomeView.RetrieveUserFBId().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    private void prepareListData() {
        headersForFavPlaceEvents = new ArrayList<>();
        favPlaceEvents = new HashMap<>();
        downloadEventsFromFavPlaces();
    }

}