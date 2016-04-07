package cz.kralicinora.myowncamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.commonsware.cwac.cam2.CameraActivity;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.ZoomStyle;

import java.io.File;
import java.util.Date;

public class Main extends Activity {
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView = (TextView) findViewById(R.id.main_textview);
        ImageButton imageButton = (ImageButton) findViewById(R.id.main_camerabutton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new CameraActivity.IntentBuilder(Main.this)
                        .facing(Facing.BACK)
                        .to(new File(Environment.getExternalStorageDirectory(), new Date().getTime() + ".jpg"))
                        .debug()
                        .zoomStyle(ZoomStyle.SEEKBAR)
                        .build();

                startActivityForResult(i, 89);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            textView.setText(String.valueOf(data));
        }
    }
}
