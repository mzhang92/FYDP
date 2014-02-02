package com.example.BTManager.local;

import com.android.internal.telephony.ITelephony;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.app.SearchManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.KeyEvent;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class LocalWordService extends Service implements TextToSpeech.OnInitListener{
  private final IBinder mBinder = new MyBinder();
  private ArrayList<String> list = new ArrayList<String>();
  
 // Handler h;
  
  String current_command = "0";
  
  final int RECIEVE_MESSAGE = 1;		// Status  for Handler
  private BluetoothAdapter btAdapter = null;
  private BluetoothSocket btSocket = null;
  
  
  private StringBuilder sb = new StringBuilder();
  private ConnectedThread mConnectedThread;
  
  private static final String TAG = "Bluetooth Service Test";
  
  // SPP UUID service
  private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 
  // MAC-address of Bluetooth module (you must edit this line)
  private static String address = "00:13:01:04:16:55";
  
  private TextToSpeech tts;
  
  
  @Override 
  public void onCreate() {
  

  }
  
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    btAdapter = BluetoothAdapter.getDefaultAdapter();		// get Bluetooth adapter
    Log.d(TAG, "...Checking for Bluetooth...");
    checkBTState();
  
    BluetoothDevice device = btAdapter.getRemoteDevice(address);
    
    tts = new TextToSpeech(this, this);

	try {
		btSocket = createBluetoothSocket(device);
	} catch (IOException e) {
		errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
	}
	
    btAdapter.cancelDiscovery();
    
    Log.d(TAG, "...Connecting...");
    try {
      btSocket.connect();
      Log.d(TAG, "....Connection ok...");
      //speakOut("Service successfully started");
    } catch (IOException e) {
      try {
        btSocket.close();
      } catch (IOException e2) {
        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
      }
    }
    
    // Create a data stream so we can talk to server.
    Log.d(TAG, "...Create Socket...");
	  
    mConnectedThread = new ConnectedThread(btSocket);
	mConnectedThread.start();
	
    return Service.START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return mBinder;
  }

  public class MyBinder extends Binder {
    LocalWordService getService() {
      return LocalWordService.this;
    }
  }

  public List<String> getWordList() {
    return list;
  }



private void checkBTState() {
    // Check for Bluetooth support and then check to make sure it is turned on
    // Emulator doesn't support Bluetooth and will return null
	
    if(btAdapter==null) { 
      errorExit("Fatal Error", "Bluetooth not support");
    } else {
      if (btAdapter.isEnabled()) {
        Log.d(TAG, "...Bluetooth ON...");
      } else {
        //Prompt user to turn on Bluetooth
        //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //startActivityForResult(enableBtIntent, 1);
    	  
    	  errorExit("Fatal Error", "Please turn on Bluetooth!");
      }
    }
  }

private void errorExit(String title, String message){
    Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
    this.stopSelf();
  }



