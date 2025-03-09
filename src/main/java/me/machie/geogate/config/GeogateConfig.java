package me.machie.geogate.config;

@SuppressWarnings("unused")
public class GeogateConfig {
    private int thing1 = 0;
    private int thing2 = 1;
    private float thing3 = 2.77f;

    public void setThing1(int thing1) {
        this.thing1 = thing1;
    }

    public void setThing2(int thing2) {
        this.thing2 = thing2;
    }

    public void setThing3(float thing3) {
        this.thing3 = thing3;
    }

    public int getThing1() {
        return thing1;
    }

    public int getThing2() {
        return thing2;
    }

    public float getThing3() {
        return thing3;
    }
}
