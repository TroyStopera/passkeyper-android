package com.passkeyper.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.passkeyper.android.R;
import com.passkeyper.android.UserPreferences;
import com.passkeyper.android.Vault;
import com.passkeyper.android.adapter.EntryAdapter;
import com.passkeyper.android.util.SnackbarUndoDelete;
import com.passkeyper.android.vaultmodel.EntryRecord;

import java.util.Collection;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, EntryAdapter.OnActionListener, SnackbarUndoDelete.SnackBarDeleteListener<EntryRecord>, EntryAdapter.OnEntryExpandedListener, SearchView.OnQueryTextListener {

    private static final int EDIT_REQUEST_CODE = 24;

    private UserPreferences userPreferences;

    private DrawerLayout drawer;
    private RecyclerView entryRecyclerView;
    private EntryAdapter entryAdapter;
    private SearchView searchView;
    private SnackbarUndoDelete<EntryRecord> snackbarUndoDelete;
    private Toast toast;

    private long lastBackPress = -1;

    @Override
    public void onBackPressed() {
        //first try to close the drawer
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        //then try to close the search view
        else if (searchView != null && !searchView.isIconified()) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setIconified(true);
            entryAdapter.showAll();
        }
        //then try to collapse selected entry
        else if (entryAdapter != null && entryAdapter.hasExpandedEntry()) {
            entryAdapter.collapseSelected();
        }
        //if back is pressed twice within 3.5 seconds then sign out
        else if (System.currentTimeMillis() - lastBackPress <= 3500) {
            if (toast != null) toast.cancel();
            Vault vault = Vault.get();
            vault.signOut();
            vault.requestSignIn(this, MainActivity.class);
            finishAffinity();
            super.onBackPressed();
        }
        //show a toast to press back again
        else {
            lastBackPress = System.currentTimeMillis();
            toast = Toast.makeText(this, R.string.main_back_to_sign_out, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                Vault vault = Vault.get();
                vault.signOut();
                vault.requestSignIn(this, MainActivity.class);
                finish();
                return true;
            case R.id.action_sort_alpha_asc:
                userPreferences.setSortOrder(EntryAdapter.SortOrder.AtoZ);
                entryAdapter = new EntryAdapter(this, Vault.get().getManager(), EntryAdapter.SortOrder.AtoZ);
                entryRecyclerView.swapAdapter(entryAdapter, false);
                entryRecyclerView.smoothScrollToPosition(0);
                return true;
            case R.id.action_sort_alpha_desc:
                userPreferences.setSortOrder(EntryAdapter.SortOrder.ZtoA);
                entryAdapter = new EntryAdapter(this, Vault.get().getManager(), EntryAdapter.SortOrder.ZtoA);
                entryRecyclerView.swapAdapter(entryAdapter, false);
                entryRecyclerView.smoothScrollToPosition(0);
                return true;
            case R.id.action_sort_chron_asc:
                userPreferences.setSortOrder(EntryAdapter.SortOrder.OldestFirst);
                entryAdapter = new EntryAdapter(this, Vault.get().getManager(), EntryAdapter.SortOrder.OldestFirst);
                entryRecyclerView.swapAdapter(entryAdapter, false);
                entryRecyclerView.smoothScrollToPosition(0);
                return true;
            case R.id.action_sort_chron_desc:
                userPreferences.setSortOrder(EntryAdapter.SortOrder.NewestFirst);
                entryAdapter = new EntryAdapter(this, Vault.get().getManager(), EntryAdapter.SortOrder.NewestFirst);
                entryRecyclerView.swapAdapter(entryAdapter, false);
                entryRecyclerView.smoothScrollToPosition(0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
            case R.id.nav_sign_in:
                startActivity(new Intent(this, ManageLoginActivity.class));
                break;
        }
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
            Vault.get().getManager().delete(entryRecord);
    }

    @Override
    public void onUndo(Collection<EntryRecord> entryRecords) {
        entryAdapter.addAll(entryRecords);
    }

    @Override
    public void onEntryExpanded(int pos) {
        entryRecyclerView.getLayoutManager().scrollToPosition(pos);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        entryAdapter.filter(query);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        entryAdapter.filter(newText);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_REQUEST_CODE) {
            if (resultCode == EditEntryActivity.RESULT_ENTRY_CREATED)
                entryAdapter.add(data.getParcelableExtra(EditEntryActivity.ENTRY_RECORD_EXTRA_KEY));
            else if (resultCode == EditEntryActivity.RESULT_ENTRY_DELETED)
                entryAdapter.remove(data.getParcelableExtra(EditEntryActivity.ENTRY_RECORD_EXTRA_KEY));
            else if (resultCode == EditEntryActivity.RESULT_ENTRY_UPDATED)
                entryAdapter.collapseSelected();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userPreferences = new UserPreferences(this);
        snackbarUndoDelete = new SnackbarUndoDelete<>(
                findViewById(R.id.main_activity_root),
                getString(R.string.main_entry_deleted),
                getString(R.string.main_entries_deleted),
                this
        );

        drawer = findViewById(R.id.drawer_layout);
        entryRecyclerView = findViewById(R.id.vault_recycler_view);
        entryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        entryRecyclerView.addItemDecoration(new DividerItemDecoration(entryRecyclerView.getContext(), getResources().getConfiguration().orientation));

        findViewById(R.id.fab).setOnClickListener(view -> startActivityForResult(new Intent(MainActivity.this, EditEntryActivity.class), EDIT_REQUEST_CODE));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        entryRecyclerView.setVisibility(View.INVISIBLE);
        if (snackbarUndoDelete != null)
            snackbarUndoDelete.forceDismissSnackbar();
        //collapse the currently selected entry to remove sensitive data from memory
        if (entryAdapter != null)
            entryAdapter.collapseSelected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        entryRecyclerView.setVisibility(View.VISIBLE);
        Vault vault = Vault.get();

        if (vault.hasManager() || vault.loadManager()) {
            if (entryAdapter == null) {
                entryAdapter = new EntryAdapter(this, vault.getManager(), userPreferences.getSortOrder());
                entryAdapter.setOnClickListener(this);
                entryAdapter.setOnEntryExpandedListener(this);
                entryRecyclerView.setAdapter(entryAdapter);
            } else {
                entryAdapter.setVaultManager(vault.getManager());
                entryAdapter.reload();
            }
        } else vault.requestSignIn(this);
    }

}
