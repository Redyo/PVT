package pvt.pvt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static pvt.pvt.R.id.imageView;

public class ExpandableListAdapterForEvent extends BaseExpandableListAdapter {

    private Context context;
    private List<String> headers;
    private HashMap<String, List<Person>> persons;
    private HashMap<Person, List<Child>> personsChildren;
    private long otherUserID;

    public ExpandableListAdapterForEvent(Context context, List<String> headers,
                                         HashMap<String, List<Person>> persons, HashMap<Person, List<Child>> personsChildren) {
        this.context = context;
        this.headers = headers;
        this.persons = persons;
        this.personsChildren = personsChildren;


    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.persons.get(this.headers.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item_profile, null);
        }
        Person person = (Person) getChild(groupPosition, childPosition);
        final ImageView icon = (ImageView) convertView.findViewById(R.id.type);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL fbPictureUrl = new URL("https://graph.facebook.com/" + String.valueOf(person.getFacebookID()) + "/picture?type=large");
            InputStream inputStream = (InputStream) fbPictureUrl.getContent();
            Bitmap fbUserImage = (BitmapFactory.decodeStream(inputStream));
            icon.setImageBitmap(fbUserImage);
        } catch (IOException e) {
            Log.v("ExpListForEvent", "IOException");
        }

        final TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
        icon.setFocusable(false);
        ;

        List<Child> values = null;
        txtListChild.setText(person.getName());
        if (personsChildren != null) {
            for (Map.Entry<Person, List<Child>> ee : personsChildren.entrySet()) {
                if (ee.getKey() == person) {
                    values = ee.getValue();

                }
            }

            if (personsChildren.get(person) != null) {
                if (values != null) {
                    String s = " med barn på ";

                    Iterator<Child> iter = values.iterator();
                    if (values.size() > 2) {
                        for (int i = 0; i < values.size() - 2; i++) {
                            Log.v("ExpAdapter", "size: " + values.size());
                            Child curr = iter.next();
                            s += curr.getAge() + ", ";
                        }
                    }
                    s += iter.next().getAge();
                    if (iter.hasNext())
                        s += " och " + iter.next().getAge();
                    s += " år.";

                    txtListChild.setText(person.getName() + s);
                }

            } else {
                txtListChild.setText(person.getName());
            }
        }
        final int grpPos = groupPosition;
        final int childPos = childPosition;
        // String headerTitle = (String) getGroup(groupPosition);
        //txtListChild.setText(me.toString());
        return convertView;
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        // ||
        return this.persons.get(this.headers.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.headers.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return headers.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        final String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group_profile, null);

        }


        final TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListItem);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);


        return convertView;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

