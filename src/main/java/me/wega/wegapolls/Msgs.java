package me.wega.wegapolls;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

@UtilityClass
public final class Msgs {
    public static final MiniMessage MM = MiniMessage.miniMessage();

    public static Component msg(String miniMessage) {
        return MM.deserialize(miniMessage);
    }
}