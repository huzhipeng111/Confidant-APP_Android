package com.stratagile.pnrouter.ui.activity.main.component

import com.stratagile.pnrouter.application.AppComponent
import com.stratagile.pnrouter.ui.activity.base.ActivityScope
import com.stratagile.pnrouter.ui.activity.main.FeedBackActivity
import com.stratagile.pnrouter.ui.activity.main.module.FeedBackModule

import dagger.Component

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.main
 * @Description: The component for FeedBackActivity
 * @date 2020/06/08 15:27:19
 */
@ActivityScope
@Component(dependencies = arrayOf(AppComponent::class), modules = arrayOf(FeedBackModule::class))
interface FeedBackComponent {
    fun inject(FeedBackActivity: FeedBackActivity): FeedBackActivity
}