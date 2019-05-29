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

package net.sf.hale.view;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.ScrollPane.Fixed;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import net.sf.hale.Game;
import net.sf.hale.bonus.Bonus.Type;
import net.sf.hale.entity.*;
import net.sf.hale.entity.Inventory.Slot;
import net.sf.hale.entity.ItemList.Entry;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.Recipe;
import net.sf.hale.widgets.IconViewer;
import net.sf.hale.widgets.TextAreaNoInput;

/**
 * The popup used to select an item when crafting an item where the result is one
 * of the ingredients (i.e. enchantments)
 *
 * @author Jared
 */
public class SelectCraftItemPopup extends PopupWindow {
    private ItemViewer selectedItemViewer;

    private Recipe recipe;
    private Content content;

    private ScrollPane scrollPane;
    private DialogLayout scrollPaneContent;

    private Label title, recipeName;
    private Button cancel, accept;

    /**
     * Creates a new PopupWindow with the specified parent
     *
     * @param parent the parent widget
     */
    public SelectCraftItemPopup(Widget parent, Recipe recipe) {
        super(parent);

        this.recipe = recipe;

        setCloseOnEscape(false);
        setCloseOnClickedOutside(false);

        content = new Content();
        add(content);

        String skill = recipe.getSkill().getPresentTenseVerb();
        String titleText = "Select an Item to " + skill;

        title = new Label(titleText);
        title.setTheme("titlelabel");
        content.add(title);

        recipeName = new Label("Recipe: " + recipe.getName());
        recipeName.setTheme("recipelabel");
        content.add(recipeName);

        cancel = new Button();
        cancel.addCallback(new Runnable() {
            @Override
            public void run() {
                closePopup();
            }
        });
        cancel.setTheme("cancelbutton");
        content.add(cancel);

        accept = new Button();

        accept.addCallback(new Runnable() {
            @Override
            public void run() {
                closePopup();
                SelectCraftItemPopup.this.recipe.craft(selectedItemViewer.item,
                        selectedItemViewer.parent, selectedItemViewer.slot);
            }
        });
        accept.setTheme("acceptbutton");
        content.add(accept);

        scrollPaneContent = new DialogLayout();
        scrollPaneContent.setTheme("content");
        scrollPane = new ScrollPane(scrollPaneContent);
        scrollPane.setFixed(Fixed.HORIZONTAL);
        content.add(scrollPane);

        Group mainH = scrollPaneContent.createParallelGroup();
        Group mainV = scrollPaneContent.createSequentialGroup();

        populateItemsList(mainH, mainV);

        scrollPaneContent.setHorizontalGroup(mainH);
        scrollPaneContent.setVerticalGroup(mainV);

        setCraftEnabled();
    }

    private void setCraftEnabled() {
        accept.setEnabled(selectedItemViewer != null);
    }

    private void populateItemsList(Group mainH, Group mainV) {
        for (Creature creature : Game.curCampaign.party) {
            // check equipped items
            for (Slot slot : Slot.values()) {
                EquippableItem item = creature.inventory.getEquippedItem(slot);

                if (item == null) continue;

                if (!item.getTemplate().getEnchantments().isEmpty()) continue;

                if (recipe.getIngredientItemTypes().contains(item.getTemplate().getType())) {
                    ItemViewer viewer = new ItemViewer(item, creature, slot);

                    mainH.addWidget(viewer);
                    mainV.addWidget(viewer);
                }
            }

            // check unequipped items
            for (Entry entry : creature.inventory.getUnequippedItems()) {
                ItemTemplate template = EntityManager.getItemTemplate(entry.getID());

                if (template instanceof EquippableItemTemplate) {
                    EquippableItemTemplate itemTemplate = (EquippableItemTemplate) template;

                    if (!itemTemplate.getEnchantments().isEmpty()) continue;

                    if (recipe.getIngredientItemTypes().contains(itemTemplate.getType())) {
                        ItemViewer viewer = new ItemViewer(entry.createItem(), creature, null);

                        mainH.addWidget(viewer);
                        mainV.addWidget(viewer);
                    }
                }
            }
        }
    }

    private class Content extends Widget {
        private int gap;
        private int maxHeight;

        @Override
        protected void applyTheme(ThemeInfo themeInfo) {
            super.applyTheme(themeInfo);

            gap = themeInfo.getParameter("gap", 0);
            maxHeight = themeInfo.getParameter("maxHeight", 0);
        }

