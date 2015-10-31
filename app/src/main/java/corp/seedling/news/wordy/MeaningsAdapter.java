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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MeaningsAdapter extends BaseAdapter {
    private static final String TAG = MeaningsAdapter.class.getSimpleName();
    // Declare Variables
    Context mContext;
    LayoutInflater inflater;
    private List<Generic> mGenericList = new ArrayList<>();
    String mKeyword;
    MainActivityFragment mMainActivityFragment;

    public MeaningsAdapter(Context context, MainActivityFragment mainActivityFragment) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        mMainActivityFragment = mainActivityFragment;
    }

    public void setKeyword(String keyword){
        Log.e(TAG, "setKeyword Called: " + keyword);
        this.mKeyword = keyword;
    }


    public void setGenericList(List<Generic> genericArrayList){
        this.mGenericList = genericArrayList;
    }


    public class ViewHolder {
        @Bind(R.id.list_item_usage_textview) TextView usageTextView;
        @Bind(R.id.list_item_usage_headline) TextView usageHeadlineView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getCount() {
        return mGenericList.size();
    }

    @Override
    public Object getItem(int position) {
        return mGenericList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;

        if (view == null) {

            view = inflater.inflate(R.layout.list_item_usage, null);
            holder = new ViewHolder(view);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }


        String mKeywordLower = mKeyword.toLowerCase(Locale.US);
        //set the headline
        String headline = mGenericList.get(position).getText1();

        //To highlight keyword in the headline also
        String formattedHeadline = headline.toLowerCase(Locale.US).replaceAll("’", "'");
        int indexHeadline = formattedHeadline.indexOf(mKeywordLower);

        SpannableString content = new SpannableString(headline);

        while (indexHeadline != -1) {

            content.setSpan(
                    new BackgroundColorSpan(Color.YELLOW),
                    indexHeadline,
                    indexHeadline + mKeyword.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            content.setSpan(
                    new StyleSpan(Typeface.BOLD_ITALIC),
                    indexHeadline,
                    indexHeadline + mKeyword.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            indexHeadline = formattedHeadline.indexOf(mKeywordLower , indexHeadline + mKeywordLower.length());
//            Log.e(TAG, " indexHeadline Recalc = " + indexHeadline);
        }


        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//        Log.e(TAG, " final content = " + content);
        holder.usageHeadlineView.setText(content);

        String formattedUsageText = mGenericList.get(position).getText2();
//        Log.e(TAG, "formattedUsageText = " + formattedUsageText);
        formattedUsageText =  formattedUsageText
                .replaceAll("<strong>", "")
                .replaceAll("</strong>", "")
                .replaceAll("’", "'");
        String formattedLower = formattedUsageText.toLowerCase(Locale.US);

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
