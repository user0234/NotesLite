package com.hellow.noteslite.utils

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hellow.noteslite.repository.NotesRepository
import com.hellow.noteslite.ui.mainActivity.MainActivityViewModel

class MainViewModelProvider(val app: Application,
                            private val repository: NotesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainActivityViewModel(app,repository) as T
    }

}