private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
    if(Build.VERSION.SDK_INT >= 10){
        try {
            final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
            return (BluetoothSocket) m.invoke(device, MY_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
    }
    return  device.createRfcommSocketToServiceRecord(MY_UUID);
	}

private class ConnectedThread extends Thread {
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
	private final InputStream mmInStream;
    private final OutputStream mmOutStream;
 
    public ConnectedThread(BluetoothSocket socket) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
 
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }
 
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }
 
    public void run() {
        byte[] buffer = new byte[256];  // buffer store for the stream
        int bytes; // bytes returned from read()
    	Context context = getBaseContext();
    	TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        // Keep listening to the InputStream until an exception occurs
    	int prev_state = 0;
    	int state = 0;
        while (true) {
        	
        	Log.d(TAG, "Current Command: " + current_command);
        	//current_command = "238"
        	prev_state = state;
        	state = tm.getCallState(); 

        	if (state != prev_state){
        		
        		write(state);
        		if ( state == 1) {
        			speakOut("Incoming Call");			
        		}
        	}
        		

        	try {
                // Read from the InputStream
        		if(mmInStream.available() > 0) {
	                bytes = mmInStream.read(buffer);		// Get number of bytes and message in "buffer"
	                //h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();		// Send to message queue Handler
	                
	                //Log.d(TAG, "data is " + bytes);
	            	String strIncom = new String(buffer, 0, bytes);					// create string from bytes array
	            	sb.append(strIncom);												// append string
	            	int endOfLineIndex = sb.indexOf("\n");							// determine the end-of-line
	            	String test = sb.toString();
	            	if (endOfLineIndex > 0) { 											// if end-of-line,
	            		String sbprint = sb.substring(0, endOfLineIndex);				// extract string
	                    sb.delete(0, sbprint.length()+ 4);										// and clear
	                    current_command = sbprint;
	                    //Log.d(TAG, "in IH " + test);
	                } else if(endOfLineIndex == 0) {
	                	sb.delete(0, 1);	
	                }
        		}
            	
            } catch (IOException e) {
            	Log.d(TAG, "Nothing to read");
                continue;
            }
   
        	
      	
        	
        	// Re-factor to avoid code duplication for states 1 and 2 if when this thing works
        	if (state == 0 && Integer.parseInt(current_command) == 238)
        	{
            	current_command = "0";
        		MakeCall();
        	}
        	else if (state == 1 && Integer.parseInt(current_command) == 204)
        	{
            	current_command = "0";
        		// Accept call
            	speakOut("Accepting Call");
        		try
        		{
                	Class c = Class.forName(tm.getClass().getName());
                	Method m = c.getDeclaredMethod("getITelephony");
         		   	m.setAccessible(true);
                	ITelephony telephonyService = (ITelephony) m.invoke(tm);      	
                	//telephonyService.answerRingingCall();
                    //Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);             
                    //buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
                    //context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

                    // froyo and beyond trigger on buttonUp instead of buttonDown
                    Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);               
                    buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
                    context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
        		}
        		catch (Exception e)
        		{
        			e.printStackTrace();
                    // Simulate a press of the headset button to pick up the call

        		}
        	}
        	else if (state == 1 && Integer.parseInt(current_command) == 221)
        	{
            	current_command = "0";
        		// Reject call
            	speakOut("Rejecting Call");
        		try
        		{
                	Class c = Class.forName(tm.getClass().getName());
                	Method m = c.getDeclaredMethod("getITelephony");
         		   	m.setAccessible(true);
                	ITelephony telephonyService = (ITelephony) m.invoke(tm);      	
                	telephonyService.endCall();
        		}
        		catch (Exception e)
        		{
        			e.printStackTrace();	 			
        		}
        	}
        	else if (state == 2 && Integer.parseInt(current_command) == 221)
        	{
            	current_command = "0";
        		// Ending call
            	speakOut("Ending Call");
        		try
        		{
                	Class c = Class.forName(tm.getClass().getName());
                	Method m = c.getDeclaredMethod("getITelephony");
         		   	m.setAccessible(true);
                	ITelephony telephonyService = (ITelephony) m.invoke(tm);      	
                	telephonyService.endCall();
        		}
        		catch (Exception e)
        		{
        			e.printStackTrace();	 			
        		}
        	}
        	
//        	switch (Integer.parseInt(current_command)) {
//        		case 170: 
//        			break;
//        		case 187:
//        			break;
//        		case 204: up
//        			break;
//        		case 221: down
//        			break;
//        		case 238:
//        			//function call to launch voice recognition
//        			MakeCall();
//        			break;
//        	}
        	
        	
        }
    }
 
    private void MakeCall() {		    	
	 	Intent dlgIntent = new Intent(getBaseContext(), CallActivity.class);
	 	dlgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	 	getApplication().startActivity(dlgIntent);
	}
    
    public int PhoneState()
    {
    	TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    	int callState = tm.getCallState(); 
    	
    	return callState;
    }
    
    /*public class CallReceiver extends BroadcastReceiver {
    	
    	Context context = null;
    	private static final String TAG = "Phone call";
    	private ITelephony telephonyService;
    	
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		Log.v(TAG, "Receiving...");
    		TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    			
    		try {
    			Class c = Class.forName(telephony.getClass().getName());
    			 Method m = c.getDeclaredMethod("getITelephony");
    			   m.setAccessible(true);
    			   telephonyService = (ITelephony) m.invoke(telephony);
    			   //telephonyService.endCall();
    			   telephonyService.answerRingingCall();
    		} catch (Exception e) {			
    			e.printStackTrace();						
    		}
    	
    	}
    }*/
    
	/* Call this from the main activity to send data to the remote device */
    public void write(String message) {
    	Log.d(TAG, "...Data to send: " + message + "...");
    	byte[] msgBuffer = message.getBytes();
    	//Log.d(TAG, msgBuffer);
    	try {
            mmOutStream.write(msgBuffer);
        } catch (IOException e) {
            Log.d(TAG, "...Error data send: " + e.getMessage() + "...");     
          }
    }
    
    public void write(int message) {
    	Log.d(TAG, "...Data to send: " + message + "...");
    	byte[] msgBuffer = ("" + message).getBytes();
    	//Log.d(TAG, msgBuffer);
    	try {
            mmOutStream.write(msgBuffer);
        } catch (IOException e) {
            Log.d(TAG, "...Error data send: " + e.getMessage() + "...");     
          }
    }
}

@Override
public void onInit(int status) {
	if (status == TextToSpeech.SUCCESS)
		{
			int result = tts.setLanguage(Locale.CANADA);
			speakOut("Service successfully launched");
		}
		else {
			Log.v(TAG, "Could not initiate tts");
		}
	
}

public void speakOut(String msg)
{
	tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
}

}