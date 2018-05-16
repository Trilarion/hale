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

package net.sf.hale.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hale.Game;
import net.sf.hale.ability.Ability;
import net.sf.hale.ability.AbilitySelectionList;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.EntityManager;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Inventory.Slot;
import net.sf.hale.entity.WeaponTemplate;
import net.sf.hale.icon.SubIcon;
import net.sf.hale.icon.SubIcon.Type;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.util.SimpleJSONArray;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;
import net.sf.hale.util.SimpleJSONParser;

public class Race {
	private final String id;
	private final String name;
	
	private final String icon;
	
	private final int movementCost;
	private final WeaponTemplate defaultWeapon;
	private final boolean playerSelectable;
	private final List<RacialType> racialTypes;
	private final String descriptionFile;
	
	private final String maleForegroundIcon;
	private final String maleBackgroundIcon;
	private final String maleEarsIcon;
	
	private final String femaleForegroundIcon;
	private final String femaleBackgroundIcon;
	private final String femaleEarsIcon;
	
	private final String maleClothesIcon;
	private final String femaleClothesIcon;
	
	private final Map<Integer, List<String>> abilitySelectionLists;
	
	private final String subIconRaceString;
	private final Map<Type, Point> iconOffsets;
	
	private final List<String> abilities;
	
	private final List<String> randomMaleNames, randomFemaleNames, hairAndBeardColors, skinColors;
	private final boolean hasBeard, hasHair;
	private final String defaultBeardColor, defaultHairColor, defaultSkinColor;
	private final int defaultBeardIndex, defaultHairIndex;
	private final List<Integer> selectableBeardIndices, selectableHairIndices;
	
	private final int baseStr, baseDex, baseCon, baseInt, baseWis, baseCha;
	
	private final boolean showDetailedDescription;
	
	private final List<Slot> restrictedSlots;
	
