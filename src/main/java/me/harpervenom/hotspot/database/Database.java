package me.harpervenom.hotspot.database;

import me.harpervenom.hotspot.database.managers.PlayersManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class Database {

    private Connection connection;

    public PlayersManager players;

    public void init() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath()
                            + "/hotspot.db");

            players = new PlayersManager(connection);

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()){
                connection.close();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
