package com.burningaltar.ducttapebackend;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * A very simple backend that fetches data from a Google spreadsheet.
 * <p>
 * To use: <br>
 * <ol>
 * <li>Create a Google Docs spreadsheet
 * <li>Change the "Sharing Settings" to "Anyone who has the link can view"
 * <li>Copy the 40ish character key from the URL
 * <li>Put this class in your Android project
 * <li>Add internet uses-permission to the manifest
 * <li>Have your activity implement
 * {@link DownloadGoogleSpreadsheetDataListener}
 * <li>Call
 * {@link DuctTapeBackend#downloadGoogleSpreadsheetData(String, DownloadGoogleSpreadsheetDataListener)}
 * using the spreadsheet key and the listener
 * </ol>
 * <p>
 * That'll get you the csv as a big string. To parse it out to a String[][], use
 * {@link DuctTapeBackend#parseCsvToRowColData(String)}
 *
 * @author brianherbert <briandherbert@gmail.com>
 */
public class DuctTapeBackend {
    public static final String TAG = DuctTapeBackend.class.getSimpleName();

    /**
     * Used to toggle logging
     */
    public static final boolean DEBUG = true;

    /**
     * Max chars to read from the spreadsheet
     */
    public static final int MAX_PAGE_SRC = 300000;

    /**
     * Placeholder for error response (remember, Duct Tape)
     */
    public static final String ERROR_RESULT = "XXXERRORXXX";

    public static final char QUOTE = '"';
    public static final char COMMA = ',';

    /**
     * The generic form of a Google Drive spreadsheet url for CSV output. The %s
     * placeholder will be replaced with a spreadsheet key. Note that this is
     * fragile and the whole thing will fall apart if Google changes this
     */
    public static final String CSV_URL = "https://docs.google.com/spreadsheets/d/%s/export?format=csv";

    public static interface DownloadGoogleSpreadsheetDataListener {
        /**
         * Note that this will not be called on the UI thread! Use
         * Activity.runOnUiThread() to update UI
         */
        public void onSpreadsheetDataLoaded(final String csv);

        /**
         * Note that this will not be called back on the UI thread! Use
         * Activity.runOnUiThread() to update UI
         */
        public void onSpreadsheetDataFailed(final String message);
    }

    public static void downloadGoogleSpreadsheetData(String key,
                                                     DownloadGoogleSpreadsheetDataListener listener) {
        String url = String.format(CSV_URL, key);

        if (DEBUG)
            Log.v(TAG, "Download spreadsheet at " + url);

        Thread thread = new GetSpreadsheetDataThread(url, listener);
        thread.start();
    }

    private static class GetSpreadsheetDataThread extends Thread {
        private String url;
        private DownloadGoogleSpreadsheetDataListener listener;

        public GetSpreadsheetDataThread(String url,
                                        DownloadGoogleSpreadsheetDataListener listener) {
            this.url = url;
            this.listener = listener;
        }

        @Override
        public void run() {
            String csv = getPageSourceAsDesktop(url, null);
            if (listener != null) {
                if (csv.indexOf(ERROR_RESULT) == 0) {
                    String errorMsg = csv.substring(ERROR_RESULT
                            .length());

                    Log.e(TAG, "Error getting values: " + errorMsg);
                    listener.onSpreadsheetDataFailed(csv.substring(ERROR_RESULT
                            .length()));
                } else {
                    listener.onSpreadsheetDataLoaded(csv);
                }
            }
        }
    }

    private static String getPageSourceAsDesktop(String urlStr, String searchFor) {
        if (DEBUG)
            Log.v(TAG, "Getting page src for " + urlStr);

        URL url = null;
        String inputLine = "";

        BufferedReader in = null;
        try {
            url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);

            // Ensure that this is a csv
            String fileInfo = connection.getHeaderField("Content-Disposition");
            if (fileInfo == null || !fileInfo.contains(".csv")) {
                throw new Exception("File isn't csv formatted!");
            }

            InputStream stream = connection.getInputStream();
            in = new BufferedReader(new InputStreamReader(stream));

            int c;
            while ((c = in.read()) != -1) {
                inputLine += (char) c;
            }
        } catch (Exception e) {
            return ERROR_RESULT + " message: " + e.getMessage();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Exception e) {
            }
        }

        if (DEBUG) Log.v(TAG, "Got csv:\n" + inputLine);
        return inputLine;
    }

    /**
     * Returns a 2d array, where the first index references the row and the
     * second is the column
     */
    public static String[][] parseCsvToRowColData(String csv) {
        if (csv == null || csv.length() == 0) {
            if (DEBUG)
                Log.w(TAG, "csv is empty!");
            return null;
        }

        int numRows;
        int numCols;

        String rows[] = csv.split("\\r?\\n");

        if (rows == null || rows.length == 0) {
            if (DEBUG)
                Log.w(TAG, "No rows data!");
            return null;
        }

        if (rows[0] == null || rows[0].length() == 0) {
            if (DEBUG)
                Log.w(TAG, "No columns data!");
            return null;
        }

        List<String> colsList = getColumnsFromRow(rows[0]);

        if (colsList == null || colsList.size() == 0) {
            if (DEBUG)
                Log.w(TAG, "Error getting first row's columns!");
            return null;
        }

        String[] firstRowCols = colsList.toArray(new String[colsList
                .size()]);

        numCols = firstRowCols.length;
        numRows = rows.length;

        String[][] rowColData = new String[numRows][numCols];
        rowColData[0] = firstRowCols;

        for (int rowIdx = 1; rowIdx < rows.length; rowIdx++) {
            colsList = getColumnsFromRow(rows[rowIdx]);

            if (colsList.size() != numCols) {
                if (DEBUG)
                    Log.w(TAG,
                            "Number of columns is inconsistent between rows!");
                return null;
            }

            String[] cols = colsList.toArray(new String[colsList
                    .size()]);

            rowColData[rowIdx] = cols;
        }

        return rowColData;
    }

    private static List<String> getColumnsFromRow(String row) {
        if (DEBUG)
            Log.v(TAG, "Get cols from row " + row);
        int length = row.length();

        ArrayList<String> colVals = new ArrayList<String>();

        int idx = 0;

        while (idx < length) {
            String cell = "";
            char firstChar = row.charAt(idx);

            boolean inQuotes = false;

            if (firstChar == COMMA) {
                // Empty cell; we're done
                colVals.add(cell);
                idx++;

                continue;
            }

            // Not an empty cell
            if (firstChar != QUOTE) {
                // Ignore starting quotes
                cell += firstChar;
            } else {
                inQuotes = true;
            }

            // Now find the end of the column entry
            while (idx < length - 1) {
                idx++;

                char c = row.charAt(idx);
                if (c == QUOTE) {
                    // We hit a quote. The next char MUST be a quote, comma,
                    // or EOF
                    idx++;

                    if (idx >= length) {
                        // We're done.
                        break;
                    }

                    char charAfterQuote = row.charAt(idx);

                    if (charAfterQuote == COMMA) {
                        // done
                        break;
                    } else if (charAfterQuote == QUOTE) {
                        // Two quotes in a row means a single quote in the
                        // cell
                        cell += QUOTE;
                    } else {
                        // PROBLEM!!
                        if (DEBUG)
                            Log.w(TAG, "Unexpected parse!");
                        return null;
                    }
                } else if (!inQuotes && c == COMMA) {
                    break;
                } else {
                    cell += c;
                }
            }

            colVals.add(cell);
            idx++;
        }

        // If the last char was a comma, we need to add an empty cell
        if (length > 0 && row.charAt(length - 1) == COMMA) {
            colVals.add("");
        }

        if (DEBUG)
            Log.v(TAG, "Cols " + colVals);
        return colVals;
    }
}