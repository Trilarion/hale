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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import net.sf.hale.Game;
import net.sf.hale.area.Area;
import net.sf.hale.loading.SaveWriter;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;

/**
 * The menu bar for the swing campaign Editor
 * @author Jared
 *
 */

public class EditorMenuBar extends JMenuBar {
    private static final long serialVersionUID = 4598856247854294461L;
    private SwingEditor frame;
	
	private JMenu areasMenu;
	private JMenuItem createAreaItem;
	private JMenu editorsMenu;
	
	private JTextField logItem;
	
	private JMenuItem saveItem;
	
	/**
	 * Creates a new menu bar
	 * @param frame the parent frame
	 */
	
	public EditorMenuBar(SwingEditor frame) {
		this.frame = frame;
		
		// create campaign menu
		JMenu campaignMenu = new JMenu("Campaign");
		campaignMenu.setMnemonic(KeyEvent.VK_C);
		add(campaignMenu);
		
		JMenuItem newItem = new JMenuItem(new NewAction());
		newItem.setEnabled(false);
		newItem.setMnemonic(KeyEvent.VK_N);
		campaignMenu.add(newItem);
		
		saveItem = new JMenuItem(new SaveAction());
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		campaignMenu.add(saveItem);
		
		// add open menu and a menu item for each campaign directory
		JMenu openMenu = new JMenu("Open");
		openMenu.setMnemonic(KeyEvent.VK_O);
		campaignMenu.add(openMenu);
		
		File campaignDir = new File("campaigns");
		String[] fileList = campaignDir.list();
        for (String aFileList : fileList) {
            File f = new File("campaigns/" + aFileList);
            if (f.isDirectory() && !".svn".equals(f.getName())) {
                JMenuItem openItem = new JMenuItem(new OpenAction(aFileList));
                openMenu.add(openItem);
            }
        }

		JMenuItem extractItem = new JMenuItem("Extract Zip", KeyEvent.VK_E);
		extractItem.setEnabled(false);
		campaignMenu.add(extractItem);
		
		JMenuItem compressItem = new JMenuItem("Compress to Zip", KeyEvent.VK_C);
		compressItem.setEnabled(false);
		campaignMenu.add(compressItem);
		
		JMenuItem exitItem = new JMenuItem(new ExitAction());
		exitItem.setMnemonic(KeyEvent.VK_X);
		campaignMenu.add(exitItem);
		
		// create areas menu, it won't be populated until a campaign is loaded
		areasMenu = new JMenu("Areas");
		areasMenu.setEnabled(false);
		areasMenu.setMnemonic(KeyEvent.VK_A);
		add(areasMenu);
		
		createAreaItem = new JMenuItem(new CreateAreaAction());
		createAreaItem.setMnemonic(KeyEvent.VK_C);
		areasMenu.add(createAreaItem);
		
		// create editors menu
		editorsMenu = new JMenu("Windows");
		editorsMenu.setMnemonic(KeyEvent.VK_E);
		editorsMenu.setEnabled(false);
		add(editorsMenu);
		
		JMenuItem createEditorItem = new JMenuItem(new CreateEditorAction());
		createEditorItem.setMnemonic(KeyEvent.VK_N);
		editorsMenu.add(createEditorItem);
		
		JMenuItem closeEditorsItem = new JMenuItem(new CloseEditorsAction());
		closeEditorsItem.setMnemonic(KeyEvent.VK_C);
		editorsMenu.add(closeEditorsItem);
		
		JMenuItem showLogViewerItem = new JMenuItem(new ShowLogViewerAction());
		showLogViewerItem.setMnemonic(KeyEvent.VK_L);
		editorsMenu.add(showLogViewerItem);
		
		logItem = new JTextField();
		logItem.setEditable(false);
		logItem.setHorizontalAlignment(JTextField.RIGHT);
		logItem.setBorder(null);
		add(logItem);
		
		updateCampaign();
	}
	
	/**
	 * Sets the text that is displayed in the upper right hand corner of the screen (the most recent log entry)
	 * @param text the log text to display
	 */
	
	public void setLogText(String text) {
		logItem.setText(text);
	}
	
	/**
	 * This method is called after a campaign is loaded / unloaded, to
	 * update the status of the menu bars
	 */
	
