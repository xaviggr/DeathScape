package dc.Business.groups;

/**
 * Represents the set of permissions that can be assigned to a group or player.
 */
public enum Permission {

    /**
     * Permission to manage groups.
     */
    GROUP,

    /**
     * Permission to ban players.
     */
    BAN,

    /**
     * Permission to kick players from the server.
     */
    KICK,

    /**
     * Permission to teleport players.
     */
    TELEPORT,

    /**
     * Permission to reload the server or configurations.
     */
    RELOAD,

    /**
     * Permission to manage or view reports.
     */
    REPORTS,

    /**
     * Permission to activate or use Banshee abilities.
     */
    BANSHEE,

    /**
     * Permission to manage chat settings or moderate chat.
     */
    CHAT,

    /**
     * Permission to heal players.
     */
    HEAL,

    /**
     * Permission to view or manage server days.
     */
    DAYS,

    /**
     * Permission to mute players in chat.
     */
    MUTE;

    /**
     * Converts the permission name to a lowercase string for display or storage purposes.
     *
     * @return The lowercase representation of the permission name.
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
