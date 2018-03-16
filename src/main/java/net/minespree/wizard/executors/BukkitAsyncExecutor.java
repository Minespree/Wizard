package net.minespree.wizard.executors;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

/**
 * A {@link Executor} wrapper around the Bukkit scheduler API that will spawn async tasks.
 */
@RequiredArgsConstructor(staticName = "create")
public class BukkitAsyncExecutor implements Executor {
    @NonNull
    private final Plugin plugin;

    @Override
    public void execute(@Nonnull Runnable command) {
        Preconditions.checkNotNull(command, "command");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, command);
    }
}
