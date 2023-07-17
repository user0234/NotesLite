package com.hellow.noteslite.adaptor

import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hellow.noteslite.databinding.NoteListItemBinding
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.utils.ConstantValues


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
         holder.binding.tvTime.text = ConstantValues.dateConvert(currentItem.id)
        val color:String = ConstantValues.BackGroundColor[currentItem.backgroundColor]
        holder.binding.root.setCardBackgroundColor((Color.parseColor(color)))
        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(currentItem)
            }
        }
        val res: Resources = holder.itemView.context.resources
        holder.binding.tvTitle.width =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 275f, res.displayMetrics).toInt()

        holder.itemView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN ->  {
                         // change the size on view
                         // TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65f, resources.displayMetrics)
                    }
                    MotionEvent.ACTION_UP -> {
                         // reset the size of view
                    }
                }

                return v?.onTouchEvent(event) ?: true
            }
        })
    }

    private var onItemClickListener: ((NoteItem) -> Unit)? = null

    fun setOnItemClickListener(listener: (NoteItem) -> Unit) {
        onItemClickListener = listener
    }


}

