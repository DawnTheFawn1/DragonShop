package com.rysingdragon.dragonshop;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class MessageManager {

    public static final ResourceBundle resource = ResourceBundle.getBundle("assets.dragonshop.messages");

    public static Text getText(String key) {
        return TextSerializers.FORMATTING_CODE.deserialize(resource.getString(key));
    }

    public static Text getFormattedText(String key, Object... replacements) {
        return TextSerializers.FORMATTING_CODE.deserialize(getFormattedString(key, replacements));
    }

    public static String getString(String key) {
        return resource.getString(key);
    }

    public static String getFormattedString(String key, Object... replacements) {
        return MessageFormat.format(getString(key), replacements);
    }

}
