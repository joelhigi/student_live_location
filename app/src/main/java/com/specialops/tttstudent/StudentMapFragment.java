package com.specialops.tttstudent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class StudentMapFragment extends Fragment {

    private Double latitude = -1.9353;
    private Double longitude = 30.1586;
    private Map<String,LatLng> currentCoordinates = new HashMap<String,LatLng>();
    public StudentMapFragment(Map<String,LatLng> newCoordinates)
    {
        currentCoordinates.clear();
        currentCoordinates = newCoordinates;
    }

    public StudentMapFragment()
    {

    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            for (Map.Entry<String,LatLng> entry : currentCoordinates.entrySet())
            {
                Log.e("Key",entry.getKey());
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions()
                        .position(entry.getValue())
                        .title(entry.getKey())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_icon_resized)));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(entry.getValue()));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(entry.getValue(), 17));
            }
            googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("CMU Africa Campus"));

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}