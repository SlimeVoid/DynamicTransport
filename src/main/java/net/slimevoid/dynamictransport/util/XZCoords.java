package net.slimevoid.dynamictransport.util;

public class XZCoords {
    public int x;
    public int z;

    public XZCoords(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {

        return o instanceof XZCoords && this.x == ((XZCoords) o).x
               && this.z == ((XZCoords) o).z;

    }

}
