package net.sf.hale.area;

import net.sf.hale.Game;
import net.sf.hale.rules.Dice;
import net.sf.hale.tileset.TerrainTile;
import net.sf.hale.tileset.TerrainType;
import net.sf.hale.tileset.Tileset;
import net.sf.hale.util.PointImmutable;
import net.sf.hale.util.SimpleJSONObject;

/**
 * For handling of procedural generation of terrain within an area
 *
 * @author Jared
 */
public class Procedural {
    private final Tileset tileset;
    private final Area area;
    private final String baseTerrain;
    private final Generator generator;
    private long seed;
    private Dice random;

    /**
     * Creates a new ProceduralGeneration object for the specified area
     *
     * @param area
     * @param data
     */
    public Procedural(Area area, SimpleJSONObject data) {
        this.area = area;
        tileset = Game.curCampaign.getTileset(area.getTileset());

        baseTerrain = data.get("baseTerrain", null);

        seed = Game.dice.randSeed();

        if (data.containsKey("gridGenerator")) {
            generator = new GridGenerator(area, data.getObject("gridGenerator"));
        } else {
            throw new IllegalArgumentException("Generator for area " + area.getID() + " does not specify a generator type");
        }
    }

    /**
     * Returns a list of the 6 hex tiles adjacent to the specified grid coordinates
     *
     * @param gridX
     * @param gridY
     * @return the list of adjacent tiles
     */
    public static PointImmutable[] getAdjacentTiles(int gridX, int gridY) {
        PointImmutable[] adjacent = new PointImmutable[6];

        adjacent[0] = new PointImmutable(gridX, gridY - 1); // North
        adjacent[1] = new PointImmutable(gridX + 1, gridY - (gridX + 1) % 2); // North-East
        adjacent[2] = new PointImmutable(gridX + 1, gridY + gridX % 2); // South-East
        adjacent[3] = new PointImmutable(gridX, gridY + 1); // South
        adjacent[4] = new PointImmutable(gridX - 1, gridY + gridX % 2); // South-West
        adjacent[5] = new PointImmutable(gridX - 1, gridY - (gridX + 1) % 2); // North-West

        return adjacent;
    }

    /**
     * Returns the value of the seed used by this generator
     *
     * @return the seed value
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Sets the random seed being used by this generator
     *
     * @param seed
     */
    public void setSeed(long seed) {
        this.seed = seed;
    }

    /**
     * Generates layers and tiles for the parent area based on the attributes of this generator
     */
    public void generateLayers() {
        random = new Dice(seed);

        // fill base terrain
        TerrainType baseTerrain = tileset.getTerrainType(this.baseTerrain);

        for (int x = 0; x < area.getWidth(); x++) {
            for (int y = 0; y < area.getHeight(); y++) {
                TerrainTile tile = baseTerrain.getRandomTerrainTile(random);

                area.getTileGrid().addTile(tile.getID(), tile.getLayerID(), x, y);

                area.getTransparency()[x][y] = true;
                area.getPassability()[x][y] = true;
            }
        }

        generator.setDice(random);
        generator.generate();
    }
}
