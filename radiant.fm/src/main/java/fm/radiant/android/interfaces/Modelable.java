package fm.radiant.android.interfaces;

public abstract class Modelable {
    private int id;
    private String createdAt;
    private String updatedAt;

    public Integer getId() {
        return id;
    }

    public String getStringId() {
        return String.valueOf(id);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return getClass() == object.getClass() && hashCode() == object.hashCode() && version().equals(((Modelable) object).version());
    }

    protected String version() {
        return updatedAt == null ? createdAt : updatedAt;
    }
}