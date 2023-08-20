package com.screwitstudio.ala.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.screwitstudio.ala.R
import com.screwitstudio.ala.data.model.Alarm
import com.screwitstudio.ala.receiver.AlarmReceiver
import com.screwitstudio.ala.ui.main.MainActivity
import com.screwitstudio.ala.ui.screen.BottomSheetFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlarmAdapter(
    private val fragmentManager: FragmentManager
) : ListAdapter<Alarm, AlarmAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarmItem = getItem(position)

        // Use the formatTimestampToTimeString function to format the time
        val formattedTime = formatTimestampToTimeString(alarmItem.timeMillis)

        holder.textViewTime.text = "Time: $formattedTime"
        holder.textViewDays.text = "Days: ${alarmItem.selectedDays.joinToString(", ")}"
        holder.textViewTitle.text = alarmItem.title

        // Remove the previous OnCheckedChangeListener to avoid issues
        holder.switchAlarm.setOnCheckedChangeListener(null)

        // Set the state of the SwitchCompat based on isEnabled
        holder.switchAlarm.isChecked = alarmItem.isEnabled

        // Add an OnCheckedChangeListener to the SwitchCompat
        holder.switchAlarm.setOnCheckedChangeListener { _, isChecked ->
            // Depending on the isChecked state, set or cancel the alarm
            alarmItem.isEnabled = isChecked
            if (isChecked) {
                (holder.itemView.context as? MainActivity)?.setAlarmForSavedData(alarmItem)
            } else {
                (holder.itemView.context as? MainActivity)?.cancelAlarm(alarmItem)
            }
        }

        // Add an OnClickListener to open the BottomSheetFragment
        holder.itemView.setOnClickListener {
            val bottomSheetFragment = BottomSheetFragment.newInstance(alarmItem)
            bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        val switchAlarm: SwitchCompat = itemView.findViewById(R.id.switchAlarm)
        val textViewDays: TextView = itemView.findViewById(R.id.textViewDays)
        val textViewTitle: TextView = itemView.findViewById(R.id.textviewTitle)
    }

    fun formatTimestampToTimeString(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private class DiffCallback : DiffUtil.ItemCallback<Alarm>() {
        override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
            return oldItem == newItem
        }
    }
}

