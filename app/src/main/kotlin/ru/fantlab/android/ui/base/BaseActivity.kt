package ru.fantlab.android.ui.base

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.bumptech.glide.Glide
import com.evernote.android.state.State
import com.evernote.android.state.StateSaver
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import net.grandcentrix.thirtyinch.TiActivity
import ru.fantlab.android.App
import ru.fantlab.android.R
import ru.fantlab.android.data.dao.model.AbstractLogin
import ru.fantlab.android.helper.*
import ru.fantlab.android.provider.theme.ThemeEngine
import ru.fantlab.android.ui.base.mvp.BaseMvp
import ru.fantlab.android.ui.base.mvp.presenter.BasePresenter
import ru.fantlab.android.ui.modules.login.LoginActivity
import ru.fantlab.android.ui.modules.main.MainActivity
import ru.fantlab.android.ui.widgets.dialog.MessageDialogView
import ru.fantlab.android.ui.widgets.dialog.ProgressDialogFragment
import java.util.*

/**
 * Created by Kosh on 24 May 2016, 8:48 PM
 */
abstract class BaseActivity<V : BaseMvp.View, P : BasePresenter<V>>
	: TiActivity<P, V>(), BaseMvp.View, NavigationView.OnNavigationItemSelectedListener {

	@JvmField
	@BindView(R.id.toolbar)
	var toolbar: Toolbar? = null

	@JvmField
	@BindView(R.id.appbar)
	var appbar: AppBarLayout? = null

	@JvmField
	@BindView(R.id.drawer)
	var drawer: DrawerLayout? = null

	@JvmField
	@BindView(R.id.extrasNav)
	var extraNav: NavigationView? = null

	@JvmField
	@BindView(R.id.accountsNav)
	var accountsNav: NavigationView? = null

	@State
	var isProgressShowing: Boolean = false

	@State
	var presenterStateBundle = Bundle()

	private var toast: Toast? = null
	private var backPressTimer: Long = 0
	//private var mainNavDrawer: MainNavDrawer? = null

	@LayoutRes
	protected abstract fun layout(): Int

	protected abstract fun isTransparent(): Boolean

	protected abstract fun canBack(): Boolean

	protected abstract fun isSecured(): Boolean

	override fun onCreate(savedInstanceState: Bundle?) {
		setTaskName(null)
		setupTheme()
		AppHelper.updateAppLanguage(this)
		super.onCreate(savedInstanceState)
		if (layout() != 0) {
			setContentView(layout())
			ButterKnife.bind(this)
		}
		if (!validateAuth()) return
		showChangelog()
		initPresenterBundle(savedInstanceState)
		setupToolbarAndStatusBar(toolbar)
		//mainNavDrawer = MainNavDrawer(this, extraNav, accountsNav)
		setupNavigationView()
		setupDrawer()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		StateSaver.saveInstanceState(this, outState)
		presenter.onSaveInstanceState(presenterStateBundle)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (canBack()) {
			if (item.itemId == android.R.id.home) {
				onBackPressed()
				return true
			}
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == BundleConstant.REFRESH_CODE) {
				onThemeChanged()
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
	}

	override fun onBackPressed() {
		if (drawer?.isDrawerOpen(GravityCompat.START) == true) {
			closeDrawer()
		} else {
			val clickTwiceToExit = !PrefGetter.isTwiceBackButtonDisabled()
			superOnBackPressed(clickTwiceToExit)
		}
	}

	private fun superOnBackPressed(didClickTwice: Boolean) {
		if (this is MainActivity) {
			if (didClickTwice) {
				if (canExit()) {
					super.onBackPressed()
				}
			} else {
				super.onBackPressed()
			}
		} else {
			super.onBackPressed()
		}
	}

	private fun canExit(): Boolean {
		if (backPressTimer + 2000 > System.currentTimeMillis()) {
			return true
		} else {
			Toast.makeText(App.instance, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show()
		}
		backPressTimer = System.currentTimeMillis()
		return false
	}

	override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
		if (isOk && bundle != null) {
			val logout = bundle.getBoolean("logout")
			if (logout) {
				onRequireLogin()
			}
		}
	}

	override fun onDialogDismissed() {
	}

	@Optional
	@OnClick(R.id.logout)
	internal fun onLogoutClicked() {
		closeDrawer()
		onLogoutPressed()
	}

	protected fun selectMenuItem(@IdRes id: Int) {
		extraNav?.let {
			with(it.menu.findItem(id)) {
				isCheckable = true
				isChecked = true
			}
		}
	}

	override fun showProgress(resId: Int, cancelable: Boolean) {
		var msg = getString(R.string.in_progress)
		if (resId != 0) {
			msg = getString(resId)
		}
		if (!isProgressShowing && !isFinishing) {
			var fragment = AppHelper.getFragmentByTag(supportFragmentManager, ProgressDialogFragment.TAG)
							as ProgressDialogFragment?
			if (fragment == null) {
				isProgressShowing = true
				fragment = ProgressDialogFragment.newInstance(msg, cancelable)
				fragment.show(supportFragmentManager, ProgressDialogFragment.TAG)
			}
		}
	}

	override fun hideProgress() {
		val fragment = AppHelper.getFragmentByTag(supportFragmentManager, ProgressDialogFragment.TAG)
				as ProgressDialogFragment?
		fragment?.let {
			isProgressShowing = false
			it.dismiss()
		}
	}

	override fun showMessage(titleRes: Int, msgRes: Int) {
		showMessage(getString(titleRes), getString(msgRes))
	}

	override fun showMessage(titleRes: String, msgRes: String) {
		hideProgress()
		toast?.cancel()
		val context = App.instance
		toast = if (titleRes == context.getString(R.string.error))
			Toasty.error(context, msgRes, Toast.LENGTH_LONG)
		else
			Toasty.info(context, msgRes, Toast.LENGTH_LONG)
		toast?.show()
	}

	override fun showErrorMessage(msgRes: String) {
		showMessage(getString(R.string.error), msgRes)
	}

	override fun isLoggedIn(): Boolean {
		return PrefGetter.getToken() != null
		// todo убрать первую строку и раскомментировать вторую после появления инфы об юзере
		//return AbstractLogin.getUser() != null
	}

	override fun onRequireLogin() {
		Toasty.warning(App.instance, getString(R.string.unauthorized_user), Toast.LENGTH_LONG).show()
		val glide = Glide.get(App.instance)
		presenter.manageViewDisposable(Observable.fromCallable<Any> {
			glide.clearDiskCache()
			PrefGetter.setToken(null)
			AbstractLogin.logout()
			true
		}.observe().subscribe({
			glide.clearMemory()
			val intent = Intent(this, LoginActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
			startActivity(intent)
			finishAffinity()
		}))
	}

	override fun onLogoutPressed() {
		MessageDialogView.newInstance(
				bundleTitle = getString(R.string.logout),
				bundleMsg = getString(R.string.confirm_message),
				bundle = Bundler.start()
						.put(BundleConstant.YES_NO_EXTRA, true)
						.put("logout", true)
						.end()
		).show(supportFragmentManager, MessageDialogView.TAG)
	}

	override fun onThemeChanged() {
		if (this is MainActivity) {
			recreate()
		} else {
			val intent = Intent(this, MainActivity::class.java)
			with(intent) {
				putExtras(Bundler.start().put(BundleConstant.YES_NO_EXTRA, true).end())
				addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			}
			startActivity(intent)
			finish()
		}
	}

	override fun onOpenSettings() {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun onOpenUrlInBrowser() {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun onScrollTop(index: Int) {
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		closeDrawer()
		//mainNavDrawer.onMainNavItemClick(item)
		return false

	}

	protected fun setTaskName(name: String?) {
		setTaskDescription(ActivityManager.TaskDescription(name, null, ViewHelper.getPrimaryDarkColor(this)))
	}

	private fun setupTheme() {
		ThemeEngine.apply(this)
	}

	private fun validateAuth(): Boolean {
		if (!isSecured()) {
			if (!isLoggedIn()) {
				onRequireLogin()
				return false
			}
		}
		return true
	}

	private fun showChangelog() {
		if (PrefGetter.showWhatsNew()) {
			// todo показать чейнджлог
			//ChangelogBottomSheetDialog().show(supportFragmentManager, "ChangelogBottomSheetDialog")
		}
	}

	private fun initPresenterBundle(savedInstanceState: Bundle?) {
		if (savedInstanceState != null && !savedInstanceState.isEmpty) {
			StateSaver.restoreInstanceState(this, savedInstanceState)
			presenter.onRestoreInstanceState(presenterStateBundle)
		}
	}

	private fun setupToolbarAndStatusBar(toolbar: Toolbar?) {
		changeStatusBarColor(isTransparent())
		toolbar?.let {
			setSupportActionBar(it)
			if (canBack()) {
				val supportActionBar = supportActionBar
				supportActionBar?.let {
					it.setHomeAsUpIndicator(R.drawable.ic_back)
					it.setDisplayHomeAsUpEnabled(true)
					if (canBack()) {
						getToolbarNavigationIcon(toolbar)?.setOnLongClickListener({
							val intent = Intent(this, MainActivity::class.java)
							startActivity(intent)
							finish()
							true
						})
					}
				}
			}
		}
	}

	protected fun setToolbarIcon(@DrawableRes res: Int) {
		supportActionBar?.let {
			with(it) {
				setHomeAsUpIndicator(res)
				setDisplayHomeAsUpEnabled(true)
			}
		}
	}

	protected fun hideShowShadow(show: Boolean) {
		appbar?.elevation = if (show) resources.getDimension(R.dimen.spacing_micro) else 0.0f
	}

	protected fun changeStatusBarColor(isTransparent: Boolean) {
		if (!isTransparent) {
			window.statusBarColor = ViewHelper.getPrimaryDarkColor(this)
		}
	}

	private fun getToolbarNavigationIcon(toolbar: Toolbar): View? {
		val hadContentDescription = TextUtils.isEmpty(toolbar.navigationContentDescription)
		val contentDescription = if (!hadContentDescription)
			toolbar.navigationContentDescription.toString()
		else
			"navigationIcon"
		toolbar.navigationContentDescription = contentDescription
		val potentialViews = ArrayList<View>()
		toolbar.findViewsWithText(potentialViews, contentDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
		var navIcon: View? = null
		if (potentialViews.size > 0) {
			navIcon = potentialViews[0]
		}
		if (hadContentDescription) toolbar.navigationContentDescription = null
		return navIcon
	}

	protected fun setupNavigationView() {
		extraNav?.setNavigationItemSelectedListener(this)
		//mainNavDrawer.setupViewDrawer()
	}

	private fun setupDrawer() {
		if (this !is MainActivity) {
			if (!PrefGetter.isNavDrawerHintShowed()) {
				drawer?.let {
					it.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
						override fun onPreDraw(): Boolean {
							it.openDrawer(GravityCompat.START)
							it.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
								override fun onDrawerOpened(drawerView: View) {
									super.onDrawerOpened(drawerView)
									drawerView.postDelayed({
										closeDrawer()
										drawer?.removeDrawerListener(this)
									}, 1000)
								}
							})
							it.viewTreeObserver.removeOnPreDrawListener(this)
							return true
						}
					})
				}
			}
		}
	}

	protected fun closeDrawer() {
		drawer?.closeDrawer(GravityCompat.START)
	}

	protected fun onRestartApp() {
		val intent = Intent(this, MainActivity::class.java)
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
		startActivity(intent)
		finishAndRemoveTask()
	}
}