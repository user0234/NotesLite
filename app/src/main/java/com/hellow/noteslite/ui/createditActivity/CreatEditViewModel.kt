package com.hellow.noteslite.ui.createditActivity

import android.app.Application
import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.repository.NotesRepository
import kotlinx.coroutines.launch

class CreatEditViewModel(app: Application,
    private val repository: NotesRepository
) : AndroidViewModel(app) {

   // these are for keeping the state
   private var _title = MutableLiveData<String>("")
   val titleLiveData:LiveData<String>
       get() = _title

    fun setTitle(value:String){
        if(_title.value.equals(value)){
            return
        }
        _title.value = value
    }

    private var _description = MutableLiveData<String>()
    val descLiveData:LiveData<String>
        get() = _description

    fun setDesc(value:String){
        if(_description.value.equals(value)){
            return
        }
        _description.value = value
    }

    private var _themeColor = MutableLiveData<Int>()
    val themeLiveData:LiveData<Int>
        get() = _themeColor

    fun setThemeValue(value:Int){
        if(_themeColor.value == value){
            return
        }
        _themeColor.value = value
    }

    private var _time = MutableLiveData<String>()
    val timeLiveData:LiveData<String>
        get() = _time

    private fun setTimeValue(value:String){
        if(_time.value == value){
            return
        }
        _time.value = value
    }

    // current stored note
   private lateinit var currentNote:NoteItem

    fun setCurrentNote(noteItem: NoteItem){
        currentNote = noteItem

        setTitle(noteItem.title)
        setDesc(noteItem.description)
        setThemeValue(noteItem.backgroundColor)
        setTimeValue(noteItem.id)
    }

    fun deleteNote() = viewModelScope.launch {
        repository.deleteNote(currentNote)
    }

    fun updateNote() {

        currentNote.title = _title.value!!
        currentNote.description = _description.value!!
        currentNote.backgroundColor = _themeColor.value!!

        if(currentNote.title == "" && currentNote.description == ""){
            viewModelScope.launch {
                repository.deleteNote(currentNote)
            }
        } else{
            viewModelScope.launch {
                repository.updateNote(currentNote)
            }
        }



    }

}