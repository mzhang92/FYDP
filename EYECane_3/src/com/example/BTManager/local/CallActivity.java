package com.example.BTManager.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.example.BTManager.local.R;
 
import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
 
public class CallActivity extends Activity implements TextToSpeech.OnInitListener{
	 private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
	 
	 private EditText metTextHint;
	 private ListView mlvTextMatches;
	 private Spinner msTextMatches;
	 private Button mbtSpeak;
	 private TextToSpeech tts;
	 Uri SMS_INBOX = Uri.parse("content://sms/inbox");
	 ContentResolver resolver;
 
 @Override
 public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_main);
  
  tts = new TextToSpeech(this, this);
  //metTextHint = (EditText) findViewById(R.id.etTextHint);
  //mlvTextMatches = (ListView) findViewById(R.id.lvTextMatches);
  //msTextMatches = (Spinner) findViewById(R.id.sNoOfMatches);
  //mbtSpeak = (Button) findViewById(R.id.btSpeak);
  checkVoiceRecognition();  
 }
 
 public void checkVoiceRecognition() {
  // Check if voice recognition is present
  PackageManager pm = getPackageManager();
  List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
    RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
  if (activities.size() == 0) {
   mbtSpeak.setEnabled(false);
   mbtSpeak.setText("Voice recognizer not present");
   Toast.makeText(this, "Voice recognizer not present",
     Toast.LENGTH_SHORT).show();
  }
  else
  {
	  Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	  
	  // Specify the calling package to identify your application
	  intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
	    .getPackage().getName());
	 
	  // Display an hint to the user about what he should say.
//	  intent.putExtra(RecognizerIntent.EXTRA_PROMPT, metTextHint.getText()
	//    .toString());
	 
	  // Given an hint to the recognizer about what the user is going to say
	  //There are two form of language model available
	  //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
	  //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
	intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
	 
	  // If number of Matches is not selected then return show toast message
/*	  if (msTextMatches.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
	   Toast.makeText(this, "Please select No. of Matches from spinner",
	     Toast.LENGTH_SHORT).show();
	   return;
	  } */
	 
//	  int noOfMatches = Integer.parseInt(msTextMatches.getSelectedItem()
	//    .toString());
	  // Specify how many results you want to receive. The results will be
	  // sorted where the first result is the one with higher confidence.
	  intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
	  //Start the Voice recognizer activity for the result.
	  startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);	  
	  
  }
 }
 
 public void speak(View view) {
  Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 
  // Specify the calling package to identify your application
  intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
    .getPackage().getName());
 
  // Display an hint to the user about what he should say.
  intent.putExtra(RecognizerIntent.EXTRA_PROMPT, metTextHint.getText()
    .toString());
 
  // Given an hint to the recognizer about what the user is going to say
  //There are two form of language model available
  //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
  //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
 
  // If number of Matches is not selected then return show toast message
  if (msTextMatches.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
   Toast.makeText(this, "Please select No. of Matches from spinner",
     Toast.LENGTH_SHORT).show();
   return;
  }
 
  int noOfMatches = Integer.parseInt(msTextMatches.getSelectedItem()
    .toString());
  // Specify how many results you want to receive. The results will be
  // sorted where the first result is the one with higher confidence.
  intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, noOfMatches);
  //Start the Voice recognizer activity for the result.
  startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
 }
 
 @Override
 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
 
   //If Voice recognition is successful then it returns RESULT_OK
   if(resultCode == RESULT_OK) {
 
    ArrayList<String> textMatchList = data
    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 
    if (!textMatchList.isEmpty()) {
     // If first Match contains the 'search' word
     // Then start web search.
     if (textMatchList.get(0).contains("search")) {
 
        String searchQuery = textMatchList.get(0);
        searchQuery = searchQuery.replace("search","");
        Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
        search.putExtra(SearchManager.QUERY, searchQuery);
        startActivity(search);
     } 
     else if (textMatchList.get(0).contains("call")) {
		 String Contact = textMatchList.get(0);
	 	 Contact = Contact.replace("call ", "");
	 	 //must parse to capitalize the string?	
 	    
 	 	Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
 	 	String[] projection = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
 	 	                ContactsContract.CommonDataKinds.Phone.NUMBER};
 	 	//String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?";
 	    //String[] selectionArguments = { "Swapan Shah" }; 

 	 	Cursor people = getContentResolver().query(uri, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
 	 	
 	 	ArrayList<String> names = new ArrayList<String>();
 	 	ArrayList<String> numbers = new ArrayList<String>();
 	 	people.moveToFirst();
 	 	
 	 	while(!people.isAfterLast()) 
 	 	{
 	 		names.add(people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
 	 		numbers.add(people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
 	 		people.moveToNext(); 
 	 	}
 	 	
 	 	int index = Collections.binarySearch(names, Contact, String.CASE_INSENSITIVE_ORDER);
 	 	
 	 	if (index > 0)
 	 	{
 	 		Intent callIntent = new Intent(Intent.ACTION_CALL);
	 	    //callIntent.setData(Uri.parse("tel:5197293642"));
 	 		String number = numbers.get(index);
	 	    callIntent.setData(Uri.parse("tel:" + number));
	 	    startActivity(callIntent);
 	 	}
 	 	else
 	 	{
 	 		speakOut("Contact does not exist, please try again");
 	 	}
 
     }
     // if using maps
     
     else if (textMatchList.get(0).contains("direction"))
     {
    	 
     }
     
     // if playing music
     else if (textMatchList.get(0).contains("music"))
     {
    	 
     }
     
     // if text message 
     else if (textMatchList.get(0).contains("text"))
     {
    	 //may or may not actually implement this
	 	if (textMatchList.get(1).contains("read"))
	 	{
	 		//maybe add implementation for any new incoming message
	 		
	 		//check to see if there is any new messages
	 		checkAnyNew();
	 		//if user wants to reply, call same function as send with necessary info
	 	}
	 	else if (textMatchList.get(1).contains("send"))
	 	{
	 		//pull Contact information
	 		String Edit = textMatchList.get(0);
	 		Edit = Edit.replace("text send ", "");
	 		
	 		String[] Command = Edit.split(" ");
	 		String Contact = Command[0] + " " + Command[1];
	 		// refractor code from call and use it 
	 		
	 		//parse for message
	 		/*StringBuilder text = new StringBuilder();
	 		for (int i = 2; i < Command.length; i++)
	 		{
	 			text.append(Command[i] + " ");
	 		}
	 		String message = text.toString(); */
	 		
	 		String number = "519729985";
	 		String message = "Test";
	 		
	 		sendSMS(number, message);
	 	}
     }
    
     
     else {    	  
    	 speakOut("Invalid command, please try again");
         // populate the Matches
        /* mlvTextMatches.setAdapter(new ArrayAdapter<String>(this,
	        android.R.layout.simple_list_item_1,
	        textMatchList));    */   
     }
 
    }
   //Result code for various error.
   }else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
    showToastMessage("Audio Error");
   }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
    showToastMessage("Client Error");
   }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
    showToastMessage("Network Error");
   }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
    showToastMessage("No Match");
   }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
    showToastMessage("Server Error");
   }
  super.onActivityResult(requestCode, resultCode, data);
 }
 

	private void sendSMS(String number, String message) {
	// TODO Auto-generated method stub
	
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(number, null, message, null, null);
	}

	private void checkAnyNew() {
	// TODO Auto-generated method stub
		int unread = getMessageCountUnread();
		String message = getMessage(unread);
		
	}
	
	@SuppressWarnings("deprecation")
	public int getMessageCountUnread(){
	    Cursor c = resolver.query(SMS_INBOX, null, "read = 0", null, null);
	    int unreadMessagesCount = c.getCount();
	    c.deactivate();
	    return unreadMessagesCount;
	}
	
	public String getMessage(int messages) {
	      Cursor cur = resolver.query(SMS_INBOX, null, "read = 0", null,null);
	      String sms = "Message >> \n";
	      int end = 0;
	      while (cur.moveToNext()) {
	          sms += "From :" + cur.getString(2) + " : " + cur.getString(11)+"\n";
	          if(end == messages)
	              break;
	          end++;
	      }
	      return sms;
	}
	
	public void setMessageStatusRead() {
	    ContentValues values = new ContentValues();
	    values.put("read",true);
	    //resolver.update(SMS_INBOX,values, "_id="+SmsMessageId, null);
	}

	/**
	 * Helper method to show the toast message
	 **/
	 void showToastMessage(String message){
	  Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	 }
	 
	 /**
	 * Helper method to answer phone call
	 **/
	 void AnswerPhoneCall()
	 {
		Intent headSetUnPluggedintent = new Intent(Intent.ACTION_HEADSET_PLUG);
	    headSetUnPluggedintent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
	    headSetUnPluggedintent.putExtra("state", 0);
	    headSetUnPluggedintent.putExtra("name", "Headset");
	    try {
	        sendOrderedBroadcast(headSetUnPluggedintent, null);
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	    
	 }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onInit(int status) {
    	if (status == TextToSpeech.SUCCESS)
		{
			int result = tts.setLanguage(Locale.CANADA);
			//speakOut("Speech Ready");
		}    
    	
    }

    public void speakOut(String msg)
    {
    	tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
    }
    
}

