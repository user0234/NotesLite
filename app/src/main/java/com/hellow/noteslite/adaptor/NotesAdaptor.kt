package com.hellow.noteslite.adaptor

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.transition.AutoTransition
import android.transition.TransitionManager
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
        when (holder.itemView.context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                if(currentItem.backgroundColor == 0){
                    holder.binding.root.setCardBackgroundColor(Color.parseColor(ConstantValues.NightModeDefaultTheme.backGround_color))
                    holder.binding.tvTitle.setTextColor(Color.parseColor(ConstantValues.NightModeDefaultTheme.title_color))
                    holder.binding.tvTime.setTextColor(Color.parseColor(ConstantValues.NightModeDefaultTheme.subTitle_color))
                    holder.binding.tvDescription.setTextColor(Color.parseColor(ConstantValues.NightModeDefaultTheme.subTitle_color))
                }else{
                    holder.binding.root.setCardBackgroundColor(Color.parseColor(ConstantValues.BackGroundColor[currentItem.backgroundColor]))
                    holder.binding.tvTitle.setTextColor(Color.parseColor(ConstantValues.titleColor[currentItem.backgroundColor]))
                    holder.binding.tvTime.setTextColor(Color.parseColor(ConstantValues.subTitleColor[currentItem.backgroundColor]))
                    holder.binding.tvDescription.setTextColor(Color.parseColor(ConstantValues.subTitleColor[currentItem.backgroundColor]))

                }
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                holder.binding.root.setCardBackgroundColor(Color.parseColor(ConstantValues.BackGroundColor[currentItem.backgroundColor]))
                holder.binding.tvTitle.setTextColor(Color.parseColor(ConstantValues.titleColor[currentItem.backgroundColor]))
                holder.binding.tvTime.setTextColor(Color.parseColor(ConstantValues.subTitleColor[currentItem.backgroundColor]))
                holder.binding.tvDescription.setTextColor(Color.parseColor(ConstantValues.subTitleColor[currentItem.backgroundColor]))

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                holder.binding.root.setCardBackgroundColor(Color.parseColor(ConstantValues.BackGroundColor[currentItem.backgroundColor]))
                holder.binding.tvTitle.setTextColor(Color.parseColor(ConstantValues.titleColor[currentItem.backgroundColor]))
                holder.binding.tvTime.setTextColor(Color.parseColor(ConstantValues.subTitleColor[currentItem.backgroundColor]))
                holder.binding.tvDescription.setTextColor(Color.parseColor(ConstantValues.subTitleColor[currentItem.backgroundColor]))
            }
        }
         holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(currentItem,holder.binding.noteLl,holder.adapterPosition)
            }
             holder.itemView.animate()
                 .scaleY(1F)
                 .scaleX(1F)
                 .setDuration(200)
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
                            .setDuration(200)
                            .start()
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        holder.itemView.animate()
                            .scaleX(1F)
                            .scaleY(1F)
                            .setDuration(100)
                            .start()
                    }
                    MotionEvent.ACTION_UP -> {
                        holder.itemView.animate()
                            .scaleX(1F)
                            .scaleY(1F)
                            .setDuration(100)
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

