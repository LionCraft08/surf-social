package dev.slne.surf.social.chat.menu

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.gui.common.mutableInt2ObjectMapOf
import dev.slne.surf.gui.common.toComponent
import dev.slne.surf.gui.common.toLoreComponents
import dev.slne.surf.gui.menu.button.Button
import dev.slne.surf.gui.menu.item.HeadItemBuilder
import dev.slne.surf.gui.menu.menu.MenuService
import dev.slne.surf.gui.menu.menu.specific.ScrollableMenu
import dev.slne.surf.gui.user.User
import dev.slne.surf.gui.user.UserManager
import dev.slne.surf.social.chat.SurfChat
import dev.slne.surf.social.chat.`object`.ChatUser
import dev.slne.surf.social.chat.plugin
import dev.slne.surf.social.chat.util.Components
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

/**
 * Menu to Manage ignored users
 * id: ignore
 *
 */
class ChatIgnoreMenu(user: User): ScrollableMenu(
    "Ignorierte Spieler".toComponent(),
    mutableInt2ObjectMapOf()
) {
    init {
        plugin.launch {
            ChatUser.getUser(user.uuid).ignoreList.forEach {
                t -> addButton(Button(
                HeadItemBuilder().headTextureFromUuid(t)
                    .name("<red>${Bukkit.getOfflinePlayer(t).name}".toComponent())
                    .lore(("<orange>Du ignorierst diesen Spieler." +
                            "<br><white>Klicke hier um die Blockierung für " +
                            "<br><white>diesen Spieler aufzuheben").toLoreComponents()).build(),
                {
                    plugin.launch {
                        ChatUser.getUser(user.uuid).ignoreList.remove(t)
                        SurfChat.send(
                            Bukkit.getPlayer(user.uuid) ?: return@launch,
                            Components.getIgnoreComponent(Bukkit.getOfflinePlayer(t), false)
                        )
                        MenuService.openMenu(user, "ignore")
                    }
                })
                )
            }

        }
    }
}