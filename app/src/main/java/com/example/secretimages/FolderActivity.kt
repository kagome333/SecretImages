package com.example.secretimages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.secretimages.databinding.ActivityFolderBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FolderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFolderBinding
    private lateinit var adapter: GalleryAdapter
    private lateinit var folderDir: File
    private var currentPhotoFile: File? = null

    private val cameraPermLauncher = registerForActivityResult(
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
                } else it.delete()
            }
        } else currentPhotoFile?.delete()
        currentPhotoFile = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val folderPath = intent.getStringExtra("folder_path") ?: run { finish(); return }
        val folderName = intent.getStringExtra("folder_name") ?: "フォルダ"
        folderDir = File(folderPath)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = folderName
            setDisplayHomeAsUpEnabled(true)
        }

        adapter = GalleryAdapter(this, onFolderClick = {}, onPhotoClick = { showPhotoOptions(it) })
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter

        binding.fabCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                launchCamera()
            else cameraPermLauncher.launch(Manifest.permission.CAMERA)
        }
        loadPhotos()
    }

    override fun onResume() {
        super.onResume()
        loadPhotos()
    }

    private fun launchCamera() {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(folderDir, "IMG_$ts.jpg")
        currentPhotoFile = file
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
        if (intent.resolveActivity(packageManager) != null) takePictureLauncher.launch(intent)
        else Toast.makeText(this, "カメラアプリが見つかりません", Toast.LENGTH_SHORT).show()
    }

    private fun loadPhotos() {
        val photos = folderDir.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in listOf("jpg","jpeg","png") }
            ?.sortedByDescending { it.lastModified() }
            ?.map { GalleryItem.Photo(it) } ?: emptyList()
        adapter.submitList(photos)
        binding.tvEmpty.visibility = if (photos.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showPhotoOptions(file: File) {
        AlertDialog.Builder(this)
            .setTitle("写真の操作")
            .setItems(arrayOf("削除")) { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("削除確認").setMessage("この写真を削除しますか？")
                    .setPositiveButton("削除") { _, _ ->
                        file.delete(); loadPhotos()
                        Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("キャンセル", null).show()
            }
            .setNegativeButton("キャンセル", null).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 99, 0, "フォルダを削除").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            99 -> {
                AlertDialog.Builder(this)
                    .setTitle("フォルダを削除")
                    .setMessage("フォルダと中の写真を全で�bb�fi8�e��o��fx�b��'ȊB���]��]]�P�]ۊ�bb�fi�H���O���\�\��[]T�X�\��]�[J
N��[�\�

HB���]�Y�]]�P�]ۊ��x����������ȋ�[
K����
B��YB�B�[�HO��\\��ۓ�[ۜ�][T�[X�Y
][JB�B�B�B
