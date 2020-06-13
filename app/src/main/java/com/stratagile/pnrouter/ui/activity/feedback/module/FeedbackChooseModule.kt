package com.stratagile.pnrouter.ui.activity.feedback.module

import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.base.ActivityScope
import com.stratagile.pnrouter.ui.activity.feedback.FeedbackChooseActivity
import com.stratagile.pnrouter.ui.activity.feedback.contract.FeedbackChooseContract
import com.stratagile.pnrouter.ui.activity.feedback.presenter.FeedbackChoosePresenter

import dagger.Module;
import dagger.Provides;

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: The moduele of FeedbackChooseActivity, provide field for FeedbackChooseActivity
 * @date 2020/06/08 17:26:18
 */
@Module
class FeedbackChooseModule (private val mView: FeedbackChooseContract.View) {

    @Provides
    @ActivityScope
    fun provideFeedbackChoosePresenter(httpAPIWrapper: HttpAPIWrapper) :FeedbackChoosePresenter {
        return FeedbackChoosePresenter(httpAPIWrapper, mView)
    }

    @Provides
    @ActivityScope
    fun provideFeedbackChooseActivity() : FeedbackChooseActivity {
        return mView as FeedbackChooseActivity
    }
}