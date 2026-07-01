package me.lojosho.hibiscuscommons.nms;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface NMSPacketSender {

    void sendPacket(@NotNull Object packet, @NotNull Player... players);
    void sendPacket(@NotNull Object packet, @NotNull List<Player> players);
    void sendBundle(@NotNull List<Object> packets, @NotNull Player... players);
    void sendBundle(@NotNull List<Object> packets, @NotNull List<Player> players);

}