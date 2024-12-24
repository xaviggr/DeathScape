package dc.Persistence.player;

import dc.Business.groups.GroupData;
import dc.Business.player.PlayerData;
import dc.Persistence.groups.GroupDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PlayerEditDatabase {

    public static void setPlayerAsDeath(Player playerDeath) {

        PlayerData playerdata = PlayerDatabase.getPlayerDataFromDatabase (playerDeath.getName ());
        if (playerdata == null) {
            playerDeath.kickPlayer ("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerdata.setDeaths(playerdata.getDeaths() + 1);
            playerdata.setDead(true);
            PlayerDatabase.addPlayerDataToDatabase (playerdata);
        }
    }

    public static void setPlayerBanDate(Player player) {
        PlayerData playerdata = PlayerDatabase.getPlayerDataFromDatabase (player.getName ());
        if (playerdata == null) {
            player.kickPlayer ("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerdata.setBanTime();
            playerdata.setBanDate();
            PlayerDatabase.addPlayerDataToDatabase (playerdata);
        }
    }

    public static void setPlayerTimePlayed(Player player, int new_segundos, int new_minutos, int new_horas) {
        PlayerData playerdata = PlayerDatabase.getPlayerDataFromDatabase (player.getName ());
        if (playerdata == null) {
            player.kickPlayer ("Error al cargar tus datos, contacta con un administrador.");
        } else {

            String[] partes = playerdata.getTimePlayed().split("\\s+");

            int segundos = 0, minutos = 0, horas = 0;

            for (String parte : partes) {
                if (parte.endsWith("s")) {
                    segundos = Integer.parseInt(parte.substring(0, parte.length() - 1)); // Convertir los segundos a entero
                } else if (parte.endsWith("m")) {
                    minutos = Integer.parseInt(parte.substring(0, parte.length() - 1));// Convertir los minutos a entero
                } else if (parte.endsWith("h")) {
                    horas = Integer.parseInt(parte.substring(0, parte.length() - 1));// Convertir las horas a entero
                }
            }

            segundos += new_segundos;
            if (segundos >= 60) {
                new_minutos += segundos / 60;
                segundos = segundos % 60;
            }
            minutos += new_minutos;
            if (minutos >= 60) {
                new_horas += minutos / 60;
                minutos = minutos % 60;
            }
            horas += new_horas;

            playerdata.setTimePlayed(horas + "h " + minutos + "m " + segundos + "s");
            PlayerDatabase.addPlayerDataToDatabase(playerdata);
        }
    }

    public static void setPlayerCoords(Player player) {
        PlayerData playerdata = PlayerDatabase.getPlayerDataFromDatabase (player.getName ());
        if (playerdata == null) {
            player.kickPlayer ("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerdata.setCoords(player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ());
            playerdata.setDimension(player.getWorld().getName());
            PlayerDatabase.addPlayerDataToDatabase (playerdata);
        }
    }

    public static void setPlayerCoords(String position, String playerName) {
        PlayerData playerdata = PlayerDatabase.getPlayerDataFromDatabase (playerName);
        Player player = Bukkit.getPlayer(playerName);

        if (playerdata == null) {
            Objects.requireNonNull(player).kickPlayer ("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerdata.setCoords(position);
            assert player != null;
            playerdata.setDimension(player.getWorld().getName());
            PlayerDatabase.addPlayerDataToDatabase (playerdata);
        }
    }

    public static void setPlayerReSpawnCoords(String position, String playerName) {
        PlayerData playerdata = PlayerDatabase.getPlayerDataFromDatabase (playerName);

        if (playerdata == null) {
            Objects.requireNonNull(Bukkit.getPlayer(playerName)).kickPlayer ("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerdata.setCoords(position);
            playerdata.setDimension(Objects.requireNonNull(Bukkit.getWorld("world")).getName());
            PlayerDatabase.addPlayerDataToDatabase (playerdata);
        }
    }

    public static void UnbanPlayer(String player_name) {
        PlayerData playerdata = PlayerDatabase.getPlayerDataFromDatabase (player_name);
        if (playerdata != null) {
            playerdata.setDead(false);
            playerdata.setBanDate("0");
            playerdata.setBantime("0");
            playerdata.setHealth(20);
            PlayerDatabase.addPlayerDataToDatabase (playerdata);
        }
    }

    public static boolean addPlayerToGroup(String player, String group) {
        GroupData groupData = GroupDatabase.getGroupData(group.toLowerCase());
        if (groupData == null) {
            return false;
        }
        if (groupData.getPlayers().contains(player)) {
            return false;
        }
        removePlayerFromGroup(player);
        groupData.addPlayer(player);
        GroupDatabase.addGroupData(groupData);
        PlayerDatabase.setPlayerGroup(player, group);
        return true;
    }

    public static void removePlayerFromGroup(String player) {
        GroupData groupData = GroupDatabase.getGroupData(Objects.requireNonNull(PlayerDatabase.getPlayerDataFromDatabase(player)).getGroup());
        assert groupData != null;
        if (!groupData.getPlayers().contains(player)) {
            return;
        }
        groupData.removePlayer(player);
        GroupDatabase.addGroupData(groupData);
    }

    public static Location getPlayerLocation(String name) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(name);
        if (playerData == null) {
            return null;
        }
        String[] coords = playerData.getCoords().split(",");
        World world = playerData.getDimension();
        return new Location(world, Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
    }

    public static void setPlayerHealth(Player player) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData == null) {
            player.kickPlayer("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerData.setHealth(player.getHealth());
            PlayerDatabase.addPlayerDataToDatabase(playerData);
        }

    }

    public static boolean isPlayerBanned(String targetPlayer) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(targetPlayer);
        if (playerData == null) {
            return false;
        }
        return playerData.isDead();
    }
}
