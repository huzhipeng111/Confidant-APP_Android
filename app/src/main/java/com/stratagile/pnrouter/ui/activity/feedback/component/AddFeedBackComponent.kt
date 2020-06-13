package com.stratagile.pnrouter.ui.activity.feedback.component

import com.stratagile.pnrouter.application.AppComponent
import com.stratagile.pnrouter.ui.activity.base.ActivityScope
import com.stratagile.pnrouter.ui.activity.feedback.AddFeedBackActivity
import com.stratagile.pnrouter.ui.activity.feedback.module.AddFeedBackModule

import dagger.Component

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: The component for AddFeedBackActivity
 * @date 2020/06/08 15:57:42
 */
@ActivityScope
@Component(dependencies = arrayOf(AppComponent::class), modules = arrayOf(AddFeedBackModule::class))
interface AddFeedBackComponent {
    fun inject(AddFeedBackActivity: AddFeedBackActivity): AddFeedBackActivity
}