package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PriceService {

    // ── 50 Nifty stocks — Yahoo symbol → display name ────────
    private static final Map<String, String> YAHOO_TO_DISPLAY = new LinkedHashMap<>();
    private static final Map<String, String> DISPLAY_TO_YAHOO = new LinkedHashMap<>();
    private static final Map<String, String> DISPLAY_TO_SECTOR = new LinkedHashMap<>();

    static {
        // Financial
        add("HDFCBANK.NS",     "HDFCBANK",     "Financial");
        add("ICICIBANK.NS",    "ICICIBANK",    "Financial");
        add("KOTAKBANK.NS",    "KOTAKBANK",    "Financial");
        add("AXISBANK.NS",     "AXISBANK",     "Financial");
        add("SBILIFE.NS",      "SBILIFE",      "Financial");
        add("BAJFINANCE.NS",   "BAJFINANCE",   "Financial");
        add("BAJAJFINSV.NS",   "BAJAJFINSV",   "Financial");
        add("HDFCLIFE.NS",     "HDFCLIFE",     "Financial");
        add("ICICIPRULI.NS",   "ICICIPRULI",   "Financial");
        add("SBIN.NS",         "SBIN",         "Financial");

        // IT
        add("TCS.NS",          "TCS",          "IT");
        add("INFY.NS",         "INFY",         "IT");
        add("WIPRO.NS",        "WIPRO",        "IT");
        add("HCLTECH.NS",      "HCLTECH",      "IT");
        add("TECHM.NS",        "TECHM",        "IT");
        add("LTI.NS",          "LTI",          "IT");
        add("MPHASIS.NS",      "MPHASIS",      "IT");
        add("PERSISTENT.NS",   "PERSISTENT",   "IT");
        add("COFORGE.NS",      "COFORGE",      "IT");
        add("LTIM.NS",         "LTIM",         "IT");

        // Energy & Oil
        add("RELIANCE.NS",     "RELIANCE",     "Energy");
        add("ONGC.NS",         "ONGC",         "Energy");
        add("BPCL.NS",         "BPCL",         "Energy");
        add("IOC.NS",          "IOC",          "Energy");
        add("COALINDIA.NS",    "COALINDIA",    "Energy");
        add("NTPC.NS",         "NTPC",         "Energy");
        add("POWERGRID.NS",    "POWERGRID",    "Energy");
        add("ADANIGREEN.NS",   "ADANIGREEN",   "Energy");
        add("ADANIPORTS.NS",   "ADANIPORTS",   "Energy");
        add("TATAPOWER.NS",    "TATAPOWER",    "Energy");

        // Consumer
        add("HINDUNILVR.NS",   "HINDUNILVR",   "Consumer");
        add("ITC.NS",          "ITC",          "Consumer");
        add("NESTLEIND.NS",    "NESTLEIND",    "Consumer");
        add("BRITANNIA.NS",    "BRITANNIA",    "Consumer");
        add("DABUR.NS",        "DABUR",        "Consumer");
        add("MARICO.NS",       "MARICO",       "Consumer");
        add("GODREJCP.NS",     "GODREJCP",     "Consumer");
        add("TATACONSUM.NS",   "TATACONSUM",   "Consumer");
        add("COLPAL.NS",       "COLPAL",       "Consumer");
        add("VBL.NS",          "VBL",          "Consumer");

        // Auto
        add("MARUTI.NS",       "MARUTI",       "Auto");
        add("TATAMOTORS.NS",   "TATAMOTORS",   "Auto");
        add("M%26M.NS",        "M&M",          "Auto");
        add("BAJAJ-AUTO.NS",   "BAJAJ-AUTO",   "Auto");
        add("HEROMOTOCO.NS",   "HEROMOTOCO",   "Auto");
        add("EICHERMOT.NS",    "EICHERMOT",    "Auto");

        // Pharma
        add("SUNPHARMA.NS",    "SUNPHARMA",    "Pharma");
        add("DRREDDY.NS",      "DRREDDY",      "Pharma");
        add("CIPLA.NS",        "CIPLA",        "Pharma");
        add("DIVISLAB.NS",     "DIVISLAB",     "Pharma");
    }

    private static void add(String yahoo, String display, String sector) {
        YAHOO_TO_DISPLAY.put(yahoo,   display);
        DISPLAY_TO_YAHOO.put(display, yahoo);
        DISPLAY_TO_SECTOR.put(display, sector);
    }

    // Fallback prices
    private static final Map<String, Double> FALLBACK = new HashMap<>();
    static {
        FALLBACK.put("HDFCBANK",   1600.0); FALLBACK.put("ICICIBANK",  1100.0);
        FALLBACK.put("KOTAKBANK",  1800.0); FALLBACK.put("AXISBANK",   1100.0);
        FALLBACK.put("SBILIFE",    1400.0); FALLBACK.put("BAJFINANCE", 6800.0);
        FALLBACK.put("BAJAJFINSV",1600.0);  FALLBACK.put("HDFCLIFE",    600.0);
        FALLBACK.put("ICICIPRULI", 600.0);  FALLBACK.put("SBIN",        800.0);
        FALLBACK.put("TCS",        3900.0); FALLBACK.put("INFY",       1450.0);
        FALLBACK.put("WIPRO",       420.0); FALLBACK.put("HCLTECH",    1500.0);
        FALLBACK.put("TECHM",      1200.0); FALLBACK.put("LTI",        5000.0);
        FALLBACK.put("MPHASIS",    2500.0); FALLBACK.put("PERSISTENT", 4000.0);
        FALLBACK.put("COFORGE",    6000.0); FALLBACK.put("LTIM",       5500.0);
        FALLBACK.put("RELIANCE",   2500.0); FALLBACK.put("ONGC",        280.0);
        FALLBACK.put("BPCL",        600.0); FALLBACK.put("IOC",         180.0);
        FALLBACK.put("COALINDIA",   400.0); FALLBACK.put("NTPC",        350.0);
        FALLBACK.put("POWERGRID",   300.0); FALLBACK.put("ADANIGREEN", 1800.0);
        FALLBACK.put("ADANIPORTS",  800.0); FALLBACK.put("TATAPOWER",   400.0);
        FALLBACK.put("HINDUNILVR", 2400.0); FALLBACK.put("ITC",         450.0);
        FALLBACK.put("NESTLEIND", 22000.0); FALLBACK.put("BRITANNIA",  5000.0);
        FALLBACK.put("DABUR",       550.0); FALLBACK.put("MARICO",      550.0);
        FALLBACK.put("GODREJCP",   1200.0); FALLBACK.put("TATACONSUM",  900.0);
        FALLBACK.put("COLPAL",     2800.0); FALLBACK.put("VBL",        1500.0);
        FALLBACK.put("MARUTI",    10000.0); FALLBACK.put("TATAMOTORS",  900.0);
        FALLBACK.put("M&M",        1800.0); FALLBACK.put("BAJAJ-AUTO",  8000.0);
        FALLBACK.put("HEROMOTOCO", 4000.0); FALLBACK.put("EICHERMOT",   3500.0);
        FALLBACK.put("SUNPHARMA",  1600.0); FALLBACK.put("DRREDDY",     6000.0);
        FALLBACK.put("CIPLA",      1400.0); FALLBACK.put("DIVISLAB",    3500.0);
    }

    // Last known prices cache
    private Map<String, Double> lastKnownPrices = new HashMap<>(FALLBACK);

    // ─────────────────────────────────────────────────────────
    //  Fetch all prices
    // ─────────────────────────────────────────────────────────
    public Map<String, Double> fetchPrices() {
        Map<String, Double> result = new HashMap<>();

        for (Map.Entry<String, String> entry : YAHOO_TO_DISPLAY.entrySet()) {
            String yahoo   = entry.getKey();
            String display = entry.getValue();
            try {
                double price = fetchSinglePrice(yahoo);
                result.put(display, price);
                lastKnownPrices.put(display, price);
            } catch (Exception e) {
                result.put(display,
                        lastKnownPrices.getOrDefault(display,
                        FALLBACK.getOrDefault(display, 100.0)));
                System.err.println("Failed: " + yahoo + " — " + e.getMessage());
            }
        }

        return result;
    }

    // ─────────────────────────────────────────────────────────
    //  Fetch single symbol
    // ─────────────────────────────────────────────────────────
    private double fetchSinglePrice(String yahooSymbol) throws Exception {
        String urlStr = "https://query1.finance.yahoo.com/v8/finance/chart/"
                + yahooSymbol + "?interval=1m&range=1d";

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200)
            throw new Exception("HTTP " + conn.getResponseCode());

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        conn.disconnect();

        return parsePrice(sb.toString());
    }

    // ─────────────────────────────────────────────────────────
    //  Parse price from JSON
    // ─────────────────────────────────────────────────────────
    private double parsePrice(String json) throws Exception {
        String key = "\"regularMarketPrice\":";
        int idx = json.indexOf(key);
        if (idx == -1) {
            key = "\"previousClose\":";
            idx = json.indexOf(key);
        }
        if (idx == -1) throw new Exception("Price key not found");

        int start = idx + key.length();
        int end   = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);

        return Double.parseDouble(json.substring(start, end).trim());
    }

    // ─────────────────────────────────────────────────────────
    //  Public getters
    // ─────────────────────────────────────────────────────────
    public Map<String, Double> getLastKnownPrices() {
        return lastKnownPrices;
    }

    public static Map<String, String> getDisplayToSector() {
        return DISPLAY_TO_SECTOR;
    }

    public static String[] getDisplaySymbols() {
        return DISPLAY_TO_YAHOO.keySet().toArray(new String[0]);
    }

    public static String getSector(String display) {
        return DISPLAY_TO_SECTOR.getOrDefault(display, "Other");
    }
}