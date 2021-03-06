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
import android.support.v7.widget.GridLayoutManager;
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

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class MainActivity extends AppCompatActivity implements AccountListAdapter.ListItemClickListener{

    public static final String ACCOUNTLIST = "accountList";

    /*
    API keys are retrieved from BuildConfig,
    Admin key is unused as no administration privileges are required at this time.
    Perhaps it will come into play in the future, that is why I included it.
     */
    String ApplicationID = BuildConfig.AppID;
    String AdminApiKey = BuildConfig.ApiKey;
    String SearchApiKey = BuildConfig.ApiKey1;

    Client client = new Client(ApplicationID,SearchApiKey);
    Index index = client.getIndex("dev_tax");

    AccountListAdapter accountListAdapter;
    ArrayList<Account> accountList;
    Query query;
    String globalStringQuery;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        recyclerView.setHasFixedSize(true);
        if(savedInstanceState != null){
            if(Objects.requireNonNull(savedInstanceState.getParcelableArrayList(ACCOUNTLIST)).size() > 0){
                accountList = savedInstanceState.getParcelableArrayList(ACCOUNTLIST);
            }
        }else{
            accountList= new ArrayList<>();
        }

        accountListAdapter = new AccountListAdapter(accountList,getApplicationContext(),this);

        final GridLayoutManager layoutManager;
        int columnCount = 1;
        //check if device is a tablet
        if(getResources().getBoolean(R.bool.isTablet)){
            columnCount = 3;
            layoutManager = new GridLayoutManager(this,columnCount);
        }else{
            if(getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
                columnCount = 2;
            }else{
                columnCount = 1;
            }
             layoutManager = new GridLayoutManager(this,columnCount);
        }

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(accountListAdapter);
        errorString = getResources().getString(R.string.networkError);
        imageID = R.drawable.nowifi;

        methodCalled = "getAll";

        getAllJSON();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ACCOUNTLIST,accountList);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        accountList = savedInstanceState.getParcelableArrayList(ACCOUNTLIST);
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
                accountList.clear();
                accountListAdapter.notifyDataSetChanged();
                methodCalled = "onQueryTextSubmit";
                globalStringQuery = stringQuery;
                getAllJSON();
                accountListAdapter.notifyDataSetChanged();
                return true;
            };

            @Override
            public boolean onQueryTextChange(String newText) {

                accountList.clear();
                accountListAdapter.notifyDataSetChanged();
                if(newText.length() == 0){
                    methodCalled = "getAll";
                    getAllJSON();

                }else{
                    methodCalled = "onQueryTextSubmit";
                    globalStringQuery = newText;
                }
                getAllJSON();
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
                query = new Query(globalStringQuery).setHitsPerPage(1000).setPage(0);
                index.searchAsync(query, new CompletionHandler() {
                    @Override
                    public void requestCompleted(JSONObject content, AlgoliaException error) {
                        handleJSON(content);
                        accountListAdapter.notifyDataSetChanged();
                    }
                });
            }else {
                query = new Query("*").setHitsPerPage(1000).setPage(0);
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
        accountListAdapter.notifyDataSetChanged();
        if(accountList.size() == 0){
            errorString = getResources().getString(R.string.emptySearch);
            imageID = R.drawable.sad;
            showError();
        }
    }

}
