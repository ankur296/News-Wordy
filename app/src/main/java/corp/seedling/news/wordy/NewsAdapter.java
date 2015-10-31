package corp.seedling.news.wordy;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class NewsAdapter extends BaseAdapter {
    private static final String TAG = NewsAdapter.class.getSimpleName();
    // Declare Variables
    Context mContext;
    LayoutInflater inflater;
    private List<News> mNewsList = new ArrayList<>();
    String mKeyword;

    public NewsAdapter(Context context) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
    }

    public void setKeyword(String keyword){
        this.mKeyword = keyword;
    }

    public void setNewsList(List<News> newsArrayList){
        this.mNewsList = newsArrayList;
    }

    public class ViewHolder {
        TextView usageTextView;
        TextView usageHeadlineView;
    }

    @Override
    public int getCount() {
        return mNewsList.size();
    }

    @Override
    public Object getItem(int position) {
        return mNewsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.list_item_usage, null);
            // Locate the TextViews in listview_item.xml
            holder.usageTextView = (TextView) view.findViewById(R.id.list_item_usage_textview);
            holder.usageHeadlineView = (TextView) view.findViewById(R.id.list_item_usage_headline);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // Set the results into TextViews

        //Extract the news object
        News news = mNewsList.get(position);

        //set the headline
        String headline = news.getHeadline();
        SpannableString content = new SpannableString(headline);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        holder.usageHeadlineView.setText(content);

        //set the snippet
        String formattedUsageText = "..." + news.getSnippet() + "...";
        Log.e(TAG, "formattedUsageText = " + formattedUsageText);
        formattedUsageText =  formattedUsageText.replaceAll("<strong>" , "").replaceAll("</strong>" , "");

        String formattedLower = formattedUsageText.toLowerCase(Locale.US);
        String mKeywordLower = mKeyword.toLowerCase(Locale.US);

        int index = (formattedLower).indexOf(mKeywordLower);
        Spannable wordtoSpan = new SpannableString(Html.fromHtml(formattedUsageText));

        while (index != -1) {

            wordtoSpan.setSpan(
                    new BackgroundColorSpan(Color.YELLOW),
                    index,
                    index + mKeyword.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            wordtoSpan.setSpan(
                    new StyleSpan(Typeface.BOLD_ITALIC),
                    index,
                    index + mKeyword.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            index = formattedLower.indexOf(mKeywordLower , index + mKeywordLower.length());

        }

        holder.usageTextView.setText(wordtoSpan);

        return view;
    }
}
