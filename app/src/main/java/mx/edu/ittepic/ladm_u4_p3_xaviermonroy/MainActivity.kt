package mx.edu.ittepic.ladm_u4_p3_xaviermonroy

import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    // Variables
    val siPermisos = 1
    var nombreBD = "turismo"
    var listaDatos = ArrayList<String>()
    var listaID = ArrayList<String>()
    var hilo: Hilo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Pedir permisos.
        var permisoReadSMS =
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS)
        var permisoSendSMS =
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
        var permisoReceiveSMS =
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS)

        if (permisoReadSMS != PackageManager.PERMISSION_GRANTED || permisoSendSMS != PackageManager.PERMISSION_GRANTED ||
                permisoReceiveSMS != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_SMS, android.Manifest.permission.SEND_SMS,
                            android.Manifest.permission.RECEIVE_SMS), siPermisos)
        }

        // Mostrar la lista de lugares turísticos.
        mostrarLista()

        // Agregar lugares turísticos.
        btnAgregar.setOnClickListener {
            // Asegurarse de que los campos tengan datos.
            if (txtEstado.text.toString().isEmpty() || txtLugar.text.toString().isEmpty()) {
                mensaje("Debe de llenar todos los campos")
                return@setOnClickListener
            }

            agregarLugar()
            txtLugar.requestFocus()
        }

        // Borrar las cajas de texto.
        btnBorar.setOnClickListener {
            txtEstado.setText("")
            txtLugar.setText("")
            txtEstado.requestFocus()
        }

        // Mostrar el último mensaje recibido.
        lblUltimo.setOnClickListener {
            verUltimo()
        }

        // Iniciar el hilo.
        hilo = Hilo(this)
        hilo!!.start()

    }

    // Si permisos otorgados.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == siPermisos) { verUltimo() }
    }

    // Mostrar lista
    private fun mostrarLista() {
        // Limpiar los arreglos.
        listaDatos.clear()
        listaID.clear()
        try {
            val cursor = BaseDatos(this, nombreBD, null, 1).readableDatabase
                    .rawQuery("SELECT * FROM LUGAR", null)
            var resultado = ""

            if (cursor.moveToFirst()) {
                do {
                    resultado = "\nESTADO: " + cursor.getString(1) + "\n" +
                            "LUGAR: " + cursor.getString(2)
                    cursor.getString(1)
                    listaDatos.add(resultado)
                    listaID.add(cursor.getString(0))
                } while (cursor.moveToNext())
            } else {
                // Si no encuentra datos.
                listaDatos.add("NO HAY REGISTROS")
            }
            // Llenar la lista.
            var adaptador = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaDatos)

            listaLugares.adapter = adaptador

            listaLugares.setOnItemClickListener { parent, view, position, id ->
                AlertDialog.Builder(this)
                        .setTitle("ATENCIÓN")
                        .setMessage("¿Quiere eliminar el lugar turístico?\n" + listaDatos[position])
                        .setPositiveButton("Eliminar") { d, i ->
                            eliminar(listaID[position])
                        }
                        .setNegativeButton("Cancelar") { d, i -> }
                        .show()
            }
        } catch (err: SQLiteException) {
            Toast.makeText(this, err.message, Toast.LENGTH_LONG).show()
        }
    }

    // Función para eliminar.
    private fun eliminar(id: String) {
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var eliminar = baseDatos.writableDatabase
            var SQL = "DELETE FROM LUGAR WHERE ID = ?"
            var parametros = arrayOf(id)
            eliminar.execSQL(SQL, parametros)
            baseDatos.close()

            mensaje("Se eliminó el lugar turístico")
        } catch (e: SQLiteException) {
            mensaje(e.message!!)
        }
        // Mostrar la lista actualizada.
        mostrarLista()
    }

    // Agrergar los lugares turísticos.
    private fun agregarLugar() {
        var estado = txtEstado.text.toString().toUpperCase()
        var lugar = txtLugar.text.toString().toUpperCase()

        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var insertar = baseDatos.writableDatabase
            var SQL = "INSERT INTO LUGAR VALUES(NULL,'${estado}','${lugar}')"
            insertar.execSQL(SQL)
            baseDatos.close()
        } catch (e: SQLiteException) { mensaje(e.message!!) }

        // Mostrar la lista.
        mostrarLista()
        mensaje("Se agregó correctamente el lugar turístico")
    }

    // Ver último mensaje recibido.
    fun verUltimo() {
        try {
            val cursor = BaseDatos(this, nombreBD, null, 1).readableDatabase
                    .rawQuery("SELECT * FROM ENTRANTES", null)

            var ultimo = ""
            if (cursor.moveToFirst()) {
                var respuesta = ""

                if (cursor.getString(2).equals("1")) {
                    respuesta = "Ha sido respondido"
                } else { respuesta = "No ha sido respodido" }

                do {
                    ultimo = "ÚLTIMO MENSAJE RECIBIDO:\nCelular de origen: " +
                            cursor.getString(0) + "\nContenido: " +
                            cursor.getString(1) + "\n" + respuesta
                } while (cursor.moveToNext())
            } else {
                ultimo = "No hay mensajes, la tabla está vacía."
            }
            lblUltimo.setText(ultimo)
        } catch (err: SQLiteException) {
            Toast.makeText(this, err.message, Toast.LENGTH_LONG).show()
        }
    }

    // Mostrar mensajes mediante toast
    private fun mensaje(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    // Enviar mensajes
    fun enviarSMS() {
        var estado = ""
        var cadenaMensaje = ""

        try {
            val cursor = BaseDatos(this, nombreBD,null,1).readableDatabase
                    .rawQuery("SELECT * FROM ENTRANTES",null)

            var ultimoNum = ""
            var ultimoMsj = ""
            var respuesta = ""

            if(cursor.moveToFirst()) {
                do {
                    ultimoNum = cursor.getString(0)
                    ultimoMsj = cursor.getString(1)
                    respuesta = cursor.getString(2)

                    if (validaSintaxis(ultimoMsj)) {
                        var contenido = ultimoMsj.split("-")
                        estado = contenido[1].toUpperCase()


                        cadenaMensaje = consultaLugar(estado)

                        if (respuesta.equals("0")) {
                            SmsManager.getDefault().sendTextMessage(ultimoNum, null, cadenaMensaje, null, null)
                            cambiaRespuesta(ultimoNum)
                        }
                    } else{
                        if (respuesta.equals("0")) {
                            cadenaMensaje = "¡Error! La sintaxis es: VISITA-ESTADO."
                            SmsManager.getDefault().sendTextMessage(ultimoNum,null,cadenaMensaje,null,null)
                            cambiaRespuesta(ultimoNum)
                        }
                    }
                }while(cursor.moveToNext())
            }
            cursor.close()
        }catch (error: SQLiteException){ Toast.makeText(this,error.message, Toast.LENGTH_LONG).show() }
    }

    // Valida la sintaxis.
    fun validaSintaxis(contenido: String): Boolean {
        try {
            var divideContenido = contenido.split("-".toRegex(), 2)
            var visita = divideContenido[0].toUpperCase()

            if (visita.equals("VISITA") && divideContenido.size == 2) { return true }

        } catch (e: IndexOutOfBoundsException) { return false }

        return false
    }

    // Consulta lugar turístico.
    fun consultaLugar(estado: String): String {
        var resultado = ""
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM LUGAR WHERE ESTADO = ?"
            var parametros = arrayOf(estado)
            var cursor = select.rawQuery(SQL, parametros)

            if (cursor.moveToFirst()) {
                do {
                    // SI HAY RESULTADO.
                    resultado += "Lugar: " + cursor.getString(2) + " \n  "
                } while (cursor.moveToNext())
            } else { resultado = "No existen lugares turísticos registrados para ese estado." }

            select.close()
            baseDatos.close()
        } catch (error: SQLiteException) { }

        return resultado
    }

    // Cambiar el estado de la respuesta
    fun cambiaRespuesta (numero:String){
        // 0: No respondido
        // 1: Respondido
        try{
            var baseDatos = BaseDatos(this,nombreBD,null,1)
            var insertar = baseDatos.writableDatabase
            var SQL = "UPDATE ENTRANTES SET RESPUESTA ='1' WHERE NUMERO = ?"
            var parametros = arrayOf(numero)
            insertar.execSQL(SQL,parametros)
            insertar.close()
            baseDatos.close()
        }catch (error: SQLiteException){ Toast.makeText(this,error.message, Toast.LENGTH_LONG).show() }
    }

    // Clase hilo que se ejecutará cada segundo para responder los mensajes
    class Hilo (p:MainActivity) : Thread() {
        private var iniciar = false
        private var puntero = p

        override fun run() {
            super.run()
            iniciar = true
            while (iniciar) {
                sleep(1000)
                puntero.runOnUiThread {
                    puntero.enviarSMS()
                }
            }
        }
    }
}
