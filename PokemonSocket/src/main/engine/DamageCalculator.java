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

        // Muy efectivo (2.0)
        TYPE_CHART[1][4] = 2.0; // Fire vs Grass
        TYPE_CHART[1][5] = 2.0; // Fire vs Ice
        TYPE_CHART[1][11] = 2.0; // Fire vs Bug
        TYPE_CHART[1][16] = 2.0; // Fire vs Steel
        TYPE_CHART[2][1] = 2.0; // Water vs Fire
        TYPE_CHART[2][8] = 2.0; // Water vs Ground
        TYPE_CHART[2][12] = 2.0; // Water vs Rock
        TYPE_CHART[3][2] = 2.0; // Electric vs Water
        TYPE_CHART[3][9] = 2.0; // Electric vs Flying
        TYPE_CHART[4][2] = 2.0; // Grass vs Water
        TYPE_CHART[4][8] = 2.0; // Grass vs Ground
        TYPE_CHART[4][12] = 2.0; // Grass vs Rock
        TYPE_CHART[5][4] = 2.0; // Ice vs Grass
        TYPE_CHART[5][9] = 2.0; // Ice vs Flying
        TYPE_CHART[5][14] = 2.0; // Ice vs Dragon
        TYPE_CHART[6][0] = 2.0; // Fighting vs Normal
        TYPE_CHART[6][5] = 2.0; // Fighting vs Ice
        TYPE_CHART[6][12] = 2.0; // Fighting vs Rock
        TYPE_CHART[6][15] = 2.0; // Fighting vs Dark
        TYPE_CHART[6][16] = 2.0; // Fighting vs Steel
        TYPE_CHART[7][4] = 2.0; // Poison vs Grass
        TYPE_CHART[7][17] = 2.0; // Poison vs Fairy
        TYPE_CHART[8][3] = 2.0; // Ground vs Electric
        TYPE_CHART[8][7] = 2.0; // Ground vs Poison
        TYPE_CHART[8][12] = 2.0; // Ground vs Rock
        TYPE_CHART[8][16] = 2.0; // Ground vs Steel
        TYPE_CHART[9][4] = 2.0; // Flying vs Grass
        TYPE_CHART[9][6] = 2.0; // Flying vs Fighting
        TYPE_CHART[9][11] = 2.0; // Flying vs Bug
        TYPE_CHART[10][6] = 2.0; // Psychic vs Fighting
        TYPE_CHART[10][7] = 2.0; // Psychic vs Poison
        TYPE_CHART[11][4] = 2.0; // Bug vs Grass
        TYPE_CHART[11][10] = 2.0; // Bug vs Psychic
        TYPE_CHART[11][15] = 2.0; // Bug vs Dark
        TYPE_CHART[12][1] = 2.0; // Rock vs Fire
        TYPE_CHART[12][5] = 2.0; // Rock vs Ice
        TYPE_CHART[12][9] = 2.0; // Rock vs Flying
        TYPE_CHART[12][11] = 2.0; // Rock vs Bug
        TYPE_CHART[13][10] = 2.0; // Ghost vs Psychic
        TYPE_CHART[13][13] = 2.0; // Ghost vs Ghost
        TYPE_CHART[14][14] = 2.0; // Dragon vs Dragon
        TYPE_CHART[15][10] = 2.0; // Dark vs Psychic
        TYPE_CHART[15][13] = 2.0; // Dark vs Ghost
        TYPE_CHART[16][5] = 2.0; // Steel vs Ice
        TYPE_CHART[16][12] = 2.0; // Steel vs Rock
        TYPE_CHART[16][17] = 2.0; // Steel vs Fairy
        TYPE_CHART[17][6] = 2.0; // Fairy vs Fighting
        TYPE_CHART[17][14] = 2.0; // Fairy vs Dragon
        TYPE_CHART[17][15] = 2.0; // Fairy vs Dark

        // Not very effective (0.5)
        TYPE_CHART[0][12] = 0.5; // Normal vs Rock
        TYPE_CHART[0][16] = 0.5; // Normal vs Steel
        TYPE_CHART[1][1] = 0.5; // Fire vs Fire
        TYPE_CHART[1][2] = 0.5; // Fire vs Water
        TYPE_CHART[1][12] = 0.5; // Fire vs Rock
        TYPE_CHART[1][14] = 0.5; // Fire vs Dragon
        TYPE_CHART[2][2] = 0.5; // Water vs Water
        TYPE_CHART[2][4] = 0.5; // Water vs Grass
        TYPE_CHART[2][14] = 0.5; // Water vs Dragon
        TYPE_CHART[3][3] = 0.5; // Electric vs Electric
        TYPE_CHART[3][4] = 0.5; // Electric vs Grass
        TYPE_CHART[3][14] = 0.5; // Electric vs Dragon
        TYPE_CHART[4][1] = 0.5; // Grass vs Fire
        TYPE_CHART[4][4] = 0.5; // Grass vs Grass
        TYPE_CHART[4][7] = 0.5; // Grass vs Poison
        TYPE_CHART[4][9] = 0.5; // Grass vs Flying
        TYPE_CHART[4][11] = 0.5; // Grass vs Bug
        TYPE_CHART[4][14] = 0.5; // Grass vs Dragon
        TYPE_CHART[4][16] = 0.5; // Grass vs Steel
        TYPE_CHART[5][1] = 0.5; // Ice vs Fire
        TYPE_CHART[5][2] = 0.5; // Ice vs Water
        TYPE_CHART[5][5] = 0.5; // Ice vs Ice
        TYPE_CHART[5][16] = 0.5; // Ice vs Steel
        TYPE_CHART[6][9] = 0.5; // Fighting vs Flying
        TYPE_CHART[6][7] = 0.5; // Fighting vs Poison
        TYPE_CHART[6][10] = 0.5; // Fighting vs Psychic
        TYPE_CHART[6][11] = 0.5; // Fighting vs Bug
        TYPE_CHART[6][17] = 0.5; // Fighting vs Fairy
        TYPE_CHART[7][7] = 0.5; // Poison vs Poison
        TYPE_CHART[7][8] = 0.5; // Poison vs Ground
        TYPE_CHART[7][12] = 0.5; // Poison vs Rock
        TYPE_CHART[7][13] = 0.5; // Poison vs Ghost
        TYPE_CHART[7][16] = 0.5; // Poison vs Steel
        TYPE_CHART[8][4] = 0.5; // Ground vs Grass
        TYPE_CHART[8][11] = 0.5; // Ground vs Bug
        TYPE_CHART[9][3] = 0.5; // Flying vs Electric
        TYPE_CHART[9][12] = 0.5; // Flying vs Rock
        TYPE_CHART[9][16] = 0.5; // Flying vs Steel
        TYPE_CHART[10][10] = 0.5; // Psychic vs Psychic
        TYPE_CHART[10][16] = 0.5; // Psychic vs Steel
        TYPE_CHART[11][1] = 0.5; // Bug vs Fire
        TYPE_CHART[11][6] = 0.5; // Bug vs Fighting
        TYPE_CHART[11][7] = 0.5; // Bug vs Poison
        TYPE_CHART[11][9] = 0.5; // Bug vs Flying
        TYPE_CHART[11][13] = 0.5; // Bug vs Ghost
        TYPE_CHART[11][16] = 0.5; // Bug vs Steel
        TYPE_CHART[11][17] = 0.5; // Bug vs Fairy
        TYPE_CHART[12][6] = 0.5; // Rock vs Fighting
        TYPE_CHART[12][8] = 0.5; // Rock vs Ground
        TYPE_CHART[12][16] = 0.5; // Rock vs Steel
        TYPE_CHART[13][15] = 0.5; // Ghost vs Dark
        TYPE_CHART[14][16] = 0.5; // Dragon vs Steel
        TYPE_CHART[15][6] = 0.5; // Dark vs Fighting
        TYPE_CHART[15][15] = 0.5; // Dark vs Dark
        TYPE_CHART[15][17] = 0.5; // Dark vs Fairy
        TYPE_CHART[16][1] = 0.5; // Steel vs Fire
        TYPE_CHART[16][2] = 0.5; // Steel vs Water
        TYPE_CHART[16][3] = 0.5; // Steel vs Electric
        TYPE_CHART[16][16] = 0.5; // Steel vs Steel
        TYPE_CHART[17][1] = 0.5; // Fairy vs Fire
        TYPE_CHART[17][7] = 0.5; // Fairy vs Poison
        TYPE_CHART[17][16] = 0.5; // Fairy vs Steel

        // No effect (0.0)
        TYPE_CHART[0][13] = 0.0; // Normal vs Ghost
        TYPE_CHART[6][13] = 0.0; // Fighting vs Ghost
        TYPE_CHART[7][16] = 0.0; // Poison vs Steel
        TYPE_CHART[8][9] = 0.0; // Ground vs Flying
        TYPE_CHART[13][0] = 0.0; // Ghost vs Normal
        TYPE_CHART[3][8] = 0.0; // Electric vs Ground
        TYPE_CHART[14][17] = 0.0; // Dragon vs Fairy
        TYPE_CHART[15][17] = 0.0; // Dark vs Fairy
    }

    public static double getTypeMultiplier(String attackType, String defenseType) {
        Integer attackIndex = TYPE_INDEX.get(attackType);
        Integer defenseIndex = TYPE_INDEX.get(defenseType);
        if (attackIndex == null || defenseIndex == null) {
            return 1.0; // Default to neutral if type not found
        }
        return TYPE_CHART[attackIndex][defenseIndex];
    }
}