	public void updateCampaign() {
		areasMenu.removeAll();
		
		areasMenu.add(createAreaItem);
		areasMenu.addSeparator();
		
		if (Game.curCampaign == null) {
			areasMenu.setEnabled(false);
			editorsMenu.setEnabled(false);
			
			saveItem.setEnabled(false);
		} else {
			areasMenu.setEnabled(true);
			editorsMenu.setEnabled(true);
			
			saveItem.setEnabled(true);
			
			// populate the list of areas
			File areaDir = new File("campaigns/" + Game.curCampaign.getID() + "/areas");
			List<File> areaFiles = FileUtil.getFiles(areaDir);
			for (File file : areaFiles) {
				String ref = file.getName().substring(0, file.getName().length() - ResourceType.JSON.getExtension().length());
				
				JMenuItem item = new JMenuItem(new OpenAreaAction(ref));
				areasMenu.add(item);
			}
		}
	}
	
	private static class ShowLogViewerAction extends AbstractAction {
        private static final long serialVersionUID = -3611878058486723106L;

        private ShowLogViewerAction() { super("Show Log Viewer"); }
		
		@Override public void actionPerformed(ActionEvent e) {
			EditorManager.showLogViewer();
		}
	}
	
	private static class CreateEditorAction extends AbstractAction {
        private static final long serialVersionUID = -5714162841035187430L;

        private CreateEditorAction() { super("Create New Editor"); }
		
		@Override public void actionPerformed(ActionEvent e) {
			EditorManager.createNewEditor();
		}
	}
	
	private static class CloseEditorsAction extends AbstractAction {
        private static final long serialVersionUID = -1906343608215693699L;

        private CloseEditorsAction() { super("Close All Editors"); }
		
		@Override public void actionPerformed(ActionEvent e) {
			EditorManager.closeAllEditors();
		}
	}
	
	private class CreateAreaAction extends AbstractAction {
        private static final long serialVersionUID = 5002980950021254492L;

        private CreateAreaAction() { super("Create"); }
		
		@Override public void actionPerformed(ActionEvent e) {
			CreateAreaDialog dialog = new CreateAreaDialog(frame);
			dialog.setVisible(true);
		}
	}
	
	private class OpenAreaAction extends AbstractAction {
        private static final long serialVersionUID = -1924177985489346230L;
        private String areaID;
		
		private OpenAreaAction(String areaID) {
			super(areaID);
			this.areaID = areaID;
		}
		
		@Override public void actionPerformed(ActionEvent e) {
			// set sprite manager to save bufferedImages of all spritesheets for use later
			SpriteManager.setSaveSourceImages(true);
			
			Area area = Game.curCampaign.getArea(areaID);
			AreaUtil.setMatrix(area.getExplored(), true);
			
			AreaRenderer viewer = new AreaRenderer(area, frame.getOpenGLCanvas());
			frame.setAreaViewer(viewer);
			
			EditorManager.addLogEntry("Opened area: " + area.getID());
		}
	}
	
	private static class NewAction extends AbstractAction {
        private static final long serialVersionUID = 5624286329069345214L;

        private NewAction() { super("New"); }
		
		@Override public void actionPerformed(ActionEvent e) {
			
		}
	}
	
	private class SaveAction extends AbstractAction {
        private static final long serialVersionUID = 6545714628966761638L;

        private SaveAction() {
			super("Save");
		}
		
		@Override public void actionPerformed(ActionEvent e) {
			if (frame.getPalette() != null && frame.getPalette().getArea() != null) {
				Area area = frame.getPalette().getArea();
				
				try {
					PrintWriter out = new PrintWriter(new File("campaigns/" + Game.curCampaign.getID() +
							"/areas/" + area.getID() + ResourceType.JSON.getExtension()));

					SaveWriter.CompactArrays = true;
					SaveWriter.writeJSON(area.writeToJSON(), out);
					SaveWriter.CompactArrays = false;
					
					out.close();
					
					EditorManager.addLogEntry("Saved area: " + area.getID());
				} catch (Exception ex) {
					Logger.appendToErrorLog("Error saving area" + area.getID(), ex);
					
					EditorManager.addLogEntry("Error saving area: " + area.getID());
				}
			}
		}
	}
	
	private class OpenAction extends AbstractAction {
        private static final long serialVersionUID = 2282772406817617325L;
        private String campaignID;
		
		private OpenAction(String id) {
			super(id);
            campaignID = id;
		}
		
		@Override public void actionPerformed(ActionEvent e) {
			CampaignLoader loader = new CampaignLoader(EditorMenuBar.this, campaignID);
			loader.execute();
			
			EditorManager.addLogEntry("Opened campaign: " + campaignID);
		}
	}
	
	private class ExitAction extends AbstractAction {
        private static final long serialVersionUID = 1111936164372344122L;

        private ExitAction() { super("Exit"); }
		
		@Override public void actionPerformed(ActionEvent e) {
			WindowEvent wev = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
		}
	}
}
