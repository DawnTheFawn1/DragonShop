package com.rysingdragon.dragonshop;

import org.spongepowered.api.service.permission.PermissionDescription;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

    String[] value();

    String permissionLevel() default PermissionDescription.ROLE_USER;

}
