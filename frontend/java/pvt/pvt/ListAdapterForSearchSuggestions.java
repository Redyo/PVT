package pvt.pvt;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterForSearchSuggestions extends BaseExpandableListAdapter {

    //1 park, 2 plaskdamm, 3 öppen förskola

    private Context context;
    private ArrayList<String> headers;
    private ArrayList<Place> rowItems;

    public ListAdapterForSearchSuggestions(Context context, ArrayList<String> headers, ArrayList<Place> rowItems) {
        this.context = context;
        this.headers = headers;
        this.rowItems = rowItems;
        headers = new ArrayList<>();
        headers.add("Förslag på platser nära dig");
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item_search, null);
        }
        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        ImageView icon = (ImageView) convertView.findViewById(R.id.type);
        icon.setFocusable(false);
        Place p = (Place) getChild(groupPosition, childPosition);
        txtListChild.setText(p.getName());
        if (p.getType() == 1) {
            icon.setImageResource(R.drawable.sand1);
        } else if (p.getType() == 3) {
            icon.setImageResource(R.drawable.skol1);
        } else if (p.getType() == 2) {
            icon.setImageResource(R.drawable.sim1);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getGroupCount() {
        return this.headers.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return rowItems.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.headers.get(groupPosition);
    }

    public Object getChild(int groupPosition, int childPosititon) {
        return rowItems.get(childPosititon);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group_profile, null);
        }
        final TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListItem);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
        ExpandableListView mExpandableListView = (ExpandableListView) parent;
        mExpandableListView.expandGroup(groupPosition);
        return convertView;
    }


}
