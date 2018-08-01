package app.gerida;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;

public class SplashActivity extends Activity {

    private Intent i;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    i = new Intent(SplashActivity.this,
                            MainActivity.class);

                } catch (Exception e) {

                } finally {
                    startActivity(i);
                    finish();
                }
            }
        };
        welcomeThread.start();
    }

}