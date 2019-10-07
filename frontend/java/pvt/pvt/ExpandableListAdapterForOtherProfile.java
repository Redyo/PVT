package pvt.pvt;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapterForOtherProfile extends BaseExpandableListAdapter{

    private Context context;
    private List<String> headers;
    private HashMap<String, List<Child>> children;
    private HashMap<String, List<Place>> places;
    private HashMap<String, List<Event>> events;
    private List<ImageButton> buttons = new ArrayList<>();

    public ExpandableListAdapterForOtherProfile(Context context, List<String> headers,
                                           HashMap<String, List<Child>> children, HashMap<String, List<Place>> places, HashMap<String, List<Event>> events) {
        this.context = context;
        this.headers = headers;
        this.children = children;
        this.places = places;
        this.events = events;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        boolean children = true;
        boolean events = true;
        boolean places = true;
        if (this.children.get(this.headers.get(groupPosition))
                == null) {
            children = false;
        }
        if(this.places.get(this.headers.get(groupPosition))
                == null){
            places = false;
        }

        if(this.events.get(this.headers.get(groupPosition))
                == null){
            events = false;
        }

        if(events){
            return this.events.get(this.headers.get(groupPosition))
                    .get(childPosititon);
        }
        if(places){
            return this.places.get(this.headers.get(groupPosition))
                    .get(childPosititon);
        }
        if(children){
            return this.children.get(this.headers.get(groupPosition))
                    .get(childPosititon);
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
            convertView = infalInflater.inflate(R.layout.list_item_other_profile, null);

        }

        final TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
        Child c = null;
        Place p = null;
        Event e = null;
        boolean a = false;
        ImageButton icon = (ImageButton) convertView.findViewById(R.id.type);

        icon.setFocusable(false);
        if (getChild(groupPosition, childPosition) instanceof Child) {
            c = (Child) getChild(groupPosition, childPosition);
            //txtListChild.setText(c.toString());


                txtListChild.setText(c.getAge() + " år");
                icon.setImageResource(R.drawable.p);

        }
        else if(getChild(groupPosition, childPosition) instanceof Event){
            e = (Event) getChild(groupPosition, childPosition);
            icon.setImageResource(R.drawable.sand1);
            txtListChild.setText(e.toString());

            if(e.getType().equals("Park")){
                icon.setImageResource(R.drawable.sand1);
            }
            else if(e.getType().equals("Förskola")){
                icon.setImageResource(R.drawable.skol1);
            }

            else if(e.getType().equals("Plaskdamm")){
                icon.setImageResource(R.drawable.sim1);
            }

        }
        else {
            p = (Place) getChild(groupPosition, childPosition);
            icon.setImageResource(R.drawable.sand1);
            txtListChild.setText(p.toString());
        }

        return convertView;
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        // ||
        boolean children = true;
        boolean events = true;
        boolean places = true;
        if (this.children.get(this.headers.get(groupPosition))
                == null) {
            children = false;
        }
        if(this.places.get(this.headers.get(groupPosition))
                == null){
            places = false;
        }

        if(this.events.get(this.headers.get(groupPosition))
                == null){
            events = false;
        }

        if(events){
            return this.events.get(this.headers.get(groupPosition))
                    .size();
        }
        if(places){
            return this.places.get(this.headers.get(groupPosition))
                    .size();
        }
        if(children){
            return this.children.get(this.headers.get(groupPosition))
                    .size();
        }
        return 0;

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
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        final String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group_profile, null);
            buttons.add((ImageButton) convertView.findViewById(R.id.addbtn));
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


