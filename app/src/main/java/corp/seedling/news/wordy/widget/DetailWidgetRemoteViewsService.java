package corp.seedling.news.wordy.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import corp.seedling.news.wordy.R;
import corp.seedling.news.wordy.data.WordContract.WordEntry;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    private static final String TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                Log.e(TAG, "onCreate WIDGET");
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                Log.e(TAG, "onDataSetChanged WIDGET");

                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission

                final long identityToken = Binder.clearCallingIdentity();

                //get data for the words which have hotToday col as 1
                data = getContentResolver().query(
                        WordEntry.CONTENT_URI,
                        new String[]{WordEntry.COLUMN_WORD},
                        WordEntry.COLUMN_HOT_TODAY + " = ? ",
                        new String[]{"1"},
                        null,
                        null
                        );
                Cursor cursor = getContentResolver().query(
                        WordEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                        );

                Log.e(TAG, "onDataSetChanged Cursor, hotwords today = "+ DatabaseUtils.dumpCursorToString(data));
                Log.e(TAG, "onDataSetChanged Cursor FULL= "+ DatabaseUtils.dumpCursorToString(cursor));



                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                String word = data.getString(data.getColumnIndexOrThrow(WordEntry.COLUMN_WORD));
                views.setTextViewText(R.id.list_item_widget,word);

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra("keyword", word);
                views.setOnClickFillInIntent(R.id.list_item_widget, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
