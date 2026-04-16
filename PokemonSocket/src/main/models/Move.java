package models;

public class Move {
    private int id;
    private String name;
    private String type;
    private int power;
    private int accuracy;
    private int pp;
    private int currentPp;
    private String category;

    public Move(int id, String name, String type, int power, int accuracy,
                int pp, int currentPp, String category) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.pp = pp;
        this.currentPp = currentPp;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getPower() {
        return power;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public int getPp() {
        return pp;
    }

    public int getCurrentPp() {
        return currentPp;
    }

    public String getCategory() {
        return category;
    }

    public void setCurrentPp(int currentPp) {
        this.currentPp = currentPp;
    }
}
