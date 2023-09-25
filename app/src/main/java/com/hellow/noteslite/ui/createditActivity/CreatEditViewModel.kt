package com.hellow.noteslite.ui.createditActivity

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hellow.noteslite.adaptor.descAdaptor.EditAdaptor
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.model.NoteSubItem
import com.hellow.noteslite.model.NoteSubItemType
import com.hellow.noteslite.model.ThemeItem
import com.hellow.noteslite.repository.NotesRepository
import com.hellow.noteslite.utils.Event
import com.hellow.noteslite.utils.send
import kotlinx.coroutines.launch

class CreatEditViewModel(
    val app: Application,
    private val repository: NotesRepository,
    private val currentItem: NoteItem,
) : AndroidViewModel(app), EditAdaptor.Callback {

    // to handle focus request
    private val _focusEvent = MutableLiveData<Event<FocusChange>>()
    val focusEvent: LiveData<Event<FocusChange>>
        get() = _focusEvent

    private val _focusGainEvent = MutableLiveData<Boolean>(false)
    val focusGainEvent: LiveData<Boolean>
        get() = _focusGainEvent

    private val _checkBoxVisibilityEvent = MutableLiveData<Event<CheckBoxVisibility>>()
    val checkBoxVisibilityEvent: LiveData<Event<CheckBoxVisibility>>
        get() = _checkBoxVisibilityEvent

    fun changeCheckBoxVisibility() {
        var currentCheck = "String"
        if (listItems[focusPosition].type == NoteSubItemType.CheckBox) {
            currentCheck = "CheckBox"
        }
        Log.i("Check Changed", "Current Check - ${currentCheck}")

        _checkBoxVisibilityEvent.send(CheckBoxVisibility(focusPosition))
        listItems[focusPosition].type =
            if (listItems[focusPosition].type == NoteSubItemType.String) {
                NoteSubItemType.CheckBox
            } else {
                NoteSubItemType.String
            }

        updateListItems()
    }


    private val _editItems =
        MutableLiveData<MutableList<NoteSubItem>>(currentItem.description.toMutableList())
    val editItems: LiveData<out List<NoteSubItem>>
        get() = _editItems

    private val _title =
        MutableLiveData<String>(currentItem.title)
    val title: LiveData<String>
        get() = _title

    fun setTitle(text: String) {
        _title.value = text
    }

    var focusPosition: Int = -1;

    private fun updateListItems() {
        var i = 0
        while (i < listItems.size) {
            listItems[i].id = i
            i++
        }
        _editItems.value = listItems.toMutableList()
    }

    private val listItems: MutableList<NoteSubItem> = currentItem.description.toMutableList()

    fun getThemeItem(value: Int): ThemeItem {

        return when (app.applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                repository.getTheme(value, true)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                repository.getTheme(value, false)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                repository.getTheme(value, false)
            }

            else -> {
                repository.getTheme(value, false)
            }
        }
    }


    private fun focusItemAt(pos: Int, textPos: Int, itemExists: Boolean) {
        _focusEvent.send(FocusChange(pos, textPos, itemExists))
    }

    fun getAllThemes() = repository.getAllTheme()

    fun deleteNote() = viewModelScope.launch {
        repository.deleteNote(currentItem)
    }

    fun updateNote() {
        //   val currentItem =
    }
//
//    fun updateListId() {
//
//    }


    override fun newItemAdded(pos: Int, textCurrent: String, textNext: String) {

        val previousItemType = listItems[pos].type
        val item = NoteSubItem(pos + 1, previousItemType, false, textNext)

        Log.i(
            "Item Added",
            "current text :${textCurrent} , next text:${textNext} , type = ${previousItemType}"
        )

        listItems[pos].textValue = textCurrent

        if (pos == listItems.size) {
            listItems.add(item)
            updateListItems()
        } else {
            listItems.add((pos + 1), item)
            updateListItems()
        }

        focusItemAt(pos + 1, textNext.length, false)
    }

    override fun itemDeleted(pos: Int, text: String) {
        Log.i("Item Delete", "before text $text")

        val focusLoc = listItems[pos - 1].textValue.length
        Log.i("Item Delete", "text at  ${listItems[pos - 1].textValue}")

        val textValue = listItems[pos - 1].textValue + text
        listItems[pos - 1].textValue = textValue

        Log.i("Item Delete", "final text $textValue , selPos - $focusLoc")

        Log.i("Item Delete", "before remove ${listItems.size}")
        listItems.removeAt(pos)
        Log.i("Item Delete", "after remove ${listItems.size}")
        updateListItems()
        focusItemAt(pos - 1, focusLoc, true)
    }

    fun resetFocusGainEvent() {
        _focusGainEvent.value = false
    }


    override fun focusLose(pos: Int, text: String) {
        // focus lost
        Log.i("Focus Changed", "Focus lost - $pos")

        // listItems[pos].textValue = text
        updateListItems()

        focusPosition = -1
    }

    override fun focusGain(pos: Int) {
        Log.i("Focus Changed", "Focus gain - $pos")
        // set the is focused in activity ui
        focusPosition = pos
        _focusGainEvent.value = true

    }

    fun changeItem() {

    }

    override fun checkChanged(pos: Int, isChecked: Boolean) {
        listItems[pos].checkBox = isChecked
    }

    override fun textChanged(pos: Int, text: String) {
        // update the current text
        Log.i("text Value Changed", "Text value - ${text} ,  pos - ${pos}")
        listItems[pos].textValue = text
    }

    data class FocusChange(val itemPos: Int, val pos: Int, val itemExists: Boolean)

    data class CheckBoxVisibility(val pos: Int)

}