package com.wiley.wrox.chapter8;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class WearActivity extends Activity {

  private TextView mTextView;

  private GoogleApiClient mApiClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_wear);
    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        mTextView = (TextView) stub.findViewById(R.id.text);
      }
    });

    mApiClient = new GoogleApiClient.Builder(this)
        .addApi(LocationServices.API)
        .addConnectionCallbacks(mConnectionListener)
        .addOnConnectionFailedListener(mConnectionFailedListener)
        .build();
  }

  @Override
  protected void onResume() {
    super.onResume();

    mApiClient.connect();
  }

  @Override
  protected void onPause() {
    super.onPause();

    LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient,
        mLocationListener);

    mApiClient.disconnect();
  }

  private GoogleApiClient.ConnectionCallbacks mConnectionListener = new
      GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
          Log.d("TEST", "onConnected API - requesting location");
          LocationRequest request = LocationRequest.create();

          LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, request, mLocationListener);
        }

        @Override
        public void onConnectionSuspended(int i) {
        }
      };

  private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener =
      new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
          Log.d("TEST", "onConnectionFailed");
        }
      };

  private boolean hasGpsSupport() {
    return getPackageManager().hasSystemFeature(PackageManager
        .FEATURE_LOCATION_GPS);
  }

  private LocationListener mLocationListener = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
      if (Geocoder.isPresent()) {
        new AsyncTask<Double, Void, Address>() {
          @Override
          protected Address doInBackground(Double...doubles) {
            Log.d("TEST", "doInBackground");

            Geocoder coder = new Geocoder(WearActivity.this, Locale.getDefault());

            List<Address> addressList = null;
            try {
              addressList = coder.getFromLocation(doubles[0], doubles[1], 1);
              if (addressList != null && addressList.size() > 0) {
                return addressList.get(0);
              }
            } catch (IOException e) {
              e.printStackTrace();
            }

            return null;
          }
        }.execute(location.getLatitude(), location.getLongitude());
      }else{
        mTextView.setText("No geocoder");
      }
      //mTextView.setText(location.getLatitude() + ", " + location.getLongitude());
    }
  };

}
