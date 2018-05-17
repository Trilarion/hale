/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.sf.hale.swingeditor;

import net.sf.hale.resource.PackageType;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A model of any generic type of asset
 *
 * @author Jared
 */
public class AssetModel<E> implements ListModel {
    private AssetType assetType;

    private List<ListDataListener> listeners;

    private List<Entry> entries;

    /**
     * Creates a new AssetModel, listing all assets of the specified type
     *
     * @param type
     */
    public AssetModel(AssetType type) {
        assetType = type;

        listeners = new ArrayList<>(2);
        entries = new ArrayList<>();

        Set<String> resources = ResourceManager.getResourcesInDirectory(type.getContainingDirectory());

        for (String resource : resources) {
            PackageType packageType = ResourceManager.getPackageTypeOfResource(resource);

            String assetID = ResourceManager.getResourceIDNoPath(resource, ResourceType.JSON);

            Entry entry = new Entry(assetID, packageType);
            entries.add(entry);
        }
    }

    /**
     * Gets the entry at the specified index
     *
     * @param index
     * @return the specified entry
     */
    public Entry getEntry(int index) {
        return entries.get(index);
    }

    @Override
    public String getElementAt(int index) {
        return entries.get(index).elementID;
    }

    @Override
    public int getSize() {
        return entries.size();
    }

    @Override
    public void addListDataListener(ListDataListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListDataListener(ListDataListener listener) {
        listeners.remove(listener);
    }

    /**
     * A single entry in this model.  Each entry stores only the string
     * and whether it is a core or campaign resource, until it is modified
     *
     * @author Jared
     */
    public class Entry {
        public final String assetID;
        public final PackageType type;

        private final String elementID;

        private Entry(String assetID, PackageType type) {
            this.assetID = assetID;
            this.type = type;

            switch (type) {
                case Campaign:
                    elementID = assetID;
                    break;
                default:
                    elementID = "Core : " + assetID;
            }
        }

        /**
         * Returns the asset for this entry
         *
         * @return the asset for this entry
         */
        @SuppressWarnings("unchecked")
        public E getAsset() {
            return (E) assetType.getAsset(assetID);
        }

        /**
         * Creates a new Sub Editor to edit this entry's asset
         *
         * @param parent the parent frame
         * @return the new sub editor
         */
        public JPanel createAssetSubEditor(JFrame parent) {
            return assetType.createSubEditor(parent, assetID);
        }
    }
}
