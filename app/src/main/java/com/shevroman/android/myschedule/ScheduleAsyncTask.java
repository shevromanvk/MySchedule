package com.shevroman.android.myschedule;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.shevroman.android.myschedule.ui.GroupScheduleActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by Рома on 10/15/2017.
 */

public class ScheduleAsyncTask extends AsyncTask<URL, Integer, String> {
    public ScheduleAsyncTask() {

    }

    private String csvResponse = "";
    private static final String SCHEDULE_URL =
            "https://docs.google.com/spreadsheets/d/e/2PACX-1vRYTu3kPdUePi3A2PiTYpHf64a" +
                    "9gQynzN522BYUsLPIMnUtvqVpxJHs2bc3hCVNalLCQu9G3SVHpgK7/pub?output=csv";
    private static final String CACHE_FILE_NAME = "scheduleCache.csv";

    @Override
    protected String doInBackground(URL... params) {
        String LOG_TAG = getClass().getSimpleName();
        URL url = createUrl(SCHEDULE_URL);
        try {
            csvResponse = makeHttpRequest(url);
            FileUtils.writeToFile(CACHE_FILE_NAME, csvResponse);
            App.getInstance().getScheduleRepository().setSchedule(csvResponse);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error with making HTTP Request", e);
            csvResponse = FileUtils.readFromFile(CACHE_FILE_NAME);
        }


        return csvResponse;
    }


    @Override
    protected void onPostExecute(String s) {
        if (TextUtils.isEmpty(s)) {
            showToast("Підключіться до Інтернету, щоб завантажити останню версію розкладу");
        }
    }


    /**
     * Returns new URL object from the given string URL.
     */
    private URL createUrl(String stringUrl) {
        String LOG_TAG = GroupScheduleActivity.class.getSimpleName();
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    /**
     * Make an HTTP request to the given SCHEDULE_URL and return a String as the response.
     */
    private String makeHttpRequest(java.net.URL url) throws IOException {
        String LOG_TAG = GroupScheduleActivity.class.getSimpleName();
        String csvResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                inputStream = urlConnection.getInputStream();
                csvResponse = readFromStream(inputStream);
            } else Log.v(LOG_TAG, "Response code: " + statusCode);
        } catch (IOException e) {
            Log.v(LOG_TAG, "CSV response couldn't receive");
            throw e;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return csvResponse;
    }


    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line).append("\n");
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    public static void showToast(String message) {
        Toast.makeText(App.getInstance(), message, Toast.LENGTH_LONG).show();
    }
}
