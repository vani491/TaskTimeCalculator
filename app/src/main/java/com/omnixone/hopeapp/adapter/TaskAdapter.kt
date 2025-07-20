package com.omnixone.hopeapp.adapter

import android.graphics.Color
import android.text.format.DateUtils.formatElapsedTime
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omnixone.hopeapp.R
import com.omnixone.hopeapp.databinding.ItemTaskBinding
import com.omnixone.hopeapp.db.entity.TaskEntity
import java.util.concurrent.TimeUnit


class TaskAdapter : ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder>(DiffCallback()) {

    private var onEditClick: ((TaskEntity) -> Unit)? = null
    private var onDeleteClick: ((TaskEntity) -> Unit)? = null
    private var onTaskClick: ((TaskEntity) -> Unit)? = null

    // This value will now represent only the live timer (start from 0)
    private var runningTaskUuid: String? = null
    private var runningTaskElapsed: Long = 0L


    private var taskTodayDurations: Map<Int, Long> = emptyMap()

    fun setOnEditClickListener(listener: (TaskEntity) -> Unit) {
        onEditClick = listener
    }

    fun setOnDeleteClickListener(listener: (TaskEntity) -> Unit) {
        onDeleteClick = listener
    }

    fun setOnTaskClickListener(listener: (TaskEntity) -> Unit) {
        onTaskClick = listener
    }

    //Update selected task and elapsed time
    fun updateSelectedTask(uuid: String?, elapsed: Long) {
        runningTaskUuid = uuid
        runningTaskElapsed = elapsed
        notifyDataSetChanged()
    }


    fun setTodayDurations(durations: Map<Int, Long>) {
        taskTodayDurations = durations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TaskEntity) {
            binding.taskTitle.text = task.title
            binding.taskDescription.text = task.description

            val context = binding.root.context
            val isRunning = task.uuid.toString() == runningTaskUuid
            //Log.e("WANT", "Is Running : $isRunning runningTaskUuid : $runningTaskUuid")

           /* // Set background
            binding.root.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (isRunning) R.color.edit_button_text else R.color.white
                )
            )*/

            // Set background using drawable
            binding.root.setBackgroundResource(
                if (isRunning) R.drawable.active_card_background else R.drawable.non_active_background
            )

            // Always show total time
            val total = taskTodayDurations[task.uuid] ?: 0L
            binding.tvTotalTimer.text = "${formatElapsedTime(total)}"

            // Show running time if selected
            if (isRunning) {
                binding.tvTimer.visibility = View.VISIBLE
                binding.tvTimer.text = "${formatElapsedTime(runningTaskElapsed)}"

                binding.taskTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                binding.tvTotalTimer.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                binding.tvTimer.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))


            } else {
                binding.tvTimer.visibility = View.INVISIBLE
                binding.taskTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.secondary_text))
                binding.tvTotalTimer.setTextColor(ContextCompat.getColor(binding.root.context, R.color.secondary_text))
                binding.tvTimer.setTextColor(ContextCompat.getColor(binding.root.context, R.color.secondary_text))


            }



            binding.root.setOnClickListener {
                onTaskClick?.invoke(task)
            }

            binding.root.setOnLongClickListener {
                onEditClick?.invoke(task)
                true
            }


        }


        // ⏱ Format milliseconds to HH:mm:ss
        private fun formatElapsedTime(millis: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(millis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
        override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity) =
            oldItem.uuid == newItem.uuid

        override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity) =
            oldItem == newItem
    }
}








