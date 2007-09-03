package com.sun.javafx.runtime.location;

/**
 * Wrapper class that creates an ObjectLocation<Integer> view of an IntLocation
 *
 * @author Brian Goetz
 */
class IntObjectLocation implements ObjectLocation<Integer> {
    private final IntLocation location;

    public IntObjectLocation(IntLocation location) {
        this.location = location;
    }

    public Integer get() {
        return location.get();
    }

    public void set(Integer value) {
        location.set(value);
    }

    public boolean isValid() {
        return location.isValid();
    }

    public boolean isLazy() {
        return location.isLazy();
    }

    public void invalidate() {
        location.invalidate();
    }

    public void update() {
        location.update();
    }

    public void addChangeListener(ChangeListener listener) {
        location.addChangeListener(listener);
    }

    public void addWeakListener(ChangeListener listener) {
        location.addWeakListener(listener);
    }

    public ChangeListener getWeakChangeListener() {
        return location.getWeakChangeListener();
    }
}

class IntObjectMutableLocation extends IntObjectLocation implements MutableLocation {
    public IntObjectMutableLocation(IntLocation location) {
        super(location);
    }
}
