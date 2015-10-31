package corp.seedling.news.wordy;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ClearableEditText extends RelativeLayout
{
    LayoutInflater inflater = null;
    EditText edit_text;
    Button btn_clear;
    Context mContext;
    private static final String TAG = ClearableEditText.class.getSimpleName();

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initViews(context);
        mContext = context;
    }

    public ClearableEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initViews(context);
        mContext = context;
    }

    public ClearableEditText(Context context)
    {
        super(context);
        initViews(context);
        mContext = context;
    }


    void initViews(final Context context)
    {
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.clearable_edit_text, this, true);
        edit_text = (EditText) findViewById(R.id.clearable_edit);
        btn_clear = (Button) findViewById(R.id.clearable_button_clear);
        btn_clear.setVisibility(RelativeLayout.INVISIBLE);
        clearText();
        showHideClearButton();

        edit_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    ((MainActivity) getContext()).onWordEntered(v.getText().toString());
                    InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(edit_text.getWindowToken(), 0);
                    edit_text.setSelectAllOnFocus(true);
                    return true;
                }
                return false;
            }
        });
    }

    public void hideKeyboard() {
        Log.e(TAG, "HIDE KEYPAD");
        try {
            InputMethodManager in = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = findViewById(android.R.id.content);
            in.showSoftInput(view,
                    InputMethodManager.SHOW_FORCED);
        } catch (Throwable e) {
            // handle any un-expected UI issue if happened
        }

    }

    void clearText()
    {
        btn_clear.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                edit_text.setText("");
                hideKeyboard();
            }
        });
    }

    void showHideClearButton()
    {
        edit_text.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.length() > 0)
                    btn_clear.setVisibility(RelativeLayout.VISIBLE);
                else
                    btn_clear.setVisibility(RelativeLayout.INVISIBLE);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }
            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });
    }

    public Editable getText()
    {
        Editable text = edit_text.getText();
        return text;
    }


    public interface EditTextCallback {
        /**
         * Callback for when a word has been enetered.
         */
        public void onWordEntered(String word);
    }
}
