package com.stratagile.pnrouter.ui.activity.feedback.component

import com.stratagile.pnrouter.application.AppComponent
import com.stratagile.pnrouter.ui.activity.base.ActivityScope
import com.stratagile.pnrouter.ui.activity.feedback.FeedbackDetailActivity
import com.stratagile.pnrouter.ui.activity.feedback.module.FeedbackDetailModule

import dagger.Component

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: The component for FeedbackDetailActivity
 * @date 2020/06/12 09:37:41
 */
@ActivityScope
@Component(dependencies = arrayOf(AppComponent::class), modules = arrayOf(FeedbackDetailModule::class))
interface FeedbackDetailComponent {
    fun inject(FeedbackDetailActivity: FeedbackDetailActivity): FeedbackDetailActivity
}