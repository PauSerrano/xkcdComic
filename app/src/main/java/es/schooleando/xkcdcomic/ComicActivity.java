package es.schooleando.xkcdcomic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class ComicActivity extends AppCompatActivity implements BgResultReceiver.Receiver {

    //Primero damos ****** PERMISOS ******* A LA APP para acceder a la red en el Manifest

    private static final String LOGTAG = "xkcdCommic_MainActivity";

    private BgResultReceiver mResultReceiver;

    private TextView tvUrl;
    private TextView tvProgreso;
    private ImageView ivImagen;
    private ProgressBar barraProgreso;
    private Button boton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic);

        tvUrl = (TextView) findViewById(R.id.textView);
        tvProgreso = (TextView) findViewById(R.id.textView2);
        ivImagen = (ImageView) findViewById(R.id.imageView);
        barraProgreso = (ProgressBar) findViewById(R.id.progressBar);
        boton = (Button) findViewById(R.id.button);

        //** Creamos un objeto MiResultReceiver **
        mResultReceiver = new BgResultReceiver(new Handler());
        mResultReceiver.setReceiver(this);

        barraProgreso.setMax(100);
        barraProgreso.setProgress(0);

        Log.d(LOGTAG, "Lanzamos el intent del servicio");

        // Esto es gratis: al arrancar debemos cargar el cómic actual
        Intent intent = new Intent(this, DownloadIntentService.class);
        intent.putExtra("url", "http://xkcd.com/info.0.json");
        intent.putExtra("receiver", mResultReceiver);
        startService(intent);




    }


    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        Log.d(LOGTAG, "Estamos en ReceiverResult");

        int progreso = 0;

        switch (resultCode){
            case DownloadIntentService.PROGRESS:
                Log.d(LOGTAG, "Estamos en ReceiverResult_PROGRESO");


                barraProgreso.setProgress(Integer.parseInt(resultData.getString("ServicioDescarga")));
                tvProgreso.setText(resultData.getString("ServicioDescarga"));


                break;
            case DownloadIntentService.FINSISHED:
                Log.d(LOGTAG, "Estamos en ReceiverResult_FINALIZADO");

                ivImagen.setImageBitmap((Bitmap) resultData.get("bitmapImagen"));

                break;
            case DownloadIntentService.ERROR:
                Log.d(LOGTAG, "Estamos en ReceiverResult_LOG");

                Toast.makeText(this, "Error al descargar la imagen", Toast.LENGTH_SHORT).show();
                break;
            case DownloadIntentService.INICIO:

                tvUrl.setText((resultData.getString("urlImg")));
                //tvUrl.setText("comprobando");
                break;


        }


        // TODO: podemos recibir diferentes resultCodes del IntentService
        //      ERROR -> ha habido un problema de la conexión (Toast)
        //      PROGRESS -> nos estamos descargando la imagen (ProgressBar)
        //      OK -> nos hemos descargado la imagen correctamente. (ImageView)
        // Debeis controlar cada caso


    }

    public void descargarOtra(View view) {

        Random random = new Random();

        int numComic = random.nextInt(1000);

        String urlImagenNueva = "http://xkcd.com/"+numComic+"/info.0.json";

        // Esto es gratis: al arrancar debemos cargar el cómic actual
        Intent intent = new Intent(this, DownloadIntentService.class);
        intent.putExtra("url", urlImagenNueva);
        intent.putExtra("receiver", mResultReceiver);
        startService(intent);

    }

    // TODO: Falta un callback de ImageView para hacer click en la imagen y que se descargue otro comic aleatorio.


}
