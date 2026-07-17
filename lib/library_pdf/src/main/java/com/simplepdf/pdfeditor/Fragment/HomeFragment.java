package com.simplepdf.pdfeditor.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.ContextThemeWrapper;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.simplepdf.pdfeditor.Activity.ChoosePdfActivity;
import com.simplepdf.pdfeditor.Activity.DigitalSignatureActivity;
import com.simplepdf.pdfeditor.Activity.MainActivityExternal;
import com.simplepdf.pdfeditor.Activity.MainActivityExternal1;
import com.simplepdf.pdfeditor.Activity.MainActivityExternalSynthetic2;
import com.simplepdf.pdfeditor.CallbackListener.RecyclerItemClick;
import com.simplepdf.pdfeditor.CallbackListener.ViewCallBackListener;
import com.simplepdf.pdfeditor.DialogFragment.DocumentBottomSheet;
import com.simplepdf.pdfeditor.DialogFragment.MenuBottomSheet;
import com.simplepdf.pdfeditor.DialogFragment.PDFOptionBottomSheet;
import com.simplepdf.pdfeditor.DialogFragment.PDFSortByBottomSheet;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.Signature.SignatureActivity;
import com.simplepdf.pdfeditor.adapter.PDFListViewAdapter;
import com.simplepdf.pdfeditor.databinding.DialogPermissionsBinding;
import com.simplepdf.pdfeditor.databinding.FileAlertDialogBinding;
import com.simplepdf.pdfeditor.databinding.FragmentHomeBinding;
import com.simplepdf.pdfeditor.databinding.SignatureDeletetDialogBinding;
import com.simplepdf.pdfeditor.model.PDFFileModel;
import com.simplepdf.pdfeditor.util.AppConstants;
import com.simplepdf.pdfeditor.util.BetterActivityResult;
import com.simplepdf.pdfeditor.util.Constant;
import com.simplepdf.pdfeditor.util.ScaleImageMatrix;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragmentPdf";
    private static final int REQUEST_GALLERY_IMAGE = 2001;
    private static final int REQUEST_CAMERA_IMAGE = 2002;
    private static final String KEY_CAMERA_IMAGE_PATH = "camera_image_path";
    private PDFListViewAdapter adapter;
    FragmentHomeBinding binding;
    private Activity mContext;
    private boolean permissionNotify = false;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
    EditText searchEditText;
    SearchView searchView;
    List<PDFFileModel> PDFitemsList = new ArrayList();
    int SortBy = 3;
    boolean isSelectAll = false;
    public boolean isFilter = false;
    String SearchValue = "";
    File imageFile = null;
    private String cameraImagePath = null;
    private boolean refreshPdfListOnResume = false;
    private boolean waitingForExternalImageResult = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    public static void lambda$editPdf$11(ActivityResult activityResult) {
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void setProgressVisible(boolean visible, String reason) {
        Log.d(TAG, "progress " + (visible ? "VISIBLE" : "GONE") + ": " + reason);
        if (this.binding != null && this.binding.llProgress != null && this.binding.llProgress.progress != null) {
            this.binding.llProgress.progress.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private String bitmapInfo(Bitmap bitmap) {
        if (bitmap == null) {
            return "null";
        }
        return bitmap.getWidth() + "x" + bitmap.getHeight() + ", bytes=" + bitmap.getByteCount();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            this.cameraImagePath = savedInstanceState.getString(KEY_CAMERA_IMAGE_PATH);
            if (!TextUtils.isEmpty(this.cameraImagePath)) {
                this.imageFile = new File(this.cameraImagePath);
                Log.d(TAG, "restore camera image file=" + this.cameraImagePath);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(this.cameraImagePath)) {
            outState.putString(KEY_CAMERA_IMAGE_PATH, this.cameraImagePath);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = DataBindingUtil.inflate(pdfThemeInflater(inflater), R.layout.fragment_home, container, false);
        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mContext = requireActivity();
        setToolbar();
        init();
        setViewListener();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (this.waitingForExternalImageResult) {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (HomeFragment.this.waitingForExternalImageResult) {
                        Log.d(TAG, "external image result callback not received after resume");
                        HomeFragment.this.waitingForExternalImageResult = false;
                        HomeFragment.this.setProgressVisible(false, "external image result missing");
                    }
                }
            }, 700L);
        }
        if (this.refreshPdfListOnResume && this.binding != null && this.adapter != null) {
            this.refreshPdfListOnResume = false;
            reloadPdfFileList();
        }
    }
    private LayoutInflater pdfThemeInflater() {
        return LayoutInflater.from(pdfThemeContext());
    }

    private LayoutInflater pdfThemeInflater(LayoutInflater inflater) {
        return inflater.cloneInContext(pdfThemeContext());
    }

    private Context pdfThemeContext() {
        return new ContextThemeWrapper(requireContext(), R.style.Theme_PdfSignature);
    }
    public void setToolbar() {
        try {
            ((AppCompatActivity) requireActivity()).setSupportActionBar(this.binding.toolbar);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(AppConstants.fromHtml("<font color='#ff3378'>&#160;&#160;&#160;&#160;Sign</font><font color='#323743'>&#160;PDF Document</font>"));
            spannableStringBuilder.setSpan(new StyleSpan(1), 0, AppConstants.fromHtml("<font color='#ff3378'>&#160;&#160;&#160;&#160;Sign</font><font color='#323743'>&#160;PDF Document</font>").length(), 33);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(spannableStringBuilder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        registerForContextMenu(this.binding.rvPdfListView);
        setRecyclerView();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (Build.VERSION.SDK_INT >= 30) {
                this.permissionNotify = AppConstants.checkStoragePermissionApi30(this.mContext);
            }
            if (!this.permissionNotify) {
                if (Build.VERSION.SDK_INT >= 30) {
                    openDialogPermission();
                    return;
                }
                return;
            }
        } else {
            if (Build.VERSION.SDK_INT >= 30) {
                this.permissionNotify = AppConstants.checkStoragePermissionApi30(this.mContext);
            } else {
                this.permissionNotify = AppConstants.checkStoragePermissionApi19(this.mContext);
            }
            if (!this.permissionNotify) {
                if (Build.VERSION.SDK_INT >= 30) {
                    openDialogPermission();
                    return;
                } else if (Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 1001);
                    return;
                } else {
                    return;
                }
            }
        }

        getAllPdfFileList();
    }

    private void HandleExternalData() {
        Intent intent = requireActivity().getIntent();
        String stringExtra = intent.getStringExtra("ActivityAction");
        ArrayList parcelableArrayListExtra = intent.getParcelableArrayListExtra("PDFOpen");
        if (TextUtils.isEmpty(stringExtra)) {
            return;
        }
        Intent intent2 = new Intent(requireContext().getApplicationContext(), DigitalSignatureActivity.class);
        intent2.putExtra("ActivityAction", stringExtra);
        intent2.putExtra("PDFOpen", parcelableArrayListExtra);
        launchPdfEditor(intent2, new BetterActivityResult.OnActivityResult<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult activityResult) {
                HomeFragment.this.m74xfe339031(activityResult);
            }
        });
    }


    public void m74xfe339031(ActivityResult activityResult) {
        Intent data;
        if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null) {
            return;
        }
        this.PDFitemsList.add(new PDFFileModel(new File(data.getStringExtra("NewPDFPath")), false));
        PDFListViewAdapter pDFListViewAdapter = this.adapter;
        if (pDFListViewAdapter != null) {
            pDFListViewAdapter.notifyItemInserted(this.PDFitemsList.size());
        }
        PDFSortingArrayList(this.SortBy, this.PDFitemsList);
    }

    private void openDialogPermission() {
        DialogPermissionsBinding dialogPermissionsBinding = (DialogPermissionsBinding) DataBindingUtil.inflate(pdfThemeInflater(), R.layout.dialog_permissions, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        dialog.setContentView(dialogPermissionsBinding.getRoot());
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.show();
        Glide.with(this).load(Integer.valueOf((int) R.raw.permission_switch)).into(dialogPermissionsBinding.imgPermission);
        dialogPermissionsBinding.cardSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                HomeFragment.this.showPermissionNotifyDialog();
            }
        });
        dialogPermissionsBinding.cardCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                HomeFragment.this.requireActivity().finish();
            }
        });
    }

    public void showPermissionNotifyDialog() {
        if (Build.VERSION.SDK_INT >= 30) {
            this.activityLauncher.launch(new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION", Uri.parse("package:" + requireContext().getPackageName())), new BetterActivityResult.OnActivityResult() {
                @Override
                public final void onActivityResult(Object obj) {
                    HomeFragment.this.m90xf5459d72((ActivityResult) obj);
                }
            });
        } else if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 1001);
        }
    }


    public void m90xf5459d72(ActivityResult activityResult) {
        if (AppConstants.checkStoragePermissionApi30(this.mContext)) {
            getAllPdfFileList();
        } else {
            openDialogPermission();
        }
    }

    public void setViewListener() {
        this.binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                HomeFragment.this.m89x87e301de(view);
            }
        });
        this.binding.mcvSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeFragment.this.bottomSheetSortPdf();
            }
        });
        this.binding.mcvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuBottomSheet newInstance = MenuBottomSheet.newInstance(new RecyclerItemClick() {
                    @Override
                    public void onItemClick(int i) {
                        HomeFragment.this.handleMenuAction(i);
                    }
                });
                newInstance.setCancelable(true);
                newInstance.show(HomeFragment.this.getParentFragmentManager(), "Menu");
            }
        });
        this.binding.mcvSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeFragment HomeFragment = HomeFragment.this;
                HomeFragment.isSelectAll = !HomeFragment.isSelectAll;
                Glide.with(HomeFragment.this.mContext).load(Integer.valueOf(HomeFragment.this.isSelectAll ? R.drawable.ic_check : R.drawable.ic_uncheck)).into(HomeFragment.this.binding.imgSelect);
                HomeFragment.this.adapter.setAllSelection(HomeFragment.this.isSelectAll);
            }
        });
        this.binding.mcvPDFShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeFragment.this.sharePdfFile(null, -1, true);
            }
        });
        this.binding.mcvPDFDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HomeFragment.this.adapter.getFinalSelectedList().size() == 0) {
                    HomeFragment.this.showToast("At Least one item select");
                } else {
                    HomeFragment.this.deletePDF(null, -1, true);
                }
            }
        });
    }

    private void handleMenuAction(int action) {
        if (action == MenuBottomSheet.ACTION_SIGNATURES) {
            openSignatureManager();
        } else if (action == MenuBottomSheet.ACTION_SHARE_APP) {
            AppConstants.shareApp(requireActivity());
        }
    }

    private void openSignatureManager() {
        Intent intent = new Intent(requireContext().getApplicationContext(), SignatureActivity.class);
        intent.putExtra("FromMenu", true);
        this.activityLauncher.launch(intent, new BetterActivityResult.OnActivityResult() {
            @Override
            public void onActivityResult(Object obj) {
                HomeFragment.this.handleSignatureManagerResult((ActivityResult) obj);
            }
        });
    }

    private void handleSignatureManagerResult(ActivityResult activityResult) {
        if (activityResult.getResultCode() == Activity.RESULT_OK) {
            getAllPdfFileList();
        }
    }

    public void m89x87e301de(View view) {

        DocumentBottomSheet newInstance = DocumentBottomSheet.newInstance(new RecyclerItemClick() {
            @Override
            public final void onItemClick(int i) {
                HomeFragment.this.m88x885967dd(i);
            }
        });
        newInstance.setCancelable(true);
        newInstance.show(getParentFragmentManager(), "BottomSheetDocument");
    }


    @SuppressLint("WrongConstant")
    public void m88x885967dd(int i) {
        EditText editText = this.searchEditText;
        if (editText != null) {
            editText.setText("");
            SearchView searchView = this.searchView;
            if (searchView != null && !searchView.isIconified()) {
                this.searchView.setIconified(true);
            }
        }
        if (i == 66) {
            this.activityLauncher.launch(new Intent(requireContext().getApplicationContext(), ChoosePdfActivity.class), new BetterActivityResult.OnActivityResult() {
                @Override
                public final void onActivityResult(Object obj) {
                    HomeFragment.this.m83x8aa965d8((ActivityResult) obj);
                }
            });
        } else if (i == 77) {
            Intent intent = new Intent(requireContext().getApplicationContext(), DigitalSignatureActivity.class);
            intent.putExtra("ActivityAction", "FileSearch");
            launchPdfEditor(intent, new BetterActivityResult.OnActivityResult<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    HomeFragment.this.m84x8a32ffd9(activityResult);
                }
            });
        } else if (i == 88) {
            this.waitingForExternalImageResult = true;
            setProgressVisible(true, "open gallery picker");
            try {
                Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent2.setType("image/*");
                intent2.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent2, REQUEST_GALLERY_IMAGE);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "open gallery picker failed", e);
                this.waitingForExternalImageResult = false;
                setProgressVisible(false, "open gallery picker failed");
                showToast("Something wrong please try again!");
            } catch (Exception e) {
                Log.e(TAG, "open gallery picker failed", e);
                this.waitingForExternalImageResult = false;
                setProgressVisible(false, "open gallery picker failed");
                showToast("Something wrong please try again!");
            }
        } else if (i == 99 && Build.VERSION.SDK_INT >= 23) {
            if (requireContext().checkSelfPermission("android.permission.CAMERA") != 0) {
                requestPermissions(new String[]{"android.permission.CAMERA"}, 1002);
                return;
            }
            this.waitingForExternalImageResult = true;
            setProgressVisible(true, "open camera");
            OpenCamera();
        }
    }


    private void launchPdfEditor(Intent intent, BetterActivityResult.OnActivityResult<ActivityResult> onActivityResult) {
        this.refreshPdfListOnResume = true;
        this.activityLauncher.launch(intent, onActivityResult);
    }
    public void m83x8aa965d8(ActivityResult activityResult) {
        Intent data;
        if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null) {
            return;
        }
        String stringExtra = data.getStringExtra("choosePdf");
        Intent intent = new Intent(requireContext().getApplicationContext(), DigitalSignatureActivity.class);
        intent.putExtra("ActivityAction", "selectPdf");
        intent.putExtra("filePath", stringExtra);
        setProgressVisible(false, "hide progress");
        launchPdfEditor(intent, new BetterActivityResult.OnActivityResult<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult activityResult) {
                HomeFragment.this.m82x8b1fcbd7(activityResult);
            }
        });
    }


    public void m82x8b1fcbd7(ActivityResult activityResult) {
        handleSavedPdfResult(activityResult);
    }


    public void m84x8a32ffd9(ActivityResult activityResult) {
        handleSavedPdfResult(activityResult);
    }


    private void handleSavedPdfResult(ActivityResult activityResult) {
        Log.d(TAG, "handleSavedPdfResult resultCode=" + (activityResult == null ? "null" : activityResult.getResultCode()) + ", data=" + (activityResult == null ? "null" : activityResult.getData()));
        if (activityResult == null || activityResult.getResultCode() != Activity.RESULT_OK) {
            return;
        }
        Intent data = activityResult.getData();
        if (data == null || TextUtils.isEmpty(data.getStringExtra("NewPDFPath"))) {
            Log.d(TAG, "handleSavedPdfResult ignored, NewPDFPath missing");
            return;
        }
        Log.d(TAG, "handleSavedPdfResult reload, NewPDFPath=" + data.getStringExtra("NewPDFPath"));
        reloadPdfFileList();
    }
    public void m87x88cfcddc(ActivityResult activityResult) throws Exception {
        this.waitingForExternalImageResult = false;
        Log.d(TAG, "gallery result received resultCode=" + (activityResult == null ? "null" : activityResult.getResultCode()) + ", data=" + (activityResult == null ? "null" : activityResult.getData()));
        if (activityResult == null || activityResult.getResultCode() != Activity.RESULT_OK) {
            setProgressVisible(false, "gallery canceled or failed");
            return;
        }
        Intent data = activityResult.getData();
        if (data == null || data.getData() == null) {
            Log.d(TAG, "gallery result has no uri");
            setProgressVisible(false, "gallery result empty");
            showToast("Something Wrong!");
            return;
        }
        handleGalleryUri(data.getData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY_IMAGE) {
            this.waitingForExternalImageResult = false;
            Log.d(TAG, "gallery legacy result resultCode=" + resultCode + ", data=" + data);
            if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                setProgressVisible(false, "gallery canceled or failed");
                return;
            }
            handleGalleryUri(data.getData());
        } else if (requestCode == REQUEST_CAMERA_IMAGE) {
            this.waitingForExternalImageResult = false;
            Log.d(TAG, "camera legacy result resultCode=" + resultCode + ", data=" + data);
            m77x95836461(new ActivityResult(resultCode, data));
        }
    }
    private void handleGalleryUri(final Uri imageUri) {
        this.waitingForExternalImageResult = false;
        if (imageUri == null) {
            Log.d(TAG, "gallery uri is null");
            setProgressVisible(false, "gallery uri null");
            showToast("Something Wrong!");
            return;
        }
        Log.d(TAG, "gallery uri=" + imageUri);
        this.disposable.add(Observable.fromCallable(new Callable() {
            @Override
            public final Object call() throws Exception {
                Log.d(TAG, "decode image start uri=" + imageUri);
                return HomeFragment.this.m85x89bc99da(imageUri);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer() {
            @Override
            public final void accept(Object obj) throws Exception {
                Log.d(TAG, "decode image success bitmap=" + HomeFragment.this.bitmapInfo((Bitmap) obj));
                HomeFragment.this.m86x894633db((Bitmap) obj);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Log.e(TAG, "decode image failed", throwable);
                HomeFragment.this.setProgressVisible(false, "decode image failed");
                HomeFragment.this.showToast("Something wrong please try again!");
            }
        }));
    }

    public Bitmap m85x89bc99da(Uri uri) {
        long start = System.currentTimeMillis();
        Bitmap bitmap = null;
        ParcelFileDescriptor openFileDescriptor = null;
        try {
            openFileDescriptor = requireContext().getContentResolver().openFileDescriptor(uri, "r");
            if (openFileDescriptor == null) {
                Log.d(TAG, "decode image openFileDescriptor returned null uri=" + uri);
                return null;
            }
            FileDescriptor fileDescriptor = openFileDescriptor.getFileDescriptor();
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            Log.d(TAG, "decodeFileDescriptor done in " + (System.currentTimeMillis() - start) + "ms, bitmap=" + bitmapInfo(bitmap));
        } catch (IOException e) {
            Log.e(TAG, "decode image IOException uri=" + uri, e);
        } finally {
            if (openFileDescriptor != null) {
                try {
                    openFileDescriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }


    public void m86x894633db(Bitmap bitmap) throws Exception {
        Log.d(TAG, "handle decoded bitmap=" + bitmapInfo(bitmap));
        if (bitmap == null) {
            setProgressVisible(false, "bitmap null");
            showToast("Something wrong please try again!");
            return;
        }
        createPdfFromBitmap(bitmap);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.pro_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        this.searchView = searchView;
        EditText editText = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        this.searchEditText = editText;
        editText.setTextColor(getResources().getColor(R.color.textColor1));
        this.searchEditText.setHintTextColor(getResources().getColor(R.color.textColor2));
        ((ImageView) this.searchView.findViewById(androidx.appcompat.R.id.search_button)).setImageResource(R.drawable.menu_search);
        this.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String str) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String str) {
                HomeFragment.this.SearchValue = str;
                HomeFragment.this.isFilter = !TextUtils.isEmpty(str.trim());
                if (HomeFragment.this.adapter != null) {
                    HomeFragment.this.adapter.getFilter().filter(str);
                    return false;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {

        return false;
    }

    public void setRecyclerView() {
        this.adapter = new PDFListViewAdapter(pdfThemeContext(), this.PDFitemsList, new ViewCallBackListener() {
            @Override
            public void onItemClicked(int i, final int i2) {
                final PDFFileModel pDFFileModel;
                if (HomeFragment.this.isFilter) {
                    pDFFileModel = HomeFragment.this.adapter.getList().get(i2);
                } else {
                    pDFFileModel = HomeFragment.this.PDFitemsList.get(i2);
                }
                if (i == R.id.card_parent) {
                    HomeFragment.this.openPDF(pDFFileModel.getFile());
                } else if (i == R.id.mcvImgOption) {
                    PDFOptionBottomSheet newInstance = PDFOptionBottomSheet.newInstance(pDFFileModel.getFile().getAbsolutePath(), new RecyclerItemClick() {
                        @Override
                        public void onItemClick(int i3) {
                            if (i3 == 11) {
                                HomeFragment.this.openPDF(pDFFileModel.getFile());
                            } else if (i3 == 22) {
                                HomeFragment.this.editPdf(pDFFileModel.getFile());
                            } else if (i3 == 33) {
                                HomeFragment.this.renamePDF(pDFFileModel, i2);
                            } else if (i3 == 44) {
                                HomeFragment.this.sharePdfFile(pDFFileModel, i2, false);
                            } else if (i3 != 55) {
                            } else {
                                HomeFragment.this.deletePDF(pDFFileModel, i2, false);
                            }
                        }
                    });
                    newInstance.setCancelable(true);
                    newInstance.show(HomeFragment.this.getParentFragmentManager(), "Pdf Option");
                }
            }

            @Override
            public void onItemLongClick(int i, int i2) {
                HomeFragment.this.binding.toolbar.setVisibility(View.GONE);
                HomeFragment.this.binding.llLayoutSelectAll.setVisibility(View.VISIBLE);
                HomeFragment.this.binding.txtSelectCount.setText(String.format("%s Selected", Integer.valueOf(HomeFragment.this.adapter.getFinalSelectedList().size())));
                HomeFragment.this.binding.mcvBottomView.setVisibility(View.GONE);
                HomeFragment.this.binding.fab.setVisibility(View.GONE);
                HomeFragment.this.binding.mcvBottomOption.startAnimation(AnimationUtils.loadAnimation(HomeFragment.this.mContext, R.anim.bottom_up));
                HomeFragment.this.binding.mcvBottomOption.setVisibility(View.VISIBLE);
            }

            @Override
            public void onItemViewCount(int i) {
                HomeFragment.this.binding.txtSelectCount.setText(String.format("%s Selected", Integer.valueOf(i)));
                if (HomeFragment.this.adapter.getFinalSelectedList().size() == 0) {
                    PDFListViewAdapter pDFListViewAdapter = HomeFragment.this.adapter;
                    HomeFragment.this.isSelectAll = false;
                    pDFListViewAdapter.setSelectAll(false);
                    Glide.with(HomeFragment.this.mContext).load(Integer.valueOf((int) R.drawable.ic_uncheck)).into(HomeFragment.this.binding.imgSelect);
                }
            }
        });
        this.binding.rvPdfListView.setAdapter(this.adapter);
        noDataView(this.PDFitemsList);
        this.binding.rvPdfListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                if (HomeFragment.this.binding.llLayoutSelectAll.getVisibility() == View.GONE) {
                    if (i2 > 0) {
                        HomeFragment.this.binding.fab.hide();
                        HomeFragment.this.binding.mcvBottomView.setVisibility(View.GONE);
                    } else {
                        HomeFragment.this.binding.fab.show();
                        HomeFragment.this.binding.mcvBottomView.setVisibility(View.VISIBLE);
                        HomeFragment.this.binding.fab.setVisibility(View.VISIBLE);
                    }
                }
                super.onScrolled(recyclerView, i, i2);
            }
        });
    }

    public void noDataView(List<PDFFileModel> list) {
        this.binding.noData.nodata.setVisibility(list.size() > 0 ? View.GONE : View.VISIBLE);
        if (list.size() == 0 && this.binding.llLayoutSelectAll.getVisibility() == View.VISIBLE) {
            this.binding.toolbar.setVisibility(View.VISIBLE);
            this.binding.llLayoutSelectAll.setVisibility(View.GONE);
            this.binding.mcvBottomOption.setVisibility(View.GONE);
            this.binding.mcvBottomView.setVisibility(View.VISIBLE);
            this.binding.fab.setVisibility(View.VISIBLE);
            this.adapter.setSelectedVisible(false);
            PDFListViewAdapter pDFListViewAdapter = this.adapter;
            this.isSelectAll = false;
            pDFListViewAdapter.setAllSelection(false);
            Glide.with(this.mContext).load(Integer.valueOf((int) R.drawable.ic_uncheck)).into(this.binding.imgSelect);
            this.binding.txtSelectCount.setText(String.format("%s Selected", 0));
        }
    }

    public void getAllPdfFileList() {
        HandleExternalData();
        reloadPdfFileList();
    }

    private void reloadPdfFileList() {
        this.PDFitemsList.clear();
        File file = new File(Constant.DirectoryPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File file2 : listFiles) {
                if (file2.isFile() && file2.length() > 0) {
                    this.PDFitemsList.add(new PDFFileModel(file2, false));
                }
            }
        }
        Collections.sort(this.PDFitemsList, MainActivityExternal1.INSTANCE);
        PDFSortingArrayList(this.SortBy, this.PDFitemsList);
        if (this.isFilter && this.adapter != null) {
            this.adapter.getFilter().filter(this.SearchValue);
        }
    }


    public static int lambda$getAllPdfFileList$10(PDFFileModel pDFFileModel, PDFFileModel pDFFileModel2) {
        int i = ((pDFFileModel2.getFile().lastModified() - pDFFileModel.getFile().lastModified()) > 0L ? 1 : ((pDFFileModel2.getFile().lastModified() - pDFFileModel.getFile().lastModified()) == 0L ? 0 : -1));
        if (i < 0) {
            return -1;
        }
        return i > 0 ? 1 : 0;
    }

    public void openPDF(File file) {
        Intent intent = new Intent(requireContext().getApplicationContext(), DigitalSignatureActivity.class);
        intent.putExtra("ActivityAction", "selectPdf");
        intent.putExtra("filePath", file.getAbsolutePath());
        launchPdfEditor(intent, new BetterActivityResult.OnActivityResult<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult activityResult) {
                HomeFragment.this.m82x8b1fcbd7(activityResult);
            }
        });
    }

    public void editPdf(File file) {
        Uri fromFile = Uri.fromFile(file);
        Intent intent = new Intent(requireContext().getApplicationContext(), DigitalSignatureActivity.class);
        intent.putExtra("ActivityAction", "PDFEdit");
        intent.putExtra("PDFUri", fromFile.toString());
        intent.putExtra("PDFName", file.getName());
        launchPdfEditor(intent, new BetterActivityResult.OnActivityResult<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult activityResult) {
                HomeFragment.this.handleSavedPdfResult(activityResult);
            }
        });
    }

    public void renamePDF(final PDFFileModel pDFFileModel, int i) {
        final FileAlertDialogBinding fileAlertDialogBinding = (FileAlertDialogBinding) DataBindingUtil.inflate(pdfThemeInflater(), R.layout.file_alert_dialog, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        dialog.setContentView(fileAlertDialogBinding.getRoot());
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.setCancelable(false);
        fileAlertDialogBinding.txtTitle.setText("Rename");
        fileAlertDialogBinding.txtHeading.setText("Rename PDF File");
        fileAlertDialogBinding.etFileName.setText(FilenameUtils.removeExtension(pDFFileModel.getFile().getName()));
        fileAlertDialogBinding.txtSave.setText("Update");
        fileAlertDialogBinding.cardSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                HomeFragment.this.m81x2e860759(fileAlertDialogBinding, pDFFileModel, dialog, view);
            }
        });
        fileAlertDialogBinding.cardCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public void m81x2e860759(FileAlertDialogBinding fileAlertDialogBinding, PDFFileModel pDFFileModel, Dialog dialog, View view) {
        try {
            final String obj = fileAlertDialogBinding.etFileName.getText().toString();
            if (pDFFileModel.getFile().getName().equals(obj)) {
                showToast("Old PDF Name and New PDF Name is same.");
            } else {
                if (pDFFileModel.getFile().exists()) {
                    String parent = pDFFileModel.getFile().getParent();
                    File file = new File(parent, obj + ".pdf");
                    PDFFileModel pDFFileModel2 = new PDFFileModel(file, false);
                    if (pDFFileModel.getFile().renameTo(file)) {
                        showToast("Rename Pdf File Successfully");
                        int indexOf = this.PDFitemsList.indexOf(pDFFileModel);
                        if (this.isFilter) {
                            this.PDFitemsList.set(indexOf, pDFFileModel2);
                            int indexOf2 = this.adapter.getList().indexOf(pDFFileModel);
                            if (containsItemFilter(pDFFileModel2.getFile()) && indexOf2 != -1) {
                                this.adapter.getList().set(indexOf2, pDFFileModel2);
                                this.adapter.notifyItemChanged(indexOf2);
                            } else if (indexOf2 != -1) {
                                this.adapter.getList().remove(indexOf2);
                                this.adapter.notifyItemRemoved(indexOf2);
                                PDFListViewAdapter pDFListViewAdapter = this.adapter;
                                pDFListViewAdapter.notifyItemRangeChanged(indexOf2, pDFListViewAdapter.getList().size());
                            }
                            dialog.dismiss();
                            return;
                        }
                        if (this.PDFitemsList.indexOf(pDFFileModel) != -1) {
                            this.PDFitemsList.set(indexOf, pDFFileModel2);
                            this.adapter.notifyItemChanged(indexOf);
                        }
                        if (this.adapter.getList().indexOf(pDFFileModel) != -1) {
                            this.adapter.getList().set(indexOf, pDFFileModel2);
                        }
                    }
                } else {
                    showToast("Original file Source not available");
                }
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean containsItemFilter(File file) {
        return file.getName().toLowerCase().contains(this.SearchValue.toLowerCase());
    }

    public void sharePdfFile(PDFFileModel pDFFileModel, int i, boolean z) {
        if (z) {
            ArrayList<PDFFileModel> finalSelectedList = this.adapter.getFinalSelectedList();
            if (finalSelectedList.size() <= 0) {
                showToast("At Least one item select");
                return;
            }
            Intent intent = new Intent();
            intent.setAction("android.intent.action.SEND_MULTIPLE");
            intent.putExtra("android.intent.extra.SUBJECT", " ");
            intent.setType("application/pdf");
            ArrayList<Uri> arrayList = new ArrayList<>();
            for (PDFFileModel pDFFileModel2 : finalSelectedList) {
                arrayList.add(uriFromFile(new File(pDFFileModel2.getFile().getAbsolutePath())));
            }
            intent.putParcelableArrayListExtra("android.intent.extra.STREAM", arrayList);
            startActivity(intent);
            return;
        }
        Uri uri = null;
        if (this.isFilter) {
            int indexOf = this.PDFitemsList.indexOf(pDFFileModel);
            if (indexOf != -1) {
                uri = uriFromFile(this.PDFitemsList.get(indexOf).getFile());
            }
        } else {
            uri = uriFromFile(pDFFileModel.getFile());
        }
        Intent intent2 = new Intent("android.intent.action.SEND");
        intent2.putExtra("android.intent.extra.STREAM", uri);
        intent2.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent2.setDataAndType(uri, "application/pdf");
        try {
            startActivity(Intent.createChooser(intent2, "share.."));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void deletePDF(final PDFFileModel pDFFileModel, final int i, final boolean z) {
        SignatureDeletetDialogBinding signatureDeletetDialogBinding = (SignatureDeletetDialogBinding) DataBindingUtil.inflate(pdfThemeInflater(), R.layout.signature_deletet_dialog, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        signatureDeletetDialogBinding.txtDescription.setText("Do you really want to delete\nthese PDF?");
        dialog.setContentView(signatureDeletetDialogBinding.getRoot());
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.setCancelable(false);
        signatureDeletetDialogBinding.cardSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                HomeFragment.this.m79x5e565608(z, dialog, pDFFileModel, i, view);
            }
        });
        signatureDeletetDialogBinding.cardCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public void m79x5e565608(boolean z, Dialog dialog, PDFFileModel pDFFileModel, int i, View view) {
        if (z) {
            ArrayList<PDFFileModel> finalSelectedList = this.adapter.getFinalSelectedList();
            for (int i2 = 0; i2 < finalSelectedList.size(); i2++) {
                PDFFileModel pDFFileModel2 = finalSelectedList.get(i2);
                if (this.isFilter) {
                    int indexOf = this.adapter.getList().indexOf(pDFFileModel2);
                    if (indexOf != -1) {
                        pDFFileModel2.getFile().delete();
                        this.adapter.getList().remove(indexOf);
                        this.adapter.notifyItemRemoved(indexOf);
                        PDFListViewAdapter pDFListViewAdapter = this.adapter;
                        pDFListViewAdapter.notifyItemRangeChanged(indexOf, pDFListViewAdapter.getList().size());
                    }
                    int indexOf2 = this.PDFitemsList.indexOf(pDFFileModel2);
                    if (indexOf2 != -1) {
                        this.PDFitemsList.remove(indexOf2);
                    }
                } else {
                    int indexOf3 = this.PDFitemsList.indexOf(pDFFileModel2);
                    if (indexOf3 != -1) {
                        pDFFileModel2.getFile().delete();
                        this.PDFitemsList.remove(indexOf3);
                        this.adapter.notifyItemRemoved(indexOf3);
                        this.adapter.notifyItemRangeChanged(indexOf3, this.PDFitemsList.size());
                        int indexOf4 = this.adapter.getList().indexOf(pDFFileModel2);
                        if (indexOf4 != -1) {
                            this.adapter.getList().remove(indexOf4);
                        }
                    }
                }
            }
            if (!this.isFilter) {
                noDataView(this.PDFitemsList);
            }
            dialog.dismiss();
            return;
        }
        try {
            if (this.isFilter) {
                int indexOf5 = this.PDFitemsList.indexOf(pDFFileModel);
                if (indexOf5 != -1) {
                    this.PDFitemsList.remove(indexOf5);
                    pDFFileModel.getFile().delete();
                    this.adapter.getList().remove(i);
                    this.adapter.notifyItemRemoved(i);
                    PDFListViewAdapter pDFListViewAdapter2 = this.adapter;
                    pDFListViewAdapter2.notifyItemRangeChanged(i, pDFListViewAdapter2.getList().size());
                }
            } else {
                pDFFileModel.getFile().delete();
                int indexOf6 = this.PDFitemsList.indexOf(pDFFileModel);
                if (indexOf6 != -1) {
                    this.PDFitemsList.remove(indexOf6);
                    this.adapter.notifyItemRemoved(indexOf6);
                    noDataView(this.PDFitemsList);
                }
                int indexOf7 = this.adapter.getList().indexOf(pDFFileModel);
                if (indexOf7 != -1) {
                    this.adapter.getList().remove(indexOf7);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.dismiss();
    }

    public void bottomSheetSortPdf() {
        PDFSortByBottomSheet newInstance = PDFSortByBottomSheet.newInstance(this.SortBy, new RecyclerItemClick() {
            @Override
            public void onItemClick(int i) {
                if (i <= 0) {
                    i = 3;
                }
                HomeFragment.this.SortBy = i;
                HomeFragment.this.adapter.setSortBy(HomeFragment.this.SortBy);
                HomeFragment HomeFragment = HomeFragment.this;
                HomeFragment.PDFSortingArrayList(i, HomeFragment.adapter.getList());
            }
        });
        newInstance.setCancelable(true);
        newInstance.show(getParentFragmentManager(), "Sort By");
    }

    public void PDFSortingArrayList(final int i, List<PDFFileModel> list) {

        Collections.sort(list, new Comparator<PDFFileModel>() {
            public int compare(PDFFileModel obj1, PDFFileModel obj2) {
                return HomeFragment.lambda$PDFSortingArrayList$16(i, (PDFFileModel) obj1, (PDFFileModel) obj2); // To compare string values
            }
        });
        this.PDFitemsList = list;
        PDFListViewAdapter pDFListViewAdapter = this.adapter;
        if (pDFListViewAdapter != null) {
            pDFListViewAdapter.setList(list);
            noDataView(this.PDFitemsList);
        }
    }


    public static int lambda$PDFSortingArrayList$16(int i, PDFFileModel pDFFileModel, PDFFileModel pDFFileModel2) {
        if (i == 1) {
            return pDFFileModel.getFile().getName().compareTo(pDFFileModel2.getFile().getName());
        }
        if (i == 2) {
            return pDFFileModel2.getFile().getName().compareTo(pDFFileModel.getFile().getName());
        }
        if (i == 3) {
            int i2 = ((pDFFileModel2.getFile().lastModified() - pDFFileModel.getFile().lastModified()) > 0L ? 1 : ((pDFFileModel2.getFile().lastModified() - pDFFileModel.getFile().lastModified()) == 0L ? 0 : -1));
            if (i2 < 0) {
                return -1;
            }
            return i2 > 0 ? 1 : 0;
        } else if (i == 4) {
            int i3 = ((pDFFileModel2.getFile().lastModified() - pDFFileModel.getFile().lastModified()) > 0L ? 1 : ((pDFFileModel2.getFile().lastModified() - pDFFileModel.getFile().lastModified()) == 0L ? 0 : -1));
            if (i3 < 0) {
                return 1;
            }
            return i3 > 0 ? -1 : 0;
        } else if (i == 5) {
            return Long.compare(pDFFileModel.getFile().length(), pDFFileModel2.getFile().length());
        } else {
            if (i == 6) {
                return Long.compare(pDFFileModel2.getFile().length(), pDFFileModel.getFile().length());
            }
            return 0;
        }
    }

    public Uri uriFromFile(File file) {
        if (Build.VERSION.SDK_INT >= 24) {
            Context applicationContext = requireContext().getApplicationContext();
            return FileProvider.getUriForFile(applicationContext, requireContext().getPackageName() + ".provider", file);
        }
        return Uri.fromFile(file);
    }

    public void createPdfFromBitmap(final Bitmap bitmap) {
        Log.d(TAG, "createPdfFromBitmap bitmap=" + bitmapInfo(bitmap));
        this.disposable.add(Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Log.d(TAG, "create temp pdf start bitmap=" + HomeFragment.this.bitmapInfo(bitmap));
                return HomeFragment.this.createTempPdfFromBitmap(bitmap);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String absolutePath) {
                Log.d(TAG, "create temp pdf success path=" + absolutePath);
                HomeFragment.this.setProgressVisible(false, "temp pdf created");
                if (TextUtils.isEmpty(absolutePath)) {
                    HomeFragment.this.showToast("Something wrong please try again!");
                    return;
                }
                Intent intent = new Intent(HomeFragment.this.requireContext().getApplicationContext(), DigitalSignatureActivity.class);
                intent.putExtra("ActivityAction", "ImageToPdf");
                intent.putExtra("filePath", absolutePath);
                HomeFragment.this.launchPdfEditor(intent, new BetterActivityResult.OnActivityResult<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult activityResult) {
                        HomeFragment.this.m78x22fd89a3(activityResult);
                    }
                });
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Log.e(TAG, "create temp pdf failed", throwable);
                HomeFragment.this.setProgressVisible(false, "create temp pdf failed");
                HomeFragment.this.showToast("Something wrong please try again!");
            }
        }));
    }

    private String createTempPdfFromBitmap(Bitmap bitmap) throws IOException {
        File file = new File(AppConstants.getTempDirectoryPath(), "tempPDF.pdf");
        Log.d(TAG, "createTempPdfFromBitmap target=" + file.getAbsolutePath() + ", bitmap=" + bitmapInfo(bitmap));
        if (file.exists()) {
            file.delete();
        }
        String absolutePath = file.getAbsolutePath();
        float width = PDRectangle.A4.getWidth();
        float height = PDRectangle.A4.getHeight();
        PDDocument pDDocument = new PDDocument();
        try {
            PDPage pDPage = new PDPage(new PDRectangle(width, height));
            pDDocument.addPage(pDPage);
            PDImageXObject createFromImage = JPEGFactory.createFromImage(pDDocument, bitmap, 0.9f);
            PDPageContentStream pDPageContentStream = new PDPageContentStream(pDDocument, pDPage);
            try {
                ScaleImageMatrix scaleImageMatrix = new ScaleImageMatrix(createFromImage.getWidth(), createFromImage.getHeight());
                scaleImageMatrix.scaleToFit(width, height);
                pDPageContentStream.drawImage(createFromImage, (int) ((PDRectangle.A4.getWidth() - scaleImageMatrix.getScaledWidth()) / 2.0f), (int) ((PDRectangle.A4.getHeight() - scaleImageMatrix.getScaledHeight()) / 2.0f), scaleImageMatrix.getScaledWidth(), scaleImageMatrix.getScaledHeight());
            } finally {
                pDPageContentStream.close();
            }
            pDDocument.save(absolutePath);
            Log.d(TAG, "createTempPdfFromBitmap saved exists=" + file.exists() + ", length=" + file.length());
            return absolutePath;
        } finally {
            pDDocument.close();
        }
    }


    public void m78x22fd89a3(ActivityResult activityResult) {
        Log.d(TAG, "ImageToPdf editor result resultCode=" + (activityResult == null ? "null" : activityResult.getResultCode()) + ", data=" + (activityResult == null ? "null" : activityResult.getData()));
        handleSavedPdfResult(activityResult);
        AppConstants.DeleteTempData();
    }

    @SuppressLint("WrongConstant")
    public void OpenCamera() {
        try {
            this.imageFile = File.createTempFile("tempIMG", ".jpg", new File(AppConstants.getTempDirectoryPath()));
            this.cameraImagePath = this.imageFile.getAbsolutePath();
            Log.d(TAG, "camera temp file=" + this.cameraImagePath);
        } catch (IOException e) {
            Log.e(TAG, "create camera temp file failed", e);
            this.waitingForExternalImageResult = false;
            setProgressVisible(false, "create camera temp file failed");
            showToast("Something wrong please try again!");
            return;
        }
        if (this.imageFile == null) {
            Log.d(TAG, "camera temp file is null");
            this.waitingForExternalImageResult = false;
            setProgressVisible(false, "camera temp file null");
            showToast("Something wrong please try again!");
            return;
        }
        Activity activity = this.mContext;
        Uri uriForFile = FileProvider.getUriForFile(activity, requireContext().getPackageName() + ".provider", this.imageFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
        intent.setClipData(ClipData.newUri(requireContext().getContentResolver(), "camera_output", uriForFile));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Log.d(TAG, "open camera output=" + uriForFile + ", file=" + this.cameraImagePath);
        try {
            startActivityForResult(intent, REQUEST_CAMERA_IMAGE);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "open camera failed", e);
            this.waitingForExternalImageResult = false;
            setProgressVisible(false, "open camera failed");
            showToast("Something wrong please try again!");
        } catch (Exception e) {
            Log.e(TAG, "open camera failed", e);
            this.waitingForExternalImageResult = false;
            setProgressVisible(false, "open camera failed");
            showToast("Something wrong please try again!");
        }
    }


    public void m77x95836461(ActivityResult activityResult) {
        this.waitingForExternalImageResult = false;
        Log.d(TAG, "camera result received resultCode=" + (activityResult == null ? "null" : activityResult.getResultCode()));
        if (activityResult != null && activityResult.getResultCode() == -1) {
            this.disposable.add(Observable.fromCallable(new Callable() {
                @Override
                public final Object call() throws Exception {
                    Log.d(TAG, "decode camera image start file=" + (HomeFragment.this.imageFile == null ? "null" : HomeFragment.this.imageFile.getAbsolutePath()));
                    return HomeFragment.this.m75xa0268e4a();
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer() {
                @Override
                public final void accept(Object obj) throws Exception {
                    Log.d(TAG, "decode camera image success bitmap=" + HomeFragment.this.bitmapInfo((Bitmap) obj));
                    HomeFragment.this.m76x9fb0284b((Bitmap) obj);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    Log.e(TAG, "decode camera image failed", throwable);
                    HomeFragment.this.setProgressVisible(false, "decode camera image failed");
                    HomeFragment.this.showToast("Something wrong please try again!");
                }
            }));
        } else {
            setProgressVisible(false, "camera canceled or failed");
        }
    }


    public Bitmap m75xa0268e4a() throws Exception {
        if (this.imageFile == null && !TextUtils.isEmpty(this.cameraImagePath)) {
            this.imageFile = new File(this.cameraImagePath);
        }
        if (this.imageFile == null) {
            Log.d(TAG, "camera image file is null before decode");
            return null;
        }
        Log.d(TAG, "camera image file exists=" + this.imageFile.exists() + ", length=" + this.imageFile.length() + ", path=" + this.imageFile.getAbsolutePath());
        if (!this.imageFile.exists() || this.imageFile.length() <= 0) {
            return null;
        }
        int attributeInt = new ExifInterface(this.imageFile.getAbsolutePath()).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifToDegrees = AppConstants.exifToDegrees(attributeInt);
        Matrix matrix = new Matrix();
        if (exifToDegrees != 0) {
            matrix.preRotate(exifToDegrees);
        }
        Bitmap bitmap = BitmapFactory.decodeFile(this.imageFile.getAbsolutePath());
        if (bitmap == null) {
            Log.d(TAG, "BitmapFactory.decodeFile returned null for camera image");
            return null;
        }
        Bitmap rotatedBitmap = bitmap;
        if (exifToDegrees != 0) {
            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }
        }
        if (!this.imageFile.delete()) {
            Log.d(TAG, "camera temp image delete failed path=" + this.imageFile.getAbsolutePath());
        }
        this.cameraImagePath = null;
        this.imageFile = null;
        return rotatedBitmap;
    }


    public void m76x9fb0284b(Bitmap bitmap) throws Exception {
        if (bitmap == null) {
            setProgressVisible(false, "bitmap null");
            showToast("Something wrong please try again!");
            return;
        }
        createPdfFromBitmap(bitmap);
    }

    @Override
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 1002) {
            if (iArr.length > 0 && iArr[0] == 0) {
                this.waitingForExternalImageResult = true;
                setProgressVisible(true, "show progress");
                OpenCamera();
            } else {
                this.waitingForExternalImageResult = false;
                setProgressVisible(false, "camera permission denied");
                alertCameraPermission();
            }
        }
        if (i == 1001) {
            if (iArr[0] == 0 && iArr[1] == 0) {
                getAllPdfFileList();
            } else if (shouldShowRequestPermissionRationale("android.permission.READ_EXTERNAL_STORAGE") || shouldShowRequestPermissionRationale("android.permission.WRITE_EXTERNAL_STORAGE")) {
                alertStoragePermission(0);
            } else if (iArr[0] == -1) {
                alertStoragePermission(-1);
            } else {
                this.activityLauncher.launch(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + requireContext().getPackageName())), new BetterActivityResult.OnActivityResult() {
                    @Override
                    public final void onActivityResult(Object obj) {
                        HomeFragment.this.m80x11f8af60((ActivityResult) obj);
                    }
                });
            }
        }
    }


    public void m80x11f8af60(ActivityResult activityResult) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            getAllPdfFileList();

        } else {
            if (AppConstants.checkStoragePermissionApi19(this.mContext)) {
                getAllPdfFileList();
            } else if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 1001);
            }
        }


    }

    private void alertCameraPermission() {
        DialogPermissionsBinding dialogPermissionsBinding = (DialogPermissionsBinding) DataBindingUtil.inflate(pdfThemeInflater(), R.layout.dialog_permissions, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        dialog.setContentView(dialogPermissionsBinding.getRoot());
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.show();
        dialogPermissionsBinding.txtTitle.setText("Camera Permission");
        dialogPermissionsBinding.imgPermission.setVisibility(View.GONE);
        dialogPermissionsBinding.txtPermissionMsg.setText("Camera permission is required in order to take Image to PDF feature, please enable permission.");
        dialogPermissionsBinding.txtCancel.setText("Cancel");
        dialogPermissionsBinding.txtSave.setText("Settings");
        dialogPermissionsBinding.cardCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialogPermissionsBinding.cardSave.setOnClickListener(new AnonymousClass14(dialog));
    }


    public class AnonymousClass14 implements View.OnClickListener {
        final Dialog val$dialog;


        AnonymousClass14(Dialog dialog) {
            this.val$dialog = dialog;
        }

        @Override
        public void onClick(View view) {
            this.val$dialog.dismiss();
            HomeFragment.this.activityLauncher.launch(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + requireContext().getPackageName())), MainActivityExternalSynthetic2.INSTANCE);
        }
    }


    public void alertStoragePermission(int i) {
        DialogPermissionsBinding dialogPermissionsBinding = (DialogPermissionsBinding) DataBindingUtil.inflate(pdfThemeInflater(), R.layout.dialog_permissions, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        dialog.setContentView(dialogPermissionsBinding.getRoot());
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.show();
        dialogPermissionsBinding.imgPermission.setVisibility(View.GONE);
        dialogPermissionsBinding.txtPermissionMsg.setText("Storage permission is required in order to provide manage and edit PDF feature, please enable permission.");
        dialogPermissionsBinding.txtCancel.setText("ok");
        dialogPermissionsBinding.txtSave.setText("Settings");
        if (i == -1) {
            dialogPermissionsBinding.cardCancel.setVisibility(View.INVISIBLE);
        }
        dialogPermissionsBinding.cardCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (Build.VERSION.SDK_INT >= 23) {
                    HomeFragment.this.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 1001);
                }
            }
        });
        dialogPermissionsBinding.cardSave.setOnClickListener(new AnonymousClass16(dialog, i));
    }


    public class AnonymousClass16 implements View.OnClickListener {
        final Dialog val$dialog;
        final int val$perCode;

        AnonymousClass16(Dialog dialog, int i) {
            this.val$dialog = dialog;
            this.val$perCode = i;
        }

        @Override
        public void onClick(View view) {
            this.val$dialog.dismiss();
            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + requireContext().getPackageName()));
            BetterActivityResult<Intent, ActivityResult> betterActivityResult = HomeFragment.this.activityLauncher;
            final int i = this.val$perCode;
            betterActivityResult.launch(intent, new BetterActivityResult.OnActivityResult() {
                @Override
                public final void onActivityResult(Object obj) {
                    AnonymousClass16.this.m91x5f4cd026(i, (ActivityResult) obj);
                }
            });
        }


        public void m91x5f4cd026(int i, ActivityResult activityResult) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                HomeFragment.this.getAllPdfFileList();
            } else {
                if (!AppConstants.checkStoragePermissionApi19(HomeFragment.this.mContext)) {
                    HomeFragment.this.alertStoragePermission(i);
                } else {
                    HomeFragment.this.getAllPdfFileList();
                }
            }


        }
    }

    @Override
    public void onDestroyView() {
        mainHandler.removeCallbacksAndMessages(null);
        waitingForExternalImageResult = false;
        disposable.clear();
        binding = null;
        super.onDestroyView();
    }

    public boolean handleBackPressed() {
        if (this.binding.llLayoutSelectAll.getVisibility() == View.VISIBLE) {
            this.binding.toolbar.setVisibility(View.VISIBLE);
            this.binding.llLayoutSelectAll.setVisibility(View.GONE);
            this.binding.mcvBottomOption.setVisibility(View.GONE);
            this.binding.mcvBottomView.setVisibility(View.VISIBLE);
            this.binding.fab.setVisibility(View.VISIBLE);
            this.adapter.setSelectedVisible(false);
            PDFListViewAdapter pDFListViewAdapter = this.adapter;
            this.isSelectAll = false;
            pDFListViewAdapter.setAllSelection(false);
            Glide.with(this.mContext).load(Integer.valueOf((int) R.drawable.ic_uncheck)).into(this.binding.imgSelect);
            this.binding.txtSelectCount.setText(String.format("%s Selected", 0));
            return true;
        } else if (this.searchView != null && !this.searchView.isIconified()) {
            this.searchView.setIconified(true);
            return true;
        }
        return false;
    }


}
