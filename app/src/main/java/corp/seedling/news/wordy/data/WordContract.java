package corp.seedling.news.wordy.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class WordContract {

    public static final String CONTENT_AUTHORITY = "corp.seedling.news.wordy";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_WORDS = "words";

    public static final class WordEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WORDS).build();

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WORDS;

        public static final String TABLE_NAME = "favorites";

        public static final String COLUMN_WORD = "Word";
        public static final String COLUMN_HOT_TODAY = "HotToday";
    }
}