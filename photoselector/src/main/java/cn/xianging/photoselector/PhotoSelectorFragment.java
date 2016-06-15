package cn.xianging.photoselector;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class PhotoSelectorFragment extends Fragment implements
        PhotoAdapter.OnPhotoItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    static final int PERMISSIONS_REQUEST_CAMERA = 1;
    static final String EXTRA_MAX_PICK = "EXTRA_MAX_PICK";
    static final String EXTRA_PICKED = "EXTRA_PICKED";
    static final String RESTORE_FILE_URI = "FILE_URI";
    static final int RESULT_CAMERA = 0x110;
    static final int RESULT_PICK = 0x111;

    private PhotoSelectorFragmentDelegate delegate;

    GridView.OnItemClickListener mOnPhotoItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // do nothing
        }
    };

    ListView.OnItemClickListener mOnPhotoFolderClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mFolderAdapter.setSelectedIndex((int) id);
            FolderInfo folder = mFolderAdapter.getSelectedFolder();
            mFolderNameText.setText(folder.getFolderName());
            hideFolderList();

            if (currentFolderPosition != position) {
                getLoaderManager().destroyLoader(currentFolderPosition);
                currentFolderPosition = position;
            }
            getLoaderManager().initLoader(currentFolderPosition, null, PhotoSelectorFragment.this);
        }
    };

    private int mMaxPick;
    private List<PhotoInfo> selectedPhotos;
    private int currentFolderPosition = 0;

    private GridView mPhotoGridView;
    private TextView mFolderNameText;
    private TextView mPreviewText;
    private FrameLayout mFolderListViewContainer;
    private ListView mFolderListView;

    private FolderAdapter mFolderAdapter;
    private PhotoAdapter mPhotoAdapter;

    private Uri photoUriForCamera;

    private ImageLoader mImageLoader;
    private OnPhotoSelectedListener mPhotoSelectedListener;
    private OnPhotoTakenListener mPhotoTakenListener;

    public void setPhotoSelectedListener(OnPhotoSelectedListener photoSelectedListener) {
        mPhotoSelectedListener = photoSelectedListener;
    }

    public void setPhotoTakenListener(OnPhotoTakenListener photoTakenListener) {
        mPhotoTakenListener = photoTakenListener;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public static PhotoSelectorFragment newInstance(int maxPick, ArrayList<PhotoInfo> pickedPhotos) {
        PhotoSelectorFragment fragment = new PhotoSelectorFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_MAX_PICK, maxPick);
        args.putSerializable(EXTRA_PICKED, pickedPhotos);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("ALL")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mMaxPick = args.getInt(EXTRA_MAX_PICK, 6);
            if (mMaxPick <= 0 || mMaxPick > 6) {
                mMaxPick = 6;
            }
            Serializable serializable = args.getSerializable(EXTRA_PICKED);
            if (serializable instanceof List) {
                List list = (List) serializable;
                if (!list.isEmpty() && !(list.get(0) instanceof PhotoInfo)) {
                    throw new RuntimeException("you should pass a list with PhotoInfo");
                }
                selectedPhotos = (ArrayList<PhotoInfo>) serializable;
            }
        }

        if (selectedPhotos == null) {
            selectedPhotos = new ArrayList<>();
        }

        this.delegate = new PhotoSelectorFragmentDelegate(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_selector, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mPhotoGridView = (GridView) view.findViewById(R.id.image_grid_view);
        LinearLayout mFolderSelectLayout = (LinearLayout) view.findViewById(R.id.select_folder);
        mFolderSelectLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFolderList();
            }
        });
        mFolderNameText = (TextView) view.findViewById(R.id.folder_name);
        mFolderNameText.setText(FolderInfo.FOLDER_NAME_ALL);
        mPreviewText = (TextView) view.findViewById(R.id.preview);
        mPreviewText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do nothing
            }
        });
        mFolderListViewContainer = (FrameLayout) view.findViewById(R.id.folder_list_view_container);
        mFolderListView = (ListView) view.findViewById(R.id.folder_list_view);

        initFolderListView();
        getLoaderManager().initLoader(currentFolderPosition, null, this);
    }

    private void initFolderListView() {
        List<FolderInfo> folderList = delegate.getFolderList();
        mFolderAdapter = new FolderAdapter(mImageLoader, folderList);
        mFolderListView.setAdapter(mFolderAdapter);
        mFolderListView.setOnItemClickListener(mOnPhotoFolderClick);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_PICK) {

        } else if (requestCode == RESULT_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                PhotoInfo photo = new PhotoInfo(photoUriForCamera.toString());
                if (mPhotoTakenListener != null) {
                    mPhotoTakenListener.onPhotoTaken(photo);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /** camera */

    /**
     * 兼容 M 版本后权限的处理
     */
    public void requestCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                /**
                 * fragment中调用此方法申请权限不会走fragment的回调
                 */
//                ActivityCompat.requestPermissions(
//                        getActivity(),
//                        new String[]{Manifest.permission.CAMERA},
//                        PERMISSIONS_REQUEST_CAMERA
//                );
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_CAMERA
                );
            } else {
                startCameraForResult();
            }
        } else {
            startCameraForResult();
        }
    }

    private void startCameraForResult() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoUriForCamera = getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUriForCamera);
        startActivityForResult(intent, RESULT_CAMERA);
    }

    private Uri getOutputMediaFileUri() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp"
        );
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            return null;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        return Uri.fromFile(mediaFile);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraForResult();
            } else {
                Toast.makeText(getContext(), "您拒绝了对相机的授权", Toast.LENGTH_LONG).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * loader callbacks
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return delegate.createLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (isShowAllPhotos()) {
            mPhotoAdapter = new PhotoAllAdapter(getContext(), data, false, this);
        } else {
            mPhotoAdapter = new PhotoAdapter(getContext(), data, false, this);
        }
        mPhotoAdapter.setPhotoItemPickedListener(this);
        mPhotoGridView.setAdapter(mPhotoAdapter);
        mPhotoGridView.setOnItemClickListener(mOnPhotoItemClick);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPhotoAdapter.swapCursor(null);
    }

    boolean isShowAllPhotos() {
        return currentFolderPosition == 0;
    }

    FolderInfo selectedFolder() {
        return ((FolderAdapter) mFolderListView.getAdapter()).getSelectedFolder();
    }

    /** photo select listener */

    @Override
    public boolean shouldSelectThisPhoto(String photoPath) {
        if (selectedPhotos.size() >= mMaxPick) {
            Toast.makeText(getContext(), String.format("最多选择%d张", mMaxPick), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onPhotoItemSelected(String photoPath, boolean isSelected) {
        if (isSelected) {
            addSelectedPhoto(photoPath);
        } else {
            removeSelectedPhoto(photoPath);
        }
        updateSelectedPhotoCount();
        if (mPhotoSelectedListener != null) {
            mPhotoSelectedListener.onPhotoSelected(selectedPhotos);
        }
    }

    /** photos methods */

    public boolean isPhotoSelected(String photoPath) {
        for (PhotoInfo photo : selectedPhotos) {
            if (photo.getPath().equals(photoPath)) {
                return true;
            }
        }
        return false;
    }

    private void addSelectedPhoto(String photoPath) {
        if (!isPhotoSelected(photoPath)) {
            selectedPhotos.add(new PhotoInfo(photoPath));
        }
    }

    private void removeSelectedPhoto(String photoPath) {
        Iterator<PhotoInfo> it = selectedPhotos.iterator();
        while (it.hasNext()) {
            PhotoInfo photo = it.next();
            if (photo.getPath().equals(photoPath)) {
                it.remove();
                break;
            }
        }
    }

    private void updateSelectedPhotoCount() {
        String countInfo = String.format("已选(%d/%d)", selectedPhotos.size(), mMaxPick);
        mPreviewText.setText(countInfo);
    }

    /** folder list methods */

    private void toggleFolderList() {
        if (mFolderListViewContainer.getVisibility() == View.VISIBLE) {
            hideFolderList();
        } else {
            showFolderList();
        }
    }

    private void showFolderList() {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.listview_up);
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.listview_fade_in);

        mFolderListView.startAnimation(animation);
        mFolderListViewContainer.startAnimation(fadeIn);
        mFolderListViewContainer.setVisibility(View.VISIBLE);
    }

    private void hideFolderList() {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.listview_down);
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.listview_fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFolderListViewContainer.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mFolderListView.startAnimation(animation);
        mFolderListViewContainer.startAnimation(fadeOut);
    }
}
