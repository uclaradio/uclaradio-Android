package com.uclaradio.uclaradio.Fragments.ScheduleFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.uclaradio.uclaradio.R;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

  private List<ScheduleData> items;
  private HashMap<String, Integer> dayToNum = new HashMap<>();
  private HashMap<String, String> dayToLongDay = new HashMap<>();

  private Context context;

  private int convertDayToNum(String day) {
    return dayToNum.get(day);
  }

  public ScheduleAdapter(List<ScheduleData> items, Context appContext) {
    context = appContext;

    dayToNum.put("Sun",0); dayToLongDay.put("Sun", "Sunday");
    dayToNum.put("Mon",1); dayToLongDay.put("Mon", "Monday");
    dayToNum.put("Tue",2); dayToLongDay.put("Tue", "Tuesday");
    dayToNum.put("Wed",3); dayToLongDay.put("Wed", "Wednesday");
    dayToNum.put("Thu",4); dayToLongDay.put("Thu", "Thursday");
    dayToNum.put("Fri",5); dayToLongDay.put("Fri", "Friday");
    dayToNum.put("Sat",6); dayToLongDay.put("Sat", "Saturday");

    Comparator<ScheduleData> dateComparator = new Comparator<ScheduleData>() {
      @Override
      public int compare(ScheduleData a, ScheduleData b) {
        Integer aDay = convertDayToNum(a.getDay());
        Integer bDay = convertDayToNum(b.getDay());
        Integer dayComp = aDay.compareTo(bDay);
        if (dayComp != 0) {
          return dayComp;
        }
        else {
          SimpleDateFormat sdf = new SimpleDateFormat("hhaa"); //hourAM/PM
          try {
            Date aTime = sdf.parse(a.getTime().toUpperCase());
            Log.d("aTime", aTime.toString());
            Date bTime = sdf.parse(b.getTime().toUpperCase());
            Log.d("bTime", bTime.toString());

            return aTime.compareTo(bTime);
          } catch (ParseException ex) {
              Log.e("ERROR", ex.getMessage());
              return a.getTime().compareTo(b.getTime());
          }
        }
      }
    };
    Collections.sort(items, dateComparator);

    this.items = items;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedule_item, parent, false);
    final ViewHolder holder = new ViewHolder(itemView);
    itemView.setOnClickListener(new View.OnClickListener() {
      @SuppressLint("SetTextI18n")
      @Override
      public void onClick(View view) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = ((Activity) context).getLayoutInflater()
                .inflate(R.layout.show_bottom_sheet_layout, null);

        ImageView showImage = sheetView.findViewById(R.id.show_image);

        TextView showTitle = sheetView.findViewById(R.id.show_title);
        TextView showGenre = sheetView.findViewById(R.id.show_genre);
        TextView showTime  = sheetView.findViewById(R.id.show_time);
        TextView showDjs   = sheetView.findViewById(R.id.show_djs);
        TextView showBlurb = sheetView.findViewById(R.id.show_blurb);

        int position = holder.getAdapterPosition();
        ScheduleData show = items.get(position);

        Picasso.get().setLoggingEnabled(true);
        Picasso.get()
                .load("https://uclaradio.com" + show.getPictureUrl())
                .resize(250, 250)
                .into(showImage);
        Picasso.get().setLoggingEnabled(false);
        showTitle.setText(show.getTitle());
        showTime.setText(dayToLongDay.get(show.getDay()) + "s at " + show.getTime());

        if (show.getGenre() == null)
          showGenre.setVisibility(View.GONE);
        else
          showGenre.setText(show.getGenre());

        if (show.getBlurb() == null)
          showBlurb.setVisibility(View.GONE);
        else
          showBlurb.setText(show.getBlurb());

        StringBuilder djString = new StringBuilder();
        ArrayList<String> djs = new ArrayList<>(show.getDjs().values());

        switch (djs.size()) {
          case 1:
            djString.append(djs.get(0));
            break;
          case 2:
            djString.append(djs.get(0))
                    .append(" and ")
                    .append(djs.get(1));
            break;
          default:
            for (int i = 0; i < djs.size()-1; i++) {
              djString.append(djs.get(i))
                      .append(", ");
            }
            djString.append("and ")
                    .append(djs.get(djs.size()-1));
        }
        showDjs.setText(djString.toString());

        dialog.setContentView(sheetView);
        dialog.show();
      }
    });

    return holder;
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    holder.setIsRecyclable(false);
    ScheduleData item = items.get(holder.getAdapterPosition());
    holder.text_title.setText(item.getTitle());
    holder.text_time.setText(item.getTime());
    holder.text_day.setText(dayToLongDay.get(item.getDay()));

    if(item.getGenre() == null) {
      holder.text_genre.setVisibility(View.GONE);
    } else {
      holder.text_genre.setText(item.getGenre());
    }

    // If it's the first item in the list or if the day doesn't match the previous
    //  entry's day
    if (holder.getAdapterPosition() == 0
            || !item.getDay().equals(items.get(holder.getAdapterPosition()-1).getDay())) {
        holder.text_day.setVisibility(View.VISIBLE);
    }

    String imageUrl = "https://uclaradio.com" + item.getPictureUrl();
    if (item.getPictureUrl() == null)
      imageUrl = "https://uclaradio.com/img/bear_transparent.png";
    Log.d("TAG", "ALBUM IMAGE URL: " + imageUrl);

    Picasso.get()
            .load(imageUrl)
            .resize(250, 250)
            .into(holder.image_show);
  }

  @Override
  public int getItemCount() {
    return items != null ? items.size() : 0;
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    private TextView text_title;
    private TextView text_genre;
    private TextView text_time;
    private TextView text_day;
    private ImageView image_show;

    public ViewHolder(View itemView) {
      super(itemView);
      this.text_title = itemView.findViewById(R.id.show_title);
      this.text_time = itemView.findViewById(R.id.show_time);
      this.text_genre = itemView.findViewById(R.id.show_genre);
      this.image_show = itemView.findViewById(R.id.schedule_image);
      this.text_day = itemView.findViewById(R.id.schedule_day);
    }
  }
}
