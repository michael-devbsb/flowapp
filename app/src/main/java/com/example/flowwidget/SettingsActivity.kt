package com.example.flowwidget

import android.app.TimePickerDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

class SettingsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: RoutineAdapter
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var tvMonth: TextView
    private var activeBlockColor: Int = Color.parseColor("#34495e") // Default
    
    private var selectedCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        db = AppDatabase.getDatabase(this)
        tvMonth = findViewById(R.id.tv_current_month) // Inicializa

        updateMonthHeader()
        loadActiveBlockColor()
        setupCalendar()
        setupRoutineList()

        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            showRoutineBottomSheet(null)
        }

        loadBlocks()
    }

    private fun loadActiveBlockColor() {
        lifecycleScope.launch {
            val now = Calendar.getInstance()
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)
            val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)

            val activeBlock = withContext(Dispatchers.IO) {
                db.routineDao().getActiveBlock(timeStr, dayOfWeek, dateStr)
            }
            activeBlock?.let {
                try {
                    activeBlockColor = Color.parseColor(it.colorHex)
                    calendarAdapter.updateActiveColor(activeBlockColor)
                } catch (e: Exception) {}
            }
        }
    }

    private fun updateMonthHeader() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonth.text = sdf.format(selectedCalendar.time).uppercase()
    }
    private fun setupCalendar() {
        val rvCalendar = findViewById<RecyclerView>(R.id.rv_calendar)
        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        
        val days = mutableListOf<CalendarDay>()
        val cal = Calendar.getInstance()

        // Iniciar de um domingo há 1 ano (52 semanas)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        cal.add(Calendar.WEEK_OF_YEAR, -52)
        
        val today = Calendar.getInstance()
        var todayPos = -1

        for (i in 0 until (104 * 7)) { // 2 anos de calendário (104 semanas)
            val day = Calendar.getInstance().apply { time = cal.time }
            val isSelected = isSameDay(day, today)
            if (isSelected) todayPos = i
            days.add(CalendarDay(day, isSelected))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        calendarAdapter = CalendarAdapter(days, activeBlockColor, todayPos) { selectedDay ->
            selectedCalendar = selectedDay
            updateMonthHeader()
            loadBlocks()
        }
        rvCalendar.adapter = calendarAdapter

        // Rolar para a semana de hoje (linha do meio das 3 visíveis)
        val todayRow = todayPos / 7
        rvCalendar.scrollToPosition((todayRow - 1) * 7)

        rvCalendar.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                updateMonthOnScroll(rvCalendar)
            }
        })
    }

    private fun updateMonthOnScroll(rv: RecyclerView) {
        val layoutManager = rv.layoutManager as GridLayoutManager
        val firstPos = layoutManager.findFirstVisibleItemPosition()
        val lastPos = layoutManager.findLastVisibleItemPosition()
        
        if (firstPos == RecyclerView.NO_POSITION || lastPos == RecyclerView.NO_POSITION) return
        
        val monthCounts = mutableMapOf<Int, Int>()
        val yearCounts = mutableMapOf<Int, Int>()
        
        for (i in firstPos..lastPos) {
            val item = calendarAdapter.getItem(i)
            val month = item.calendar.get(Calendar.MONTH)
            val year = item.calendar.get(Calendar.YEAR)
            monthCounts[month] = monthCounts.getOrDefault(month, 0) + 1
            yearCounts[year] = yearCounts.getOrDefault(year, 0) + 1
        }
        
        val topMonth = monthCounts.maxByOrNull { it.value }?.key ?: return
        val topYear = yearCounts.maxByOrNull { it.value }?.key ?: return
        
        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, topMonth)
            set(Calendar.YEAR, topYear)
        }
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonth.text = sdf.format(cal.time).uppercase()
    }

    private fun setupRoutineList() {
        val rvRoutines = findViewById<RecyclerView>(R.id.rv_routines)
        //rvRoutines.layoutManager = LinearLayoutManager(this)
        adapter = RoutineAdapter(
            onEdit = { showRoutineBottomSheet(it) },
            onDelete = { showDeleteConfirmation(it) },
            onClick = { showRoutineDetail(it) }
        )
        rvRoutines.adapter = adapter
    }

    private fun loadBlocks() {
        lifecycleScope.launch {
            val dayOfWeek = selectedCalendar.get(Calendar.DAY_OF_WEEK)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
            
            val blocks = withContext(Dispatchers.IO) {
                db.routineDao().getBlocksForDay(dayOfWeek, dateStr)
            }
            adapter.submitList(blocks.sortedBy { it.startTime })
        }
    }

    private fun showRoutineBottomSheet(block: RoutineBlock?) {
        val sheet = RoutineBottomSheet(block, selectedCalendar) {
            loadBlocks()
        }
        sheet.show(supportFragmentManager, "RoutineSheet")
    }

    private fun showDeleteConfirmation(block: RoutineBlock) {
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("Excluir Bloco")
            .setMessage("Deseja realmente remover '${block.name}'?")
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    db.routineDao().deleteBlock(block)
                    loadBlocks()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showRoutineDetail(block: RoutineBlock) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_routine_detail, null)
        val dialog = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val name = dialogView.findViewById<TextView>(R.id.tv_detail_name)
        val time = dialogView.findViewById<TextView>(R.id.tv_detail_time)
        val info = dialogView.findViewById<TextView>(R.id.tv_detail_info)
        val tasks = dialogView.findViewById<TextView>(R.id.tv_detail_tasks)
        val colorBar = dialogView.findViewById<View>(R.id.view_detail_color)
        val btnClose = dialogView.findViewById<Button>(R.id.btn_close_detail)

        name.text = block.name
        time.text = "${block.startTime} - ${block.endTime}"
        
        val typeStr = if (block.isFixed) "Fixa" else "Pontual"
        val whenStr = if (block.isFixed) {
            val daysMap = mapOf(1 to "Dom", 2 to "Seg", 3 to "Ter", 4 to "Qua", 5 to "Qui", 6 to "Sex", 7 to "Sab")
            block.selectedDays?.split(",")?.mapNotNull { daysMap[it.toInt()] }?.joinToString(", ") ?: ""
        } else {
            block.date ?: ""
        }
        info.text = "$typeStr • $whenStr"
        
        tasks.text = if (block.tasks.isNullOrBlank()) "Nenhuma tarefa listada" else block.tasks
        
        try {
            val color = Color.parseColor(block.colorHex)
            colorBar.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
        } catch (e: Exception) {}

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}

