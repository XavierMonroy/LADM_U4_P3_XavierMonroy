package mx.edu.ittepic.ladm_u4_p3_xaviermonroy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Build
import android.telephony.SmsMessage
import android.widget.Toast

class SmsReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras

        if(extras != null){
            var sms = extras.get("pdus") as Array<Any>
            for(indice in sms.indices){
                val formato = extras.getString("format")

                var smsMensaje  = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    SmsMessage.createFromPdu(sms[indice] as ByteArray, formato)
                }else{
                    SmsMessage.createFromPdu(sms[indice] as ByteArray)
                }

                var celularOrigen = smsMensaje.originatingAddress
                var contenidoSMS = smsMensaje.messageBody.toString()

                // Guardar sobre una tabla SQLite
                try{
                    var baseDatos = BaseDatos(context, "turismo", null, 1)
                    var insertar = baseDatos.writableDatabase
                    var SQL = "INSERT INTO ENTRANTES VALUES('${celularOrigen}','${contenidoSMS}', '0')"
                    insertar.execSQL(SQL)
                    baseDatos.close()
                }catch (err: SQLiteException){
                    Toast.makeText(context, err.message, Toast.LENGTH_LONG)
                        .show()
                }
                Toast.makeText(context, "ENTRÓ CONTENIDO: ${contenidoSMS}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}