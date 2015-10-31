package corp.seedling.news.wordy;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import corp.seedling.news.wordy.R;

public class FavActivity extends AppCompatActivity implements FavActivityFragment.RowSelectionCallback{

    String mKeyword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav);
        ((FavActivityFragment)getSupportFragmentManager().findFragmentById(R.id.frag_fav)).setRowSelectionCallback(this);
    }

    @Override
    public void onBackPressed() {
        setResult(MainActivity.RESULT_CODE_BACK_PRESSED);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fav, menu);
        return true;
    }


    @Override
    public void rowSelected(String word) {
        this.mKeyword = word;
        setResult(RESULT_OK);
        finish();
        Bundle bundle = new Bundle();
        bundle.putString("src" , "From Fav");
        startActivity(new Intent(this, MainActivity.class).putExtra("keyword", word));
    }
}
