package app.jumpoint;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

class MyLocationListener implements LocationListener {

    public static Location locationGPS;
    public static Location locationNet;
    public static double longitude;
    public static double latitude;
    //public static boolean workGPS;
    public static String statusLoc;

    public static void SetUpLocationListener(Context context) // это нужно запустить в самом начале работы программы
    {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new MyLocationListener();

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0, // Задержка (миллисекунды)
                5, // Количество метров для обновления
                locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                5,
                locationListener);

        locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        //statusLoc = locationManager.addProximityAlert(latitude);
        //workGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onLocationChanged(Location loc) {
        longitude = loc.getLongitude();
        latitude = loc.getLatitude();
    }
    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}
        //onLocationChanged – новые данные о местоположении, объект Location.
        //onProviderDisabled – указанный провайдер был отключен пользователем.
        //onProviderEnabled – указанный провайдер был включен пользователем.
        //onStatusChanged – изменился статус указанного провайдера. В поле status могут быть значения OUT_OF_SERVICE (данные будут недоступны долгое время), TEMPORARILY_UNAVAILABLE (данные временно недоступны), AVAILABLE (все ок, данные доступны).
