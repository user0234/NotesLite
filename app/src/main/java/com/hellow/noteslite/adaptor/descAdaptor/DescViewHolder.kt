package com.hellow.noteslite.adaptor.descAdaptor

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hellow.noteslite.databinding.CheckBoxAdaptorItemBinding
import com.hellow.noteslite.model.NoteSubItem
import com.hellow.noteslite.model.NoteSubItemType
import com.hellow.noteslite.utils.showKeyboard

sealed interface EditFocusableViewHolder {
    fun setFocus(pos: Int)
    fun showCheckBox()
}

class CheckBoxItemViewHolder(
    binding: CheckBoxAdaptorItemBinding,
    callback: EditAdaptor.Callback,
) : RecyclerView.ViewHolder(binding.root), EditFocusableViewHolder {

    private val editText = binding.etSubText
    private val checkBox = binding.checkBoxSubMain
    private var itemValue: NoteSubItem? = null

    init {
        editText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    callback.focusGain(pos)
                }
                // we got focus on this item
            } else {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    callback.focusLose(pos, editText.text.toString())
                }
                // we lost focus from this item

            }
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    callback.textChanged(pos,s.toString())
                }

            }

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.contains("\n")) {
                    var newLinePos: Int = 0
                    while (newLinePos < text.length) {
                        if (text[newLinePos] == '\n') {
                            break
                        }
                        newLinePos++
                    }
                    val textCurrent = text.substring(0, newLinePos)
                    val textNext = if (newLinePos == text.length - 1) {
                        ""
                    } else {
                        text.substring(newLinePos + 1, text.length)
                    }

                    editText.setText(textCurrent)
                    editText.setSelection(textCurrent.length)
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        callback.newItemAdded(pos, textCurrent, textNext)
                    }
                }
            }

        })
        editText.setOnKeyListener { _, keyCode, keyEvent ->
            val isCursorAtStart =
                editText.selectionStart == 0 && editText.selectionStart == editText.selectionEnd

            if (keyCode == KeyEvent.KEYCODE_DEL && isCursorAtStart) {
                val pos = itemValue?.id ?: bindingAdapterPosition
                val text = editText.text.toString()

                if (pos != RecyclerView.NO_POSITION && pos != 0) {
                    callback.itemDeleted(pos, text)
                }
            }
            return@setOnKeyListener true
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                callback.checkChanged(pos, isChecked)
            }
        }
    }

    fun bind(item: NoteSubItem) {
        itemValue = item
        editText.setText(item.textValue)
        checkBox.isChecked = item.checkBox
        if(item.type == NoteSubItemType.String){
            checkBox.visibility = View.GONE
        }else{
            checkBox.visibility = View.VISIBLE
        }
    }

    override fun setFocus(pos: Int) {
        editText.requestFocus()
        editText.setSelection(pos)
        editText.showKeyboard()
    }

    override fun showCheckBox() {
        if (checkBox.visibility == View.GONE) {
            checkBox.visibility = View.VISIBLE
        } else {
            checkBox.visibility = View.GONE
        }
    }

}

//class TextItemViewHolder(
//    binding: TextAdaptorItemBinding,
//    callback: EditAdaptor.Callback,
//) : RecyclerView.ViewHolder(binding.root), EditFocusableViewHolder {
//    private val editText = binding.etSubText
//    private var item: NoteSubItem? = null
//
//    init {
//        editText.setOnFocusChangeListener { view, hasFocus ->
//            if (hasFocus) {
//                val pos = bindingAdapterPosition
//                if (pos != RecyclerView.NO_POSITION) {
//                    callback.focusGain(pos)
//                }
//                // we got focus on this item
//            } else {
//                val pos = bindingAdapterPosition
//                if (pos != RecyclerView.NO_POSITION) {
//                    callback.focusLose(pos)
//                }
//                // we lost focus from this item
//
//            }
//        }
//        editText.addTextChangedListener { text ->
//            if (text.toString().endsWith("\n") || text.toString() == "\n") {
//                val pos = bindingAdapterPosition
//                if (pos != RecyclerView.NO_POSITION) {
//                    callback.newItemAdded(pos, text.toString())
//                }
//
//            }
//        }
//        editText.setOnKeyListener { _, keyCode, keyEvent ->
//            val isCursorAtStart =
//                editText.selectionStart == 0 && editText.selectionStart == editText.selectionEnd
//
//            if (keyCode == KeyEvent.KEYCODE_DEL && isCursorAtStart) {
//                val pos = bindingAdapterPosition
//                val text = editText.text.toString()
//
//                // don't know for now
//                if (pos != RecyclerView.NO_POSITION || pos != 0) {
//                    callback.itemDeleted(pos, text)
//                }
//            }
//            return@setOnKeyListener true
//        }
//
//
//    }
//
//    fun bind(item: NoteSubItem) {
//        this.item = item
//        editText.setText(item.textValue)
//    }
//
//    override fun setFocus(pos: Int) {
//        editText.requestFocus()
//        editText.setSelection(pos)
//        editText.showKeyboard()
//    }
//
//
//}