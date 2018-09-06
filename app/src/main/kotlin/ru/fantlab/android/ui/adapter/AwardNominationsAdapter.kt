package ru.fantlab.android.ui.adapter

import android.view.ViewGroup
import ru.fantlab.android.data.dao.model.AuthorInList
import ru.fantlab.android.data.dao.model.Award
import ru.fantlab.android.ui.adapter.viewholder.AuthorsViewHolder
import ru.fantlab.android.ui.adapter.viewholder.AwardNominationViewHolder
import ru.fantlab.android.ui.widgets.recyclerview.BaseRecyclerAdapter
import ru.fantlab.android.ui.widgets.recyclerview.BaseViewHolder
import java.util.*

class AwardNominationsAdapter constructor(authors: ArrayList<Award.Nominations>)
	: BaseRecyclerAdapter<Award.Nominations, AwardNominationViewHolder, BaseViewHolder.OnItemClickListener<Award.Nominations>>(authors) {

	override fun viewHolder(parent: ViewGroup, viewType: Int): AwardNominationViewHolder
			= AwardNominationViewHolder.newInstance(parent, this)

	override fun onBindView(holder: AwardNominationViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
}