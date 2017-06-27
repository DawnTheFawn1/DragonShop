package com.rysingdragon.dragonshop.commands;

import com.rysingdragon.dragonshop.Command;
import com.rysingdragon.dragonshop.arguments.RewardArgument;
import com.rysingdragon.dragonshop.arguments.ShopArgument;
import com.rysingdragon.dragonshop.config.ShopConfig;
import com.rysingdragon.dragonshop.menus.ShopMenu;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

@Command("deletereward")
public class DeleteRewardCommand extends BaseCommand {

    @Override
    public CommandElement getArgs() {
        return GenericArguments.seq(
                new ShopArgument(Text.of("shop")),
                new RewardArgument(Text.of("reward"))
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        ShopMenu shop = (ShopMenu) args.getOne("shop").get();
        ShopConfig.Reward reward = (ShopConfig.Reward) args.getOne("reward").get();
        ShopConfig config = shop.getConfig();

        config.rewards.remove(reward.name);
        config.save();
        src.sendMessage(Text.of("successfully deleted this reward"));

        return CommandResult.success();
    }
}
