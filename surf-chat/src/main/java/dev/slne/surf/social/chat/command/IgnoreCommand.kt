package dev.slne.surf.social.chat.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.offlinePlayerArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.gui.menu.menu.MenuService
import dev.slne.surf.gui.user.UserManager
import dev.slne.surf.social.chat.SurfChat
import dev.slne.surf.social.chat.menu.ChatIgnoreMenu
import dev.slne.surf.social.chat.`object`.ChatUser
import dev.slne.surf.social.chat.plugin
import dev.slne.surf.social.chat.util.Components
import org.bukkit.OfflinePlayer

class IgnoreCommand(commandName: String) : CommandAPICommand(commandName) {
    init {
        withPermission("surf.chat.command.ignore")
        offlinePlayerArgument("target", true)

        playerExecutor { player, args ->
            if (args.args.isEmpty()) {
                val menu = ChatIgnoreMenu(UserManager[player.uniqueId])
                menu.open(UserManager[player.uniqueId])
                return@playerExecutor
            }
            val target: OfflinePlayer by args
            val targetUuid = target.uniqueId

            plugin.launch {
                val user = ChatUser.getUser(player.uniqueId)
                val targetUser = ChatUser.getUser(targetUuid)

                if (targetUuid == player.uniqueId) {
                    SurfChat.send(player, Components.cannotIgnoreSelf)
                    return@launch
                }

                if (user.isIgnoring(targetUuid)) {
                    user.ignoreList.remove(targetUuid)
                    SurfChat.send(player, Components.getIgnoreComponent(target, false))
                } else {
                    user.ignoreList.add(targetUuid)
                    SurfChat.send(player, Components.getIgnoreComponent(target, true))
                }
            }
        }
    }
}
