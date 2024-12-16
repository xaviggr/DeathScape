package dc.Business.groups;


public enum Permission {
    GROUP,
    BAN,
    KICK,
    TELEPORT,
    RELOAD,
    REPORTS,
    BANSHEE,
    CHAT,
    HEAL,
    DAYS,
    MUTE;


    @Override
    public String toString() {
        return name().toLowerCase();
    }
}