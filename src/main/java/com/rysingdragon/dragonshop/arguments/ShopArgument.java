package com.rysingdragon.dragonshop.arguments;

import com.google.common.collect.Lists;
import com.rysingdragon.dragonshop.DragonShop;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

import javax.annotation.Nullable;

public class ShopArgument extends CommandElement {

    public ShopArgument(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        if (args.hasNext()) {
            String shop = args.next();
            if (DragonShop.getShops().containsKey(shop)) {
                return DragonShop.getShops().get(shop);
            }
        }
        throw args.createError(Text.of(TextColors.RED, "Shop not found!"));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Lists.newArrayList(DragonShop.getShops().keySet());
    }
}
