package com.stratagile.pnrouter.ui.activity.feedback.module

import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.base.ActivityScope
import com.stratagile.pnrouter.ui.activity.feedback.FeedbackDetailActivity
import com.stratagile.pnrouter.ui.activity.feedback.contract.FeedbackDetailContract
import com.stratagile.pnrouter.ui.activity.feedback.presenter.FeedbackDetailPresenter

import dagger.Module;
import dagger.Provides;

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: The moduele of FeedbackDetailActivity, provide field for FeedbackDetailActivity
 * @date 2020/06/12 09:37:41
 */
@Module
class FeedbackDetailModule (private val mView: FeedbackDetailContract.View) {

    @Provides
    @ActivityScope
    fun provideFeedbackDetailPresenter(httpAPIWrapper: HttpAPIWrapper) :FeedbackDetailPresenter {
        return FeedbackDetailPresenter(httpAPIWrapper, mView)
    }

    @Provides
    @ActivityScope
    fun provideFeedbackDetailActivity() : FeedbackDetailActivity {
        return mView as FeedbackDetailActivity
    }
}