	public Race(String id, SimpleJSONParser parser) {
		this.id = id;
        name = parser.get("name", id);
		
		String defaultWeaponID = parser.get("defaultWeapon", null);
        defaultWeapon = (WeaponTemplate)EntityManager.getItemTemplate(defaultWeaponID);
		
		parser.setWarnOnMissingKeys(false);

        movementCost = parser.get("baseMovementCost", 1000);
        descriptionFile = parser.get("descriptionFile", "descriptions/races/" + name + ResourceType.HTML.getExtension());
        icon = parser.get("icon", null);
        playerSelectable = parser.get("playerSelectable", false);
        showDetailedDescription = parser.get("showDetailedDescription", true);
		
		if (parser.containsKey("baseAttributes")) {
			SimpleJSONArray attrArray = parser.getArray("baseAttributes");
			
			Iterator<SimpleJSONArrayEntry> iter = attrArray.iterator();

            baseStr = iter.next().getInt(10);
            baseDex = iter.next().getInt(10);
            baseCon = iter.next().getInt(10);
            baseInt = iter.next().getInt(10);
            baseWis = iter.next().getInt(10);
            baseCha = iter.next().getInt(10);
		} else {
            baseStr = 10;
            baseDex = 10;
            baseCon = 10;
            baseInt = 10;
            baseWis = 10;
            baseCha = 10;
		}
		
		List<Slot> restrictedSlots = new ArrayList<>();
		if (parser.containsKey("restrictedSlots")) {
			SimpleJSONArray array = parser.getArray("restrictedSlots");
			for (SimpleJSONArrayEntry entry : array) {
				String slotID = entry.getString();
				restrictedSlots.add(Slot.valueOf(slotID));
			}
		}
		this.restrictedSlots = Collections.unmodifiableList(restrictedSlots);
		
		abilities = new ArrayList<>();
		if (parser.containsKey("abilities")) {
			SimpleJSONArray array = parser.getArray("abilities");
			for (SimpleJSONArrayEntry entry : array) {
				abilities.add(entry.getString());
			}
		}
		((ArrayList<String>)abilities).trimToSize();
		
		racialTypes = new ArrayList<>();
		if (parser.containsKey("racialTypes")) {
			SimpleJSONArray array = parser.getArray("racialTypes");
			for (SimpleJSONArrayEntry entry : array) {
				racialTypes.add(Game.ruleset.getRacialType(entry.getString()));
			}
		}
		((ArrayList<RacialType>)racialTypes).trimToSize();

        hasBeard = parser.get("hasBeard", true);
        hasHair = parser.get("hasHair", true);
        defaultBeardIndex = parser.get("defaultBeardIndex", 1);
        defaultHairIndex = parser.get("defaultHairIndex", 1);
        defaultBeardColor = parser.get("defaultBeardColor", null);
        defaultHairColor = parser.get("defaultHairColor", null);
        defaultSkinColor = parser.get("defaultSkinColor", null);
		
		List<Integer> selectableHairIndices = new ArrayList<>();
		if (parser.containsKey("selectableHairIndices")) {
			SimpleJSONArray array = parser.getArray("selectableHairIndices");
			for (SimpleJSONArrayEntry entry : array) {
				selectableHairIndices.add(entry.getInt(1));
			}
		}
		this.selectableHairIndices = Collections.unmodifiableList(selectableHairIndices);
		
		List<Integer> selectableBeardIndices = new ArrayList<>();
		if (parser.containsKey("selectableBeardIndices")) {
			SimpleJSONArray array = parser.getArray("selectableBeardIndices");
			for (SimpleJSONArrayEntry entry : array) {
				selectableBeardIndices.add(entry.getInt(1));
			}
		}
		this.selectableBeardIndices = Collections.unmodifiableList(selectableBeardIndices);
		
		
		List<String> hairAndBeardColors = new ArrayList<>();
		if (parser.containsKey("hairAndBeardColors")) {
			SimpleJSONArray array = parser.getArray("hairAndBeardColors");
			for (SimpleJSONArrayEntry entry : array) {
				hairAndBeardColors.add(entry.getString());
			}
		}
		this.hairAndBeardColors = Collections.unmodifiableList(hairAndBeardColors);
		
		List<String> skinColors = new ArrayList<>();
		if (parser.containsKey("skinColors")) {
			SimpleJSONArray array = parser.getArray("skinColors");
			for (SimpleJSONArrayEntry entry : array) {
				skinColors.add(entry.getString());
			}
		}
		this.skinColors = Collections.unmodifiableList(skinColors);

        subIconRaceString = parser.get("subIconRaceString", null);
		if (parser.containsKey("icons")) {
			SimpleJSONObject obj = parser.getObject("icons");

            maleBackgroundIcon = obj.get("maleBackground", null);
            maleForegroundIcon = obj.get("maleForeground", null);
            maleEarsIcon = obj.get("maleEars", null);
            femaleBackgroundIcon = obj.get("femaleBackground", null);
            femaleForegroundIcon = obj.get("femaleForeground", null);
            femaleEarsIcon = obj.get("femaleEars", null);
            maleClothesIcon = obj.get("maleClothes", null);
            femaleClothesIcon = obj.get("femaleClothes", null);
		} else {
            maleBackgroundIcon = null;
            maleForegroundIcon = null;
            maleEarsIcon = null;
            femaleBackgroundIcon = null;
            femaleForegroundIcon = null;
            femaleEarsIcon = null;
            maleClothesIcon = null;
            femaleClothesIcon = null;
		}
		
		iconOffsets = new HashMap<>();
		if (parser.containsKey("iconOffsets")) {
			SimpleJSONObject obj = parser.getObject("iconOffsets");
			
			for (String key : obj.keySet()) {
				Type type = Type.valueOf(key);
				
				SimpleJSONArray array = obj.getArray(key);
				Iterator<SimpleJSONArrayEntry> iter = array.iterator();
				
				int x = iter.next().getInt(0);
				int y = iter.next().getInt(0);
				
				iconOffsets.put(type, new Point(x, y));
			}
		}
		
		abilitySelectionLists = new HashMap<>();
		if (parser.containsKey("abilitySelectionsFromList")) {
			SimpleJSONObject obj = parser.getObject("abilitySelectionsFromList");
			
			for (String listID : obj.keySet()) {
				SimpleJSONArray array = obj.getArray(listID);
				
				for (SimpleJSONArrayEntry entry : array) {
					int level = entry.getInt(0);
					
					addAbilitySelectionListAtLevel(listID, level);
				}
			}
		}
		
		randomMaleNames = new ArrayList<>();
		if (parser.containsKey("randomMaleNames")) {
			SimpleJSONArray array = parser.getArray("randomMaleNames");
			for (SimpleJSONArrayEntry entry : array) {
				randomMaleNames.add(entry.getString());
			}
		}
		((ArrayList<String>)randomMaleNames).trimToSize();
		
		randomFemaleNames = new ArrayList<>();
		if (parser.containsKey("randomFemaleNames")) {
			SimpleJSONArray array = parser.getArray("randomFemaleNames");
			for (SimpleJSONArrayEntry entry : array) {
				randomFemaleNames.add(entry.getString());
			}
		}
		((ArrayList<String>)randomFemaleNames).trimToSize();
		
		parser.warnOnUnusedKeys();
	}
	
