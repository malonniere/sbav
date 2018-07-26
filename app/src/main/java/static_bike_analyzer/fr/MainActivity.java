package static_bike_analyzer.fr;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.anastr.speedviewlib.ImageSpeedometer;
import com.github.anastr.speedviewlib.SpeedView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;

import java.util.Timer;
import java.util.TimerTask;
import java.util.*;

import static_bike_analyzer.fr.LineGraphSeries;

import static_bike_analyzer.fr.R;
import static_bike_analyzer.fr.BlunoLibrary;




public class MainActivity  extends BlunoLibrary {
    private static final int REQUEST_BLUETOOTH = 1;
    private static final int REQUEST_BLUETOOTH_ADMIN = 1;
    private static final int REQUEST_BLUETOOTH_PRIVIILEDGED = 1;
    private static String[] PERMISSIONS_BLUETOOTH = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
	private Button buttonScan;
	private Button buttonRz;
	private Button buttonSerialSend;
	private EditText serialSendText;
	private TextView serialReceivedText;
	private TextView maxView;
	private ImageSpeedometer speedView;
	private GraphView graph;
	private LineGraphSeries<DataPoint> series;
	private LineGraphSeries<DataPoint> series2;
    private LineGraphSeries<DataPoint> series3;
	private double graph2LastXValue ;
	private double vRevive;
	private double vMax=0;
	private String TAG = "MAin";

	private GraphManager gm;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        onCreateProcess();														//onCreate Process by BlunoLibrary

        ArrayList<DataPoint> ldp = new ArrayList<DataPoint>();
        ldp.add(new DataPoint(0, 0));
        ldp.add(new DataPoint(200, 15));
        ldp.add(new DataPoint(400, 25));
        ldp.add(new DataPoint(1200, 15));
        ldp.add(new DataPoint(1500, 35));
        gm = new GraphManager();
        gm.setSeries(ldp);



//		gm.set_f(0.5, 15);
//		gm.example();

