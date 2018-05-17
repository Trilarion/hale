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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * A standalone editor that can switch between many modes to edit
 * different aspects of the campaign data
 *
 * @author Jared
 */
public class AssetEditor extends JFrame implements ListSelectionListener {
    private static final long serialVersionUID = 1713341621255338207L;
    private AssetModel<?> listModel;
    private JList list;
    private int currentIndex = -1;

    private JPanel subEditor;
    private JPanel right;

    /**
     * Creates a new AssetEditor in the default editing mode
     */
    public AssetEditor() {
        setTitle("Hale Editor");
        setSize(800, 600);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu editorMenu = new JMenu("Editor");
        menuBar.add(editorMenu);

        JMenuItem closeEditor = new JMenuItem(new CloseEditorAction());
        editorMenu.add(closeEditor);

        JMenu modeMenu = new JMenu("Mode");
        menuBar.add(modeMenu);

        ButtonGroup modeGroup = new ButtonGroup();
        for (AssetType mode : AssetType.values()) {
            JMenuItem menuItem = new JRadioButtonMenuItem(new SetModeAction(mode));
            modeMenu.add(menuItem);
            modeGroup.add(menuItem);
        }

        JPanel left = new JPanel(new BorderLayout());

        list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.addListSelectionListener(this);

        JScrollPane listScroll = new JScrollPane(list);
        left.add(listScroll);

        right = new JPanel(new BorderLayout());

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
    }

    public void setMode(AssetModel<?> model) {
        list.setModel(model);
        listModel = model;
        currentIndex = -1;
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        int newIndex = list.getSelectedIndex();

        if (newIndex == currentIndex) return;

        currentIndex = newIndex;

        if (currentIndex < 0) return;

        if (subEditor != null) {
            right.remove(subEditor);
        }

        subEditor = listModel.getEntry(currentIndex).createAssetSubEditor(this);

        right.add(subEditor);
        right.revalidate();
    }

    private class SetModeAction extends AbstractAction {
        private static final long serialVersionUID = -5589551401514122552L;
        private AssetType mode;

        private SetModeAction(AssetType mode) {
            super(mode.toString());
            this.mode = mode;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            mode.setMode(AssetEditor.this);
        }
    }

    private class CloseEditorAction extends AbstractAction {
        private static final long serialVersionUID = 8328251230910458938L;

        private CloseEditorAction() {
            super("Close");
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            EditorManager.closeEditor(AssetEditor.this);
        }
    }
}
