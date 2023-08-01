package com.hellow.noteslite.adaptor

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hellow.noteslite.databinding.ThemeListItemBinding
import com.hellow.noteslite.model.ThemeItem
import timber.log.Timber
import java.security.AccessController.getContext


open class ThemeAdaptor: RecyclerView.Adapter<ThemeAdaptor.ThemeViewHolder>(){

    inner class ThemeViewHolder(val binding: ThemeListItemBinding): RecyclerView.ViewHolder(binding.root){
    }
     var currentSelected:Int = 0
    private val differCallBack = object : DiffUtil.ItemCallback<ThemeItem>(){


        override fun areContentsTheSame(oldItem: ThemeItem, newItem: ThemeItem): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: ThemeItem, newItem: ThemeItem): Boolean {
           return oldItem.title_color == newItem.title_color
        }
    }
    val differ = AsyncListDiffer(this,differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {

        val binding: ThemeListItemBinding = ThemeListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ThemeViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val currentItem =  differ.currentList[position]
          // set the data on and changes per item
        Timber.i("theme_value data set")
        holder.itemView.setOnClickListener {
            currentSelected = position
            onItemClickListener?.let {
                it(position)
            }
        }

        holder.binding.ivThemeItem.setBackgroundColor(Color.parseColor(currentItem.backGround_color))
         

        if(position == currentSelected){
            val scale:Float = holder.itemView.context.resources.displayMetrics.density

            holder.binding.root.strokeWidth = (10 * scale + 0.5f).toInt()
        }else{
            holder.binding.root.strokeWidth = 0
        }
    }

    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }
}