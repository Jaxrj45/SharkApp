package com.example.tibuapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tibuapp.data.local.dao.ProductoDao
import com.example.tibuapp.data.local.dao.UsuarioDao
import com.example.tibuapp.data.local.dao.VentaDao
import com.example.tibuapp.data.local.entity.DetalleVentaEntity
import com.example.tibuapp.data.local.entity.ProductoEntity
import com.example.tibuapp.data.local.entity.UsuarioEntity
import com.example.tibuapp.data.local.entity.VentaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(
    entities = [
        ProductoEntity::class,
        UsuarioEntity::class,
        VentaEntity::class,
        DetalleVentaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase(){

    abstract fun productoDao(): ProductoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun ventaDao(): VentaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si ya existe una instancia, la reutiliza
            // Si no, crea una nueva — esto evita crear múltiples bases de datos
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tibu_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Datos iniciales al crear la BD por primera vez
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    poblarDatosIniciales(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun poblarDatosIniciales(db: AppDatabase){
            db.usuarioDao().insertar(
                UsuarioEntity(
                    nombre = "Edwin",
                    pin = "5134",
                    rol = "ADMIN"
                )
            )

            db.usuarioDao().insertar(
                UsuarioEntity(
                    nombre = "ADMIN2",
                    pin = "1201",
                    rol = "ADMIN"
                )
            )

            db.usuarioDao().insertar(
                UsuarioEntity(
                    nombre = "Mari",
                    pin = "1201",
                    rol = "EMPLEADO"
                )
            )

            db.productoDao().insertar(
                ProductoEntity(
                    nombre = "Cuaderno universitario",
                    precio = 15.00,
                    stock = 50,
                    categoria = "Cuadernos"
                )
            )
            db.productoDao().insertar(
                ProductoEntity(
                    nombre = "Lápiz HB",
                    precio = 2.50,
                    stock = 200,
                    categoria = "Escritura"
                )
            )
        }
    }

}