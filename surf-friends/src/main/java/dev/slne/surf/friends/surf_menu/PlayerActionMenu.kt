package dev.slne.surf.friends.surf_menu

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.slne.surf.friends.FriendManager
import dev.slne.surf.friends.SurfFriendsPlugin
import dev.slne.surf.friends.plugin
import dev.slne.surf.friends.prefix
import dev.slne.surf.gui.common.mutableInt2ObjectMapOf
import dev.slne.surf.gui.common.toComponent
import dev.slne.surf.gui.common.toLoreComponents
import dev.slne.surf.gui.menu.button.Button
import dev.slne.surf.gui.menu.item.CustomItemProvider
import dev.slne.surf.gui.menu.item.HeadItemBuilder
import dev.slne.surf.gui.menu.menu.DefaultMenu
import dev.slne.surf.gui.menu.menu.MenuService
import dev.slne.surf.gui.menu.menu.MenuType
import dev.slne.surf.gui.menu.menu.specific.ConfirmMenu
import dev.slne.surf.gui.menu.menu.specific.TextInputMenu
import dev.slne.surf.gui.user.UserManager
import dev.slne.surf.gui.util.Slot
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import kotlinx.coroutines.CoroutineScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.checkerframework.checker.units.qual.s
import java.text.SimpleDateFormat
import java.util.UUID
import kotlin.coroutines.CoroutineContext

/**
 * A Menu containing actions for a specific player.
 * ID: player
 * @param args viewer_uuid:target_uuid
 * @constructor Creates a new PlayerActionMenu for the specified player.
 * @author @LionK08
 */
class PlayerActionMenu (args: String) : DefaultMenu(
    Component.text("Spieler ${Bukkit.getOfflinePlayer(args.substringBefore(":")).name} verwalten"),
    MenuType.GENERIC9X6,
    mutableInt2ObjectMapOf<Button>()
) {
    var player: UUID = UUID.fromString(args.substringBefore(":"))
    var target: UUID = UUID.fromString(args.substringAfter(":"))

    init {
        plugin.launch {
            getButtons(player, target).forEach { (slot, button) ->
                setButton(slot, button)
            }
        }

    }
}

