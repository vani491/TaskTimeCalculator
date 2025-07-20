package com.omnixone.hopeapp.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.omnixone.hopeapp.databinding.FragmentHomeBinding
import com.omnixone.hopeapp.viewmodel.TaskViewModel
import android.widget.Toast



import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.omnixone.hopeapp.R
import com.omnixone.hopeapp.TaskApp
import com.omnixone.hopeapp.adapter.TaskAdapter
import com.omnixone.hopeapp.db.entity.TaskEntity
import com.omnixone.hopeapp.repository.TaskRepository
import com.omnixone.hopeapp.viewmodel.TaskViewModelFactory


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskAdapter: TaskAdapter

    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(
            TaskRepository((requireActivity().application as TaskApp).database.taskDao(),(requireActivity().application as TaskApp).database.taskSessionDao() ),
            requireActivity().application
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeTasks()
        setupClickListeners()

        // Resume timer if a task was running
        taskViewModel.resumeTimerIfRunning()


    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter()
        binding.recyclerView.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

    }




    private fun setupClickListeners() {

        binding.addTask.setOnClickListener {
            showAddTaskDialog()
        }

        taskAdapter.setOnTaskClickListener { task ->
            taskViewModel.onTaskSelected(task)
        }

        taskAdapter.setOnEditClickListener { task ->
            showEditTaskDialog(task)
        }

        taskAdapter.setOnDeleteClickListener { task ->
            showDeleteConfirmationDialog(task)
        }
    }

    // Observe tasks
    private fun observeTasks() {
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
        }

        // Observe timer and update selected task
        taskViewModel.elapsedTime.observe(viewLifecycleOwner) { elapsed ->
            val selectedTaskUuid = taskViewModel.getSelectedTaskUuid() // We'll add this getter
            taskAdapter.updateSelectedTask(selectedTaskUuid, elapsed)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onResume() {
        super.onResume()
        taskViewModel.resumeTimerIfRunning()
        taskViewModel.loadTodayDurations {
            taskAdapter.setTodayDurations(taskViewModel.getTodayDurationsMap())
        }
    }





    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val descEditText = dialogView.findViewById<EditText>(R.id.editTextDescription)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val description = descEditText.text.toString().trim()

                if (title.isNotEmpty()) {
                    val newTask = TaskEntity(index = 1,title = title, description = description)
                    taskViewModel.insertTask(newTask)
                } else {

                    Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showEditTaskDialog(task: TaskEntity) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_task, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.etTitle)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.etDescription)

        titleEditText.setText(task.title)
        descriptionEditText.setText(task.description)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()

                if (title.isNotEmpty()) {
                    val updatedTask = task.copy(
                        title = title,
                        description = description
                    )
                    taskViewModel.update(updatedTask)
                } else {
                    Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(task: TaskEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                taskViewModel.delete(task)
                Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


}