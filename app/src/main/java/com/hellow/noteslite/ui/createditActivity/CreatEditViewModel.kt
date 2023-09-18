package com.hellow.noteslite.ui.createditActivity

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.model.NoteSubItem
import com.hellow.noteslite.model.NoteSubItemType
import com.hellow.noteslite.model.ThemeItem
import com.hellow.noteslite.repository.NotesRepository
import kotlinx.coroutines.launch

class CreatEditViewModel(
    val app: Application,
    private val repository: NotesRepository,
    private val currentItem: NoteItem,
) : AndroidViewModel(app) {

    // set the items for livedata and state saving
    private var _description = MutableLiveData<List<NoteSubItem>>(currentItem.description)
    val descListLiveData: LiveData<List<NoteSubItem>>
        get() = _description

    private var _themeColor = MutableLiveData<Int>(currentItem.backgroundColor)
    val themeLiveData: LiveData<Int>
        get() = _themeColor
    fun setTitle(value: String) {
         currentItem.title = value
    }
    fun setThemeValue(value: Int) {
        if (_themeColor.value == value) {
            return
        }
        _themeColor.value = value

        currentItem.backgroundColor = value

    }

    fun getThemeItem(value:Int):ThemeItem{

       return when (app.applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                repository.getTheme(value,true)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                repository.getTheme(value,false)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                repository.getTheme(value,false)
            }

           else -> {
               repository.getTheme(value,false)
           }
       }
    }
    fun getAllThemes() = repository.getAllTheme()

    fun changeDescriptionItem(item: NoteSubItem){
        val list: MutableList<NoteSubItem> = _description.value as MutableList<NoteSubItem>

        list[item.id].type = item.type
        list[item.id].textValue = item.textValue

        _description.value = list
    }

    fun editDescriptionItem(isCreate:Boolean,position:Int,type:NoteSubItemType,text:String){
             if(isCreate){
                  val item:NoteSubItem = NoteSubItem(position + 1,type,false,"")
                  addNewDescriptionItem(item)
             }else{
                  deleteDescriptionItem(position)
             }
    }

   private fun addNewDescriptionItem(item: NoteSubItem) {
        val myList: MutableList<NoteSubItem> = _description.value!!.toMutableList()

        if(item.id==myList.size){
            // inserted at end
            myList.add(item)
        }else{
            // inserted at pos
            myList.add(item.id,item)
            var value:Int = item.id + 1
            while (value < myList.size){

                myList[value].id = value
                ++value
            }
        }
        _description.value = myList
    }

   private fun deleteDescriptionItem(position: Int){
        val myList:MutableList<NoteSubItem> = _description.value!!.toMutableList()
       myList.removeAt(position)
       myList.sortBy { it.id }
        var value = 0
        while (value < myList.size){

            myList[value].id = value
            value++
        }

       _description.value = myList
    }

 fun updateDescriptionItem(value:List<NoteSubItem>){
       _description.value = value
   }


    fun deleteNote() = viewModelScope.launch {
        repository.deleteNote(currentItem)
    }

    fun updateNote() {

        currentItem.description = _description.value!!
        currentItem.backgroundColor = _themeColor.value!!

        for(i in currentItem.description){
            i.textValue.removeSuffix("\n")
            if(i.textValue!= ""){
                currentItem.descriptionText = i.textValue
                break
            }
        }

        if (currentItem.title == "" && currentItem.descriptionText == "") {
            viewModelScope.launch {
                repository.deleteNote(currentItem)
            }
        } else {
            viewModelScope.launch {
                repository.updateNote(currentItem)
            }
        }

    }

}