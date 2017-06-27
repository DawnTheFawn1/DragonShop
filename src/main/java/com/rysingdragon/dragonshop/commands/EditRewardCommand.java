package com.rysingdragon.dragonshop.commands;

import com.rysingdragon.conversation.Conversation;
import com.rysingdragon.conversation.ConversationChannel;
import com.rysingdragon.conversation.Question;
import com.rysingdragon.dragonshop.Command;
import com.rysingdragon.dragonshop.ShopUtils;
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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Command("editreward")
public class EditRewardCommand extends BaseCommand {

    @Override
    public CommandElement getArgs() {
        return GenericArguments.seq(
                new ShopArgument(Text.of("shop")),
                new RewardArgument(Text.of("reward"))
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;

            ShopMenu shopMenu = (ShopMenu) args.getOne("shop").get();
            ShopConfig.Reward reward = (ShopConfig.Reward) args.getOne("reward").get();
            src.sendMessage(Text.of("reward name: ", reward.name));
            List<String> properties = new ArrayList<>(Arrays.asList("reward-name", "reward-type", "gui-item", "inventory-index", "cost"));

            ConversationChannel channel = new ConversationChannel();
            channel.addMember(player);
            player.setMessageChannel(channel);

            switch (reward.rewardType) {
                case COMMAND:
                    properties.add("commands");
                    break;
                case ITEM:
                    properties.add("items");
                    break;
                case SHOP:
                    properties.add("shop");
            }


            List<String> commandsToAdd = new ArrayList<>();
            Map<String, String> choices = new HashMap<>();
            properties.forEach(s -> choices.put(s, s));

            Question property = Question.builder().id("command").prompt("Input a reward property to begin. Here are the valid properties: " + properties)
                    .argument(GenericArguments.choices(Text.EMPTY, choices)).handler((reply, question) -> {

                        if (!properties.contains(reply)) {
                            player.sendMessage(Text.of("That is not a valid property"));
                            question.setNextQuestion(question);
                        }

                        switch (reply) {
                            case "commands":
                                Question addCommand = Question.builder().id("add-command").prompt("Input a command to add it to this reward. Input "
                                        + "'exit' to finish.").handler((response, questionTwo) -> {
                                    if (response.equalsIgnoreCase("exit")) {
                                        if (!commandsToAdd.isEmpty()) {
                                            reward.commands = commandsToAdd;
                                            shopMenu.getConfig().save();
                                        }
                                        player.sendMessage(Text.of("Commands have been added successfully"));
                                        questionTwo.setNextQuestion(null);
                                    } else {
                                        commandsToAdd.add(response);
                                        questionTwo.setNextQuestion(questionTwo);
                                    }
                                }).build();

                                question.setNextQuestion(addCommand);
                                break;
                            case "reward-name":
                                break;
                            case "gui-item":
                                ShopUtils.openDisplayItemInventory(player, shopMenu.getConfig(), reward);
                                question.setNextQuestion(null);
                                break;
                            case "inventory-index":
                                break;
                            case "cost":
                                Question cost = Question.builder().handler((response, questionTwo) -> {

                                    try {
                                        reward.cost = Double.parseDouble(response);
                                        shopMenu.getConfig().save();
                                    } catch (NumberFormatException e) {
                                        questionTwo.setNextQuestion(questionTwo);
                                        player.sendMessage(Text.of(TextColors.RED, "invalid number!"));
                                    }

                                }).build();

                                question.setNextQuestion(cost);
                                break;
                        }


                    }).build();

            Conversation conversation = channel.startConversation(property);

            conversation.stopHandler(() -> {

            });



        }

        return CommandResult.success();
    }
}
