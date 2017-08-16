package io.github.mthli.Ninja.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import io.github.mthli.Ninja.Database.Record;
import io.github.mthli.Ninja.R;
import io.github.mthli.Ninja.Utils.BookmarksUtil;

import java.util.List;

public class RecordAdapter extends ArrayAdapter<Record> {
    private Context context;
    private int layoutResId;
    private List<Record> list;

    public RecordAdapter(Context context, int layoutResId, List<Record> list) {
        super(context, layoutResId, list);
        this.context = context;
        this.layoutResId = layoutResId;
        this.list = list;
    }

    private static class Holder {
        TextView title;
        RelativeTimeTextView time;
        TextView url;
        ImageView fav;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
            holder = new Holder();
            holder.title = (TextView) view.findViewById(R.id.record_item_title);
            holder.time = (RelativeTimeTextView) view.findViewById(R.id.record_item_time);
            holder.url = (TextView) view.findViewById(R.id.record_item_url);
            holder.fav = (ImageView) view.findViewById(R.id.favicons);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        Record record = list.get(position);
        holder.title.setText(record.getTitle());
        if (record.getTime() == 0){
            holder.time.setVisibility(View.INVISIBLE);
        } else {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setReferenceTime(record.getTime());
        }

        holder.url.setText(record.getURL());

        try{
            holder.fav.setImageResource(R.drawable.def_favicons);
            if (record.getFaviconResId() != 0){
                holder.fav.setImageResource(record.getFaviconResId());
            } else {
                String favFile = record.getFaviconFile();
                BookmarksUtil bkUtil = BookmarksUtil.getInstance(context);
                Bitmap bitmap = bkUtil.getFaviconsBitmap(favFile);
                if (bitmap != null)
                    holder.fav.setImageBitmap(bitmap);
            }
        } catch (Exception e){

        }
        return view;
    }
}