package com.example.mod_tan_p2p.file

data class FileTree(
    val dirLeafs: List<FileLeaf.DirectoryFileLeaf>,
    val fileLeafs: List<FileLeaf.CommonFileLeaf>,
    val path: String,
    val parentTree: FileTree?
)

fun FileTree.isRootFileTree(): Boolean = parentTree == null