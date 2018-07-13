package static_bike_analyzer.fr;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;

/**
 * Created by clement_besnier on 11/07/2018.
 */

public class GraphManager {
    private double a;
    private double b;
    private ArrayList<ArrayList<DataPoint>> lldp = new ArrayList<>();

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
        return a*x+b%40;
    }

    void setSeries(ArrayList<DataPoint> ldp)
    {
        this.lldp.add(ldp);
    }

    double getExtrapolation(double x, int i)
    {
        double xBefore, yBefore, xAfter, yAfter, m = 0, p = 0;

        ArrayList<DataPoint> ldp = lldp.get(i);
        if(x >= ldp.get(ldp.size()-1).getX())
        {
            return ldp.get(ldp.size()-1).getY();
        }
        for(int j = 0; i < ldp.size()-1 ; j ++)
        {
            if(ldp.get(j).getX() == x)
            {
                return ldp.get(j).getY();
            }
            Log.d("GM", "x avant : "+ldp.get(j).getX()+" x après : " + ldp.get(j+1).getX());
            if((ldp.get(j).getX() <= x) && (x <= ldp.get(j + 1).getX()) && ldp.get(j).getX() != ldp.get(j + 1).getX())
            {
                xBefore = ldp.get(j).getX();
                yBefore = ldp.get(j).getY();
                xAfter = ldp.get(j + 1).getX();
                yAfter = ldp.get(j + 1).getY();
                Log.d("GM", "ok");

                m = (yAfter - yBefore)/(xAfter - xBefore);
                p = yBefore - m*xBefore;
                break;
            }
        }

        return m*x+p;
    }

    void example()
    {
        ArrayList<DataPoint> ldp = new ArrayList<DataPoint>();
        ldp.add(new DataPoint(0, 4));
        ldp.add(new DataPoint(2, 6));
        ldp.add(new DataPoint(7, 14));
        ldp.add(new DataPoint(8, 7));
        ldp.add(new DataPoint(16, 16));
        setSeries(ldp);
        Log.d("GM extrapolation", "valeur : "+getExtrapolation(3, 0));
        Log.d("GM extrapolation", "valeur : "+getExtrapolation(3.5, 0));
        Log.d("GM extrapolation", "valeur : "+getExtrapolation(4, 0));


    }



}
