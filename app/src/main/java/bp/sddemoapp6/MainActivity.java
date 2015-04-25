package bp.sddemoapp6;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;


public class MainActivity extends ActionBarActivity {

    private Random r;
    private int goodId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        r = new Random();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void middleButtonOnClick(View v){
        Button b = (Button)v;
        b.setText("Clicked");
        int opt = r.nextInt(4);
        if(opt == 0){
            SetUpperLeftButton();
        }
        else if(opt == 1){
            SetUpperRightButton();
        }
        else if(opt == 2){
            SetLowerLeftButton();
        }
        else{
            SetLowerRightButton();
        }
    }

    public void outerButtonClick(View v){
        Button b = (Button)v;
        TextView tv = (TextView)findViewById(R.id.txtResult);
        if(b.getId() == goodId){
            tv.setText("Win!");
        }
        else{
            tv.setText("Lose");
        }
        b = (Button)findViewById(R.id.btnUpperLeft);
        SetButtonColor(b, Color.GRAY);
        b = (Button)findViewById(R.id.btnUpperRight);
        SetButtonColor(b, Color.GRAY);
        b = (Button)findViewById(R.id.btnLowerLeft);
        SetButtonColor(b, Color.GRAY);
        b = (Button)findViewById(R.id.btnLowerRight);
        SetButtonColor(b, Color.GRAY);

    }

    private void SetUpperLeftButton() {
        Button b = (Button)findViewById(R.id.btnUpperLeft);
        SetButtonColor(b, Color.RED);
    }

    private void SetUpperRightButton(){
        Button b = (Button)findViewById(R.id.btnUpperRight);
        SetButtonColor(b, Color.RED);
    }

    private void SetLowerLeftButton(){
        Button b = (Button)findViewById(R.id.btnLowerLeft);
        SetButtonColor(b, Color.RED);
    }
    private void SetLowerRightButton(){
        Button b = (Button)findViewById(R.id.btnLowerRight);
        SetButtonColor(b, Color.RED);
    }

    private void SetButtonColor(Button b, int color){
        b.setBackgroundColor(color);
        goodId = b.getId();
    }
}
