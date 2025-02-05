package com.example.multimedia;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


import java.io.IOException;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    //Desclaramos las variables

    private ImageView imageView_ADC;
    private TextView textArtits_ADC, textViewSongTitle_ADC;
    private SeekBar seekBar_ADC;
    private ImageButton buttonPrevious_ADC, buttonRewind_ADC, buttonPlayPause_ADC, buttonStop_ADC, buttonForward_ADC, buttonNext_ADC;
    private Button buttonVolver_ADC;

    //MediaPlayer para reproducir las canciones
    private MediaPlayer mediaPlayer;

    //Pasamos la lista de canciones y posicion
    private ArrayList<String> songPaths;
    private int currentPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //Cargamos las varaibles
        imageView_ADC = findViewById(R.id.imageView_ADC);
        textArtits_ADC = findViewById(R.id.textArtits_ADC);
        textViewSongTitle_ADC = findViewById(R.id.textViewSongTitle_ADC);
        seekBar_ADC = findViewById(R.id.seekBar_ADC);
        buttonPrevious_ADC = findViewById(R.id.buttonPrevious_ADC);
        buttonRewind_ADC = findViewById(R.id.buttonRewind_ADC);
        buttonPlayPause_ADC = findViewById(R.id.buttonPlayPause_ADC);
        buttonStop_ADC = findViewById(R.id.buttonStop_ADC);
        buttonForward_ADC = findViewById(R.id.buttonForward_ADC);
        buttonNext_ADC = findViewById(R.id.buttonNext_ADC);
        buttonVolver_ADC = findViewById(R.id.buttonVolver_ADC);

        //Recuperamos los datos del intent de la actividad principal
        Intent intent = getIntent();
        if (intent != null) {
            songPaths = intent.getStringArrayListExtra("songPaths"); //lista de las canciones
            currentPosition = intent.getIntExtra("position", 0); //posición inicial
        }
        if (songPaths == null || songPaths.isEmpty()) {
            //Manejamos el caso cuando no haya canciones
            Toast.makeText(this, "No se pudo cargar la lista de canciones", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //Configuramos el MediaPlayer
        mediaPlayer = new MediaPlayer();
        playSong(); // Reproduce la cancion seleccionada

        //Configuramos el Listener del Seekbar
        seekBar_ADC.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress); // Ajustamos la posición del MediaPlayer
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { //Pausa la reproducción miestras se ajusta
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { //Reanuda la reproducción después de ajustarla

            }
        });


        //Configuramos los botones
        buttonPrevious_ADC.setOnClickListener(view -> playPreviousSong()); //Reproducir la cancion anterior
        buttonRewind_ADC.setOnClickListener(view -> rewind10sec()); //Retroceder 10 segundos
        buttonPlayPause_ADC.setOnClickListener(view -> playPause()); //Reproducir o pausar
        buttonStop_ADC.setOnClickListener(view -> stopPlay()); //Detiene la reproduccion
        buttonForward_ADC.setOnClickListener(view -> forward10Sec()); //Avanza 10 segundos
        buttonNext_ADC.setOnClickListener(view -> nextSong()); //Siguiente cancion
        buttonVolver_ADC.setOnClickListener(view -> {
            stopPlay(); //Detiene la reproduccion
            finish(); //Finaliza la actividad y vuelve a la anterior
        });
    }

    //Metodo para reproducir las canciones
    private void playSong() {
        if (songPaths != null && currentPosition >= 0 && currentPosition < songPaths.size()) {
            try {
                //Reiniciamos el MediaPlayer antes de configurar una nueva cancion
                mediaPlayer.reset();

                // Cargar canción desde assets
                AssetFileDescriptor afd = getAssets().openFd(songPaths.get(currentPosition));
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();

                // Configuramos el Listener para preparar el MediaPlayer
                mediaPlayer.setOnPreparedListener(mp -> {
                    mediaPlayer.start(); // Iniciar la reproducción

                // Configuramos el SeekBar
                    seekBar_ADC.setMax(mediaPlayer.getDuration()); // Establecemos la duración total
                });

                // Configuramos un Listener para actualizar el SeekBar mientras se reproduce
                mediaPlayer.setOnSeekCompleteListener(seekMp -> {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar_ADC.setProgress(currentPosition);
                });

                // Listener para actualizar el SeekBar al finalizar la canción
                mediaPlayer.setOnCompletionListener(mp -> {
                    nextSong(); // Pasamos automáticamente a la siguiente canción
                });

                // Preparamos el MediaPlayer de forma asíncrona
                mediaPlayer.prepareAsync();

                //Actualizamos los metadatos
                updateMetadata();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al reproducir la canción.", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "No se puede reproducir la canción seleccionada.", Toast.LENGTH_SHORT).show();
        }
    }


    // Metodo para actualizar los metadatos
    private void updateMetadata() {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            AssetFileDescriptor afd = getAssets().openFd(songPaths.get(currentPosition));
            retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            // Obtener metadatos
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            byte[] artwork = retriever.getEmbeddedPicture();

            // Valores por defecto si faltan datos
            textArtits_ADC.setText(artist != null ? artist : "Desconocido");
            textViewSongTitle_ADC.setText(title != null ? title : "Título desconocido");

            if (artwork != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(artwork, 0, artwork.length);
                imageView_ADC.setImageBitmap(bitmap);
            } else {
                imageView_ADC.setImageResource(R.drawable.ts); // Imagen por defecto
            }
            retriever.release();
        } catch (IOException e) {
            e.printStackTrace();
            imageView_ADC.setImageResource(R.drawable.ts);
            textArtits_ADC.setText("Desconocido");
            textViewSongTitle_ADC.setText("Título desconocido");
        }
    }

    //Metodos para el funcionamiento de los botones

    //Play-Pause
    private void playPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause(); // Pausar la canción
        } else {
            mediaPlayer.start(); // Reanudar la canción

        }
    }

    //Siguiente cancion
    private void nextSong() {
        // Mover la posición actual hacia adelante (con ciclo)
        currentPosition = (currentPosition + 1) % songPaths.size();
        playSong(); // Reproducir la nueva canción
    }

    // Reproducir la canción anterior
    private void playPreviousSong() {
        // Mover la posición actual hacia atrás (con ciclo)
        currentPosition = (currentPosition - 1 + songPaths.size()) % songPaths.size();
        playSong(); // Reproducir la nueva canción
    }

    // Retroceder 10 segundos en la canción
    private void rewind10sec() {
        int newPosition = mediaPlayer.getCurrentPosition() - 10000; // Restar 10 segundos
        mediaPlayer.seekTo(Math.max(newPosition, 0)); // Asegurar que no sea menor a 0
    }

    // Avanzar 10 segundos en la canción
    private void forward10Sec() {
        int newPosition = mediaPlayer.getCurrentPosition() + 10000; // Sumar 10 segundos
        mediaPlayer.seekTo(Math.min(newPosition, mediaPlayer.getDuration())); // No exceder la duración total
    }

    // Detener la reproducción de la canción
    private void stopPlay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop(); // Detener el MediaPlayer
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
