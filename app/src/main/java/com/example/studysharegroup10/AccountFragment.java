package com.example.studysharegroup10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AccountFragment extends Fragment {

    private ShapeableImageView profileImage;
    private TextInputEditText displayNameInput;

    private ActivityResultLauncher<String> pickPhoto;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickPhoto = registerForActivityResult(new ActivityResultContracts.GetContent(), this::onPhotoPicked);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_account);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateHome();
            }
        });

        profileImage = view.findViewById(R.id.profile_image);
        displayNameInput = view.findViewById(R.id.display_name_input);
        TextView email = view.findViewById(R.id.account_email);
        TextView uid = view.findViewById(R.id.account_uid);
        TextView accountType = view.findViewById(R.id.account_type);
        MaterialButton btnPhoto = view.findViewById(R.id.btn_change_photo);
        MaterialButton btnSave = view.findViewById(R.id.btn_save_profile);
        MaterialButton btnLogout = view.findViewById(R.id.btn_log_out);

        displayNameInput.setText(ProfileRepository.getDisplayName(requireContext()));
        loadProfilePhoto();

        btnPhoto.setOnClickListener(v -> pickPhoto.launch("image/*"));
        btnSave.setOnClickListener(v -> {
            CharSequence t = displayNameInput.getText();
            ProfileRepository.setDisplayName(requireContext(), t != null ? t.toString() : "");
            Snackbar.make(requireActivity().findViewById(R.id.main), R.string.profile_saved, Snackbar.LENGTH_SHORT)
                    .show();
        });
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Snackbar.make(requireActivity().findViewById(R.id.main), R.string.logged_out, Snackbar.LENGTH_SHORT)
                    .show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onUserSignedOut();
            }
            bindAuthFields(email, uid, accountType);
        });

        bindAuthFields(email, uid, accountType);
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) {
            bindAuthFields(
                    view.findViewById(R.id.account_email),
                    view.findViewById(R.id.account_uid),
                    view.findViewById(R.id.account_type));
        }
    }

    private void bindAuthFields(TextView email, TextView uid, TextView accountType) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            email.setText(R.string.account_not_signed_in);
            uid.setText("");
            accountType.setText("");
            return;
        }
        String mail = user.getEmail();
        email.setText(!TextUtils.isEmpty(mail) ? mail : getString(R.string.account_no_email));
        uid.setText(getString(R.string.account_uid_fmt, user.getUid()));
        if (user.isAnonymous()) {
            accountType.setText(R.string.account_type_guest);
        } else {
            accountType.setText(R.string.account_type_signed_in);
        }
    }

    private void loadProfilePhoto() {
        String path = ProfileRepository.getPhotoPath(requireContext());
        if (!TextUtils.isEmpty(path)) {
            File f = new File(path);
            if (f.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                profileImage.setImageBitmap(bmp);
                return;
            }
        }
        profileImage.setImageResource(R.drawable.ic_profile_placeholder);
    }

    private void onPhotoPicked(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            File out = new File(requireContext().getFilesDir(), "profile_avatar.jpg");
            try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
                 FileOutputStream fo = new FileOutputStream(out)) {
                if (in == null) {
                    throw new IllegalStateException("openInputStream null");
                }
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    fo.write(buf, 0, n);
                }
            }
            ProfileRepository.setPhotoPath(requireContext(), out.getAbsolutePath());
            loadProfilePhoto();
            Snackbar.make(requireActivity().findViewById(R.id.main), R.string.profile_photo_updated,
                    Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.profile_photo_error, e.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
    }
}
