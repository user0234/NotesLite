package com.hellow.noteslite.adaptor

import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hellow.noteslite.databinding.NoteListItemBinding
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.model.ThemeItem
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
        holder.binding.tvDescription.text = currentItem.descriptionText
         holder.binding.tvTime.text = ConstantValues.dateConvert(currentItem.id)


        val currentTheme:ThemeItem = when (holder.itemView.context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                ConstantValues.getNightModeTheme(currentItem.backgroundColor)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                ConstantValues.getLightModeTheme(currentItem.backgroundColor)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                ConstantValues.getLightModeTheme(currentItem.backgroundColor)
                 }

            else -> {
                ConstantValues.getLightModeTheme(currentItem.backgroundColor)
            }
        }

        holder.binding.root.setCardBackgroundColor(Color.parseColor(currentTheme.backGroundColor))
        holder.binding.tvTitle.setTextColor(Color.parseColor(currentTheme.editTextColor))
        holder.binding.tvTime.setTextColor(Color.parseColor(currentTheme.hintTextColor))
        holder.binding.tvDescription.setTextColor(Color.parseColor(currentTheme.hintTextColor))


        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(currentItem,holder.binding.noteLl,holder.adapterPosition)
            }
             holder.itemView.animate()
                 .scaleY(1F)
                 .scaleX(1F)
                 .setDuration(80)
                 .start()
            holder.binding.expendTransitionView.visibility = View.GONE
        }

        holder.itemView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                when(event?.action){
                    MotionEvent.ACTION_DOWN -> {
                        holder.itemView.animate()
                            .scaleY(0.83F)
                            .scaleX(0.83F)
                            .setDuration(80)
                            .start()
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        holder.itemView.animate()
                            .scaleX(1F)
                            .scaleY(1F)
                            .setDuration(80)
                            .start()
                    }
                    MotionEvent.ACTION_UP -> {
                        holder.itemView.animate()
                            .scaleX(1F)
                            .scaleY(1F)
                            .setDuration(80)
                            .start()
                    }
                }
                 return v?.onTouchEvent(event) ?: true
            }

        })
//        val res: Resources = holder.itemView.context.resources
//        holder.binding.tvTitle.width =
//            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 275f, res.displayMetrics).toInt()

    }

    private var onItemClickListener: ((NoteItem,View,Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (NoteItem,View,Int) -> Unit) {
        onItemClickListener = listener
    }

}

