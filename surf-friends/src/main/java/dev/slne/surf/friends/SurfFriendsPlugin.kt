package dev.slne.surf.friends

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents

import dev.slne.surf.friends.command.FriendCommand
import dev.slne.surf.friends.command.subcommand.FriendAddCommand
import dev.slne.surf.friends.command.subcommand.FriendListCommand
import dev.slne.surf.friends.database.Database
import dev.slne.surf.friends.listener.PlayerJoinListener
import dev.slne.surf.friends.listener.PlayerQuitListener
import dev.slne.surf.friends.listener.util.PluginColor
import dev.slne.surf.friends.surf_menu.FriendRequestsMenu
import dev.slne.surf.friends.surf_menu.FriendsMenu
import dev.slne.surf.friends.surf_menu.PlayerActionMenu
import dev.slne.surf.gui.menu.menu.MenuService

import kotlinx.coroutines.DelicateCoroutinesApi

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

val plugin: SurfFriendsPlugin get() = JavaPlugin.getPlugin(SurfFriendsPlugin::class.java)

val prefix = Component.text(">> ", NamedTextColor.GRAY)
    .append(Component.text("Friends", PluginColor.BLUE_LIGHT))
    .append(Component.text(" | ", NamedTextColor.DARK_GRAY))

class SurfFriendsPlugin : SuspendingJavaPlugin() {
    override fun onEnable() {
        this.registerCommands()
        this.registerListener()

        saveDefaultConfig()
        Database.createConnection()
        registerUIs()
    }

    override suspend fun onDisableAsync() {
        FriendManager.saveAll(true)
    }

    private fun registerCommands() {
        FriendCommand("friend").register()
        FriendAddCommand("fa").register()
        FriendListCommand("fl").register()
    }

    private fun registerUIs(){
        MenuService.registerNewMenu("player", PlayerActionMenu::class.java)
        MenuService.registerNewMenu("friends", FriendsMenu::class.java)
        MenuService.registerNewMenu("friends_requests", FriendRequestsMenu::class.java)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun registerListener() {
        Bukkit.getPluginManager().registerSuspendingEvents(PlayerQuitListener, this)
        Bukkit.getPluginManager().registerSuspendingEvents(PlayerJoinListener, this)
    }
}

/*
    *
    * info friend on friend remove and add when on other server
    * update friend on other server when removed or added
    * use cloud for sending players to other servers when "jumping" to friend
    * use cloud to get server from player and ignored message when server is "blocked" (to prevent spoiling to coming events etc.)
    *
 */
