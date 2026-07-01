package me.lojosho.hibiscuscommons.nms;

import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface NMSPacketBuilder {

    Object buildEntityMovePacket(int entityId, @NotNull Location from, @NotNull Location to, boolean onGround);

    Object buildEntityLookAtPacket(int entityId, @NotNull Location location);

    default Object buildEntityRotatePacket(int entityId, Location location, boolean onGround) {
        return buildEntityRotatePacket(entityId, location.getYaw(), location.getPitch(), onGround);
    }
    Object buildEntityRotatePacket(int entityId, float originalYaw, float pitch, boolean onGround);

    default Object buildEntityRotateHeadPacket(int entityId, @NotNull Location location) {
        return buildEntityRotateHeadPacket(entityId, location.getYaw());
    }
    Object buildEntityRotateHeadPacket(int entityId, float yaw);

    Object buildEntityMountPacket(int mountId, int[] passengerIds);

    Object buildEntityLeashPacket(int leashEntity, int entityId);

    Object buildEntityTeleportPacket(int entityId,
                                            double x,
                                            double y,
                                            double z,
                                            float yaw,
                                            float pitch,
                                            boolean onGround);

    Object buildEntityCameraPacket(int entityId);

    default Object buildEntitySpawnPacket(
            int entityId,
            @NotNull UUID uuid,
            @NotNull EntityType entityType,
            @NotNull Location location
    ) {
        return buildEntitySpawnPacket(entityId, uuid, entityType, location.x(), location.y(), location.z(), location.getYaw(), location.getPitch());
    }
    Object buildEntitySpawnPacket(
            int entityId,
            @NotNull UUID uuid,
            @NotNull EntityType entityType,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    );

    Object buildEntityMetadataPacket(int entityId, Map<Integer, Number> dataValues);

    Object buildEntityDestroyPacket(IntList entityIds);

    Object buildEntityAttributePacket(
            int entityId,
            Attribute attribute,
            double value
    );

    Object buildEntityEquipmentSlotUpdatePacket(
            int entityId,
            @NotNull Map<EquipmentSlot, ItemStack> equipment
    );

    Object buildPlayerSlotUpdatePacket(Player player, int slot);

    Object buildPlayerGamemodeChangePacket(@NotNull GameMode gameMode);

    Object buildPlayerInfoAddPacket(
            @NotNull final Player skinnedPlayer,
            final int entityId,
            @NotNull final UUID uuid,
            @NotNull final String npcName
    );

    Object buildPlayerInfoRemovePacket(List<UUID> uuids);

    Object buildPlayerScoreboardRemovePacket(Player player, String name);
    Object buildPlayerScoreboardCreatePacket(Player player, String name);
    Object buildPlayerScoreboardAddPlayersPacket(Player player, String name);
}