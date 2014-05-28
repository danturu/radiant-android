package fm.radiant.android.lib;

import com.google.gson.annotations.Expose;

public abstract class Model {
    @Expose
    private int id;

    @Expose
    private String createdAt;

    @Expose
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
        return getClass() == object.getClass() && hashCode() == object.hashCode() && version().equals(((Model) object).version());
    }

    protected String version() {
        return updatedAt == null ? createdAt : updatedAt;
    }
}