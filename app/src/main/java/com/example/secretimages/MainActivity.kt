package com.example.secretimages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.secretimages.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PhotoGridAdapter
    private var currentPhotoFile: File? = null

    private val secretDir: File
        get() = File(filesDir, "secret_photos").also { it.mkdirs() }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(this, "カメラの権限が必要です", Toast.LENGTH_SHORT).show()
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            currentPhotoFile?.let {
                if (it.exists() && it.length() > 0) {
                    Toast.makeText(this, "写真を保存しました", Toast.LENGTH_SHORT).show()
                    loadPhotos()
                } else {
                    it.delete()
                }
            }
        } else {
            currentPhotoFile?.delete()
        }
        currentPhotoFile = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        adapter = PhotoGridAdapter(this) { file -> showPhotoOptions(file) }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter

        binding.fabCamera.setOnClickListener { checkCameraPermission() }
        loadPhotos()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(secretDir, "IMG_$timestamp.jpg")
        currentPhotoFile = photoFile

        val uri: Uri = FileProvider.getUriForFile(
            this, "${packageName}.fileprovider", photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
        if (intent.resolveActivity(packageManager) != null) {
            takePictureLauncher.launch(intent)
        } else {
            Toast.makeText(this, "カメラアプリが見つかりません", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPhotos() {
        val photos = secretDir.listFiles()
            ?.filter { it.extension.lowercase() in listOf("jpg", "jpeg", "png") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
        adapter.submitList(photos)
        binding.tvEmpty.visibility = if (photos.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showPhotoOptions(file: File) {
        AlertDialog.Builder(this)
            .setTitle("写真の操作")
            .setItems(arrayOf("削除")) { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("削除確認")
                    .setMessage("この写真を削除しますか？")
                    .setPositiveButton("削除") { _, _ ->
                        file.delete()
                        loadPhotos()
                        Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("キャンセル", null)
                    .show()
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_password -> {
                startActivity(Intent(this, ChangePasswordActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}
