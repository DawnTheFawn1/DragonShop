package com.rysingdragon.dragonshop;

import com.rysingdragon.dragonshop.commands.BaseCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;

import java.util.List;

import javax.annotation.Nullable;

public class CommandManager {

    public static void registerCommand(BaseCommand command) {
        Command annotation = command.getClass().getAnnotation(Command.class);
        Sponge.getCommandManager().register(DragonShop.getInstance(), getSpec(command, null), annotation.value());
    }

    private static CommandSpec getSpec(BaseCommand command, @Nullable Command parent)  {
        Command annotation = command.getClass().getAnnotation(Command.class);

        StringBuilder permissionBuilder = new StringBuilder(DragonShop.ID + ".");
        if (parent != null) {
            permissionBuilder.append(parent.value()[0]).append(".");
        }
        permissionBuilder.append(annotation.value()[0]);
        command.setBasePermission(permissionBuilder.toString());

        //TODO PermissionDescriptions

        String descriptionKey;
        String extendedKey;
        if (parent == null) {
            descriptionKey = "command." + annotation.value()[0] + ".description";
            extendedKey = "command." + annotation.value()[0] + ".extended";
        } else {
            descriptionKey = "command." + parent.value()[0] + "." + annotation.value()[0] + ".description";
            extendedKey = "command." + parent.value()[0] + "." + annotation.value()[0] + ".extended";
        }

        CommandSpec.Builder builder = CommandSpec.builder()
                .permission(command.getBasePermission() + ".command")
                .executor(command)
                .arguments(command.getArgs());

        if (MessageManager.resource.containsKey(descriptionKey)) {
            builder.description(MessageManager.getText(descriptionKey));
        }

        if (MessageManager.resource.containsKey(extendedKey)) {
            builder.extendedDescription(MessageManager.getText(extendedKey));
        }

        if (!command.getChildren().isEmpty()) {
            List<Class<? extends BaseCommand>> children = command.getChildren();
            for (Class<? extends BaseCommand> childClass : children) {
                try {
                    BaseCommand child = childClass.newInstance();
                    Command childAnnotation = childClass.getAnnotation(Command.class);
                    builder.child(getSpec(child, annotation), childAnnotation.value());
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return builder.build();
    }

}
