package in.digibuddies.vivian;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import static in.digibuddies.vivian.R.layout.activity_hologram;

/**
 * A Chat Screen Activity
 */
public class Hologram extends AppCompatActivity {
    SensorManager sensorManager;
    SensorEventListener proximitySensorListener;
    Sensor proximitySensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_hologram);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
            Button button = (Button)findViewById(R.id.button2);
            sensorManager =
                    (SensorManager) getSystemService(SENSOR_SERVICE);
            proximitySensor =
                    sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if(proximitySensor == null) {
                Log.e("proximityerror", "Proximity sensor not available.");
                // Close app
            }

            proximitySensorListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    if(sensorEvent.values[0] < proximitySensor.getMaximumRange()) {

                        } else {
                           }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                }
            };

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(proximitySensorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(proximitySensorListener,
                proximitySensor, 2 * 1000 * 1000);
    }
}
