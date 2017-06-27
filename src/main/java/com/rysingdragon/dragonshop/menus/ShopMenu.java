package com.rysingdragon.dragonshop.menus;

import com.rysingdragon.dragonshop.DragonShop;
import com.rysingdragon.dragonshop.config.ShopConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.OrderedInventory;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ShopMenu extends BaseMenu {

    public ShopMenu(int height, Text name, ShopConfig config) {
        super(height, name);
        this.setConfig(config);
    }

    @Override
    public Map<Class<? extends InteractInventoryEvent>, Consumer<? extends InteractInventoryEvent>> getListeners() {
        Map<Class<? extends InteractInventoryEvent>, Consumer<? extends InteractInventoryEvent>> map = new HashMap<>();
        map.put(ClickInventoryEvent.Shift.class, event -> onShiftClick((ClickInventoryEvent.Shift) event));
        map.put(ClickInventoryEvent.class, event -> onClick((ClickInventoryEvent) event));
        map.put(InteractInventoryEvent.Open.class, event -> onOpen((InteractInventoryEvent.Open) event));
        return map;
    }

    public void onOpen(InteractInventoryEvent.Open event) {
        if (!event.getCause().first(Player.class).isPresent())
            return;

        Player player = event.getCause().first(Player.class).get();

        if (!player.hasPermission(DragonShop.ID + ".shop." + this.getName() + ".open")) {
            event.setCancelled(true);
            player.sendMessage(Text.of("You do not have permission to open this shop"));
        }
    }

    public void onClick(ClickInventoryEvent event) {
        if (!event.getCause().first(Player.class).isPresent() || event instanceof ClickInventoryEvent.Shift)
            return;

        Player player = event.getCause().first(Player.class).get();

        event.getTransactions().stream()
                .filter(slotTransaction -> slotTransaction.getSlot().getProperty(SlotIndex.class, "slotindex").isPresent())
                .filter(slotTransaction -> slotTransaction.getSlot().getProperty(SlotIndex.class, "slotindex").get().getValue() != null)
                .forEach(transaction -> {
                    for (ShopConfig.Reward reward : this.getConfig().rewards.values()) {
                        if (reward.inventoryIndex == transaction.getSlot().getProperty(SlotIndex.class, "slotindex").get().getValue() + 1) {
                            event.getCursorTransaction().setValid(false);
                            switch (reward.rewardType) {
                                case COMMAND:
                                    if (reward.commands != null) {
                                        for (String command : reward.commands) {
                                            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command.replace("{player}", player.getName()));
                                        }
                                    }
                                    break;
                                case ITEM:
                                    break;
                                case SHOP:
                                    ShopMenu menu = DragonShop.getShops().get(reward.shop);

                                    menu.getInventory().clear();
                                    OrderedInventory inventory = menu.getInventory().query(OrderedInventory.class);
                                    for (ShopConfig.Reward r : menu.getConfig().rewards.values()) {
                                        if (inventory.getSlot(SlotIndex.of(r.inventoryIndex - 1)).isPresent()) {
                                            ItemStack guiItem = r.guiItem.copy();
                                            if (r.displayName != null) {
                                                Text displayName = TextSerializers.FORMATTING_CODE.deserialize(r.displayName);
                                                guiItem.offer(Keys.DISPLAY_NAME, displayName);
                                            }
                                            guiItem.offer(Keys.ITEM_LORE, Arrays.asList(Text.of("cost: " + r.cost)));
                                            inventory.getSlot(SlotIndex.of(r.inventoryIndex - 1)).get().set(guiItem);
                                        }
                                    }

                                    Task.builder().delayTicks(1).execute(task -> {
                                        player.closeInventory(Cause.of(NamedCause.source(DragonShop.getInstance()), NamedCause.owner(player)));
                                        player.openInventory(menu.getInventory(), Cause.of(NamedCause.source(DragonShop.getInstance()), NamedCause.owner(player)));
                                    }).submit(DragonShop.getInstance());
                                    break;
                            }
                            DragonShop.getEconomy().getOrCreateAccount(player.getUniqueId()).ifPresent(account -> {
                                BigDecimal cost = new BigDecimal(reward.cost);
                                if (reward.cost > 0 && account.getBalance(DragonShop.getEconomy().getDefaultCurrency()).compareTo(cost) >= 0) {
                                    account.withdraw(DragonShop.getEconomy().getDefaultCurrency(), cost, Cause.of(NamedCause.source(DragonShop.getInstance())));
                                    Text rewardName = TextSerializers.FORMATTING_CODE.deserialize(reward.name);
                                    player.sendMessage(Text.of("You have bought a ", rewardName, TextColors.RESET, " for ", TextColors.GOLD,
                                            DragonShop.getEconomy().getDefaultCurrency().format(cost, 2)));
                                }
                            });
                        }
                    }
        });

        event.setCancelled(true);
    }

    public void onShiftClick(ClickInventoryEvent.Shift event) {
        if (!event.getCause().first(Player.class).isPresent())
            return;

        event.getTransactions().forEach(transaction -> {
            Slot slot = transaction.getSlot();
            if (slot.parent().equals(this.getInventory())) {
                ItemStack stack = transaction.getOriginal().createStack();
                if (event instanceof ClickInventoryEvent.Shift.Primary && (stack.getQuantity() + 1) <= stack.getMaxStackQuantity()) {
                    stack.setQuantity(stack.getQuantity() + 1);
                    transaction.setCustom(stack);
                } else if (event instanceof ClickInventoryEvent.Shift.Secondary && (stack.getQuantity() - 1) > 0) {
                    stack.setQuantity(stack.getQuantity() - 1);
                    transaction.setCustom(stack);
                } else {
                    transaction.setValid(false);
                }
            } else {
                transaction.setValid(false);
            }
        });
    }

}