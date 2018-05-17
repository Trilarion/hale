package net.sf.hale.swingeditor;

import net.sf.hale.Game;
import net.sf.hale.area.Area;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.resource.Sprite;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.swingeditor.AreaRenderer.ViewHandler;
import net.sf.hale.tileset.*;
import net.sf.hale.util.PointImmutable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for selecting different types of objects such as terrain which
 * can then be painted onto the current area
 *
 * @author Jared
 */
public class AreaPalette extends JPanel implements ViewHandler {
    private static final long serialVersionUID = -8154952701111718745L;
    private final String[] tabTitles = {"Terrain", "Features", "Elevation", "Tiles", "Passable", "Transparent"};
    private final AreaClickHandler[] defaultHandlers = {new TerrainAction(), new FeatureAction(),
            new ElevationAction(), new TileAction(), new PassableAction(), new TransparentAction()};
    private AreaRenderer renderer;
    private Area area;
    private Tileset tileset;
    private TerrainGrid grid;
    private int tabIndex;

    private JLabel mouse, view;

    /**
     * Creates a new palette.  It is empty until an area is set
     */
    public AreaPalette() {
        super(new GridBagLayout());
    }

    /**
     * Returns the area associated with this palette
     *
     * @return the area
     */
    public Area getArea() {
        return area;
    }

    /**
     * Sets the area this palette is interacting with.  If non-null,
     * adds widgets for the area's tileset.  If null, all children
     * are removed from this palette
     *
     * @param area
     */
    public void setArea(AreaRenderer areaRenderer) {
        renderer = areaRenderer;
        area = areaRenderer.getArea();

        removeAll();

        if (area != null) {
            tileset = Game.curCampaign.getTileset(area.getTileset());

            addWidgets();
        }

        tabIndex = 0;
        renderer.setClickHandler(defaultHandlers[tabIndex]);
        renderer.setViewHandler(this);

        grid = new TerrainGrid(area);
    }

    private void addWidgets() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(2, 5, 2, 5);
        c.anchor = GridBagConstraints.WEST;

        c.gridwidth = 3;
        JLabel areaName = new JLabel("Editing Area: " + area.getID());
        add(areaName, c);

        c.gridy++;
        mouse = new JLabel(" ");
        add(mouse, c);

        c.gridy++;
        view = new JLabel("View at -1, -1");
        add(view, c);

        c.gridy++;
        c.gridwidth = 1;
        c.ipadx = 100;
        JLabel title = new JLabel("Tileset: " + area.getTileset());
        add(title, c);

        c.gridx++;
        c.ipadx = 0;
        add(new JLabel("Radius"), c);

        c.gridx++;
        JSpinner radius = new JSpinner(renderer.getMouseRadiusModel());
        add(radius, c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        JTabbedPane contentPane = new JTabbedPane();
        contentPane.addChangeListener(new TabChangedListener());
        add(contentPane, c);

        // add terrain tab
        List<JButton> tileButtons = new ArrayList<>();

        List<String> terrainTypeIDs = new ArrayList<>(tileset.getTerrainTypeIDs());
        Collections.sort(terrainTypeIDs);

        for (String terrainTypeID : terrainTypeIDs) {
            tileButtons.add(createTerrainButton(tileset.getTerrainType(terrainTypeID)));
        }

        contentPane.addTab(tabTitles[0], getTabPanel(tileButtons));

        // add features tab
        tileButtons.clear();

        List<String> featureTypeIDs = new ArrayList<>(tileset.getFeatureTypeIDs());
        Collections.sort(featureTypeIDs);

        for (String featureTypeID : featureTypeIDs) {
            tileButtons.add(createFeatureButton(tileset.getFeatureType(featureTypeID)));
        }

        contentPane.addTab(tabTitles[1], getTabPanel(tileButtons));

        // add elevation tab
        contentPane.addTab(tabTitles[2], new JPanel());

        // add tiles tab
        tileButtons.clear();

        for (String layerID : tileset.getLayerIDs()) {
            Layer layer = tileset.getLayer(layerID);

            for (String tileID : layer.getTiles()) {
                tileButtons.add(createTileButton(tileID, layerID));
            }
        }

        contentPane.addTab(tabTitles[3], getTabPanel(tileButtons));

        contentPane.addTab(tabTitles[4], new JPanel());

        contentPane.addTab(tabTitles[5], new JPanel());
    }

