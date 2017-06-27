package com.rysingdragon.dragonshop;

import com.google.common.reflect.TypeToken;
import com.rysingdragon.dragonshop.config.ShopConfig;
import com.rysingdragon.dragonshop.menus.ShopMenu;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShopUtils {

    public static void loadShops() throws IOException {
        Path shopDir = DragonShop.getInstance().getConfigDir().resolve("shops");
        Files.walk(shopDir).filter(child -> !child.equals(shopDir)).forEach(child -> {
            try {
                String shopName = child.getFileName().toString().replace(".json", "");
                ShopConfig config = getShopConfig(shopName);
                ShopMenu menu = new ShopMenu(config.inventorySize, TextSerializers.FORMATTING_CODE.deserialize(config.displayName), config);
                System.out.println("adding shop: " + shopName);
                DragonShop.getShops().put(shopName, menu);
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
            }
        });
    }

    public static ShopConfig getShopConfig(String name) throws IOException, ObjectMappingException {
        Path path = DragonShop.getInstance().getConfigDir().resolve("shops/" + name + ".json");

        TypeSerializerCollection serializers = ConfigurationOptions.defaults().getSerializers().newChild();
        serializers.registerType(TypeToken.of(ItemStack.class), new ItemStackSerializer());
        ConfigurationOptions options = ConfigurationOptions.defaults();

        GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setIndent(4)
                .setDefaultOptions(options)
                .setPath(path)
                .build();

        ShopConfig config;
        if (!Files.exists(path)) {
            config = new ShopConfig(loader, loader.createEmptyNode());
            config.save();
        } else {
            ConfigurationNode root = loader.load(options);
            config = root.getValue(ShopConfig.type);
            config.setLoader(loader);
            config.setRootNode(root);
        }
        return config;
    }

    public static String formatName(Text name) {
        return formatName(name.toPlain());
    }

    public static String formatName(String name) {
        return name.toLowerCase().replace(" ", "_");
    }

    public static void openDisplayItemInventory(Player player, ShopConfig config, ShopConfig.Reward reward) {
        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.AQUA, "Display-Item")))
                .of(InventoryArchetypes.DISPENSER)
                .listener(InteractInventoryEvent.Close.class, event -> {

                    if (!event.getCause().first(Player.class).isPresent())
                        return;

                    Inventory queryResult = event.getTargetInventory().query(GridInventory.class);

                    for (Inventory grid : queryResult) {
                        if (grid.getArchetype() != InventoryArchetypes.DISPENSER) {
                            continue;
                        }

                        if (grid.size() == 1) {
                            reward.guiItem = grid.peek().get();
                            config.save();
                            player.sendMessage(Text.of("Successfully saved display item"));
                        } else {
                            player.sendMessage(Text.of("Unable to save display item, you must only put 1 item in the inventory before closing"));
                        }
                    }

                })
                .build(DragonShop.getInstance());
        player.openInventory(inventory, Cause.of(NamedCause.owner(DragonShop.getInstance())));
    }

}
