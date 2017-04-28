package main.java.com.djrapitops.plan.data.cache.queue;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class DataCacheClearQueue {

    private BlockingQueue<UUID> q;
    private ClearSetup s;

    /**
     *
     * @param plugin
     * @param handler
     */
    public DataCacheClearQueue(Plan plugin, DataCacheHandler handler) {
        q = new ArrayBlockingQueue(Settings.PROCESS_CLEAR_LIMIT.getNumber());
        s = new ClearSetup();
        s.go(q, handler);
    }

    /**
     *
     * @param uuid
     */
    public void scheduleForClear(UUID uuid) {
        q.add(uuid);
    }

    /**
     *
     * @param uuids
     */
    public void scheduleForClear(Collection<UUID> uuids) {
        try {
            q.addAll(uuids);
        } catch (IllegalStateException e) {
            getPlugin(Plan.class).logError(Phrase.ERROR_TOO_SMALL_QUEUE.parse("Clear Queue", Settings.PROCESS_CLEAR_LIMIT.getNumber() + ""));
        }
    }

    /**
     *
     */
    public void stop() {
        s.stop();
    }
}

class ClearConsumer implements Runnable {

    private final BlockingQueue<UUID> queue;
    private final DataCacheHandler handler;
    private boolean run;

    ClearConsumer(BlockingQueue q, DataCacheHandler handler) {
        queue = q;
        this.handler = handler;
        run = true;
    }

    @Override
    public void run() {
        try {
            while (run) {
                consume(queue.take());
            }
        } catch (InterruptedException ex) {
        }
    }

    void consume(UUID uuid) {
        try {
            if (handler.isDataAccessed(uuid)) {
                queue.add(uuid);
            } else if (!getOfflinePlayer(uuid).isOnline()) {
                handler.clearFromCache(uuid);
            }
            // if online remove from clear list
        } catch (Exception ex) {
            getPlugin(Plan.class).toLog(this.getClass().getName(), ex);
        }
    }

    void stop() {
        run = false;
    }
}

class ClearSetup {

    private ClearConsumer one;
    private ClearConsumer two;

    void go(BlockingQueue<UUID> q, DataCacheHandler handler) {
        one = new ClearConsumer(q, handler);
        two = new ClearConsumer(q, handler);
        new Thread(one).start();
        new Thread(two).start();
    }

    void stop() {
        one.stop();
        two.stop();
    }
}