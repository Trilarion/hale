/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2015 Jared Stephen
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

package net.sf.hale.quickbar;

import net.sf.hale.ability.Ability;
import net.sf.hale.icon.Icon;
import net.sf.hale.icon.IconFactory;
import net.sf.hale.util.SimpleJSONObject;

import java.util.*;

/**
 * a set of activateable abilities listed together under a common grouping in the quickbar
 *
 * @author Jared
 */
public class QuickbarGroup {
    private final String name;
    private final Icon icon;
    private final String tooltip;
    private final List<Ability> abilities;

    /**
     * Creates a new QuickbarGroup by parsing the JSON
     *
     * @param data
     * @param abilitiesByGroup
     */
    public QuickbarGroup(SimpleJSONObject data, Map<String, List<Ability>> abilitiesByGroup) {
        name = data.get("name", null);
        tooltip = data.get("tooltip", null);

        if (data.containsKey("icon")) {
            icon = IconFactory.createIcon(data.getObject("icon"));
        } else {
            icon = IconFactory.emptyIcon;
        }

        List<Ability> abilities = new ArrayList<>();
        for (Ability ability : abilitiesByGroup.get(name)) {
            abilities.add(ability);
        }

        Collections.sort(abilities, new Comparator<Ability>() {
            @Override
            public int compare(Ability o1, Ability o2) {
                return o1.getName().compareTo(o2.getName());
            }

        });

        ((ArrayList<Ability>) abilities).trimToSize();
        this.abilities = Collections.unmodifiableList(abilities);
    }

    /**
     * Returns the mouseover text for the button associated with this group
     *
     * @return the mouseover text
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Returns the icon for this group
     *
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Returns the name / ID of this group
     *
     * @return the name / ID
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of abilities in this group.  Note that this list may not be
     * modified
     *
     * @return the list of abilities in this group
     */
    public List<Ability> getAbilities() {
        return abilities;
    }
}
