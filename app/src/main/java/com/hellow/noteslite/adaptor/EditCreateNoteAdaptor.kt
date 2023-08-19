package com.hellow.noteslite.adaptor

import android.graphics.Color
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hellow.noteslite.databinding.CreateEditAdaptorCheckboxItemBinding
import com.hellow.noteslite.databinding.CreateEditAdaptorStringItemBinding
import com.hellow.noteslite.model.NoteSubItem
import com.hellow.noteslite.model.NoteSubItemType
import com.hellow.noteslite.model.ThemeItem

class EditCreateNoteAdaptor(private val themeItem: ThemeItem) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ViewHolderStringType(val stringBinding: CreateEditAdaptorStringItemBinding) :
        RecyclerView.ViewHolder(stringBinding.root)

    inner class ViewHolderCheckBoxType(val checkBoxBinding: CreateEditAdaptorCheckboxItemBinding) :
        RecyclerView.ViewHolder(checkBoxBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<NoteSubItem>() {
        override fun areItemsTheSame(oldItem: NoteSubItem, newItem: NoteSubItem): Boolean {
            return oldItem.id == newItem.id && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: NoteSubItem, newItem: NoteSubItem): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallBack)
    var keyBoardFocusItem: Int = -1

    // set the theme when attach adaptor and also when we change theme
    var noteItemTheme  = themeItem


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            0 -> {
                val stringBinding: CreateEditAdaptorStringItemBinding =
                    CreateEditAdaptorStringItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return ViewHolderStringType(stringBinding)
            }

            1 -> {
                val checkBoxBinding: CreateEditAdaptorCheckboxItemBinding =
                    CreateEditAdaptorCheckboxItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return ViewHolderCheckBoxType(checkBoxBinding)
            }
        }
        val stringBinding: CreateEditAdaptorStringItemBinding =
            CreateEditAdaptorStringItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolderStringType(stringBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = differ.currentList[position]
        when (currentItem.type) {
            NoteSubItemType.String -> {

                val stringHolder = holder as ViewHolderStringType
                val stringBinding = stringHolder.stringBinding

                stringBinding.etSubText.setText(currentItem.textValue)
                stringBinding.etSubText.setTextColor(Color.parseColor(themeItem.editTextColor))

                stringBinding.etSubText.setOnFocusChangeListener { _, hasFocus  ->
                    // save the text when change viewType and change the way focus works
                              if(hasFocus){
                                  keyBoardFocusItem = holder.adapterPosition
                                  onItemChangeFocusListener?.let {
                                      it(true)
                                  }
                              }else{
                              //    differ.currentList[position].data = stringBinding.etSubText.text.toString()
                                  keyBoardFocusItem = -1
                                  differ.currentList[holder.adapterPosition].textValue = stringBinding.etSubText.text.toString()
                                  onItemChangeFocusListener?.let {
                                      it(false)
                                  }
                              }
                }

                stringBinding.etSubText.setOnKeyListener { _, keyCode, _ ->


                    if (keyCode == KeyEvent.KEYCODE_ENTER ) {
                        // create next item of same type  and add focus to it
                       keyBoardFocusItem = position + 1
                        onItemClickListener?.let {
                            it(currentItem.type,true,position)
                        }
                    }
                    if (keyCode == KeyEvent.KEYCODE_DEL && differ.currentList.size != 1 && stringBinding.etSubText.text.toString() == "" ) {
                       // delete the current item and set focus to previous item
                        keyBoardFocusItem = position - 1
                        onItemClickListener?.let {
                            it(currentItem.type,false,position)
                        }
                    }

                    true
                }
                if((keyBoardFocusItem != -1) && (keyBoardFocusItem == position)){
                    stringBinding.etSubText.requestFocus()

                }

            }

            NoteSubItemType.CheckBox -> {
                val checkBoxHolder = holder as ViewHolderCheckBoxType
                val checkBoxBinding = checkBoxHolder.checkBoxBinding
                checkBoxBinding.checkBoxSubMain.isChecked = currentItem.checkBox!!
                checkBoxBinding.etSubText.setText(currentItem.textValue)
                if(checkBoxBinding.checkBoxSubMain.isChecked){
                    checkBoxBinding.etSubText.setTextColor(Color.parseColor(themeItem.hintTextColor))
                }else{
                    checkBoxBinding.etSubText.setTextColor(Color.parseColor(themeItem.editTextColor))
                }

                checkBoxBinding.checkBoxSubMain.setOnClickListener {
                    if(checkBoxBinding.checkBoxSubMain.isChecked){
                        // change the color of text to when checked
                        checkBoxBinding.etSubText.setTextColor(Color.parseColor(themeItem.hintTextColor))
                        differ.currentList[position].checkBox = true

                      //  android:buttonTint
                    }else{
                        // change the color of text to when unchecked
                        checkBoxBinding.etSubText.setTextColor(Color.parseColor(themeItem.editTextColor))
                        differ.currentList[position].checkBox = false
                    }
                }



                checkBoxBinding.etSubText.setOnFocusChangeListener { view, hasFocus ->

                    if(hasFocus){
                        keyBoardFocusItem = holder.adapterPosition
                        onItemChangeFocusListener?.let {
                            it(true)
                        }
                    }else{
                //        differ.currentList[position].data = checkBoxBinding.etSubText.text.toString()
                        keyBoardFocusItem = -1
                        differ.currentList[holder.adapterPosition].textValue = checkBoxBinding.etSubText.text.toString()
                        onItemChangeFocusListener?.let {
                            it(false)
                        }
                    }
                }

                checkBoxBinding.etSubText.setOnKeyListener { _, keyCode, _ ->


                    if (keyCode == KeyEvent.KEYCODE_ENTER ) {
                        // create next item of same type  and add focus to it
                        keyBoardFocusItem = position + 1
                        onItemClickListener?.let {
                            it(currentItem.type,true,position + 1)
                        }
                    }
                    if (keyCode == KeyEvent.KEYCODE_DEL && differ.currentList.size != 1 && checkBoxBinding.etSubText.text.toString() == "" ) {
                        // delete the current item and set focus to previous item
                        keyBoardFocusItem = position - 1
                        onItemClickListener?.let {
                            it(currentItem.type,false,position)
                        }
                    }

                    true
                }

                if(keyBoardFocusItem != -1){
                    if(keyBoardFocusItem == position){
                        checkBoxBinding.etSubText.requestFocus()
                    }
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {

        return when (differ.currentList[position].type) {
            NoteSubItemType.String -> {
                0
            }

            NoteSubItemType.CheckBox -> {
                1
            }

        }
    }

    private var onItemChangeFocusListener: ((Boolean) -> Unit)? = null

    fun setOnItemChangeFocusListener(listener: (Boolean) -> Unit) {
        onItemChangeFocusListener = listener
    }

    private var onItemClickListener: ((NoteSubItemType,Boolean,Int ) -> Unit)? = null

    fun setOnItemClickListener(listener: (NoteSubItemType,Boolean,Int ) -> Unit) {
        onItemClickListener = listener
    }



}