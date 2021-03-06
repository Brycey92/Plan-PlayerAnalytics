package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.GenericLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;

/**
 * This SubCommand is used to view the version and the database type in use.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class InfoCommand extends CommandNode {

    private final PlanPlugin plugin;
    private final Locale locale;

    public InfoCommand(PlanPlugin plugin) {
        super("info", Permissions.INFO.getPermission(), CommandType.CONSOLE);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.get(CmdHelpLang.INFO).toString());
        this.plugin = plugin;
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        String yes = locale.getString(GenericLang.YES);
        String no = locale.getString(GenericLang.NO);

        String updateAvailable = VersionCheckSystem.isNewVersionAvailable() ? yes : no;
        String connectedToBungee = ConnectionSystem.getInstance().isServerAvailable() ? yes : no;
        String[] messages = {
                locale.getString(CommandLang.HEADER_INFO),
                "",
                locale.getString(CommandLang.INFO_VERSION, plugin.getVersion()),
                locale.getString(CommandLang.INFO_UPDATE, updateAvailable),
                locale.getString(CommandLang.INFO_DATABASE, Database.getActive().getName()),
                locale.getString(CommandLang.INFO_BUNGEE_CONNECTION, connectedToBungee),
                "",
                ">"
        };
        sender.sendMessage(messages);
    }

}
