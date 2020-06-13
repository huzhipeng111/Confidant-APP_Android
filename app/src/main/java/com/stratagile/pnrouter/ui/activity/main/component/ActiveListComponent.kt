package com.stratagile.pnrouter.ui.activity.main.component

import com.stratagile.pnrouter.application.AppComponent
import com.stratagile.pnrouter.ui.activity.base.ActivityScope
import com.stratagile.pnrouter.ui.activity.main.ActiveListActivity
import com.stratagile.pnrouter.ui.activity.main.module.ActiveListModule

import dagger.Component

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.main
 * @Description: The component for ActiveListActivity
 * @date 2020/05/19 18:03:37
 */
@ActivityScope
@Component(dependencies = arrayOf(AppComponent::class), modules = arrayOf(ActiveListModule::class))
interface ActiveListComponent {
    fun inject(ActiveListActivity: ActiveListActivity): ActiveListActivity
}