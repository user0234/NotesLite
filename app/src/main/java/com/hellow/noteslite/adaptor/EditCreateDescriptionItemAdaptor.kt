package com.hellow.noteslite.adaptor

import android.graphics.Color
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
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
            return oldItem.type == newItem.type &&  oldItem.id == newItem.id
        }
    }
    val differ = AsyncListDiffer(this, differCallBack)
    var focusItemPosition:Int = -1
    var noteItemTheme:ThemeItem = themeItem

    // this to keep up the theme info to change text color
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

        holder.binding.etSubText.setOnFocusChangeListener { _, hasFocus ->
            onItemChangeFocusListener?.let {
                it(hasFocus,holder.adapterPosition,holder.binding.etSubText.text.toString())
            }
        }

        holder.binding.etSubText.addTextChangedListener { text ->
            if(text.toString().endsWith("\n")){
                holder.binding.etSubText.setText(text.toString().removeSuffix("\n"))

                onItemAddDeleteListener?.let { item ->
                    item(true,currentItem.type,position,text.toString().removeSuffix("\n"))
                }
            }else{
                if(text.toString() == "\n"){
                    holder.binding.etSubText.setText("")
                }
            }
        }

        holder.binding.etSubText.setOnKeyListener { _, keyCode, keyEvent ->

                // change to next or previous item based on if the item is to be added or deleted
            if ((focusItemPosition != 0) && (keyCode == KeyEvent.KEYCODE_DEL)  ) {
                // delete the current item and set focus to previous item
                onItemAddDeleteListener?.let {
                    it(false,currentItem.type,position,holder.binding.etSubText.text.toString())
                }
            }

            true
        }

        // on create stuff to be done
        holder.binding.etSubText.setText(currentItem.textValue)
        if(currentItem.id == 0){
            holder.binding.etSubText.hint = "Make SomeThing"
        } else{
            holder.binding.etSubText.hint = ""
        }


        if(focusItemPosition == position){

            holder.binding.etSubText.requestFocus()
            holder.binding.etSubText.setSelection(holder.binding.etSubText.text.toString().length)
        }


        if (currentItem.type == NoteSubItemType.String) {
            holder.binding.checkBoxSubMain.visibility = View.GONE
        } else {
            holder.binding.checkBoxSubMain.visibility = View.VISIBLE
            holder.binding.checkBoxSubMain.isChecked = currentItem.checkBox
            if (currentItem.checkBox) {
                holder.binding.etSubText.setTextColor(Color.parseColor(noteItemTheme.hintTextColor))
            } else {
                holder.binding.etSubText.setTextColor(Color.parseColor(noteItemTheme.editTextColor))
            }
        }

        holder.binding.checkBoxSubMain.setOnClickListener {
            // after changing it got checked
            if (holder.binding.checkBoxSubMain.isChecked) {
                // change color of text and update in differ
                differ.currentList[holder.adapterPosition].checkBox = true
                holder.binding.etSubText.setTextColor(Color.parseColor(noteItemTheme.hintTextColor))
            } else {
                // after changing it got Unchecked
                differ.currentList[holder.adapterPosition].checkBox = false
                holder.binding.etSubText.setTextColor(Color.parseColor(noteItemTheme.editTextColor))
            }
        }


    }

    private var onItemChangeFocusListener: ((Boolean,Int,String) -> Unit)? = null

    fun setOnItemChangeFocusListener(listener: (Boolean,Int,String) -> Unit) {
        onItemChangeFocusListener = listener
    }

    // isCreate:Boolean,Item type :NoteSubItemType,position of current Item:Int
    private var onItemAddDeleteListener: ((Boolean,NoteSubItemType,Int,String) -> Unit)? = null

    fun setOnItemAddDeleteListener(listener: (Boolean,NoteSubItemType,Int,String) -> Unit) {
        onItemAddDeleteListener = listener
    }


}