	private void addAbilitySelectionListAtLevel(String listID, int level) {
		List<String> listsAtLevel = abilitySelectionLists.get(level);
		if (listsAtLevel == null) {
			listsAtLevel = new ArrayList<>(1);
			abilitySelectionLists.put(level, listsAtLevel);
		}
		
		listsAtLevel.add(listID);
	}
	
	/**
	 * returns true if the slot is restricted - this race cannot equip items in the specified slot,
	 * false otherwise
	 * @param slot
	 * @return whether the slot is restricted
	 */
	
	public boolean isSlotRestricted(Slot slot) {
		return restrictedSlots.contains(slot);
	}
	
	/**
	 * returns true if the slot is restricted - this race cannot equip items in the specified slot,
	 * false otherwise
	 * @param slot
	 * @return whether the slot is restricted
	 */
	
	public boolean isSlotRestricted(String slot) {
		return isSlotRestricted(Slot.valueOf(slot));
	}
	
	/**
	 * Returns the string that should be used for this race when selecting sub icons from the
	 * set of available sub icons.
	 * @return the string used for this race when selecting sub icons
	 */
	
	public String getSubIconRaceString() { return subIconRaceString; }
	
	/**
	 * Returns true if members of this race should draw this sub icon type, false
	 * otherwise.  Races will only draw sub icon types that have an explicitly
	 * definied icon offset.  Note that this method is only applicable for sub icons
	 * associated with inventory items, base racial sub icons can always be drawn.
	 * @param type
	 * @return whether members of this race should draw sub icons of this type
	 */
	
	public boolean drawsSubIconType(Type type) {
		return iconOffsets.containsKey(type);
	}
	
	public Point getIconOffset(Type type) {
		if (!iconOffsets.containsKey(type)) {
			iconOffsets.put(type, new Point(0, 0));
		}
		
		return new Point(iconOffsets.get(type));
	}
	
	private String getRandomFromList(List<String> names) {
		switch (names.size()) {
		case 0:
			return "";
		case 1:
			return names.get(0);
		default:
			return names.get(Game.dice.rand(0, names.size() - 1));
		}
	}
	
	public int getDefaultBeardIndex() { return defaultBeardIndex; }
	public int getDefaultHairIndex() { return defaultHairIndex; }
	public String getDefaultHairColor() { return defaultHairColor; }
	public String getDefaultBeardColor() { return defaultBeardColor; }
	public String getDefaultSkinColor() { return defaultSkinColor; }
	
	/**
	 * returns the list of indices (sub-icon sprite indices) of hair icons that are valid for this
	 * race as a player selection
	 * @return the list of valid hair indices
	 */
	
	public List<Integer> getSelectableHairIndices() { return selectableHairIndices; }
	
	/**
	 * returns the list of indices (sub-icon sprite indices) of beard icons that are valid for this
	 * race as a player selection
	 * @return the list of valid beard indices
	 */
	
	public List<Integer> getSelectableBeardIndices() { return selectableBeardIndices; }
	
	/**
	 * returns the list of standard hair and beard colors for this race
	 * @return the list of standard hair and beard colors for this race
	 */
	
	public List<String> getHairAndBeardColors() { return hairAndBeardColors; }
	
	/**
	 * returns the list of standard skin colors for this race
	 * @return the list of standard skin colors for this race
	 */
	
	public List<String> getSkinColors() { return skinColors; }
	