data class CalendarDay(val calendar: Calendar, var isSelected: Boolean = false)

class CalendarAdapter(
    private val days: List<CalendarDay>,
    private var activeColor: Int,
    initialSelectedPos: Int,
    private val onDaySelected: (Calendar) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    private var selectedPos = initialSelectedPos

    fun getItem(position: Int) = days[position]

    fun updateActiveColor(color: Int) {
        activeColor = color
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = days[position]
        holder.bind(item, position)
    }

    override fun getItemCount() = days.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayName = view.findViewById<TextView>(R.id.tv_day_name)
        val tvDayNumber = view.findViewById<TextView>(R.id.tv_day_number)
        val container = view.findViewById<View>(R.id.container_day)

        fun bind(item: CalendarDay, position: Int) {
            val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
            tvDayName.text = sdfDay.format(item.calendar.time).uppercase()
            tvDayNumber.text = item.calendar.get(Calendar.DAY_OF_MONTH).toString()

            val today = Calendar.getInstance()
            val isToday = isSameDay(item.calendar, today)

            // Lógica de Degradê Temporal
            val diffDays = Math.abs((item.calendar.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            
            val alpha = when {
                diffDays <= 3 -> 1.0f
                diffDays <= 10 -> 0.6f
                else -> 0.3f
            }
            
            tvDayName.alpha = alpha
            tvDayNumber.alpha = alpha

            // Visual logic for Today and Selected
            when {
                isToday && item.isSelected -> {
                    // Hoje e Selecionado: Fundo colorido + Borda branca no foreground
                    tvDayNumber.setBackgroundResource(R.drawable.bg_calendar_today)
                    tvDayNumber.backgroundTintList = android.content.res.ColorStateList.valueOf(activeColor)
                    tvDayNumber.foreground = androidx.core.content.ContextCompat.getDrawable(tvDayNumber.context, R.drawable.bg_calendar_selected)
                }
                isToday -> {
                    // Apenas Hoje: Fundo colorido, sem borda no foreground
                    tvDayNumber.setBackgroundResource(R.drawable.bg_calendar_today)
                    tvDayNumber.backgroundTintList = android.content.res.ColorStateList.valueOf(activeColor)
                    tvDayNumber.foreground = null
                }
                item.isSelected -> {
                    // Apenas Selecionado: Fundo transparente, apenas borda no foreground
                    tvDayNumber.setBackgroundColor(Color.TRANSPARENT)
                    tvDayNumber.foreground = androidx.core.content.ContextCompat.getDrawable(tvDayNumber.context, R.drawable.bg_calendar_selected)
                }
                else -> {
                    // Dia comum: Limpa fundo e borda
                    tvDayNumber.setBackgroundColor(Color.TRANSPARENT)
                    tvDayNumber.foreground = null
                }
            }
            // O container agora fica sempre transparente para não interferir
            container.setBackgroundColor(Color.TRANSPARENT)

            container.setOnClickListener {
                val oldPos = selectedPos
                if (oldPos != -1) {
                    days[oldPos].isSelected = false
                    notifyItemChanged(oldPos)
                }
                selectedPos = adapterPosition
                days[selectedPos].isSelected = true
                notifyItemChanged(selectedPos)
                onDaySelected(item.calendar)
            }
        }
    }
}

class RoutineAdapter(
    private val onEdit: (RoutineBlock) -> Unit,
    private val onDelete: (RoutineBlock) -> Unit,
    private val onClick: (RoutineBlock) -> Unit
) : RecyclerView.Adapter<RoutineAdapter.ViewHolder>() {

    private var list = listOf<RoutineBlock>()

    fun submitList(newList: List<RoutineBlock>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount() = list.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tv_routine_name)
        val time = view.findViewById<TextView>(R.id.tv_routine_time)
        val indicator = view.findViewById<View>(R.id.view_color_indicator)
        val btnEdit = view.findViewById<ImageButton>(R.id.btn_edit)
        val btnDelete = view.findViewById<ImageButton>(R.id.btn_delete)

        fun bind(block: RoutineBlock) {
            name.text = block.name + if (!block.isFixed) " (Pontual)" else ""
            time.text = "${block.startTime} - ${block.endTime}"
            try {
                val color = Color.parseColor(block.colorHex)
                indicator.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
            } catch (e: Exception) {
                indicator.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.GRAY)
            }

            btnEdit.setOnClickListener { onEdit(block) }
            btnDelete.setOnClickListener { onDelete(block) }
            itemView.setOnClickListener { onClick(block) }
        }
    }
}

