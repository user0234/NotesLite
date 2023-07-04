package com.hellow.noteslite.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hellow.noteslite.databinding.NoteListItemBinding
import com.hellow.noteslite.model.NoteItem

open class NotesAdaptor: RecyclerView.Adapter<NotesAdaptor.NotesViewHolder>(){

    inner class NotesViewHolder(val binding: NoteListItemBinding): RecyclerView.ViewHolder(binding.root){
    }

    private val differCallBack = object : DiffUtil.ItemCallback<NoteItem>(){
        override fun areItemsTheSame(oldItem: NoteItem, newItem: NoteItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NoteItem, newItem: NoteItem): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this,differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {

        val binding:NoteListItemBinding = NoteListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return NotesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val currentItem =  differ.currentList[position]
        holder.binding.tvTitle.text = currentItem.title
        holder.binding.tvDescription.text = currentItem.description
      //  holder.binding.tvTime.text = dateConverter(currentItem.date)
        holder.binding.root.setCardBackgroundColor(currentItem.backgroundColor)
        setOnItemClickListener {
            onItemClickListener?.let {
                it(currentItem)
            }
        }
    }

    private var onItemClickListener: ((NoteItem) -> Unit)? = null

    fun setOnItemClickListener(listener: (NoteItem) -> Unit) {
        onItemClickListener = listener
    }
}