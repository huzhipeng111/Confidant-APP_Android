package com.stratagile.pnrouter.ui.activity.feedback.component

import com.stratagile.pnrouter.application.AppComponent
import com.stratagile.pnrouter.ui.activity.base.ActivityScope
import com.stratagile.pnrouter.ui.activity.feedback.FeedbackAddActivity
import com.stratagile.pnrouter.ui.activity.feedback.module.FeedbackAddModule

import dagger.Component

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: The component for FeedbackAddActivity
 * @date 2020/06/12 12:54:37
 */
@ActivityScope
@Component(dependencies = arrayOf(AppComponent::class), modules = arrayOf(FeedbackAddModule::class))
interface FeedbackAddComponent {
    fun inject(FeedbackAddActivity: FeedbackAddActivity): FeedbackAddActivity
}