        @Override
        protected void layout() {
            int centerX = getInnerX() + getInnerWidth() / 2;

            title.setSize(title.getPreferredWidth(), title.getPreferredHeight());
            title.setPosition(centerX - title.getWidth() / 2, getInnerY());

            recipeName.setSize(recipeName.getPreferredWidth(), recipeName.getPreferredHeight());
            recipeName.setPosition(centerX - recipeName.getWidth() / 2, title.getBottom());

            accept.setSize(accept.getPreferredWidth(), accept.getPreferredHeight());
            cancel.setSize(cancel.getPreferredWidth(), cancel.getPreferredHeight());

            accept.setPosition(centerX - accept.getWidth() - gap, getInnerBottom() - accept.getHeight());
            cancel.setPosition(centerX + gap, getInnerBottom() - cancel.getHeight());

            scrollPane.setSize(getInnerWidth(), getInnerHeight() - recipeName.getHeight() -
                    title.getHeight() - accept.getHeight() - 2 * gap);
            scrollPane.setPosition(getInnerX(), recipeName.getBottom() + gap);
        }

        @Override
        public int getPreferredWidth() {
            int width = Math.max(title.getPreferredWidth(), scrollPane.getPreferredWidth());

            return width + getBorderHorizontal();
        }

        @Override
        public int getPreferredHeight() {
            int height = title.getPreferredHeight() + 2 * gap + recipeName.getPreferredHeight() +
                    scrollPane.getPreferredHeight() + accept.getPreferredHeight();

            return Math.min(maxHeight, height + getBorderVertical());
        }
    }

    private class ItemViewer extends ToggleButton implements Runnable {
        private IconViewer viewer;
        private TextArea textArea;

        private Slot slot;
        private Creature parent;
        private Item item;

        private ItemViewer(Item item, Creature parent, Slot slot) {
            this.item = item;
            this.parent = parent;
            this.slot = slot;

            addCallback(this);

            viewer = new IconViewer(item.getTemplate().getIcon());
            viewer.setEventHandlingEnabled(false);
            add(viewer);

            HTMLTextAreaModel model = new HTMLTextAreaModel();

            StringBuilder sb = new StringBuilder();
            appendItemText(sb);

            model.setHtml(sb.toString());

            textArea = new TextAreaNoInput(model);
            add(textArea);
        }

        @Override
        public void run() {
            if (selectedItemViewer != null)
                selectedItemViewer.setActive(false);

            setActive(true);
            selectedItemViewer = this;

            setCraftEnabled();
        }

        private void appendItemText(StringBuilder sb) {
            sb.append("<div style=\"font-family: medium-bold;\">");

            sb.append(item.getLongName());
            sb.append("</div>");

            sb.append("Value: <span style=\"font-family: blue;\">");
            sb.append(new Currency(item.getQualityValue()).shortString(100));
            sb.append("</span>");

            if (item instanceof Weapon) {
                Weapon weapon = (Weapon) item;

                sb.append("<div>Base Damage: ");

                float damageMult = 1.0f + (weapon.getQualityDamageBonus() + weapon.bonuses.get(Type.WeaponDamage)) / 100.0f;
                float damageMin = (float) weapon.getTemplate().getMinDamage() * damageMult;
                float damageMax = (float) weapon.getTemplate().getMaxDamage() * damageMult;

                sb.append("<span style=\"font-family: red;\">");
                sb.append(Game.numberFormat(1).format(damageMin));
                sb.append("</span>");

                sb.append(" to <span style=\"font-family: red;\">");
                sb.append(Game.numberFormat(1).format(damageMax));
                sb.append("</span>");

                sb.append("</div>");
            } else if (item instanceof Armor) {
                Armor armor = (Armor) item;

                sb.append("<div>Defense <span style=\"font-family: green;\">");
                sb.append(Game.numberFormat(1).format(armor.getQualityModifiedArmorClass()));
                sb.append("</span></div>");
            }

            sb.append("<div>");
            if (slot != null) {
                sb.append("<span style=\"font-family: blue\">Equipped</span> by ");
            } else
                sb.append("Owned by ");

            sb.append("<span style=\"font-family: green\">");
            sb.append(parent.getTemplate().getName());
            sb.append("</span></div>");
        }

        @Override
        protected void layout() {
            viewer.setSize(viewer.getPreferredWidth(), viewer.getPreferredHeight());
            viewer.setPosition(getInnerX(), getInnerY() + getInnerHeight() / 2 - viewer.getHeight() / 2);

            textArea.setPosition(viewer.getRight(), getInnerY());
            textArea.setSize(Math.max(0, getInnerWidth() - viewer.getWidth()), getInnerHeight());
        }

        @Override
        public int getPreferredHeight() {
            return Math.max(viewer.getPreferredHeight(), textArea.getPreferredInnerHeight() +
                    textArea.getBorderVertical()) + getBorderVertical();
        }
    }
}
