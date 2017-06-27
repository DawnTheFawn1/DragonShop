package com.rysingdragon.dragonshop.menus;

import com.rysingdragon.dragonshop.DragonShop;
import com.rysingdragon.dragonshop.ShopUtils;
import com.rysingdragon.dragonshop.config.ShopConfig;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.function.Consumer;

public abstract class BaseMenu {

    private Inventory inventory;
    private ShopConfig config;
    private String name;

    public <E extends InteractInventoryEvent> BaseMenu(int height, Text title) {
        this.name = ShopUtils.formatName(title);
        Inventory.Builder builder = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(title))
                .property(InventoryDimension.PROPERTY_NAM, InventoryDimension.of(9, height));

        Map<Class<E>, Consumer<E>> map = getListeners().getClass().cast(getListeners());

        map.entrySet().forEach(entry -> builder.listener(entry.getKey(), entry.getValue()));
        this.inventory = builder.build(DragonShop.getInstance());
    }

    public String getName() {
        return this.name;
    }

    public abstract Map<Class<? extends InteractInventoryEvent>, Consumer<? extends InteractInventoryEvent>> getListeners();

    public Inventory getInventory() {
        return this.inventory;
    }

    public void setConfig(ShopConfig config) {
        this.config = config;
    }

    public ShopConfig getConfig() {
        return this.config;
    }
}