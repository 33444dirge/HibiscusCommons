package me.lojosho.hibiscuscommons.plugins;

import com.google.common.collect.ImmutableList;
import me.lojosho.hibiscuscommons.HibiscusPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SubPlugins {

    private static final List<HibiscusPlugin> SUB_PLUGINS = new ArrayList<>();
    private static volatile List<HibiscusPlugin> snapshot = ImmutableList.of();

    @NotNull
    public static List<HibiscusPlugin> getSubPlugins() {
        return snapshot;
    }

    @ApiStatus.Internal
    public static synchronized void addSubPlugin(@NotNull HibiscusPlugin plugin) {
        SUB_PLUGINS.add(plugin);
        snapshot = ImmutableList.copyOf(SUB_PLUGINS);
    }

    @ApiStatus.Internal
    public static synchronized void removeSubPlugin(@NotNull HibiscusPlugin plugin) {
        SUB_PLUGINS.remove(plugin);
        snapshot = ImmutableList.copyOf(SUB_PLUGINS);
    }
}
