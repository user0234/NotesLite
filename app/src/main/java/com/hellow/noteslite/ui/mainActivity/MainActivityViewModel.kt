package com.hellow.noteslite.ui.mainActivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.repository.NotesRepository
import kotlinx.coroutines.launch

class MainActivityViewModel(app: Application,
                            private val repository: NotesRepository
                            ) : AndroidViewModel(app) {

                      fun getNotesList() = repository.getNotes()

                         fun deleteNote(note: NoteItem) = viewModelScope.launch {
                           repository.deleteNote(note)
                     }
                          fun addNote(note: NoteItem) = viewModelScope.launch {
                              repository.createNote(note)
                          }

                           }