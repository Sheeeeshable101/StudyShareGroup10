package com.example.studysharegroup10;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final SummarizerClient summarizerClient = new SummarizerClient();

    private LinearProgressIndicator uploadProgress;
    private TextView uploadStatus;
    private TextView summaryResult;
    private TextInputEditText notesInput;
    private MaterialButton summarizeButton;

    private final FirebaseAuth.AuthStateListener authListener = auth -> refreshAuthStatus();

    private ActivityResultLauncher<String> pickUpload;
    private ActivityResultLauncher<String[]> pickPdf;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickUpload = registerForActivityResult(new ActivityResultContracts.GetContent(), this::onFilePickedForUpload);
        pickPdf = registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onPdfPickedForView);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        uploadProgress = view.findViewById(R.id.upload_progress);
        uploadStatus = view.findViewById(R.id.upload_status);
        summaryResult = view.findViewById(R.id.summary_result);
        notesInput = view.findViewById(R.id.notes_input);
        summarizeButton = view.findViewById(R.id.btn_summarize);

        MaterialButton btnUpload = view.findViewById(R.id.btn_upload_storage);
        MaterialButton btnPdf = view.findViewById(R.id.btn_open_pdf);

        btnUpload.setOnClickListener(v -> pickUpload.launch("*/*"));
        btnPdf.setOnClickListener(v -> pickPdf.launch(new String[]{"application/pdf"}));

        summarizeButton.setOnClickListener(v -> runSummarize());

        updateSummarizeEnabled();
        refreshAuthStatus();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
        refreshAuthStatus();
    }

    @Override
    public void onStop() {
        FirebaseAuth.getInstance().removeAuthStateListener(authListener);
        super.onStop();
    }

    public void refreshAuthStatus() {
        if (uploadStatus == null || !isAdded()) {
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            uploadStatus.setText(R.string.firebase_signing_in);
        } else {
            uploadStatus.setText(R.string.firebase_signed_in);
        }
    }

    private void updateSummarizeEnabled() {
        summarizeButton.setEnabled(summarizerClient.isConfigured());
        if (!summarizerClient.isConfigured()) {
            summaryResult.setText(R.string.summarizer_not_configured);
        }
    }

    private void onFilePickedForUpload(Uri uri) {
        if (uri == null) {
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Snackbar.make(requireActivity().findViewById(R.id.main), R.string.firebase_wait_auth, Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        uploadProgress.setVisibility(View.VISIBLE);
        uploadProgress.setIndeterminate(true);
        uploadStatus.setText(R.string.upload_in_progress);

        String name = "file_" + System.currentTimeMillis();
        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("uploads")
                .child(user.getUid())
                .child(name);

        ref.putFile(uri)
                .addOnProgressListener(snapshot -> {
                    long total = snapshot.getTotalByteCount();
                    if (total > 0) {
                        uploadProgress.setIndeterminate(false);
                        int p = (int) (100 * snapshot.getBytesTransferred() / total);
                        uploadProgress.setProgress(p, true);
                    }
                })
                .addOnSuccessListener(taskSnapshot -> {
                    uploadProgress.setVisibility(View.GONE);
                    uploadStatus.setText(R.string.upload_success);
                    Snackbar.make(requireActivity().findViewById(R.id.main), R.string.upload_success,
                            Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    uploadProgress.setVisibility(View.GONE);
                    uploadStatus.setText(R.string.upload_failed);
                    Snackbar.make(requireActivity().findViewById(R.id.main),
                            getString(R.string.upload_error_fmt, e.getMessage()), Snackbar.LENGTH_LONG).show();
                });
    }

    private void onPdfPickedForView(Uri uri) {
        if (uri == null) {
            return;
        }
        io.execute(() -> {
            try {
                File local = UriIo.copyUriToCache(requireContext(), uri, ".pdf");
                requireActivity().runOnUiThread(() -> {
                    Intent i = new Intent(requireContext(), PdfViewerActivity.class);
                    i.putExtra(PdfViewerActivity.EXTRA_FILE_PATH, local.getAbsolutePath());
                    startActivity(i);
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), getString(R.string.pdf_open_error, e.getMessage()),
                                Toast.LENGTH_LONG).show());
            }
        });
    }

    private void runSummarize() {
        if (!summarizerClient.isConfigured()) {
            Snackbar.make(requireActivity().findViewById(R.id.main), R.string.summarizer_not_configured,
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        CharSequence text = notesInput.getText();
        if (text == null || TextUtils.isEmpty(text.toString().trim())) {
            Snackbar.make(requireActivity().findViewById(R.id.main), R.string.summarizer_empty_notes,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        summarizeButton.setEnabled(false);
        summaryResult.setText(R.string.summarizer_working);
        io.execute(() -> {
            try {
                String out = summarizerClient.summarize(text.toString().trim());
                requireActivity().runOnUiThread(() -> {
                    summaryResult.setText(out);
                    summarizeButton.setEnabled(true);
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    summaryResult.setText(getString(R.string.summarizer_error, e.getMessage()));
                    summarizeButton.setEnabled(true);
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        io.shutdown();
        super.onDestroyView();
    }
}
