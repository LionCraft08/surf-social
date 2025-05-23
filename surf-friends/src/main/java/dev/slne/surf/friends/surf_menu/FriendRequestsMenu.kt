package dev.slne.surf.friends.surf_menu

import com.github.retrooper.packetevents.protocol.item.type.ItemTypes
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.slne.surf.friends.FriendManager
import dev.slne.surf.friends.plugin
import dev.slne.surf.gui.common.mutableInt2ObjectMapOf
import dev.slne.surf.gui.common.toComponent
import dev.slne.surf.gui.common.toLoreComponents
import dev.slne.surf.gui.menu.button.Button
import dev.slne.surf.gui.menu.item.CustomItemProvider
import dev.slne.surf.gui.menu.item.HeadItemBuilder
import dev.slne.surf.gui.menu.item.ItemBuilder
import dev.slne.surf.gui.menu.menu.MenuService
import dev.slne.surf.gui.menu.menu.specific.ScrollableMenu
import dev.slne.surf.gui.menu.menu.specific.TextInputMenu
import dev.slne.surf.gui.user.User
import dev.slne.surf.gui.user.UserManager
import dev.slne.surf.gui.util.Slot
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import java.util.UUID

/**
 * Menu for friend requests
 * ID: friends_requests
 *
 * @param args - UUID of the user to show friend requests for
 * @constructor Create Friend requests menu
 * @author Lion
 * @since 4.0.0
 */
class FriendRequestsMenu (user: User): ScrollableMenu(
    "Freundschaftsanfragen".toComponent(),
    mutableInt2ObjectMapOf()
) {
    val userID = user.uuid
    init {
        setButton(Slot(1, 0).toSlot(), CustomItemProvider("friends_connected").toButton() {
                (user, type, i, stack, menu) ->
            MenuService.openMenu(user, "friends")
        })
        setButton(Slot(2, 0), CustomItemProvider("friend_requests_connected_gray").toButton())
        setButton(Slot(2, 5).toSlot(), Button(ItemBuilder(ItemTypes.ANVIL, {
            lore("Füge einen Freund hinzu, indem<br>du seinen Namen eingibst.".toLoreComponents())
        }),{
            openFriendRequestByNameMenu(userID)
        }))
        plugin.launch {
            if (FriendManager.getFriendRequests(userID).isEmpty()) {
                addButton(CustomItemProvider("not_available", true,
                    "<green>Alles erledigt :)".toComponent(),
                    "<white>Du hast keine Freundschafts-<br>anfragen".toLoreComponents()
                    ).toButton())
            }else FriendManager.getFriendRequests(userID).forEach {
                addButton(Button(HeadItemBuilder().headTextureFromUuid(it)
                    .name("Freundschaftsanfrage von ${Bukkit.getOfflinePlayer(it).name}".toComponent())
                    .lore(mutableListOf<Component>(
                        Component.translatable("key.attack").append(" um die Anfrage zu akzeptieren".toComponent().color(
                            TextColor.color(0, 255, 150))),
                        Component.translatable("key.use").append(" um die Anfrage abzulehnen".toComponent().color(
                            TextColor.color(255, 0, 100))),
                        Component.translatable("key.drop").append(" um das Menu für diesen Spieler zu öffnen".toComponent().color(
                            TextColor.color(0, 150, 255)))
                    )).build(), {
                    (user, type, i, stack, menu) ->
                    MenuService.openMenu(user, "friend_requests")
                }))
            }
        }
    }
}
fun openFriendRequestByNameMenu(player: UUID){
    TextInputMenu(
        "Spielername eingeben".toComponent(),
        { (user, _, _, _, _, text) ->
            plugin.launch {
                FriendManager.tryFriendRequest(Bukkit.getPlayer(user.uuid)!!, Bukkit.getOfflinePlayer(text))
            }
        }).open(UserManager[player])
}