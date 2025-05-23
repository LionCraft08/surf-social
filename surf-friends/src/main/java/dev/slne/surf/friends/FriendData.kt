package dev.slne.surf.friends

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

data class FriendData(
    val player: UUID,
    val friends: ObjectSet<UUID>  = ObjectArraySet(),
    val friendNotes:HashMap<UUID,String?> = HashMap(),
    val friendRequests: ObjectSet<UUID> = ObjectArraySet(),
    var allowRequests: Boolean = true
)
