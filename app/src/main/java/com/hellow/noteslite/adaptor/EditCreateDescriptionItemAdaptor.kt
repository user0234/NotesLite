package com.hellow.noteslite.adaptor

import android.graphics.Color
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hellow.noteslite.databinding.CreateEditDescriptionItemBinding
import com.hellow.noteslite.model.NoteSubItem
import com.hellow.noteslite.model.NoteSubItemType
import com.hellow.noteslite.model.ThemeItem

class EditCreateDescriptionItemAdaptor(private val themeItem: ThemeItem) :
    RecyclerView.Adapter<EditCreateDescriptionItemAdaptor.ViewHolderDescriptionItem>() {

    inner class ViewHolderDescriptionItem(val binding: CreateEditDescriptionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<NoteSubItem>() {
        override fun areItemsTheSame(oldItem: NoteSubItem, newItem: NoteSubItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NoteSubItem, newItem: NoteSubItem): Boolean {
            return oldItem.type == newItem.type && oldItem.textValue == newItem.textValue
        }
    }
    val differ = AsyncListDiffer(this, differCallBack)

    // this to keep up the theme info to change text color
    var noteItemTheme = themeItem
    var keyBoardFocusItem: Int = -1
    var wasFocusTitle = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolderDescriptionItem {
        val checkBoxBinding: CreateEditDescriptionItemBinding =
            CreateEditDescriptionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolderDescriptionItem(checkBoxBinding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolderDescriptionItem, position: Int) {
        val currentItem: NoteSubItem = differ.currentList[position]

        // on create stuff to be done
        holder.binding.etSubText.setText(currentItem.textValue)
        if(position == 0){
            holder.binding.etSubText.hint = "Make SomeThing"
        }

        if (currentItem.type == NoteSubItemType.String) {
            holder.binding.checkBoxSubMain.visibility = View.GONE
        } else {
            holder.binding.checkBoxSubMain.visibility = View.VISIBLE
            holder.binding.checkBoxSubMain.isChecked = currentItem.checkBox
            if (currentItem.checkBox) {
                holder.binding.etSubText.setTextColor(Color.parseColor(themeItem.hintTextColor))
            } else {
                holder.binding.etSubText.setTextColor(Color.parseColor(themeItem.editTextColor))
            }
        }

        holder.binding.checkBoxSubMain.setOnClickListener {
            // after changing it got checked
            if (holder.binding.checkBoxSubMain.isChecked) {
                // change color of text and update in differ
                differ.currentList[holder.adapterPosition].checkBox = true
                holder.binding.etSubText.setTextColor(Color.parseColor(themeItem.hintTextColor))
            } else {
                // after changing it got Unchecked
                differ.currentList[holder.adapterPosition].checkBox = false
                holder.binding.etSubText.setTextColor(Color.parseColor(themeItem.editTextColor))
            }
        }

        holder.binding.etSubText.setOnFocusChangeListener { _, hasFocus ->
                  if(hasFocus){
                      // current focus is got set on this item so change the $keyBoardFocusItem to this position
                      // and call a function to let the activity know
                      keyBoardFocusItem = position

                      if(wasFocusTitle){
                           wasFocusTitle = false

                      }


                      onItemChangeFocusListener?.let {
                          it(true,position)
                      }

                  }else{
                     // current focus is got set on this item so change the $$keyBoardFocusItem to -1
                     //  and call a function to let the activity know
                      currentItem.id
                      keyBoardFocusItem = -1
                     differ.currentList[position].textValue = holder.binding.etSubText.text.toString()
                      onItemChangeFocusListener?.let {
                          it(false,position)
                      }
                  }
        }
        holder.binding.etSubText.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_GO ) {
                // create next item of same type  and add focus to it
                holder.binding.etSubText.clearFocus()
                onItemAddDeleteListener?.let {
                    it(true,currentItem.type,holder.adapterPosition,holder.binding.etSubText.text.toString())
                }

                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false;

        }

        holder.binding.etSubText.setOnKeyListener { _, keyCode, keyEvent ->
                // change to next or previous item based on if the item is to be added or deleted
            if (keyCode == KeyEvent.KEYCODE_ENTER ) {
                // create next item of same type  and add focus to it
                holder.binding.etSubText.clearFocus()
                onItemAddDeleteListener?.let {
                    it(true,currentItem.type,holder.adapterPosition,holder.binding.etSubText.text.toString())
                }
            }
            if (keyCode == KeyEvent.KEYCODE_DEL && differ.currentList.size != 1 && holder.binding.etSubText.toString() == "" ) {
                // delete the current item and set focus to previous item
                holder.binding.etSubText.clearFocus()
                onItemAddDeleteListener?.let {
                    it(false,currentItem.type,holder.adapterPosition,holder.binding.etSubText.text.toString())
                }
            }

            true
        }


        if(keyBoardFocusItem == position){
            onItemChangeFocusListener?.let {
                it(true,position)
            }
        }


    }

    private var onItemChangeFocusListener: ((Boolean,Int) -> Unit)? = null

    fun setOnItemChangeFocusListener(listener: (Boolean,Int) -> Unit) {
        onItemChangeFocusListener = listener
    }


    // isCreate:Boolean,Item type :NoteSubItemType,position of current Item:Int
    private var onItemAddDeleteListener: ((Boolean,NoteSubItemType,Int,String) -> Unit)? = null

    fun setOnItemAddDeleteListener(listener: (Boolean,NoteSubItemType,Int,String) -> Unit) {
        onItemAddDeleteListener = listener
    }
}