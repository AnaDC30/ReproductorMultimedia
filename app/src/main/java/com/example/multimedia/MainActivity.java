package com.example.multimedia;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Declaración de variables
    private RecyclerView recyclerView_ADC; // RecyclerView para mostrar la lista de canciones
    private ArrayList<String> songDetailsList = new ArrayList<>(); // Lista para almacenar los detalles de las canciones
    private ArrayList<Bitmap> songArtworkList = new ArrayList<>(); // Lista para almacenar las carátulas
    private ArrayList<String> songPaths = new ArrayList<>(); // Lista para almacenar las rutas de las canciones

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vincular RecyclerView al diseño
        recyclerView_ADC = findViewById(R.id.recyclerView_ADC);
        recyclerView_ADC.setLayoutManager(new LinearLayoutManager(this)); // Configurar LinearLayoutManager

        // Cargar canciones desde la carpeta assets
        loadSongsFromAssets();

        // Verificar si la lista está vacía
        if (songDetailsList.isEmpty()) {
            Log.d("MainActivity", "No se encontraron canciones en la carpeta assets.");
        } else {
            Log.d("MainActivity", "Canciones cargadas: " + songDetailsList.toString());
        }

        // Creamos y asignamos el adaptador
        SongsAdapter adapter = new SongsAdapter(songDetailsList, songArtworkList, position -> {
            // Abrir PlayerActivity al hacer clic en un elemento
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putStringArrayListExtra("songPaths", songPaths); // Lista de rutas
            intent.putExtra("position", position); // Posición seleccionada
            startActivity(intent);
        });
        recyclerView_ADC.setAdapter(adapter);
    }


     //Método para cargar canciones desde la carpeta assets.

    private void loadSongsFromAssets() {
        try {
            // Obtener los archivos en la carpeta "assets/music"
            String[] assetsFiles = getAssets().list("music");
            if (assetsFiles != null) {
                for (String fileName : assetsFiles) {
                    if (fileName.endsWith(".mp3")) {
                        String songPath = "music/" + fileName; // Ruta relativa
                        songPaths.add(songPath); // Agregar la ruta a la lista

                        // Obtener la carátula y los metadatos
                        Bitmap artwork = getSongArtwork(songPath);
                        String songDetails = getSongMetadata(songPath, fileName);

                        songDetailsList.add(songDetails); // Agregar los detalles a la lista
                        songArtworkList.add(artwork);    // Agregar la carátula a la lista
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


     //Método para obtener los metadatos de una canción.

    private String getSongMetadata(String songPath, String fileName) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        String artist = "Desconocido";
        String title = "Desconocido";
        String duration = "0";

        try {
            AssetFileDescriptor afd = getAssets().openFd(songPath);
            retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            // Extraer metadatos
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            // Valores por defecto si no hay datos
            if (artist == null || artist.isEmpty()) artist = "Desconocido";
            if (title == null || title.isEmpty()) title = fileName.substring(0, fileName.lastIndexOf("."));
            if (duration == null || duration.isEmpty()) duration = "0";
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }

        // Formatear duración
        long durationInMillis = Long.parseLong(duration);
        String formattedDuration = String.format("%02d:%02d",
                (durationInMillis / 1000) / 60,
                (durationInMillis / 1000) % 60
        );

        return artist + " - " + title + " - " + formattedDuration;
    }


     //Método para obtener la carátula de una canción.

    private Bitmap getSongArtwork(String songPath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            AssetFileDescriptor afd = getAssets().openFd(songPath);
            retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            byte[] artworkData = retriever.getEmbeddedPicture(); // Obtenemos la imagen
            if (artworkData != null) {
                return BitmapFactory.decodeByteArray(artworkData, 0, artworkData.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return null; // Retorna null si no hay carátula
    }


     //Adaptador para el RecyclerView con la clase SongsAdapter.

    private class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {

        private final ArrayList<String> songDetailsList;
        private final ArrayList<Bitmap> songArtworkList;
        private final OnItemClickListener listener;

        public SongsAdapter(ArrayList<String> songDetailsList, ArrayList<Bitmap> songArtworkList, OnItemClickListener listener) {
            this.songDetailsList = songDetailsList;
            this.songArtworkList = songArtworkList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false); //Hemos creado un xml para las cards de las canciones
            return new SongViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
            String[] details = songDetailsList.get(position).split(" - ");

            holder.title.setText(details[1]);
            holder.artist.setText(details[0]);
            holder.duration.setText(details[2]);

            // Configurar carátula
            if (songArtworkList.get(position) != null) {
                holder.artwork.setImageBitmap(songArtworkList.get(position));
            } else {
                holder.artwork.setImageResource(R.drawable.ts); // Imagen por defecto
            }

            holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
        }

        @Override
        public int getItemCount() {
            return songDetailsList.size();
        }

        public class SongViewHolder extends RecyclerView.ViewHolder {
            TextView title, artist, duration;
            ImageView artwork;

            public SongViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.textViewTitle_ADC);
                artist = itemView.findViewById(R.id.textViewArtist_ADC);
                duration = itemView.findViewById(R.id.textViewAlbum_ADC);
                artwork = itemView.findViewById(R.id.imageView_ADC);
            }
        }
    }



     //Interfaz para manejar clics en los elementos.

    interface OnItemClickListener {
        void onItemClick(int position);
    }
}
