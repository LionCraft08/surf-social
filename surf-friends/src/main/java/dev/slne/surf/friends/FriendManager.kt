@file:OptIn(DelicateCoroutinesApi::class)

package dev.slne.surf.friends

import com.github.benmanes.caffeine.cache.Caffeine
import dev.hsbrysk.caffeine.CoroutineLoadingCache
import dev.hsbrysk.caffeine.buildCoroutine
import dev.slne.surf.friends.database.Database
import dev.slne.surf.friends.listener.util.PluginColor
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

object FriendManager {
    val cache: CoroutineLoadingCache<UUID, FriendData> =
        Caffeine.newBuilder().buildCoroutine { player: UUID -> loadFriendData(player) }

    /* Friend Management */
    suspend fun addFriend(player: UUID, target: UUID) {
        val playerData = queryFriendData(player)
        val targetData = queryFriendData(target)

        if(this.areFriends(player, target)) {
            return
        }

        playerData.friends.add(target);
        targetData.friends.add(player)

        cache.put(player, playerData)
        cache.put(target, targetData)

        sendMessage(player, Component.text("Du bist nun mit ")
                .append(Component.text(getName(target), PluginColor.GOLD))
                .append(Component.text(" befreundet."))
        )

        sendMessage(target, Component.text("Du bist nun mit ")
                .append(Component.text(getName(player), PluginColor.GOLD))
                .append(Component.text(" befreundet."))
        )
    }

    suspend fun removeFriend(player: UUID, target: UUID) {
        val playerData = queryFriendData(player)
        val targetData = queryFriendData(target)

        if (!playerData.friends.remove(target)) {
            return
        }

        if (!targetData.friends.remove(player)) {
            return
        }

        cache.put(player, playerData)
        cache.put(target, targetData)

        sendMessage(
            player, Component.text("Du hast ")
                .append(Component.text(getName(target), PluginColor.GOLD))
                .append(Component.text(" aus deiner Freundesliste entfernt."))
        )

        sendMessage(
            target, Component.text("Du wurdest von ")
                .append(Component.text(getName(player), PluginColor.GOLD))
                .append(Component.text("´s Freundesliste entfernt."))
        )
    }
    suspend fun tryFriendRequest(player: Player, target: OfflinePlayer): Boolean{
        if (hasFriendRequest(player.uniqueId, target.uniqueId)) {
            player.sendMessage(Component.text("Du hast bereits eine Freundschaftsanfrage von ${target.name}", PluginColor.RED))
            return false
        }

        if (hasFriendRequest(target.uniqueId, player.uniqueId)) {
            player.sendMessage(Component.text("Du hast bereits eine Freundschaftsanfrage an ${target.name} gesendet.", PluginColor.RED))
            return false
        }

        if (areFriends(target.uniqueId, player.uniqueId)) {
            player.sendMessage(Component.text("Du bist bereits mit ${target.name} befreundet.", PluginColor.RED))
            return false
        }

        if (target == player) {
            player.sendMessage(Component.text("Du kannst nicht mit dir selbst befreundet sein.", PluginColor.RED))
            return false
        }

        if (!isAllowingRequests(target.uniqueId)) {
            sendMessage(player.uniqueId, Component.text("${target.name} hat Freundschaftsanfragen deaktiviert. Sie wurde trotzdem geschickt, der Spieler hat aber keine Benachrichtigung bekommen!", PluginColor.RED))
        }

        sendFriendRequest(player.uniqueId, target.uniqueId)
        return true
    }

    suspend fun sendFriendRequest(player: UUID, target: UUID) {
        val targetData = queryFriendData(target)

        if (targetData.friends.contains(player)) {
            return
        }

        if(targetData.friendRequests.contains(player)) {
            return
        }

        if(player == target) {
            return
        }

        if (!targetData.friendRequests.add(player)) {
            return
        }

        cache.put(target, targetData)

        sendMessage(player, Component.text("Du hast eine Freundschaftsanfrage an ")
                .append(Component.text(getName(target), PluginColor.GOLD))
                .append(Component.text(" gesendet."))
        )

        if (targetData.allowRequests) {
            sendMessage(target, Component.text("Du hast eine Freundschaftsanfrage von ")
                    .append(Component.text(getName(player), PluginColor.GOLD))
                    .append(Component.text(" erhalten. "))
                    .append(Component.text("[AKZEPTIEREN]", PluginColor.LIGHT_GREEN).clickEvent(ClickEvent.runCommand("/friend accept " + getNameOrUUID(player))))
                    .append(Component.text(" [ABLEHNEN]", PluginColor.RED).clickEvent(ClickEvent.runCommand("/friend deny " + getNameOrUUID(player))))
            )
        }
    }

