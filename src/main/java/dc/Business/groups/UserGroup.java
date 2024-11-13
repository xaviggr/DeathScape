package dc.Business.groups;

public class UserGroup extends GroupData {

    private String color;

    public UserGroup(String name, String prefix, String color) {
        this.name = name;
        this.prefix = prefix;
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
