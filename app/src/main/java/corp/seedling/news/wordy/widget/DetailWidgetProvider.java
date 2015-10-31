package corp.seedling.news.wordy.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import corp.seedling.news.wordy.DetailActivity;
import corp.seedling.news.wordy.FavActivity;
import corp.seedling.news.wordy.MainActivity;
import corp.seedling.news.wordy.R;
import corp.seedling.news.wordy.sync.MySyncAdapter;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetProvider extends AppWidgetProvider {

    private static final String TAG = DetailWidgetProvider.class.getSimpleName();

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
Log.e(TAG, "onUpdate WIDGET");
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_detail);

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, FavActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }

            boolean useMainActivity = context.getResources().getBoolean(R.bool.use_main_activity);

            Intent clickIntentTemplate = useMainActivity
                    ? new Intent(context, MainActivity.class)
                    : new Intent(context, DetailActivity.class);

//            clickIntentTemplate.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            clickIntentTemplate.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            clickIntentTemplate.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setPendingIntentTemplate(R.id.fav_lv_widget, clickPendingIntentTemplate);
            views.setEmptyView(R.id.fav_lv_widget, R.id.empty_fav_lv_widget);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        Log.e(TAG, "Rxd broadcast to widget to UPDATE ");
        if (MySyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {


            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.fav_lv_widget);
        }
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {

        views.setRemoteAdapter(R.id.fav_lv_widget,
                new Intent(context, DetailWidgetRemoteViewsService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {

        views.setRemoteAdapter(0, R.id.fav_lv_widget,
                new Intent(context, DetailWidgetRemoteViewsService.class));
    }
}
