package engine;

import java.util.HashMap;
import java.util.Map;

public class DamageCalculator {
    private static final String[] TYPES = {
        "Normal", "Fire", "Water", "Electric", "Grass", "Ice", "Fighting", "Poison",
        "Ground", "Flying", "Psychic", "Bug", "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy"
    };

    private static final Map<String, Integer> TYPE_INDEX = new HashMap<>();
    private static final double[][] TYPE_CHART = new double[18][18];

    static {
        for (int i = 0; i < TYPES.length; i++) {
            TYPE_INDEX.put(TYPES[i], i);
        }

        for (int i = 0; i < 18; i++) {
            for (int j = 0; j < 18; j++) {
                TYPE_CHART[i][j] = 1.0;
            }
        }

        
        TYPE_CHART[1][4] = 2.0; 
        TYPE_CHART[1][5] = 2.0; 
        TYPE_CHART[1][11] = 2.0; 
        TYPE_CHART[1][16] = 2.0; 
        TYPE_CHART[2][1] = 2.0; 
        TYPE_CHART[2][8] = 2.0; 
        TYPE_CHART[2][12] = 2.0; 
        TYPE_CHART[3][2] = 2.0; 
        TYPE_CHART[3][9] = 2.0; 
        TYPE_CHART[4][2] = 2.0; 
        TYPE_CHART[4][8] = 2.0; 
        TYPE_CHART[4][12] = 2.0; 
        TYPE_CHART[5][4] = 2.0; 
        TYPE_CHART[5][9] = 2.0; 
        TYPE_CHART[5][14] = 2.0; 
        TYPE_CHART[6][0] = 2.0; 
        TYPE_CHART[6][5] = 2.0; 
        TYPE_CHART[6][12] = 2.0; 
        TYPE_CHART[6][15] = 2.0; 
        TYPE_CHART[6][16] = 2.0; 
        TYPE_CHART[7][4] = 2.0; 
        TYPE_CHART[7][17] = 2.0; 
        TYPE_CHART[8][3] = 2.0; 
        TYPE_CHART[8][7] = 2.0; 
        TYPE_CHART[8][12] = 2.0; 
        TYPE_CHART[8][16] = 2.0; 
        TYPE_CHART[9][4] = 2.0; 
        TYPE_CHART[9][6] = 2.0; 
        TYPE_CHART[9][11] = 2.0; 
        TYPE_CHART[10][6] = 2.0; 
        TYPE_CHART[10][7] = 2.0; 
        TYPE_CHART[11][4] = 2.0; 
        TYPE_CHART[11][10] = 2.0; 
        TYPE_CHART[11][15] = 2.0; 
        TYPE_CHART[12][1] = 2.0; 
        TYPE_CHART[12][5] = 2.0; 
        TYPE_CHART[12][9] = 2.0; 
        TYPE_CHART[12][11] = 2.0; 
        TYPE_CHART[13][10] = 2.0; 
        TYPE_CHART[13][13] = 2.0; 
        TYPE_CHART[14][14] = 2.0; 
        TYPE_CHART[15][10] = 2.0; 
        TYPE_CHART[15][13] = 2.0; 
        TYPE_CHART[16][5] = 2.0; 
        TYPE_CHART[16][12] = 2.0; 
        TYPE_CHART[16][17] = 2.0; 
        TYPE_CHART[17][6] = 2.0; 
        TYPE_CHART[17][14] = 2.0; 
        TYPE_CHART[17][15] = 2.0; 

        
        TYPE_CHART[0][12] = 0.5; 
        TYPE_CHART[0][16] = 0.5; 
        TYPE_CHART[1][1] = 0.5; 
        TYPE_CHART[1][2] = 0.5; 
        TYPE_CHART[1][12] = 0.5; 
        TYPE_CHART[1][14] = 0.5; 
        TYPE_CHART[2][2] = 0.5; 
        TYPE_CHART[2][4] = 0.5; 
        TYPE_CHART[2][14] = 0.5; 
        TYPE_CHART[3][3] = 0.5; 
        TYPE_CHART[3][4] = 0.5; 
        TYPE_CHART[3][14] = 0.5; 
        TYPE_CHART[4][1] = 0.5; 
        TYPE_CHART[4][4] = 0.5; 
        TYPE_CHART[4][7] = 0.5; 
        TYPE_CHART[4][9] = 0.5; 
        TYPE_CHART[4][11] = 0.5; 
        TYPE_CHART[4][14] = 0.5; 
        TYPE_CHART[4][16] = 0.5; 
        TYPE_CHART[5][1] = 0.5; 
        TYPE_CHART[5][2] = 0.5; 
        TYPE_CHART[5][5] = 0.5; 
        TYPE_CHART[5][16] = 0.5; 
        TYPE_CHART[6][9] = 0.5; 
        TYPE_CHART[6][7] = 0.5; 
        TYPE_CHART[6][10] = 0.5; 
        TYPE_CHART[6][11] = 0.5; 
        TYPE_CHART[6][17] = 0.5; 
        TYPE_CHART[7][7] = 0.5; 
        TYPE_CHART[7][8] = 0.5; 
        TYPE_CHART[7][12] = 0.5; 
        TYPE_CHART[7][13] = 0.5; 
        TYPE_CHART[7][16] = 0.5; 
        TYPE_CHART[8][4] = 0.5; 
        TYPE_CHART[8][11] = 0.5; 
        TYPE_CHART[9][3] = 0.5; 
        TYPE_CHART[9][12] = 0.5; 
        TYPE_CHART[9][16] = 0.5; 
        TYPE_CHART[10][10] = 0.5; 
        TYPE_CHART[10][16] = 0.5; 
        TYPE_CHART[11][1] = 0.5; 
        TYPE_CHART[11][6] = 0.5; 
        TYPE_CHART[11][7] = 0.5; 
        TYPE_CHART[11][9] = 0.5; 
        TYPE_CHART[11][13] = 0.5; 
        TYPE_CHART[11][16] = 0.5; 
        TYPE_CHART[11][17] = 0.5; 
        TYPE_CHART[12][6] = 0.5; 
        TYPE_CHART[12][8] = 0.5; 
        TYPE_CHART[12][16] = 0.5; 
        TYPE_CHART[13][15] = 0.5; 
        TYPE_CHART[14][16] = 0.5; 
        TYPE_CHART[15][6] = 0.5; 
        TYPE_CHART[15][15] = 0.5; 
        TYPE_CHART[15][17] = 0.5; 
        TYPE_CHART[16][1] = 0.5; 
        TYPE_CHART[16][2] = 0.5; 
        TYPE_CHART[16][3] = 0.5; 
        TYPE_CHART[16][16] = 0.5; 
        TYPE_CHART[17][1] = 0.5; 
        TYPE_CHART[17][7] = 0.5; 
        TYPE_CHART[17][16] = 0.5; 

        
        TYPE_CHART[0][13] = 0.0; 
        TYPE_CHART[6][13] = 0.0; 
        TYPE_CHART[7][16] = 0.0; 
        TYPE_CHART[8][9] = 0.0; 
        TYPE_CHART[13][0] = 0.0; 
        TYPE_CHART[3][8] = 0.0; 
        TYPE_CHART[14][17] = 0.0; 
        TYPE_CHART[15][17] = 0.0; 
    }

    public static double getTypeMultiplier(String attackType, String defenseType) {
        Integer attackIndex = TYPE_INDEX.get(attackType);
        Integer defenseIndex = TYPE_INDEX.get(defenseType);
        if (attackIndex == null || defenseIndex == null) {
            return 1.0; 
        }
        return TYPE_CHART[attackIndex][defenseIndex];
    }
}
