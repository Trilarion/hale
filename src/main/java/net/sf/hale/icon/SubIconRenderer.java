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

package net.sf.hale.icon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.icon.ComposedCreatureIcon.Entry;
import net.sf.hale.icon.SubIcon.Factory;
import net.sf.hale.icon.SubIcon.Type;
import net.sf.hale.rules.Race;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.rules.Ruleset.Gender;
import net.sf.hale.util.Point;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Color;

/**
 * A SubIconRenderer is based on a creature ComposedCreatureIcon, and is modifiable; it
 * changes as the creature it is based on changes.
 * @author Jared
 *
 */

public class SubIconRenderer implements IconRenderer {
	private final List<SubIcon> subIcons;
	
	private Color skinColor;
	private Color clothingColor;
	
	private SubIcon beard;
	private SubIcon hair;
	private SubIcon ears;
	
	/**
	 * Creates a new SubIconRenderer for a parent with the specified icon, race, and gender
	 * @param icon
	 * @param race
	 * @param gender
	 */
	
	public SubIconRenderer(ComposedCreatureIcon icon, Race race, Gender gender) {
		subIcons = new ArrayList<>();

        skinColor = icon.getSkinColor();
        clothingColor = icon.getClothingColor();
		
		if (!icon.containsBaseBackgroundSubIcon()) {
			addBaseRacialSubIcons(race, gender);
		}
		
		for (Entry entry : icon) {
			Factory factory = new Factory(entry.type, race, gender);
			factory.setPrimaryIcon(entry.spriteID, entry.color);
			
			add(factory.createSubIcon());
		}
		
		Collections.sort(subIcons);
	}
	
	/**
	 * Creates a new SubIconList which is a copy of the
	 * specified Icon
	 * @param other
	 */
	
	protected SubIconRenderer(SubIconRenderer other) {
		subIcons = new ArrayList<>(other.subIcons);
		
		beard = other.beard;
		hair = other.hair;
		ears = other.ears;
		
		skinColor = other.skinColor;
		clothingColor = other.clothingColor;
	}
	
	public void setSkinColor(Color color) {
        skinColor = color;
	}
	
	public void setClothingColor(Color color) {
        clothingColor = color;
	}
	
	public Color getSkinColor() {
		return skinColor;
	}
	
	public Color getClothingColor() {
		return clothingColor;
	}
	
	/**
	 * Adds the base background and foreground icons for the specified
	 * race and gender to this Icon
	 * @param race
	 * @param gender
	 */
	
	private void addBaseRacialSubIcons(Race race, Gender gender) {
		// remove any old base icons
		remove(Type.BaseBackground);
		remove(Type.BaseForeground);
		remove(Type.Ears);
		
		switch (gender) {
		case Male:
			if (race.getMaleBackgroundIcon() != null) {
				Factory factory = new Factory(Type.BaseBackground, race, gender);
				factory.setPrimaryIcon(race.getMaleBackgroundIcon(), getSkinColor());
				factory.setSecondaryIcon(race.getMaleClothesIcon(), getClothingColor());
				add(factory.createSubIcon());
			}
			
			if (race.getMaleForegroundIcon() != null) {
				Factory factory = new Factory(Type.BaseForeground, race, gender);
				factory.setPrimaryIcon(race.getMaleForegroundIcon(), getSkinColor());
				add(factory.createSubIcon());
			}
			
			if (race.getMaleEarsIcon() != null) {
				Factory factory = new Factory(Type.Ears, race, gender);
				factory.setPrimaryIcon(race.getMaleEarsIcon(), getSkinColor());
				add(factory.createSubIcon());
			}
			break;
		case Female:
			if (race.getFemaleBackgroundIcon() != null) {
				Factory factory = new Factory(Type.BaseBackground, race, gender);
				factory.setPrimaryIcon(race.getFemaleBackgroundIcon(), getSkinColor());
				factory.setSecondaryIcon(race.getFemaleClothesIcon(), getClothingColor());
				add(factory.createSubIcon());
			}
			
			if (race.getFemaleForegroundIcon() != null) {
				Factory factory = new Factory(Type.BaseForeground, race, gender);
				factory.setPrimaryIcon(race.getFemaleForegroundIcon(), getSkinColor());
				add(factory.createSubIcon());
			}
			
			if (race.getFemaleEarsIcon() != null) {
				Factory factory = new Factory(Type.Ears, race, gender);
				factory.setPrimaryIcon(race.getFemaleEarsIcon(), getSkinColor());
				add(factory.createSubIcon());
			}
			break;
		}
	}
	
	/**
	 * Adds the specified SubIcon to the list of SubIcons displayed by this Icon
	 * @param subIcon
	 */
	
