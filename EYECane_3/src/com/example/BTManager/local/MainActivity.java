package com.example.BTManager.local;

import java.util.ArrayList;
import java.util.List;

import com.example.BTManager.local.R;

//import android.R;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ListActivity {
  private LocalWordService s;

  
/** Called when the activity is first created. */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    wordList = new ArrayList<String>();
    adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, android.R.id.text1,
        wordList);
    setListAdapter(adapter);
    //startService(new Intent(this, LocalWordService.class));
  }

  @Override
  protected void onResume() {
    super.onPause();
    bindService(new Intent(this, LocalWordService.class), mConnection,
        Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onPause() {
    super.onPause();
    unbindService(mConnection);
  }


  private ServiceConnection mConnection = new ServiceConnection() {

    public void onServiceConnected(ComponentName className, IBinder binder) {
      s = ((LocalWordService.MyBinder) binder).getService();
      Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT)
          .show();
    }

    public void onServiceDisconnected(ComponentName className) {
      s = null;
    }
  };
  private ArrayAdapter<String> adapter;
  private List<String> wordList;

  public void showServiceData(View view) {
    if (s != null) {

      Toast.makeText(this, "Number of elements " + s.getWordList().size(),
          Toast.LENGTH_SHORT).show();
      wordList.clear();
      wordList.addAll(s.getWordList());
      adapter.notifyDataSetChanged();
    }
  }
  
  public void StartService(View view) {
	    startService(new Intent(this, LocalWordService.class));
	    final Button button = (Button) findViewById(R.id.button1);
	    button.setEnabled(false);
}
  

}