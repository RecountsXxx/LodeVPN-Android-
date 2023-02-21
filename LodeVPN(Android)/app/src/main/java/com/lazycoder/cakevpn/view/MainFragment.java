package com.lazycoder.cakevpn.view;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.lazycoder.cakevpn.CheckInternetConnection;
import com.lazycoder.cakevpn.R;
import com.lazycoder.cakevpn.SharedPreference;
import com.lazycoder.cakevpn.Utils;
import com.lazycoder.cakevpn.databinding.FragmentActivityBinding;
import com.lazycoder.cakevpn.interfaces.ChangeServer;
import com.lazycoder.cakevpn.model.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;
import static android.app.Activity.RESULT_OK;

public class MainFragment extends Fragment implements  View.OnClickListener, ChangeServer {

    private Server server;
    private CheckInternetConnection connection;

    private OpenVPNThread vpnThread = new OpenVPNThread();
    private OpenVPNService vpnService = new OpenVPNService();
    boolean vpnStart = false;
    private SharedPreference preference;
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private ListFragment listFragment = new ListFragment();
    private FragmentActivityBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            int bar = R.id.connection_progress;
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_activity, null, false);
        View view = binding.getRoot();

        binding.optimalServerBtn.setOnClickListener(this);

        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = (AdView)binding.adView;
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        loadInterstitialAd();

        initializeAll();
        Bundle bundle = getArguments();
        if (bundle != null) {
            String country = bundle.getString("country");
            String ip = bundle.getString("ip");
            String login = bundle.getString("login");
            String flag = bundle.getString("flag");
            String password = bundle.getString("password");
            server = new Server(country,
                    flag,
                    ip,
                    login,
                    password);
            switch (country) {
                case "Poland":
                    binding.imageLocation.setImageResource(R.drawable.pl);
                    break;
                case "USA":
                    binding.imageLocation.setImageResource(R.drawable.usa_flag);
                    break;
                case "Canada":
                    binding.imageLocation.setImageResource(R.drawable.ca);
                    break;
                case "Germany":
                    binding.imageLocation.setImageResource(R.drawable.de);
                    break;
                case "France":
                    binding.imageLocation.setImageResource(R.drawable.fr_flag);
                    break;
            }
            binding.locationText.setText(country);

        }
        return view;
    }
    private void initializeAll() {
        preference = new SharedPreference(getContext());
        binding.locationText.setText("Poland");
        server = new Server("Poland",
                Utils.getImgURL(R.drawable.pl),
                "Poland.ovpn",
                "vpnbook",
                "rxtasfh");

        connection = new CheckInternetConnection();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.vpnBtn.setOnClickListener(this);

        // Checking is vpn already running or not
        isServiceRunning();
        VpnStatus.initLogCache(getActivity().getCacheDir());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vpnBtn:
                // Vpn is running, user would like to disconnect current connection.
                if (vpnStart) {
                    confirmDisconnect();
                }else {
                    prepareVpn();
                }
                break;
            case R.id.optimal_server_btn:
                FragmentTransaction transaction = getFragmentManager().beginTransaction(); // Или getSupportFragmentManager(), если используете support.v4
                transaction.replace(R.id.container, listFragment); // Заменяете вторым фрагментом. Т.е. вместо метода `add()`, используете метод `replace()`
                transaction.addToBackStack(null); // Добавляете в backstack, чтобы можно было вернутся обратно
                transaction.commit(); // Коммитете
                loadInterstitialAd();
                if(mInterstitialAd != null){
                    mInterstitialAd.show(getActivity());
                }
                break;
        }
    }


    public void confirmDisconnect(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getString(R.string.connection_close_confirm));

        builder.setPositiveButton(getActivity().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                binding.connectionProgress.setVisibility(View.INVISIBLE);
                stopVpn();
                loadInterstitialAd();
                if(mInterstitialAd != null){
                    mInterstitialAd.show(getActivity());
                }
            }

        });
        builder.setNegativeButton(getActivity().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                loadInterstitialAd();
                if(mInterstitialAd != null){
                    mInterstitialAd.show(getActivity());
                }
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void prepareVpn() {
        if (!vpnStart) {
            if (getInternetStatus()) {

                // Checking permission for network monitor
                Intent intent = VpnService.prepare(getContext());

                if (intent != null) {
                    startActivityForResult(intent, 1);
                } else startVpn();//have already permission

                // Update confection status
                status("connecting");

            } else {
                status("noInternet");
            }

        } else if (stopVpn()) {

            // VPN is stopped, show a Toast message.
            showToast("Disconnect Successfully");
            vpnStart = false;
        }
    }

    public boolean stopVpn() {
        try {
            vpnThread.stop();

            status("connect");
            vpnStart = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            //Permission granted, start the VPN
            startVpn();
        } else {
            showToast("Permission Deny !! ");
        }
    }


    public boolean getInternetStatus() {
        return connection.netCheck(getContext());
    }

    public void isServiceRunning() {
        setStatus(vpnService.getStatus());
    }

    private void startVpn() {
        try {
            InputStream conf = getActivity().getAssets().open(server.getOvpn());
            InputStreamReader isr = new InputStreamReader(conf);
            BufferedReader br = new BufferedReader(isr);
            String config = "";
            String line;

            while (true) {
                line = br.readLine();
                if (line == null) break;
                config += line + "\n";
            }

            br.readLine();
            OpenVpnApi.startVpn(getContext(), config, server.getCountry(), server.getOvpnUserName(), server.getOvpnUserPassword());

            // Update log
            binding.logTv.setText("Connecting...");
            vpnStart = true;
            binding.connectionProgress.setVisibility(View.VISIBLE);

        } catch (IOException | RemoteException e) {
            e.printStackTrace();
        }
    }



    @SuppressLint("SuspiciousIndentation")
    public void setStatus(String connectionState) {
        if (connectionState!= null)
        switch (connectionState) {
            case "DISCONNECTED":
                status("connect");
                vpnStart = false;
                vpnService.setDefaultStatus();
                binding.logTv.setText("");
                binding.vpnBtn.setImageResource(R.drawable.power_off);
                break;
            case "CONNECTED":
                loadInterstitialAd();
                if(mInterstitialAd != null){
                    mInterstitialAd.show(getActivity());
                }
                vpnStart = true;// it will use after restart this activity
                status("connected");
                binding.logTv.setText("Connected");
                binding.connectionProgress.setVisibility(View.INVISIBLE);
                break;
            case "WAIT":
                binding.logTv.setText("Waiting");
                binding.connectionProgress.setVisibility(View.VISIBLE);
                break;
            case "AUTH":
                binding.logTv.setText("Authenticating");
                binding.connectionProgress.setVisibility(View.VISIBLE);
                break;
            case "RECONNECTING":
                status("connecting");
                binding.logTv.setText("Reconnecting");
                binding.connectionProgress.setVisibility(View.VISIBLE);
                break;
            case "NONETWORK":
                binding.logTv.setText("No network connection");
                binding.vpnBtn.setImageResource(R.drawable.power_off);
                binding.connectionProgress.setVisibility(View.INVISIBLE);
                break;
        }

    }

    public void status(String status) {

        if (status.equals("connect")) {
            binding.vpnBtn.setImageResource(R.drawable.power_stop);
        } else if (status.equals("connecting")) {
            binding.vpnBtn.setImageResource(R.drawable.power_off);
        } else if (status.equals("connected")) {
           binding.vpnBtn.setImageResource(R.drawable.power_stop);
        }
        else if (status.equals("tryDifferentServer")) {
            binding.vpnBtn.setImageResource(R.drawable.power_off);
       }
        else if (status.equals("loading")) {
            binding.vpnBtn.setImageResource(R.drawable.power_off);
        }
        else if (status.equals("invalidDevice")) {
            binding.vpnBtn.setImageResource(R.drawable.power_off);
        }
        else if (status.equals("authenticationCheck")) {
            binding.vpnBtn.setImageResource(R.drawable.power_off);
        }
        else if (status.equals("noInternet")) {
            binding.vpnBtn.setImageResource(R.drawable.power_off);
        }

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setStatus(intent.getStringExtra("state"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                String duration = intent.getStringExtra("duration");
                String lastPacketReceive = intent.getStringExtra("lastPacketReceive");
                String byteIn = intent.getStringExtra("byteIn");
                String byteOut = intent.getStringExtra("byteOut");

                if (duration == null) duration = "00:00:00";
                if (byteIn == null) byteIn = " 0 B";
                if (byteOut == null) byteOut = " 0 B";
                updateConnectionStatus(duration, byteIn, byteOut);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    public void updateConnectionStatus(String duration, String byteIn, String byteOut) {
        binding.durationTv.setText("Duration: " + duration);
        int byteInIndex = byteIn.indexOf("-");
        int byteOutIndex = byteOut.indexOf("-");
        binding.byteInTv.setText(byteIn.substring(byteInIndex + 2,byteIn.length()));
        binding.byteOutTv.setText(byteOut.substring(byteOutIndex + 2,byteOut.length()));
    }

    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void newServer(Server server) {
        this.server = server;

        // Stop previous connection
        if (vpnStart) {
            stopVpn();
        }

        prepareVpn();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));

        if (server == null) {
            server = preference.getServer();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onStop() {
        if (server != null) {
            preference.saveServer(server);
        }

        super.onStop();
    }


    public void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(getContext(), "ca-app-pub-7778810849910920/6436736634", adRequest,
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
