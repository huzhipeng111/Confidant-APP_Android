package com.stratagile.pnrouter.ui.activity.main.module

import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.base.ActivityScope
import com.stratagile.pnrouter.ui.activity.main.FeedBackActivity
import com.stratagile.pnrouter.ui.activity.main.contract.FeedBackContract
import com.stratagile.pnrouter.ui.activity.main.presenter.FeedBackPresenter

import dagger.Module;
import dagger.Provides;

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.main
 * @Description: The moduele of FeedBackActivity, provide field for FeedBackActivity
 * @date 2020/06/08 15:27:19
 */
@Module
class FeedBackModule (private val mView: FeedBackContract.View) {

    @Provides
    @ActivityScope
    fun provideFeedBackPresenter(httpAPIWrapper: HttpAPIWrapper) :FeedBackPresenter {
        return FeedBackPresenter(httpAPIWrapper, mView)
    }

    @Provides
    @ActivityScope
    fun provideFeedBackActivity() : FeedBackActivity {
        return mView as FeedBackActivity
    }
}