	/**
	 * returns whether player characters of this race can have a beard
	 * @return whether player characters of this race can have a beard
	 */
	
	public boolean hasBeard() { return hasBeard; }
	
	/**
	 * Returns whether player characters can select a hair icon on character
	 * creation
	 * @return whether player characters can select a hairstyle
	 */
	
	public boolean hasHair() { return hasHair; }
	
	public String getRandomMaleName() {
		return getRandomFromList(randomMaleNames);
	}
	
	public String getRandomFemaleName() {
		return getRandomFromList(randomFemaleNames);
	}
	
	public boolean showDetailedDescription() { return showDetailedDescription; }
	
	public String getMaleClothesIcon() { return maleClothesIcon; }
	public String getFemaleClothesIcon() { return femaleClothesIcon; }
	
	public String getFemaleEarsIcon() { return femaleEarsIcon; }
	public String getFemaleForegroundIcon() { return femaleForegroundIcon; }
	public String getFemaleBackgroundIcon() { return femaleBackgroundIcon; }
	public String getMaleEarsIcon() { return maleEarsIcon; }
	public String getMaleForegroundIcon() { return maleForegroundIcon; }
	public String getMaleBackgroundIcon() { return maleBackgroundIcon; }
	
	public String getDescriptionFile() {
		return ResourceManager.getResourceAsString(descriptionFile);
	}
	
	public String getID() { return id; }
	public String getName() { return name; }
	public String getIcon() { return icon; }
	public int getMovementCost() { return movementCost; }
	public boolean isPlayerSelectable() { return playerSelectable; }

	public int getBaseStr() { return baseStr; }
	public int getBaseDex() { return baseDex; }
	public int getBaseCon() { return baseCon; }
	public int getBaseInt() { return baseInt; }
	public int getBaseWis() { return baseWis; }
	public int getBaseCha() { return baseCha; }
	
	public boolean hasRacialType(String racialTypeID) {
		for (RacialType type : racialTypes) {
			if (type.getName().equals(racialTypeID)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * For each AbilitySelectionList in the returned List, the Creature gaining
	 * the creature level specified pick one Ability from that
	 * AbilitySelectionList.  If their are no selections to be made, the List
	 * will be empty.
	 * 
	 * @param level the creature level that has been gained
	 * @return the List of AbilitySelectionLists to choose abilities from
	 */
	
	public List<AbilitySelectionList> getAbilitySelectionsAddedAtLevel(int level) {
		List<String> idList = abilitySelectionLists.get(level);
		
		if (idList == null) return Collections.emptyList();
		
		List<AbilitySelectionList> lists = new ArrayList<>(idList.size());
		for (String id : idList) {
			lists.add(Game.ruleset.getAbilitySelectionList(id));
		}
		
		return lists;
	}
	
	public List<RacialType> getRacialTypes() {
		return new ArrayList<>(racialTypes);
	}
	
	/**
	 * Adds all Racial abilities for this Race to the specified Creature.
	 * 
	 * @param creature the Creature to add abilities to
	 */
	
	public void addAbilitiesToCreature(Creature creature) {
		for (String abilityID : abilities) {
			Ability ability = Game.ruleset.getAbility(abilityID);
			if (ability == null) {
				Logger.appendToWarningLog("Racial ability " + abilityID + " for race " + id + " not found.");
				continue;
			}
			
			creature.abilities.addRacialAbility(ability);
		}
	}
	
	/**
	 * Returns a set containing all AbilitySelectionLists that are referenced at any
	 * level within this Race
	 * @return the set of AbilitySelectionLists
	 */
	
	public Set<AbilitySelectionList> getAllReferencedAbilitySelectionLists() {
		Set<AbilitySelectionList> lists = new LinkedHashSet<>();
		
		for (int level : abilitySelectionLists.keySet()) {
			for (String listID : abilitySelectionLists.get(level)) {
				AbilitySelectionList list = Game.ruleset.getAbilitySelectionList(listID);
				lists.add(list);
			}
		}
		
		return lists;
	}
	
	/**
	 * Returns the weapon template of this race's default (unarmed) weapon
	 * @return the default weapon template
	 */
	
	public WeaponTemplate getDefaultWeaponTemplate() { return defaultWeapon; }
}
