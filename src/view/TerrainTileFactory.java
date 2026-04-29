package view;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fabrique des tuiles de terrain.

 * Ici il y a deux familles de tuiles :
 * - l'herbe, qui doit utiliser directement l'asset valide par l'utilisateur,
 * - la terre, qui reste une tuile pixel art fixe.
 */
public final class TerrainTileFactory {
    // Taille de référence utilisée pour précharger les tuiles les plus courantes.
    private static final int GRASS_PIXEL_TILE_SIZE = 16;
    // Motif source utilisé quand on doit reconstruire une herbe de secours en pixel-art.
    private static final String GRASS_SYMBOLS = "0123456789ABCDEFGHIJKLMNOPQR";
    // Cache des variantes d'herbe par taille de tuile demandée.
    private static final Map<Integer, Image[]> GRASS_TILE_CACHE = new ConcurrentHashMap<>();
    // Cache des variantes de terre par taille de tuile demandée.
    private static final Map<Integer, Image[]> SOIL_TILE_CACHE = new ConcurrentHashMap<>();
    // Cache des tuiles chemin/pierre par taille de tuile demandée.
    private static final Map<Integer, Image[]> STONE_WITH_GRASS_TILE_CACHE = new ConcurrentHashMap<>();
    // Cache des tuiles de rivière par taille de tuile demandée.
    private static final Map<Integer, Image[]> RIVER_TILE_CACHE = new ConcurrentHashMap<>();

    /**
     * Empêche d'instancier cette classe.
     * Toutes les méthodes sont statiques.
     */
    private TerrainTileFactory() {
        // Classe utilitaire : on interdit l'instanciation.
    }

    /**
     * Renvoie la ou les tuiles d'herbe utilisées pour le terrain.
     */
    public static Image[] createGrassTiles(int pixelSize) {
        return GRASS_TILE_CACHE.computeIfAbsent(pixelSize, TerrainTileFactory::buildGrassTiles);
    }

    /**
     * Renvoie la ou les tuiles de terre utilisées pour les zones labourées.
     */
    public static Image[] createSoilTiles(int pixelSize) {
        return SOIL_TILE_CACHE.computeIfAbsent(pixelSize, TerrainTileFactory::buildSoilTiles);
    }

    /**
     * Renvoie la tuile qui mélange pierre et herbe.
     * Elle sert aux chemins et à certaines zones non cultivables.
     */
    public static Image[] createStoneWithGrass(int pixelSize) {
        return STONE_WITH_GRASS_TILE_CACHE.computeIfAbsent(
                pixelSize,
                ignored -> new Image[] { ImageLoader.load("/assets/stone_with_grass.png") }
        );
    }

    /**
     * Renvoie la tuile de rivière utilisée sur la carte.
     */
    public static Image[] createRiverTiles(int pixelSize) {
        return RIVER_TILE_CACHE.computeIfAbsent(
                pixelSize,
                ignored -> new Image[] { ImageLoader.load("/assets/river.png") }
        );
    }

    /**
     * Charge à l'avance les tuiles partagées les plus utilisées.
     */
    public static void warmupSharedTiles() {
        createGrassTiles(GRASS_PIXEL_TILE_SIZE);
        createSoilTiles(GRASS_PIXEL_TILE_SIZE);
        createStoneWithGrass(GRASS_PIXEL_TILE_SIZE);
        createRiverTiles(GRASS_PIXEL_TILE_SIZE);
    }

    /**
     * Construit les tuiles d'herbe.
     * Si une vraie image existe dans les assets, elle est utilisée en priorité.
     */
    private static Image[] buildGrassTiles(int pixelSize) {
        Image grassTile = ImageLoader.load("/assets/grass.png");
        if (grassTile != null) {
            return new Image[] { grassTile };
        }

        return new Image[] { createReferenceGrassTile(pixelSize) };
    }

