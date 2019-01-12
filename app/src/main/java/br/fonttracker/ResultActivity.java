package br.fonttracker;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.inflationx.calligraphy3.CalligraphyTypefaceSpan;
import io.github.inflationx.calligraphy3.TypefaceUtils;

public class ResultActivity extends AppCompatActivity {

    @BindView(R.id.txtDetect)
    EditText txtDetect;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    ResultAdapter resultAdapter;

    List<Classificador.Reconhecimento> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classification);
        ButterKnife.bind(this);
        InputMethodManager im = (InputMethodManager) this.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        results = (List<Classificador.Reconhecimento>) getIntent().getSerializableExtra("results");
        String txt = getIntent().getStringExtra("txtDetect");
        txtDetect.setText(txt);

        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLinearLayout);

        resultAdapter = new ResultAdapter(results,txt,this);

        recyclerView.setAdapter(resultAdapter);

        txtDetect.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    resultAdapter = new ResultAdapter(results,txtDetect.getText().toString(),ResultActivity.this);
                    recyclerView.setAdapter(resultAdapter);
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(txtDetect.getWindowToken(), 0);
                    handled = true;
                }
                return handled;
            }
        });
    }

}
