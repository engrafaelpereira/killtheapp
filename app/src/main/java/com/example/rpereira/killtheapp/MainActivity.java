package com.example.rpereira.killtheapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.rpereira.killtheapp.layout.AboutFragment;
import com.example.rpereira.killtheapp.layout.ProcessListFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new ProcessListFragment())
                    .commit();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            flipCard();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void flipCard() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            return;
        }

        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out)
                .replace(R.id.container, new AboutFragment())
                .addToBackStack(null)
                .commit();
    }
}
