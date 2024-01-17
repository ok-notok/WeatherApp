package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast?";
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    private TextView weatherTextView;
    private LinearLayout forecastLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherTextView = findViewById(R.id.weatherTextView);
        forecastLayout = findViewById(R.id.forecastLayout);

        SharedPreferences preferences = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);
        String location = preferences.getString("pref_location", "Istanbul");
        String unit = preferences.getString("pref_unit", "metric");

        getWeatherData(location, unit);
        getWeeklyForecast(location, unit);
        getWindForecast(location, unit);
    }

    @SuppressLint("StaticFieldLeak")
    private void getWeatherData(String location, String unit) {
        String url = WEATHER_URL + "q=" + location + "&units=" + unit + "&appid=" + API_KEY;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL weatherUrl = new URL(params[0]);
                    HttpURLConnection connection = (HttpURLConnection) weatherUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        Scanner scanner = new Scanner(weatherUrl.openStream());
                        StringBuilder data = new StringBuilder();
                        while (scanner.hasNext()) {
                            data.append(scanner.nextLine());
                        }
                        scanner.close();
                        return data.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);

                        JSONObject mainData = jsonObject.getJSONObject("main");
                        double temperature = mainData.getDouble("temp");
                        double feelsLike = mainData.getDouble("feels_like");
                        int humidity = mainData.getInt("humidity");
                        JSONObject weatherDetails = jsonObject.getJSONArray("weather").getJSONObject(0);
                        String weatherDescription = weatherDetails.getString("description");
                        String iconCode = weatherDetails.getString("icon");
                        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

                        String weatherInfo = "Temperature: " + String.format("%.2f", temperature) + (unit.equals("metric") ? "°C" : "°F") + "\n" +
                                "Feels Like: " + String.format("%.2f", feelsLike) + (unit.equals("metric") ? "°C" : "°F") + "\n" +
                                "Humidity: " + humidity + "%\n" +
                                "Condition: " + weatherDescription;

                        weatherTextView.setText(weatherInfo);

                        // Fetch and display the current weather icon
                        ImageView currentWeatherIcon = findViewById(R.id.currentWeatherIcon);
                        new DownloadImageTask(currentWeatherIcon).execute(iconUrl);

                    } catch (Exception e) {
                        e.printStackTrace();
                        weatherTextView.setText("Failed to parse weather data.");
                    }
                } else {
                    weatherTextView.setText("Didn't receive weather data.");
                }
            }
        }.execute(url);
    }
    @SuppressLint("StaticFieldLeak")
    private void getWeeklyForecast(String location, String unit) {
        String url = FORECAST_URL + "q=" + location + "&units=" + unit + "&appid=" + API_KEY;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL forecastUrl = new URL(params[0]);
                    HttpURLConnection connection = (HttpURLConnection) forecastUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        Scanner scanner = new Scanner(forecastUrl.openStream());
                        StringBuilder data = new StringBuilder();
                        while (scanner.hasNext()) {
                            data.append(scanner.nextLine());
                        }
                        scanner.close();
                        return data.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @SuppressLint("DefaultLocale")
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        JSONArray forecastArray = jsonObject.getJSONArray("list");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd MMM", Locale.getDefault());
                        Set<String> processedDates = new HashSet<>();

                        forecastLayout.removeAllViews();
                        for (int i = 0; i < forecastArray.length(); i++) {
                            JSONObject forecast = forecastArray.getJSONObject(i);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(forecast.getLong("dt") * 1000L);

                            String date = dateFormat.format(calendar.getTime());
                            if (!processedDates.contains(date)) {
                                double temp = forecast.getJSONObject("main").getDouble("temp");
                                String iconCode = forecast.getJSONArray("weather").getJSONObject(0).getString("icon");
                                String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

                                LinearLayout horizontalLayout = new LinearLayout(MainActivity.this);
                                horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                                horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));

                                TextView forecastTextView = new TextView(MainActivity.this);
                                forecastTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));
                                forecastTextView.setText(String.format("%s: %.1f %s", date, temp, unit.equals("metric") ? "°C" : "°F"));

                                ImageView weatherIcon = new ImageView(MainActivity.this);
                                weatherIcon.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));
                                new DownloadImageTask(weatherIcon).execute(iconUrl);

                                horizontalLayout.addView(weatherIcon);
                                horizontalLayout.addView(forecastTextView);
                                forecastLayout.addView(horizontalLayout);

                                processedDates.add(date);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute(url);
    }

    @SuppressLint("StaticFieldLeak")
    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // Method to fetch the wind forecast
    @SuppressLint("StaticFieldLeak")
    private void getWindForecast(String location, String unit) {
        String url = WEATHER_URL + "q=" + location + "&units=" + unit + "&appid=" + API_KEY;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL windUrl = new URL(params[0]);
                    HttpURLConnection connection = (HttpURLConnection) windUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        Scanner scanner = new Scanner(windUrl.openStream());
                        StringBuilder data = new StringBuilder();
                        while (scanner.hasNext()) {
                            data.append(scanner.nextLine());
                        }
                        scanner.close();
                        return data.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        JSONObject windData = jsonObject.getJSONObject("wind");
                        double windSpeed = windData.getDouble("speed");
                        int windDegree = windData.has("deg") ? windData.getInt("deg") : 0;

                        TextView windTextView = findViewById(R.id.windTextView);
                        windTextView.setText(String.format(Locale.getDefault(), "Wind Speed: %.1f m/s, Direction: %d°", windSpeed, windDegree));

                    } catch (Exception e) {
                        e.printStackTrace();
                        TextView windTextView = findViewById(R.id.windTextView);
                        windTextView.setText("Error fetching wind data.");
                    }
                } else {
                    TextView windTextView = findViewById(R.id.windTextView);
                    windTextView.setText("No wind data received.");
                }
            }
        }.execute(url);
    }
}
