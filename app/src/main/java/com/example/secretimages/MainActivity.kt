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
import android.widget.EditText
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
    private lateinit var adapter: GalleryAdapter
    private var currentPhotoFile: File? = null

    private val secretDir: File
        get() = File(filesDir, "secret_photos").also { it.mkdirs() }

    private val cameraPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera(secretDir)
        else Toast.makeText(this, "カメラの権限が必要です", Toast.LENGTH_SHORT).show()
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            currentPhotoFile?.let {
                if (it.exists() && it.length() > 0) {
                    Toast.makeText(this, "写真を保存しました", Toast.LENGTH_SHORT).show()
                    loadGallery()
                } else it.delete()
            }
        } else currentPhotoFile?.delete()
        currentPhotoFile = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        title = "シークレット画像"

        adapter = GalleryAdapter(this,
            onFolderClick = { folder ->
                startActivity(Intent(this, FolderActivity::class.java).apply {
                    putExtra("folder_path", folder.absolutePath)
                    putExtra("folder_name", folder.name)
                })
            },
            onPhotoClick = { file -> showPhotoOptions(file) }
        )

        val lm = GridLayoutManager(this, 3)
        lm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) =
                if (adapter.isFolder(position)) 3 else 1
        }
        binding.recyclerView.layoutManager = lm
        binding.recyclerView.adapter = adapter

        binding.fabCamera.setOnClickListener { showFabMenu() }
        loadGallery()
    }

    override fun onResume() {
        super.onResume()
        loadGallery()
    }

    private fun showFabMenu() {
        AlertDialog.Builder(this)
            .setItems(arrayOf("写真を撮影", "フォルダを作成")) { _, i ->
                when (i) {
                    0 -> checkCameraAndShoot(secretDir)
                    1 -> showCreateFolderDialog()
                }
            }.show()
    }

    private fun showCreateFolderDialog() {
        val et = EditText(this).apply {
            hint = "フォルダ名"
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(this)
            .setTitle("フォルダを作成")
            .setView(et)
            .setPositiveButton("作成") { _, _ ->
                val name = et.text.toString().trim()
                if (name.isNotEmpty()) {
                    val f = File(secretDir, name)
                    if (!f.exists()) { f.mkdirs(); loadGallery() }
                    else Toast.makeText(this, "同じ名前のフォルダがあります", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun checkCameraAndShoot(saveDir: File) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            launchCamera(saveDir)
        else cameraPermLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun launchCamera(saveDir: File) {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(saveDir, "IMG_$ts.jpg")
        currentPhotoFile = file
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
        if (intent.resolveActivity(packageManager) != null) takePictureLauncher.launch(intent)
        else Toast.makeText(this, "カメラアプリが見つかりません", Toast.LENGTH_SHORT).show()
    }

    private fun loadGallery() {
        val items = mutableListOf<GalleryItem>()
        secretDir.listFiles()?.filter { it.isDirectory }?.sortedBy { it.name }
            ?.forEach { items.add(GalleryItem.Folder(it)) }
        secretDir.listFiles()?.filter { it.isFile && it.extension.lowercase() in listOf("jpg","jpeg","png") }
            ?.sortedByDescending { it.lastModified() }
            ?.forEach { items.add(GalleryItem.Photo(it)) }
        adapter.submitList(items)
        binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showPhotoOptions(file: File) {
        AlertDialog.Builder(this)
            .setTitle("写真の操作")
            .setItems(arrayOf("削除")) { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("削除確認").setMessage("この写真を削除しますお？")
                    .setPositiveButton("削除") { _, _ ->
                        file.delete(); loadGallery()
                        Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("キャンセル", null).show()
            }
            .setNegativeButton("キャンセル", null).show()
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

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() { finishAffinity() }
}
