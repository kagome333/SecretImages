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
                    loadItems()
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

        adapter = GalleryAdapter(
            context = this,
            onFolderClick = { dir -> openFolder(dir) },
            onFolderLongClick = { dir -> showFolderOptions(dir) },
            onPhotoClick = { file -> showPhotoOptions(file) }
        )
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter

        binding.fabCamera.setOnClickListener { checkCameraPermission() }
        loadItems()
    }

    override fun onResume() {
        super.onResume()
        loadItems()
    }

    private fun loadItems() {
        val files = secretDir.listFiles() ?: emptyArray()
        val folders = files.filter { it.isDirectory }
            .sortedBy { it.name }
            .map { GalleryItem.Folder(it) }
        val photos = files.filter { it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png") }
            .sortedByDescending { it.lastModified() }
            .map { GalleryItem.Photo(it) }
        val items = folders + photos
        adapter.submitList(items)
        binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openFolder(dir: File) {
        startActivity(Intent(this, FolderActivity::class.java).apply {
            putExtra("folder_path", dir.absolutePath)
            putExtra("folder_name", dir.name)
        })
    }

    private fun showFolderOptions(dir: File) {
        AlertDialog.Builder(this)
            .setTitle(dir.name)
            .setItems(arrayOf("名前を変更", "削除")) { _, which ->
                when (which) {
                    0 -> showRenameFolderDialog(dir)
                    1 -> confirmDeleteFolder(dir)
                }
            }
            .show()
    }

    private fun showRenameFolderDialog(dir: File) {
        val input = EditText(this).apply {
            setText(dir.name)
            hint = "新しいフォルダ名"
            setPadding(48, 24, 48, 24)
            selectAll()
        }
        AlertDialog.Builder(this)
            .setTitle("フォルダの名前を変更")
            .setView(input)
            .setPositiveButton("変更") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(this, "フォルダ名を入力してください", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val newDir = File(dir.parent, newName)
                if (newDir.exists()) {
                    Toast.makeText(this, "同名のフォルダが既に存在します", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (dir.renameTo(newDir)) {
                    Toast.makeText(this, "名前を変更しました", Toast.LENGTH_SHORT).show()
                    loadItems()
                } else {
                    Toast.makeText(this, "名前の変更に失敗しました", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun confirmDeleteFolder(dir: File) {
        AlertDialog.Builder(this)
            .setTitle("フォルダを削除")
            .setMessage("「${dir.name}」とその中の写真を全て削除しますか？")
            .setPositiveButton("削除") { _, _ ->
                dir.deleteRecursively()
                Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show()
                loadItems()
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(secretDir, "IMG_${timestamp}.jpg")
        currentPhotoFile = photoFile
        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
        if (intent.resolveActivity(packageManager) != null) {
            takePictureLauncher.launch(intent)
        } else {
            Toast.makeText(this, "カメラアプリが見つかりません", Toast.LENGTH_SHORT).show()
        }
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
                        loadItems()
                        Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("キャンセル", null)
                    .show()
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun showCreateFolderDialog() {
        val input = EditText(this).apply {
            hint = "フォルダ名を入力"
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(this)
            .setTitle("フォルダを作成")
            .setView(input)
            .setPositiveButton("作成") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "フォルダ名を入力してください", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val folder = File(secretDir, name)
                if (folder.exists()) {
                    Toast.makeText(this, "同名のフォルダが既に存在します", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                folder.mkdirs()
                Toast.makeText(this, "フォルダを作成しました", Toast.LENGTH_SHORT).show()
                loadItems()
                openFolder(folder)
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
            R.id.action_create_folder -> {
                showCreateFolderDialog()
                true
            }
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
