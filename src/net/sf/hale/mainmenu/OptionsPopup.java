/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
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

package net.sf.hale.mainmenu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.DisplayMode;

import net.sf.hale.Config;
import net.sf.hale.Game;
import net.sf.hale.util.Logger;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterFloat;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleFloatModel;

/**
 * The menu Popup responsible for setting game options, such
 * as resolution and tooltip delay.
 * @author Jared Stephen
 *
 */

public class OptionsPopup extends PopupWindow {
	private MainMenu mainMenu;
	private Content content;
	
	public OptionsPopup(MainMenu mainMenu) {
		super(mainMenu);
		this.mainMenu = mainMenu;
		
		content = new Content();
		add(content);
		
		setCloseOnClickedOutside(false);
		setCloseOnEscape(true);
	}
	
	/**
	 * Saves the config specified by the arguments to config.txt
	 * 
	 * @param resX ResolutionX
	 * @param resY ResolutionY
	 * @param edResX EditorResolutionX
	 * @param edResY EditorResolutionY
	 * @param fullscreen true for fullscreen mode, false for windowed mode
	 * @param tooltipDelay Delay until tooltips appear in milliseconds
	 * @param combatDelay combat delay in milliseconds
	 */
	
	private void writeConfigToFile(int resX, int resY, int edResX, int edResY,
			boolean fullscreen, int tooltipDelay, int combatDelay) {
		File fout = new File("config.json");
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fout));
			
			out.write("{"); out.newLine();
			
			writeSimpleKey(out, "Resolution", "[ " + resX + ", " + resY + " ]", 1);
			writeSimpleKey(out, "Fullscreen", Boolean.toString(fullscreen), 1);
			writeSimpleKey(out, "EditorResolution", "[ " + edResX + ", " + edResY + " ]", 1);
			writeSimpleKey(out, "ShowFPS", Boolean.toString(Game.config.showFPS()), 1);
			writeSimpleKey(out, "CapFPS", Boolean.toString(Game.config.capFPS()), 1);
			writeSimpleKey(out, "TooltipDelay", Integer.toString(tooltipDelay), 1);
			writeSimpleKey(out, "CombatDelay", Integer.toString(combatDelay), 1);
			writeSimpleKey(out, "ScriptConsoleEnabled", Boolean.toString(Game.config.isScriptConsoleEnabled()), 1);
			writeSimpleKey(out, "DebugMode", Boolean.toString(Game.config.isDebugModeEnabled()), 1);
			writeSimpleKey(out, "WarningMode", Boolean.toString(Game.config.isWarningModeEnabled()), 1);
			writeSimpleKey(out, "CheckForUpdatesInterval", Long.toString(Game.config.getCheckForUpdatesInterval()), 1);
			if (Game.config.randSeedSet()) {
				writeSimpleKey(out, "RandSeed", Long.toString(Game.config.getRandSeed()), 1);
			}
			
			out.write("  \"Keybindings\" : {"); out.newLine();
			
			// get the list of all keyboard actions, sorted alphabetically
			List<String> keyNames = Game.config.getKeyActionNames();
			Collections.sort(keyNames);
			
			// write the keyboard bindings
			for (String key : keyNames) {
				int keyCode = Game.config.getKeyForAction(key);
				String keyName = Event.getKeyNameForCode(keyCode);
				
				writeSimpleKey(out, key, "\"" + keyName + "\"", 2);
			}
			
			out.write("  }"); out.newLine();
			
			out.write("}"); out.newLine();
			
			out.close();
		} catch (Exception e) {
			Logger.appendToErrorLog("Error writing config file.", e);
		}
	}
	
	private void writeSimpleKey(BufferedWriter out, String key, String value, int indent) throws IOException {
		for (int i = 0; i < indent; i++) {
			out.write("  ");
		}
		
		out.write("\"");
		out.write(key);
		out.write("\" : ");
		out.write(value);
		out.write(",");
		out.newLine();
	}
	
	private class Content extends DialogLayout {
		private Label title, editorTitle;
		private Button accept, cancel;
		
		private final ToggleButton fullscreen;
		private final ComboBox<String> modesBox;
		private final SimpleChangableListModel<String> modesModel;
		private final ValueAdjusterFloat tooltipDelay;
		private final ValueAdjusterFloat combatSpeed;
		private final ComboBox<String> editorModesBox;
		private final Label modesTitle, tooltipTitle, tooltipUnits, editorModesTitle;
		private final Label combatSpeedTitle, combatSpeedUnits;
		
		private Group mainH, mainV;
		
		private Content() {
			mainH = createParallelGroup();
			mainV = createSequentialGroup();
			
			title = new Label();
			title.setTheme("titlelabel");
			addHorizontalWidgets(title);
			
			modesTitle = new Label();
			modesTitle.setTheme("modeslabel");
			
			modesModel = new SimpleChangableListModel<String>();
			modesBox = new ComboBox<String>(modesModel);
			modesBox.setTheme("modesbox");
			modesBox.addCallback(new Runnable() {
				@Override public void run() {
					setAcceptEnabled();
				}
			});
			addHorizontalWidgets(modesTitle, modesBox);
			
			fullscreen = new ToggleButton();
			fullscreen.setTheme("fullscreentoggle");
			addHorizontalWidgets(fullscreen);
			
			tooltipTitle = new Label();
			tooltipTitle.setTheme("tooltiplabel");
			
			tooltipDelay = new ValueAdjusterFloat(new SimpleFloatModel(0.1f, 2.0f, 0.4f));
			tooltipDelay.setStepSize(0.1f);
			tooltipDelay.setTheme("tooltipadjuster");
			
			tooltipUnits = new Label();
			tooltipUnits.setTheme("tooltipunitslabel");
			addHorizontalWidgets(tooltipTitle, tooltipDelay, tooltipUnits);
			
			combatSpeedTitle = new Label();
			combatSpeedTitle.setTheme("combatspeedlabel");
			
			combatSpeed = new ValueAdjusterFloat(new SimpleFloatModel(0.05f, 0.25f, 0.15f));
			combatSpeed.setStepSize(0.05f);
			combatSpeed.setTheme("combatspeedadjuster");
			
			combatSpeedUnits = new Label();
			combatSpeedUnits.setTheme("combatspeedunitslabel");
			addHorizontalWidgets(combatSpeedTitle, combatSpeed, combatSpeedUnits);
			
			mainV.addGap(DialogLayout.LARGE_GAP);
			
			editorTitle = new Label();
			editorTitle.setTheme("editortitlelabel");
			addHorizontalWidgets(editorTitle);
			
			editorModesTitle = new Label();
			editorModesTitle.setTheme("editormodeslabel");
			
			editorModesBox = new ComboBox<String>(modesModel);
			editorModesBox.addCallback(new Runnable() {
				@Override public void run() {
					setAcceptEnabled();
				}
			});
			editorModesBox.setTheme("editormodesbox");
			addHorizontalWidgets(editorModesTitle, editorModesBox);
			
			mainV.addGap(DialogLayout.LARGE_GAP);
			
			accept = new Button();
			accept.setTheme("acceptbutton");
			accept.addCallback(new Runnable() {
				@Override public void run() {
					applySettings();
					OptionsPopup.this.closePopup();
				}
			});
			
			cancel = new Button();
			cancel.setTheme("cancelbutton");
			cancel.addCallback(new Runnable() {
				@Override public void run() {
					OptionsPopup.this.closePopup();
				}
			});
			
			addHorizontalWidgets(accept, cancel);
			
			setHorizontalGroup(mainH);
			setVerticalGroup(mainV);
			
			// initialize all content
			fullscreen.setActive(Game.config.getFullscreen());
			tooltipDelay.setValue( (float)Game.config.getTooltipDelay() / 1000.0f);
			
			combatSpeed.setValue( (float)Game.config.getCombatDelay() / 1000.0f);
			
			modesModel.clear();
			for (DisplayMode mode : Game.allDisplayModes) {
				modesModel.addElement(mode.getWidth() + " x " + mode.getHeight());
			}
			
			int index = Config.getMatchingDisplayMode(Game.config.getResolutionX(), Game.config.getResolutionY());
			modesBox.setSelected(index);
			
			int editorIndex = Config.getMatchingDisplayMode(Game.config.getEditorResolutionX(),
					Game.config.getEditorResolutionY());
			editorModesBox.setSelected(editorIndex);
			
			setAcceptEnabled();
		}
		
		private void setAcceptEnabled() {
			accept.setEnabled(modesBox.getSelected() != -1 && editorModesBox.getSelected() != -1);
		}
		
		private void addHorizontalWidgets(Widget... widgets) {
			switch (widgets.length) {
			case 0: break;
			case 1:
				mainH.addWidget(widgets[0]);
				mainV.addWidget(widgets[0]);
				break;
			default:
				Group gH = createSequentialGroup(widgets);
				Group gV = createParallelGroup(widgets);
				mainH.addGroup(gH);
				mainV.addGroup(gV);
			}
		}
		
		private void applySettings() {
			DisplayMode mode = Game.allDisplayModes.get(this.modesBox.getSelected());
			boolean fullscreen = this.fullscreen.isActive();
			
			// round tooltip delay to 100 ms
			int tooltipDelay = ( ((int)(this.tooltipDelay.getValue() * 1000.0f)) + 50) / 100;
			tooltipDelay *= 100;
			
			DisplayMode edMode = Game.allDisplayModes.get(this.editorModesBox.getSelected());
			
			// round combat speed to 50 ms
			int combatSpeed = ( ((int)(this.combatSpeed.getValue() * 1000.0f)) + 25) / 50;
			combatSpeed *= 50;
			
			writeConfigToFile(mode.getWidth(), mode.getHeight(), edMode.getWidth(),
					edMode.getHeight(), fullscreen, tooltipDelay, combatSpeed);
			
			if (mode.getWidth() == Game.config.getResolutionX() && mode.getHeight() == Game.config.getResolutionY() &&
					fullscreen == Game.config.getFullscreen()) {
				
				Game.config = new Config();
				return;
			}
			
			Game.config = new Config();
			mainMenu.restartMenu();
		}
	}
}
