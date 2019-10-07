package pvt.pvt;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateEventActivity extends AppCompatActivity {

    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://pvt2017.herokuapp.com/").addConverterFactory(GsonConverterFactory.create()).build();
    final HerokuInterfaceService herokuService = retrofit.create(HerokuInterfaceService.class);
    private String[] items;
    private EditText desc;
    private TextView tvt;
    private Person me;
    private String date;
    private String startTime;
    private int startTimeInt;
    private int startTimeIntMin;
    private String endTime;
    private String description;
    private int parkId;
    private Long fbId;
    private Long userID;
    private ArrayList<Child> selectedChildren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolevent);
        toolbar.setTitle("Skapa ny aktivitet");
        setSupportActionBar(toolbar);
        me = new Person("", "", 0);
        parkId = getIntent().getIntExtra("ID", 0);
        TextView locationText = (TextView) findViewById(R.id.loctxt);
        locationText.setText(getIntent().getStringExtra("parkName"));

        try {
            new CreateEventActivity.RetrieveUserFBId().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Call<ResponseBody> userInfo = herokuService.getUser(userID);
        userInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONArray personInfo = new JSONArray(response.body().string());
                    JSONObject personObject = personInfo.getJSONObject(0);
                    String firstName = personObject.get("first_name").toString();
                    String lastName = personObject.get("last_name").toString();
                    me.setFirstName(firstName);
                    me.setLastName(lastName);
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
        getHighestEventId();
    }


    public void dateOnClick(final View v) {
        tvt = (TextView) findViewById(R.id.tvd);

        //Get Current date
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener datepick = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year,
                                  int month, int dayOfMonth) {
                tvt.setText(String.format("%04d/%02d/%02d", year, month + 1, dayOfMonth));
                date = tvt.getText().toString();
            }
        };
        DatePickerDialog datePicker2 = new DatePickerDialog(CreateEventActivity.this, datepick, year, month, day) {};
        datePicker2.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePicker2.show();
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

    public void timeOnClick(final View v) {
        tvt = (TextView) findViewById(R.id.tv);

        // Get Current time
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(CreateEventActivity.this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        int mMinHour = 6;
                        int mMinMinute = 0;
                        int mMaxHour = 21;
                        int mMaxMinute = 0;
                        if (((hourOfDay < mMinHour ) || (hourOfDay == mMinHour && minute < mMinMinute))
                                || ((hourOfDay > mMaxHour) || (hourOfDay == mMaxHour && minute > mMaxMinute))) {
                            createToast("Gilltiga tider för en aktivitet är mellan 06:00 och 21:00");
                        }
                        else {
                            startTimeInt=hourOfDay;
                            startTimeIntMin=minute;
                            tvt.setText(String.format("%02d:%02d", hourOfDay, minute));
                            startTime = tvt.getText().toString();
                        }
                    }
                }, hour, minute, true);
        timePickerDialog.show();
    }

    public void endTimeOnClick(final View v) {
        tvt = (TextView) findViewById(R.id.tv2);

        // Get Current time
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int mMinHour = 6;
                        int mMinMinute = 0;
                        int mMaxHour = 21;
                        int mMaxMinute = 0;
                        if (((hourOfDay < mMinHour ) || (hourOfDay == mMinHour && minute < mMinMinute))
                                || ((hourOfDay > mMaxHour) || (hourOfDay == mMaxHour && minute > mMaxMinute))) {
                            createToast("Gilltiga tider för en aktivitet är mellan 06:00 och 21:00");
                        } else if (startTimeInt>hourOfDay ){
                            createToast("Aktivitetens sluttid måste sättas efter vald starttid");
                        } else if (startTimeInt == hourOfDay){
                            if (startTimeIntMin >= minute){
                                createToast("Aktivitetens sluttid måste sättas efter vald starttid");
                            }
                            else {
                                tvt.setText(String.format("%02d:%02d", hourOfDay, minute));
                                endTime = tvt.getText().toString();
                            }
                        }
                        else {
                            tvt.setText(String.format("%02d:%02d", hourOfDay, minute));
                            endTime = tvt.getText().toString();
                        }
                    }
                }, hour, minute, true);

        if (startTime == null){
            createToast("Välj starttid först");
        }else{
            timePickerDialog.show();
        }
    }

    public void selectChildrenDialog() {
        List<Child> children = me.getChildren();
        items = new String[children.size()];
        final Child[] childArray = new Child[children.size()];

        for (int i = 0; i < children.size(); i++) {
            items[i] = children.get(i).toString();
            childArray[i] = children.get(i);
        }


       selectedChildren = new ArrayList<>();
        desc = (EditText) findViewById(R.id.txtinfo);
        description = desc.getText().toString();

        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Vilka barn kommer?")
                .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            selectedChildren.add(childArray[indexSelected]);
                        } else if (selectedChildren.contains(childArray[indexSelected])) {
                            selectedChildren.remove(childArray[indexSelected]);
                        }
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (selectedChildren.size() == 0) {
                            selectChildrenDialog();
                            createToast("Du kan inte skapa en aktivitet om du inte tar med dig ett barn");
                        } else if (date == null || startTime == null || endTime == null || description.equals(desc.getHint()) || description.equals("")) {
                            createToast("Du har inte fyllt i alla fält");
                        } else {
                            create();

                            //addAttendee(selectedChildren);

                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.TOP| Gravity.LEFT, 0, 0);
                            Toast.makeText(CreateEventActivity.this, "Aktivitet skapad", Toast.LENGTH_SHORT).show();


                            createToast("Aktivitet skapad");

                            finish();
                            PlaceActivity.getInstance().restart();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialog.show();
    }

    private void getHighestEventId() {
        Call<ResponseBody> makeCall = herokuService.getMaxEventId();
        makeCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String tmp = response.body().string();
                    String value = tmp.replaceAll("[^0-9]","");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v("HomeView, onFailure", "Unable to connect to heroku");
            }
        });
    }

    private String childIdsAsString(List<Child> children) {
        String ret = "";
        for (Child c : children) {
            ret += c.getID() + ",";
        }
        return ret;
    }

    private void create() {
            new GraphRequest(AccessToken.getCurrentAccessToken(), "/me", null, HttpMethod.GET, new GraphRequest.Callback() {
                public void onCompleted(GraphResponse response) {
                    try {
                        JSONObject userObject = response.getJSONObject();
                        fbId = Long.parseLong(userObject.getString("id"));
                        JSONObject eventObject = new JSONObject();
                        eventObject.put("location_id", parkId);
                        eventObject.put("user_id", fbId.toString());
                        eventObject.put("date", date);
                        eventObject.put("start_time", startTime);
                        eventObject.put("end_time", endTime);
                        eventObject.put("description", description);
                        String s = "," + childIdsAsString(selectedChildren);
                        eventObject.put("attending_children_ids", s);
                        Call<ResponseBody> makeCall = herokuService.createEvent(eventObject);
                        makeCall.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.v("HomeView, onFailure", "Unable to connect to heroku");
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).executeAsync();
    }

    public void buttonOnClickCreateEvent(final View v) {
            selectChildrenDialog();
        }

    private void createToast(String toastText){
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.TOP | Gravity.LEFT, 0, 0);
        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
    }
}