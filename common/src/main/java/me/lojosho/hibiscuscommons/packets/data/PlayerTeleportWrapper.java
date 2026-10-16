package me.lojosho.hibiscuscommons.packets.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlayerTeleportWrapper {
    private final int teleportId;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
}
