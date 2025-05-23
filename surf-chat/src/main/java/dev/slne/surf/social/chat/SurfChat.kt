package dev.slne.surf.social.chat

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.jorel.commandapi.CommandAPI
import dev.slne.surf.gui.menu.menu.BookProvider
import dev.slne.surf.gui.menu.menu.MenuService
import dev.slne.surf.social.chat.command.*
import dev.slne.surf.social.chat.command.channel.ChannelCommand
import dev.slne.surf.social.chat.listener.PlayerAsyncChatListener
import dev.slne.surf.social.chat.listener.PlayerQuitListener
import dev.slne.surf.social.chat.menu.ChatIgnoreMenu
import dev.slne.surf.social.chat.`object`.Message
import dev.slne.surf.social.chat.permission.SurfChatPermissions
import dev.slne.surf.social.chat.provider.ConfigurationProvider
import dev.slne.surf.social.chat.provider.WordBlacklistProvider
import dev.slne.surf.social.chat.service.ChatFilterService
import dev.slne.surf.social.chat.service.ChatHistoryService
import dev.slne.surf.social.chat.service.DatabaseService
import dev.slne.surf.social.chat.util.MessageBuilder
import dev.slne.surf.surfapi.core.api.SurfCoreApi
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.util.InternalSurfApi
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.io.path.div

class SurfChat : SuspendingJavaPlugin() {
    override suspend fun onEnableAsync() {
        CommandAPI.unregister("msg")
        CommandAPI.unregister("tell")
        CommandAPI.unregister("w")

        WordBlacklistProvider.instance = WordBlacklistProvider(instance.dataPath / "blocked.txt")

        PrivateMessageCommand("msg").register()
        ChannelCommand("channel").register()
        SurfChatCommand("surfchat").register()
        IgnoreCommand("ignore").register()
        TogglePmCommand("togglepm").register()
        ReplyCommand("reply").register()
        SurfChatHistoryCommand("history").register() //temporär, kann entfernt werden
        WordBlacklistCommand("blacklist").register()

        this.saveDefaultConfig()

        ConfigurationProvider.load()
        DatabaseService.connect()

        Bukkit.getPluginManager().registerEvents(PlayerAsyncChatListener(), this)
        Bukkit.getPluginManager().registerEvents(PlayerQuitListener(), this)

        SurfChatPermissions

        MenuService.registerNewMenu("ignore", ChatIgnoreMenu::class.java)
    }

    override suspend fun onDisableAsync() {
        ConfigurationProvider.save()
        WordBlacklistProvider.instance.save(instance.dataPath / "blocked.txt")

        DatabaseService.saveAll()
        DatabaseService.disconnect()
    }

    companion object {
        val instance: SurfChat get() = getPlugin(SurfChat::class.java)

        fun send(player: OfflinePlayer, text: MessageBuilder, messageID: UUID = UUID.randomUUID()) {
            send(player, text.build(), messageID)
        }

        fun send(player: OfflinePlayer, text: Component, messageID: UUID = UUID.randomUUID()) {
            val message = Colors.PREFIX.append(text)
            val onlinePlayer = player.player ?: return

            onlinePlayer.sendMessage(message)
            ChatHistoryService.insertNewMessage(
                player.uniqueId,
                Message("Unknown", player.name ?: player.uniqueId.toString(), message),
                messageID
            )
        }

        fun send(text: Component, receiver:Audience, messageID: UUID = UUID.randomUUID()){
            if (receiver is Player){
                send(receiver, text, messageID)
            }else{
                receiver.sendMessage(Colors.PREFIX.append(text))
            }
        }
    }
}

val plugin get() = SurfChat.instance
