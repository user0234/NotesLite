package com.hellow.noteslite.utils

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.repository.NotesRepository
import com.hellow.noteslite.ui.createditActivity.CreatEditViewModel

class CreatEditViewModelProvider(val app: Application,
                                 private val repository: NotesRepository,
                                 private val currentItem:NoteItem
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreatEditViewModel(app,repository,currentItem) as T
    }

}