    private JScrollPane getTabPanel(List<JButton> tileButtons) {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);

        int row = 0;
        int col = 0;

        for (JButton tileButton : tileButtons) {
            c.gridx = row;
            c.gridy = col;

            panel.add(tileButton, c);

            row++;
            if (row == 2) {
                row = 0;
                col++;
            }
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(64);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollPane;
    }

    private Icon getIconFromImage(String tileID, String layerID) {
        String spriteID = tileset.getLayer(layerID).getSpriteID(tileID);

        String spriteSheetID = ResourceManager.getResourceDirectory(spriteID) + ResourceType.PNG.getExtension();

        BufferedImage sourceImage = SpriteManager.getSourceImage(spriteSheetID);

        Sprite tileSprite = SpriteManager.getImage(spriteID);

        int x = (int) (tileSprite.getTexCoordStartX() * sourceImage.getWidth());
        int y = (int) (tileSprite.getTexCoordStartY() * sourceImage.getHeight());
        int x2 = (int) (tileSprite.getTexCoordEndX() * sourceImage.getWidth());
        int y2 = (int) (tileSprite.getTexCoordEndY() * sourceImage.getHeight());

        return new ImageIcon(sourceImage.getSubimage(x, y, x2 - x, y2 - y));
    }

    private JButton createTileButton(String tileID, String layerID) {
        return new JButton(new TileAction(null, tileID, layerID, getIconFromImage(tileID, layerID)));
    }

    private JButton createTerrainButton(TerrainType terrainType) {
        TerrainTile previewTile = terrainType.getPreviewTile();
        String tileID = previewTile.getID();
        String layerID = previewTile.getLayerID();

        JButton button = new JButton(new TerrainAction(terrainType, tileID, layerID, getIconFromImage(tileID, layerID)));
        button.setVerticalTextPosition(SwingConstants.TOP);
        button.setHorizontalTextPosition(SwingConstants.CENTER);

        return button;
    }

    private JButton createFeatureButton(FeatureType featureType) {
        TerrainTile previewTile = featureType.getPreviewTile();
        String tileID = previewTile.getID();
        String layerID = previewTile.getLayerID();

        JButton button = new JButton(new FeatureAction(featureType, tileID, layerID, getIconFromImage(tileID, layerID)));
        button.setVerticalTextPosition(SwingConstants.TOP);
        button.setHorizontalTextPosition(SwingConstants.CENTER);

        return button;
    }

    @Override
    public void mouseMoved(int gridx, int gridy) {
        mouse.setText("Mouse at " + gridx + ", " + gridy);
    }

    @Override
    public void viewMoved(int gridx, int gridy) {
        view.setText("View at " + gridx + ", " + gridy);
    }

    /**
     * Used by AreaRenderer to pass information about a given click
     * back to the appropriate action
     *
     * @author jared
     */
    public interface AreaClickHandler {
        /**
         * Called when the user left clicks on the area
         *
         * @param x the grid x coordinate
         * @param y the grid y coordinate
         * @param r the grid radius
         */
        void leftClicked(int x, int y, int r);

        /**
         * Called when the user right clicks on the area
         *
         * @param x the grid x coordinate
         * @param y the grid y coordinate
         * @param r the grid radius
         */
        void rightClicked(int x, int y, int r);
    }

    private class TabChangedListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            JTabbedPane source = (JTabbedPane) changeEvent.getSource();

            int index = source.getSelectedIndex();

