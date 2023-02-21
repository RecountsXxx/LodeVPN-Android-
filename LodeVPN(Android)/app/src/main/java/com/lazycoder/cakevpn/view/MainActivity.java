package com.lazycoder.cakevpn.view;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lazycoder.cakevpn.LoadingActivity;
import com.lazycoder.cakevpn.R;
import com.lazycoder.cakevpn.interfaces.ChangeServer;
import com.lazycoder.cakevpn.interfaces.NavItemClickListener;
import com.lazycoder.cakevpn.interfaces.OnDataI;
import com.lazycoder.cakevpn.model.Server;

import java.util.ArrayList;
import java.util.Locale;

import com.lazycoder.cakevpn.Utils;



public class MainActivity extends AppCompatActivity implements OnDataI{
    private FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    private Fragment fragment;
    private RecyclerView serverListRv;
    private ArrayList<Server> serverLists;
    private DrawerLayout drawer;
    InterstitialAd mInterstitialAd;
    private Server server;
    private ChangeServer changeServer;
    public static final String TAG = "Lode VPN";
    ListFragment servers = new ListFragment();
    MainFragment main = new MainFragment();
    BottomNavigationView nav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadInterstitialAd();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        nav =  findViewById(R.id.nav);
        nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,main).commit();
                        Bundle bundle = new Bundle();
                        if(server != null){
                            bundle.putString("login",server.getOvpnUserName());
                            bundle.putString("password",server.getOvpnUserPassword());
                            bundle.putString("ip",server.getOvpn());
                            bundle.putString("country",server.getCountry());
                            bundle.putString("flag",server.getFlagUrl());
                            main.setArguments(bundle);
                        }
                        else{
                            bundle.putString("login","vpnbook");
                            bundle.putString("password","rxtasfh");
                            bundle.putString("ip","Poland.ovpn");
                            bundle.putString("country","Poland");
                            bundle.putString("flag","pl.png");
                            main.setArguments(bundle);
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,  main).commit();
                                break;
                    case R.id.servers:
                        loadInterstitialAd();
                        if(mInterstitialAd != null){
                            mInterstitialAd.show(MainActivity.this);
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,servers).commit();
                        break;
                    default:
                }return true;

            }
        });




        initializeAll();
        transaction.add(R.id.container, fragment);
        transaction.commit();

    }
    private void initializeAll() {
        drawer = findViewById(R.id.drawer_layout);
        fragment = new MainFragment();
        changeServer = (ChangeServer) fragment;

    }
    @Override
    public void onDataPass(String contry,String flagURL, String ip, String login, String password) {
        server = new Server(contry,flagURL,ip,login,password);
    }


    public void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, "ca-app-pub-7778810849910920/6436736634", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;


                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        mInterstitialAd = null;
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        mInterstitialAd = null;
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;

                        String error = String.format(
                                Locale.ENGLISH,
                                "domain: %s, code: %d, message: %s",
                                loadAdError.getDomain(),
                                loadAdError.getCode(),
                                loadAdError.getMessage());
                    }
                });
    }

}
