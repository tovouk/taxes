package com.josehinojo.algoliademo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.MyViewHolder> {
    ArrayList<Account> accountList;
    Context context;
    final private ListItemClickListener itemClickListener;

    public interface ListItemClickListener{
        void onListItemClick(Account account);
    }

    public AccountListAdapter(ArrayList<Account> accountList,Context context,ListItemClickListener listItemClickListener){
        this.accountList = accountList;
        this.context = context;
        this.itemClickListener = listItemClickListener;
    }



    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView name;
        TextView email;
        TextView phone;
        TextView address;
        TextView socialSecurity;
        TextView moneyOwed;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            email = itemView.findViewById(R.id.email);
            phone = itemView.findViewById(R.id.phone);
            address = itemView.findViewById(R.id.address);
            socialSecurity = itemView.findViewById(R.id.social_security);
            moneyOwed = itemView.findViewById(R.id.owed);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            itemClickListener.onListItemClick(accountList.get(position));
        }

    }



    @NonNull
    @Override
    public AccountListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        return new MyViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(@NonNull AccountListAdapter.MyViewHolder holder, int position) {
        Account account = this.accountList.get(position);

        holder.name.setText(account.getName());
        holder.email.setText(account.getEmail());
        holder.phone.setText(account.getPhone());
        holder.address.setText(account.getAddress());
        holder.socialSecurity.setText(String.valueOf(account.getSocialSecurity()));
        holder.moneyOwed.setText(String.valueOf(account.getMoneyOwed()));
    }



    @Override

    public int getItemCount() {
        return accountList.size();
    }

}
