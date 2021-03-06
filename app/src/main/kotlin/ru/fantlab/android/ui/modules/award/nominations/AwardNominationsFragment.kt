package ru.fantlab.android.ui.modules.award.nominations

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import kotlinx.android.synthetic.main.micro_grid_refresh_list.*
import kotlinx.android.synthetic.main.state_layout.*
import ru.fantlab.android.R
import ru.fantlab.android.data.dao.model.Award
import ru.fantlab.android.helper.BundleConstant
import ru.fantlab.android.helper.Bundler
import ru.fantlab.android.ui.adapter.AwardNominationsAdapter
import ru.fantlab.android.ui.base.BaseFragment
import ru.fantlab.android.ui.modules.award.AwardPagerMvp

class AwardNominationsFragment : BaseFragment<AwardNominationsMvp.View, AwardNominationsPresenter>(),
		AwardNominationsMvp.View {

	override fun fragmentLayout() = R.layout.micro_grid_refresh_list

	private val adapter: AwardNominationsAdapter by lazy { AwardNominationsAdapter(arrayListOf()) }
	private var countCallback: AwardPagerMvp.View? = null

	override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
		stateLayout.setEmptyText(R.string.no_nominations)
		stateLayout.setOnReloadListener(this)
		refresh.setOnRefreshListener(this)
		recycler.setEmptyView(stateLayout, refresh)
		recycler.addDivider()
		adapter.listener = presenter
		recycler.adapter = adapter
		presenter.onFragmentCreated(arguments!!)
	}

	override fun providePresenter() = AwardNominationsPresenter()

	override fun showProgress(@StringRes resId: Int, cancelable: Boolean) {
		refresh.isRefreshing = true
		stateLayout.showProgress()
	}

	override fun hideProgress() {
		refresh.isRefreshing = false
		stateLayout.showReload(recycler.adapter?.itemCount ?: 0)
	}

	override fun showErrorMessage(msgRes: String?) {
		hideProgress()
		super.showErrorMessage(msgRes)
	}

	override fun showMessage(titleRes: Int, msgRes: Int) {
		hideProgress()
		super.showMessage(titleRes, msgRes)
	}

	companion object {

		fun newInstance(awardId: Int): AwardNominationsFragment {
			val view = AwardNominationsFragment()
			view.arguments = Bundler.start().put(BundleConstant.EXTRA, awardId).end()
			return view
		}
	}

	override fun onRefresh() {
		presenter.getNominations(true)
	}

	override fun onClick(v: View?) {
		onRefresh()
	}

	override fun onNotifyAdapter(items: List<Award.Nominations>?) {
		hideProgress()
		if (!items.isNullOrEmpty()) {
			adapter.insertItems(items)
		} else {
			adapter.clear()
			stateLayout.showEmptyState()
			return
		}
		fastScroller.attachRecyclerView(recycler)
	}

	override fun onSetTabCount(allCount: Int) {
		countCallback?.onSetBadge(2, allCount)
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		if (context is AwardPagerMvp.View) {
			countCallback = context
		}
	}

	override fun onDetach() {
		countCallback = null
		super.onDetach()
	}

	override fun onItemClicked(item: Award.Nominations) {
	}
}