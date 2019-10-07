package pvt.pvt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Event {
    private String childrenAge;
    private int eventId;
    private String type;
    private String name;
    private String date;
    private String startTime;
    private String endTime;
    private String description;
    private int noOfAttendees;
    private ArrayList<Person> attendees = new ArrayList<>();
    private HashMap<Person, List<Child>> attendeesChildren = new HashMap<>();

    public Event(String name, String date, String startTime, String endTime, String type, String description) {
        this.name = name;
        this.date = date;
        this.startTime = startTime.substring(0, startTime.length() - 3); // tar bort sekunder
        this.endTime = endTime.substring(0, endTime.length() - 3);
        this.type = type;
        this.description = description;
    }

    // EVENT ID MÅSTE JU SPARAS
    public Event(int eventId, String name, String date, String startTime, String endTime, String type, String description) {
        this(name, date, startTime, endTime, type, description);
        this.eventId = eventId;
    }

    // La till noOfAttendees, pallade inte att fixa den förra konstruktorn
    public Event(int eventId, String name, String date, String startTime, String endTime, String type, String description, int noOfAttendees) {
        this(eventId, name, date, startTime, endTime, type, description);
        this.noOfAttendees = noOfAttendees;
    }

    public Event(int eventId, String name, String date, String startTime, String endTime, String type, String description, int noOfAttendees, String childrenAge) {
        this(eventId, name, date, startTime, endTime, type, description, noOfAttendees);
        this.childrenAge = childrenAge;
    }

    public void addAttendee(Person p, List<Child> children) {
        attendees.add(p);
        attendeesChildren.put(p, children);
    }

    public int getEventId() {
        return eventId;
    }

    public String getDescription() {
        return description;
    }

    public String getNoOfAttendees() {
        return String.valueOf(noOfAttendees);
    }

    public void removeAttendee(Person p) {
        attendeesChildren.remove(p);
        attendees.remove(p);
    }

    public String toString() {
        return name + " " + date + " " + startTime;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getNeatDate() {
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

        if (date.equals(today))
            return "Idag";
        else if (date.equals(tomorrow))
            return "Imorgon";

        Date date = null;
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outFormat = new SimpleDateFormat("EEEE d MMM", new Locale("sv", "SE"));

        try {
            date = inFormat.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String dateString = outFormat.format(date);
        dateString = dateString.substring(0, 1).toUpperCase() + dateString.substring(1);
        return (dateString);
    }

    public String getDetailedDate() {
        Date date = null;
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outFormat = new SimpleDateFormat("EEEE d MMM yyyy", new Locale("sv", "SE"));

        try {
            date = inFormat.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String dateString = outFormat.format(date);
        dateString = dateString.substring(0, 1).toUpperCase() + dateString.substring(1);
        return (dateString);
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getTimeInterval() {
        return startTime + " - " + endTime;
    }

    public String getEventLabel() {
        return "<span style=\"font-size:25px\"><b>" + description + "</b></span><br/>" + startTime + " till " + endTime + "<br>" +
                "Antal deltagare: " + noOfAttendees;

    }

    public String getType() {
        return type;
    }

    public ArrayList<Person> getAttendees() {
        return attendees;
    }

    public HashMap<Person, List<Child>> getAttendeesChildren() {
        return attendeesChildren;
    }

    public String getChildrenAge() {
        return childrenAge;
    }


}
