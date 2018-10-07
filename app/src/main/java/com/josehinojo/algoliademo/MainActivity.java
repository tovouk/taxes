package com.josehinojo.algoliademo;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements AccountListAdapter.ListItemClickListener{
    String ApplicationID = BuildConfig.AppID;
    String AdminApiKey = BuildConfig.ApiKey;
    String SearchApiKey = BuildConfig.ApiKey1;

    Client client = new Client(ApplicationID,AdminApiKey);
    Index index = client.getIndex("dev_tax");

    AccountListAdapter accountListAdapter;
    ArrayList<Account> accountList = new ArrayList<>();


    @BindView(R.id.toolbar)Toolbar toolbar;
    @BindView(R.id.recyclerView)RecyclerView recyclerView;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        recyclerView.setHasFixedSize(true);
        accountListAdapter = new AccountListAdapter(accountList,getApplicationContext(),this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(accountListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    //https://stackoverflow.com/questions/33250875/how-to-add-search-bar-with-edit-text-in-toolbar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem searchItem = menu.findItem(R.id.search_badge);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Query algoliaQuery = new Query(query)
                        .setHitsPerPage(50);
                index.searchAsync(algoliaQuery, new CompletionHandler() {
                    @Override
                    public void requestCompleted(JSONObject content, AlgoliaException error) {
                        JSONArray hits = null;
                        try {
                            hits = content.getJSONArray("hits");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Intent intent = new Intent(Intent.ACTION_SEND);
//                        intent.setType("message/rfc822");
//                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"recipient@example.com"});
//                        intent.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
//                        intent.putExtra(Intent.EXTRA_TEXT, content.toString());
//                        startActivity(Intent.createChooser(intent, "Send mail..."));
                        for(int i = 0;i<hits.length();i++){
                            try {
                                JSONObject hit = hits.getJSONObject(i);
                                String firstName = hit.getString("first_name");
                                String lastName = hit.getString("last_name");
                                String email = hit.getString("email");
                                String phone = hit.getString("phone");
                                String streetAddress = hit.getString("streetAddress");
                                String city = hit.getString("City");
                                String state = hit.getString("State");
                                int socialSecurity = hit.getInt("socialSecurity");
                                double moneyOwed = hit.getDouble("amountOwed");
                                Account account = new Account(firstName,lastName,email,phone,streetAddress,
                                        city,state,socialSecurity,moneyOwed);
                                account.setName();
                                account.setAddress();
                                Toast.makeText(getApplicationContext(),account.getName(),Toast.LENGTH_LONG).show();
                                accountList.add(account);
                                Log.i("object",account.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        accountListAdapter.notifyDataSetChanged();
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                accountList.clear();
                accountListAdapter.notifyDataSetChanged();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.search_badge:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onListItemClick(Account account) {

    }
}
