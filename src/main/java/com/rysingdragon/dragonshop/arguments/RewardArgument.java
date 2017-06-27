package com.rysingdragon.dragonshop.arguments;

import com.rysingdragon.dragonshop.DragonShop;
import com.rysingdragon.dragonshop.menus.ShopMenu;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class RewardArgument extends CommandElement {

    public RewardArgument(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        if (args.hasNext()) {
            String rewardName = args.next();
            int state = (int) args.getState();

            ShopMenu shopMenu = DragonShop.getShops().get(args.getAll().get(state - 1));
            if (shopMenu.getConfig().rewards.containsKey(rewardName)) {
                return shopMenu.getConfig().rewards.get(rewardName);
            }
        }
        throw args.createError(Text.of(TextColors.RED, "Reward not found!"));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        ShopMenu menu = (ShopMenu) context.getOne("shop").get();
        return menu.getConfig().getRootNode().getNode("rewards").getChildrenMap().keySet().stream().filter(key -> key instanceof String)
                .map(key -> (String) key).collect(Collectors.toList());
    }
}





















