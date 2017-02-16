package kotowari.scaffold.model;

/**
 * @author kawasima
 */
public class EntityField {
    private String name;
    private Class<?> type;
    private boolean id;

    public EntityField(String name, Class<?> type, boolean id) {
        this.name = name;
        this.type = type;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isId() {
        return id;
    }

    @Override
    public String toString() {
        return "EntityField{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", id=" + id +
                '}';
    }
}