        // creating timer task, timer
        TimerTask tache = new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG, "working at fixed rate delay");
                    graph2LastXValue += 1d;
			        double v ;
			        //v=Math.sin(Math.toRadians(graph2LastXValue))*50+50;
					v=gm.getExtrapolation(graph2LastXValue, 0);
					try {
						series3.appendData(new DataPoint(graph2LastXValue, vRevive), false,400,true);
						series2.appendData(new DataPoint(graph2LastXValue, v + 20), false, 400,true);
						series.appendData(new DataPoint(graph2LastXValue, v), true, 400,false);
					}catch(Exception e){
						Log.d(TAG, "GraphExeption"+e.getMessage());
					}
                }
            };
        Timer timere = new Timer();
        timere.schedule(tache, 500L, 100L);


        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200
		maxView=(TextView) findViewById(R.id.maxSpeed);
        serialReceivedText=(TextView) findViewById(R.id.editText2);	            //initial the EditText of the received data
        serialSendText=(EditText) findViewById(R.id.serialSendText);			//initial the EditText of the sending data
		//speedView = (SpeedView) findViewById(R.id.speedView);
        speedView = (ImageSpeedometer) findViewById(R.id.imageSpeedometer);

        graph = (GraphView) findViewById(R.id.graph);
		//ne marche pas
		graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
        {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return super.formatLabel(value/10, true)+"s";
                } else {
                    // show currency for y values
                    return super.formatLabel(value, false);
                }
            }
        });
        graph.getViewport().setXAxisBoundsManual(true);
		graph.getViewport().setYAxisBoundsManual(true);
		graph.getViewport().setMinX(0);
		graph.getViewport().setMaxX(400);
        graph.getViewport().setMaxXAxisSize(400);
		graph.getViewport().setMinY(0);
		graph.getViewport().setMaxY(60);
		graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
		series = new LineGraphSeries<>();
        series2 = new LineGraphSeries<>();
        series3 = new LineGraphSeries<>();
		series.setDrawBackground(true);
		//series.setBackgroundColor((Color.argb(100, 255, 218, 172)));
		graph.addSeries(series);
		series2.setDrawBackground(true);
		series2.setOntop(true);
		series2.setColor((Color.argb(255, 255, 0, 0)));
		series2.setBackgroundColor((Color.argb(50, 255, 0, 0)));
		graph.addSeries(series2);
        series3.setBackgroundColor((Color.argb(100, 0, 255, 0)));
        graph.addSeries(series3);

		graph2LastXValue = 4d;


        buttonSerialSend = (Button) findViewById(R.id.buttonSerialSend);		//initial the button for sending the data
        buttonSerialSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				serialSend(serialSendText.getText().toString());				//send the data to the BLUNO
			}
		});


        buttonScan = (Button) findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
        buttonScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				buttonScanOnClickProcess();										//Alert Dialog for selecting the BLE device
			}
		});
		buttonRz = (Button) findViewById(R.id.buttonRz);						//initial the RZ button
		buttonRz.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				series.resetData(new DataPoint[] {});
				series2.resetData(new DataPoint[] {});
				series3.resetData(new DataPoint[] {});
				graph2LastXValue=0;
				vMax=0;
				maxView.setText(String.valueOf(vMax)+"Km/h");
			}
		});
	}

	public static boolean isActivityVisible() {
		if (activityVisible) return true;
		else return false;
	}
	private static boolean activityVisible;

	protected void onResume(){
        activityVisible = true;
	    super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess(); //onResume Process by BlunoLibrary
        //buttonScanOnClickProcess();//VB 23/06 supprime le 19/07
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    protected void onPause() {
        activityVisible = false;
	    super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }
	
	protected void onStop() {
        activityVisible = false;
	    super.onStop();
		onStopProcess();														//onStop Process by BlunoLibrary
	}
    
	@Override
    protected void onDestroy() {
        activityVisible = false;
	    super.onDestroy();
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

	@Override
	public void onConnectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
		switch (theConnectionState) {											//Four connection state
		case isConnected:
			buttonScan.setText("Connected");
			break;
		case isConnecting:
			buttonScan.setText("Connecting");
			break;
		case isToScan:
			buttonScan.setText("Reconnecter ");
          	if (isActivityVisible()){
          		//buttonScanOnClickProcess();//VB 15/06 pour tentative de reconnexion automatique
			}

//			mConnectionState=connectionStateEnum.isScanning;//remis en place 21/07
//			onConnectionStateChange(mConnectionState);//
//			scanLeDevice(true);//
//			mScanDeviceDialog.show();//
			break;
		case isScanning:
			buttonScan.setText("Scanning");
			break;
		case isDisconnecting:
			buttonScan.setText("isDisconnecting");
			break;
		default:
			break;
		}
	}

	@Override
	public void onSerialReceived(String theString) {							//Once connection data received, this function will be called

		// TODO Auto-generated method stub
		float v;
		serialReceivedText.setText(theString);							//append the text into the EditText

		try
		{
			v = Float.parseFloat(theString);
			//System.out.println (num);
			//SpeedView speedView = (SpeedView) findViewById(R.id.speedView);
			speedView.setWithTremble(false);
			speedView.speedTo(v,0);
			vRevive=v;
			if (v>vMax){
				vMax=v;
				maxView.setText(String.valueOf(vMax)+"Km/h");
			}
//			graph2LastXValue += 1d;
//			series.appendData(new DataPoint(graph2LastXValue, v), true, 400);
//			series2.appendData(new DataPoint(graph2LastXValue, v+50), true, 400);
		}catch(NumberFormatException e)
		{
			System.out.println("La chaine de caractères n'est pas un nombre parsable!!");
		}
		;

		//The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
		//((ScrollView)serialReceivedText.getParent()).fullScroll(View.FOCUS_DOWN);
	}

//    new Timer().scheduleAtFixedRate(new TimerTask(){
//        @Override
//        public void run(){
//            System.out.println("A Kiss every 5 seconds");
//        }
//    },0,5000);


}