package br.fonttracker;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.inflationx.calligraphy3.CalligraphyTypefaceSpan;
import io.github.inflationx.calligraphy3.TypefaceUtils;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private List<Classificador.Reconhecimento> mDataset;
    private String txtDetect;
    Activity activity;
    ClipboardManager clipboard;

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cardView)
        CardView cardView;

        @BindView(R.id.txtFontName)
        TextView txtFontName;

        @BindView(R.id.txtConfianca)
        TextView txtConfianca;

        @BindView(R.id.txtFont)
        TextView txtFont;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    public ResultAdapter(List<Classificador.Reconhecimento> mDataset, String txtDetect, Activity activity) {
        this.mDataset = mDataset;
        this.txtDetect = txtDetect;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        ResultAdapter.ViewHolder vh = new ResultAdapter.ViewHolder(view);
        // Gets a handle to the clipboard service.
        clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.txtFontName.setText(mDataset.get(position).getTitulo());
        holder.txtConfianca.setText("Prob. "+String.valueOf(Math.round(mDataset.get(position).getConfianca()*100))+"%");

        SpannableStringBuilder sBuilder = new SpannableStringBuilder();
        sBuilder.append(txtDetect);
        CalligraphyTypefaceSpan typefaceSpan = null;

        switch (mDataset.get(position).getTitulo()){
            case "broadway":
                typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(activity.getAssets(), "fonts/Broadway.ttf"));
                break;
            case "octobre":
                typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(activity.getAssets(), "fonts/Octobre.ttf"));
                break;
            case "robert":
                typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(activity.getAssets(), "fonts/Robert.ttf"));
                break;
            case "signatra":
                typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(activity.getAssets(), "fonts/Signatra.ttf"));
                break;
        }

        sBuilder.setSpan(typefaceSpan, 0, sBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.txtFont.setText(sBuilder, TextView.BufferType.SPANNABLE);
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipData clip = ClipData.newPlainText("Font name", mDataset.get(position).getTitulo());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(activity, "Font name copied", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
