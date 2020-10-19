package com.example.test14

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recordpopup.view.*


class IdeaAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<IdeaAdapter.IdeaViewHolder>() {
    var context = context;
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var ideaObjects = emptyList<IdeaObject>() // Cached copy of words

    inner class IdeaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ideaItemView: TextView = itemView.findViewById(R.id.textView)
        val ideaItemImage: ImageView = itemView.findViewById(R.id.imageView)
        val completed: ImageView = itemView.findViewById(R.id.completed)
        val view: View = itemView
        var recorded: ImageView = itemView.findViewById(R.id.audio)
        var id: Int = 0
        var path: String? = null;
        init{
            itemView.setOnClickListener{
                if(path != null)
                {
                    (context as MainActivity).playAudio(path)
                }
            }
            itemView.setOnLongClickListener {
                (context as MainActivity).updateActivity(id)
                return@setOnLongClickListener true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdeaViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return IdeaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: IdeaViewHolder, position: Int) {
        var current = ideaObjects[position]
        holder.id = current.id;
        holder.ideaItemView.text = current.text

        if(current.img == "null")
        {
            val rnds = (0..11).random()
            if (rnds < 10/3)
            {
                holder.view.setBackgroundColor(Color.parseColor("#40c1de"))
            }
            else if (rnds < 2*10/3)
            {
                holder.view.setBackgroundColor(Color.parseColor("#f5a356"))
            }
            else
            {
                holder.view.setBackgroundColor(Color.parseColor("#71d957"))
            }

        }
        else{
            Glide
                .with(context)
                .load(current.img)
                .placeholder(R.drawable.buffer_image)
                .override(600, 400)
                .centerCrop()
                .into(holder.ideaItemImage);
        }
        holder.completed.setOnClickListener {
            current.completed = !current.completed;
            (context as MainActivity).updateIdea(current)
            if(current.completed)
            {
                holder.completed.setColorFilter(Color.rgb(0, 255, 0), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
            else
            {
                holder.completed.clearColorFilter();
            }
        }
        if(current.completed)
        {
            holder.completed.setColorFilter(Color.rgb(0, 255, 0), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        else
        {
            holder.completed.clearColorFilter();
        }
        if (current.rcd != null)
        {
            holder.recorded.visibility = View.VISIBLE;
            holder.path = current.rcd;
        }
        else
        {
            holder.recorded.visibility = View.INVISIBLE;
        }

    }

    internal fun setWords(ideas: List<IdeaObject>) {
        this.ideaObjects = ideas
        notifyDataSetChanged()
    }

    override fun getItemCount() = ideaObjects.size
}