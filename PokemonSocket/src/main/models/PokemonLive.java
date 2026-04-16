package models;

public class PokemonLive {
    private int id;
    private int pokedexId;
    private String nickname;
    private int level;
    private int currentHp;
    private int maxHp;
    private int attack;
    private int defense;
    private int speed;
    private Move[] moves;
    private String status;

    public PokemonLive(int id, int pokedexId, String nickname, int level,
                       int currentHp, int maxHp, int attack, int defense,
                       int speed, Move[] moves, String status) {
        this.id = id;
        this.pokedexId = pokedexId;
        this.nickname = nickname != null ? nickname : "";
        this.level = level;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.moves = moves != null ? moves : new Move[4];
        this.status = status != null ? status : "NONE";
    }

    public static int calculateHp(int baseHp, int iv, int ev, int level) {
        return ((2 * baseHp + iv + (ev / 4)) * level) / 100 + level + 10;
    }

    public static int calculateStat(int baseStat, int iv, int ev, int level) {
        return ((2 * baseStat + iv + (ev / 4)) * level) / 100 + 5;
    }

    public int getId() {
        return id;
    }

    public int getPokedexId() {
        return pokedexId;
    }

    public String getNickname() {
        return nickname;
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpeed() {
        return speed;
    }

    public Move[] getMoves() {
        return moves;
    }

    public String getStatus() {
        return status;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMoves(Move[] moves) {
        this.moves = moves;
    }
}
