package in.digibuddies.vivian;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;

public class Splash extends Activity {

    // Splash screen timer

    private static int SPLASH_TIME_OUT = 5000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        KenBurnsView kbv = (KenBurnsView) findViewById(R.id.image);
       // scheduleAlarm();
        RandomTransitionGenerator generator = new RandomTransitionGenerator(4900,new AccelerateDecelerateInterpolator());
        kbv.setTransitionGenerator(generator);
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar3);
        progressBar.setIndeterminate(true);
       // setAnimation();
      // Handler handler=new Handler();

      /*   handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                ((SquareLoading) findViewById(R.id.sq)).setVisibility(View.VISIBLE);
            }
        }, 1000);
*/

      //  getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent i = new Intent(Splash.this, MainActivity.class);
                i.putExtra("target","none");
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);

    }
 /*
    private void setAnimation() {
       ObjectAnimator scaleXAnimation = ObjectAnimator.ofFloat(findViewById(R.id.welcome_text), "scaleX", 5.0F, 1.0F);
        scaleXAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleXAnimation.setDuration(1200);
        ObjectAnimator scaleYAnimation = ObjectAnimator.ofFloat(findViewById(R.id.welcome_text), "scaleY", 5.0F, 1.0F);
        scaleYAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleYAnimation.setDuration(1200);
        ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(findViewById(R.id.welcome_text), "alpha", 0.0F, 1.0F);
        alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimation.setDuration(1200);
       AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleXAnimation).with(scaleYAnimation).with(alphaAnimation);
        animatorSet.setStartDelay(500);
        animatorSet.start();

        findViewById(R.id.imagelogo).setAlpha(1.0F);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.translate_top_to_center);
        findViewById(R.id.imagelogo).startAnimation(anim);
    }
    // Setup a recurring alarm every half hour


    public void scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), alarmreceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, alarmreceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_HALF_HOUR, pIntent);

    }*/

    protected void onStartNewActivity() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        onStartNewActivity();
    }


}
