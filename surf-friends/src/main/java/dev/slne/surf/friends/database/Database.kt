package dev.slne.surf.friends.database

import com.google.gson.Gson
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.slne.surf.friends.*
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.sql.SQLException
import java.util.*


object Database {
    private lateinit var dataSource: HikariDataSource

    fun createConnection() {
        val config = HikariConfig()
        config.jdbcUrl = plugin.config.getString("mysql.url")
        config.username = plugin.config.getString("mysql.user")
        config.password = plugin.config.getString("mysql.password")

        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        config.maximumPoolSize = 10
        config.maxLifetime = 600000

        dataSource = HikariDataSource(config)
        createTable()
    }

    private fun createTable() {
        val table: String = """
            CREATE TABLE IF NOT EXISTS surffriends (
            uuid VARCHAR(36) PRIMARY KEY,
            friends TEXT,
            friendrequests TEXT,
            friendnotes TEXT,
            allowrequests boolean
            );
            
            """.trimIndent()

        try {

            val source = dataSource;

            source.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.execute(table)
                }
            }
        } catch (e: SQLException) {
            Bukkit.getConsoleSender().sendMessage(
                prefix.append(
                    Component.text(
                        e.message ?: "Unknown (SQL Exeption in Database: createTable)"
                    )
                )
            )
        }
    }

    suspend fun getFriendData(player: UUID): FriendData {
        val query = "SELECT * FROM surffriends WHERE uuid = ?"

        return withContext(Dispatchers.IO) {
            try {
                val source = dataSource;

                source.connection.use { connection ->
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, player.toString())
                        statement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                val friends: String = resultSet.getString("friends")
                                val friendRequests: String = resultSet.getString("friendrequests")
                                val allowRequests: Boolean = resultSet.getBoolean("allowrequests")
                                val friendNotes: String = resultSet.getString("friendnotes")

                                val friendsList = if (friends.isEmpty()) {
                                    ObjectArraySet()
                                } else {
                                    ObjectArraySet(friends.split(",")
                                        .map { UUID.fromString(it.trim()) })
                                }

                                val friendRequestsList = if (friendRequests.isEmpty()) {
                                    ObjectArraySet()
                                } else {
                                    ObjectArraySet(friendRequests.split(",")
                                        .map { UUID.fromString(it.trim()) })
                                }

                                val friendNotesList = HashMap<UUID, String?>()
                                if (!friendNotes.isEmpty()) {
                                    val map = Gson().fromJson(friendNotes, HashMap::class.java) as HashMap<String, String>
                                    map.forEach { (k, v) -> friendNotesList[UUID.fromString(k)] = v }
                                }

                                return@withContext FriendData(
                                    player,
                                    friendsList,
                                    friendNotesList,
                                    friendRequestsList,
                                    allowRequests
                                )
                            }
                        }
                    }
                }
            } catch (e: SQLException) {
                Bukkit.getConsoleSender().sendMessage(
                    prefix.append(
                        Component.text(e.message ?: "Unknown SQL error")
                    )
                )
            }

            return@withContext FriendManager.newFriendData(player)
        }
    }

    suspend fun saveFriendData(friendData: FriendData) {
        val query = """
        INSERT INTO surffriends (uuid, friends, friendnotes, friendrequests, allowrequests)
        VALUES (?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
        friends = VALUES(friends),
        friendnotes = VALUES(friendnotes),
        friendrequests = VALUES(friendrequests),
        allowrequests = VALUES(allowrequests);
    """.trimIndent()

        val friends = friendData.friends.joinToString(",") { it.toString() }
        val friendRequests = friendData.friendRequests.joinToString(",") { it.toString() }
        val friendNotes = Gson().toJson(friendData.friendNotes)

        withContext(Dispatchers.IO) {
            try {
                val source = dataSource;

                source.connection.use { connection ->
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, friendData.player.toString())
                        statement.setString(2, friends)
                        statement.setString(3, friendNotes)
                        statement.setString(4, friendRequests)
                        statement.setBoolean(5, friendData.allowRequests)
                        statement.executeUpdate()
                    }
                }
            } catch (e: SQLException) {
                Bukkit.getConsoleSender().sendMessage(
                    prefix.append(
                        Component.text(
                            e.message ?: "Unknown SQL error"
                        )
                    )
                )
            }
        }
    }


    fun closeConnection() {
        val source = dataSource

        if (!source.isClosed) {
            source.close()
        }
    }
}
