package com.example.mod_tan_p2p.base

import android.app.Activity
import com.example.mod_tan_p2p.core.BindLife
import com.example.mod_tan_p2p.core.Stateable
import com.example.mod_tan_p2p.databinding.FileTreeLayoutBinding
import com.example.mod_tan_p2p.file.FileLeaf
import com.example.mod_tan_p2p.file.FileTree
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import java.io.File

class FileTreeUI(
    private val binding: FileTreeLayoutBinding,
    private val rootTreeUpdater: () -> Single<FileTree>,
    private val subTreeUpdater: (parentTree: FileTree, dir: FileLeaf.DirectoryFileLeaf) -> Single<FileTree>
) : Stateable<FileTreeUI.Companion.FileTreeState>, BindLife,
    CoroutineScope by CoroutineScope(Dispatchers.Main) {


    override val lifeCompositeDisposable: CompositeDisposable = CompositeDisposable()
    override val stateStore: Subject<FileTreeState> = BehaviorSubject.createDefault(FileTreeState())

    fun start() {
        val context = binding.root.context as Activity
        launch(Dispatchers.IO) {
            val rootTree = rootTreeUpdater().await()
            updateState { it.copy(fileTree = rootTree) }.await()
        }

        render({ it.fileTree }) {
            binding.pathTv.text = it.path
        }.bindLife()


    }

    companion object {
        object FileSelectChange

        enum class FileSortType {
            SortByDate,
            SortByName
        }

        data class FileTreeState(
            val fileTree: FileTree = FileTree(
                dirLeafs = emptyList(),
                fileLeafs = emptyList(),
                path = File.separator,
                parentTree = null
            ),
            val selectedFiles: List<FileLeaf.CommonFileLeaf> = emptyList(),
            val sortType: FileSortType = FileSortType.SortByName
        ) {

        }

        private fun List<FileLeaf.CommonFileLeaf>.sortFile(sortType: FileSortType): List<FileLeaf.CommonFileLeaf> =
            when (sortType) {
                FileSortType.SortByDate -> {
                    sortedByDescending { it.lastModified }
                }

                FileSortType.SortByName -> {
                    sortedBy { it.name }
                }
            }

        private fun List<FileLeaf.DirectoryFileLeaf>.sortDir(sortType: FileSortType): List<FileLeaf.DirectoryFileLeaf> =
            when (sortType) {
                FileSortType.SortByDate -> {
                    sortedByDescending { it.lastModified }
                }

                FileSortType.SortByName -> {
                    sortedBy { it.name }
                }
            }
    }
}