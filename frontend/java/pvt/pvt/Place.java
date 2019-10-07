package pvt.pvt;

import java.util.ArrayList;

/**
 * Created by Alex on 4/25/2017.
 */

public class Place {

    private String name;
    private int x, y;
    private int id;
    private int type;
    private String description;

    public Place(String name, int x, int y){
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public Place(String name, int id){
        this.name = name;
        this.id = id;
    }

    public Place(String name, int id, String type){
        this(name,id);

        switch(type){
            case "1":
                this.type = 1;
                break;
            case "2":
                this.type = 2;
                break;
            case "3":
                this.type = 3;
                break;
        }
    }

    public Place(String name, int id, String type, String description){
        this(name,id,type);
        this.description = description;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }


    public int getId(){return id;}

    private ArrayList<Event> events = new ArrayList<>();
    public Place(String name){
        this.name = name;
    }

    public void addEvent(Event e){
        events.add(e);
    }
    public void removeEvent(Event e){
        events.remove(e);
    }

    public ArrayList<Event> getEvents(){
        return events;
    }

    public String toString(){
        return name;
    }

    public String getName(){
        return name;
    }

    public void setType(int type){ this.type=type;}

    public int getType(){return type;}

    public String getDescription() {
        return description;
    }

}
