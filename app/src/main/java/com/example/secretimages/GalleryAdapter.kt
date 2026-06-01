package com.example.secretimages

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.secretimages.databinding.ItemFolderBinding
import com.example.secretimages.databinding.ItemPhotoBinding
import java.io.File

class GalleryAdapter(
    private val context: Context,
    private val onFolderClick: (File) -> Unit,
    private val onFolderLongClick: (File) -> Unit = {},
    private val onPhotoClick: (File) -> Unit
) : ListAdapter<GalleryItem, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        const val TYPE_FOLDER = 0
        const val TYPE_PHOTO = 1
        val DIFF = object : DiffUtil.ItemCallback<GalleryItem>() {
            override fun areItemsTheSame(a: GalleryItem, b: GalleryItem) = when {
                a is GalleryItem.Folder && b is GalleryItem.Folder -> a.dir.path == b.dir.path
                a is GalleryItem.Photo && b is GalleryItem.Photo -> a.file.path == b.file.path
                else -> false
            }
            override fun areContentsTheSame(a: GalleryItem, b: GalleryItem) = areItemsTheSame(a, b)
        }
    }

    override fun getItemViewType(pos: Int) = when (getItem(pos)) {
        is GalleryItem.Folder -> TYPE_FOLDER
        is GalleryItem.Photo -> TYPE_PHOTO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_FOLDER)
            FolderVH(ItemFolderBinding.inflate(inf, parent, false))
        else
            PhotoVH(ItemPhotoBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        when (val item = getItem(pos)) {
            is GalleryItem.Folder -> (holder as FolderVH).bind(item.dir)
            is GalleryItem.Photo -> (holder as PhotoVH).bind(item.file)
        }
    }

    inner class FolderVH(private val b: ItemFolderBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(dir: File) {
            b.tvFolderName.text = dir.name
            val count = dir.listFiles()?.count { it.isFile } ?: 0
            b.tvPhotoCount.text = "${count}枚"
            val first = dir.listFiles()?.filter {
                it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png")
            }?.firstOrNull()
            if (first != null) {
                b.ivFolderThumb.setImageBitmap(BitmapFactory.decodeFile(first.absolutePath))
            } else {
                b.ivFolderThumb.setImageResource(android.R.drawable.ic_menu_gallery)
            }
            b.root.setOnClickListener { onFolderClick(dir) }
            b.root.setOnLongClickListener { onFolderLongClick(dir); true }
        }
    }

    inner class PhotoVH(private val b: ItemPhotoBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(file: File) {
            b.imageView.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
            b.root.setOnClickListener { onPhotoClick(file) }
        }
    }
}
