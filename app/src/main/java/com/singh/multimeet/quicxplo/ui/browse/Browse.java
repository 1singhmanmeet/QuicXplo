package com.singh.multimeet.quicxplo.ui.browse;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.circulardialog.CDialog;
import com.example.circulardialog.extras.CDConstants;
import com.singh.fileEx.FileEx;
import com.singh.multimeet.quicxplo.AppController;
import com.singh.fileEx.model.FileDirectory;
import com.singh.multimeet.quicxplo.BreadCrumbsListener;
import com.singh.multimeet.quicxplo.OnDirectoryChangeListener;
import com.singh.multimeet.quicxplo.OnItemSelectedListener;
import com.singh.multimeet.quicxplo.OnRecyclerItemClickListener;
import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.Util;
import com.singh.multimeet.quicxplo.adapter.BreadCrumbsAdapter;
import com.singh.multimeet.quicxplo.adapter.FilesAdapter;
import com.singh.multimeet.quicxplo.adapter.StorageChooserAdapter;
import com.singh.multimeet.quicxplo.model.StorageSelection;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class Browse extends AppCompatActivity implements OnItemSelectedListener, SearchView.OnQueryTextListener, BreadCrumbsListener {

    RecyclerView contentList;
    RecyclerView crumbs;
    String TAG = Browse.class.getSimpleName();
    List<FileDirectory> fileDirectoryList = new ArrayList<>();
    List<FileDirectory> selectedList = new ArrayList<>();
    List<StorageSelection> storageSelectionList = new ArrayList<>();
    Map<String, File> mountedDevices = new HashMap<>();
    String dir;
    FileEx fileEx;
    public static int TARGET_DESTINATION = FileEx.INTERNAL;
    public static int SOURCE_DESTINATION = FileEx.INTERNAL;
    public static String EXTERNAL_TARGET_DESTINATION = "";
    public static String SOURCE_TARGET_DESTINATION = "";
    public static int COPY = 11;
    public static int MOVE = 12;
    final int WRITE_REQUEST_CODE = 44;
    FilesAdapter filesAdapter;
    boolean isCopied = false;
    boolean isMovable = false;
    static final int EXTERNAL = 35;
    FloatingActionButton options, copy, cut, paste, rename, details, delete, create, share;
    TextView copy_t, cut_t, rename_t, details_t, delete_t, create_t, share_t;
    Toolbar toolbar;
    Dialog dialog;
    static String MIME_TYPE = "";
    StorageChooserAdapter storageChooserAdapter;
    BreadCrumbsAdapter breadCrumbsAdapter;
    DocumentFile pickedDir;
    RelativeLayout root;
    TextView emptyText;
    ImageView imageView;
    ScrollView scrollView;
    SharedPreferences sharedPreferences;
    private InterstitialAd interstitialAd;
    boolean operationFlag=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.singh.multimeet.quicxplo.R.layout.activity_browse);
        contentList = findViewById(com.singh.multimeet.quicxplo.R.id.contentList);
        sharedPreferences = getSharedPreferences(Util.DIR_DATA, Context.MODE_PRIVATE);

        // creating ads
        MobileAds.initialize(this);
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(com.singh.multimeet.quicxplo.R.string.UNIT_ID));

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                interstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e(TAG, "failed to load ad...");
            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.

                // onNewIntent(getIntent());
            }

        });
        // checking External uri permissions.
        toolbar = findViewById(com.singh.multimeet.quicxplo.R.id.toolbar);
        setSupportActionBar(toolbar);
        root = findViewById(com.singh.multimeet.quicxplo.R.id.root);
        crumbs = findViewById(com.singh.multimeet.quicxplo.R.id.crumbs);
        crumbs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        scrollView = findViewById(com.singh.multimeet.quicxplo.R.id.scroll);


        // Floating option buttons
        copy = findViewById(com.singh.multimeet.quicxplo.R.id.copy);
        cut = findViewById(com.singh.multimeet.quicxplo.R.id.move);
        rename = findViewById(com.singh.multimeet.quicxplo.R.id.rename);
        details = findViewById(com.singh.multimeet.quicxplo.R.id.details);
        paste = findViewById(com.singh.multimeet.quicxplo.R.id.paste);
        delete = findViewById(com.singh.multimeet.quicxplo.R.id.delete);
        create = findViewById(com.singh.multimeet.quicxplo.R.id.create);
        options = findViewById(com.singh.multimeet.quicxplo.R.id.options);
        share = findViewById(com.singh.multimeet.quicxplo.R.id.share);

        // Tooltips
        copy_t = findViewById(com.singh.multimeet.quicxplo.R.id.copy_t);
        cut_t = findViewById(com.singh.multimeet.quicxplo.R.id.move_t);
        rename_t = findViewById(com.singh.multimeet.quicxplo.R.id.rename_t);
        details_t = findViewById(com.singh.multimeet.quicxplo.R.id.details_t);
        delete_t = findViewById(com.singh.multimeet.quicxplo.R.id.delete_t);
        create_t = findViewById(com.singh.multimeet.quicxplo.R.id.create_t);
        share_t = findViewById(com.singh.multimeet.quicxplo.R.id.share_t);

        // creating options menu
        options.setOnClickListener((view -> {
            boolean toggle=(isMovable||isCopied);
           if(toggle){
               options.setImageResource(com.singh.multimeet.quicxplo.R.drawable.options);
               filesAdapter.clearSelectedList();
               filesAdapter.disableSelection();
               paste.setVisibility(View.GONE);
               showOrHideOptions(View.GONE);
               isCopied=false;
               isMovable=false;
           }

           else if (scrollView.getVisibility() == View.GONE) {
                showOrHideOptions(View.VISIBLE);

            } else {
                showOrHideOptions(View.GONE);
            }
        }));

        // set listeners for all option buttons.
        setCreateButtonListener();
        setRenameButtonListener();
        setDeleteButtonListener();
        setCutButtonListener();
        setCopyButtonListener();
        setPasteButtonListener();
        setShareButtonListener();
        setDetailButtonListener();


        contentList.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent() != null) {
            dir = getIntent().getStringExtra(getResources().getString(com.singh.multimeet.quicxplo.R.string.dir_reference));
        }

        if (isExternalAvailable() && dir != null) {
            fileEx = FileEx.newFileManager(dir, this);
            if (sharedPreferences.getString(Util.BASE_URI, "").equals("")
                    && !dir.contains("emulated"))
                startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), EXTERNAL);
            fileEx.setCurrentDir(dir);
            breadCrumbsAdapter = new BreadCrumbsAdapter(fileEx.getCurrentDir(), crumbs);
            breadCrumbsAdapter.setBreadCrumbsListener(this);
            crumbs.setAdapter(breadCrumbsAdapter);
            crumbs.smoothScrollToPosition(fileEx.getCurrentDir().length() - 1);
            loadDirectories();
        }

        setTypeFace();
        setOrRefreshAdapter();
        mountStorage();
        createEmptyMessage();
    }


    void successDialog(String message, int type, int size) {
        new CDialog(this).createAlert(message,
                type,   // Type of dialog
                size)    //  size of dialog
                .setAnimation(CDConstants.SCALE_FROM_BOTTOM_TO_TOP)     //  Animation for enter/exit
                .setDuration(2000)   // in milliseconds
                .setTextSize(CDConstants.NORMAL_TEXT_SIZE)  // CDConstants.LARGE_TEXT_SIZE, CDConstants.NORMAL_TEXT_SIZE
                .show();

    }

    void setShareButtonListener() {


        share.setOnClickListener(view -> {
            if (selectedList.size() == 0) {
                successDialog("Select a file!!!", CDConstants.WARNING, CDConstants.MEDIUM);
                return;
            }
            if (selectedList.size() > 0) {
                operationFlag=true;
                ArrayList<Uri> uris = new ArrayList<>();

                for (FileDirectory file : selectedList) {
                    uris.add(Uri.fromFile(new File(file.getPath())));
                }
                final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("*/*");
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                startActivity(Intent.createChooser(intent, "Send"));
            } else {
                Toast.makeText(getApplicationContext(), "Please select some file.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void setDetailButtonListener() {
        details.setOnClickListener(view -> {
            if (selectedList.size() == 0) {
                successDialog("Select a file!!!", CDConstants.WARNING, CDConstants.MEDIUM);
                return;
            }
            if (selectedList.size() == 1) {
                operationFlag=true;
                Dialog propertiesDialog = new Dialog(this);
                propertiesDialog.setCancelable(true);
                propertiesDialog.getWindow().setBackgroundDrawableResource(com.singh.multimeet.quicxplo.R.drawable.curved_back);
                propertiesDialog.setContentView(com.singh.multimeet.quicxplo.R.layout.properties_dialog);

                TextView path = propertiesDialog.findViewById(com.singh.multimeet.quicxplo.R.id.path);
                TextView size = propertiesDialog.findViewById(com.singh.multimeet.quicxplo.R.id.size);
                TextView lastModified = propertiesDialog.findViewById(com.singh.multimeet.quicxplo.R.id.lastModified);
                TextView type = propertiesDialog.findViewById(com.singh.multimeet.quicxplo.R.id.type);

                // Font setting
                path.setTypeface(AppController.getTypeface());
                size.setTypeface(AppController.getTypeface());
                lastModified.setTypeface(AppController.getTypeface());
                type.setTypeface(AppController.getTypeface());

                // ok button listener
                Button ok = propertiesDialog.findViewById(com.singh.multimeet.quicxplo.R.id.ok);
                ok.setOnClickListener(view1 -> {
                    propertiesDialog.dismiss();
                });

                // setting data for properties
                path.setText(String.format(Locale.US, getResources()
                        .getString(com.singh.multimeet.quicxplo.R.string.path), selectedList.get(0).getPath()));
                size.setText(String.format(Locale.US, getResources()
                        .getString(com.singh.multimeet.quicxplo.R.string.size), selectedList.get(0).getSize()));

                if (selectedList.get(0).getFileOrDir() == FileDirectory.DIR) {
                    type.setText(String.format(Locale.US, getResources()
                            .getString(com.singh.multimeet.quicxplo.R.string.type), "Folder"));
                } else {
                    String ext = MimeTypeMap.getFileExtensionFromUrl(selectedList.get(0).getName().replace(" ", ""));
                    type.setText(String.format(Locale.US, getResources()
                            .getString(com.singh.multimeet.quicxplo.R.string.type), ext));
                }
                lastModified.setText(String.format(Locale.US, getResources()
                        .getString(com.singh.multimeet.quicxplo.R.string.lastModified), selectedList.get(0).getDate()));
                propertiesDialog.show();
            }
        });
    }

    void createEmptyMessage() {

        imageView = new ImageView(this);
        imageView.setImageResource(com.singh.multimeet.quicxplo.R.drawable.empty);
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        imageParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        imageView.setId(com.singh.multimeet.quicxplo.R.id.empty_image);

        emptyText = new TextView(this);
        emptyText.setTextSize(18);
        emptyText.setText(com.singh.multimeet.quicxplo.R.string.empty_text);
        emptyText.setTypeface(AppController.getTypeface());
        emptyText.setId(com.singh.multimeet.quicxplo.R.id.empty_text);

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.addRule(RelativeLayout.BELOW, com.singh.multimeet.quicxplo.R.id.empty_image);
        textParams.setMargins(10, 10, 10, 10);
        textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        imageView.setLayoutParams(imageParams);
        emptyText.setLayoutParams(textParams);

    }

    void showOrHideEmptyMessage(int selection) {

        switch (selection) {

            case View.VISIBLE:
                root.addView(imageView);
                root.addView(emptyText);
                break;

            case View.GONE:
                root.removeView(imageView);
                root.removeView(emptyText);

        }
    }

    // Mounting available storage
    void mountStorage() {
        mountedDevices = FileEx.getAllStorageLocations();
        Iterator iterator = mountedDevices.keySet().iterator();
        String key = "";
        double total, used;
        while (iterator.hasNext()) {
            key = "" + iterator.next();
            fileEx.changeRootDirectory(mountedDevices.get(key).getAbsolutePath());
            storageSelectionList.add(new StorageSelection(mountedDevices.get(key).getAbsolutePath(), key));
        }
        fileEx.changeRootDirectory(dir);
        fileEx.setCurrentDir(dir);
    }

    // Dialog to choose destination for copy and move.
    void createStorageChooser() {
        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setOnDismissListener(dialogInterface -> {
            options.setImageResource(R.drawable.options);
        });
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(com.singh.multimeet.quicxplo.R.drawable.curved_back);
        dialog.setContentView(com.singh.multimeet.quicxplo.R.layout.storage_chooser_dialog);
        RecyclerView chooser = dialog.findViewById(com.singh.multimeet.quicxplo.R.id.chooser);
        chooser.setLayoutManager(new LinearLayoutManager(this));
        chooser.setHasFixedSize(true);
        storageChooserAdapter = new StorageChooserAdapter(storageSelectionList);
        chooser.setAdapter(storageChooserAdapter);
        dialog.show();
    }


    void setStorageChooserListener() {
        if (storageChooserAdapter != null) {
            storageChooserAdapter.setOnRecyclerItemClickListener(new OnRecyclerItemClickListener() {
                @Override
                public void onLongClick(View view, int position) {

                }

                @Override
                public void onClick(View view, int position) {
                    String title = storageSelectionList.get(position).getTitle();

                    // if External storage is selected, then pas base document uri.
                    if (title.charAt(0) == 'E') {

                        TARGET_DESTINATION = FileEx.EXTERNAL;
                    }

                    // for internal just go with normal path.
                    fileEx.changeRootDirectory(storageSelectionList.get(position).getPath());
                    Toast.makeText(getApplicationContext(), "Path: " + storageSelectionList
                            .get(position).getPath(), Toast.LENGTH_SHORT).show();
                    fileEx.setCurrentDir(storageSelectionList.get(position).getPath());
                    loadDirectories();
                    breadCrumbsAdapter.setCrumbList(storageSelectionList.get(position).getPath());
                    setOrRefreshAdapter();
                    dialog.dismiss();
                }
            });
        }
    }

    public Flowable<Integer> copyOrMoveFile(String inputPath, String inputFile, String outputPath, DocumentFile pickedDir, int select) {

        return Flowable.create(new FlowableOnSubscribe<Integer>() {

            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                InputStream in = null;
                OutputStream out = null;
                try {
                    //create output directory if it doesn't exist
                    File dir = new File(outputPath);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    long size = new File(inputPath).length();
                    size /= 1024;
                    long total = 0;

                    Uri uri = Uri.fromFile(new File(inputPath));
                    MIME_TYPE = MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                    in = new FileInputStream(new File(inputPath + inputFile));

                    DocumentFile file = pickedDir.createFile(MIME_TYPE, outputPath);
                    out = getContentResolver().openOutputStream(file.getUri());

                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        total += read / 1024;
                        if (e != null)
                            e.onNext((int) ((total / (double) size) * 100));
                        out.write(buffer, 0, read);
                    }

                    if (select == MOVE) {
                        file.delete();
                        Log.e(TAG, "moved!!!");
                    } else
                        Log.e(TAG, "copied!!!");
                    e.onComplete();
                    in.close();
                    out.flush();
                    out.close();

                } catch (Exception exp) {
                    exp.printStackTrace();
                    Log.e(TAG, exp.getMessage());
                }
            }
        }, BackpressureStrategy.BUFFER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == EXTERNAL) {
            Uri treeUri = data.getData();
            pickedDir = DocumentFile.fromTreeUri(this, treeUri);
            sharedPreferences.edit().putString(Util.BASE_URI, treeUri.toString()).apply();
            //grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            //getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Log.e(TAG, "External URI: " + treeUri.toString() + " extension" + MimeTypeMap.getFileExtensionFromUrl(treeUri.toString()));
        }
    }

    private void setDeleteButtonListener() {
        delete.setOnClickListener((view) -> {
            if (selectedList.size() == 0) {
                successDialog("Select a file!!!", CDConstants.WARNING, CDConstants.MEDIUM);
                return;
            }
            operationFlag=true;
            new AlertDialog.Builder(this)
                    .setCancelable(false).setTitle("Delete")
                    .setMessage("Do you want to delete...?")
                    .setPositiveButton("Yup", (dialogInterface, i) -> {

                        for (FileDirectory fileDirectory : selectedList) {
                            if (fileEx.getCurrentDir().contains("emulated")) {
                                fileEx.delete(fileDirectory.getPath());
                            } else
                                delete(fileDirectory.getPath(), fileDirectory.getName());
                        }

                        Toast.makeText(getApplicationContext(), "Files deleted.", Toast.LENGTH_SHORT).show();
                        filesAdapter.clearSelectedList();
                        filesAdapter.disableSelection();

                        paste.setVisibility(View.GONE);
                        filesAdapter.getOnDirectoryChangeListener()
                                .onDirectoryChange(loadDirectories());
                        showOrHideOptions(View.GONE);
                    }).setNegativeButton("No", ((dialogInterface, i) -> {
            })).show();
        });
    }

    // To delete files from external storage.
    void delete(String path, String name) {


        Flowable.create(new FlowableOnSubscribe<Void>() {
            @Override
            public void subscribe(FlowableEmitter<Void> e) throws Exception {

                Uri treeUri = Uri.parse(sharedPreferences.getString(Util.BASE_URI, ""));
                DocumentFile documentFile = DocumentFile.fromTreeUri(Browse.this, treeUri);
                documentFile = documentFile.findFile(name);
                if (documentFile != null) {
                    documentFile.delete();

                } else {
                    String processedPath = Util.getProcessedPath(new File(path).getParent());
                    documentFile = Util.getDocumentFile(Browse.this, processedPath, name, treeUri);
                    documentFile.delete();
                }
                e.onComplete();

            }
        }, BackpressureStrategy.BUFFER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new FlowableSubscriber<Void>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "delete error: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        //Toast.makeText(getApplicationContext(),"Files deleted.",Toast.LENGTH_SHORT).show();
                        filesAdapter.clearSelectedList();
                        filesAdapter.disableSelection();
                        paste.setVisibility(View.GONE);
                        filesAdapter.getOnDirectoryChangeListener()
                                .onDirectoryChange(loadDirectories());
                        showOrHideOptions(View.GONE);
                        interstitialAd.loadAd(new AdRequest.Builder().build());
                    }
                });
    }


    private void setCutButtonListener() {

        cut.setOnClickListener((view) -> {
            if (selectedList.size() == 0) {
                successDialog("Select a file!!!", CDConstants.WARNING, CDConstants.MEDIUM);
                return;
            }
            options.setImageResource(com.singh.multimeet.quicxplo.R.drawable.cancel1);
            operationFlag=true;
            isCopied = false;
            isMovable = true;
            filesAdapter.disableSelection();
            paste.setVisibility(View.VISIBLE);
            create.setVisibility(View.GONE);
            create_t.setVisibility(View.GONE);
            cut.setVisibility(View.GONE);
            cut_t.setVisibility(View.GONE);
            copy.setVisibility(View.GONE);
            copy_t.setVisibility(View.GONE);
            details.setVisibility(View.GONE);
            details_t.setVisibility(View.GONE);
            rename.setVisibility(View.GONE);
            rename_t.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            delete_t.setVisibility(View.GONE);
            share.setVisibility(view.GONE);
            share_t.setVisibility(View.GONE);

            String dir = fileEx.getCurrentDir();
            if (!dir.contains("emulated")) {
                SOURCE_DESTINATION = FileEx.EXTERNAL;
            }
            createStorageChooser();
            setStorageChooserListener();
        });

    }

    private void setCopyButtonListener() {
        copy.setOnClickListener((view) -> {
            if (selectedList.size() == 0) {
                successDialog("Select a file!!!", CDConstants.WARNING, CDConstants.MEDIUM);
                return;
            }
            options.setImageResource(com.singh.multimeet.quicxplo.R.drawable.cancel1);
            operationFlag=true;
            // filesAdapter.disableSelection();
            isMovable = false;
            isCopied = true;
            filesAdapter.disableSelection();
            delete.setVisibility(View.GONE);
            delete_t.setVisibility(View.GONE);
            paste.setVisibility(View.VISIBLE);
            cut.setVisibility(View.GONE);
            cut_t.setVisibility(View.GONE);
            copy.setVisibility(View.GONE);
            copy_t.setVisibility(View.GONE);
            details.setVisibility(View.GONE);
            details_t.setVisibility(View.GONE);
            rename.setVisibility(View.GONE);
            rename_t.setVisibility(View.GONE);
            create_t.setVisibility(View.GONE);
            create.setVisibility(View.GONE);
            share.setVisibility(view.GONE);
            share_t.setVisibility(View.GONE);
            String dir = fileEx.getCurrentDir();
            if (!dir.contains("emulated")) {
                SOURCE_DESTINATION = FileEx.EXTERNAL;
            }
            createStorageChooser();
            setStorageChooserListener();
        });
    }

    private void setRenameButtonListener() {

        rename.setOnClickListener((view -> {
            if (selectedList.size() == 0) {
                successDialog("Select a file!!!", CDConstants.WARNING, CDConstants.MEDIUM);
                return;
            }
            operationFlag=true;
            Dialog dialog = new Dialog(this);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(com.singh.multimeet.quicxplo.R.drawable.curved_back);
            dialog.setContentView(com.singh.multimeet.quicxplo.R.layout.rename_dialog_view);
            EditText newName = dialog.findViewById(com.singh.multimeet.quicxplo.R.id.new_name);
            newName.setText(selectedList.get(0).getName());
            FloatingActionButton done = dialog.findViewById(com.singh.multimeet.quicxplo.R.id.done);
            done.setOnClickListener((view1 -> {
                if (!newName.getText().equals("")) {
                    if (fileEx.getCurrentDir().contains("emulated")) {
                        fileEx.renameTo(selectedList.get(0).getPath(), newName.getText().toString());
                        filesAdapter.clearSelectedList();
                        filesAdapter.disableSelection();
                        filesAdapter.getOnDirectoryChangeListener()
                                .onDirectoryChange(loadDirectories());
                        showOrHideOptions(View.GONE);
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Renamed successfully!!!", Toast.LENGTH_SHORT).show();

                    } else {
                        Uri treeUri = Uri.parse(sharedPreferences.getString(Util.BASE_URI, ""));
                        DocumentFile documentFile = Util.getDocumentFile(this,
                                Util.getProcessedPath(selectedList.get(0).getPath())
                                , null, treeUri);
                        documentFile.renameTo(newName.getText().toString());
                        filesAdapter.clearSelectedList();
                        filesAdapter.disableSelection();
                        filesAdapter.getOnDirectoryChangeListener()
                                .onDirectoryChange(loadDirectories());
                        showOrHideOptions(View.GONE);
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Renamed successfully!!!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "lease give some same name", Toast.LENGTH_SHORT)
                            .show();
                }
            }));
            dialog.show();
        }));
    }

    private void setCreateButtonListener() {

        create.setOnClickListener(view -> {
            operationFlag=true;
            Dialog dialog = new Dialog(this);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(com.singh.multimeet.quicxplo.R.drawable.curved_back);
            dialog.setContentView(com.singh.multimeet.quicxplo.R.layout.rename_dialog_view);
            TextView title = dialog.findViewById(com.singh.multimeet.quicxplo.R.id.title);
            title.setText("New Folder");
            EditText newName = dialog.findViewById(com.singh.multimeet.quicxplo.R.id.new_name);
            newName.setText("New Folder");
            FloatingActionButton done = dialog.findViewById(com.singh.multimeet.quicxplo.R.id.done);
            done.setOnClickListener(view1 -> {
                if (!newName.getText().toString().equals("")) {
                    File newDir = new File(fileEx.getCurrentDir() + "/" + newName.getText());
                    try {
                        if (!fileEx.getCurrentDir().contains("emulated")) {
                            if (!newDir.exists()) {
                                Uri treeUri = Uri.parse(sharedPreferences.getString(Util.BASE_URI, ""));
                                DocumentFile documentFile = Util.getDocumentFile(this,
                                        Util.getProcessedPath(fileEx.getCurrentDir())
                                        , null, treeUri);
                                documentFile.createDirectory(newName.getText().toString());
                                showOrHideOptions(View.GONE);
                                setOrRefreshAdapter();
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Folder created", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(getApplicationContext(), "Folder already exists!!!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (!newDir.exists()) {
                                showOrHideOptions(View.GONE);
                                setOrRefreshAdapter();
                                newDir.mkdirs();
                                Toast.makeText(getApplicationContext(), "Folder created", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else
                                Toast.makeText(getApplicationContext(), "Folder already exists!!!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Something went wrong!!!", Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(getApplicationContext(), "Please give some name", Toast.LENGTH_SHORT).show();
            });
            dialog.show();

        });
    }

    private void setPasteButtonListener() {

        paste.setOnClickListener((view) -> {
            if (selectedList.size() == 0) {
                successDialog("Select a file!!!", CDConstants.WARNING, CDConstants.MEDIUM);
                return;
            }
            options.setImageResource(R.drawable.options);
            operationFlag=true;
            Dialog progressDialog = new Dialog(this);
            progressDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            progressDialog.getWindow().setBackgroundDrawableResource(com.singh.multimeet.quicxplo.R.drawable.curved_back);
            progressDialog.setContentView(com.singh.multimeet.quicxplo.R.layout.progress_view);
            ProgressBar progressBar = progressDialog.findViewById(com.singh.multimeet.quicxplo.R.id.progressBar);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            TextView percentage = progressDialog.findViewById(com.singh.multimeet.quicxplo.R.id.percentage);
            //Log.e(TAG,"Lis size: "+selectedList.size());
            for (FileDirectory file : selectedList) {

                Flowable<Integer> flowable = null;
                FileInputStream inputStream;
                FileOutputStream outputStream;
                try {
                    inputStream = new FileInputStream(new File(file.getPath()));

                    // Checking target Location
                    if ((TARGET_DESTINATION == FileEx.EXTERNAL && SOURCE_DESTINATION == FileEx.INTERNAL)
                            || (TARGET_DESTINATION == FileEx.EXTERNAL && SOURCE_DESTINATION == FileEx.EXTERNAL)) {
                        Log.e(TAG, "destination Path: " + fileEx.getCurrentDir() + "/" + file.getName());

                        Uri treeUri = Uri.parse(sharedPreferences.getString(Util.BASE_URI, ""));

                        pickedDir = Util.getDocumentFile(this,
                                Util.getProcessedPath(fileEx.getCurrentDir())
                                , null, treeUri);

                        if (isCopied) {
                            flowable = copyOrMoveFile(file.getPath(), "", file.getName(), pickedDir, COPY);
                        } else {
                            flowable = copyOrMoveFile(file.getPath(), "", file.getName(), pickedDir, MOVE);
                        }

                        flowable.observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(new Subscriber<Integer>() {

                                    @Override
                                    public void onSubscribe(Subscription s) {
                                        progressDialog.show();
                                        s.request(Long.MAX_VALUE);
                                    }

                                    @Override
                                    public void onNext(Integer integer) {
                                        progressBar.setProgress(integer);
                                        percentage.setText("Copying " + integer + "%");
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        Log.e(TAG, "Error while subscribing: " + t.getMessage());
                                    }

                                    @Override
                                    public void onComplete() {
                                        filesAdapter.clearSelectedList();
                                        filesAdapter.disableSelection();
                                        paste.setVisibility(View.GONE);
                                        progressDialog.dismiss();
                                        filesAdapter.getOnDirectoryChangeListener()
                                                .onDirectoryChange(loadDirectories());
                                        showOrHideOptions(View.GONE);
                                        successDialog("Success", CDConstants.SUCCESS, CDConstants.NORMAL_TEXT_SIZE);
                                        showOrHideEmptyMessage(View.GONE);
                                        interstitialAd.loadAd(new AdRequest.Builder().build());
                                        isCopied=false;
                                        isMovable=false;
                                    }
                                });
                        return;
                    } else {
                        //Log.e(TAG,"Simple output Stream: ");
                        outputStream = new FileOutputStream(new File(fileEx.getCurrentDir() + "/" + file.getName()));
                    }


                    if (isCopied) {
                        Log.e(TAG, "copy directory: " + fileEx.getCurrentDir());
                        flowable = fileEx.copyOrMoveFile(file.getPath(), inputStream, outputStream, FileEx.COPY);
                    } else {
                        Log.e(TAG, "move directory: " + fileEx.getCurrentDir());
                        flowable = fileEx.copyOrMoveFile(file.getPath(), inputStream, outputStream, FileEx.MOVE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                flowable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new FlowableSubscriber<Integer>() {

                            @Override
                            public void onSubscribe(Subscription s) {
                                progressDialog.show();
                                s.request(Long.MAX_VALUE);
                            }

                            @Override
                            public void onNext(Integer integer) {
                                progressBar.setProgress(integer);
                                percentage.setText("Copying " + integer + "%");
                            }

                            @Override
                            public void onError(Throwable t) {
                                Log.e(TAG, "Error: " + t.getLocalizedMessage());
                            }

                            @Override
                            public void onComplete() {
                                filesAdapter.clearSelectedList();
                                filesAdapter.disableSelection();
                                paste.setVisibility(View.GONE);
                                progressDialog.dismiss();
                                filesAdapter.getOnDirectoryChangeListener()
                                        .onDirectoryChange(loadDirectories());
                                showOrHideOptions(View.GONE);
                                interstitialAd.loadAd(new AdRequest.Builder().build());
                                isCopied=false;
                                isMovable=false;
                            }
                        });
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            dir = intent.getStringExtra(getResources().getString(com.singh.multimeet.quicxplo.R.string.dir_reference));
        }

        if (isExternalAvailable() && dir != null) {
            fileEx = FileEx.newFileManager(dir, this);
            loadDirectories();
            breadCrumbsAdapter = new BreadCrumbsAdapter(fileEx.getCurrentDir(), crumbs);
            breadCrumbsAdapter.setCrumbList(fileEx.getCurrentDir());
            crumbs.setAdapter(breadCrumbsAdapter);
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            fileEx = FileEx.newFileManager(savedInstanceState.getString("dir"), this);

        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setOrRefreshAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.singh.multimeet.quicxplo.R.menu.menu_browse, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(com.singh.multimeet.quicxplo.R.id.search).getActionView();
        //searchView.setOnQueryTextListener(this);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setOrRefreshAdapter();
    }

    public void setOrRefreshAdapter() {
        filesAdapter = new FilesAdapter(this, fileDirectoryList, fileEx, this);
        filesAdapter.setOnAdpaterDirectoryChangeListener(new OnDirectoryChangeListener() {
            @Override
            public void onDirectoryChange(List<?> newList) {
                if (newList.size() == 0)
                    showOrHideEmptyMessage(View.VISIBLE);
                else
                    showOrHideEmptyMessage(View.GONE);
                breadCrumbsAdapter.setCrumbList(fileEx.getCurrentDir());
                crumbs.smoothScrollToPosition(breadCrumbsAdapter.getItemCount() - 1);
            }
        });
        filesAdapter.setContext(this);
        contentList.setAdapter(filesAdapter);
    }

    public List<FileDirectory> loadDirectories() {
        fileDirectoryList.clear();
        for (String name : fileEx.listFiles()) {
            if (fileEx.isFile(name)) {
                fileDirectoryList.add(new FileDirectory(name,
                        FileDirectory.FILE, fileEx.getFileSize(name),
                        fileEx.getAbsoluteInfo(fileEx.getFilePath(name)),
                        fileEx.getFilePath(name)));
            } else {
                fileDirectoryList.add(new FileDirectory(name,
                        FileDirectory.DIR, fileEx.getFileSize(name),
                        fileEx.getAbsoluteInfo(fileEx.getFilePath(name))
                        , fileEx.getFilePath(name)));
            }
        }
        return fileDirectoryList;
    }

    public boolean isExternalAvailable() {

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("dir", dir);
    }

    @Override
    public void onBackPressed() {

        if(scrollView.getVisibility() == View.VISIBLE && !operationFlag){
            showOrHideOptions(View.GONE);
            filesAdapter.disableSelection();
            filesAdapter.clearSelectedList();
            //Toast.makeText(getApplicationContext(),"disable selection",Toast.LENGTH_SHORT).show();
        }
        else if (scrollView.getVisibility() == View.VISIBLE) {

            showOrHideOptions(View.GONE);

        } else if (fileEx == null) {
            NavUtils.navigateUpFromSameTask(this);
        } else if (!fileEx.goUp()) {
            NavUtils.navigateUpFromSameTask(this);
        } else {
            loadDirectories();
            filesAdapter.getOnDirectoryChangeListener().onDirectoryChange(fileDirectoryList);
            if (fileDirectoryList.size() == 0)
                showOrHideEmptyMessage(View.VISIBLE);
            else
                showOrHideEmptyMessage(View.GONE);
            breadCrumbsAdapter.setCrumbList(fileEx.getCurrentDir());
            crumbs.smoothScrollToPosition(breadCrumbsAdapter.getItemCount() - 1);

        }
    }

    public void setTypeFace() {
        try {
            Typeface typeface = AppController.getTypeface();
            for (Field field : this.getClass().getFields()) {
                if (field.get(this) instanceof TextView) {
                    ((TextView) field.get(this)).setTypeface(typeface);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemListChanged(List<FileDirectory> list) {
        this.selectedList = list;
        Log.d(TAG, "onItem added: " + selectedList.size());
        if (list.size() > 1) {
            cut.setVisibility(View.VISIBLE);
            cut_t.setVisibility(View.VISIBLE);
            copy.setVisibility(View.VISIBLE);
            copy_t.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            delete_t.setVisibility(View.VISIBLE);
            details.setVisibility(View.GONE);
            details_t.setVisibility(View.GONE);
            rename.setVisibility(View.GONE);
            rename_t.setVisibility(View.GONE);
            create.setVisibility(View.INVISIBLE);
            create_t.setVisibility(View.INVISIBLE);
            paste.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        } else if (list.size() == 0) {
            showOrHideOptions(View.GONE);
        } else {
            showOrHideOptions(View.VISIBLE);
        }
    }

    public void showOrHideOptions(int select) {
        switch (select) {

            case View.GONE:
                scrollView.setVisibility(View.GONE);
                break;

            case View.VISIBLE:

                scrollView.setVisibility(View.VISIBLE);
                cut.setVisibility(View.VISIBLE);
                copy.setVisibility(View.VISIBLE);
                details.setVisibility(View.VISIBLE);
                rename.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
                share.setVisibility(View.VISIBLE);
                create.setVisibility(View.VISIBLE);

                cut_t.setVisibility(View.VISIBLE);
                copy_t.setVisibility(View.VISIBLE);
                details_t.setVisibility(View.VISIBLE);
                rename_t.setVisibility(View.VISIBLE);
                delete_t.setVisibility(View.VISIBLE);
                create_t.setVisibility(View.VISIBLE);
                share_t.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(getApplicationContext(), "" + query, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        return false;
    }

    @Override
    public void onCrumbSelected(String requiredPath) {
        if (requiredPath == null) {
            Toast.makeText(getApplicationContext(), "This region is not accessible.", Toast.LENGTH_SHORT).show();
        } else {
            fileEx.setCurrentDir(requiredPath);
            breadCrumbsAdapter.setCrumbList(fileEx.getCurrentDir());

            filesAdapter.getOnDirectoryChangeListener().onDirectoryChange(loadDirectories());
            crumbs.smoothScrollToPosition(breadCrumbsAdapter.getItemCount() - 1);
            if (fileDirectoryList.size() == 0)
                showOrHideEmptyMessage(View.VISIBLE);
            else
                showOrHideEmptyMessage(View.GONE);

        }
    }
}
