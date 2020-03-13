package kst.ksti.chauffeur.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AutoCompleteParse;
import kst.ksti.chauffeur.common.RecyclerViewAdapterCallback;
import kst.ksti.chauffeur.common.SearchEntity;
import kst.ksti.chauffeur.utility.Logger;

/**
 * Created by KJH on 2017-11-06.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<SearchEntity> itemLists = new ArrayList<>();
    private RecyclerViewAdapterCallback callback;
    private int destinationListTotalCount = 0;
    private int page = 0;

    public int getDestinationListTotalCount() {
        return destinationListTotalCount;
    }

    public void setDestinationListTotalCount(int destinationListTotalCount) {
        this.destinationListTotalCount = destinationListTotalCount;
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView address;

        CustomViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            address = itemView.findViewById(R.id.tv_address);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final int ItemPosition = position;

        if(itemLists.size() <= position)
        {
            Logger.e("LOG1@@@ : itemLists size = " + itemLists.size() + ", ItemPosition = " + ItemPosition);
            return;
        }

        if( holder instanceof CustomViewHolder) {
            final CustomViewHolder viewHolder = (CustomViewHolder)holder;

            viewHolder.title.setText(itemLists.get(position).getTitle());
            viewHolder.address.setText(itemLists.get(position).getAddress());

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onItemClick(ItemPosition, itemLists.get(ItemPosition));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemLists.size();
    }

    public void setData(ArrayList<SearchEntity> itemLists) {
        if(page <= 1)
            this.itemLists = itemLists;
        else
        {
            for(int i = 0; i < itemLists.size(); i++)
            {
                this.itemLists.add(itemLists.get(i));
            }
        }
    }

    public void setCallback(RecyclerViewAdapterCallback callback) {
        this.callback = callback;
    }

    public void clear() {
        itemLists.clear();
        notifyDataSetChanged();
    }

    public void filter(String keyword, int page) {
        if (keyword.length() > 0) {
            // 첫페이지 로딩 하는게 아니라면 리스트를 추가로 받는 것이다.
            if(page <= 1)
                clear();

            this.page = page;

            Logger.d("LOG2## : TMap 목적지 " + page + "페이지 요청");
            AutoCompleteParse parser = new AutoCompleteParse(this, page);
            parser.execute(keyword);
        }
    }
}
