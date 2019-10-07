package pvt.pvt;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventActivity extends AppCompatActivity {
    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://pvt2017.herokuapp.com/").addConverterFactory(GsonConverterFactory.create()).build();
    final HerokuInterfaceService herokuService = retrofit.create(HerokuInterfaceService.class);

    private ExpandableListAdapterForEvent listAdapter;
    private ExpandableListView expListView;
    private List<String> headers = new ArrayList<>();
    private HashMap<String, List<Person>> expListContent = new HashMap<>();
    private TextView activityName, description, date, time, participantCount;
    private int eventID;
    private Event event;
    private long userID;
    private Person me;
    private String eventCreator;
    private CheckBox isAttendingBox;
    private boolean firstTimeChecked = false;
    private boolean isEnabled = true;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_activity);
        //hämta min fb info:
        try {
            new EventActivity.RetrieveUserFBId().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        headers.add("Deltagare");
        eventID = getIntent().getIntExtra("ID", 0);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar3);
        mActionBarToolbar.setTitle("Aktivitet");
        setSupportActionBar(mActionBarToolbar);

        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        activityName = (TextView) findViewById(R.id.place_name);
        description = (TextView) findViewById(R.id.description);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
        participantCount = (TextView) findViewById(R.id.participant_count);
        isAttendingBox = (CheckBox) findViewById(R.id.checkBox);
        isAttendingBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isEnabled) {
                    if (isChecked) {
                        if (!firstTimeChecked) {
                            selectChildrenDialog();
                        } else {
                            firstTimeChecked = false;
                        }
                    } else {
                        removeMe();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Gå in på din profil för att ta bort evenemanget", Toast.LENGTH_SHORT).show();
                }
            }
        });

        downloadEventInfo();

        me = new Person("", "", 0);

        Call<ResponseBody> userInfo = herokuService.getUser(userID);
        userInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray personInfo = new JSONArray(response.body().string());
                    JSONObject personObject = personInfo.getJSONObject(0);
                    me.setFirstName(personObject.get("first_name").toString());
                    me.setLastName(personObject.get("last_name").toString());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v("Profile, onFailure", "Unable to connect to heroku");
            }
        });

        Call<ResponseBody> userChildrenInfo = herokuService.getUserChildren(userID);
        userChildrenInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray personInfo = new JSONArray(response.body().string());
                    for (int i = 0; i < personInfo.length(); i++) {
                        JSONObject obj = personInfo.getJSONObject(i);
                        me.addChild(new Child(Integer.parseInt(obj.get("child_id").toString()), Integer.parseInt(obj.get("age").toString())));
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

        Call<ResponseBody> getUserEvents = herokuService.selectEventByUser(userID);
        getUserEvents.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String tmp = response.body().string();
                    JSONArray usersEvents = new JSONArray(tmp);
                    for (int i = 0; i < usersEvents.length(); i++) {
                        JSONObject obj = usersEvents.getJSONObject(i);
                        if (Integer.parseInt(obj.get("event_id").toString()) == eventID) {
                            firstTimeChecked = true;
                            isAttendingBox.setChecked(true);
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });

        Button chatButton = (Button) findViewById(R.id.chatButton);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, ChatMain.class);
                intent.putExtra("ID", eventID);
                startActivity(intent);
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
            return "" + userID;
        }
    }

    private void addMe(List<Child> attendingChildren) {
        try {
            JSONObject eventObject = new JSONObject();
            eventObject.put("event_id", eventID);
            eventObject.put("user_id", userID);
            eventObject.put("attending_children_ids", childIdsAsString(attendingChildren));

            Call<ResponseBody> makeCall = herokuService.addEventAttendee(eventObject);
            makeCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        downloadEventInfo();
                    } else {
                        Toast.makeText(getApplicationContext(), "serverfel: " + response.message(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "anslutningsfel: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String childIdsAsString(List<Child> children) {
        String ret = ",";
        for (Child c : children) {
            ret += c.getID() + ",";
        }
        return ret;
    }

    private void removeMe() {
        try {
            JSONObject eventObject = new JSONObject();
            eventObject.put("event_id", eventID);
            eventObject.put("user_id", userID);
            Call<ResponseBody> makeCall = herokuService.deleteEventAttendee(eventObject);
            makeCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    downloadEventInfo();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void downloadEventAttendees() {
        Call<ResponseBody> getAttendees = herokuService.selectEventAttendees(eventID);
        getAttendees.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray eventAttendees = new JSONArray(response.body().string());
                    Log.v("EventActivity", "attendees JSON: " + eventAttendees.toString());

                    for (int i = 0; i < eventAttendees.length(); i++) {
                        JSONObject obj = eventAttendees.getJSONObject(i);
                        Person p = new Person(obj.get("first_name").toString(), obj.get("last_name").toString(), 0, Long.parseLong(obj.get("user_id").toString()));
                        ArrayList<Child> children = new ArrayList<>();

                        String childAges = obj.get("children").toString();
                        List<String> split = Arrays.asList(childAges.split(","));
                        for (String s : split) {
                            if (s != null && !s.isEmpty()) {
                                children.add(new Child(0, Integer.parseInt(s)));
                            }
                        }
                        addAttendeeToList(p, children);
                    }
                    updateView();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private void downloadEventInfo() {
        Call<ResponseBody> eventInfo = herokuService.selectEvent(eventID);
        eventInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray eventData = new JSONArray(response.body().string());
                    JSONObject o = eventData.getJSONObject(0);
                    event = new Event(o.get("name").toString(), o.get("date").toString(), o.get("start_time").toString(), o.get("end_time").toString(), o.get("type_name").toString(), o.get("description").toString());
                    eventCreator = o.get("user_id").toString();
                    if (Long.parseLong(eventCreator) == userID) {
                        isAttendingBox.setText("Gå in på din profil för att ta bort ett evenemang");
                        isAttendingBox.setEnabled(false);
                        isEnabled = false;
                    }
                    downloadEventAttendees();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private void updateView() {
        expListContent.put(headers.get(0), event.getAttendees());
        listAdapter = new ExpandableListAdapterForEvent(this, headers, expListContent, event.getAttendeesChildren());
        expListView.setAdapter(listAdapter);
        expListView.setClickable(true);
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Person p = (Person) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
                long fbId;
                for (Person person : event.getAttendees()) {
                    if (person.getName().equals(p.getName())) {
                        fbId = person.getFacebookID();
                        goToOtherProfile(fbId);
                    }
                }
                return false;
            }
        });
        for (int i = 0; i < headers.size(); i++) {
            expListView.expandGroup(i);
        }
        activityName.setText(event.getName());
        description.setText(event.getDescription());
        date.setText(event.getDetailedDate());
        time.setText(event.getStartTime() + " - " + event.getEndTime());
        participantCount.setText(event.getAttendees().size() + " deltagare");
        listAdapter.notifyDataSetChanged();
    }


    public void selectChildrenDialog() {
        List<Child> children = me.getChildren();
        final String[] items = new String[children.size()];
        final Child[] childArray = new Child[children.size()];

        for (int i = 0; i < children.size(); i++) {
            items[i] = children.get(i).toString();
            childArray[i] = children.get(i);
        }

        final ArrayList<Child> selectedChildren = new ArrayList<>();

        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Vilka barn kommer?")
                .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            selectedChildren.add(childArray[indexSelected]);
                        } else {
                            selectedChildren.remove(childArray[indexSelected]);
                        }
                    }

                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (selectedChildren.size() == 0) {
                            selectChildrenDialog();
                            Toast.makeText(getApplicationContext(), "Du kan inte anmäla dig om du inte tar med dig ett barn", Toast.LENGTH_SHORT).show();
                        } else {
                            addMe(selectedChildren);
                            updateView();
                        }

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        isAttendingBox.setChecked(false);
                    }

                }).create();
        dialog.show();
    }

    private void goToOtherProfile(long id) {
        if(id != userID) {
            Intent intent = new Intent(this, OtherProfile.class);
            intent.putExtra("ID", id);
            startActivity(intent);
        }
    }


    private void addAttendeeToList(Person person, List<Child> children) {
        event.addAttendee(person, children);
    }


//    @Override
//    public void onBackPressed() {
//
////        HomeView hv = HomeView.getHomeView();
////        if(hv.bookedTabSelected){
////            hv.refreshForBookedEvents();
////        }
////        else if(hv.sthlmTabSelected){
////            hv.refreshForSthlmEvents();
////        }
////        else if(hv.favsTabSelected){
////            hv.refreshForFavPlaceEvents();
////        }
//        super.onBackPressed();
//    }

}
