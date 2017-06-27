package com.rysingdragon.dragonshop.commands;

import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCommand implements CommandExecutor {

    private String basePermission;

    public void setBasePermission(String basePermission) {
        this.basePermission = basePermission;
    }

    public String getBasePermission() {
        return this.basePermission;
    }

    public CommandElement getArgs() {
        return GenericArguments.none();
    }

    public List<Class<? extends BaseCommand>> getChildren() {
        return new ArrayList<>();
    }

}