    suspend fun acceptFriendRequest(player: UUID, target: UUID) {
        val playerData = queryFriendData(player)
        val targetData = queryFriendData(target)

        playerData.friendRequests.remove(target);
        targetData.friendRequests.remove(player)

        cache.put(player, playerData)
        cache.put(target, targetData)

        addFriend(player, target)

        sendMessage(
            player, Component.text("Du hast die Freundschaftsanfrage von ")
                .append(Component.text(getName(target), PluginColor.GOLD))
                .append(Component.text(" akzeptiert."))
        )

        sendMessage(
            target, Component.text("Deine Freundschaftsanfrage an ")
                .append(Component.text(getName(player), PluginColor.GOLD))
                .append(Component.text(" wurde akzeptiert."))
        )
    }

    suspend fun denyFriendRequest(player: UUID, target: UUID) {
        val playerData = queryFriendData(player)
        val targetData = queryFriendData(target)

        playerData.friendRequests.remove(target)
        targetData.friendRequests.remove(player)
        cache.put(player, playerData)
        cache.put(target, targetData)

        sendMessage(
            player, Component.text("Du hast die Freundschaftsanfrage von ")
                .append(Component.text(getName(target), PluginColor.GOLD))
                .append(Component.text(" abgelehnt."))
        )

        sendMessage(
            target, Component.text("Deine Freundschaftsanfrage an ")
                .append(Component.text(getName(player), PluginColor.GOLD))
                .append(Component.text(" wurde abgelehnt."))
        )
    }

    suspend fun hasFriendRequest(player: UUID, target: UUID): Boolean =
        queryFriendData(player).friendRequests.contains(target)

    suspend fun getFriendRequests(player: UUID): ObjectSet<UUID> =
        queryFriendData(player).friendRequests

    suspend fun toggle(player: UUID): Boolean {
        val playerData = queryFriendData(player)

        playerData.allowRequests = !(playerData.allowRequests)
        cache.put(player, playerData)

        return playerData.allowRequests
    }
    suspend fun getFriendNote(player: UUID, target: UUID): String?{
        return queryFriendData(player).friendNotes[target]
    }
    suspend fun setFriendNote(player: UUID, target: UUID, note: String?){
        val playerData = queryFriendData(player)
        playerData.friendNotes[target] = note
        cache.put(player, playerData)
    }
    suspend fun removeFriendNote(player: UUID, target: UUID){
        val playerData = queryFriendData(player)
        playerData.friendNotes.remove(target)
        cache.put(player, playerData)
    }

    suspend fun isAllowingRequests(player: UUID): Boolean = queryFriendData(player).allowRequests

    private suspend fun queryFriendData(player: UUID): FriendData =
        cache.get(player) ?: newFriendData(player)

    suspend fun saveFriendData(player: UUID) = Database.saveFriendData(queryFriendData(player))

    suspend fun saveAll(closeConnection: Boolean = true) {
        cache.synchronous().asMap().map { player -> saveFriendData(player.key) }

        if (closeConnection) {
            withContext(Dispatchers.IO) {
                Database.closeConnection()
            }
        }
    }

    fun sendMessage(uuid: UUID, message: Component = Component.text("???")) {
        val player = Bukkit.getPlayer(uuid) ?: return

        player.sendMessage(prefix.append(message))
    }

    private fun getName(uuid: UUID) = Bukkit.getOfflinePlayer(uuid).name ?: "Unbekannt"
    private fun getNameOrUUID(uuid: UUID) = Bukkit.getOfflinePlayer(uuid).name ?: uuid

    suspend fun getOnlineFriends(player: UUID): ObjectList<Player> {
        // TODO: Cloud implementation
        //FIXME Müsste hier ned ne uuid list abgefragt werden weil
        return ObjectArrayList()
    }

    suspend fun getServer(player: UUID): String {
        // TODO: Cloud implementation
        return "???"
    }

    suspend fun isOnline(player: UUID): Boolean?{
        //TODO Cloud implementation
        return null
    }
    suspend fun getLastSeen(player: UUID): Date{
        return Date(Bukkit.getOfflinePlayer(player).lastSeen)
        //TODO Should also be requested from Velocity via Cloud
    }

    suspend fun getFriends(player: UUID): ObjectSet<UUID> = queryFriendData(player).friends

    suspend fun areFriends(player: UUID, target: UUID): Boolean =
        queryFriendData(player).friends.contains(target)

    suspend fun loadFriendData(player: UUID): FriendData = Database.getFriendData(player)
    
    fun newFriendData(player: UUID): FriendData = FriendData(player)
}
