package dev.slne.surf.friends.surf_menu

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.slne.surf.friends.FriendManager
import dev.slne.surf.friends.command.subcommand.FriendRequestListCommand
import dev.slne.surf.friends.plugin
import dev.slne.surf.gui.common.mutableInt2ObjectMapOf
import dev.slne.surf.gui.common.toComponent
import dev.slne.surf.gui.common.toLoreComponents
import dev.slne.surf.gui.menu.button.Button
import dev.slne.surf.gui.menu.item.CustomItemProvider
import dev.slne.surf.gui.menu.item.HeadItemBuilder
import dev.slne.surf.gui.menu.menu.MenuService
import dev.slne.surf.gui.menu.menu.specific.ScrollableMenu
import dev.slne.surf.gui.user.User
import dev.slne.surf.gui.util.Slot
import java.util.UUID

/**
 * Represents the FriendsMenu, a scrollable interface to display and manage a user's friends.
 * ID: friends
 *
 * @constructor Constructs a new FriendsMenu with the provided user ID.
 * @param args A string representation of the user ID.
 */
class FriendsMenu(user: User) : ScrollableMenu(
    "Freunde".toComponent(),
    mutableInt2ObjectMapOf(),
    Button(
    CustomItemProvider(
        "plus", true,
        "Freund hinzufügen".toComponent(),
        "Eine Freundschaftsanfrage verschicken".toLoreComponents()).build(), { executeComponent ->
        val (user, _, _, _, _) = executeComponent
        MenuService.openMenu(user, "add_friend")
    })
){
    val userID = user.uuid
    init {
        plugin.launch {
            FriendManager.getFriends(userID).forEach {
                addButton(Button(HeadItemBuilder().headTextureFromUuid(it).lore("Öffnet das Menu für diesen Spieler".toLoreComponents()).build(),
                    {
                    (user, _, _, _, _) ->
                    MenuService.openMenu(user, "player:$userID:$it")
                }
                ))
            }
        }
        setButton(Slot(1, 0).toSlot(), CustomItemProvider("friends_connected_gray").toButton())
        setButton(Slot(2, 0), Button(CustomItemProvider("friend_requests").build(), {
            (user, type, i, stack, menu) ->
            MenuService.openMenu(user, "friend_requests")
        }))
    }
}