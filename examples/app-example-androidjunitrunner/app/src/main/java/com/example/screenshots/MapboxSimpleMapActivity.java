package com.example.screenshots;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class MapboxSimpleMapActivity extends AppCompatActivity {

  private MapboxMap map;
  private MapView mapView;
  private static final LatLng[] LAT_LNGS = new LatLng[] {
    new LatLng(38.897424, -77.036508),
    new LatLng(38.909698, -77.029642),
    new LatLng(38.907227, -77.036530),
    new LatLng(38.905607, -77.031916),
    new LatLng(38.889441, -77.050134),
    new LatLng(38.888000, -77.050000) // Slight overlap to show re-ordering on selection
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Mapbox.getInstance(this, getString(R.string.access_token));

    setContentView(R.layout.activity_mapbox_simple_map);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);

    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        mapboxMap.setAllowConcurrentMultipleOpenInfoWindows(true);
        // add default ViewMarker markers
        for (int i = 0; i < LAT_LNGS.length; i++) {
          MarkerView marker = mapboxMap.addMarker(new MarkerViewOptions()
            .position(LAT_LNGS[i])
            .title(String.valueOf(i))
          );
          if (i % 2 == 0) {
            mapboxMap.selectMarker(marker);
          }
        }
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
