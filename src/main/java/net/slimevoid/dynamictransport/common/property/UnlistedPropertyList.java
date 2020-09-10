package net.slimevoid.dynamictransport.common.property;

import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.List;

public class UnlistedPropertyList<T> implements IUnlistedProperty<List<T>> {

    private final String name;

    public UnlistedPropertyList(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(List<T> value) {
        return true;
    }

    @Override
    public Class<List<T>> getType() {
        return (Class<List<T>>)(Class<?>)List.class;
    }

    @Override
    public String valueToString(List<T> value) {
        return value.toString();
    }
}