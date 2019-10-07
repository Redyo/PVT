package pvt.pvt;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    Fragment searchFr, homeFr, profileFr;
    Fragment currentFr;
    Fragment selectedFragment;
    BottomNavigationView bottomNavigationView;

    static MainActivity main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        searchFr = new Search();
        homeFr = new HomeView();
        profileFr = new Profile();


        main = this;

        bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.navigation_search:
                                hideKeyBoard();
                                Log.v("MainActivity", "goto search");
                                selectedFragment = searchFr;
                                break;
                            case R.id.navigation_home:
                                hideKeyBoard();
                                Log.v("MainActivity", "goto home");
                                refreshHomeView();
                                selectedFragment = homeFr;
                                break;
                            case R.id.navigation_profile:
                                hideKeyBoard();
                                Log.v("MainActivity", "goto profile");
                                refreshProfile();
                                selectedFragment = profileFr;
                                break;
                        }

                        if (selectedFragment == currentFr)
                            return true;

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.hide(currentFr);
                        transaction.show(selectedFragment);
                        transaction.commit();
                        currentFr = selectedFragment;
                        return true;
                    }
                });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.content, searchFr);
        transaction.hide(searchFr);
        transaction.add(R.id.content, profileFr);
        transaction.hide(profileFr);
        transaction.add(R.id.content, homeFr);
        transaction.commit();
        currentFr = homeFr;
    }

    private void refreshHomeView(){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent i = new Intent("TAG_REFRESH");
        lbm.sendBroadcast(i);
    }

    private void refreshProfile(){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent i = new Intent("TAG_REFRESH");
        lbm.sendBroadcast(i);
    }

    public static MainActivity getInstance(){
        return main;
    }


    private void hideKeyBoard(){
        Search s = new Search().getInstance();
        s.hideKeyBoard();
    }

    public void goToSearch(){
        selectedFragment = searchFr;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        bottomNavigationView.setSelectedItemId(R.id.navigation_search);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.hide(currentFr);
        transaction.show(selectedFragment);
        transaction.commit();
        currentFr = selectedFragment;
    }
}