    /**
     * Construit les tuiles de terre.
     * Si l'image existe dans les assets, elle est utilisée telle quelle.
     */
    private static Image[] buildSoilTiles(int pixelSize) {
        Image soilTile = ImageLoader.load("/assets/Terre.png");
        if (soilTile != null) {
            return new Image[] { soilTile };
        }
        return null;
    }

    /**
     * Fabrique une tuile d'herbe en code quand aucune image d'herbe n'est disponible.
     * La méthode construit d'abord une grande texture, puis la transforme en petite tuile pixel-art.
     */
    private static BufferedImage createReferenceGrassTile(int pixelSize) {
        Color[] sourcePalette = {
                new Color(71, 139, 26),
                new Color(95, 157, 21),
                new Color(106, 167, 18),
                new Color(80, 143, 22),
                new Color(121, 178, 16),
                new Color(103, 167, 18),
                new Color(87, 153, 23),
                new Color(96, 161, 20),
                new Color(102, 163, 20),
                new Color(89, 157, 22),
                new Color(83, 149, 23),
                new Color(118, 178, 15),
                new Color(98, 164, 19),
                new Color(110, 170, 17),
                new Color(113, 174, 16),
                new Color(111, 172, 17),
                new Color(126, 181, 15),
                new Color(117, 175, 16),
                new Color(133, 186, 14),
                new Color(145, 193, 13),
                new Color(136, 188, 15),
                new Color(64, 129, 25),
                new Color(129, 183, 14),
                new Color(144, 189, 21),
                new Color(47, 107, 27),
                new Color(167, 202, 30),
                new Color(128, 179, 20),
                new Color(115, 166, 22)
        };

        /*
         * Palette finale un peu plus courte que la precedente. Pour un effet pixel art plus marqué.
         */
        Color[] finalPalette = {
                new Color(47, 107, 27),
                new Color(64, 129, 25),
                new Color(83, 149, 23),
                new Color(98, 164, 19),
                new Color(117, 175, 16),
                new Color(133, 186, 14),
                new Color(167, 202, 30)
        };

        String[] pattern = {
                "011233456678776497A56BCD7531AC2692EF5813156E073501A7A6",
                "63D1AD79D9034457G826259271B76AH6E9CD82A821I9CF649J67F9",
                "EKE6L21ACBK0CJKJMJCJ54D8N9IHMAG1DFG2MHB9FJ7K4M4HL3C33H",
                "EC66LLO055JKPGJ4JMJKMI5IKJGPDBFB92AF7KEIKMIKKIH21OO31C",
                "2H63OOOO32HJDKFKDG5H45G2KBJBKM476A6DJ5JG4GJPIHD3LLOO03",
                "65DD8G14GJBI1A6KIJDH22HFFBF5665FMGIIBBG44H5J0MHFA6062D",
                "9FCDBCKD42D7JJQ2F5C66CL30962AH677FDE44GKFE33JMDICCC96C",
                "7H2J2JGGFBEJJJKM8HA2A10308379DKR4DGD45MHI1LOOOL9CJ6J5B",
                "IJK4JJRPPNE2MK5C6A86R3O3O7O76DGKNFK494I9CCA7331HEJJHKH",
                "MDIJM1QPPPF754ADG3C3GL2L0080L5GDJFCHJD549KJ4CJFIJIMI24",
                "KJ4GEJFQ1A0636L56EEI2MJF1GH2D42F9H5M2IDA7GA89HHKHKKKKI",
                "G7J4JGJI5D733OOL1DBBJKJPPIJI1H6A9C51DB3800L6L6HBJ4JBKB",
                "5MBJHJMIMI23OLLLL2MIGKMPPJMKJD81A79D9D7LRA3210DK4J2MKE",
                "F16CMDMEBEH5H2H9DFDIPPDI85JEB24IB3AA335PPPRMBKGGF956F5",
                "6D2IFJ5MKIGJMJ4KCDB5PP503I5H9E9EH22LLE2IPQBGGBFE703C91",
                "156C5545JEKPKJMN2J93LLOL03944HII4IKKEGGGBC555D6930OL09",
                "0DM2DC5JMJJDJJJKJG43LOOO3LJ4MJIKJKHJJEGFHD62677H73A08F",
                "1DARPRMIIJMPNMJJMM61123355GJGKJKFKJP4JD991156B1MEIHK4E",
                "A6QPPPBCJMK4KBH4EJKMKCEEI2K5K7GDJKJMJ4K969HAN04ABCMH52",
                "F08RRR54DMHJBM52GDG4HHDH91JDGIKGJJJJJJCKJ151B2DMC5MMI2",
                "GIGMJD571CDF5JEJGG9C6ECJ2MFG77AG745GIKHJBJKJBM4IJJCGDD",
                "5GGKMJFR0565GGJM4H0AA5J4JKKG7LOF3L54G85JJPKKJJGJFKHH2D",
                "568CCCK4IM5ACIHGCHD48GJMPJK4L2OLL03A1IMKBMHIK4FGCPDC76",
                "6736152HHE5KKFAA1M34MH8KEH4D10OOLLL3EDG5JFCD5GEMAAG97A",
                "609472ECEGGF2A1DLAO67BEJMMCMG7663DDJHB5E961DE4A632L105",
                "17FFEMMMDHCC676LOOLL7HN4JGJBGGKMPKJ4JKG92949H53LOOOOA0",
                "AEA6IPD9HFAAL6PQL8O22KBKBJIKGI2HGKJJHGM4GG0960GA3O0O67",
                "0830131PD3118PPPNEM44FGEFDDF2QJMDD5BIFGDHB936AH94CG666",
                "7LH0LL3I36DJ7QRQRBC2D49H69CM5K274DQIGKD8P6A89J927A0905",
                "5830LOOO11MGMBDF658717169507BHDFFHHBB48PPPF447690ADAE0",
                "9DC73366AG1H8I8296A21D60350115FDF7D2G2R1R1AH2KDALK0EA1",
                "A1HBIQGB29285699E92513063LO66D975KIBC2279H5HH2GIJM87A6",
                "7HKC40J2JKEPKHC7F1K9I9ALOOOLL17RJG1D157DHJJ4JPPIM5Q24Q",
                "BGGGKJ4JJGBMJKBB7M5I2K3636051JEI2JGI5CDJKIDKIPPJNI4JGB",
                "19DMHEK4MII85HDEFM4G445IKBBC2HJKJQJNPIBPMBHGC394PPM2CF",
                "39C7FKFKF51C68EGMHJMGJ5NIJFIJF2D2JPNJJPMKKAA3AL76N0F27",
                "AF5891C5C08ALA8MCI8KGMPGJBIKDBCGI4QIGJKD2D43AOOOOA9C6C",
                "BK7IA7H61000O10AE1F4IQMMEIJ2K8H7DCF7G2F72D75LLOLOL0FHQ",
                "MQJKPBG3LOOOO33RKF55FGB4JIGI5C9509CDHGCMHB5EF57CGIGGPI",
                "GJMGQGMMD3L3L1GMM52F2DGEHDGE4D76A6D2D2644CD55MIGGIGJD4",
                "MJKMP4JEM5KIKGJQ9DA5183H6F0D1HHKDGJKD5G4AD65075DH5K4I2",
                "E5FJBJMJIJKPKJK5J11A2KJ20AGCGJPMCMGHJBC3B8310LLCHJ1GE4",
                "CFA5AKJJJKJIJIKIKG5H4I2M2IFHA18310D4D93L078GD6E7296H9B",
                "3309A6HB4KQJFM45JKK4E278998CA3OOP1177CCF949D22A3L3LAG9",
                "78AL088444FDMNJKHGJK5906A4MF0LOOLOLL99BG4MQQAH8R60AI59",
                "AD9APPPP789QK4JH5BF4EB2DPEE5CCAA5A5AAG8C4DJQNNPNPNMBC6",
                "1LA9RNPR9MMH9J5KP4FC4JJMKHK4DAHFJFK0J2MGBM94JKGJJMF5D5",
                "9A8FQ445MDIFJGIJ4K9FEJJKJK42AEJJQPIPQJ5HMBJKJKMMK4DH99",
                "CG4IG4MI54FJDJ45691C9G4GED97IIJQJGIMJBGJGJE4BGHGDKQ72H",
                "97295CD84M445HE73L6L055H122QQF4HB51KEM4I2KGD1PPR58K277",
                "CDA603A924722H6AL3OO10DFGIM2CMCB993AD5G7F97HPPPNA88D63",
                "6D6LL0LQR6LI2B9LOOOOOLLHCC79HRK20LL0E235A56D31RAA1L8L1",
                "D14MIHK881HME55633636CD1GB02010A81HHA92A1K24JEA53G26IA",
                "A02613DL6H9CCH9MBECC1CC696E16LOL7A0L3AA9H5255663773L01"
        };

        BufferedImage base = createTransparentTile(pattern.length);
        for (int y = 0; y < pattern.length; y++) {
            String row = pattern[y];
            for (int x = 0; x < row.length(); x++) {
                int paletteIndex = GRASS_SYMBOLS.indexOf(row.charAt(x));
                if (paletteIndex < 0) {
                    continue;
                }
                paintPixel(base, x, y, sourcePalette[paletteIndex]);
            }
        }

        /*
         * Pipeline final :
         * - on retire une partie du "grand relief" responsable de la lecture courbee,
         * - on neutralise legerement la direction dominante,
         * - on reduit vers une vraie petite tuile 16x16,
         * - on groupe un peu les pixels pour obtenir un rendu un peu plus sprite.
         */
        BufferedImage straightened = neutralizeGrassCurvature(base, sourcePalette);
        BufferedImage isotropic = blendGrassWithRotations(straightened, sourcePalette);
        BufferedImage pixelated = downscaleGrassTile(isotropic, finalPalette);
        BufferedImage clustered = clusterGrassPixels(pixelated, finalPalette);

        return scaleNearest(clustered, pixelSize);
    }

