package com.rysingdragon.dragonshop.commands;

import com.rysingdragon.dragonshop.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;
import java.util.List;

@Command("shopadmin")
public class ShopAdminCommand extends BaseCommand {

    @Override
    public List<Class<? extends BaseCommand>> getChildren() {
        return Arrays.asList(CreateShopCommand.class, AddRewardCommand.class, DeleteRewardCommand.class, EditRewardCommand.class);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        Text.Builder formattedText = Text.builder("/");
        formattedText.append(Text.of(this.getClass().getAnnotation(Command.class).value()[0]));
        formattedText.append(Text.of(" "));

        if (!getChildren().isEmpty()) {
            formattedText.append(Text.of("["));
            for (int i = 0; i < getChildren().size(); i++) {
                Class<? extends BaseCommand> child = getChildren().get(i);
                Command annotation = child.getAnnotation(Command.class);
                if (i != 0) {
                    formattedText.append(Text.of(" | "));
                }
                formattedText.append(Text.of(annotation.value()[0]));
            }
            formattedText.append(Text.of("]"));
        }

        throw new CommandException(Text.of(TextColors.RED, "invalid usage: ", Text.NEW_LINE,
                formattedText.build()));
    }
}
