package br.unimes.approomproduto.view
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import br.unimes.approomproduto.dao.ProductDao
import br.unimes.approomproduto.databinding.ActivityMainBinding
import br.unimes.approomproduto.db.AppDatabase
import br.unimes.approomproduto.model.Product
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database-name")
            .fallbackToDestructiveMigration()
            .build()
        productDao = db.productDao()

        // Itens 1 e 2: validação + limpar campos
        binding.btnInserirProduto.setOnClickListener {
            val name = binding.editProductName.text.toString().trim()
            val price = binding.editProductPrice.text.toString().toDoubleOrNull()
            val quantity = binding.editProductQuantity.text.toString().toIntOrNull() ?: 0
            val description = binding.editProductDescription.text.toString()

            if (name.isEmpty()) {
                binding.editProductName.error = "Informe o nome"
                return@setOnClickListener
            }
            if (price == null || price <= 0) {
                binding.editProductPrice.error = "Informe um preço válido"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                productDao.insertAll(Product(name = name, price = price,
                    description = description, quantity = quantity))
                Toast.makeText(this@MainActivity, "Produto inserido!", Toast.LENGTH_SHORT).show()
                limparCampos()
            }
        }

        // Itens 3 e 4: lista na tela + total
        binding.btnListarProdutos.setOnClickListener {
            lifecycleScope.launch { mostrarLista(productDao.getAll()) }
        }

        // Item 5: buscar por nome
        binding.btnBuscar.setOnClickListener {
            val texto = binding.editBusca.text.toString().trim()
            lifecycleScope.launch { mostrarLista(productDao.buscarPorNome("%$texto%")) }
        }

        // Item 6: excluir todos e por id
        binding.btnExcluirTodos.setOnClickListener {
            lifecycleScope.launch {
                productDao.deleteAll()
                mostrarLista(productDao.getAll())
            }
        }
        binding.btnExcluirPorId.setOnClickListener {
            val id = binding.editProductId.text.toString().toIntOrNull()
            if (id == null) {
                binding.editProductId.error = "Informe o ID"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val apagados = productDao.deleteById(id)
                val msg = if (apagados > 0) "Produto excluído!" else "ID não encontrado"
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                mostrarLista(productDao.getAll())
            }
        }

        // Item 7: atualizar preço
        binding.btnAtualizarPreco.setOnClickListener {
            val id = binding.editProductId.text.toString().toIntOrNull()
            val novoPreco = binding.editNovoPreco.text.toString().toDoubleOrNull()
            if (id == null) {
                binding.editProductId.error = "Informe o ID"
                return@setOnClickListener
            }
            if (novoPreco == null || novoPreco <= 0) {
                binding.editNovoPreco.error = "Informe um preço válido"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val produto = productDao.getById(id)
                if (produto == null) {
                    Toast.makeText(this@MainActivity, "ID não encontrado", Toast.LENGTH_SHORT).show()
                } else {
                    productDao.update(produto.copy(price = novoPreco))
                    Toast.makeText(this@MainActivity, "Preço atualizado!", Toast.LENGTH_SHORT).show()
                    mostrarLista(productDao.getAll())
                }
            }
        }
    }

    private fun limparCampos() {
        binding.editProductName.text.clear()
        binding.editProductPrice.text.clear()
        binding.editProductQuantity.text.clear()
        binding.editProductDescription.text.clear()
        binding.editProductName.requestFocus()
    }

    private fun mostrarLista(products: List<Product>) {
        if (products.isEmpty()) {
            binding.txtResultado.text = "Nenhum produto encontrado."
            return
        }
        val linhas = products.joinToString("\n") {
            "ID ${it.id} | ${it.name} | R$ %.2f | Qtd: ${it.quantity} | ${it.description}".format(it.price)
        }
        val total = products.sumOf { it.price }
        binding.txtResultado.text =
            "$linhas\n\nQuantidade de produtos: ${products.size}\nSoma dos preços: R$ %.2f".format(total)
    }
}