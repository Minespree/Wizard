package net.minespree.wizard.util;

import com.comphenix.packetwrapper.WrapperPlayServerChat;
import com.comphenix.packetwrapper.WrapperPlayServerPlayerListHeaderFooter;
import com.comphenix.packetwrapper.WrapperPlayServerTitle;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.minespree.babel.BabelStringMessageType;
import net.minespree.wizard.WizardPlugin;
import org.bukkit.entity.Player;

public class MessageUtil {

    public static void send(Player player, EnumWrappers.TitleAction action, String text, int fadeIn, int stay, int fadeOut) {
        WrapperPlayServerTitle packet = new WrapperPlayServerTitle();
        packet.setAction(action);
        packet.setFadeIn(fadeIn);
        packet.setFadeOut(fadeOut);
        packet.setStay(stay);
        packet.setTitle(WrappedChatComponent.fromJson(formJSONString(text)));

        try {
            WizardPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet.getHandle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendTitle(Player player, BabelStringMessageType title, int fadeIn, int stay, int fadeOut, Object... params) {
        sendTitle(player, title.toString(player, params), fadeIn, stay, fadeOut);
    }

    public static void sendSubtitle(Player player, BabelStringMessageType subtitle, int fadeIn, int stay, int fadeOut, Object... params) {
        sendSubtitle(player, subtitle.toString(player, params), fadeIn, stay, fadeOut);
    }

    public static void sendActionBar(Player player, BabelStringMessageType actionBar, Object... params) {
        sendActionBar(player, actionBar.toString(player, params));
    }

    public static void sendTitle(Player player, String title, int fadeIn, int stay, int fadeOut) {
        send(player, EnumWrappers.TitleAction.TITLE, title, fadeIn, stay, fadeOut);
        send(player, EnumWrappers.TitleAction.SUBTITLE, " ", fadeIn, stay, fadeOut);
    }

    public static void sendSubtitle(Player player, String subtitle, int fadeIn, int stay, int fadeOut) {
        send(player, EnumWrappers.TitleAction.TITLE, " ", fadeIn, stay, fadeOut);
        send(player, EnumWrappers.TitleAction.SUBTITLE, subtitle, fadeIn, stay, fadeOut);
    }

    public static void sendTitleSubtitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        send(player, EnumWrappers.TitleAction.TITLE, title, fadeIn, stay, fadeOut);
        send(player, EnumWrappers.TitleAction.SUBTITLE, subtitle, fadeIn, stay, fadeOut);
    }

    public static void sendTitleSubtitle(Player player, BabelStringMessageType title, BabelStringMessageType subtitle, int fadeIn, int stay, int fadeOut) {
        send(player, EnumWrappers.TitleAction.TITLE, title.toString(player), fadeIn, stay, fadeOut);
        send(player, EnumWrappers.TitleAction.SUBTITLE, subtitle.toString(player), fadeIn, stay, fadeOut);
    }

    public static void sendActionBar(Player player, String actionBar) {
        WrapperPlayServerChat chat = new WrapperPlayServerChat();
        chat.setPosition((byte) 2);
        chat.setMessage(WrappedChatComponent.fromText(actionBar));
        try {
            WizardPlugin.getPlugin().getProtocolManager().sendServerPacket(player, chat.getHandle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendTabHeaderFooter(Player player, String header, String footer) {
        WrapperPlayServerPlayerListHeaderFooter packet = new WrapperPlayServerPlayerListHeaderFooter();
        packet.setFooter(WrappedChatComponent.fromText(footer));
        packet.setHeader(WrappedChatComponent.fromText(header));
        try {
            WizardPlugin.getPlugin().getProtocolManager().sendServerPacket(player, packet.getHandle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String formJSONString(String text) {
        return "{\"text\":\"" + text + "\"}";
    }


}
