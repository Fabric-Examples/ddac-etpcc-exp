package org.etpcc.algorithms;

import java.util.HashSet;

/**
 * Created by liangjiao on 17/10/22.
 */
public class Entity {
    private String name;
    private String type;
    private HashSet<Model> modelSet;

    public Entity(String name, String type, HashSet<Model> modelSet) {
        this.name = name;
        this.type = type;
        this.modelSet = modelSet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HashSet<Model> getModelSet() {
        return modelSet;
    }

    public void setModelSet(HashSet<Model> modelSet) {
        this.modelSet = modelSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        return name.equals(entity.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
