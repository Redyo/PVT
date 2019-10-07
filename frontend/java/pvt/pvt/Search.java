package pvt.pvt;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Search extends Fragment {
    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://pvt2017.herokuapp.com/").build();
    final HerokuInterfaceService herokuService = retrofit.create(HerokuInterfaceService.class);

    private ArrayAdapter<String> adapter;
    private ExpandableListView listView;
    private EditText editText;
    private JSONArray places;
    private ArrayList<String> itemList;
    static Search search;
    private ListAdapterForSearchSuggestions listAdapterForSearchSuggestions;
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        places = new JSONArray();
        itemList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), R.layout.search_list_item, R.id.txtitem, itemList);
        search = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_main, container, false);
        editText = (EditText) view.findViewById(R.id.txtsearch);
        editText.setHint("Sök");
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    places = new JSONArray();
                    calculationByDistance();
                } else {
                    searchItem(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editText.setCursorVisible(false);


        calculationByDistance();

        return view;
    }


    public static Search getInstance(){
        return search;
    }

    public void searchItem(String textToSearch) {
        try {
            Call<ResponseBody> locations = herokuService.searchLocation(textToSearch);
            locations.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        places = new JSONArray(response.body().string());
                        updateList();
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

    public void updateList() {
        itemList.clear();

        ArrayList<Place> placeArray = new ArrayList<>();
        for (int i = 0; i < places.length(); i++) {
            try {
                JSONObject obj = places.getJSONObject(i);
                String name = obj.get("name").toString();
                Integer id = Integer.parseInt(obj.get("location_id").toString());
                String type = obj.get("location_type").toString();
                String desc = obj.get("description").toString();
                placeArray.add(new Place(name, id, type, desc));


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        updateView("Sökresultat", placeArray);
    }

    public void calculationByDistance() {
        itemList.clear();
        try {
            double lat;
            double lng;
            LocationManager lm = null;
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            }
            if(lm!=null) {
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                lat = location.getLongitude();
                lng = location.getLatitude();
            }else {
                lat = 59.408067;
                lng = 17.946959;
            }
            Call<ResponseBody> getPlaces = herokuService.getLocationsNearYou(lat, lng);

            getPlaces.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        ArrayList<Place> placeArray = new ArrayList<>();
                        JSONArray nearbyPlaces = new JSONArray(response.body().string());
                        for (int i = 0; i < nearbyPlaces.length(); i++) {
                            JSONObject obj = nearbyPlaces.getJSONObject(i);
                            String name = obj.get("name_short").toString();
                            String type = obj.get("location_type").toString();
                            String desc = obj.get("description").toString();
                            int id = Integer.parseInt(obj.get("location_id").toString());
                            placeArray.add(new Place(name + "\nAvstånd: " + obj.get("distance").toString() + " km", id, type, desc));

                        }
                        updateView("Förslag på platser nära dig", placeArray);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.v("Search, onFailure", "Unable to connect to heroku");
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void hideKeyBoard(){
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }



    public void updateView(String header, final ArrayList<Place> placeArray) {
        ArrayList<String> headers = new ArrayList<>();
        headers.add(header);
        listAdapterForSearchSuggestions = new ListAdapterForSearchSuggestions(getContext(), headers, placeArray);
        listView = (ExpandableListView) view.findViewById(R.id.lvExp);
        listView.setAdapter(listAdapterForSearchSuggestions);
        listAdapterForSearchSuggestions.notifyDataSetChanged();
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(getContext(), PlaceActivity.class);
                intent.putExtra("ID", placeArray.get(childPosition).getId());

                // SPARA NEDANSTÅENDE TVÅ RADER, DE BEHÖVS I PLACEACTIVITY
                intent.putExtra("TYPE", placeArray.get(childPosition).getType());
                intent.putExtra("DESCRIPTION", placeArray.get(childPosition).getDescription());

                startActivity(intent);
                return false;
            }
        });
    }


}