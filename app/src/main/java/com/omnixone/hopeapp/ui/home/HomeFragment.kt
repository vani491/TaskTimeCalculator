package com.omnixone.hopeapp.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
import org.json.JSONObject


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
        //load random quotes
        loadRandomQuote()
        // Resume timer if a task was running
        taskViewModel.resumeTimerIfRunning()
    }





    private fun loadRandomQuote() {
        try {
            // JSON file read
            val json = resources.openRawResource(R.raw.quotes).bufferedReader().use { it.readText() }

            // Parse करें
            val jsonObject = JSONObject(json)
            val quotesArray = jsonObject.getJSONArray("quotes")

            // Random quote pick
            val randomIndex = (0 until quotesArray.length()).random()
            val randomQuote = quotesArray.getString(randomIndex)


            binding.firstQuote.text = "\"$randomQuote\""

        } catch (e: Exception) {
            binding.firstQuote.text = "\"Push yourself, because no one else is going to do it for you.\""
        }
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
            //Immediately reflect UI change with 0L elapsed
            //taskAdapter.updateSelectedTask(task.uuid.toString(), 0L)
        }

        taskAdapter.setOnEditClickListener { task ->
            showEditTaskDialog(task)
        }

    }

    // Observe tasks
    private fun observeTasks() {
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
        }

        // Observe timer and update selected task
        taskViewModel.elapsedTime.observe(viewLifecycleOwner) { elapsed ->
            val selectedTaskUuid = taskViewModel.getSelectedTaskUuid()
            Log.e("HOPE","4. currentTaskUuid : $selectedTaskUuid")// We'll add this getter
            taskAdapter.updateSelectedTask(selectedTaskUuid, elapsed)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onResume() {
        super.onResume()
        loadRandomQuote()
        taskViewModel.resumeTimerIfRunning()
        taskViewModel.loadTodayDurations {
            taskAdapter.setTodayDurations(taskViewModel.getTodayDurationsMap())
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val indexEditText = dialogView.findViewById<EditText>(R.id.editTextIndex)
        val titleEditText = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val descEditText = dialogView.findViewById<EditText>(R.id.editTextDescription)
        val totalHoursEditText = dialogView.findViewById<EditText>(R.id.editTextTotalHours)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val description = descEditText.text.toString().trim()
                val indexStr = indexEditText.text.toString().trim()
                val totalHoursStr = totalHoursEditText.text.toString().trim()

                if (title.isNotEmpty() && indexStr.isNotEmpty() && totalHoursStr.isNotEmpty()) {
                    val index = indexStr.toIntOrNull() ?: 0
                    val totalHours = totalHoursStr.toIntOrNull() ?: 0

                    val newTask = TaskEntity(
                        index = index,
                        title = title,
                        description = description,
                        totalHours = totalHours
                    )
                    taskViewModel.insertTask(newTask)
                } else {
                    Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    private fun showEditTaskDialog(task: TaskEntity) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_task, null)

        val titleEditText = dialogView.findViewById<EditText>(R.id.etTitle)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.etDescription)
        val indexEditText = dialogView.findViewById<EditText>(R.id.etIndex)
        val totalHoursEditText = dialogView.findViewById<EditText>(R.id.etTotalHours)

        titleEditText.setText(task.title)
        descriptionEditText.setText(task.description)
        indexEditText.setText(task.index.toString())
        totalHoursEditText.setText(task.totalHours.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val index = indexEditText.text.toString().toIntOrNull() ?: 0
                val totalHours = totalHoursEditText.text.toString().toIntOrNull() ?: 0

                if (title.isNotEmpty()) {
                    val updatedTask = task.copy(
                        title = title,
                        description = description,
                        index = index,
                        totalHours = totalHours
                    )
                    taskViewModel.update(updatedTask)
                } else {
                    Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Delete") { _, _ ->
                taskViewModel.delete(task)
                Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
            }
            .show()
    }



}