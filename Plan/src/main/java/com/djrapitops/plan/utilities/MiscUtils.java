package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.utilities.FormatUtils;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class MiscUtils {

    /**
     * Checks the version and returns response String.
     *
     * @return String informing about status of plugins version.
     */
    public static String checkVersion() {
        Plan plugin = getPlugin(Plan.class);
        String cVersion;
        String lineWithVersion;
        try {
            URL githubUrl = new URL("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/src/plugin.yml");
            lineWithVersion = "";
            Scanner websiteScanner = new Scanner(githubUrl.openStream());
            while (websiteScanner.hasNextLine()) {
                String line = websiteScanner.nextLine();
                if (line.toLowerCase().contains("version")) {
                    lineWithVersion = line;
                    break;
                }
            }
            String versionString = lineWithVersion.split(": ")[1];
            double newestVersionNumber = FormatUtils.parseVersionDouble(versionString);
            cVersion = plugin.getDescription().getVersion();
            double currentVersionNumber = FormatUtils.parseVersionDouble(cVersion);
            if (newestVersionNumber > currentVersionNumber) {
                return "New Version (" + versionString + ") is availible at https://www.spigotmc.org/resources/plan-player-analytics.32536/";
            } else {
                return "You're running the latest version";
            }
        } catch (Exception e) {
            plugin.logToFile("Failed to compare versions.\n" + e);
        }
        return "Failed to get newest version number.";
    }

    public static String getPlayerDisplayname(String[] args, CommandSender sender) {
        String playerName = "";
        Plan plugin = getPlugin(Plan.class);
        if (args.length > 0) {
            if ((args[0].equals("-a")) || (args[0].equals("-r"))) {
                playerName = "ArgumentGivenError";
                plugin.log("No username given, returned empty username.");
                plugin.logToFile(Phrase.ERROR_NO_USERNAME + args[0]);
            } else if (sender.hasPermission("plan.inspect.other") || !(sender instanceof Player)) {
                playerName = args[0];
            }
        } else {
            try {
                Player player = plugin.getServer().getPlayer(UUIDFetcher.getUUIDOf(sender.getName()));
                playerName = player.getName();
            } catch (Exception e) {
                playerName = "ConsoleNotPlayerErr";
            }
        }
        return playerName;
    }

    public static Set<OfflinePlayer> getMatchingDisplaynames(String search) {
        List<OfflinePlayer> players = new ArrayList<>();
        players.addAll(Arrays.asList(Bukkit.getOfflinePlayers()));
        Set<OfflinePlayer> matches = new HashSet<>();
        players.parallelStream().filter((OfflinePlayer player) -> (player.getName().contains(search))).forEach((OfflinePlayer player) -> {
            matches.add(player);
        });
        return matches;
    }

}