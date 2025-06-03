package com.example.cs3130_bonusassignment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cs3130_bonusassignment.model.Element

class ListAdapter(
    private val items: List<Element>,
    private val category: String,
    private val onItemClick: (Element) -> Unit
) : RecyclerView.Adapter<ListAdapter.PoiViewHolder>() {

    inner class PoiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.poi_name)
        val iconView: ImageView = view.findViewById(R.id.poi_icon)
        val subtitleText: TextView = view.findViewById(R.id.poi_subtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_design, parent, false)
        return PoiViewHolder(view)
    }

    override fun onBindViewHolder(holder: PoiViewHolder, position: Int) {

        val item = items[position]
        val type = item.tags?.amenity ?: item.tags?.shop ?: item.tags?.leisure ?: "Unknown type"
        val hours = item.tags?.opening_hours ?: "No hours listed"

        holder.nameText.text = item.tags?.name ?: "Unnamed Place"
        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.subtitleText.text = "Type: $type\nHours: $hours"

        // ADDING ICONS BASED ON CATEGORY
        val iconRes = when (category) {
            "restaurant" -> R.drawable.ic_restaurant
            "fuel" -> R.drawable.ic_gas_station
            "car_repair" -> R.drawable.ic_garage
            "park" -> R.drawable.ic_park
            "supermarket" -> R.drawable.ic_supermarket
            else -> R.drawable.ic_launcher_foreground
        }
        holder.iconView.setImageResource(iconRes)
    }
    override fun getItemCount(): Int = items.size
}