suspend fun getButtons(source: UUID, target: UUID): Int2ObjectMap<Button> {
    val buttons = mutableInt2ObjectMapOf<Button>()
    var targetPlayer = Bukkit.getOfflinePlayer(target)
    buttons.put(
        Slot(4, 2).toSlot(),
        Button(HeadItemBuilder().headTextureFromUuid(target).build())
    )
    buttons.put(
        Slot(1, 4).toSlot(),
        if (FriendManager.areFriends(source, target)) {
            CustomItemProvider(
                "minus", true,
                "<red>Freund entfernen".toComponent(),
                "Entfernt ${targetPlayer.name} aus deiner Freundesliste".toLoreComponents()
            ).toButton { (user, type, i, stack, menu) ->
                ConfirmMenu({
                    plugin.launch {
                        FriendManager.removeFriend(source, target)
                        MenuService.openMenu(UserManager[source], "player:$source:$target")
                    }
                }).open(user)
            }
        } else if (!FriendManager.hasFriendRequest(target, source)) {
            CustomItemProvider(
                "plus", true,
                "<green>Freundesanfrage schicken".toComponent(),
                "<white>Schickt dem Spieler eine Freundschafts-<br>anfrage, wenn dieser Anfragen nicht <br>deaktiviert hat.".toLoreComponents()
            ).toButton { (user, type, i, stack, menu) ->
                run {
                    if (menu is PlayerActionMenu) {
                        plugin.launch {
                            FriendManager.tryFriendRequest(Bukkit.getPlayer(user.uuid)!!, Bukkit.getOfflinePlayer(menu.target))
                        }
                        user.closeCurrentMenu()
                    }
                }
            }
        } else {//TODO add case to accept a incoming Friend request by this Player
            CustomItemProvider(
                "not_available",
                true,
                Component.text("Aktion nicht verfügbar"),
                ("<gray>Du kannst diesem Spieler keine <br>Freundschaftsanfrage senden," +
                        "da bereits <br> eine Anfrage aussteht.").toLoreComponents()
            ).toButton()
        }
    )
    buttons.put(
        Slot(3, 4).toSlot(), CustomItemProvider(
            "whisper", true,
            "<orange>Direktnachricht".toComponent(),
            "<yellow>Sende eine Direktnachricht an ${targetPlayer.name}".toLoreComponents()
        ).toButton { (user, type, i, stack, menu) ->
            user.sendMessage(
                prefix.append(
                    "<white>Klicke <blue>hier<white>, um eine Direktnachricht an ${targetPlayer.name} zu senden".toComponent()
                        .hoverEvent(
                            "<gray>Direktnachrichtfeld öffnen".toComponent()
                        ).clickEvent(
                        ClickEvent.suggestCommand("/pm ${targetPlayer.name}")
                    )
                )
            )
            user.closeCurrentMenu()
        })//Maybe improve or change that
    buttons.put(
        Slot(5, 4).toSlot(), CustomItemProvider(
            "jump",
            true,
            "<yellow>Jump to Player".toComponent(),
            "<orange>Verbindet dich mit dem<br> aktuellen Server von ${targetPlayer.name}".toLoreComponents()
        ).toButton { (user, type, i, stack, menu) ->
            user.sendMessage(prefix.append("<orange>Diese Funktion ist noch nicht verfügbar".toComponent()))
            //TODO: Jump to player
        })
    buttons.put(
        Slot(7, 4).toSlot(),
        CustomItemProvider(
            "ignore",
            true,
            "<red>Diesen Spieler ignorieren".toComponent(),
            ("<white>Blockiert diesen Spieler<br>" +
                    "<red>Dies trennt eure Freundschaft und<br>" +
                    "du siehst keine Nachrichten mehr von ${targetPlayer.name} ").toLoreComponents()
        ).toButton { (user, type, i, stack, menu) ->
            ConfirmMenu({
                plugin.launch {
                    if (FriendManager.areFriends(user.uuid, target)) {
                        FriendManager.removeFriend(user.uuid, target)
                        user.sendMessage(prefix.append("<red>Du hast ${targetPlayer.name} aus deiner Freundesliste entfernt.".toComponent()))
                    }
                    (user.player as Player).performCommand("ignore ${targetPlayer.name}")
                }
            })
        })
    buttons.put(
        Slot(2, 3).toSlot(),
        CustomItemProvider(
            "report",
            true,
            "<orange>Spieler melden".toComponent(),
            "<white>Öffnet das Spieler-Melden-Formular".toLoreComponents()
        ).toButton { (user, type, i, stack, menu) ->
            MenuService.openMenu(user, "report:${target}")
        })
    //TODO: Add a report menu?
    val text = FriendManager.getFriendNote(source, target) ?: ""
    buttons.put(
        Slot(6, 3).toSlot(),
        CustomItemProvider(
            "info",
            true,
            "<orange>Spieler-Info".toComponent(),
            ("<white>|-> ${getOnlineString(target)}" +
                    "<br><white>|-> Spieler-Notiz:"+
                    "<br><white>${FriendManager.getFriendNote(source, target)?:"Nicht verfügbar"}"+
                    "<br><white>Klicke zum bearbeiten").toLoreComponents()
        ).toButton { (user, _, _, _, _) ->
            TextInputMenu(
                "Edit friend note".toComponent(),
                { (user, _, _, _, _, text) ->
                    plugin.launch {
                        FriendManager.setFriendNote(user.uuid, target, text)
                    }
                }, text
            ).open(user)
        })

    return buttons
}

suspend fun getOnlineString(uuid: UUID): String{
    val online = FriendManager.isOnline(uuid)
    return if(online == null){
        "Nicht Verfügbar, @Blame Cloud-Devs"
    }else if(online){
        "<green> Online auf ${FriendManager.getServer(uuid)}"
    }else{
        "<red>Offline seit ${SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(FriendManager.getLastSeen(uuid))}"
    }
}