package com.example.parisbistro.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.example.parisbistro.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
<<<<<<< HEAD
                startActivity(new Intent(getBaseContext(), MainActivity.class));
            }
        },2500);

    }
}
=======
                startActivity(new Intent(getBaseContext(),MainActivity.class));
            }
        },2500);


    }
}

>>>>>>> 0d9bd449a8bf8b4ee6b74402a0390cbba74a76c4
