package com.josehinojo.algoliademo;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.transition.Fade;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.algolia.search.saas.helpers.BrowseIterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

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
    Query query;
    int pageNumber;
    int searchPageNumber;
    String methodCalled;

    //from https://stackoverflow.com/questions/26543131/how-to-implement-endless-list-with-recyclerview
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 1;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    @BindView(R.id.toolbar)Toolbar toolbar;
    @BindView(R.id.recyclerView)RecyclerView recyclerView;
    @BindView(R.id.networkError)ImageView networkError;
    @BindView(R.id.errorMessage)TextView errorMessage;

    String errorString;
    int imageID;

    SearchView searchView;

    //    Todo Add tablet layout
    //    todo saveinstancestate to save list on orientation change instead of calling api again
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        recyclerView.setHasFixedSize(true);
        accountListAdapter = new AccountListAdapter(accountList,getApplicationContext(),this);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(accountListAdapter);
        errorString = getResources().getString(R.string.networkError);
        imageID = R.drawable.nowifi;

        methodCalled = "getAll";
        pageNumber = 1;
        searchPageNumber = 1;
        //from https://stackoverflow.com/questions/26543131/how-to-implement-endless-list-with-recyclerview
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0)
                {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if (totalItemCount > previousTotal) {
                            loading = false;
                            previousTotal = totalItemCount;
                        }
                    }
                    if (!loading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        loading = true;
                        pageNumber++;
                        searchPageNumber++;
                        getAllJSON();
                    }

                }

            }
        });
        getAllJSON();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    public void showError(){
        errorMessage.setText(errorString);
        networkError.setImageDrawable(getResources().getDrawable(imageID));
        recyclerView.setVisibility(View.INVISIBLE);
        networkError.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.VISIBLE);

    }

    public void showResults(){
        networkError.setVisibility(View.INVISIBLE);
        errorMessage.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }


    //https://stackoverflow.com/questions/33250875/how-to-add-search-bar-with-edit-text-in-toolbar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem searchItem = menu.findItem(R.id.search_badge);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String stringQuery) {
                methodCalled = "onQueryTextSubmit";
                query = new Query(stringQuery)
                        .setHitsPerPage(10).setPage(searchPageNumber);
                getAllJSON();
                return true;
            };

            @Override
            public boolean onQueryTextChange(String newText) {
                accountList.clear();
                if(newText.length() == 0){
                    methodCalled = "getAll";
                    getAllJSON();
                }
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
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{account.getEmail()});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Debt Collection");
        intent.putExtra(Intent.EXTRA_TEXT, "Hello " + account.getName() + ",\n"+
        "We are contacting you today to let you know that your account with us is negative and " +
                        "we would like to discuss payment plans with you. As of today you owe $" +
                account.getMoneyOwed() + ". We will continue to contact you until we reach you." +
                        " We hope to hear from you soon as this matter is very serious to us.\n" +
                        "Thank you."
        );
        //no choice to call account holder as I do not want to accidentally call a random number
        startActivity(Intent.createChooser(intent, "Contact Account Holder..."));
    }



    public void getAllJSON(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if(netInfo != null && netInfo.isConnected()) {
            showResults();
            if(methodCalled.equals("onQueryTextSubmit")){

                index.searchAsync(query, new CompletionHandler() {
                    @Override
                    public void requestCompleted(JSONObject content, AlgoliaException error) {
                        handleJSON(content);
                        accountListAdapter.notifyDataSetChanged();
                    }
                });
            }else {
                query = new Query("*").setHitsPerPage(10).setPage(pageNumber);
                index.searchAsync(query, new CompletionHandler() {
                    @Override
                    public void requestCompleted(JSONObject content, AlgoliaException error) {
                        handleJSON(content);
                        accountListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }else{
            errorString = getResources().getString(R.string.networkError);
            imageID = R.drawable.nowifi;
            showError();
        }

    }

    public void handleJSON(JSONObject content){
        JSONArray hits = null;
        try {
            hits = content.getJSONArray("hits");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assert hits != null;
        for(int i = 0; i<hits.length(); i++){
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
                accountList.add(account);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(accountList.size() == 0){
            errorString = getResources().getString(R.string.emptySearch);
            imageID = R.drawable.sad;
            showError();
        }
    }

}
