package ru.divizdev.wallpapergallery.presentation.detail.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import ru.divizdev.wallpapergallery.PGApplication;
import ru.divizdev.wallpapergallery.R;
import ru.divizdev.wallpapergallery.entities.ImageCategory;
import ru.divizdev.wallpapergallery.entities.ImageUI;
import ru.divizdev.wallpapergallery.presentation.Router;
import ru.divizdev.wallpapergallery.presentation.detail.adapter.DetailPagerAdapter;
import ru.divizdev.wallpapergallery.presentation.detail.presenter.IDetailPresenter;
import ru.divizdev.wallpapergallery.presentation.detail.presenter.IImageUIListAdapter;

public class DetailActivity extends AppCompatActivity implements IDetailView, DialogConfirm.INoticeDialogListener {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private IDetailPresenter _detailPresenter = PGApplication.getFactory().getDetailPresenter();
    private Router _router = PGApplication.getFactory().getRouter();
    private ViewPager _pager;

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, DetailActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        //bind
        _pager = findViewById(R.id.pager);
        //presenter
        _detailPresenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        _detailPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                _detailPresenter.actionShare(_pager.getCurrentItem());
                return true;

            case R.id.action_about:
                _detailPresenter.actionShowAbout();
                return true;
            case R.id.action_set_wallpaper:
                _detailPresenter.actionSetWallpaper(_pager.getCurrentItem());
                return true;
            case R.id.action_download:
                _detailPresenter.actionSaveFile(_pager.getCurrentItem());
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void showImages(Integer initPosition, IImageUIListAdapter imageUIListAdapter) {
        PagerAdapter pagerAdapter = new DetailPagerAdapter(getSupportFragmentManager(), imageUIListAdapter);
        _pager.setAdapter(pagerAdapter);
        _pager.setCurrentItem(initPosition);
    }

    @Override
    public void showAboutDialog() {
        _router.navToAbout(this);
    }

    @Override
    public void showDialogConfirm() {
        DialogConfirm settingsDialog = new DialogConfirm();
        settingsDialog.show(getSupportFragmentManager(), "");
    }

    @Override
    public void navToDetailScreen() {
        _router.navToMainScreen(this);
    }

    @Override
    public void showErrorPermissionMessage() {
        Toast.makeText(this, R.string.message_error_permission, Toast.LENGTH_LONG).show();

    }


    @Override
    public void showShare(ImageUI imageUI) {
        _router.showShare(this, imageUI);
    }

    @Override
    public void setTitle(ImageCategory category) {
        int id = getResources().getIdentifier(getPackageName()+ ":string/" + category.getKeyResourceName(), null, null);
        setTitle(id);
    }

    @Override
    public void requestPermission() {
        int permission;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            } else {
                _detailPresenter.resultPermission(true, _pager.getCurrentItem());
            }
        } else {
            _detailPresenter.resultPermission(true, _pager.getCurrentItem());
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    _detailPresenter.resultPermission(true, _pager.getCurrentItem());
                } else {
                    _detailPresenter.resultPermission(false, _pager.getCurrentItem());

                }
                break;
        }
    }

    @Override
    public void onDialogConfirm() {
        _detailPresenter.resultConfirmInstallWallpaper(true, _pager.getCurrentItem());
    }

    @Override
    public void onDialogCancel() {
        _detailPresenter.resultConfirmInstallWallpaper(false, _pager.getCurrentItem());
    }
}
