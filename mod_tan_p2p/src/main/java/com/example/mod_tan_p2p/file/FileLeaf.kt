package com.example.mod_tan_p2p.file

import androidx.annotation.Keep

sealed class FileLeaf(
    val name: String,
    val path: String,
    val lastModified: Long
) {
    @Keep
    class CommonFileLeaf(name: String, path: String, val size: Long, lastModified: Long) :
        FileLeaf(name, path, lastModified)

    @Keep
    class DirectoryFileLeaf(
        name: String,
        path: String,
        val childrenCount: Long,
        lastModified: Long
    ) : FileLeaf(name, path, lastModified)
}