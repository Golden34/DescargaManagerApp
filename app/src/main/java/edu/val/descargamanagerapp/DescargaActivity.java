package edu.val.descargamanagerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;

public class DescargaActivity extends AppCompatActivity {
    private static final String URL_CANCION_COCIDITO = "https://audio-ssl.itunes.apple.com/apple-assets-us-std-000001/Music/a7/79/0b/mzm.qjnerkzx.aac.p.m4a";


    private ProgressDialog mProgressDialog;
    private Uri uri_descarga;
    private File f_destino;
    private String ruta_fichero_descarga;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descarga);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //SUPONEMOS QUE HA SIDO CONCEDIDO EL PERMISO DE ESCRITURA
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }




    /**
     * Mostramos un cuadro de dialogo de forma opcional mientas se descargar
     * un archivo
     */
    private void dibujarProgressBar () {
        /**
         * DIBUJAR EL PROCESS DIALOG
         */
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Descargando muestra . . .");
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle("Accediendo al itunes");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

    }

    private DownloadManager.Request prepararPeticionDescarga (String url)
    {
        DownloadManager.Request request = null;

        //indico qué recurso quiero descargar y el tipo
        request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Musica ");
        request.setTitle("Itunes muestra mp3 ");
        request.setMimeType("audio/mp3");
        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);//necesitamos android.permission.DOWNLOAD_WITHOUT_NOTIFICATION
        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);//se ve mientras, pero se quita al completarse
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);


       //LA RUTA QUE LE PASAMOS A DOWNLOADMANAGER DEBE SER TIPO FILE:// Y CON PATH A LA MEMORIA EXTERNA

       // String ruta_completa_fichero =  Environment.getExternalStorageDirectory().getPath()+"/cancion1.mp3";
        ruta_fichero_descarga =  getExternalFilesDir(null).getPath()+"/cancion1.mp3";//pasando null obtenemos la ruta raíz externa privada
        f_destino = new File(ruta_fichero_descarga);
        uri_descarga = Uri.fromFile(f_destino);

        request.setDestinationUri(uri_descarga);

        return request;


    }


    private void descargarFichero (String url) {

        long id_descarga = 0;
        IntentFilter filter = null;
        DescargaCompletaReceiver receiver = null;

        //opcional, para dibujar una ventana
        dibujarProgressBar();
        //importante, programo la vuelta "pongo a escuchar el fin de la descarga a mi Receiver"
        filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        receiver = new DescargaCompletaReceiver(this);
        this.registerReceiver(receiver, filter);

        //preparo la petición de descarga para que el servicio DownloadManager tenga claro qué quiero descargar y cómo
        DownloadManager.Request request = prepararPeticionDescarga(url);

        //una vez configurada la petición de descarga, se la pido al servicio de descargas
        DownloadManager manager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
        id_descarga = manager.enqueue(request);//hago la petición y me da un id, que la identifica
        receiver.setId_descarga(id_descarga);//importante para saber que es esa y no otra más tarde, cuando acabe


        Log.d(this.getClass().getCanonicalName(), "Descarga iniciada con con id = " + id_descarga);

    }



    public void descargar (View v) {
        descargarFichero(URL_CANCION_COCIDITO);
    }

    public void reproducirInvisible (View v) {

        try {

            MediaPlayer mediaPlayer = MediaPlayer.create(this, uri_descarga);
            mediaPlayer.setLooping(false);
            mediaPlayer.setVolume(100, 100);
            mediaPlayer.start();

        }
        catch (Exception e)
        {

            Log.e("Error" , "ERROR al reproducir", e);
        }
    }


    public void reproducirVisible (View v) {

        try {

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            //AL MANDAR CON UN INTENT A REPRODUCIR, NECESITO TRADUCIR LA RUTA interna (file) a EXTERNA content, PUES si no, falla

            Uri uri_content = FileProvider.getUriForFile(this, "edu.val.descargamanagerapp.fileprovider", f_destino);

            intent.setDataAndType(uri_content, "audio/mp3");
            startActivity(intent);


        }
        catch (Exception e)
        {

            Log.e("MIAPP" , "ERROR al reproducir", e);
        }
    }

    public void actualizarVentanaTrasDescarga (boolean fue_bien) {

        mProgressDialog.dismiss();
        if (fue_bien) {
            ImageView iv = findViewById(R.id.img_reproducir);
            iv.setClickable(true);
            iv.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));

        } else {
            Toast.makeText(this, "Ha habido un problema en la descarga. Podcast no disponible!", Toast.LENGTH_LONG).show();

        }
    }
}
