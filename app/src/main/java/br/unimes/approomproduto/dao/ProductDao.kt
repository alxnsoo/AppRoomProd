package  br.unimes.approomproduto.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.unimes.approomproduto.model.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM product")
    suspend fun getAll(): List<Product>

    @Insert
    suspend fun insertAll(vararg products: Product)

    // Item 5
    @Query("SELECT * FROM product WHERE name LIKE :nome")
    suspend fun buscarPorNome(nome: String): List<Product>

    // Item 6
    @Query("DELETE FROM product")
    suspend fun deleteAll()

    @Query("DELETE FROM product WHERE id = :id")
    suspend fun deleteById(id: Int): Int   // retorna quantas linhas apagou

    // Item 7
    @Query("SELECT * FROM product WHERE id = :id")
    suspend fun getById(id: Int): Product?

    @Update
    suspend fun update(product: Product)
}