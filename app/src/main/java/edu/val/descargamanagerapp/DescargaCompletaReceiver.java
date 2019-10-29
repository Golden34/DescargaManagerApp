package edu.val.descargamanagerapp;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

public class DescargaCompletaReceiver extends BroadcastReceiver {private long id_descarga;
    private Context context;




    public DescargaCompletaReceiver(Context context) {
        this.context = context;
    }



    public void setId_descarga(long id_descarga) {
        this.id_descarga = id_descarga;
    }



    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d (getClass().getCanonicalName(), "Entrando en Onrecieve ");

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(this.id_descarga);
        Cursor cursor = downloadManager.query(query);
        cursor.moveToFirst();
        int ref = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(ref);

        DescargaActivity da = (DescargaActivity)context;
        switch (status) {
            case DownloadManager.STATUS_SUCCESSFUL:

                da.actualizarVentanaTrasDescarga(true);


                break;

            case DownloadManager.STATUS_FAILED: //algo ha fallado

                da.actualizarVentanaTrasDescarga(false);

                Log.d(this.getClass().getCanonicalName(), "Ha habido un problema durante la descarga del archivo. Archivo no descargado");

                break;
        }

        /**
         * IMPORTANTÍSIMO DEREGISTRAR ESTA CLASE, DESASOCIAÁNDOLA DE LA ACTIIVIDA. sI NO, EN LA SIGUIENTE DESCARGA COMPLETA
         * ESTA CLASE SERÁ INOCADA 2 VECES, PUDIENDO PRODUCIR INCOSISTENCIAS
         */

        context.unregisterReceiver(this);


    }

    public DescargaCompletaReceiver() {
    }
}
