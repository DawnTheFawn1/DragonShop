package com.rysingdragon.dragonshop.commands;

import com.rysingdragon.dragonshop.Command;
import com.rysingdragon.dragonshop.DragonShop;
import com.rysingdragon.dragonshop.arguments.ShopArgument;
import com.rysingdragon.dragonshop.config.ShopConfig;
import com.rysingdragon.dragonshop.menus.ShopMenu;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.OrderedInventory;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Arrays;

@Command(value = "shop", permissionLevel = PermissionDescription.ROLE_USER)
public class ShopCommand extends BaseCommand {

    @Override
    public CommandElement getArgs() {
        return GenericArguments.optional(new ShopArgument(Text.of("shop")));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of("You must be a player to use this command"));
        }
        Player player = (Player) src;

        ShopMenu shop;
        if (!args.getOne("shop").isPresent()) {
            shop = DragonShop.MAIN_MENU;
        } else {
            shop = (ShopMenu) args.getOne("shop").get();
        }

        shop.getInventory().clear();
        OrderedInventory inventory = shop.getInventory().query(OrderedInventory.class);
        for (ShopConfig.Reward reward : shop.getConfig().rewards.values()) {
            if (inventory.getSlot(SlotIndex.of(reward.inventoryIndex - 1)).isPresent()) {
                ItemStack guiItem = reward.guiItem.copy();
                if (reward.displayName != null) {
                    Text displayName = TextSerializers.FORMATTING_CODE.deserialize(reward.displayName);
                    guiItem.offer(Keys.DISPLAY_NAME, displayName);
                }
                guiItem.offer(Keys.ITEM_LORE, Arrays.asList(Text.of("cost: " + reward.cost)));
                inventory.getSlot(SlotIndex.of(reward.inventoryIndex - 1)).get().set(guiItem);
            }
        }

        player.openInventory(shop.getInventory(), Cause.of(
                NamedCause.owner(DragonShop.getInstance()),
                NamedCause.source(player)
        ));
        return CommandResult.success();
    }
}
