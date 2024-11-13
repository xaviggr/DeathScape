package dc.Business.groups;


public enum Permission {
    BAN,
    KICK,
    TELEPORT,
    RELOAD,
    REPORTS,
    FLY,
    CHAT,
    HEAL,
    NULL;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}