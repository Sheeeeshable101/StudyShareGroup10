package com.example.studysharegroup10;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;

public class PdfViewerActivity extends AppCompatActivity {

    public static final String EXTRA_FILE_PATH = "extra_file_path";

    private PdfRenderer renderer;
    private int pageCount;
    private int currentPage;
    private ImageView pageView;
    private TextView pageLabel;
    private MaterialButton prevBtn;
    private MaterialButton nextBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemePalettePreference.applyActivityTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        String path = getIntent().getStringExtra(EXTRA_FILE_PATH);
        MaterialToolbar toolbar = findViewById(R.id.pdf_toolbar);
        pageView = findViewById(R.id.pdf_page_image);
        pageLabel = findViewById(R.id.pdf_page_label);
        prevBtn = findViewById(R.id.pdf_prev);
        nextBtn = findViewById(R.id.pdf_next);

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        if (path == null) {
            finish();
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            finish();
            return;
        }

        toolbar.setTitle(file.getName());

        try {
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(pfd);
            pageCount = renderer.getPageCount();
            currentPage = 0;
            renderPage();
        } catch (IOException e) {
            finish();
            return;
        }

        prevBtn.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                renderPage();
            }
        });
        nextBtn.setOnClickListener(v -> {
            if (currentPage < pageCount - 1) {
                currentPage++;
                renderPage();
            }
        });
    }

    private void renderPage() {
        if (renderer == null || pageCount <= 0) {
            return;
        }
        try (PdfRenderer.Page page = renderer.openPage(currentPage)) {
            int pw = page.getWidth();
            int ph = page.getHeight();
            int maxW = Math.min(1440, pageView.getWidth() > 0 ? pageView.getWidth() * 2 : 1080);
            float scale = Math.min(2.5f, (float) maxW / Math.max(1, pw));
            int rw = Math.max(1, Math.round(pw * scale));
            int rh = Math.max(1, Math.round(ph * scale));
            Bitmap bitmap = Bitmap.createBitmap(rw, rh, Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            Drawable prevDrawable = pageView.getDrawable();
            pageView.setImageBitmap(bitmap);
            if (prevDrawable instanceof BitmapDrawable) {
                Bitmap old = ((BitmapDrawable) prevDrawable).getBitmap();
                if (old != null && !old.isRecycled() && old != bitmap) {
                    old.recycle();
                }
            }
        }

        pageLabel.setText(getString(R.string.pdf_page_indicator, currentPage + 1, pageCount));
        prevBtn.setEnabled(currentPage > 0);
        nextBtn.setEnabled(currentPage < pageCount - 1);
    }

    @Override
    protected void onDestroy() {
        if (renderer != null) {
            renderer.close();
        }
        super.onDestroy();
    }
}
