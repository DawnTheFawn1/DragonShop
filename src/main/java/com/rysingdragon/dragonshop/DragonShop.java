package com.rysingdragon.dragonshop;

import com.google.inject.Inject;
import com.rysingdragon.conversation.ConversationListener;
import com.rysingdragon.dragonshop.commands.ShopAdminCommand;
import com.rysingdragon.dragonshop.commands.ShopCommand;
import com.rysingdragon.dragonshop.config.ShopConfig;
import com.rysingdragon.dragonshop.menus.ShopMenu;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Plugin(
        id = DragonShop.ID,
        name = DragonShop.NAME,
        version = DragonShop.VERSION,
        description = DragonShop.DESCRIPTION,
        authors = {"RysingDragon"}
)
public class DragonShop {

    public static final String ID = "dragonshop";
    public static final String NAME = "DragonShop";
    public static final String VERSION = "1.0.0";
    public static final String DESCRIPTION = "highly customizable shop plugin";

    @Inject
    private Logger logger;

    private static DragonShop instance;
    private static EconomyService economyService;
    private static Map<String, ShopMenu> shops;
    public static ShopMenu MAIN_MENU;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        instance = this;

        try {
            if (!Files.exists(configDir.resolve("shops"))) {
                System.out.println("creating shop directories");
                Files.createDirectories(configDir.resolve("shops"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        shops = new HashMap<>();
        try {
            ShopUtils.loadShops();
            if (!shops.containsKey("main_menu")) {
                ShopConfig config = ShopUtils.getShopConfig("main_menu");
                ShopMenu mainMenu = new ShopMenu(4, Text.of("Main Menu"), config);

                shops.put("main_menu", mainMenu);
            }
            MAIN_MENU = shops.get("main_menu");
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        //register commands
        logger.info("registering commands");
        CommandManager.registerCommand(new ShopCommand());
        CommandManager.registerCommand(new ShopAdminCommand());

        Sponge.getEventManager().registerListeners(this, new ConversationListener());
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
    }

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        if (economyService == null) {
            logger.warn("Economy plugin not present, this plugin will not function properly!");
        }
    }

    @Listener
    public void onServiceChange(ChangeServiceProviderEvent event) {
        if (event.getService().equals(EconomyService.class)) {
            economyService = (EconomyService) event.getNewProviderRegistration().getProvider();
        }
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        try {
            shops.clear();
            ShopUtils.loadShops();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Logger getLogger() {
        return this.logger;
    }

    public static DragonShop getInstance() {
        return instance;
    }

    public static Map<String, ShopMenu> getShops() {
        return shops;
    }

    public Path getConfigDir() {
        return this.configDir;
    }

    public static EconomyService getEconomy() {
        return economyService;
    }
}