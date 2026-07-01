package me.lojosho.hibiscuscommons.nms.v1_21_R6.packets;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NMSPacketSender implements me.lojosho.hibiscuscommons.nms.NMSPacketSender {

    @Override
    public void sendPacket(@NotNull Object packet, @NotNull Player... players) {
        final Packet<?> nmsPacket = (Packet<?>) packet;
        for (Player player : players) sendPacketToPlayer(player, nmsPacket);
    }

    @Override
    public void sendPacket(@NotNull Object packet, @NotNull List<Player> players) {
        final Packet<?> nmsPacket = (Packet<?>) packet;
        for (Player player : players) sendPacketToPlayer(player, nmsPacket);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendBundle(@NotNull List<Object> packets, @NotNull Player... players) {
        final List<Packet<? super ClientGamePacketListener>> nmsPackets = new ArrayList<>(packets.size());
        for (Object p : packets) {
            nmsPackets.add((Packet<? super ClientGamePacketListener>) p);
        }
        final ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(nmsPackets);
        for (Player player : players) sendPacketToPlayer(player, bundlePacket);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendBundle(@NotNull List<Object> packets, @NotNull List<Player> players) {
        final List<Packet<? super ClientGamePacketListener>> nmsPackets = new ArrayList<>(packets.size());
        for (Object p : packets) {
            nmsPackets.add((Packet<? super ClientGamePacketListener>) p);
        }
        final ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(nmsPackets);
        for (Player player : players) sendPacketToPlayer(player, bundlePacket);
    }

    private void sendPacketToPlayer(Player player, Packet<?> packet) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        ServerPlayerConnection connection = nmsPlayer.connection;
        connection.send(packet);
    }
}