package pvt.pvt;

public class Child {

    private String name;
    private int id;
    private int age;

    public Child(int id, int age){
        this.name = "Barn";
        this.id = id;
        this.age = age;
    }

    public String toString(){
        return name + " " +  age + " år";
    }

    public int getID(){
        return id;
    }


    public int getAge(){
        return age;
    }

}
