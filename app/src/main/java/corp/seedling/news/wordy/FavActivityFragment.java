package corp.seedling.news.wordy;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.LinearGradient;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import corp.seedling.news.wordy.data.WordContract;
import corp.seedling.news.wordy.data.WordContract.WordEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class FavActivityFragment extends Fragment implements  LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = FavActivityFragment.class.getSimpleName();
    FavAdapter mFavAdapter;
    ListView mFavListView;
    private int mCheckBoxCount = 0;
    private ActionMode mActionMode;
    RowSelectionCallback mRowSelectionCallback;
    private SparseBooleanArray checkStatus;
    SparseBooleanArray sparseBooleanArray;
int savedScrollPos = 0;

    public FavActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.e(TAG, "oncreateView enter");

        getActivity().getSupportLoaderManager().initLoader(1, null, this);
        View rootView = inflater.inflate(R.layout.fragment_fav, container, false);

        //TBD:
        if (savedInstanceState !=null){
            Log.e(TAG, "oncreateView enter : savedInstanceState !=null");
            sparseBooleanArray = savedInstanceState.getParcelable("checkbox_list");
        }



        mFavAdapter = new FavAdapter(getActivity(),null,0);

        mFavListView = (ListView)rootView.findViewById(R.id.fav_lv);
        mFavListView.setAdapter(mFavAdapter);
        mFavListView.setEmptyView(rootView.findViewById(R.id.empty_fav_lv));
        mFavListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "sending Fav word back to FAV Act: " + mFavAdapter.getData(position));

                mRowSelectionCallback.rowSelected(mFavAdapter.getData(position));
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            savedScrollPos = savedInstanceState.getInt("scroll_to", 0);
            mFavListView.setSelection(savedScrollPos);
//            mFavListView.smoothScrollToPosition(savedScrollPos);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e(TAG, "onSaveInstanceState enter");
        for (int i = 0 ; i < checkStatus.size() ; i++)
            Log.e(TAG, "checkStatus position# "+ i + " , has " + checkStatus.get(i));

        outState.putParcelable("checkbox_list", new SparseBooleanArrayParcelable(checkStatus));
        outState.putInt("scroll_to", mFavListView.getFirstVisiblePosition());
        super.onSaveInstanceState(outState);
    }


    public void setRowSelectionCallback(RowSelectionCallback rowSelectionCallback){
        this.mRowSelectionCallback = rowSelectionCallback;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "loader..create");
        return new CursorLoader(getActivity(), WordEntry.CONTENT_URI,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(TAG,"loader onLoadFinished cursor size = " +data.getCount());
        mFavAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mFavAdapter.swapCursor(null);
    }

//    private SparseBooleanArray checkStatus;
    /////////////ADAPTER CLASS ///////////////
    class FavAdapter extends CursorAdapter {
//        private ActionMode mActionMode;


        public class ViewHolder {
            TextView favItemTextView;
            CheckBox checkBox;
        }

        public FavAdapter(Context context, Cursor c, int flags ) {
            super(context, c, flags);
            checkStatus = new SparseBooleanArray();
            Log.e(TAG, "FavAdapter Ctr");

            if (sparseBooleanArray != null) {
                for(int i = 0 ; i < sparseBooleanArray.size() ; i++){
                    if (sparseBooleanArray.get(i, false)) {
                        mCheckBoxCount++;
                        Log.e(TAG, "True at "+i + "increase mCheckBoxCount to " +mCheckBoxCount);
                    }
                }
            }
        }

        public String getData(int pos){
            Cursor cursor1 = getActivity().getContentResolver().query(
                    WordEntry.CONTENT_URI,
                    new String[]{WordEntry.COLUMN_WORD},
                    null,
                    null,
                    null
            );
            cursor1.moveToPosition(pos);
            return cursor1.getString(cursor1.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_WORD));
        }

        @Override
        public View newView(final Context context, final Cursor cursor, ViewGroup parent) {
            Log.e(TAG, "newView enter ");
            View rootView = LayoutInflater.from(context).inflate(R.layout.list_item_fav, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.favItemTextView = (TextView) rootView.findViewById(R.id.list_item_fav_word);
            holder.checkBox = (CheckBox)rootView.findViewById(R.id.fav_checkbox);
            rootView.setTag(holder);

            if (sparseBooleanArray != null) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    checkStatus.put(((Integer) i), sparseBooleanArray.get(i));
                }
                if (mCheckBoxCount > 0) {
                    mActionMode = ((FavActivity) context).startActionMode(new ActionBarCallBack());
                    if (mActionMode != null) {
                        mActionMode.setSubtitle(mCheckBoxCount + " item(s) selected.");
                    }
                }
            }
            return rootView;
        }


        @Override
        public void bindView(View view, final Context context, final Cursor cursor) {
            final int position = cursor.getPosition();
            ViewHolder holder = (ViewHolder) view.getTag();
            final String word = cursor.getString(cursor.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_WORD));
            holder.checkBox.setTag(position);
            holder.favItemTextView.setText(word);
            holder.favItemTextView.setContentDescription(getString(R.string.search_for) + word);

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    boolean genuineClick = (buttonView.isChecked() != isChecked(position));
                    checkStatus.put(((Integer) buttonView.getTag()), isChecked);

                    if (genuineClick) {
                        if (isChecked) {
                            buttonView.setContentDescription(getString(R.string.selected_word)+ word);
                            ++mCheckBoxCount;
                            mActionMode = ((FavActivity) context).startActionMode(new ActionBarCallBack());

                        } else {
                            buttonView.setContentDescription(getString(R.string.unselected_word)+ word);
                            --mCheckBoxCount;
                            if (mCheckBoxCount == 0)
                                mActionMode.finish();
                        }

                        if (mActionMode != null) {
                            mActionMode.setSubtitle(mCheckBoxCount + " item(s) selected.");
                        }
                    }
                }
            });
                holder.checkBox.setChecked(isChecked(position));
        }

        private boolean isChecked(int position){
            return checkStatus.get(position, false);
        }
    }

    class ActionBarCallBack implements ActionMode.Callback{

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_fav, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Edit");
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()){

                case R.id.action_delete:
                    Cursor cursor = null;
                    String word = null;
                    int size = checkStatus.size();

                    for (int i=0; i < size ;i++) {
                        if (checkStatus.get(i)){
                            cursor = (Cursor) mFavAdapter.getItem(i);
                            word = cursor.getString(cursor.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_WORD));

                            getActivity().getContentResolver().delete(
                                    WordEntry.CONTENT_URI,
                                    WordEntry.COLUMN_WORD + " LIKE ?", new String[]{word}

                            );
                            mCheckBoxCount--;
                        }
                    }

                    //clean up the sparse array
                    for (int i=0; i < size ;i++) {
                        if (checkStatus.get(i)) {
                            checkStatus.delete(i);
                        }
                    }

                    mFavAdapter.notifyDataSetChanged();
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }

    public interface RowSelectionCallback{
        void rowSelected(String word);
    }


    @Override
    public void onDestroy() {
        Log.e(TAG, "ondestroy ENTER");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        Log.e(TAG, "ondestroyView ENTER");
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onstop ENTER");
        super.onStop();
    }
    @Override
    public void onStart() {
        Log.e(TAG, "onstart ENTER");
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "oncreate enter");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        Log.e(TAG, "onAttach enter");
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        Log.e(TAG, "onDetach enter");
        super.onDetach();
    }
}
