public class jfxc3621sub {
    private static int instance = 0;

    private int instNum;
    private int a;

    jfxc3621sub(int a) {
        this.a = a;
        instNum = ++instance;
        System.out.println("[ constructed instance #" + instNum);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof jfxc3621sub) {
            return ((jfxc3621sub) o).a == this.a;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return a;
    }

    @Override
    public String toString() {
        return "#" + instNum + " : a=" + a;
    }
}
