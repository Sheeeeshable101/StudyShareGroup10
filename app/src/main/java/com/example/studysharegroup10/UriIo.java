package com.example.studysharegroup10;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

final class UriIo {

    private UriIo() {
    }

    static File copyUriToCache(Context context, Uri uri, String suffix) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) {
                throw new IOException("Could not open document");
            }
            String base = queryDisplayName(resolver, uri);
            if (base == null || base.isEmpty()) {
                base = "upload";
            }
            String safe = base.replaceAll("[^a-zA-Z0-9._-]", "_");
            if (safe.length() > 80) {
                safe = safe.substring(0, 80);
            }
            File out = new File(context.getCacheDir(), System.currentTimeMillis() + "_" + safe + suffix);
            try (FileOutputStream fo = new FileOutputStream(out)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    fo.write(buf, 0, n);
                }
            }
            return out;
        }
    }

    private static String queryDisplayName(ContentResolver resolver, Uri uri) {
        try (Cursor c = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    return c.getString(idx);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
