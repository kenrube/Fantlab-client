package ru.fantlab.android.ui.adapter.viewholder

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.classif_parent_row_item.view.*
import ru.fantlab.android.R
import ru.fantlab.android.data.dao.model.Classificator
import ru.fantlab.android.ui.widgets.treeview.TreeNode
import ru.fantlab.android.ui.widgets.treeview.TreeViewAdapter
import ru.fantlab.android.ui.widgets.treeview.TreeViewBinder

class ClassificatorViewHolder : TreeViewBinder<ClassificatorViewHolder.ViewHolder>() {

	override val layoutId = R.layout.classif_parent_row_item

	private val selected = mutableListOf<Int>()

	override fun provideViewHolder(itemView: View): ViewHolder {
		return ViewHolder(itemView)
	}

	private fun changeSelected(isChecked: Boolean, id: Int) {
		if (isChecked) {
			selected.add(id)
		} else {
			selected.remove(id)
		}
	}

	override fun bindView(
			holder: RecyclerView.ViewHolder,
			position: Int,
			node: TreeNode<*>,
			onTreeNodeListener: TreeViewAdapter.OnTreeNodeListener?
	) {
		(holder as ClassificatorViewHolder.ViewHolder).arrow.rotation = 0f
		holder.arrow.setImageResource(R.drawable.ic_arrow_right)
		val rotateDegree = if (node.isExpand) 90f else 0f
		holder.arrow.rotation = rotateDegree
		val nodr = node.content as Classificator?
		val checked = selected.contains(nodr?.id)
		holder.name.text = nodr!!.name
		if (!nodr.description.isNullOrEmpty()) {
			holder.description.text = nodr.description
			holder.description.visibility = View.VISIBLE
		}

		if (node.isLeaf)
			holder.arrow.visibility = View.INVISIBLE
		else
			holder.arrow.visibility = View.VISIBLE
		holder.checkbox.setOnCheckedChangeListener(null)

		holder.checkbox.tag = nodr.id
		holder.checkbox.isChecked = checked

		holder.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
			val id = buttonView.tag as Int
			changeSelected(isChecked, id)
			onTreeNodeListener?.onSelected(nodr.id, isChecked)
		}
	}

	class ViewHolder(rootView: View) : TreeViewBinder.ViewHolder(rootView) {
		val arrow: ImageView = rootView.iv_arrow
		var name: TextView = rootView.name
		var description: TextView = rootView.description
		var checkbox: CheckBox = rootView.checkbox
	}
}
