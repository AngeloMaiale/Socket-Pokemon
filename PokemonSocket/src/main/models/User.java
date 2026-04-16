package models;

public class User {
    private int id;
    private String username;
    private int eloRating;

    public User(int id, String username, int eloRating) {
        this.id = id;
        this.username = username;
        this.eloRating = eloRating;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public int getEloRating() { return eloRating; }

    public void setEloRating(int eloRating) { this.eloRating = eloRating; }
}