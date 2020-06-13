package com.stratagile.pnrouter.ui.activity.main.module

import com.stratagile.pnrouter.data.api.HttpAPIWrapper
import com.stratagile.pnrouter.ui.activity.base.ActivityScope
import com.stratagile.pnrouter.ui.activity.main.ActiveListActivity
import com.stratagile.pnrouter.ui.activity.main.contract.ActiveListContract
import com.stratagile.pnrouter.ui.activity.main.presenter.ActiveListPresenter

import dagger.Module;
import dagger.Provides;

/**
 * @author hzp
 * @Package com.stratagile.pnrouter.ui.activity.main
 * @Description: The moduele of ActiveListActivity, provide field for ActiveListActivity
 * @date 2020/05/19 18:03:37
 */
@Module
class ActiveListModule (private val mView: ActiveListContract.View) {

    @Provides
    @ActivityScope
    fun provideActiveListPresenter(httpAPIWrapper: HttpAPIWrapper) :ActiveListPresenter {
        return ActiveListPresenter(httpAPIWrapper, mView)
    }

    @Provides
    @ActivityScope
    fun provideActiveListActivity() : ActiveListActivity {
        return mView as ActiveListActivity
    }
}