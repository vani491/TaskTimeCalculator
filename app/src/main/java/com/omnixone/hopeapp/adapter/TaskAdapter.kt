package com.omnixone.hopeapp.adapter

import android.text.format.DateUtils.formatElapsedTime
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

    //Track selected task and elapsed time
    private var selectedTaskUuid: String? = null
    private var elapsedTime: Long = 0L

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
        selectedTaskUuid = uuid
        elapsedTime = elapsed
        notifyDataSetChanged() // refresh list
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

            // Show time for selected task or today's total
            val context = binding.root.context
            val taskUuid = task.uuid.toString()

            // Show timer only for selected task
            if (taskUuid == selectedTaskUuid) {
                // Timer running for this task
                binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.edit_button))
                binding.tvTimer.text = formatElapsedTime(elapsedTime)
            } else {
                // Show today's total time
                binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                binding.tvTimer.text = formatElapsedTime(taskTodayDurations[task.uuid] ?: 0L)
            }

            binding.tvTimer.visibility = View.VISIBLE

            binding.root.setOnClickListener {
                onTaskClick?.invoke(task)
            }

            binding.btnEdit.setOnClickListener {
                onEditClick?.invoke(task)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick?.invoke(task)
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








