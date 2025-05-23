package dev.slne.surf.friends.command

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import dev.slne.surf.friends.command.subcommand.*
import dev.slne.surf.friends.menu.FriendMainMenu
import dev.slne.surf.gui.menu.menu.MenuService
import dev.slne.surf.gui.user.UserManager
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class FriendCommand(commandName: String) : CommandAPICommand(commandName) {
    
    init {
        withPermission("surf.friends.command.friend")

        withSubcommand(FriendAcceptCommand("accept"))
        withSubcommand(FriendAddCommand("add"))
        withSubcommand(FriendDenyCommand("deny"))
        withSubcommand(FriendRemoveCommand("remove"))
        withSubcommand(FriendToggleCommand("toggle"))
        withSubcommand(FriendJumpCommand("jump"))
        withSubcommand(FriendListCommand("list"))
        withSubcommand(FriendRequestListCommand("requests"))

        executesPlayer(PlayerCommandExecutor { player: Player, _: CommandArguments? ->
            MenuService.openMenu(UserManager[player.uniqueId], "friends:${player.uniqueId}")
            //FriendMainMenu().show(player)
        })
    }
}

fun <T> CommandArguments.getUncheckedOrFail(
    nodeName: String,
    message: String? = "$nodeName not found"
): T = getUnchecked<T>(nodeName) ?: throw CommandAPI.failWithString(message)

fun CommandArguments.getOfflinePlayerOrFail(nodeName: String): OfflinePlayer = getUncheckedOrFail(nodeName, "Der Spieler wurde nicht gefunden.")