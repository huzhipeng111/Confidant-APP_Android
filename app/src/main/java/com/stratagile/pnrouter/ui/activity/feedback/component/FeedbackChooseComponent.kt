package com.stratagile.pnrouter.ui.activity.feedback.component

import com.stratagile.pnrouter.application.AppComponent
import com.stratagile.pnrouter.ui.activity.base.ActivityScope
import com.stratagile.pnrouter.ui.activity.feedback.FeedbackChooseActivity
import com.stratagile.pnrouter.ui.activity.feedback.module.FeedbackChooseModule

import dagger.Component

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: The component for FeedbackChooseActivity
 * @date 2020/06/08 17:26:18
 */
@ActivityScope
@Component(dependencies = arrayOf(AppComponent::class), modules = arrayOf(FeedbackChooseModule::class))
interface FeedbackChooseComponent {
    fun inject(FeedbackChooseActivity: FeedbackChooseActivity): FeedbackChooseActivity
}