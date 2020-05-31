package mx.edu.ittepic.ladm_u4_p3_xaviermonroy

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(context: Context?, name : String, factory: SQLiteDatabase.CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version){
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ENTRANTES(NUMERO VARCHAR(200), MENSAJE VARCHAR(2000), RESPUESTA CHAR(1))")
        db.execSQL("CREATE TABLE LUGAR(ID INTEGER PRIMARY KEY AUTOINCREMENT, ESTADO VARCHAR(200), LUGAR VARCHAR(200))")
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
}