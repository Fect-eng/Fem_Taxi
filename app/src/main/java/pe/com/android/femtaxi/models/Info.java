package pe.com.android.femtaxi.models;

import androidx.annotation.Keep;

@Keep
public class Info {
    double km;
    double min;

    public Info() {
    }

    public Info(double km, double min) {
        this.km = km;
        this.min = min;
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    @Override
    public String toString() {
        return "Info{" +
                "km=" + km +
                ", min=" + min +
                '}';
    }
}
