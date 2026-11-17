package com.a2511500020.mahasiswa

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.a2511500020.mahasiswa.api.ApiConfig
import com.a2511500020.mahasiswa.ui.theme.InstalasiLabTheme
import com.a2511500020.mahasiswa.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        setContent {
            InstalasiLabTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cek koneksi jika btnTest diklik
        binding.btnTestConnection.setOnClickListener {
            cekKonesiApi()
        }
    }

    private fun cekKonesiApi() {
        // 1. Tampilkan indikator loading & status awal
        binding.progressBar.visibility = View.VISIBLE
        binding.btnTestConnection.isEnabled = false
        updateStatus("Status: Menghubungkan ke server...", Color.DKGRAY)

        // 2. Jalankan request jaringan secara asynchronous menggunakan Coroutine
        lifecycleScope.launch {
            try {
                // eksekusi panggilan Retrofit di thread ID
                val response = withContext(Dispatchers.IO) {
                    ApiConfig.getApiService().getDaftarMahasiswa()
                }

                // 3. Penanganan HTTP response
                if (response.isSuccessful) {
                    val body = response.body()
                    val jumlahData = body?.data?.size ?: 0
                    updateStatus(
                        "Status: Terhubung (HTTP ${response.code()})\n" +
                                "Pesan: ${body?.message ?: "OK"} ($jumlahData data ditemukan)",
                        Color.parseColor("#2E7D32") // Hijau
                    )
                } else {
                    // server merespons, namun mengembalikan kode error (404, 500, dll.)
                    updateStatus(
                        "Status: Gagal merespons (HTTP ${response.code()} - ${response.message()})",
                        Color.parseColor("#C62828") // Merah
                    )
                }
            } catch (e: SocketTimeoutException) {
                // 4. Penanganan exception: waktu koneksi habis
                updateStatus(
                    "Status: Timeout - Server terlalu lama merespons. Periksa IP & port server.",
                    Color.parseColor("#C62828")
                )
            } catch (e: IOException) {
                // penanganan exception: Tidak ada internet / server mati  / salah IP
                updateStatus(
                    "Status: Gagal terhubung ke jaringan (${e.localizedMessage})",
                    Color.parseColor("#C62828")
                )
            } catch (e: HttpException) {
                // penanganan exception: kesalahan protokol HTTP
                updateStatus(
                    "Status: kesalahan HTTP (${e.code()})",
                    Color.parseColor("#C62828")
                )
            } catch(e: Exception) {
                //penanganan Exception umum lainnya
                updateStatus(
                    "Status: Terjadi kesalahan: ${e.localizedMessage}",
                    Color.parseColor("#C62828")
                )
            } finally {
                //sembunyikan loading dan aktifkan kembali tombol
                binding.progressBar.visibility = View.GONE
                binding.btnTestConnection.isEnabled = true
            }
        }
    }

    // 5. Fungsi pembaruan status koneksi pada UI
    private fun updateStatus(pesan: String, warna: Int) {
        binding.tvConnectionStatus.text = pesan
        binding.tvConnectionStatus.setTextColor(warna)
    }

}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InstalasiLabTheme {
        Greeting("Android")
    }
}