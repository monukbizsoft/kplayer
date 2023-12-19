/*****************************************************************************
 * AlbumsProvider.kt
 *****************************************************************************
 * Copyright © 2019 KPlayer authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package com.kbizsoft.KPlayer.providers.medialibrary

import android.content.Context
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.kbizsoft.medialibrary.interfaces.Medialibrary
import com.kbizsoft.medialibrary.interfaces.media.Album
import com.kbizsoft.medialibrary.interfaces.media.Artist
import com.kbizsoft.medialibrary.interfaces.media.Genre
import com.kbizsoft.medialibrary.media.MediaLibraryItem
import com.kbizsoft.tools.Settings
import com.kbizsoft.KPlayer.viewmodels.SortableModel

class AlbumsProvider(val parent : MediaLibraryItem?, context: Context, model: SortableModel) : MedialibraryProvider<Album>(context, model) {

    override val sortKey = "${super.sortKey}_${parent?.javaClass?.simpleName}"
    override fun canSortByDuration() = true
    override fun canSortByReleaseDate() = true
    override fun canSortByArtist() = true
    override fun canSortByInsertionDate()= true

    init {
        sort = Settings.getInstance(context).getInt(sortKey, if (parent is Artist) Medialibrary.SORT_RELEASEDATE else Medialibrary.SORT_DEFAULT)
        desc = Settings.getInstance(context).getBoolean("${sortKey}_desc", false)
        onlyFavorites = Settings.getInstance(context).getBoolean("${sortKey}_only_favs", false)
    }

    override fun getAll() : Array<Album> = when (parent) {
        is Artist -> parent.getAlbums(sort, desc, Settings.includeMissing, onlyFavorites)
        is Genre -> parent.getAlbums(sort, desc, Settings.includeMissing, onlyFavorites)
        else -> medialibrary.getAlbums(sort, desc, Settings.includeMissing, onlyFavorites)
    }

    override fun getPage(loadSize: Int, startposition: Int) : Array<Album> {
        val list = if (model.filterQuery == null) when(parent) {
            is Artist -> parent.getPagedAlbums(sort, desc, Settings.includeMissing, onlyFavorites, loadSize, startposition)
            is Genre -> parent.getPagedAlbums(sort, desc, Settings.includeMissing, onlyFavorites, loadSize, startposition)
            else -> medialibrary.getPagedAlbums(sort, desc, Settings.includeMissing, onlyFavorites, loadSize, startposition)
        } else when(parent) {
            is Artist -> parent.searchAlbums(model.filterQuery, sort, desc, Settings.includeMissing, onlyFavorites, loadSize, startposition)
            is Genre -> parent.searchAlbums(model.filterQuery, sort, desc, Settings.includeMissing, onlyFavorites, loadSize, startposition)
            else -> medialibrary.searchAlbum(model.filterQuery, sort, desc, Settings.includeMissing, onlyFavorites, loadSize, startposition)
        }
        model.viewModelScope.launch { completeHeaders(list, startposition) }
        return list
    }

    override fun getTotalCount() = if (model.filterQuery == null) when(parent) {
        is Artist -> parent.albumsCount
        is Genre -> parent.albumsCount
        else -> medialibrary.albumsCount
    } else when (parent) {
        is Artist -> parent.searchAlbumsCount(model.filterQuery)
        is Genre -> parent.searchAlbumsCount(model.filterQuery)
        else -> medialibrary.getAlbumsCount(model.filterQuery)
    }
}