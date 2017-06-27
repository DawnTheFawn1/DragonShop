package com.rysingdragon.dragonshop.commands;

import com.rysingdragon.conversation.Conversation;
import com.rysingdragon.conversation.ConversationChannel;
import com.rysingdragon.conversation.Question;
import com.rysingdragon.dragonshop.Command;
import com.rysingdragon.dragonshop.ShopUtils;
import com.rysingdragon.dragonshop.arguments.ShopArgument;
import com.rysingdragon.dragonshop.config.ShopConfig;
import com.rysingdragon.dragonshop.enums.RewardType;
import com.rysingdragon.dragonshop.menus.ShopMenu;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

@Command("addreward")
public class AddRewardCommand extends BaseCommand {

    private ShopConfig config;

    @Override
    public CommandElement getArgs() {
        return GenericArguments.seq(
                new ShopArgument(Text.of("shop"))
                /*GenericArguments.string(Text.of("name")),
                GenericArguments.enumValue(Text.of("type"), RewardType.class),
                GenericArguments.integer(Text.of("slot")),
                GenericArguments.doubleNum(Text.of("cost"))*/
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of(""));
        }
        Player player = (Player) src;
        ShopMenu shop = (ShopMenu) args.getOne("shop").get();

        ConversationChannel channel = new ConversationChannel();
        channel.addMember(player);
        player.setMessageChannel(channel);

        Question cost = Question.builder().id("cost").prompt("How much do you want this to cost? (Enter 0 for no cost)").handler((reply, question) -> {
            try {
                Double.parseDouble(reply);
                question.setNextQuestion(null);
            } catch (NumberFormatException e) {
                question.setNextQuestion(question);
                src.sendMessage(Text.of(TextColors.RED, "That is not a valid number"));
            }
        }).build();

        Question index = Question.builder().id("inventory-index").prompt("What position in the inventory will this be in?").handler((reply, question) -> {
            try {
                Integer.parseInt(reply);
                question.setNextQuestion(cost);
            } catch (NumberFormatException e) {
                src.sendMessage(Text.of(TextColors.RED, "That is not a valid number"));
                question.setNextQuestion(question);
            }
        }).build();

        Question rewardType = Question.builder().id("reward-type").prompt("What type of reward will this be? Types are command, item, and shop")
                .argument(GenericArguments.enumValue(Text.EMPTY, RewardType.class)).handler((reply, question) -> {
                    try {
                        RewardType.valueOf(reply.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        src.sendMessage(Text.of(TextColors.RED, "That is not a valid reward type"));
                        question.setNextQuestion(question);
                    }
                    question.setNextQuestion(index);
        }).build();

        Question rewardName = Question.builder().id("reward-name").prompt("What is the name of the reward?").handler((reply, question) -> {
            question.setNextQuestion(rewardType);
        }).build();

        Conversation conversation = channel.startConversation(rewardName);

        conversation.stopHandler(()-> {
            ShopConfig.Reward reward = new ShopConfig.Reward();
            reward.name = conversation.getQuestion("reward-name").get().getReply();
            reward.displayName = reward.name;
            reward.rewardType = RewardType.valueOf(conversation.getQuestion("reward-type").get().getReply());
            reward.inventoryIndex = Integer.parseInt(conversation.getQuestion("inventory-index").get().getReply());
            reward.cost = Double.parseDouble(conversation.getQuestion("cost").get().getReply());

            shop.getConfig().rewards.put(TextSerializers.FORMATTING_CODE.stripCodes(ShopUtils.formatName(reward.name)), reward);
            this.config = shop.getConfig();
            ShopUtils.openDisplayItemInventory(player, config, reward);
        });




        /**ShopMenu shop = (ShopMenu) args.getOne("shop").get();
        String rewardName = (String) args.getOne("name").get();
        RewardType type = (RewardType) args.getOne("type").get();
        int inventoryIndex = (int) args.getOne("slot").get();
        double cost = (double) args.getOne("cost").get();

        ShopConfig.Reward reward = new ShopConfig.Reward();
        reward.name = rewardName;
        reward.displayName = rewardName;
        reward.rewardType = type;
        reward.inventoryIndex = inventoryIndex;
        reward.cost = cost;


        shop.getConfig().rewards.put(TextSerializers.FORMATTING_CODE.stripCodes(ShopUtils.formatName(rewardName)), reward);
        this.config = shop.getConfig();
        ShopUtils.openDisplayItemInventory(player, config, reward);*/
        //Sponge.getEventManager().registerListeners(DragonShop.getInstance(), this);
        //player.openInventory(getInv(), Cause.of(NamedCause.owner(DragonShop.getInstance()))).get();

        return CommandResult.success();
    }






    /**@Listener
    public void onClose(InteractInventoryEvent.Close event) {
        if (!event.getCause().first(Player.class).isPresent())
            return;

        Player player = event.getCause().first(Player.class).get();
        Inventory queryResult = event.getTargetInventory().query(GridInventory.class);


        for (Inventory grid : queryResult) {
            if (grid.getArchetype() == InventoryArchetypes.DISPENSER && (grid.size() == 1)) {
                this.reward.guiItem = grid.peek().get();
                this.config.save();
            } else {
                player.sendMessage(Text.of("Unable to save display item, you must only put 1 item in the inventory before closing"));
            }
        }

        Sponge.getEventManager().unregisterListeners(this);
    }

    public static void openDisplayItemInventory(Player player, ShopConfig config, ShopConfig.Reward reward) {
        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.AQUA, "Thing")))
                .of(InventoryArchetypes.DISPENSER)
                .listener(InteractInventoryEvent.Close.class, event -> {

                    if (!event.getCause().first(Player.class).isPresent())
                        return;

                    Inventory queryResult = event.getTargetInventory().query(GridInventory.class);

                    for (Inventory grid : queryResult) {
                        if (grid.size() == 1) {
                            reward.guiItem = grid.peek().get();
                            config.save();
                        } else {
                            player.sendMessage(Text.of("Unable to save display item, you must only put 1 item in the inventory before closing"));
                        }
                    }

                })
                .build(DragonShop.getInstance());
        player.openInventory(inventory, Cause.of(NamedCause.owner(DragonShop.getInstance())));
    }**/
}
