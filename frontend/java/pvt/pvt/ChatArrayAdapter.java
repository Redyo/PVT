package pvt.pvt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {
    private TextView chatText;
    private RelativeLayout layout;
    private List<ChatMessage> messageList = new ArrayList<>();

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public void add(ChatMessage message) {
        messageList.add(message);
    }

    public int getCount() {
        return this.messageList.size();
    }

    public ChatMessage getItem(int index) {
        return messageList.get(index);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.chat_item, parent, false);
        }

        chatText = (TextView) v.findViewById(R.id.SingleMessage);
        chatText.setText(getItem(position).getChatMessage());
        layout = (RelativeLayout) v.findViewById(R.id.MessageWrapper);
        return v;
    }
}

