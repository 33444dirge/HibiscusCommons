package me.lojosho.hibiscuscommons.nms.v1_21_R6.packets;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import io.papermc.paper.adventure.PaperAdventure;
import it.unimi.dsi.fastutil.ints.IntList;
import me.lojosho.hibiscuscommons.HibiscusCommonsPlugin;
import me.lojosho.hibiscuscommons.nms.NMSPacketBuilder;
import me.lojosho.hibiscuscommons.util.AdventureUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.attribute.CraftAttribute;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboard;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class NMSPackets implements NMSPacketBuilder {

    private static final Entity FAKE_NMS_ENTITY = new ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND, MinecraftServer.getServer().overworld());

    @Override
    public Object buildEntityMovePacket(int entityId, @NotNull Location from, @NotNull Location to, boolean onGround) {
        byte dx = (byte) (to.getX() - from.getX());
        byte dy = (byte) (to.getY() - from.getY());
        byte dz = (byte) (to.getZ() - from.getZ());
        return new ClientboundMoveEntityPacket.Pos(entityId, dx, dy, dz, onGround);
    }

    @Override
    public Object buildEntityLookAtPacket(int entityId, @NotNull Location location) {
        FAKE_NMS_ENTITY.setId(entityId);
        FAKE_NMS_ENTITY.setPos(location.getX(), location.getY(), location.getZ());
        return new ClientboundPlayerLookAtPacket(EntityAnchorArgument.Anchor.EYES, FAKE_NMS_ENTITY, EntityAnchorArgument.Anchor.EYES);
    }

    @Override
    public Object buildEntityRotatePacket(int entityId, float originalYaw, float pitch, boolean onGround) {
        float ROTATION_FACTOR = 256.0F / 360.0F;
        byte yaw = (byte) (originalYaw * ROTATION_FACTOR);
        byte pitchByte = (byte) (pitch * ROTATION_FACTOR);
        return new ClientboundMoveEntityPacket.Rot(entityId, yaw, pitchByte, onGround);
    }

    @Override
    public Object buildEntityRotateHeadPacket(int entityId, float yaw) {
        FAKE_NMS_ENTITY.setId(entityId);
        byte headRot = (byte) (yaw * 256.0F / 360.0F);
        return new ClientboundRotateHeadPacket(FAKE_NMS_ENTITY, headRot);
    }

    @Override
    public Object buildEntityMountPacket(int mountId, int[] passengerIds) {
        List<Entity> passengers = Arrays.stream(passengerIds).mapToObj(id -> {
            Entity passenger = new ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND, MinecraftServer.getServer().overworld());
            passenger.setId(id);
            return passenger;
        }).toList();
        FAKE_NMS_ENTITY.setId(mountId);
        FAKE_NMS_ENTITY.ejectPassengers();
        FAKE_NMS_ENTITY.passengers = ImmutableList.copyOf(passengers);
        return new ClientboundSetPassengersPacket(FAKE_NMS_ENTITY);
    }

    @Override
    public Object buildEntityLeashPacket(int leashEntity, int entityId) {
        ServerLevel level = MinecraftServer.getServer().overworld();
        Entity entity1 = new ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND, level);
        Entity entity2 = new ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND, level);
        entity1.setId(leashEntity);
        entity2.setId(entityId);
        return new ClientboundSetEntityLinkPacket(entity1, entity2);
    }

    @Override
    public Object buildEntityTeleportPacket(int entityId, double x, double y, double z, float yaw, float pitch, boolean onGround) {
        return ClientboundTeleportEntityPacket.teleport(entityId, new net.minecraft.world.entity.PositionMoveRotation(new Vec3(x, y, z), Vec3.ZERO, yaw, pitch), Set.of(), onGround);
    }

    @Override
    public Object buildEntityCameraPacket(int entityId) {
        FAKE_NMS_ENTITY.setId(entityId);
        return new ClientboundSetCameraPacket(FAKE_NMS_ENTITY);
    }

    @Override
    public Object buildEntitySpawnPacket(int entityId, @NotNull UUID uuid, @NotNull EntityType entityType, double x, double y, double z, float yaw, float pitch) {
        net.minecraft.world.entity.EntityType<?> nmsEntityType = CraftEntityType.bukkitToMinecraft(entityType);
        Vec3 velocity = Vec3.ZERO;
        float headYaw = 0f;
        return new ClientboundAddEntityPacket(entityId, uuid, x, y, z, yaw, pitch, nmsEntityType, 0, velocity, headYaw);
    }

    @Override
    public Object buildEntityMetadataPacket(int entityId, Map<Integer, Number> dataValues) {
        List<SynchedEntityData.DataValue<?>> nmsDataValues = dataValues.entrySet().stream().map(entry -> {
            int index = entry.getKey();
            Number value = entry.getValue();
            return switch (value) {
                case Byte byteVal -> new SynchedEntityData.DataValue<>(index, EntityDataSerializers.BYTE, byteVal);
                case Float floatVal -> new SynchedEntityData.DataValue<>(index, EntityDataSerializers.FLOAT, floatVal);
                case Integer intVal -> new SynchedEntityData.DataValue<>(index, EntityDataSerializers.INT, intVal);
                default ->
                        throw new IllegalArgumentException("Unsupported data value type: " + value.getClass().getSimpleName());
            };
        }).collect(Collectors.toList());
        return new ClientboundSetEntityDataPacket(entityId, nmsDataValues);
    }

    @Override
    public Object buildEntityDestroyPacket(@NotNull IntList entityIds) {
        return new ClientboundRemoveEntitiesPacket(entityIds);
    }

    @Override
    public Object buildEntityAttributePacket(int entityId, Attribute attribute, double value) {
        AttributeInstance attrInstance = new AttributeInstance(
                CraftAttribute.bukkitToMinecraftHolder(attribute),
                (ignored) -> {}
        );
        attrInstance.setBaseValue(value);
        return new ClientboundUpdateAttributesPacket(entityId, List.of(attrInstance));
    }

    @Override
    public Object buildEntityEquipmentSlotUpdatePacket(int entityId, @NotNull Map<EquipmentSlot, ItemStack> equipment) {
        final List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> pairs = new ArrayList<>();
        for (EquipmentSlot slot : equipment.keySet()) {
            net.minecraft.world.entity.EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(slot);
            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(equipment.get(slot));
            pairs.add(new Pair<>(nmsSlot, nmsItem));
        }
        return new ClientboundSetEquipmentPacket(entityId, pairs);
    }

    @Override
    public Object buildPlayerSlotUpdatePacket(@NotNull Player player, int slot) {
        int index = 0;
        ServerPlayer player1 = ((CraftPlayer) player).getHandle();
        if (index < Inventory.getSelectionSize()) {
            index += 36;
        } else if (index > 39) {
            index += 5;
        } else if (index > 35) {
            index = 8 - (index - 36);
        }
        ItemStack item = player.getInventory().getItem(slot);
        return new ClientboundContainerSetSlotPacket(player1.inventoryMenu.containerId, player1.inventoryMenu.incrementStateId(), index, CraftItemStack.asNMSCopy(item));
    }

    @Override
    public Object buildPlayerGamemodeChangePacket(@NotNull GameMode gameMode) {
        ClientboundGameEventPacket.Type type = ClientboundGameEventPacket.CHANGE_GAME_MODE;
        float param = gameMode.getValue();
        return new ClientboundGameEventPacket(type, param);
    }

    @Override
    public Object buildPlayerInfoAddPacket(@NotNull Player skinnedPlayer, int entityId, @NotNull UUID uuid, @NotNull String npcName) {
        ServerPlayer player = ((CraftPlayer) skinnedPlayer).getHandle();
        String name = npcName;
        if (name.length() > 15) name = name.substring(0, 15);
        Property property = ((CraftPlayer) skinnedPlayer).getProfile().properties().get("textures").stream().findAny().orElse(null);

        final Multimap<String, Property> multimaps = MultimapBuilder.hashKeys().arrayListValues().build(((CraftPlayer) skinnedPlayer).getProfile().properties());
        if (property != null) {
            multimaps.removeAll("textures");
            multimaps.put("textures", property);
        }
        PropertyMap map = new PropertyMap(multimaps);
        GameProfile profile = new GameProfile(uuid, name, map);

        Component component = AdventureUtils.MINI_MESSAGE.deserialize(name);
        net.minecraft.network.chat.Component nmsComponent = HibiscusCommonsPlugin.isOnPaper() ? PaperAdventure.asVanilla(component) : net.minecraft.network.chat.Component.literal(name);

        RemoteChatSession.Data chatData = null;
        RemoteChatSession session = player.getChatSession();
        if (session != null) chatData = player.getChatSession().asData();

        ClientboundPlayerInfoUpdatePacket.Entry entry = new ClientboundPlayerInfoUpdatePacket.Entry(uuid, profile, false, 0, GameType.CREATIVE, nmsComponent, true, player.listOrder, chatData);
        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER);
        return new ClientboundPlayerInfoUpdatePacket(actions, entry);
    }

    @Override
    public Object buildPlayerInfoRemovePacket(List<UUID> uuids) {
        return new ClientboundPlayerInfoRemovePacket(uuids);
    }

    @Override
    public Object buildPlayerScoreboardRemovePacket(Player player, String name) {
        PlayerTeam team = new PlayerTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), name);
        team.setNameTagVisibility(Team.Visibility.NEVER);
        return ClientboundSetPlayerTeamPacket.createRemovePacket(team);
    }

    @Override
    public Object buildPlayerScoreboardCreatePacket(Player player, String name) {
        PlayerTeam team = new PlayerTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), name);
        team.setNameTagVisibility(Team.Visibility.NEVER);
        return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
    }

    @Override
    public Object buildPlayerScoreboardAddPlayersPacket(Player player, String name) {
        PlayerTeam team = new PlayerTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), name);
        team.setNameTagVisibility(Team.Visibility.NEVER);
        return ClientboundSetPlayerTeamPacket.createMultiplePlayerPacket(team, new ArrayList<String>() {{
            add(name);
        }}, ClientboundSetPlayerTeamPacket.Action.ADD);
    }
}