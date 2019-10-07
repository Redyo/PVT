package pvt.pvt;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

/**
 * Created by alexanderrekestad on 2017-05-29.
 */

public class ChatMessage {
    private String dateTime;
    private String name;
    private String message;

    public ChatMessage(String name, String message, String dateTime) {
        this.dateTime = dateTime;
        this.name = name;
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public boolean isTheSameAs(ChatMessage obj) {
        if(obj.getName().equals(name) && obj.getDateTime().equals(dateTime)) {
            return true;
        }

        return false;
    }

    public SpannableStringBuilder getChatMessage() {

        String name = this.name + ":\n";
        String message = this.message;
        String dateTime = "\n" + this.dateTime;

        String finalString = name + message + dateTime;

        SpannableStringBuilder builder = new SpannableStringBuilder(finalString);

        int start = 0;
        int end = name.length();

        builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);

        start = end + message.length();
        end = start + dateTime.length();

        builder.setSpan(new RelativeSizeSpan(0.8f), start, end, 0);
        builder.setSpan(new ForegroundColorSpan(Color.GRAY), start, end, 0);

        return builder;
    }
}
