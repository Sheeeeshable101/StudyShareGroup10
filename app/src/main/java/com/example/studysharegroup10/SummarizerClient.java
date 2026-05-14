package com.example.studysharegroup10;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

final class SummarizerClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build();

    boolean isConfigured() {
        return BuildConfig.SUMMARIZER_URL != null
                && !BuildConfig.SUMMARIZER_URL.trim().isEmpty();
    }

    @NonNull
    String summarize(String notes) throws IOException {
        if (!isConfigured()) {
            throw new IOException("Summarizer URL not set (add summarizer.url to local.properties)");
        }
        JSONObject root = new JSONObject();
        try {
            root.put("model", "gpt-4o-mini");
            JSONArray messages = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content",
                    "Summarize the following study notes clearly for a student. "
                            + "Use short bullet points. Keep under 200 words.\n\n" + notes);
            messages.put(userMsg);
            root.put("messages", messages);
        } catch (Exception e) {
            throw new IOException("Failed to build request JSON", e);
        }

        Request.Builder rb = new Request.Builder()
                .url(BuildConfig.SUMMARIZER_URL.trim())
                .post(RequestBody.create(root.toString(), JSON));
        if (BuildConfig.SUMMARIZER_API_KEY != null && !BuildConfig.SUMMARIZER_API_KEY.trim().isEmpty()) {
            rb.header("Authorization", "Bearer " + BuildConfig.SUMMARIZER_API_KEY.trim());
        }
        try (Response response = http.newCall(rb.build()).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + ": " + truncate(body, 400));
            }
            String parsed = parseSummaryBody(body);
            if (parsed == null || parsed.isEmpty()) {
                return truncate(body, 4000);
            }
            return parsed;
        }
    }

    private static String parseSummaryBody(String body) {
        try {
            JSONObject o = new JSONObject(body);
            if (o.has("choices")) {
                JSONArray choices = o.optJSONArray("choices");
                if (choices != null && choices.length() > 0) {
                    JSONObject first = choices.optJSONObject(0);
                    if (first != null) {
                        JSONObject message = first.optJSONObject("message");
                        if (message != null) {
                            String c = message.optString("content", null);
                            if (c != null && !c.isEmpty()) {
                                return c;
                            }
                        }
                    }
                }
            }
            for (String key : new String[]{"summary", "result", "output", "text"}) {
                if (o.has(key)) {
                    String v = o.optString(key, "");
                    if (!v.isEmpty()) {
                        return v;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return body;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "…";
    }
}
