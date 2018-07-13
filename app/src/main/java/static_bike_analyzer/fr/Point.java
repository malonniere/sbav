package static_bike_analyzer.fr;

/**
 * Created by clement_besnier on 13/07/2018.
 */

public class Point {

    private final int x;
    private final int y;

    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    int getX()
    {
        return x;
    }

    int getY()
    {
        return y;
    }
}
