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
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ExpandableListAdapterForPlaceActivity extends BaseExpandableListAdapter {

    private Context context;
    private List<String> headers;
    private HashMap<String, List<Event>> events;

    public ExpandableListAdapterForPlaceActivity(Context context, List<String> headers,
                                                 HashMap<String, List<Event>> events) {
        this.context = context;
        this.headers = headers;
        this.events = events;
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

        Event e = (Event) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item_placeactivity, null);
        }

        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);

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

        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group_profile, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListItem);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(formatDate(headerTitle));

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
        String time = "\n\n" + e.getTimeInterval();
        String attendeesLabel = "\nDeltagare: ";
        String attendees = e.getNoOfAttendees();
        String finalString = description + time + attendeesLabel + attendees;

        SpannableStringBuilder builder = new SpannableStringBuilder(finalString);

        builder.setSpan(new RelativeSizeSpan(1.3f), 0, description.length(), 0);
        builder.setSpan(new StyleSpan(Typeface.BOLD), 0, description.length(), 0);

        return builder;
    }

    public String formatDate(String inDate) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // TODAYS DATE
        String today = sdf.format(d);

        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DATE, 1);
        d = c.getTime();

        // TOMORROWS DATE
        String tomorrow = sdf.format(d);

        if(inDate.equals(today))
            return "Idag";
        else if(inDate.equals(tomorrow))
            return "Imorgon";

        Date date = null;
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outFormat = new SimpleDateFormat("EEEE d MMM", new Locale("sv", "SE"));

        try {
            date = inFormat.parse(inDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String dateString = outFormat.format(date);
        dateString = dateString.substring(0, 1).toUpperCase() + dateString.substring(1);

        return (dateString);
    }
}