package br.fonttracker;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.io.Serializable;
import java.util.List;

public interface Classificador {
    public class Reconhecimento implements Serializable{

        private final String id;
        private final String titulo;
        private final Float confianca;

        private RectF localizacao;

        public String getId() {
            return id;
        }

        public String getTitulo() {
            return titulo;
        }

        public Float getConfianca() {
            return confianca;
        }

        public RectF getLocalizacao() {
            return localizacao;
        }

        public void setLocalizacao(RectF localizacao) {
            this.localizacao = localizacao;
        }

        public Reconhecimento(final String id, final String titulo, final Float confianca, final RectF localizacao){
            this.id = id;
            this.titulo = titulo;
            this.confianca = confianca;
            this.localizacao = localizacao;
        }

        @Override
        public String toString() {
            return "Reconhecimento{" +
                    "id='" + id + '\'' +
                    ", titulo='" + titulo + '\'' +
                    ", confianca=" + confianca +
                    ", localizacao=" + localizacao +
                    '}';
        }
    }

    List<Reconhecimento> recognizeImage(Bitmap bitmap);

    void enableStatLogging(final boolean debug);

    String getStatString();

    void close();

}
