package com.hellow.noteslite.adaptor.descAdaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hellow.noteslite.databinding.CheckBoxAdaptorItemBinding
import com.hellow.noteslite.model.NoteSubItem
import com.hellow.noteslite.model.NoteSubItemType
import com.hellow.noteslite.model.ThemeItem
import com.hellow.noteslite.ui.createditActivity.CreatEditViewModel

class EditAdaptor(private val themeItem: ThemeItem, val callback: Callback) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var pendingFocusChange: CreatEditViewModel.FocusChange? = null

    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
    }

    private val differCallBack = object : DiffUtil.ItemCallback<NoteSubItem>() {
        override fun areItemsTheSame(oldItem: NoteSubItem, newItem: NoteSubItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NoteSubItem, newItem: NoteSubItem): Boolean {
            return oldItem.type == newItem.type && oldItem.id == newItem.id && newItem.checkBox == oldItem.checkBox && newItem.textValue.length == oldItem.textValue.length && newItem.textValue == oldItem.textValue
        }
    }
    val differ = AsyncListDiffer(this, differCallBack)
    var focusItemPosition: Int = -1
    var noteItemTheme: ThemeItem = themeItem

    // this to keep up the theme info to change text color
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            NoteSubItemType.CheckBox.ordinal -> CheckBoxItemViewHolder(
                CheckBoxAdaptorItemBinding.inflate(inflater, parent, false),
                callback
            )

//            NoteSubItemType.CheckBox.ordinal -> TextItemViewHolder(
//                TextAdaptorItemBinding.inflate(inflater, parent, false),
//                callback
//            )

            else -> {
                error("Unknown view type")
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem: NoteSubItem = differ.currentList[position]
        when (holder) {
            is CheckBoxItemViewHolder -> {
                holder.bind(currentItem,themeItem)
            }
        }

        if (holder is EditFocusableViewHolder && position == pendingFocusChange?.itemPos) {
            // Apply pending focus change event.
            holder.setFocus(pendingFocusChange!!.pos)
            pendingFocusChange = null
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return NoteSubItemType.CheckBox.ordinal
    }

    fun changeCheckVisibility(checkboxVisible: CreatEditViewModel.CheckBoxVisibility) {
        val rcv = recyclerView ?: return
        val viewHolder = rcv.findViewHolderForAdapterPosition(checkboxVisible.pos)
        if (viewHolder is EditFocusableViewHolder) {
            viewHolder.showCheckBox()
        } else {
            return
        }
    }

    fun setItemFocus(focus: CreatEditViewModel.FocusChange) {
        val rcv = recyclerView ?: return

        // If item to focus on doesn't exist yet, save it for later.
        if (!focus.itemExists) {
            pendingFocusChange = focus
            return
        }

        val viewHolder = rcv.findViewHolderForAdapterPosition(focus.itemPos)
        if (viewHolder is EditFocusableViewHolder) {
            viewHolder.setFocus(focus.pos)
        } else {
            // No item view holder for that position.
            // Not supposed to happen, but if it does, just save it for later.
            pendingFocusChange = focus
        }
    }

    interface Callback {

        fun newItemAdded(pos: Int, textCurrent: String,textNext:String)

        fun itemDeleted(pos: Int, text: String)

        fun focusLose(pos: Int,text: String)

        fun focusGain(pos: Int)

        fun checkChanged(pos: Int, isChecked: Boolean)

        fun textChanged(pos:Int,text: String)

    }

}

