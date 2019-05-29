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

import net.sf.hale.Game;
import net.sf.hale.entity.*;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.SimpleJSONObject;
import net.sf.hale.util.SimpleJSONParser;

public class Merchant implements Saveable {
    private final LootList baseItems;
    private final String id;
    private String name;
    private int buyValuePercentage;
    private int sellValuePercentage;
    private boolean usesSpeechSkill;
    private boolean confirmOnExit;
    private int currentBuyPercentage;
    private int currentSellPercentage;
    private int respawnHours;
    private int lastRespawnRounds;
    private ItemList currentItems;

    public Merchant(String id) {
        this.id = id;

        SimpleJSONParser parser = new SimpleJSONParser("merchants/" + id, ResourceType.JSON);

        name = parser.get("name", null);
        sellValuePercentage = parser.get("sellValuePercentage", 0);
        buyValuePercentage = parser.get("buyValuePercentage", 0);
        usesSpeechSkill = parser.get("usesSpeechSkill", false);
        confirmOnExit = parser.get("confirmOnExit", false);
        respawnHours = parser.get("respawnHours", 0);

        baseItems = new LootList(parser.getArray("items"));

        parser.warnOnUnusedKeys();

        currentBuyPercentage = buyValuePercentage;
        currentSellPercentage = sellValuePercentage;
    }

    @Override
    public Object save() {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("id", id);
        data.put("lastRespawnRound", lastRespawnRounds);

        data.put("currentItems", currentItems.save());

        return data;
    }

    public void load(SimpleJSONObject data) {
        lastRespawnRounds = data.get("lastRespawnRound", 0);

        currentItems = new ItemList();
        currentItems.load(data.getArray("currentItems"));
    }

    public ItemList getCurrentItems() {
        return currentItems;
    }

    public int getRespawnHours() {
        return respawnHours;
    }

    public LootList getBaseItems() {
        return baseItems;
    }

    public boolean usesSpeechSkill() {
        return usesSpeechSkill;
    }

    public boolean confirmOnExit() {
        return confirmOnExit;
    }

    private boolean checkRespawn() {
        if (currentItems == null) return true;

        if (respawnHours == 0) return false;

        int currentRound = Game.curCampaign.getDate().getTotalRoundsElapsed();
        int elapsedRounds = currentRound - lastRespawnRounds;

        return elapsedRounds >= respawnHours * Game.curCampaign.getDate().ROUNDS_PER_HOUR;
    }

    /**
     * Gets the current list of items for this merchant, respawning if the respawn time has passed
     *
     * @return the current list of items for this merchant
     */
    public ItemList updateCurrentItems() {
        if (checkRespawn()) {
            lastRespawnRounds = Game.curCampaign.getDate().getTotalRoundsElapsed();
            currentItems = baseItems.generate();
        }

        return currentItems;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setPartySpeech(int partySpeech) {
        if (usesSpeechSkill) {
            double gapExponent = -1.0 * (double) partySpeech / (double) Game.ruleset.getValue("BuySellGapSpeechExpFactor");
            double gapPercentage = Math.exp(gapExponent);

            double base = (double) (sellValuePercentage - buyValuePercentage) / 2.0;

            int modifier = (int) Math.round(base * (1.0 - gapPercentage));


            currentBuyPercentage = buyValuePercentage + modifier;
            currentSellPercentage = sellValuePercentage - modifier;
        }
    }

    public int getBuyValuePercentage() {
        return buyValuePercentage;
    }

    public void setBuyValuePercentage(int buyValuePercentage) {
        this.buyValuePercentage = buyValuePercentage;
    }

    public int getSellValuePercentage() {
        return sellValuePercentage;
    }

    public void setSellValuePercentage(int sellValuePercentage) {
        this.sellValuePercentage = sellValuePercentage;
    }

    public int getCurrentBuyPercentage() {
        return currentBuyPercentage;
    }

    public int getCurrentSellPercentage() {
        return currentSellPercentage;
    }

    public void sellItem(Item item, Creature creature) {
        sellItem(item, creature, 1);
    }

    public void buyItem(Item item, Creature creature) {
        buyItem(item, creature, 1);
    }

    public void sellItem(Item item, Creature creature, int quantity) {
        int cost = Currency.getPlayerBuyCost(item, quantity, currentSellPercentage).getValue();

        if (Game.curCampaign.getPartyCurrency().getValue() < cost) return;

        Item soldItem = EntityManager.getItem(item.getTemplate().getID(), item.getQuality());

        Game.curCampaign.getPartyCurrency().addValue(-cost);
        creature.inventory.getUnequippedItems().add(soldItem, quantity);

        if (currentItems.getQuantity(item) != Integer.MAX_VALUE) {
            currentItems.remove(item, quantity);
        }

        Game.mainViewer.updateInterface();
    }

    public void buyItem(Item item, Creature creature, int quantity) {
        if (item.getTemplate().isQuest()) return;

        int cost = Currency.getPlayerSellCost(item, quantity, currentBuyPercentage).getValue();

        Game.curCampaign.getPartyCurrency().addValue(cost);
        creature.inventory.getUnequippedItems().remove(item, quantity);

        if (currentItems.getQuantity(item) != Integer.MAX_VALUE) {
            currentItems.add(item, quantity);
        }

        Game.mainViewer.updateInterface();
    }
}
