package com.stratagile.pnrouter.utils

import android.database.Cursor
import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.db.EmailMessageEntity
import com.stratagile.pnrouter.db.EmailMessageEntityDao
import org.greenrobot.greendao.database.Database
import java.nio.ByteBuffer

object EmailUtils {
    fun loadLocalEmail(account : String, menu : String) : MutableList<EmailMessageEntity> {
        var emailMessageEntityList = mutableListOf<EmailMessageEntity>()
        var queryString = "SELECT _id,ACCOUNT_,MENU_,FROM_,MSG_ID,SUBJECT_,TO_,CC,BCC,DATE_,TIME_STAMP_,SIZE,IS_STAR,USER_ID,SORT_ID,CONTENT_TEXT,ORIGINAL_TEXT,ORIGINAL_BODY,AES_KEY,SUBJECT_,IS_SEEN,IS_REPLY_SIGN,PRIORITY,IS_CONTAINER_ATTACHMENT,ATTACHMENT_COUNT,MESSAGE_TOTAL_COUNT,EMAIL_ATTACH_PATH FROM " + EmailMessageEntityDao.TABLENAME +" WHERE " +
                EmailMessageEntityDao.Properties.Account_.columnName + " = '" + account +
                "' and " + EmailMessageEntityDao.Properties.Menu_.columnName + " = '" + menu + "'" +
                " order by ${EmailMessageEntityDao.Properties.SortId.columnName} desc"
        val zero: Short = 0
        var cursor = AppConfig.instance.mDaoMaster!!.newSession().database.rawQuery(queryString, null)
        while (cursor.moveToNext()) {
            var emailMessageEntity = EmailMessageEntity()
            try {
                emailMessageEntity.id = cursor.getLong(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Id.columnName))
                emailMessageEntity.account_ = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Account_.columnName))
                emailMessageEntity.msgId = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.MsgId.columnName))
                emailMessageEntity.menu_ = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Menu_.columnName))
                emailMessageEntity.from_ = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.From_.columnName))
                emailMessageEntity.to_ = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.To_.columnName))
                emailMessageEntity.date_ = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Date_.columnName))
                emailMessageEntity.timeStamp_ = cursor.getLong(cursor.getColumnIndex(EmailMessageEntityDao.Properties.TimeStamp_.columnName))
                emailMessageEntity.size = cursor.getLong(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Size.columnName))
                emailMessageEntity.setIsStar(cursor.getShort(cursor.getColumnIndex(EmailMessageEntityDao.Properties.IsStar.columnName)) != zero)
                emailMessageEntity.setIsSeen(cursor.getShort(cursor.getColumnIndex(EmailMessageEntityDao.Properties.IsSeen.columnName)) != zero)
                emailMessageEntity.setIsContainerAttachment(cursor.getShort(cursor.getColumnIndex(EmailMessageEntityDao.Properties.IsContainerAttachment.columnName)) != zero)
                emailMessageEntity.setIsReplySign(cursor.getShort(cursor.getColumnIndex(EmailMessageEntityDao.Properties.IsReplySign.columnName)) != zero)
                emailMessageEntity.priority = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Priority.columnName))
                emailMessageEntity.userId = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.UserId.columnName))
                emailMessageEntity.sortId = cursor.getLong(cursor.getColumnIndex(EmailMessageEntityDao.Properties.SortId.columnName))
                emailMessageEntity.messageTotalCount = cursor.getLong(cursor.getColumnIndex(EmailMessageEntityDao.Properties.MessageTotalCount.columnName))
                emailMessageEntity.contentText = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.ContentText.columnName))
                emailMessageEntity.originalText = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.OriginalText.columnName))
                emailMessageEntity.originalBody = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.OriginalBody.columnName))
                emailMessageEntity.cc = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Cc.columnName))
                emailMessageEntity.bcc = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Bcc.columnName))
                emailMessageEntity.aesKey = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.AesKey.columnName))
                emailMessageEntity.subject_ = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Subject_.columnName))
                emailMessageEntity.attachmentCount = cursor.getInt(cursor.getColumnIndex(EmailMessageEntityDao.Properties.AttachmentCount.columnName))
                emailMessageEntity.emailAttachPath = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.EmailAttachPath.columnName))
                emailMessageEntity.content = getContent(AppConfig.instance.mDaoMaster!!.newSession().database, emailMessageEntity.id)
                emailMessageEntityList.add(emailMessageEntity)
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
        cursor.close()
        return emailMessageEntityList
    }

    fun loadLocalEmailWithMsgId(account : String, menu : String, msgId : String) : MutableList<EmailMessageEntity> {
        var emailMessageEntityList = mutableListOf<EmailMessageEntity>()
        var queryString = "SELECT _id,ACCOUNT_,MENU_,FROM_,MSG_ID,SUBJECT_,TO_,CC,BCC,DATE_,TIME_STAMP_,SIZE,IS_STAR,USER_ID,SORT_ID,CONTENT_TEXT,ORIGINAL_TEXT,ORIGINAL_BODY,AES_KEY,SUBJECT_,IS_SEEN,IS_REPLY_SIGN,PRIORITY,IS_CONTAINER_ATTACHMENT,ATTACHMENT_COUNT,MESSAGE_TOTAL_COUNT,EMAIL_ATTACH_PATH FROM " + EmailMessageEntityDao.TABLENAME +" WHERE " +
                EmailMessageEntityDao.Properties.Account_.columnName + " = '" + account +
                "' and " + EmailMessageEntityDao.Properties.Menu_.columnName + " = '" + menu + "'" +
                " and " + EmailMessageEntityDao.Properties.MsgId.columnName + " = '" + msgId + "'" +
                " order by ${EmailMessageEntityDao.Properties.SortId.columnName} desc"
        val zero: Short = 0
        var cursor = AppConfig.instance.mDaoMaster!!.newSession().database.rawQuery(queryString, null)
        while (cursor.moveToNext()) {
            var emailMessageEntity = EmailMessageEntity()
            try {
                emailMessageEntity.id = cursor.getLong(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Id.columnName))
                emailMessageEntity.account_ = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Account_.columnName))
                emailMessageEntity.msgId = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.MsgId.columnName))
                emailMessageEntity.menu_ = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Menu_.columnName))
                emailMessageEntity.from_ = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.From_.columnName))
                emailMessageEntity.to_ = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.To_.columnName))
                emailMessageEntity.date_ = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Date_.columnName))
                emailMessageEntity.timeStamp_ = cursor.getLong(cursor.getColumnIndex(EmailMessageEntityDao.Properties.TimeStamp_.columnName))
                emailMessageEntity.size = cursor.getLong(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Size.columnName))
                emailMessageEntity.setIsStar(cursor.getShort(cursor.getColumnIndex(EmailMessageEntityDao.Properties.IsStar.columnName)) != zero)
                emailMessageEntity.setIsSeen(cursor.getShort(cursor.getColumnIndex(EmailMessageEntityDao.Properties.IsSeen.columnName)) != zero)
                emailMessageEntity.setIsContainerAttachment(cursor.getShort(cursor.getColumnIndex(EmailMessageEntityDao.Properties.IsContainerAttachment.columnName)) != zero)
                emailMessageEntity.setIsReplySign(cursor.getShort(cursor.getColumnIndex(EmailMessageEntityDao.Properties.IsReplySign.columnName)) != zero)
                emailMessageEntity.priority = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Priority.columnName))
                emailMessageEntity.userId = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.UserId.columnName))
                emailMessageEntity.sortId = cursor.getLong(cursor.getColumnIndex(EmailMessageEntityDao.Properties.SortId.columnName))
                emailMessageEntity.messageTotalCount = cursor.getLong(cursor.getColumnIndex(EmailMessageEntityDao.Properties.MessageTotalCount.columnName))
                emailMessageEntity.contentText = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.ContentText.columnName))
                emailMessageEntity.originalText = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.OriginalText.columnName))
                emailMessageEntity.originalBody = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.OriginalBody.columnName))
                emailMessageEntity.cc = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Cc.columnName))
                emailMessageEntity.bcc = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Bcc.columnName))
                emailMessageEntity.aesKey = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.AesKey.columnName))
                emailMessageEntity.subject_ = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.Subject_.columnName))
                emailMessageEntity.attachmentCount = cursor.getInt(cursor.getColumnIndex(EmailMessageEntityDao.Properties.AttachmentCount.columnName))
                emailMessageEntity.emailAttachPath = cursor.getString(cursor.getColumnIndex(EmailMessageEntityDao.Properties.EmailAttachPath.columnName))
                emailMessageEntity.content = getContent(AppConfig.instance.mDaoMaster!!.newSession().database, emailMessageEntity.id)
                emailMessageEntityList.add(emailMessageEntity)
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
        cursor.close()
        return emailMessageEntityList
    }

    fun getContent(db: Database, srID: Long): String? {
        var c: Cursor? = null
        //因为安卓性能特性，只能CursorWindow只能是1M的文件
        try {
            //先判断文件大小
            c = db.rawQuery("SELECT length(CONTENT) as len FROM " + EmailMessageEntityDao.TABLENAME + " WHERE _id = $srID", null)
            var len = 0
            if (c.moveToFirst()) {
                len = c.getInt(0)
                if (c != null) {
                    c.close()
                }
                if (len < 100 * 10000) {
                    c = db.rawQuery("SELECT CONTENT FROM " + EmailMessageEntityDao.TABLENAME + " WHERE _id = $srID", null)
                    if (c.moveToNext()) {
                        return c.getString(c.getColumnIndex("CONTENT"))
                    }
                    if (c != null) {
                        c.close()
                    }
                } else {
                    val buffer: ByteBuffer = ByteBuffer.allocate(len)
                    var count = 0
                    var stemp = 600 * 1024
                    while (count < len) {
                        if (count + stemp > len) {
                            stemp = len - count
                        }
                        c = db.rawQuery("SELECT substr(CONTENT," + (count + 1) + "," + stemp + ") as CONTENT FROM  " + EmailMessageEntityDao.TABLENAME + " WHERE _id = " + srID, null)
                        if (c.moveToNext()) {
                            val temp: ByteArray = c.getBlob(c.getColumnIndex("CONTENT"))
                            val bytes = ByteArray(stemp)
                            System.arraycopy(temp, 0, bytes, 0, bytes.size)
                            buffer.put(bytes)
                        }
                        if (c != null) {
                            c.close()
                        }
                        count += stemp
                    }
                    return String(buffer.array())
                }
            }
        } finally {
            if (c != null) {
                c.close()
            }
        }
        return ""
    }
}