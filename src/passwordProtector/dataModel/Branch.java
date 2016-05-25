package passwordProtector.dataModel;

import extfx.util.HierarchyData;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Iterator;
import java.util.Objects;

/**
 * Created by Administrator on 31.12.2015.
 */
public class Branch implements HierarchyData<Branch>, Comparable<Branch> {
    private StringProperty name;
    private final ObservableList<Branch> children = FXCollections.observableArrayList();

    public Branch (String name){
        this.name = new SimpleStringProperty(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.getValue();
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    public void setChildren (ObservableList<Branch> children){
        this.children.setAll(children);
    }

    @Override
    public ObservableList<Branch> getChildren() {
        return children;
    }

    public void addChild (Branch branch){
        this.children.add(branch);
    }

    public void removeChild (String name){
        for (Iterator<Branch> iterator = children.iterator(); iterator.hasNext();) {
            Branch branch = iterator.next();
            if (branch.getName().equals(name)) {
                // Remove the current element from the iterator and the list.
                iterator.remove();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Branch other = (Branch) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
    
    @Override
     public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Branch o) {
        return getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }
}
