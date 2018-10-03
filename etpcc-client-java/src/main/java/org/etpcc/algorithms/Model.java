package org.etpcc.algorithms;

/**
 * Created by liangjiao on 17/10/19.
 */
public class Model implements Comparable<Model>{
    private String name;
    private String type;
    private int id;

    public Model(String name, String type, int id) {
        this.name = name;
        this.type = type;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Model model = (Model) o;

        return name.equals(model.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


    @Override
    public int compareTo(Model o) {
        return this.getName().compareTo(o.getName());
    }
}