class RoutineBottomSheet(
    private val existingBlock: RoutineBlock?,
    private val selectedDate: Calendar,
    private val onSaved: () -> Unit
) : BottomSheetDialogFragment() {

    private var startTime = "08:00"
    private var endTime = "10:00"
    private var punctualDate = Calendar.getInstance()
    private var selectedColorHex = "#34495e"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_routine, container, false)
        
        val tvTitle = view.findViewById<TextView>(R.id.tv_sheet_title)
        val etName = view.findViewById<TextInputEditText>(R.id.et_name)
        val etTasks = view.findViewById<TextInputEditText>(R.id.et_tasks)
        val btnStart = view.findViewById<Button>(R.id.btn_start_time)
        val btnEnd = view.findViewById<Button>(R.id.btn_end_time)
        val btnDate = view.findViewById<Button>(R.id.btn_date)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val rbFixed = view.findViewById<RadioButton>(R.id.rb_fixed)
        val rbPunctual = view.findViewById<RadioButton>(R.id.rb_punctual)
        val cgDays = view.findViewById<ChipGroup>(R.id.cg_days)
        val rgColors = view.findViewById<RadioGroup>(R.id.rg_color_selector)

        val chips = listOf(
            view.findViewById<Chip>(R.id.chip_dom),
            view.findViewById<Chip>(R.id.chip_seg),
            view.findViewById<Chip>(R.id.chip_ter),
            view.findViewById<Chip>(R.id.chip_qua),
            view.findViewById<Chip>(R.id.chip_qui),
            view.findViewById<Chip>(R.id.chip_sex),
            view.findViewById<Chip>(R.id.chip_sab)
        )

        punctualDate.time = selectedDate.time

        existingBlock?.let {
            tvTitle.text = "Editar Bloco"
            etName.setText(it.name)
            etTasks.setText(it.tasks)
            selectedColorHex = it.colorHex
            startTime = it.startTime
            endTime = it.endTime


            updateUIWithColor(selectedColorHex, rgColors)

            if (it.isFixed) {
                rbFixed.isChecked = true
                cgDays.visibility = View.VISIBLE
                btnDate.visibility = View.GONE
                it.selectedDays?.split(",")?.forEach { day ->
                    val index = day.toInt() - 1
                    if (index in chips.indices) chips[index].isChecked = true
                }
            } else {
                rbPunctual.isChecked = true
                btnDate.visibility = View.VISIBLE
                cgDays.visibility = View.GONE
                it.date?.let { d ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    sdf.parse(d)?.let { date -> punctualDate.time = date }
                }
            }
        } ?: run {
            // New block: default color and selected day
            updateUIWithColor(selectedColorHex, rgColors)
            val currentDayIndex = selectedDate.get(Calendar.DAY_OF_WEEK) - 1
            if (currentDayIndex in chips.indices) chips[currentDayIndex].isChecked = true
        }

        rgColors.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            // Pega a cor que você definiu no backgroundTint do XML
            val colorInt = selectedRadioButton.backgroundTintList?.defaultColor ?: Color.WHITE
            selectedColorHex = String.format("#%06X", (0xFFFFFF and colorInt))
        }

        btnStart.text = "Início: $startTime"
        btnEnd.text = "Fim: $endTime"
        updateDateButton(btnDate)

        rbFixed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cgDays.visibility = View.VISIBLE
                btnDate.visibility = View.GONE
            }
        }

        rbPunctual.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                btnDate.visibility = View.VISIBLE
                cgDays.visibility = View.GONE
            }
        }

        btnStart.setOnClickListener {
            showTimePicker(startTime) { 
                startTime = it
                btnStart.text = "Início: $it"
            }
        }

        btnEnd.setOnClickListener {
            showTimePicker(endTime) {
                endTime = it
                btnEnd.text = "Fim: $it"
            }
        }

        btnDate.setOnClickListener {
            showDatePicker(punctualDate) {
                punctualDate = it
                updateDateButton(btnDate)
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val tasks = etTasks.text.toString()
            val isFixed = rbFixed.isChecked

            val selectedDaysList = mutableListOf<Int>()
            chips.forEachIndexed { index, chip ->
                if (chip.isChecked) selectedDaysList.add(index + 1)
            }

            if (name.isNotEmpty()) {
                val newBlock = RoutineBlock(
                    id = existingBlock?.id ?: 0,
                    name = name,
                    startTime = startTime,
                    endTime = endTime,
                    colorHex = selectedColorHex,
                    tasks = tasks,
                    isFixed = isFixed,
                    selectedDays = if (isFixed) selectedDaysList.joinToString(",") else null,
                    date = if (!isFixed) SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(punctualDate.time) else null
                )
                
                checkConflictAndSave(newBlock, selectedDaysList)
            } else {
                Toast.makeText(context, "Nome é obrigatório", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun updateUIWithColor(hex: String, rgColors: RadioGroup) {
        selectedColorHex = hex
        try {
            val targetColor = Color.parseColor(hex)

            // Percorre todas as bolinhas para encontrar a que tem a cor salva
            for (i in 0 until rgColors.childCount) {
                val rb = rgColors.getChildAt(i) as? RadioButton
                // Se a cor da bolinha no XML for igual à cor salva no banco, ela fica selecionada
                if (rb?.backgroundTintList?.defaultColor == targetColor) {
                    rb.isChecked = true
                    break
                }
            }
        } catch (e: Exception) {
            // Se a cor for inválida, apenas ignora
        }
    }

    private fun updateDateButton(btn: Button) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        btn.text = "Data: ${sdf.format(punctualDate.time)}"
    }

    private fun checkConflictAndSave(newBlock: RoutineBlock, selectedDays: List<Int>) {
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).routineDao()
            
            if (!newBlock.isFixed) {
                val dayOfWeek = punctualDate.get(Calendar.DAY_OF_WEEK)
                val conflicts = withContext(Dispatchers.IO) {
                    dao.getConflictingFixedBlocks(dayOfWeek, newBlock.startTime, newBlock.endTime)
                }
                
                if (conflicts.isNotEmpty()) {
                    AlertDialog.Builder(requireContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
                        .setTitle("Conflito detectado")
                        .setMessage("Deseja priorizar a tarefa pontual sobre a fixa para este dia?")
                        .setPositiveButton("Sim") { _, _ -> save(newBlock) }
                        .setNegativeButton("Não", null)
                        .show()
                } else {
                    save(newBlock)
                }
            } else {
                save(newBlock)
            }
        }
    }

    private fun save(block: RoutineBlock) {
        lifecycleScope.launch {
            AppDatabase.getDatabase(requireContext()).routineDao().insertBlock(block)
            onSaved()
            dismiss()
        }
    }

    private fun showTimePicker(currentTime: String, onTimeSelected: (String) -> Unit) {
        val parts = currentTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        TimePickerDialog(requireContext(), { _, h, m ->
            onTimeSelected(String.format("%02d:%02d", h, m))
        }, hour, minute, true).show()
    }

    private fun showDatePicker(currentDate: Calendar, onDateSelected: (Calendar) -> Unit) {
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, y, m, d ->
            val cal = Calendar.getInstance().apply {
                set(y, m, d)
            }
            onDateSelected(cal)
        }, year, month, day).show()
    }
}
