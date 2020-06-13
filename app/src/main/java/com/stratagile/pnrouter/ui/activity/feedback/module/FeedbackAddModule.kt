package com.stratagile.pnrouter.ui.activity.feedback.module

import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.base.ActivityScope
import com.stratagile.pnrouter.ui.activity.feedback.FeedbackAddActivity
import com.stratagile.pnrouter.ui.activity.feedback.contract.FeedbackAddContract
import com.stratagile.pnrouter.ui.activity.feedback.presenter.FeedbackAddPresenter

import dagger.Module;
import dagger.Provides;

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: The moduele of FeedbackAddActivity, provide field for FeedbackAddActivity
 * @date 2020/06/12 12:54:37
 */
@Module
class FeedbackAddModule (private val mView: FeedbackAddContract.View) {

    @Provides
    @ActivityScope
    fun provideFeedbackAddPresenter(httpAPIWrapper: HttpAPIWrapper) :FeedbackAddPresenter {
        return FeedbackAddPresenter(httpAPIWrapper, mView)
    }

    @Provides
    @ActivityScope
    fun provideFeedbackAddActivity() : FeedbackAddActivity {
        return mView as FeedbackAddActivity
    }
}