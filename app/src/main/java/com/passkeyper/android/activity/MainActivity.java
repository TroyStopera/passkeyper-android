package com.passkeyper.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.passkeyper.android.AppVault;
import com.passkeyper.android.R;
import com.passkeyper.android.adapter.EntryAdapter;
import com.passkeyper.android.prefs.UserPreferences;
import com.passkeyper.android.util.SnackbarUndoDelete;
import com.passkeyper.android.vaultmodel.EntryRecord;

import java.util.Collection;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, EntryAdapter.OnActionListener, SnackbarUndoDelete.SnackBarDeleteListener<EntryRecord>, EntryAdapter.OnEntryExpandedListener {

    public static final int EDIT_REQUEST_CODE = 24;

    private UserPreferences userPreferences;
    private RecyclerView entryRecyclerView;
    private EntryAdapter entryAdapter;
    private SearchView searchView;
    private SnackbarUndoDelete<EntryRecord> snackbarUndoDelete;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //first try to close the drawer
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        //then try to close the search view
        else if (searchView != null && !searchView.isIconified()) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setIconified(true);
        }
        //then try to collapse selected entry
        else if (entryAdapter != null && entryAdapter.hasExpandedEntry()) {
            entryAdapter.collapseSelected();
        }
        //then, finally, sign out
        else {
            //TODO: implement 'press back again to sign out'
            AppVault appVault = AppVault.get();
            appVault.signOut();
            appVault.requestSignIn(this, MainActivity.class);
            finishAffinity();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(entryAdapter);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                AppVault appVault = AppVault.get();
                appVault.signOut();
                appVault.requestSignIn(this, MainActivity.class);
                finish();
                return true;
            case R.id.action_sort_alpha_asc:
                userPreferences.setSortOrder(EntryAdapter.SortOrder.AtoZ);
                entryAdapter.setSortOrder(EntryAdapter.SortOrder.AtoZ);
                return true;
            case R.id.action_sort_alpha_desc:
                userPreferences.setSortOrder(EntryAdapter.SortOrder.ZtoA);
                entryAdapter.setSortOrder(EntryAdapter.SortOrder.ZtoA);
                return true;
            case R.id.action_sort_chron_asc:
                userPreferences.setSortOrder(EntryAdapter.SortOrder.OldestFirst);
                entryAdapter.setSortOrder(EntryAdapter.SortOrder.OldestFirst);
                return true;
            case R.id.action_sort_chron_desc:
                userPreferences.setSortOrder(EntryAdapter.SortOrder.NewestFirst);
                entryAdapter.setSortOrder(EntryAdapter.SortOrder.NewestFirst);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onEditClicked(EntryRecord record) {
        Intent intent = new Intent(this, EditEntryActivity.class);
        intent.putExtra(EditEntryActivity.ENTRY_RECORD_EXTRA_KEY, record);
        startActivityForResult(intent, EDIT_REQUEST_CODE);
    }

    @Override
    public void onDeleteClicked(EntryRecord record) {
        entryAdapter.remove(record);
        snackbarUndoDelete.addUndoable(record);
    }

    @Override
    public void onDelete(Collection<EntryRecord> entryRecords) {
        for (EntryRecord entryRecord : entryRecords)
            AppVault.get().getManager().delete(entryRecord);
    }

    @Override
    public void onUndo(Collection<EntryRecord> entryRecords) {
        entryAdapter.addAll(entryRecords);
    }

    @Override
    public void onEntryExpanded(EntryRecord record, int pos) {
        entryRecyclerView.getLayoutManager().scrollToPosition(pos);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_REQUEST_CODE) {
            if (resultCode == EditEntryActivity.RESULT_ENTRY_CREATED)
                entryAdapter.add((EntryRecord) data.getParcelableExtra(EditEntryActivity.ENTRY_RECORD_EXTRA_KEY));
            else if (resultCode == EditEntryActivity.RESULT_ENTRY_DELETED)
                entryAdapter.remove((EntryRecord) data.getParcelableExtra(EditEntryActivity.ENTRY_RECORD_EXTRA_KEY));
            else if (resultCode == EditEntryActivity.RESULT_ENTRY_UPDATED)
                entryAdapter.collapseSelected();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userPreferences = UserPreferences.get(this);
        snackbarUndoDelete = new SnackbarUndoDelete<>(
                findViewById(R.id.main_activity_root),
                getString(R.string.main_entry_deleted),
                getString(R.string.main_entries_deleted),
                this
        );

        entryRecyclerView = (RecyclerView) findViewById(R.id.vault_recycler_view);
        entryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        entryRecyclerView.addItemDecoration(new DividerItemDecoration(entryRecyclerView.getContext(), getResources().getConfiguration().orientation));


        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, EditEntryActivity.class), EDIT_REQUEST_CODE);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (snackbarUndoDelete != null)
            snackbarUndoDelete.forceDismissSnackbar();
        //collapse the currently selected entry to remove sensitive data from memory
        if (entryAdapter != null)
            entryAdapter.collapseSelected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppVault appVault = AppVault.get();

        if (appVault.hasManager() || appVault.loadManager()) {
            if (entryAdapter == null) {
                entryAdapter = new EntryAdapter(this, appVault.getManager());
                entryAdapter.setSortOrder(userPreferences.getSortOrder());
                entryAdapter.setOnClickListener(this);
                entryAdapter.setOnEntryExpandedListener(this);
                entryRecyclerView.setAdapter(entryAdapter);
            } else {
                entryAdapter.setVaultManager(appVault.getManager());
                entryAdapter.reload();
            }
        } else appVault.requestSignIn(this);
    }

}
