// DashboardFragment.kt
package com.omnixone.hopeapp.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.omnixone.hopeapp.R
import com.omnixone.hopeapp.TaskApp

import com.omnixone.hopeapp.databinding.FragmentDashboardBinding
import com.omnixone.hopeapp.db.AppDatabase
import com.omnixone.hopeapp.repository.TaskSessionRepository
import com.omnixone.hopeapp.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DashboardFragment : Fragment() {


    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DashboardViewModel



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = (requireActivity().application as TaskApp).database.taskSessionDao()
        val taskDao = (requireActivity().application as TaskApp).database.taskDao()
        val repository = TaskSessionRepository(dao, taskDao)
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(repository) as T
            }
        }

        viewModel = ViewModelProvider(this,viewModelFactory)[DashboardViewModel::class.java]

        viewModel.loadDashboard(getTodayDate())
        observeData()
        barChartObserver()
        pieChartObserver()

    }

    private fun pieChartObserver() {



        viewModel.pieChartData.observe(viewLifecycleOwner) { dataList ->
            val pieEntries = ArrayList<PieEntry>()

            dataList.forEach {
                pieEntries.add(PieEntry(it.second, it.first))
            }

            val pieDataSet = PieDataSet(pieEntries, "")
            pieDataSet.colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.purple_200),
                ContextCompat.getColor(requireContext(), R.color.purple_500),
                ContextCompat.getColor(requireContext(), R.color.teal_200),
                ContextCompat.getColor(requireContext(), R.color.teal_700)
            )

            pieDataSet.valueTextSize = 12f
            pieDataSet.valueTextColor = Color.BLACK

            binding.pieChart.apply {
                data = PieData(pieDataSet)
                setUsePercentValues(true)
                isDrawHoleEnabled = true
                setHoleColor(Color.TRANSPARENT)
                setCenterText("Task Share")
                setCenterTextSize(16f)
                description.isEnabled = false
                legend.isEnabled = true
                animateY(1000, Easing.EaseInOutQuad)
                invalidate()
            }
        }

    }

    private fun barChartObserver() {
        viewModel.barChartData.observe(viewLifecycleOwner) { taskList ->
            val entries = ArrayList<BarEntry>()
            val labels = ArrayList<String>()

            taskList.forEachIndexed { index, pair ->
                entries.add(BarEntry(index.toFloat(), pair.second))
                labels.add(pair.first)
            }

            val dataSet = BarDataSet(entries, "Hours Spent")
            dataSet.color = ContextCompat.getColor(requireContext(), R.color.teal_700)
            val barData = BarData(dataSet)

            with(binding.barChart) {
                data = barData
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                axisLeft.axisMinimum = 0f
                axisRight.isEnabled = false
                description.isEnabled = false
                legend.isEnabled = true
                animateY(1000)
                invalidate()
            }
        }

    }

    private fun observeData() {
        // Text Data Observers
        viewModel.totalTime.observe(viewLifecycleOwner) {
            binding.tvTotalTime.text = it
        }

        viewModel.totalSessions.observe(viewLifecycleOwner) {
            binding.tvTotalSessions.text = it.toString()
        }

        viewModel.totalTasks.observe(viewLifecycleOwner) {
            binding.tvTasksWorkedOn.text = it.toString()
        }

        viewModel.topTask.observe(viewLifecycleOwner) {
            binding.tvMostProductiveTask.text = it
        }

    }


    private fun setUpBarChart() {
        // Step 2: Bar Chart Setup
        val barEntries = ArrayList<BarEntry>()
        val taskLabels = listOf("Design", "Development", "Testing", "Meeting")

        // Add bar entries (x = index, y = hours)
        barEntries.add(BarEntry(0f, 2.0f))
        barEntries.add(BarEntry(1f, 4.5f))
        barEntries.add(BarEntry(2f, 1.0f))
        barEntries.add(BarEntry(3f, 0.5f))

        val barDataSet = BarDataSet(barEntries, "Hours Spent")
        barDataSet.color = ContextCompat.getColor(requireContext(), R.color.teal_700)
        barDataSet.valueTextSize = 12f

        val data = BarData(barDataSet)
        binding.barChart.data = data

         // Axis formatting
        val xAxis = binding.barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(taskLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.labelRotationAngle = -15f

        binding.barChart.axisLeft.axisMinimum = 0f
        binding.barChart.axisRight.isEnabled = false
        binding.barChart.description.isEnabled = false
        binding.barChart.legend.isEnabled = true

        binding.barChart.animateY(1000)
        binding.barChart.invalidate()

    }

    private fun setUpPieChart() {
        // Step 3: Pie Chart Setup
        val pieEntries = ArrayList<PieEntry>()
        pieEntries.add(PieEntry(20f, "Design"))
        pieEntries.add(PieEntry(50f, "Development"))
        pieEntries.add(PieEntry(20f, "Testing"))
        pieEntries.add(PieEntry(10f, "Meeting"))

        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.purple_200),
            ContextCompat.getColor(requireContext(), R.color.purple_500),
            ContextCompat.getColor(requireContext(), R.color.teal_200),
            ContextCompat.getColor(requireContext(), R.color.teal_700)
        )

        pieDataSet.valueTextSize = 12f
        pieDataSet.valueTextColor = Color.BLACK

        val pieData = PieData(pieDataSet)

        binding.pieChart.data = pieData
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.isDrawHoleEnabled = true
        binding.pieChart.setHoleColor(Color.TRANSPARENT)
        binding.pieChart.setTransparentCircleAlpha(110)
        binding.pieChart.setCenterText("Task Share")
        binding.pieChart.setCenterTextSize(16f)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.legend.isEnabled = true

        binding.pieChart.animateY(1000, Easing.EaseInOutQuad)
        binding.pieChart.invalidate()

    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }


}