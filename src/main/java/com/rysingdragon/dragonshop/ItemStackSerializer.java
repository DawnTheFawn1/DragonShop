package com.rysingdragon.dragonshop;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemStackSerializer implements TypeSerializer<ItemStack> {

    @Override
    public ItemStack deserialize(TypeToken<?> token, ConfigurationNode node) throws ObjectMappingException {
        ItemStack.Builder builder = ItemStack.builder().quantity(node.getNode("quantity").getInt());

        if (Sponge.getRegistry().getType(ItemType.class, node.getNode("id").getString()).isPresent() ) {
            builder.itemType(Sponge.getRegistry().getType(ItemType.class, node.getNode("id").getString()).get());
        } else {
            throw new ObjectMappingException();
        }

        if (!node.getNode("lore").isVirtual()) {
            List<Text> deserializedLore = node.getNode("lore").getList(TypeToken.of(String.class))
                    .stream().map(TextSerializers.FORMATTING_CODE::deserialize).collect(Collectors.toList());
            builder.add(Keys.ITEM_LORE, deserializedLore);
        }

        if (!node.getNode("enchantments").isVirtual()) {
            List<? extends ConfigurationNode> nodeList = node.getNode("enchantments").getChildrenList();
            List<ItemEnchantment> enchantments = new ArrayList<>();

            for (ConfigurationNode configurationNode : nodeList) {
                if (Sponge.getRegistry().getType(Enchantment.class, configurationNode.getNode("id").getString()).isPresent()) {
                    Enchantment enchantment = Sponge.getRegistry().getType(Enchantment.class, configurationNode.getNode("id").getString()).get();
                    int level = configurationNode.getNode("level").getInt();
                    enchantments.add(new ItemEnchantment(enchantment, level));
                } else {
                    throw new ObjectMappingException();
                }
            }

            builder.add(Keys.ITEM_ENCHANTMENTS, enchantments);
        }

        if (!node.getNode("display-name").isVirtual()) {
            Text displayName = TextSerializers.FORMATTING_CODE.deserialize(node.getNode("display-name").getString());
            builder.add(Keys.DISPLAY_NAME, displayName);
        }

        return builder.build();
    }

    @Override
    public void serialize(TypeToken<?> type, ItemStack stack, ConfigurationNode node) throws ObjectMappingException {
        node.getNode("id").setValue(stack.getItem().getId());
        node.getNode("quantity").setValue(stack.getQuantity());
        if (stack.get(Keys.ITEM_LORE).isPresent()) {
            List<String> serializedItemLore = stack.get(Keys.ITEM_LORE).get().stream().map(TextSerializers.FORMATTING_CODE::serialize).collect(Collectors.toList());
            node.getNode("lore").setValue(serializedItemLore);
        }

        if (stack.get(Keys.ITEM_ENCHANTMENTS).isPresent()) {
            List<ConfigurationNode> nodeList = new ArrayList<>();

            stack.get(Keys.ITEM_ENCHANTMENTS).get().forEach(enchantment -> {
                ConfigurationNode configurationNode = SimpleConfigurationNode.root();
                configurationNode.getNode("level").setValue(enchantment.getLevel());
                configurationNode.getNode("id").setValue(enchantment.getEnchantment().getId());
                nodeList.add(configurationNode);
            });

            node.getNode("enchantments").setValue(nodeList);
        }

        if (stack.get(Keys.DISPLAY_NAME).isPresent()) {
            node.getNode("display-name").setValue(TextSerializers.FORMATTING_CODE.serialize(stack.get(Keys.DISPLAY_NAME).get()));
        }

    }
}
