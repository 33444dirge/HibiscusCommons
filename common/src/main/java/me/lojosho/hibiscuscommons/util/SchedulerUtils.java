package me.lojosho.hibiscuscommons.util;

import me.lojosho.hibiscuscommons.HibiscusCommonsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for handling scheduler operations across both Bukkit and Folia servers.
 * This class provides a unified API for scheduling tasks that works on both server types.
 */
public final class SchedulerUtils {

    private SchedulerUtils() {
        // Utility class
    }

    /**
     * Runs a task on the main thread.
     *
     * @param plugin The plugin that owns the task
     * @param task   The task to run
     */
    public static void runTask(Plugin plugin, Runnable task) {
        if (HibiscusCommonsPlugin.isOnFolia()) {
            runFoliaGlobalTask(task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Runs a task on the main thread after the specified delay in ticks.
     *
     * @param plugin The plugin that owns the task
     * @param task   The task to run
     * @param delay  The delay in ticks
     */
    public static void runTaskLater(Plugin plugin, Runnable task, long delay) {
        if (HibiscusCommonsPlugin.isOnFolia()) {
            runFoliaGlobalTaskDelayed(task, delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    /**
     * Runs a task on the main thread repeatedly after the specified initial delay and period.
     *
     * @param plugin The plugin that owns the task
     * @param task   The task to run
     * @param delay  The initial delay in ticks
     * @param period The period in ticks between executions
     */
    public static void runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        if (HibiscusCommonsPlugin.isOnFolia()) {
            runFoliaGlobalTaskTimer(task, delay, period);
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }

    /**
     * Runs a task asynchronously.
     *
     * @param plugin The plugin that owns the task
     * @param task   The task to run
     */
    public static void runTaskAsynchronously(Plugin plugin, Runnable task) {
        if (HibiscusCommonsPlugin.isOnFolia()) {
            runFoliaAsyncTask(task);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /**
     * Runs a task asynchronously after the specified delay in ticks.
     *
     * @param plugin The plugin that owns the task
     * @param task   The task to run
     * @param delay  The delay in ticks
     */
    public static void runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay) {
        if (HibiscusCommonsPlugin.isOnFolia()) {
            runFoliaAsyncTaskDelayed(task, delay);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
        }
    }

    /**
     * Runs a task asynchronously repeatedly after the specified initial delay and period.
     *
     * @param plugin The plugin that owns the task
     * @param task   The task to run
     * @param delay  The initial delay in ticks
     * @param period The period in ticks between executions
     */
    public static void runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period) {
        if (HibiscusCommonsPlugin.isOnFolia()) {
            runFoliaAsyncTaskTimer(task, delay, period);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
        }
    }

    /**
     * Runs a task on the entity's region thread (Folia only).
     * On non-Folia servers, this runs on the main thread.
     *
     * @param entity The entity whose region to run the task on
     * @param task   The task to run
     */
    public static void runTaskForEntity(Entity entity, Runnable task) {
        if (HibiscusCommonsPlugin.isOnFolia()) {
            runFoliaEntityTask(entity, task);
        } else {
            Bukkit.getScheduler().runTask(HibiscusCommonsPlugin.getInstance(), task);
        }
    }

    /**
     * Runs a task on the location's region thread (Folia only).
     * On non-Folia servers, this runs on the main thread.
     *
     * @param location The location whose region to run the task on
     * @param task     The task to run
     */
    public static void runTaskForLocation(Location location, Runnable task) {
        if (HibiscusCommonsPlugin.isOnFolia()) {
            runFoliaLocationTask(location, task);
        } else {
            Bukkit.getScheduler().runTask(HibiscusCommonsPlugin.getInstance(), task);
        }
    }

    // Folia-specific methods
    private static void runFoliaGlobalTask(Runnable task) {
        try {
            // Use reflection to access Folia's global region scheduler
            Class<?> schedulerClass = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Object server = schedulerClass.getMethod("getInstance").invoke(null);
            Object globalRegionScheduler = server.getClass().getMethod("getGlobalRegionScheduler").invoke(server);
            globalRegionScheduler.getClass().getMethod("run", Plugin.class, Runnable.class)
                    .invoke(globalRegionScheduler, HibiscusCommonsPlugin.getInstance(), task);
        } catch (Exception e) {
            HibiscusCommonsPlugin.getInstance().getLogger().warning("Failed to run Folia global task: " + e.getMessage());
            // Fallback to running directly (not ideal but prevents crashes)
            task.run();
        }
    }

    private static void runFoliaGlobalTaskDelayed(Runnable task, long delay) {
        try {
            Class<?> schedulerClass = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Object server = schedulerClass.getMethod("getInstance").invoke(null);
            Object globalRegionScheduler = server.getClass().getMethod("getGlobalRegionScheduler").invoke(server);
            globalRegionScheduler.getClass().getMethod("runDelayed", Plugin.class, Runnable.class, long.class)
                    .invoke(globalRegionScheduler, HibiscusCommonsPlugin.getInstance(), task, delay);
        } catch (Exception e) {
            HibiscusCommonsPlugin.getInstance().getLogger().warning("Failed to run delayed Folia global task: " + e.getMessage());
            // Fallback to running directly
            task.run();
        }
    }

    private static void runFoliaGlobalTaskTimer(Runnable task, long delay, long period) {
        try {
            Class<?> schedulerClass = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Object server = schedulerClass.getMethod("getInstance").invoke(null);
            Object globalRegionScheduler = server.getClass().getMethod("getGlobalRegionScheduler").invoke(server);
            globalRegionScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Runnable.class, long.class, long.class)
                    .invoke(globalRegionScheduler, HibiscusCommonsPlugin.getInstance(), task, delay, period);
        } catch (Exception e) {
            HibiscusCommonsPlugin.getInstance().getLogger().warning("Failed to run timed Folia global task: " + e.getMessage());
            // Fallback to running directly once
            task.run();
        }
    }

    private static void runFoliaAsyncTask(Runnable task) {
        try {
            Class<?> schedulerClass = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Object server = schedulerClass.getMethod("getInstance").invoke(null);
            Object asyncScheduler = server.getClass().getMethod("getAsyncScheduler").invoke(server);
            asyncScheduler.getClass().getMethod("runNow", Plugin.class, Runnable.class)
                    .invoke(asyncScheduler, HibiscusCommonsPlugin.getInstance(), task);
        } catch (Exception e) {
            HibiscusCommonsPlugin.getInstance().getLogger().warning("Failed to run Folia async task: " + e.getMessage());
            // Fallback to running in a new thread
            new Thread(task).start();
        }
    }

    private static void runFoliaAsyncTaskDelayed(Runnable task, long delay) {
        try {
            Class<?> schedulerClass = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Object server = schedulerClass.getMethod("getInstance").invoke(null);
            Object asyncScheduler = server.getClass().getMethod("getAsyncScheduler").invoke(server);
            long delayMs = delay * 50; // Convert ticks to milliseconds
            asyncScheduler.getClass().getMethod("runDelayed", Plugin.class, Runnable.class, long.class, TimeUnit.class)
                    .invoke(asyncScheduler, HibiscusCommonsPlugin.getInstance(), task, delayMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            HibiscusCommonsPlugin.getInstance().getLogger().warning("Failed to run delayed Folia async task: " + e.getMessage());
            // Fallback to running in a new thread after delay
            new Thread(() -> {
                try {
                    Thread.sleep(delay * 50);
                    task.run();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    private static void runFoliaAsyncTaskTimer(Runnable task, long delay, long period) {
        try {
            Class<?> schedulerClass = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Object server = schedulerClass.getMethod("getInstance").invoke(null);
            Object asyncScheduler = server.getClass().getMethod("getAsyncScheduler").invoke(server);
            long delayMs = delay * 50; // Convert ticks to milliseconds
            long periodMs = period * 50; // Convert ticks to milliseconds
            asyncScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Runnable.class, long.class, long.class, TimeUnit.class)
                    .invoke(asyncScheduler, HibiscusCommonsPlugin.getInstance(), task, delayMs, periodMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            HibiscusCommonsPlugin.getInstance().getLogger().warning("Failed to run timed Folia async task: " + e.getMessage());
            // Fallback to running in a new thread with a simple timer
            new Thread(() -> {
                try {
                    Thread.sleep(delay * 50);
                    while (true) {
                        task.run();
                        Thread.sleep(period * 50);
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    private static void runFoliaEntityTask(Entity entity, Runnable task) {
        try {
            Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
            scheduler.getClass().getMethod("run", Plugin.class, Runnable.class, Object.class, Runnable.class)
                    .invoke(scheduler, HibiscusCommonsPlugin.getInstance(), task, null, null);
        } catch (Exception e) {
            HibiscusCommonsPlugin.getInstance().getLogger().warning("Failed to run Folia entity task: " + e.getMessage());
            // Fallback to running on main thread
            Bukkit.getScheduler().runTask(HibiscusCommonsPlugin.getInstance(), task);
        }
    }

    private static void runFoliaLocationTask(Location location, Runnable task) {
        try {
            Class<?> schedulerClass = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Object server = schedulerClass.getMethod("getInstance").invoke(null);
            Object regionScheduler = server.getClass().getMethod("getRegionScheduler").invoke(server);
            regionScheduler.getClass().getMethod("run", Plugin.class, Location.class, Runnable.class)
                    .invoke(regionScheduler, HibiscusCommonsPlugin.getInstance(), location, task);
        } catch (Exception e) {
            HibiscusCommonsPlugin.getInstance().getLogger().warning("Failed to run Folia location task: " + e.getMessage());
            // Fallback to running on main thread
            Bukkit.getScheduler().runTask(HibiscusCommonsPlugin.getInstance(), task);
        }
    }
}