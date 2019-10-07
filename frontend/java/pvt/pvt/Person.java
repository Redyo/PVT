package pvt.pvt;

import java.util.ArrayList;

public class Person {

    private String firstName;
    private String lastName;
    private int likes;
    private long facebookID;
    private ArrayList<Event> attendingEvents = new ArrayList<>();
    private ArrayList<Event> myEvents;
    private ArrayList<Place> favPlaces;
    private ArrayList<Child> children;

    public Person(String firstName, String lastName, int likes) {
        children = new ArrayList<>();
        favPlaces = new ArrayList<>();
        myEvents = new ArrayList<>();
        this.firstName = firstName;
        this.lastName = lastName;
        this.likes = likes;
    }

    public Person(String firstName, String lastName, int likes, long facebookID) {
        children = new ArrayList<>();
        favPlaces = new ArrayList<>();
        myEvents = new ArrayList<>();
        this.firstName = firstName;
        this.lastName = lastName;
        this.likes = likes;
        this.facebookID = facebookID;
    }

    public long getFacebookID(){
        return facebookID;
    }

    public void setFacebookID(long facebookID){
        this.facebookID = facebookID;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes){this.likes = likes;}

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getChildrenAmount() {
        return children.size();
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public ArrayList<Place> getFavPlaces() {
        return favPlaces;
    }

    public ArrayList<Child> getChildren() {
        return children;
    }

    public ArrayList<Event> getAttendingEvents() {
        return attendingEvents;
    }

    public String getChildrenInfo() {
        String s = "";
        for (int i = 0; i < children.size(); i++) {
            s += children.get(i).getAge();
            if (i != children.size() - 1) {
                s += ", ";
            }
        }
        return s;

    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void addFavPlace(Place p) {
        favPlaces.add(p);
    }

    public void addChild(Child c) {
        children.add(c);
    }

    public void addAttendingEvent(Event e) {
        attendingEvents.add(e);
    }

    public void addMyEvent(Event e){myEvents.add(e);}

    public ArrayList getMyEvents(){return myEvents;}

    public String toString() {
        return firstName + " " + lastName + " " ;
    }
}
