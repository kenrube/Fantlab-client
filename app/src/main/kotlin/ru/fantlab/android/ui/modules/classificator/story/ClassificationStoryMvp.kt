package ru.fantlab.android.ui.modules.classificator.story

import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ru.fantlab.android.data.dao.model.ClassificatorModel
import ru.fantlab.android.ui.base.mvp.BaseMvp

interface ClassificationStoryMvp {

	interface View : BaseMvp.View,
			android.view.View.OnClickListener,
			SwipeRefreshLayout.OnRefreshListener {

		fun onInitViews(classificators: ArrayList<ClassificatorModel>)
	}

	interface Presenter : BaseMvp.Presenter {

		fun onFragmentCreated(bundle: Bundle)
	}
}