package com.stratagile.pnrouter.ui.adapter.conversation

import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.socks.library.KLog
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.db.EmailContactsEntityDao
import com.stratagile.pnrouter.db.EmailMessageEntity
import com.stratagile.pnrouter.utils.*
import com.stratagile.pnrouter.view.ImageButtonWithText

class EmaiMessageAdapter(arrayList: MutableList<EmailMessageEntity>) : BaseQuickAdapter<EmailMessageEntity, BaseViewHolder>(R.layout.email_row_item, arrayList) {
    override fun convert(helper: BaseViewHolder, item: EmailMessageEntity, payloads: MutableList<Any>) {
        KLog.i("第二次刷新到了。。。。")
        KLog.i(payloads[0] as String)

        var message = helper.getView<TextView>(R.id.message)
        if (item.originalText != null && item.originalText != "") {
            var originalTextCun = StringUitl.StripHT(item.originalText)
            /*var originalTextCunNew = originalTextCun
            var endIndex = originalTextCunNew.indexOf(" ")
            if(endIndex < 0)
            {
                endIndex = originalTextCunNew.length
            }
            originalTextCunNew =  originalTextCunNew.substring(0,endIndex)*/
            message.setText(originalTextCun.trim())
        } else {
            if (item.contentText.trim().length > 50) {
                message.setText(item.contentText.trim().substring(0, 49))
            } else {
                message.setText(item.contentText.trim())
            }
        }

        var unseen = helper.getView<TextView>(R.id.unseen)
        if (item.isSeen()) {
            unseen.visibility = View.GONE
        } else {
            unseen.visibility = View.VISIBLE
        }
        var startPic = helper.getView<TextView>(R.id.startPic)
        if (item.isStar()) {
            startPic.visibility = View.VISIBLE
        } else {
            startPic.visibility = View.GONE
        }
        var lockPic = helper.getView<TextView>(R.id.lockPic)
        if (item.originalText != null && item.originalText != "") {
            lockPic.visibility = View.VISIBLE
        } else {
            if (item.content.contains("newconfidantpass")) {
                lockPic.visibility = View.VISIBLE
            } else {
                lockPic.visibility = View.GONE
            }

        }
        var attach = helper.getView<TextView>(R.id.attach)
        /*if(item.attachmentCount >0)
        {
            attach.visibility = View.VISIBLE
            attach.text = item.attachmentCount.toString();
        }else{
            attach.visibility = View.GONE
        }*/
        if (item.isContainerAttachment()) {
            attach.visibility = View.VISIBLE
            attach.text = "";
        } else {
            attach.visibility = View.GONE
        }
    }

    var isChooseMode = false
    override fun convert(helper: BaseViewHolder, item: EmailMessageEntity) {
        var formName = ""
        var from = item.from_;
        var account = ""
        var menu = item.menu_
        if (menu.contains("Sent") || menu.contains("已发") || menu.contains("Drafts") || menu.contains("草稿")) {
            from = item.to_;
            if (from.contains(",")) {
                var formList = from.split(",")
                for (item in formList) {

                    account += item.substring(item.indexOf("<") + 1, item.length - 1) + ","
                    var localEmailContacts = AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.queryBuilder().where(EmailContactsEntityDao.Properties.Account.eq(item)).list()
                    if (localEmailContacts.size != 0) {
                        var localEmailContactsItem = localEmailContacts.get(0)
                        formName += localEmailContactsItem.name + ","
                    } else {
                        if (item.indexOf("<") >= 0) {
                            var name = item.substring(0, item.indexOf("<")).trim().replace("\"", "").replace("\"", "")
                            formName += name + ","
                        } else {
                            formName += item.substring(0, item.indexOf("@")) + ","
                        }

                    }
                }
                if (account.contains(",")) {
                    account = account.substring(0, account.lastIndexOf(","))
                }
                if (formName.contains(",")) {
                    formName = formName.substring(0, formName.lastIndexOf(","))
                }
            } else {
                account = from.substring(from.indexOf("<") + 1, from.length - 1)
                var localEmailContacts = AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.queryBuilder().where(EmailContactsEntityDao.Properties.Account.eq(account)).list()
                if (localEmailContacts.size != 0) {
                    var localEmailContactsItem = localEmailContacts.get(0)
                    formName = localEmailContactsItem.name
                } else {
                    if (from.indexOf("<") >= 0) {
                        formName = from.substring(0, from.indexOf("<"))
                    } else {
                        formName = from.substring(0, from.indexOf("@"))
                    }

                }
            }

        } else {
            if (from.indexOf("<") >= 0) {
                formName = from.substring(0, from.indexOf("<"))
            } else {
                formName = from.substring(0, from.indexOf("@"))
            }
        }

        var title = helper.getView<TextView>(R.id.title)
        formName = formName.replace("\"", "")
        formName = formName.replace("\"", "")
        title.setText(formName)
        var subject = helper.getView<TextView>(R.id.subject)
        subject.setText(item.subject_)

        var time = helper.getView<TextView>(R.id.time)
        time.setText(DateUtil.getTimestampString(DateUtil.getDate(item.date_), AppConfig.instance))


        var ivAvatar = helper.getView<ImageButtonWithText>(R.id.avatar)
        ivAvatar.setText(formName)

        var attach = helper.getView<TextView>(R.id.attach)
        var message = helper.getView<TextView>(R.id.message)
        var unseen = helper.getView<TextView>(R.id.unseen)
        var startPic = helper.getView<TextView>(R.id.startPic)
        var lockPic = helper.getView<TextView>(R.id.lockPic)

        if (item.content != null) {

            if (item.originalText != null && item.originalText != "") {
                var originalTextCun = StringUitl.StripHT(item.originalText)
                /*var originalTextCunNew = originalTextCun
                var endIndex = originalTextCunNew.indexOf(" ")
                if(endIndex < 0)
                {
                    endIndex = originalTextCunNew.length
                }
                originalTextCunNew =  originalTextCunNew.substring(0,endIndex)*/
                message.setText(originalTextCun.trim())
            } else {
                if (item.contentText.trim().length > 50) {
                    message.setText(item.contentText.trim().substring(0, 49))
                } else {
                    message.setText(item.contentText.trim())
                }
            }


            if (item.isSeen()) {
                unseen.visibility = View.GONE
            } else {
                unseen.visibility = View.VISIBLE
            }

            if (item.isStar()) {
                startPic.visibility = View.VISIBLE
            } else {
                startPic.visibility = View.GONE
            }
            if (item.originalText != null && item.originalText != "") {
                lockPic.visibility = View.VISIBLE
            } else {
                if (item.content.contains("newconfidantpass")) {
                    lockPic.visibility = View.VISIBLE
                } else {
                    lockPic.visibility = View.GONE
                }

            }
            /*if(item.attachmentCount >0)
            {
                attach.visibility = View.VISIBLE
                attach.text = item.attachmentCount.toString();
            }else{
                attach.visibility = View.GONE
            }*/
            if (item.isContainerAttachment()) {
                attach.visibility = View.VISIBLE
                attach.text = "";
            } else {
                attach.visibility = View.GONE
            }
        } else {
            attach.visibility = View.GONE
            unseen.visibility = View.GONE
            startPic.visibility = View.GONE
            lockPic.visibility = View.GONE
            message.setText("-/-")
        }
    }

}