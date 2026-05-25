package com.example.secretimages

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secretimages.databinding.ItemPhotoBinding
import java.io.File

class PhotoGridAdapter(
    private val context: Context,
    private val onLongClick: (File) -> Unit
) : ListAdapter<File, PhotoGridAdapter.PhotoViewHolder>(FileDiffCallback()) {

    inner class PhotoViewHolder(val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val file = getItem(position)
        Glide.with(context)
            .load(file)
            .centerCrop()
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.binding.imageView)

        holder.itemView.setOnLongClickListener {
            onLongClick(file)
            true
        }
    }

    class FileDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File) =
            oldItem.absolutePath == newItem.absolutePath
        override fun areContentsTheSame(oldItem: File, newItem: File) =
            oldItem.lastModified() == newItem.lastModified()
    }
}
