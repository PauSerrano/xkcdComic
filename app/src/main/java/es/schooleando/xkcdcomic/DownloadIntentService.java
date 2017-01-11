package es.schooleando.xkcdcomic;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

import static org.apache.http.params.CoreConnectionPNames.CONNECTION_TIMEOUT;

public class DownloadIntentService extends IntentService  {

    // definimos tipos de mensajes que utilizaremos en ResultReceiver
    public static final int PROGRESS = 0;
    public static final int FINSISHED = 1;
    public static final int ERROR = 2;
    public static final int INICIO = 3;



    private static final String TAG = DownloadIntentService.class.getSimpleName();
    private ResultReceiver mReceiver;
    private String urlImagenString;
    private Bitmap imagenBmpDescargada;

    public DownloadIntentService() {
        super("DownloadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        Log.d(TAG, "onHandleIntent");


        if (intent != null) {
            //++ Creamos un receptor de resultados recogido del intent que a dado paso al inicio del servicio ++
            mReceiver = intent.getParcelableExtra("receiver");

            //Obtenemos la url del intent
            String urlStringJson = intent.getStringExtra("url");

            Log.d(TAG, "obtnida la url: " + urlStringJson);


            try {
                // TODO Aquí hacemos la conexión y accedemos a la imagen.
                //Abrimos la conexion
                // TODO: Habrá que hacer 2 conexiones:
                //  1. Para descargar el resultado JSON para leer la URL.
                URL url = new URL(urlStringJson);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //Realizamos la peticion de la descarga del Json
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inJson = new BufferedInputStream(urlConnection.getInputStream());
                //Guardamos el json en un objeto JsonReader para facilitar la lectura en el caso qeu fueran varios
                JsonReader jsonReader = new JsonReader(new InputStreamReader(inJson, "UTF-8"));
                //metodo creado para obtener la url de la imagen que contiene el json obtenido del inputStream
                urlImagenString = leerJson(jsonReader);
                jsonReader.close();
                //Por comprobacion enviamos la url de la imagen obtenida
                Bundle b = new Bundle();
                b.putString("urlImg", urlImagenString);
                mReceiver.send(this.INICIO, b);

                //  2. Una vez tenemos la URL descargar la imagen en la carpeta temporal.
                URL urlImagen = new URL(urlImagenString);
                HttpURLConnection urlConnectionDescarga = (HttpURLConnection) urlImagen.openConnection();

                //Realizamos la peticion de la descarga de la imagen
                urlConnectionDescarga.setRequestMethod("GET");
                urlConnectionDescarga.connect();
                String tipo = urlConnectionDescarga.getContentType();
                int tamañoRecurso = urlConnectionDescarga.getContentLength();

                if (tipo.startsWith("image/")) {
                    InputStream is = urlConnectionDescarga.getInputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    byte[] buffer = new byte[1024];
                    int n = 0;
                    int total = 0;
                    //Mientras el resultado de lectura del buffer sea distinto a -1
                    Log.d(TAG, "Empieza la descarga de la imagen");
                    while ((n = is.read(buffer)) != -1) {
                        //Escribimos los bytes
                        bos.write(buffer, 0, n);

                        //Estado de la descarga en progreso
                        total += n;
                        if (tamañoRecurso != -1) {

                            Integer porc = (total * 100) / tamañoRecurso;
                            Bundle b1 = new Bundle();
                            b1.putString("ServicioDescarga", String.valueOf(porc));
                            mReceiver.send(this.PROGRESS, b1);

                        } else {
                            Integer porc = total;
                            Bundle b1 = new Bundle();
                            b1.putString("ServicioDescarga", String.valueOf(porc));
                            mReceiver.send(this.PROGRESS, b1);

                        }

                    }//while

                    //cerramos los Streams
                    bos.close();
                    is.close();

                    byte[] arrayImagen = bos.toByteArray();
                    imagenBmpDescargada = BitmapFactory.decodeByteArray(arrayImagen, 0, arrayImagen.length);

                    Log.d(TAG, "FINALIZA la descarga de la imagen");
                    Bundle b2 = new Bundle();
                    b2.putString("ServicioDescarga", "Finalizada la descarga");
                    b2.putParcelable("bitmapImagen", imagenBmpDescargada);
                    mReceiver.send(this.FINSISHED, b2);

                }else {

                }
                    // TODO: Devolver la URI de la imagen si todo ha ido bien.

                    // TODO: Controlar los casos en los que no ha ido bien: excepciones en las conexiones, etc...
                } catch(UnsupportedEncodingException e){
                    e.printStackTrace();
                } catch(ProtocolException e){
                    e.printStackTrace();
                } catch(MalformedURLException e){
                    e.printStackTrace();
                } catch(IOException e){
                    e.printStackTrace();
                }

            } else {

                Log.d(TAG, "Servicio NO Correcto! Intent = null");
            }

        }

        public String leerJson (JsonReader reader)throws IOException {

            String url = null;

            reader.beginObject();
            while (reader.hasNext()) {

                String name = reader.nextName();
                switch (name) {
                    case "img":
                        url = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();

            return url;
        }



    }