    /**
     * Mélange légèrement la texture d'herbe avec ses rotations
     * pour casser les grandes directions visibles dans le motif.
     */
    private static BufferedImage blendGrassWithRotations(BufferedImage source, Color[] palette) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = createTransparentTile(width);

        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                Color c0 = new Color(source.getRGB(column, row));
                Color c90 = sampleRotated90(source, column, row, width);
                Color c180 = sampleRotated180(source, column, row, width, height);
                Color c270 = sampleRotated270(source, column, row, height);

                int blendedRed = clampColor((int) Math.round(
                        (c0.getRed() * 0.58)
                                + (c90.getRed() * 0.14)
                                + (c180.getRed() * 0.14)
                                + (c270.getRed() * 0.14)
                ));
                int blendedGreen = clampColor((int) Math.round(
                        (c0.getGreen() * 0.58)
                                + (c90.getGreen() * 0.14)
                                + (c180.getGreen() * 0.14)
                                + (c270.getGreen() * 0.14)
                ));
                int blendedBlue = clampColor((int) Math.round(
                        (c0.getBlue() * 0.58)
                                + (c90.getBlue() * 0.14)
                                + (c180.getBlue() * 0.14)
                                + (c270.getBlue() * 0.14)
                ));

                Color blended = new Color(blendedRed, blendedGreen, blendedBlue);
                result.setRGB(column, row, findNearestPaletteColor(blended, palette).getRGB());
            }
        }

        return result;
    }

    /**
     * On lit la couleur du même point après une rotation à 90 degrés.
     */
    private static Color sampleRotated90(BufferedImage source, int column, int row, int width) {
        return new Color(source.getRGB(row, width - 1 - column));
    }

    /**
     * On lit la couleur du même point après une rotation à 180 degrés.
     */
    private static Color sampleRotated180(BufferedImage source, int column, int row, int width, int height) {
        return new Color(source.getRGB(width - 1 - column, height - 1 - row));
    }

    /**
     * On lit la couleur du même point après une rotation à 270 degrés.
     */
    private static Color sampleRotated270(BufferedImage source, int column, int row, int height) {
        return new Color(source.getRGB(height - 1 - row, column));
    }

    /**
     * Réduit une grande texture en une petite tuile 16x16.
     * Cela donne une apparence plus proche d'un vrai sprite de terrain.
     */
    private static BufferedImage downscaleGrassTile(BufferedImage source, Color[] palette) {
        BufferedImage result = createTransparentTile(TerrainTileFactory.GRASS_PIXEL_TILE_SIZE);
        double scale = (double) source.getWidth() / TerrainTileFactory.GRASS_PIXEL_TILE_SIZE;

        for (int targetY = 0; targetY < TerrainTileFactory.GRASS_PIXEL_TILE_SIZE; targetY++) {
            for (int targetX = 0; targetX < TerrainTileFactory.GRASS_PIXEL_TILE_SIZE; targetX++) {
                int startX = (int) Math.floor(targetX * scale);
                int endX = Math.max(startX + 1, (int) Math.floor((targetX + 1) * scale));
                int startY = (int) Math.floor(targetY * scale);
                int endY = Math.max(startY + 1, (int) Math.floor((targetY + 1) * scale));

                long redSum = 0;
                long greenSum = 0;
                long blueSum = 0;
                long count = 0;
                for (int sampleY = startY; sampleY < endY; sampleY++) {
                    for (int sampleX = startX; sampleX < endX; sampleX++) {
                        Color sample = new Color(source.getRGB(sampleX, sampleY));
                        redSum += sample.getRed();
                        greenSum += sample.getGreen();
                        blueSum += sample.getBlue();
                        count++;
                    }
                }

                Color averaged = new Color(
                        (int) Math.round(redSum / (double) count),
                        (int) Math.round(greenSum / (double) count),
                        (int) Math.round(blueSum / (double) count)
                );
                result.setRGB(targetX, targetY, findNearestPaletteColor(averaged, palette).getRGB());
            }
        }

        return result;
    }

    /**
     * Regroupe légèrement les pixels voisins pour donner un rendu plus compact et plus lisible.
     */
    private static BufferedImage clusterGrassPixels(BufferedImage source, Color[] palette) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = createTransparentTile(width);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color center = new Color(source.getRGB(x, y));
                Color left = new Color(source.getRGB(floorMod(x - 1, width), y));
                Color right = new Color(source.getRGB(floorMod(x + 1, width), y));
                Color top = new Color(source.getRGB(x, floorMod(y - 1, height)));
                Color bottom = new Color(source.getRGB(x, floorMod(y + 1, height)));

                int red = clampColor((int) Math.round(
                        (center.getRed() * 0.52)
                                + (left.getRed() * 0.12)
                                + (right.getRed() * 0.12)
                                + (top.getRed() * 0.12)
                                + (bottom.getRed() * 0.12)
                ));
                int green = clampColor((int) Math.round(
                        (center.getGreen() * 0.52)
                                + (left.getGreen() * 0.12)
                                + (right.getGreen() * 0.12)
                                + (top.getGreen() * 0.12)
                                + (bottom.getGreen() * 0.12)
                ));
                int blue = clampColor((int) Math.round(
                        (center.getBlue() * 0.52)
                                + (left.getBlue() * 0.12)
                                + (right.getBlue() * 0.12)
                                + (top.getBlue() * 0.12)
                                + (bottom.getBlue() * 0.12)
                ));

                Color averaged = new Color(red, green, blue);
                result.setRGB(x, y, findNearestPaletteColor(averaged, palette).getRGB());
            }
        }

        return result;
    }

    /**
     * Réduit les contrastes trop marqués qui donnent une impression de relief arrondi à l'herbe.
     */
    private static BufferedImage neutralizeGrassCurvature(BufferedImage source, Color[] palette) {
        int width = source.getWidth();
        int height = source.getHeight();
        double[][] luminance = new double[height][width];
        double globalLuminance = 0.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(source.getRGB(x, y));
                double value = getLuminance(color);
                luminance[y][x] = value;
                globalLuminance += value;
            }
        }
        globalLuminance /= (width * height);

        BufferedImage result = createTransparentTile(width);
        int radius = 7;
        int kernelSize = (radius * 2) + 1;
        double sampleCount = kernelSize * kernelSize;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double localLuminance = 0.0;
                for (int offsetY = -radius; offsetY <= radius; offsetY++) {
                    int sampleY = floorMod(y + offsetY, height);
                    for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                        int sampleX = floorMod(x + offsetX, width);
                        localLuminance += luminance[sampleY][sampleX];
                    }
                }
                localLuminance /= sampleCount;

                Color sourceColor = new Color(source.getRGB(x, y));

                // Valeur un peu plus forte qu'avant :
                // on veut toujours le meme rendu de base, mais un relief global moins marque.
                double luminanceDelta = (globalLuminance - localLuminance) * 0.56;
                int correctedRed = clampColor(sourceColor.getRed() + (int) Math.round(luminanceDelta));
                int correctedGreen = clampColor(sourceColor.getGreen() + (int) Math.round(luminanceDelta));
                int correctedBlue = clampColor(sourceColor.getBlue() + (int) Math.round(luminanceDelta));

                Color corrected = new Color(correctedRed, correctedGreen, correctedBlue);
                Color snapped = findNearestPaletteColor(corrected, palette);
                result.setRGB(x, y, snapped.getRGB());
            }
        }

        return result;
    }

    /**
     * Redimensionne une image sans lisser les pixels.
     * C'est important pour conserver un rendu net en pixel-art.
     */
    private static BufferedImage scaleNearest(BufferedImage source, int pixelSize) {
        if (pixelSize == source.getWidth()) {
            return source;
        }

        BufferedImage scaled = createTransparentTile(pixelSize);
        for (int y = 0; y < pixelSize; y++) {
            for (int x = 0; x < pixelSize; x++) {
                int sourceX = (x * source.getWidth()) / pixelSize;
                int sourceY = (y * source.getHeight()) / pixelSize;
                scaled.setRGB(x, y, source.getRGB(sourceX, sourceY));
            }
        }
        return scaled;
    }

    /**
     * Crée une image transparente carrée de la taille demandée.
     */
    private static BufferedImage createTransparentTile(int pixelSize) {
        return new BufferedImage(pixelSize, pixelSize, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Calcule la luminosité approximative d'une couleur.
     */
    private static double getLuminance(Color color) {
        return (0.299 * color.getRed()) + (0.587 * color.getGreen()) + (0.114 * color.getBlue());
    }

    /**
     * Empêche une composante de couleur de sortir de l'intervalle autorisé entre 0 et 255.
     */
    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * Cherche dans une palette la couleur la plus proche de la couleur demandée.
     */
    private static Color findNearestPaletteColor(Color target, Color[] palette) {
        Color best = palette[0];
        long bestDistance = Long.MAX_VALUE;
        for (Color candidate : palette) {
            long redDelta = target.getRed() - candidate.getRed();
            long greenDelta = target.getGreen() - candidate.getGreen();
            long blueDelta = target.getBlue() - candidate.getBlue();
            long distance = (redDelta * redDelta) + (greenDelta * greenDelta) + (blueDelta * blueDelta);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    /**
     * Calcule un modulo toujours positif, même si la valeur de départ est négative.
     */
    private static int floorMod(int value, int modulo) {
        int result = value % modulo;
        return result < 0 ? result + modulo : result;
    }

    /**
     * Colore un seul pixel d'une image si sa position est valide.
     */
    private static void paintPixel(BufferedImage image, int x, int y, Color color) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
            return;
        }
        image.setRGB(x, y, color.getRGB());
    }
}
