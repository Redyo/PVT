package pvt.pvt;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Redyo on 2017-05-03.
 */

public class ExpandableListAdapterForHomeView extends BaseExpandableListAdapter {

    private Context context;
    private List<String> headers;
    private HashMap<String, List<Event>> events;
    private ExpandableListView view;
    private DateFormat dateFormat;
    private Date date;
    public ExpandableListAdapterForHomeView(Context context, List<String> headers,
                                            HashMap<String, List<Event>> events, ExpandableListView view) {
        this.context = context;
        this.headers = headers;
        this.events = events;
        this.view = view;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {

        return this.events.get(this.headers.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        Event e;
        //boolean a = false;
        //final int grpPos = groupPosition;
        //final int childPos = childPosition;
        // String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item_homeview, null);
        }

        ImageButton icon = (ImageButton) convertView.findViewById(R.id.type);


        //view.setDividerHeight(20);
        icon.setFocusable(false);
        e = (Event) getChild(groupPosition, childPosition);
        icon.setImageResource(R.drawable.sand1);

        if(e.getType().equals("Park")){
            icon.setImageResource(R.drawable.sand1);
        }
        else if(e.getType().equals("Öppen förskola")){
            icon.setImageResource(R.drawable.skol1);
        }

        else if(e.getType().equals("Plaskdamm")){
            icon.setImageResource(R.drawable.sim1);
        }

        final TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);

        txtListChild.setText(constructEventRowDisplay(e));
        return convertView;
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        return this.events.get(this.headers.get(groupPosition))
                .size();
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

    public SpannableStringBuilder constructEventRowDisplay(Event e) {

        String description = e.getDescription();
        String placeName = "\n" + e.getName();
        String time = "\nTid: " + e.getTimeInterval();
        String attendeesLabel = "\nFöräldrar: ";
        String attendees = e.getNoOfAttendees();
        String children = ", Ålder på barn: " + e.getChildrenAge();
        String finalString = description + placeName + time + attendeesLabel + attendees + children;

        SpannableStringBuilder builder = new SpannableStringBuilder(finalString);

        int start = 0;
        int end = description.length();

        builder.setSpan(new RelativeSizeSpan(1.3f), start, end, 0);
        builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);

        start = end;
        end = start + placeName.length();

        builder.setSpan(new RelativeSizeSpan(1.1f), start, end, 0);


        return builder;
    }

}