	public synchronized void add(SubIcon subIcon) {
		if (subIcon == null)
			return;
		
		if (subIcon.getIcon() == null)
			return;
		
		remove(subIcon.getType());

		subIcons.add(subIcon);
		
		Collections.sort(subIcons);
		
		switch (subIcon.getType()) {
		case Ears:
			ears = subIcon;
			break;
		case Beard:
			beard = subIcon;
			break;
		case Hair:
			hair = subIcon;
			break;
		case Head:
			remove(Type.Ears);
			break;
		default:
			// do nothing special
		}
		
		if (subIcon.coversHair())
			remove(Type.Hair);
		
		if (subIcon.coversBeard())
			remove(Type.Beard);
	}
	
	/**
	 * Removes all SubIcons from this Icon
	 */
	
	public synchronized void clear() {
		subIcons.clear();
		hair = null;
		ears = null;
		beard = null;
	}
	
	/**
	 * Removes all SubIcons of the specified type from this Icon
	 * @param type
	 */
	
	public synchronized void remove(Type type) {
		Iterator<SubIcon> iter = subIcons.iterator();
		while (iter.hasNext()) {
			if (iter.next().getType() == type) {
				iter.remove();
			}
		}
		
		if (type == Type.Head && ears != null) {
			add(ears);
		}
		
		if (type != Type.Beard) {
			boolean addBeard = true;
			for (SubIcon subIcon : subIcons) {
				if (subIcon.coversBeard()) {
					addBeard = false;
					break;
				}

				if (subIcon.getType() == Type.Beard) {
					addBeard = false;
					break;
				}
			}

			if (addBeard && beard != null)
				add(beard);
		}
		
		if (type != Type.Hair) {
			boolean addHair = true;
			for (SubIcon subIcon : subIcons) {
				if (subIcon.coversHair()) {
					addHair = false;
					break;
				}
				
				if (subIcon.getType() == Type.Hair) {
					addHair = false;
					break;
				}
			}
			
			if (addHair && hair != null)
				add(hair);
		}
	}
	
	/**
	 * Returns the beard icon, even if the beard icon is not currently being
	 * drawn
	 * @return the beard icon
	 */
	
	public String getBeardIcon() {
		if (beard == null) return null;
		
		return beard.getIcon();
	}
	
	/**
	 * Returns the beard color, even if the beard icon is not currently being drawn
	 * @return the beard color
	 */
	
	public Color getBeardColor() {
		if (beard == null) return null;
		
		return beard.getColor();
	}
	
	/**
	 * Returns the hair icon, even if a helmet is equipped and the hair icon is not currently
	 * being drawn
	 * @return the hair icon
	 */
	
	public String getHairIcon() {
		if (hair == null) return null;
		
		return hair.getIcon();
	}
	
	/**
	 * Returns the hair color, even if a helmet is equipped and the hair icon is not currently
	 * being drawn
	 * @return the hair color
	 */
	
	public Color getHairColor() {
		if (hair == null) return null;
		
		return hair.getColor();
	}
	
	public Point getOffset(String type) { return getOffset(Type.valueOf(type)); }
	public Color getColor(String type) { return getColor(Type.valueOf(type)); }
	public String getIcon(String type) { return getIcon(Type.valueOf(type)); }
	
	public SubIcon getSubIcon(String type) { return getSubIcon(Type.valueOf(type)); }
	
	public SubIcon getSubIcon(Type type) {
		for (SubIcon subIcon : subIcons) {
			if (subIcon.getType() == type) return subIcon;
		}
		
		return null;
	}
	
	public Point getOffset(Type type) {
		for (SubIcon subIcon : subIcons) {
			if (subIcon.getType() == type) return subIcon.getOffset();
		}
		
		return new Point(0, 0);
	}
	
	public Color getColor(Type type) {
		for (SubIcon subIcon : subIcons) {
			if (subIcon.getType() == type) return subIcon.getColor();
		}
		
		return null;
	}
	
	public String getIcon(Type type) {
		for (SubIcon subIcon : subIcons) {
			if (subIcon.getType() == type) return subIcon.getIcon();
		}
		
		return null;
	}
	
	

	@Override public synchronized void draw(int x, int y) {
		for (SubIcon subIcon : subIcons) {
			subIcon.draw(x, y);
		}
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
	}

	@Override public void drawCentered(int x, int y, int width, int height) {
		int offsetX = x + (width - Game.TILE_SIZE) / 2;
		int offsetY = y + (height - Game.TILE_SIZE) / 2;
		
		draw(offsetX, offsetY);
	}
}
