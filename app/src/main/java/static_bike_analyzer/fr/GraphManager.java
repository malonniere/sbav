package static_bike_analyzer.fr;

/**
 * Created by clement_besnier on 11/07/2018.
 */

public class GraphManager {
    private double a;
    private double b;

    public GraphManager()
    {

    }
    void set_f(double a, double b)
    {
        this.a = a;
        this.b = b;
    }
    double f(double x)
    {
        return a*x+b%150;
    }
}
