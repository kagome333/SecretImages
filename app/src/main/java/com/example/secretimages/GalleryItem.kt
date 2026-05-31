package com.example.secretimages

import java.io.File

sealed class GalleryItem {
    data class Folder(val dir: File) : GalleryItem()
    data class Photo(val file: File) : GalleryItem()
}
