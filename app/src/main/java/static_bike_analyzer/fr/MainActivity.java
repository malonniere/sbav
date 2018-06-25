package static_bike_analyzer.fr;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
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
	private Button buttonSerialSend;
	private EditText serialSendText;
	private TextView serialReceivedText;
	//private SpeedView speedView;
	private ImageSpeedometer speedView;
	private GraphView graph;
	private LineGraphSeries<DataPoint> series;
	private LineGraphSeries<DataPoint> series2;
	private double graph2LastXValue ;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        onCreateProcess();														//onCreate Process by BlunoLibrary


        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200

        serialReceivedText=(TextView) findViewById(R.id.editText2);	//initial the EditText of the received data
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
		graph.getViewport().setMaxY(150);
		graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
		series = new LineGraphSeries<>();
        series2 = new LineGraphSeries<>();
		series.setDrawBackground(true);
		//series.setBackgroundColor((Color.argb(100, 255, 218, 172)));
		graph.addSeries(series);
		/*series2 = new LineGraphSeries<>(new DataPoint[]{
				new DataPoint(0,1),
				new DataPoint(10,2),
				new DataPoint(30,50),
				new DataPoint(350,50),
				new DataPoint(600,60)
		});*/
		series2.setDrawBackground(true);
		series2.setOntop(true);
		series2.setColor((Color.argb(255, 255, 0, 0)));
		series2.setBackgroundColor((Color.argb(100, 255, 0, 0)));
		graph.addSeries(series2);
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

       buttonScanOnClickProcess();//starting auto?
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
        buttonScanOnClickProcess();//VB 23/06
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
          		buttonScanOnClickProcess();//VB 15/06 pour tentative de reconnexion automatique
			}

//			mConnectionState=connectionStateEnum.isScanning;
//			onConnectionStateChange(mConnectionState);
//			scanLeDevice(true);
//			mScanDeviceDialog.show();
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

			graph2LastXValue += 1d;
			series.appendData(new DataPoint(graph2LastXValue, v), true, 400);
			series2.appendData(new DataPoint(graph2LastXValue, v+50), true, 400);
		}catch(NumberFormatException e)
		{
			System.out.println("La chaine de caractères n'est pas un nombre parsable!!");
		}
		;

		//The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
		//((ScrollView)serialReceivedText.getParent()).fullScroll(View.FOCUS_DOWN);
	}



}