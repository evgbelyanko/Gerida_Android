package app.gerida;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

class MyLocationListener implements LocationListener {

    static Location imHere; // здесь будет всегда доступна самая последняя информация о местоположении пользователя.
    public static double longitude = 0;
    public static double latitude = 0;
    public static boolean workGPS;

    public static void SetUpLocationListener(Context context) // это нужно запустить в самом начале работы программы
    {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new MyLocationListener();

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                5,
                locationListener); // здесь можно указать другие более подходящие вам параметры

        imHere = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        workGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onLocationChanged(Location loc) {
        imHere = loc;
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
