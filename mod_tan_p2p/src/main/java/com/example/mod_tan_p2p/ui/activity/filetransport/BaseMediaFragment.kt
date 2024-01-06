package com.example.mod_tan_p2p.ui.activity.filetransport

import android.os.Environment
import com.example.mod_tan_p2p.R
import com.example.mod_tan_p2p.base.BaseFragment
import com.example.mod_tan_p2p.databinding.BaseMediaFragmentLayoutBinding
import com.example.mod_tan_p2p.utils.MediaType
import com.tans.rxutils.QueryMediaItem
import java.io.File

class BaseMediaFragment(private val mediaType: MediaType) :
    BaseFragment<BaseMediaFragmentLayoutBinding, BaseMediaFragment.Companion.BaseMediaState>(
        layoutId = R.layout.base_media_fragment_layout,
        default = BaseMediaState()
    ) {
    private val androidRootDir: File by lazy {
        Environment.getExternalStorageDirectory()
    }

    companion object {
        private const val TAG = "BaseMediaFragment"

        data class BaseMediaState(
            val mediaItems: List<QueryMediaItem> = emptyList(),
            val selectedMediaItems: List<QueryMediaItem> = emptyList()
        )

        object MediaItemSelectChange

        enum class MediaType { Image, Video, Audio }
    }
}