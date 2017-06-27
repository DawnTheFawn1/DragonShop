package com.rysingdragon.dragonshop.commands;

import com.rysingdragon.conversation.Conversation;
import com.rysingdragon.conversation.ConversationChannel;
import com.rysingdragon.conversation.Question;
import com.rysingdragon.dragonshop.Command;
import com.rysingdragon.dragonshop.DragonShop;
import com.rysingdragon.dragonshop.ShopUtils;
import com.rysingdragon.dragonshop.config.ShopConfig;
import com.rysingdragon.dragonshop.menus.ShopMenu;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;

@Command(value = {"create"}, permissionLevel = PermissionDescription.ROLE_ADMIN)
public class CreateShopCommand extends BaseCommand {

    @Override
    public CommandElement getArgs() {
        return GenericArguments.seq(
                GenericArguments.string(Text.of("shop_name"))
                //GenericArguments.integer(Text.of("inventory_size"))
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String rawName = (String) args.getOne("shop_name").get();
        Text shopName = TextSerializers.FORMATTING_CODE.deserialize(rawName);
        //int rows = (int) args.getOne("inventory_size").get();


        if (src instanceof Player) {
            Player player = (Player) src;
            ConversationChannel channel = new ConversationChannel();
            channel.addMember(player);
            player.setMessageChannel(channel);

            Question firstQuestion = Question.builder().id("inventory_rows").prompt("how many rows do you want the inventory to have").handler((reply, question) -> {

                try {
                    Integer.parseInt(reply);
                    question.setNextQuestion(null);
                } catch (NumberFormatException e) {
                    src.sendMessage(Text.of(TextColors.RED, "That is not a valid number"));
                    question.setNextQuestion(question);
                }

            }).build();


            Conversation conversation = channel.startConversation(firstQuestion);

            conversation.stopHandler(() -> {
                try {
                    int rows = Integer.parseInt(conversation.getQuestion("inventory_rows").get().getReply());
                    String name = ShopUtils.formatName(shopName);
                    ShopConfig config = ShopUtils.getShopConfig(name);
                    config.displayName = rawName;
                    config.save();

                    ShopMenu shop = new ShopMenu(rows, shopName, config);
                    DragonShop.getShops().put(name, shop);
                    src.sendMessage(Text.of("you have successfully created this shop"));
                } catch (IOException | ObjectMappingException e) {
                    e.printStackTrace();
                }
            });

        }


        /**try {
            String name = ShopUtils.formatName(shopName);
            ShopConfig config = ShopUtils.getShopConfig(name);
            ShopMenu shop = new ShopMenu(rows, shopName, config);
            DragonShop.getShops().put(name, shop);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }**/
        return CommandResult.success();
    }
}
