package com.lazycoder.cakevpn.view;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lazycoder.cakevpn.R;
import com.lazycoder.cakevpn.adapter.ListItemAdapter;
import com.lazycoder.cakevpn.interfaces.OnDataI;
import com.lazycoder.cakevpn.model.ListItem;

import java.util.ArrayList;
public class ListFragment extends Fragment {
    private OnDataI mDataPasser;
    private ListView listView;
    private TextView textView;
    public ListFragment() {
    }
    public static ListFragment newInstance(String param1, String param2) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, null);
        String[] countrys = new String[]{"Poland Warsaw","USA New-York","USA California","Canada Vancuver","Canada Ottava","France Paris","France Marsel","Germany Berlin"};
        int[] images = new int[]{R.drawable.pl,R.drawable.usa_flag,R.drawable.usa_flag,R.drawable.ca,R.drawable.ca,R.drawable.fr_flag,R.drawable.fr_flag,R.drawable.de};

        ListItemAdapter adapter = new ListItemAdapter(getContext(),countrys,images);

        ListView listView_aac = (ListView) v.findViewById(R.id.listView);
        listView_aac.setAdapter(adapter);
        listView_aac.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainFragment main   = new MainFragment();
                int qwe = position;
                if(qwe == 0){
                    mDataPasser.onDataPass("Poland","pl.png","Poland.ovpn","vpnbook","rxtasfh");
                }
                if(qwe == 1){
                    mDataPasser.onDataPass("USA","usa_flag.png","USA1.ovpn","vpnbook","rxtasfh");
               }
                if(qwe == 2){
                    mDataPasser.onDataPass("USA","usa_flag.png","USA2.ovpn","vpnbook","rxtasfh");
                }
                if(qwe == 3){
                    mDataPasser.onDataPass("Canada","ca.png","Canada1.ovpn","vpnbook","rxtasfh");
                }
                if(qwe == 4){
                    mDataPasser.onDataPass("Canada","ca.png","Canada2.ovpn","vpnbook","rxtasfh");
                }
                if(qwe == 5){
                    mDataPasser.onDataPass("France","fr_flag.png","France1.ovpn","vpnbook","rxtasfh");
                }
                if(qwe == 6){
                    mDataPasser.onDataPass("France","fr_flag.png","France2.ovpn","vpnbook","rxtasfh");
                }
                if(qwe == 7){
                    mDataPasser.onDataPass("Germany","de.png","Germany.ovpn","vpnbook","rxtasfh");
                }
                main.stopVpn();
            }
        });

        return v;
    }


    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        mDataPasser = (OnDataI) a;
    }
}