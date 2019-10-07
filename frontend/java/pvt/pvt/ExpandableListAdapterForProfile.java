package pvt.pvt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ExpandableListAdapterForProfile extends BaseExpandableListAdapter {
    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://pvt2017.herokuapp.com/").addConverterFactory(GsonConverterFactory.create()).build();
    final HerokuInterfaceService herokuService = retrofit.create(HerokuInterfaceService.class);

    public final static int GROUP_ACTIVITIES = 0, GROUP_CHILDREN = 1, GROUP_FAV_PLACE = 2;
    private Context context;
    private List<String> headers;
    private Person me;
    private List<ImageButton> buttons = new ArrayList<>();
    private boolean btnsVisible = false;

    public ExpandableListAdapterForProfile(Context context, List<String> headers, Person me) {
        this.context = context;
        this.headers = headers;
        this.me = me;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        switch (groupPosition) {
            case GROUP_ACTIVITIES:
                if (me.getMyEvents() != null) {
                    return me.getMyEvents().get(childPosititon);
                }
                break;
            case GROUP_FAV_PLACE:
                if (me.getFavPlaces() != null) {
                    return me.getFavPlaces().get(childPosititon);
                }
                break;

            case GROUP_CHILDREN:
                if (me.getChildren() != null) {
                    return me.getChildren().get(childPosititon);
                }
                break;
        }

        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item_profile_edit, null);
            buttons.add((ImageButton) convertView.findViewById(R.id.delbtn));
        }

        final TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        Child c;
        Place p;
        Event e;
        ImageView icon = (ImageView) convertView.findViewById(R.id.type);


        icon.setFocusable(false);
        if (getChild(groupPosition, childPosition) instanceof Child) {
            c = (Child) getChild(groupPosition, childPosition);
            txtListChild.setText(c.toString());
            icon.setImageResource(R.drawable.p);

        } else if (getChild(groupPosition, childPosition) instanceof Event) {
            e = (Event) getChild(groupPosition, childPosition);
            icon.setImageResource(R.drawable.sand1);
            txtListChild.setText(e.toString());

            if (e.getType().equals("Park")) {
                icon.setImageResource(R.drawable.sand1);
            } else if (e.getType().equals("Förskola")) {
                icon.setImageResource(R.drawable.skol1);
            } else if (e.getType().equals("Plaskdamm")) {
                icon.setImageResource(R.drawable.sim1);
            }

        } else {
            p = (Place) getChild(groupPosition, childPosition);
            if (p.getType() == 1) {
                icon.setImageResource(R.drawable.sand1);
            } else if (p.getType()== 3) {
                icon.setImageResource(R.drawable.skol1);
            } else if (p.getType() == 2) {
                icon.setImageResource(R.drawable.sim1);
            }
            txtListChild.setText(p.toString());
        }


        ImageButton deletebtn = (ImageButton) convertView.findViewById(R.id.delbtn);
        deletebtn.setFocusable(false);
        deletebtn.setEnabled(btnsVisible);
        deletebtn.setVisibility(btnsVisible ? View.VISIBLE : View.GONE);

        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getChild(groupPosition, childPosition) instanceof Child) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Vill du ta bort detta barn?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Ja",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        JSONObject obj = new JSONObject();
                                        Child c = (Child) (getChild(groupPosition, childPosition));
                                        obj.put("child_id", c.getID());
                                        obj.put("parent_id", me.getFacebookID());
                                        Call<ResponseBody> makeCall = herokuService.deleteChild(obj);
                                        makeCall.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                if (response.isSuccessful()) {
                                                    me.getChildren().remove(childPosition);
                                                    notifyDataSetChanged();
                                                } else {
                                                    Toast.makeText(context, "serverfel: "+response.message(), Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                Toast.makeText(context, "anslutningsfel: "+t.getMessage(), Toast.LENGTH_LONG).show();
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

                } else if (getChild(groupPosition, childPosition) instanceof Place) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Vill du ta bort denna plats?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Ja",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        JSONObject obj = new JSONObject();
                                        Place p = (Place) (getChild(groupPosition, childPosition));
                                        obj.put("location_id", p.getId());
                                        obj.put("user_id", me.getFacebookID());
                                        Call<ResponseBody> makeCall = herokuService.deleteFavoriteLocation(obj);
                                        makeCall.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            }
                                        });
                                    } catch (JSONException e) {
                                        Log.v("ExpListAd, JSONEx", e.toString());
                                    }
                                    List<Place> place = me.getFavPlaces();
                                    place.remove(childPosition);
                                    notifyDataSetChanged();
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
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Vill du ta bort detta evenemang?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Ja",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        JSONObject obj = new JSONObject();
                                        Event e = (Event) (getChild(groupPosition, childPosition));
                                        obj.put("event_id", e.getEventId());
                                        obj.put("user_id", me.getFacebookID());
                                        Call<ResponseBody> makeCall = herokuService.deleteEvent(obj);
                                        makeCall.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            }
                                        });
                                    } catch (JSONException e) {
                                        Log.v("ExpListAd, JSONEx", e.toString());
                                    }
                                    List<Place> events = me.getMyEvents();
                                    events.remove(childPosition);
                                    notifyDataSetChanged();
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
                }
            }
        });
        return convertView;
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        switch (groupPosition) {
            case GROUP_ACTIVITIES:
                return me.getMyEvents().size();
            case GROUP_CHILDREN:
                return me.getChildren().size();
            case GROUP_FAV_PLACE:
                return me.getFavPlaces().size();
            default:
                return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.headers.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.headers.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        final String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group_profile_edit, null);
            buttons.add((ImageButton) convertView.findViewById(R.id.addbtn));
        }

        final TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListItem);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
        ImageButton addButton = (ImageButton) convertView.findViewById(R.id.addbtn);

        addButton.setFocusable(false);
        addButton.setEnabled(btnsVisible);
        addButton.setVisibility(btnsVisible ? View.VISIBLE : View.GONE);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupPosition == GROUP_CHILDREN) {
                    alertForChild();

                } else if(groupPosition == GROUP_ACTIVITIES) {
                    MainActivity m = MainActivity.getInstance();
                    m.goToSearch();

                } else if (groupPosition == GROUP_FAV_PLACE) {
                    MainActivity m = MainActivity.getInstance();
                    m.goToSearch();
                }

            }

        });

        return convertView;
    }

    private void addChild(final int age) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("age", age);
            obj.put("parent_id", me.getFacebookID());
            Call<ResponseBody> makeCall = herokuService.createChild(obj);
            makeCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        me.addChild(new Child(0, age));
                        Toast.makeText(context, "Barn tillagt.", Toast.LENGTH_SHORT).show();
                        ExpandableListAdapterForProfile.this.notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, "serverfel: " + response.message(), Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(context, "anslutningfel: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e) {
            Log.v("ExpListAdp, Jsonex", e.toString());
        }

    }

    private void alertForChild() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        alertBuilder.setCancelable(false);

        alertBuilder.setTitle("Ålder för nytt barn");
        final NumberPicker age = new NumberPicker(context);
        age.setMinValue(0);
        age.setMaxValue(15);
        layout.addView(age);

        alertBuilder.setView(layout);
        alertBuilder.setPositiveButton("Spara", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alertBuilder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        final AlertDialog dialog = alertBuilder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addChild(age.getValue());
                        MainActivity m = MainActivity.getInstance();
                        //m.goToProfile();
                        dialog.dismiss();
                    }
                });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void showBtns(boolean visible) {
        btnsVisible = visible;
        int viewage = visible ? View.VISIBLE : View.GONE;

        for (ImageButton btn : buttons) {
            btn.setEnabled(visible);
            btn.setVisibility(viewage);
        }
    }

    public boolean isBtnsVisible() {
        return btnsVisible;
    }
}
