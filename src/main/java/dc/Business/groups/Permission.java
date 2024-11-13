package dc.Business.groups;


public enum Permission {
    BAN,
    KICK,
    TELEPORT,
    BUILD,
    REPORTS,
    CHAT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}