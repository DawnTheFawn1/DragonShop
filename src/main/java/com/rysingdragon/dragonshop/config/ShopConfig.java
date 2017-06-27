package com.rysingdragon.dragonshop.config;

import com.google.common.reflect.TypeToken;
import com.rysingdragon.dragonshop.enums.RewardType;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class ShopConfig {

    private GsonConfigurationLoader loader;
    private ConfigurationNode rootNode;
    public static final TypeToken<ShopConfig> type = TypeToken.of(ShopConfig.class);

    //In order to satisfy configurate for loading config
    public ShopConfig() {}

    public ShopConfig(GsonConfigurationLoader loader, ConfigurationNode rootNode) {
        this.loader = loader;
        this.rootNode = rootNode;
    }

    public void setLoader(GsonConfigurationLoader loader) {
        this.loader = loader;
    }

    public ConfigurationNode getRootNode() {
        return this.rootNode;
    }

    public void setRootNode(ConfigurationNode rootNode) {
        this.rootNode = rootNode;
    }

    public void save() {
        try {
            this.rootNode.setValue(ShopConfig.type, this);
            loader.save(this.rootNode);
        } catch (ObjectMappingException | IOException e) {
            e.printStackTrace();
        }
    }

    @Setting(value = "display-name")
    public String displayName = TextSerializers.FORMATTING_CODE.serialize(Text.of("New Menu"));

    @Setting("inventory-size")
    public int inventorySize = 4;

    @Setting
    public Map<String, Reward> rewards = new HashMap<>();

    @ConfigSerializable
    public static class Reward {

        @Setting
        public List<String> commands;

        @Setting
        public String shop;

        @Setting
        public List<ItemStack> items;

        @Setting(value = "reward-name")
        public String name;

        @Setting(value = "display-name")
        public String displayName;

        @Setting(value = "reward-type")
        public RewardType rewardType;

        @Setting(value = "gui-item")
        public ItemStack guiItem = ItemStack.builder().itemType(ItemTypes.PAPER).quantity(1).build();

        @Setting(value = "inventory-index")
        public int inventoryIndex = 1;

        @Setting
        public double cost = 5.00;
    }
}
