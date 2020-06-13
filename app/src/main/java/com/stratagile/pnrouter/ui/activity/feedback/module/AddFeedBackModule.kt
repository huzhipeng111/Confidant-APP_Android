package com.stratagile.pnrouter.ui.activity.feedback.module

import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.base.ActivityScope
import com.stratagile.pnrouter.ui.activity.feedback.AddFeedBackActivity
import com.stratagile.pnrouter.ui.activity.feedback.contract.AddFeedBackContract
import com.stratagile.pnrouter.ui.activity.feedback.presenter.AddFeedBackPresenter

import dagger.Module;
import dagger.Provides;

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.feedback
 * @Description: The moduele of AddFeedBackActivity, provide field for AddFeedBackActivity
 * @date 2020/06/08 15:57:42
 */
@Module
class AddFeedBackModule (private val mView: AddFeedBackContract.View) {

    @Provides
    @ActivityScope
    fun provideAddFeedBackPresenter(httpAPIWrapper: HttpAPIWrapper) :AddFeedBackPresenter {
        return AddFeedBackPresenter(httpAPIWrapper, mView)
    }

    @Provides
    @ActivityScope
    fun provideAddFeedBackActivity() : AddFeedBackActivity {
        return mView as AddFeedBackActivity
    }
}