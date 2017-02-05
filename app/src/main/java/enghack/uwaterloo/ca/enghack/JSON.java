package enghack.uwaterloo.ca.enghack;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Midori on 2017-02-04.
 */

public class JSON {
    private String data = "";

    public JSONObject getJSON(final String url) {
        final Thread pathThread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL URL = new URL(url);
                    Scanner scan = new Scanner(URL.openStream());
                    scan(scan);

                } catch (IOException e) {
                    System.err.println("URL is malformed");
                }
            }
        });
        pathThread1.start();

        try {
            pathThread1.join();
            JSONObject results = new JSONObject(data);
            return results;
        } catch (InterruptedException | JSONException e) {
            System.err.println("Exception");
            return null;
        }
    }

    private void scan(Scanner s) {
        while (s.hasNext()) {
            data += s.nextLine();
        }

        s.close();
    }
}