            if (index != tabIndex) {
                tabIndex = index;
                renderer.setClickHandler(defaultHandlers[tabIndex]);
                renderer.setActionPreviewTile(null);

                renderer.setDrawPassable(tabIndex == 4);
                renderer.setDrawTransparent(tabIndex == 5);
            }
        }
    }

    private class ElevationAction implements AreaClickHandler {
        @Override
        public void leftClicked(int x, int y, int r) {
            grid.modifyElevation(x, y, r, (byte) +1);
        }

        @Override
        public void rightClicked(int x, int y, int r) {
            grid.modifyElevation(x, y, r, (byte) -1);
        }
    }

    private class PassableAction implements AreaClickHandler {
        @Override
        public void leftClicked(int x, int y, int r) {
            for (PointImmutable p : area.getPoints(x, y, r)) {
                area.getPassability()[p.x][p.y] = true;
            }
        }

        @Override
        public void rightClicked(int x, int y, int r) {
            for (PointImmutable p : area.getPoints(x, y, r)) {
                area.getPassability()[p.x][p.y] = false;
            }
        }
    }

    private class TransparentAction implements AreaClickHandler {
        @Override
        public void leftClicked(int x, int y, int r) {
            for (PointImmutable p : area.getPoints(x, y, r)) {
                area.getTransparency()[p.x][p.y] = true;
            }
        }

        @Override
        public void rightClicked(int x, int y, int r) {
            for (PointImmutable p : area.getPoints(x, y, r)) {
                area.getTransparency()[p.x][p.y] = false;
            }
        }
    }

    private class TileAction extends AbstractAction implements AreaClickHandler {
        private static final long serialVersionUID = -2427877101000044801L;
        private final String tileID;
        private final String layerID;
        private final String spriteID;

        private TileAction() {
            tileID = null;
            layerID = null;
            spriteID = null;
        }

        private TileAction(String label, String tileID, String layerID, Icon icon) {
            super(label, icon);

            this.tileID = tileID;
            this.layerID = layerID;
            spriteID = tileset.getLayer(layerID).getSpriteID(tileID);
        }

        // called when the button is clicked
        @Override
        public void actionPerformed(ActionEvent evt) {
            renderer.setActionPreviewTile(new Tile(tileID, spriteID));
            renderer.setClickHandler(this);
        }

        @Override
        public void leftClicked(int x, int y, int radius) {
            if (tileID == null) return;

            for (PointImmutable p : area.getPoints(x, y, radius)) {
                area.getTileGrid().addTile(tileID, layerID, p.x, p.y);
            }

            area.getTileGrid().cacheSprites();
        }

        @Override
        public void rightClicked(int x, int y, int r) {
            grid.removeAllTiles(x, y, r);
        }
    }

    private class TerrainAction extends TileAction {
        private static final long serialVersionUID = -7123918009508096135L;
        private final TerrainType terrainType;

        private TerrainAction() {
            terrainType = null;
        }

        private TerrainAction(TerrainType terrainType, String tileID, String layerID, Icon icon) {
            super(terrainType.getID(), tileID, layerID, icon);

            this.terrainType = terrainType;
        }

        @Override
        public void leftClicked(int x, int y, int r) {
            if (terrainType == null) return;

            grid.setTerrain(x, y, r, terrainType);
        }
    }

    private class FeatureAction extends TileAction {
        private static final long serialVersionUID = 9006287924063244373L;
        private final FeatureType featureType;

        private FeatureAction() {
            featureType = null;
        }

        private FeatureAction(FeatureType featureType, String tileID, String layerID, Icon icon) {
            super(featureType.getID(), tileID, layerID, icon);

            this.featureType = featureType;
        }

        @Override
        public void rightClicked(int x, int y, int r) {
            grid.removeFeatureTiles(x, y, r);
        }

        @Override
        public void leftClicked(int x, int y, int r) {
            if (featureType != null) {
                grid.setFeature(x, y, r, featureType);
            }
        }
    }
}
