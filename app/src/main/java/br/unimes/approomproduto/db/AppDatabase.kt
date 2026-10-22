package br.unimes.approomproduto.db

import androidx.room.Database
import androidx.room.RoomDatabase
import br.unimes.approomproduto.dao.ProductDao
import br.unimes.approomproduto.model.Product

@Database(entities = [Product::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}
