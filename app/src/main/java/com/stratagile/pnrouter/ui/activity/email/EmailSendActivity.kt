package com.stratagile.pnrouter.ui.activity.email

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.GridLayoutManager
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import com.hyphenate.easeui.model.EaseCompat
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeui.utils.PathUtils
import com.luck.picture.lib.PicturePreviewActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.observable.ImagesObservable
import com.pawegio.kandroid.longToast
import com.pawegio.kandroid.runDelayedOnUiThread
import com.pawegio.kandroid.toast
import com.smailnet.eamil.Callback.GetAttachCallback
import com.smailnet.eamil.Callback.GetSendCallback
import com.smailnet.eamil.EmailReceiveClient
import com.smailnet.eamil.EmailSendClient
import com.smailnet.eamil.MailAttachment
import com.smailnet.eamil.Utils.AESCipher
import com.smailnet.eamil.Utils.AESToolsCipher
import com.smailnet.islands.Islands
import com.socks.library.KLog
import com.stratagile.pnrouter.R
import com.stratagile.pnrouter.application.AppConfig
import com.stratagile.pnrouter.base.BaseActivity
import com.stratagile.pnrouter.constant.ConstantValue
import com.stratagile.pnrouter.data.web.PNRouterServiceMessageReceiver
import com.stratagile.pnrouter.db.*
import com.stratagile.pnrouter.entity.*
import com.stratagile.pnrouter.entity.events.SendEmailSuccess
import com.stratagile.pnrouter.gmail.GmailQuickstart
import com.stratagile.pnrouter.method.Method
import com.stratagile.pnrouter.method.MethodContext
import com.stratagile.pnrouter.method.User
import com.stratagile.pnrouter.method.Weibo
import com.stratagile.pnrouter.ui.activity.email.component.DaggerEmailSendComponent
import com.stratagile.pnrouter.ui.activity.email.contract.EmailSendContract
import com.stratagile.pnrouter.ui.activity.email.module.EmailSendModule
import com.stratagile.pnrouter.ui.activity.email.presenter.EmailSendPresenter
import com.stratagile.pnrouter.ui.activity.email.view.ColorPickerView
import com.stratagile.pnrouter.ui.activity.email.view.RichEditor
import com.stratagile.pnrouter.ui.activity.email.view.SemicolonTokenizer
import com.stratagile.pnrouter.ui.adapter.conversation.EmaiAttachAdapter
import com.stratagile.pnrouter.utils.*
import com.stratagile.pnrouter.view.SweetAlertDialog
import com.stratagile.tox.toxcore.ToxCoreJni
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionListener
import kotlinx.android.synthetic.main.email_picture_image_grid_item.*
import kotlinx.android.synthetic.main.email_send_edit.*
import org.greenrobot.eventbus.EventBus
import org.libsodium.jni.Sodium
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import javax.inject.Inject
import kotlin.concurrent.thread


/**
 * @author zl
 * @Package com.stratagile.pnrouter.ui.activity.email
 * @Description: $description
 * @date 2019/07/25 11:21:29
 * data/data/com.android.provider.media/databases/external.db
 */

class EmailSendActivity : BaseActivity(), EmailSendContract.View, View.OnClickListener, PNRouterServiceMessageReceiver.CheckmailUkeyCallback {
    override fun addFriendsAuto(jAddFriendsAutoRsp: JAddFriendsAutoRsp) {
        if (jAddFriendsAutoRsp.params.retCode == 0) {
            var selfUserId = SpUtil.getString(this, ConstantValue.userId, "")
            var pullFriend = PullFriendReq_V4(selfUserId!!)
            var sendData = BaseData(pullFriend)
            if (ConstantValue.encryptionType.equals("1")) {
                sendData = BaseData(6, pullFriend)
            }
            if (ConstantValue.isWebsocketConnected) {
                AppConfig.instance.getPNRouterServiceMessageSender().send(sendData)
            } else if (ConstantValue.isToxConnected) {
                var baseData = sendData
                var baseDataJson = baseData.baseDataToJson().replace("\\", "")
                if (ConstantValue.isAntox) {
                    //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                    //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
                } else {
                    ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.currentRouterId.substring(0, 64))
                }
            }
        }
    }


    @Inject
    internal lateinit var mPresenter: EmailSendPresenter

    /********************boolean开关 */
    //是否加粗
    internal var isClickBold = false

    //是否正在执行动画
    internal var isAnimating = false

    //是否按ol排序
    internal var isListOl = false

    //是否按ul排序
    internal var isListUL = false

    //是否下划线字体
    internal var isTextLean = false

    //是否下倾斜字体
    internal var isItalic = false

    //是否左对齐
    internal var isAlignLeft = false

    //是否右对齐
    internal var isAlignRight = false

    //是否中对齐
    internal var isAlignCenter = false

    //是否缩进
    internal var isIndent = false

    //是否较少缩进
    internal var isOutdent = false

    //是否索引
    internal var isBlockquote = false

    //字体中划线
    internal var isStrikethrough = false

    //字体上标
    internal var isSuperscript = false

    //字体下标
    internal var isSubscript = false

    private var onKeyDel = false

    private var ctrlPress = false

    /********************变量 */
    //折叠视图的宽高
    private var mFoldedViewMeasureHeight: Int = 0
    var emaiAttachAdapter: EmaiAttachAdapter? = null
    protected var cameraFile: File? = null
    protected var videoFile: File? = null
    protected val REQUEST_CODE_MAP = 1
    protected val REQUEST_CODE_CAMERA = 2
    protected val REQUEST_CODE_LOCAL = 3
    protected val REQUEST_CODE_DING_MSG = 4
    protected val REQUEST_CODE_FILE = 5
    protected val REQUEST_CODE_VIDEO = 6

    protected val REQUEST_CODE_TO = 101
    protected val REQUEST_CODE_CC = 102
    protected val REQUEST_CODE_BCC = 103
    protected val CHOOSE_PIC = 88 //选择原图还是压缩图
    private var imputOld: String? = null
    private val methods = arrayOf(Weibo)//arrayOf(Weibo,WeChat, QQ)
    private var iterator: Iterator<Method> = methods.iterator()
    private val methodContext = MethodContext()
    private val methodContextCc = MethodContext()
    private val methodContextBcc = MethodContext()
    var needSize = 0;
    var contactMapList = HashMap<String, String>()
    internal var previewImages: MutableList<LocalMedia> = java.util.ArrayList()
    var replayAll = true
    var attachListEntityNode = arrayListOf<EmailAttachEntity>()
    var dataTips = arrayListOf<String>()
    var addressBase64 = ""
    var userPassWord = ""
    var userPassWordTips = ""
    var galleryPath = ""

    private val users = arrayListOf(
            User("1", "激浊扬清", ""),
            User("15", "必须要\\n\n，不然不够长", ""))

    //flag == 3为外界选中一个文件作为附件发送邮件
    var flag = 0;
    //forward == 1 为转发，
    var foward = 0;
    var emailMeaasgeInfoData: EmailMessageEntity? = null
    var oldAdress = ""
    var attach = 0;
    var menu: String = "INBOX"
    var attachListEntity = arrayListOf<EmailAttachEntity>()
    var isSendCheck = false
    var positionIndex = 0;
    var toAdressEditLastContent = ""
    var ccAdressEditLastContent = ""
    var bccAdressEditLastContent = ""
    var InviteURLText = ""
    var routerEntity: RouterEntity? = null

    override fun checkmailUkey(jCheckmailUkeyRsp: JCheckmailUkeyRsp) {
        if (isSendCheck) {
            runOnUiThread {
                closeProgressDialog()
            }
        }
        if (jCheckmailUkeyRsp.params.retCode == 0) {
            var data = jCheckmailUkeyRsp.params.payload
            needSize = data.size;
            for (item in data) {
                if (item.pubKey != "") {
                    var value = item.pubKey;
                    val dst_public_MiKey_Friend = ByteArray(32)
                    val crypto_sign_ed25519_pk_to_curve25519_result = Sodium.crypto_sign_ed25519_pk_to_curve25519(dst_public_MiKey_Friend, RxEncodeTool.base64Decode(value))
                    if (crypto_sign_ed25519_pk_to_curve25519_result == 0) {
                        contactMapList.put(item.user, RxEncodeTool.base64Encode2String(dst_public_MiKey_Friend))
                    }
                } else {
                    var aa = AppConfig.instance.mDaoMaster!!.newSession().userEntityDao.queryBuilder().list()
                    var localFriendList = AppConfig.instance.mDaoMaster!!.newSession().userEntityDao.queryBuilder().where(UserEntityDao.Properties.Mails.`in`(item.user)).list()
                    if (localFriendList.size > 0) {
                        var it = localFriendList.get(0)
                        var value = it.signPublicKey;
                        val dst_public_MiKey_Friend = ByteArray(32)
                        val crypto_sign_ed25519_pk_to_curve25519_result = Sodium.crypto_sign_ed25519_pk_to_curve25519(dst_public_MiKey_Friend, RxEncodeTool.base64Decode(value))
                        if (crypto_sign_ed25519_pk_to_curve25519_result == 0) {
                            contactMapList.put(item.user, RxEncodeTool.base64Encode2String(dst_public_MiKey_Friend))
                        }
                    }

                }

            }
            if (isSendCheck) {
                sendEmail(true);
            }
            runOnUiThread {
                if (contactMapList.size == needSize && InviteURLText == "") {
                    lockTips.visibility = View.VISIBLE
                } else {
                    lockTips.visibility = View.GONE
                }
            }
        } else {
            needSize = 0;
            contactMapList = HashMap<String, String>()
            if (isSendCheck) {
                sendEmail(true);
            }
            runOnUiThread {
                lockTips.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        needFront = true
        isEditActivity = true
        statusBarColor = R.color.headmainColor
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        setContentView(R.layout.email_send_edit)

//        val llp2 = LinearLayout.LayoutParams(UIUtils.getDisplayWidth(this), UIUtils.getStatusBarHeight(this))
//        statusBar.setLayoutParams(llp2)
    }

    override fun initData() {
        InviteURLText = ""
        addressBase64 = ""
        AppConfig.instance.messageReceiver?.checkmailUkeyCallback = this
        var emailContactsList = AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.queryBuilder().where(EmailContactsEntityDao.Properties.Account.notEq("")).orderDesc(EmailContactsEntityDao.Properties.CreateTime).list()
        for (item in emailContactsList) {
            dataTips.add(item.account)
        }
        var adapter = ArrayAdapter<String>(this@EmailSendActivity, android.R.layout.simple_dropdown_item_1line, dataTips)
        toAdressEdit!!.setAdapter(adapter)
        toAdressEdit!!.setTokenizer(SemicolonTokenizer(';'))

        var adaptercc = ArrayAdapter<String>(this@EmailSendActivity, android.R.layout.simple_dropdown_item_1line, dataTips)
        ccAdressEdit!!.setAdapter(adaptercc)
        ccAdressEdit!!.setTokenizer(SemicolonTokenizer(';'))

        var adapterbcc = ArrayAdapter<String>(this@EmailSendActivity, android.R.layout.simple_dropdown_item_1line, dataTips)
        bccAdressEdit!!.setAdapter(adapterbcc)
        bccAdressEdit!!.setTokenizer(SemicolonTokenizer(';'))

        emailMeaasgeInfoData = AppConfig.instance.emailSendoMessageEntity
//        emailMeaasgeInfoData = intent.getParcelableExtra("emailMeaasgeInfoData")
        flag = intent.getIntExtra("flag", 0)
        foward = intent.getIntExtra("foward", 0)
        attach = intent.getIntExtra("attach", 0)
        menu = intent.getStringExtra("menu")
        positionIndex = intent.getIntExtra("positionIndex", 0)
        if (intent.hasExtra("attachListEntityNode")) {
            attachListEntityNode = intent.getParcelableArrayListExtra("attachListEntityNode")
        }
        initUI()
        initClickListener()
        sendCheck(false)
    }

    /**
     * 初始化View
     */
    private fun initUI() {
        if (methodContext.method == null) {
            switch()
        }
        cardView2.visibility = View.GONE
        val selectionEnd = toAdressEdit.length()
        val selectionStart = 5
        var aa = toAdressEdit!!.getText()
        val spans = toAdressEdit!!.getText()!!.getSpans(selectionStart, selectionEnd, User::class.java)
        initEditor()
        initMenu()
        initColorPicker()
        oldtitle.visibility = View.GONE
        list_itease_layout_info.visibility = View.GONE
        sentTitle.visibility = View.VISIBLE
        re_main_editor.visibility = View.VISIBLE
        if (flag == 1) {
            initBaseUI(emailMeaasgeInfoData!!)
            oldtitle.visibility = View.VISIBLE
            sentTitle.visibility = View.GONE
            list_itease_layout_info.visibility = View.VISIBLE
            if (foward == 1) {
                list_itease_layout_info.visibility = View.GONE
                sentTitle.visibility = View.VISIBLE
            }

        } else if (flag == 100) {

            addKeyImgParent.visibility = View.GONE
            attachList.visibility = View.GONE
            re_main_editor.visibility = View.GONE

            var myAccount = ""
            if (ConstantValue.currentEmailConfigEntity != null) {
                myAccount = ConstantValue.currentEmailConfigEntity!!.account
            }
            subject.setText(getString(R.string.You_got_an_email_from_your_friend) + " " + myAccount) //"You got an email from your friend xxxx@gmail.com"
            val lp = LinearLayout.LayoutParams(webViewParent.getLayoutParams())
            lp.setMargins(0, 0, 0, 0)
            webViewParent.setLayoutParams(lp);
            initWebviewUI()

            var routerList = AppConfig.instance.mDaoMaster!!.newSession().routerEntityDao.queryBuilder().where(RouterEntityDao.Properties.RouterId.eq(ConstantValue.currentRouterId)).list()

            if (routerList.size > 0) {
                routerEntity = routerList[0]
            }
            tvRouterName.text = "【" + routerEntity!!.routerName + "】"
            adminName.text = getString(R.string.Circle_Owner) + String(RxEncodeTool.base64Decode(routerEntity!!.adminName))
            ivAvatar2.setText(SpUtil.getString(this, ConstantValue.username, "")!!)
            var fileBase58Name = Base58.encode(RxEncodeTool.base64Decode(ConstantValue.libsodiumpublicSignKey)) + ".jpg"
            ivAvatar2.setImageFile(fileBase58Name)
            var userId = FileUtil.getLocalUserData("userid")
            var nickName = SpUtil.getString(this, ConstantValue.username, "")
            val selfNickNameBase64 = RxEncodeTool.base64Encode2String(nickName!!.toByteArray())
            var codeStr = "type_4," + userId + "," + selfNickNameBase64 + "," + ConstantValue.libsodiumpublicSignKey!!
            var routerCodeData: RouterCodeData = RouterCodeData();
            routerCodeData.id = "010001".toByteArray()
            routerCodeData.routerId = ConstantValue.currentRouterId.toByteArray()
            routerCodeData.userSn = ConstantValue.currentRouterSN.toByteArray()
            var routerCodeDataByte = routerCodeData.toByteArray();
            var base64Str = AESCipher.aesEncryptBytesToBase64(routerCodeDataByte, "welcometoqlc0101".toByteArray())
            codeStr += "," + base64Str;

            Thread(Runnable() {
                run() {
                    Thread.sleep(500)
                    runOnUiThread {
                        cardView2.visibility = View.VISIBLE
                    }
                    var bitMapAvatar = getRoundedCornerBitmap(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                    var bitmap: Bitmap = QRCodeEncoder.syncEncodeQRCode(codeStr, BGAQRCodeUtil.dp2px(AppConfig.instance, 150f), AppConfig.instance.getResources().getColor(R.color.mainColor), bitMapAvatar)
                    runOnUiThread {
                        ivQrCode2.setImageBitmap(bitmap)
                    }
                    Thread.sleep(1000)
                    saveQrCodeToPhone()
                }
            }).start()
        } else if (flag == 255) {
            var emailAdress = intent.getStringExtra("emailAdress")
            var fromAdressList = emailAdress.split(",")
            for (item in fromAdressList) {
                var myAccount = AppConfig.instance.emailConfig().account
                var toAdress = getEditText(toAdressEdit)
                if (item != "" && !item.contains(myAccount) && !toAdress.contains(item)) {
                    var fromNameTemp = ""
                    var fromAdressTemp = ""
                    if (item.indexOf("<") >= 0) {
                        fromNameTemp = item.substring(0, item.indexOf("<"))
                        fromAdressTemp = item.substring(item.indexOf("<") + 1, item.length - 1)
                    } else {
                        fromNameTemp = item.substring(0, item.indexOf("@"))
                        fromAdressTemp = item.substring(0, item.length)
                    }
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    var user = User(fromAdressTemp, fromNameTemp, fromNameTemp)
                    (toAdressEdit.text as SpannableStringBuilder)
                            .append(methodContext.newSpannable(user))
                            .append(";")
                }
            }
            toAdressEdit.post(Runnable {
                var lineCount = toAdressEdit.lineCount
                toAdressEdit.minHeight = resources.getDimension(R.dimen.x50).toInt() * lineCount
            })
        } else if (flag == 3) {
            runDelayedOnUiThread(1000, {
                var dataIntent = Intent()
                dataIntent.putExtra("path", intent.getStringExtra("filePath"))
                onActivityResult(REQUEST_CODE_FILE, Activity.RESULT_OK, dataIntent)
            })
        }
        toAdressEdit.setOnItemClickListener { parent, view, position, id ->

            var content = parent.getItemAtPosition(position).toString()
            var aa = "";
        }
        toAdressEdit.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (hasFocus) {

                } else {
                    allSpan(toAdressEdit)
                    sendCheck(false)
                }
            }
        });
        ccAdressEdit.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (hasFocus) {

                } else {
                    allSpan(ccAdressEdit)
                    sendCheck(false)
                }
            }
        });
        bccAdressEdit.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (hasFocus) {

                } else {
                    allSpan(bccAdressEdit)
                    sendCheck(false)
                    bccAdressEditLastContent = bccAdressEdit.text.toString()
                }
            }
        });
        //根据输入框输入值的改变来过滤搜索
        subject.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                var lineCount = subject.lineCount
                subject.minHeight = resources.getDimension(R.dimen.x50).toInt() * lineCount
            }
        })
        //根据输入框输入值的改变来过滤搜索
        toAdressEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                var content = s.toString()
                if (content.length > 0) {
                    var endStr = content.substring(content.length - 1, content.length)
                    var result = StringUitl.checkBiaoD(endStr)
                    if (endStr == "，" || endStr == "；" || endStr == "。" || endStr == "," || endStr == "?" || endStr == "？") {
                        toAdressEdit.text.replace(content.length - 1, content.length, ";")
                    }
                }

                var lineCount = toAdressEdit.lineCount
                toAdressEdit.minHeight = resources.getDimension(R.dimen.x50).toInt() * lineCount
            }
        })
        //根据输入框输入值的改变来过滤搜索
        ccAdressEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                var content = s.toString()
                if (content.length > 0) {
                    var endStr = content.substring(content.length - 1, content.length)
                    var result = StringUitl.checkBiaoD(endStr)
                    if (endStr == "，" || endStr == "；" || endStr == "。" || endStr == "," || endStr == "?" || endStr == "？") {
                        ccAdressEdit.text.replace(content.length - 1, content.length, ";")
                    }
                }
                var lineCount = ccAdressEdit.lineCount
                ccAdressEdit.minHeight = resources.getDimension(R.dimen.x50).toInt() * lineCount
            }
        })
        //根据输入框输入值的改变来过滤搜索
        bccAdressEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                var content = s.toString()
                if (content.length > 0) {
                    var endStr = content.substring(content.length - 1, content.length)
                    var result = StringUitl.checkBiaoD(endStr)
                    if (endStr == "，" || endStr == "；" || endStr == "。" || endStr == "," || endStr == "?" || endStr == "？") {
                        bccAdressEdit.text.replace(content.length - 1, content.length, ";")
                    }
                }
                var lineCount = bccAdressEdit.lineCount
                bccAdressEdit.minHeight = resources.getDimension(R.dimen.x50).toInt() * lineCount
            }
        })
        initAttachUI()
    }

    //生成圆角图片
    fun getRoundedCornerBitmap(bitmap: Bitmap): Bitmap {
        var offWidth = 0
        val roundPx = resources.getDimension(R.dimen.x10)
        val widht = resources.getDimension(R.dimen.x20).toInt()
        val output = Bitmap.createBitmap(bitmap.width + offWidth, bitmap.height + offWidth, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width + offWidth, bitmap.height + offWidth)
        val rect1 = Rect(widht / 4, widht / 4, bitmap.width + widht / 4 + offWidth, bitmap.height + widht / 4 + offWidth)
        val rectF = RectF(rect)
        val rectF1 = RectF(rect1)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = resources.getColor(R.color.white)


        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect1, rect1, paint)


        return output
    }

    fun saveQrCodeToPhone() {
        thread {

            val dView2 = cardView2
            dView2.isDrawingCacheEnabled = true
            dView2.buildDrawingCache()
            val bitmap2 = Bitmap.createBitmap(dView2.drawingCache)
            if (bitmap2 != null) {
                try {
                    runOnUiThread {
                        cardView2.visibility = View.GONE
                    }
                    // 获取内置SD卡路径
                    galleryPath = (Environment.getExternalStorageDirectory().toString()
                            + File.separator + Environment.DIRECTORY_DCIM
                            + File.separator + "ConfidantTemp" + File.separator)
                    val galleryPathFile = File(galleryPath)
                    if (!galleryPathFile.exists()) {
                        galleryPathFile.mkdir()
                    }
                    // 图片文件路径
                    val filePath = galleryPath + "circleCode.jpg"
                    val file = File(filePath)
                    val os = FileOutputStream(file)
                    bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, os)
                    os.flush()
                    os.close()
                    AlbumNotifyHelper.insertImageToMediaStore(AppConfig.instance, filePath, System.currentTimeMillis())
                    KLog.i("存储完成")
                } catch (e: Exception) {
                }

            }
        }
    }

    private fun allSpan(editText: EditText) {

        var textStrList = editText.text.split(";")
        var i = 0
        for (str in textStrList) {
            if (str != "") {
                var beginIndex = i
                var endIndex = i + str.length
                val spans = editText!!.getText()!!.getSpans(beginIndex, endIndex, User::class.java)
                if (spans.size == 0) {
                    var addUser = User(str, str, str)
                    /* editText.text.replace(beginIndex,endIndex,methodContext.newSpannable(addUser))
                     (editText.text as SpannableStringBuilder).append(",")*/
                    /*(editText.text as SpannableStringBuilder)
                            .append(methodContext.newSpannable(addUser))
                            .append(" ")*/
                    editText.text.replace(beginIndex, endIndex, methodContext.newSpannable(addUser))
                    /*editText.text.replace(beginIndex,endIndex,"")
                    (editText.text as SpannableStringBuilder)
                            .append(methodContext.newSpannable(addUser))
                            .append(" ")*/
                }
            }
            i += str.length + 1
        }
    }

    fun initWebviewUI() {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title1: String?) {
                super.onReceivedTitle(view, title1)
                if (title1 != null) {
                    //title.text = title1
                }
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    KLog.i("进度：" + newProgress)
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                }
                super.onProgressChanged(view, newProgress)
            }

        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                //view.loadUrl(url)
                val intent = Intent()
                intent.action = "android.intent.action.VIEW"
                val url = Uri.parse(url)
                intent.data = url
                startActivity(intent)
                return true
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, error: SslError) {
                if (error.getPrimaryError() == SslError.SSL_DATE_INVALID
                        || error.getPrimaryError() == SslError.SSL_EXPIRED
                        || error.getPrimaryError() == SslError.SSL_INVALID
                        || error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
                    handler.proceed();
                } else {
                    handler.cancel();
                }
                super.onReceivedSslError(view, handler, error)
            }

            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                KLog.i("ddddddd")
                super.onReceivedHttpError(view, request, errorResponse)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                KLog.i("ddddddd")
                super.onReceivedError(view, request, error)
            }
        }
        var myAccount = ConstantValue.currentEmailConfigEntity!!.account
        InviteURLText = "<div id=\'box\'>" +
                "<style type=\'text/css\'>" +
                "* {" +
                "padding: 0;" +
                "border: 0;" +
                "outline: 0;" +
                "margin: 0;" +
                "}" +
                "a {" +
                "text-decoration: none;" +
                "background-color: transparent" +
                "}" +
                "a:hover," +
                "a:active {" +
                "outline-width: 0;" +
                "text-decoration: none" +
                "}" +
                "#box{" +
                "margin: 0 auto;" +
                "box-sizing: border-box;" +
                "max-width: 720px;" +
                "}" +
                "#box section {" +
                "padding: 16px;" +
                "}" +
                "#box header .Star {" +
                "float: right;" +
                "}" +
                ".userHead {" +
                "display: flex;" +
                "width: 100%;" +
                "box-sizing: border-box;" +
                "border-bottom: 1px solid #e6e6e6;" +
                "}" +
                ".userHeadA {" +
                "width: 44px;" +
                "height: 44px;" +
                "padding: 16px 0;" +
                "}" +
                ".userHeadB {" +
                "width: 240px;" +
                "height: 44px;" +
                "padding: 16px 0;" +
                "outline: 0px solid #ccc;" +
                "}" +
                ".userHeadC {" +
                "flex: 1;" +
                "text-align: right;" +
                "height: 44px;" +
                "padding: 18px 0;" +
                "outline: 0px solid #ccc;" +
                "}" +
                ".userHeadAimg {" +
                "width: 44px;" +
                "height: 44px;" +
                "border-radius: 22px;" +
                "}" +
                ".userHeadBdate {" +
                "color: #ccc;" +
                "margin-left: 8px;" +
                "}" +
                ".rowDiv {" +
                "padding: 20px 0;" +
                "}" +
                "button {" +
                "background: rgba(102, 70, 247, 1);" +
                "border-radius: 7px;" +
                "color: #fff;" +
                "}" +
                ".rowDiv3Btn {" +
                "padding: 12px 34px;" +
                "background: rgba(102, 70, 247, 1);" +
                "border-radius: 7px;" +
                "color: #fff;" +
                "}" +
                ".rowDiv h3 {" +
                "font-size: 16px;" +
                "line-height: 16px;" +
                "}" +
                "#box p {" +
                "line-height: 20px;" +
                "font-size: 12px;" +
                "}" +
                "#box h3 {" +
                "line-height: 40px;" +
                "}" +
                ".qrcodeDIV {" +
                "width: 120px;" +
                "margin: 0 30px;" +
                "}" +
                ".qrcodeDIV img {" +
                "width: 120px;" +
                "}" +
                ".btn {" +
                "width: 120px;" +
                "height: 22px;" +
                "display: block;" +
                "}" +
                ".btn img {" +
                "width: 100%;" +
                "height: 100%;" +
                "}" +
                ".h3logo {" +
                "position: relative;" +
                "top: 5px;" +
                "width: 24px;" +
                "margin-right: 5px;" +
                "}" +
                ".includePng {" +
                "float: right;" +
                "width: 110px;" +
                "position: relative;" +
                "top: -24px;" +
                "}" +
                ".rowDivBtn {" +
                "display: flex;" +
                "width: 100%;" +
                "justify-content: space-between;" +
                "}" +
                ".rowDivBtn div {" +
                "width: 158px;" +
                "height: 42px;" +
                "margin:5px;" +
                "}" +
                ".rowDivBtn .rowDivBtnAddlong {" +
                "width: 179px;" +
                "}" +
                ".rowDivBtn img {" +
                "width: 100%;" +
                "}" +
                ".jusCenter {" +
                "display: flex;" +
                "justify-content: center;" +
                "align-items: center;" +
                "}" +
                ".rowDivFooter {" +
                "background: #292B33;" +
                "color: #fff;" +
                "text-align: center;" +
                "}" +
                "#box .rowDivFooter p {" +
                "line-height: 30px;" +
                "}" +
                ".rowDivFooter i {" +
                "outline: 0px solid red;" +
                "font-style: normal;" +
                "overflow: hidden;" +
                "height: 9px;" +
                "width: 15px;" +
                "display: inline-block;" +
                "line-height: 15px;" +
                "position: relative;" +
                "top: -6px;" +
                "color: #6646F7;" +
                "}" +
                ".rowDivFooter i:last-child {" +
                "top: 0px;" +
                "height: 7px;" +
                "line-height: 0px;" +
                "top: 2px;" +
                "}" +
                ".rowDivSave {" +
                "text-align: center;" +
                "border-bottom: 1px solid #E6E6E6;" +
                "padding: 0 0 30px 0;" +
                "}" +
                "@media only screen and (min-width: 992px) {" +
                "#box{" +
                "width:706px;" +
                "}" +
                "}" +
                "@media only screen and (min-width: 1200px) {" +
                "#box{" +
                "width:680px;" +
                "background: white;" +
                "}" +
                "}" +
                "</style>" +
                "<section>" +
                "<div class=\'rowDiv\'>" +
                "<h3>Dear,<br/> Greetings " + myAccount + "</h3>" +
                "<p>This invitation was sent to you from your friend using Confidant, which is the platform for secure" +
                "&nbsp;encrypted Email and message communication. </p>" +
                "<p>You are invited to join him/her to stay in touch in a private and secure manner.</p>" +
                "<br/>" +
                "<p style=\'font-size: 14px;\'>To instantly access Confidant full services</p>" +
                "</div>" +
                "<div class=\'rowDiv\' style=\'padding: 5px 0;\'>" +
                "<p style=\'color: #757380;\'>1. Download the app via </p>" +
                "</div>" +
                "<div class=\'rowDiv jusCenter\' style=\'text-align: center;padding: 0\'>" +
                "<div class=\'qrcodeDIV\'>" +
                "<img src=\'https://confidant.oss-cn-hongkong.aliyuncs.com/images/confidant_app_qr.png\'>" +
                "<a href=\'https://apps.apple.com/us/app/my-confidant/id1456735273?l=zh&ls=1\'><img src=\'https://confidant.oss-cn-hongkong.aliyuncs.com/images/confidant_ios.png\'></a>" +
                "</div>" +
                "<div class=\'qrcodeDIV\'>" +
                "<img src=\'https://confidant.oss-cn-hongkong.aliyuncs.com/images/confidant_google_qr.png\'>" +
                "<a href=\'https://play.google.com/store/apps/details?id=com.stratagile.pnrouter\'><img src=\'https://confidant.oss-cn-hongkong.aliyuncs.com/images/confidant_google.png\'></a>" +
                "</div>" +
                "</div>" +
                "<div class=\'rowDiv\'><p style=\'color: #757380;border-bottom: 1px solid #e6e6e6;padding: 10px 0px 30px 0px;\'>2.Scan your friend\'s QR code in the attachment to start chatting</p></div>" +
                "<div class=\'rowDiv\'>" +
                "<p style=\'color: #757380;\'>Once done, we highly encourage you to send back a thank you message to your friend.</p>" +
                "<p style=\'color: #757380;\'>Stay safe and secured!</p>" +
                "</div>" +
                "<div class=\'rowDiv\'>" +
                "<img style=\'width: 100%;\' src=\'https://confidant.oss-cn-hongkong.aliyuncs.com/images/tie_se.png\' />" +
                "</div>" +
                "<div class=\'rowDiv\'>" +
                "<img style=\'width: 100%;\' src=\'https://confidant.oss-cn-hongkong.aliyuncs.com/images/logo_we.png\' />" +
                "</div>" +
                "</section>" +
                "</div>";
        try {
            webView.loadDataWithBaseURL(null, InviteURLText, "text/html", "utf-8", null);
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     *
     * 这里设置接收邮件的对象
     */
    fun initBaseUI(emailMessageEntity: EmailMessageEntity) {
        var fromName = ""
        var fromAdress = ""
        if (emailMessageEntity!!.from_.indexOf("<") >= 0) {
            fromName = emailMessageEntity!!.from_.substring(0, emailMessageEntity!!.from_.indexOf("<"))
            fromAdress = emailMessageEntity!!.from_.substring(emailMessageEntity!!.from_.indexOf("<") + 1, emailMessageEntity!!.from_.length - 1)
        } else {
            fromName = emailMessageEntity!!.from_.substring(0, emailMessageEntity!!.from_.indexOf("@"))
            fromAdress = emailMessageEntity!!.from_.substring(0, emailMessageEntity!!.from_.length)
        }
        var subjectText = emailMessageEntity.subject_
        if (foward == 0) {
            if (emailMessageEntity!!.from_.indexOf("<") >= 0) {
                fromName = emailMessageEntity!!.from_.substring(0, emailMessageEntity!!.from_.indexOf("<"))
                fromAdress = emailMessageEntity!!.from_.substring(emailMessageEntity!!.from_.indexOf("<") + 1, emailMessageEntity!!.from_.length - 1)
            } else {
                fromName = emailMessageEntity!!.from_.substring(0, emailMessageEntity!!.from_.indexOf("@"))
                fromAdress = emailMessageEntity!!.from_.substring(0, emailMessageEntity!!.from_.length)
            }
        } else if (foward == 3) {
            if (emailMessageEntity!!.to_.indexOf("<") >= 0) {
                fromName = emailMessageEntity!!.to_.substring(0, emailMessageEntity!!.to_.indexOf("<"))
                fromAdress = emailMessageEntity!!.to_.substring(emailMessageEntity!!.to_.indexOf("<") + 1, emailMessageEntity!!.to_.length - 1)
            } else {
                fromName = emailMessageEntity!!.to_.substring(0, emailMessageEntity!!.to_.indexOf("@"))
                fromAdress = emailMessageEntity!!.to_.substring(0, emailMessageEntity!!.to_.length)
            }
        }
        var localEmailContacts = AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.queryBuilder().where(EmailContactsEntityDao.Properties.Account.eq(fromAdress)).list()
        if (localEmailContacts.size != 0) {
            var localEmailContactsItem = localEmailContacts.get(0)
            fromName = localEmailContactsItem.name
        }
        fromName = fromName.replace("\"", "")
        fromName = fromName.replace("\"", "")

        if (emailMessageEntity!!.to_.contains(",")) {

            //title_info.setText(fromName.trim()+"...")
            /* if(fromAdress.indexOf(",") > -1)
             {
                 draft_info.setText(fromAdress.trim().substring(0,fromAdress.indexOf(","))+"...")
             }else{
                 draft_info.setText(fromAdress.trim()+"...")
             }*/

        } else {
            //title_info.setText(fromName.trim())
            //draft_info.setText(fromAdress.trim())
        }
        var myAccount = AppConfig.instance.emailConfig().account
        var myname = myAccount.substring(0, myAccount.indexOf("@"))
        if (AppConfig.instance.emailConfig().name != null && AppConfig.instance.emailConfig().name != "") {
            myname = AppConfig.instance.emailConfig().name
        }
        title_info.setText(myname)
        draft_info.setText(myAccount)
        avatar_info.setText(myname.trim())
        var aa = "";
        if (foward == 0) {
            var fromAdressList = emailMessageEntity!!.from_.split(",")
            for (item in fromAdressList) {
                if (item != "") {
                    var fromNameTemp = ""
                    var fromAdressTemp = ""
                    if (item.indexOf("<") >= 0) {
                        fromNameTemp = item.substring(0, item.indexOf("<"))
                        fromAdressTemp = item.substring(item.indexOf("<") + 1, item.length - 1)
                    } else {
                        try {
                            fromNameTemp = item.substring(0, item.indexOf("@"))
                            fromAdressTemp = item.substring(0, item.length)
                        } catch (e : Exception) {
                            e.printStackTrace()
                        }
                    }
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    var user = User(fromAdressTemp, fromNameTemp, fromNameTemp)
                    /* (toAdressEdit.text as SpannableStringBuilder)
                             .append(methodContext.newSpannable(user))
                             .append(";")*/
                }
            }
            toAdressEdit.post(Runnable {
                var lineCount = toAdressEdit.lineCount
                toAdressEdit.minHeight = resources.getDimension(R.dimen.x50).toInt() * lineCount
            })
            if (replayAll) {
                //回复收件人
                var fromAdressList = emailMessageEntity!!.to_.split(",")


                //回复发件人
                var toAddressList = emailMessageEntity!!.from_.split(",")[0]

                var fromNameTemp = ""
                var fromAdressTemp = ""
                if (toAddressList.indexOf("<") >= 0) {
                    fromNameTemp = toAddressList.substring(0, toAddressList.indexOf("<"))
                    fromAdressTemp = toAddressList.substring(toAddressList.indexOf("<") + 1, toAddressList.length - 1)
                } else {
                    fromNameTemp = toAddressList.substring(0, toAddressList.indexOf("@"))
                    fromAdressTemp = toAddressList.substring(0, toAddressList.length)
                }
                fromNameTemp = fromNameTemp.replace("\"", "")
                fromNameTemp = fromNameTemp.replace("\"", "")
                fromNameTemp = fromNameTemp.replace("\"", "")
                if (StringUitl.isEmail(fromAdressTemp)) {
                    var user = User(fromAdressTemp, fromNameTemp, fromNameTemp)
                    (toAdressEdit.text as SpannableStringBuilder)
                            .append(methodContext.newSpannable(user))
                            .append(";")
                }

                for (item in fromAdressList) {
                    var toAdress = getEditText(toAdressEdit)
                    if (item != "" && !item.contains(myAccount) && !toAdress.contains(item)) {
                        var fromNameTemp = ""
                        var fromAdressTemp = ""
                        if (item.indexOf("<") >= 0) {
                            fromNameTemp = item.substring(0, item.indexOf("<"))
                            fromAdressTemp = item.substring(item.indexOf("<") + 1, item.length - 1)
                        } else {
                            fromNameTemp = item.substring(0, item.indexOf("@"))
                            fromAdressTemp = item.substring(0, item.length)
                        }
                        fromNameTemp = fromNameTemp.replace("\"", "")
                        fromNameTemp = fromNameTemp.replace("\"", "")
                        fromNameTemp = fromNameTemp.replace("\"", "")
//                        StringUitl.isEmail(temp)
                        var user = User(fromAdressTemp, fromNameTemp, fromNameTemp)
                        (toAdressEdit.text as SpannableStringBuilder)
                                .append(methodContext.newSpannable(user))
                                .append(";")
                    }
                }
                toAdressEdit.post(Runnable {
                    var lineCount = toAdressEdit.lineCount
                    toAdressEdit.minHeight = resources.getDimension(R.dimen.x50).toInt() * lineCount
                })
                var fromAdressListCC = emailMessageEntity!!.cc.split(",")
                for (item in fromAdressListCC) {
                    if (item != "" && !item.contains(myAccount)) {
                        var drawable = getResources().getDrawable(R.mipmap.tabbar_arrow_upper)
                        drawable.setBounds(0, 0, 48, 48);
                        showCcAndBcc.setCompoundDrawables(drawable, null, null, null);
                        ccParent.visibility = View.VISIBLE
                        bccParent.visibility = View.VISIBLE
                        var fromNameTemp = ""
                        var fromAdressTemp = ""
                        if (item.indexOf("<") >= 0) {
                            fromNameTemp = item.substring(0, item.indexOf("<"))
                            fromAdressTemp = item.substring(item.indexOf("<") + 1, item.length - 1)
                        } else {
                            fromNameTemp = item.substring(0, item.indexOf("@"))
                            fromAdressTemp = item.substring(0, item.length)
                        }
                        fromNameTemp = fromNameTemp.replace("\"", "")
                        fromNameTemp = fromNameTemp.replace("\"", "")
                        fromNameTemp = fromNameTemp.replace("\"", "")
                        var user = User(fromAdressTemp, fromNameTemp, fromNameTemp)
                        (ccAdressEdit.text as SpannableStringBuilder)
                                .append(methodContext.newSpannable(user))
                                .append(";")
                    }
                }
                ccAdressEdit.post(Runnable {
                    var lineCount = ccAdressEdit.lineCount
                    ccAdressEdit.minHeight = resources.getDimension(R.dimen.x50).toInt() * lineCount
                })
            }

        }
        if (foward == 3) {
            var fromAdressList = emailMessageEntity!!.to_.split(",")
            for (item in fromAdressList) {
                if (item != "") {
                    var fromNameTemp = ""
                    var fromAdressTemp = ""
                    if (item.indexOf("<") >= 0) {
                        fromNameTemp = item.substring(0, item.indexOf("<"))
                        fromAdressTemp = item.substring(item.indexOf("<") + 1, item.length - 1)
                    } else {
                        fromNameTemp = item.substring(0, item.indexOf("@"))
                        fromAdressTemp = item.substring(0, item.length)
                    }
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    var user = User(fromAdressTemp, fromNameTemp, fromNameTemp)
                    (toAdressEdit.text as SpannableStringBuilder)
                            .append(methodContext.newSpannable(user))
                            .append(";")
                }
            }
            toAdressEdit.post(Runnable {
                var lineCount = toAdressEdit.lineCount
                toAdressEdit.minHeight = resources.getDimension(R.dimen.x50).toInt() * lineCount
            })
            var fromAdressListCC = emailMessageEntity!!.cc.split(",")
            for (item in fromAdressListCC) {
                if (item != "") {
                    var drawable = getResources().getDrawable(R.mipmap.tabbar_arrow_upper)
                    drawable.setBounds(0, 0, 48, 48);
                    showCcAndBcc.setCompoundDrawables(drawable, null, null, null);
                    ccParent.visibility = View.VISIBLE
                    bccParent.visibility = View.VISIBLE
                    var fromNameTemp = ""
                    var fromAdressTemp = ""
                    if (item.indexOf("<") >= 0) {
                        fromNameTemp = item.substring(0, item.indexOf("<"))
                        fromAdressTemp = item.substring(item.indexOf("<") + 1, item.length - 1)
                    } else {
                        fromNameTemp = item.substring(0, item.indexOf("@"))
                        fromAdressTemp = item.substring(0, item.length)
                    }
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    var user = User(fromAdressTemp, fromNameTemp, fromNameTemp)
                    (ccAdressEdit.text as SpannableStringBuilder)
                            .append(methodContext.newSpannable(user))
                            .append(";")
                }
            }
            ccAdressEdit.post(Runnable {
                var lineCount = ccAdressEdit.lineCount
                ccAdressEdit.minHeight = resources.getDimension(R.dimen.x50).toInt() * lineCount
            })
            var fromAdressListBCC = emailMessageEntity!!.bcc.split(",")
            for (item in fromAdressListBCC) {
                if (item != "") {
                    var drawable = getResources().getDrawable(R.mipmap.tabbar_arrow_upper)
                    drawable.setBounds(0, 0, 48, 48);
                    showCcAndBcc.setCompoundDrawables(drawable, null, null, null);
                    ccParent.visibility = View.VISIBLE
                    bccParent.visibility = View.VISIBLE
                    var fromNameTemp = ""
                    var fromAdressTemp = ""
                    if (item.indexOf("<") >= 0) {
                        fromNameTemp = item.substring(0, item.indexOf("<"))
                        fromAdressTemp = item.substring(item.indexOf("<") + 1, item.length - 1)
                    } else {
                        fromNameTemp = item.substring(0, item.indexOf("@"))
                        fromAdressTemp = item.substring(0, item.length)
                    }
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    fromNameTemp = fromNameTemp.replace("\"", "")
                    var user = User(fromAdressTemp, fromNameTemp, fromNameTemp)
                    (bccAdressEdit.text as SpannableStringBuilder)
                            .append(methodContext.newSpannable(user))
                            .append(";")
                }
            }
            bccAdressEdit.post(Runnable {
                var lineCount = ccAdressEdit.lineCount
                bccAdressEdit.minHeight = resources.getDimension(R.dimen.x50).toInt() * lineCount
            })
        }
        if (subjectText != null) {
            if (foward == 0) {
                subject.setText(getString(R.string.Reply) + ":" + subjectText)
            } else {
                subject.setText(subjectText)
            }
            subject.post(Runnable {
                var lineCount = subject.lineCount
                subject.minHeight = resources.getDimension(R.dimen.x50).toInt() * lineCount
            })

        }
        val selectionEnd = toAdressEdit.length()
        val selectionStart = 0
        val spans = toAdressEdit!!.getText()!!.getSpans(selectionStart, selectionEnd, User::class.java)
        var dd = ""

        var URLText = "<html><body style ='font-size:16px;'>" + emailMessageEntity!!.content + "</body></html>";
        if (emailMessageEntity!!.originalText != null && emailMessageEntity!!.originalText != "") {
            URLText = "<html><body style ='font-size:16px;'>" + emailMessageEntity!!.originalText + "</body></html>";
        }
        var needOp = false
        if (emailMessageEntity!!.content != null && emailMessageEntity!!.content.contains("<img")) {
            needOp = true
        }
        if (emailMessageEntity!!.originalText != null && emailMessageEntity!!.originalText.contains("<img")) {
            needOp = true;
        }
        if (needOp) {
            val webSettings = webView.getSettings()
            if (Build.VERSION.SDK_INT >= 19) {
                webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK)//加载缓存否则网络
            }
            if (Build.VERSION.SDK_INT >= 19) {
                webSettings.setLoadsImagesAutomatically(true)//图片自动缩放 打开
            } else {
                webSettings.setLoadsImagesAutomatically(false)//图片自动缩放 关闭
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)//软件解码
            }
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)//硬件解码
            webView.setLayerType(View.LAYER_TYPE_NONE, null);
            webSettings.javaScriptEnabled = true // 设置支持javascript脚本
            //webSettings.setTextSize(WebSettings.TextSize.LARGEST)
//        webSettings.setPluginState(WebSettings.PluginState.ON);
            webSettings.setSupportZoom(true)// 设置可以支持缩放
            webSettings.builtInZoomControls = true// 设置出现缩放工具 是否使用WebView内置的缩放组件，由浮动在窗口上的缩放控制和手势缩放控制组成，默认false

            webSettings.displayZoomControls = false//隐藏缩放工具
            webSettings.useWideViewPort = true// 扩大比例的缩放

            webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN//自适应屏幕
            webSettings.loadWithOverviewMode = true

            /* webSettings.databaseEnabled = true//
             webSettings.savePassword = true//保存密码
             webSettings.domStorageEnabled = true//是否开启本地DOM存储  鉴于它的安全特性（任何人都能读取到它，尽管有相应的限制，将敏感数据存储在这里依然不是明智之举），Android 默认是关闭该功能的。

             webView.setSaveEnabled(true)
             webView.setKeepScreenOn(true)*/
        }


        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title1: String?) {
                super.onReceivedTitle(view, title1)
                if (title1 != null) {
                    //title.text = title1
                }
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    KLog.i("进度：" + newProgress)
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                }
                super.onProgressChanged(view, newProgress)
            }

        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                //view.loadUrl(url)
                val intent = Intent()
                intent.action = "android.intent.action.VIEW"
                val url = Uri.parse(url)
                intent.data = url
                startActivity(intent)
                return true
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, error: SslError) {
                if (error.getPrimaryError() == SslError.SSL_DATE_INVALID
                        || error.getPrimaryError() == SslError.SSL_EXPIRED
                        || error.getPrimaryError() == SslError.SSL_INVALID
                        || error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
                    handler.proceed();
                } else {
                    handler.cancel();
                }
                super.onReceivedSslError(view, handler, error)
            }

            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                KLog.i("ddddddd")
                super.onReceivedHttpError(view, request, errorResponse)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                KLog.i("ddddddd")
                super.onReceivedError(view, request, error)
            }
        }

        if (foward == 3) {
            if (emailMessageEntity!!.originalText != null && emailMessageEntity!!.originalText != "") {
                re_main_editor.setHtml(emailMessageEntity!!.originalText)
            } else {
                re_main_editor.setHtml(emailMessageEntity!!.content)
            }

        } else {
            try {
                webView.loadDataWithBaseURL(null, URLText, "text/html", "utf-8", null);
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    private fun initAttachUI() {
        attachListEntity = arrayListOf<EmailAttachEntity>()
        var attachCount = false
        if (emailMeaasgeInfoData != null) {
            attachCount = emailMeaasgeInfoData!!.isContainerAttachment()
        }
        if (attachCount) {
            val save_dir = PathUtils.getInstance().filePath.toString() + "/"
            var addMenu = false
            var attachListData = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.menu_ + "_" + emailMeaasgeInfoData!!.msgId)).list()
            if (attachListData.size == 0) {
                addMenu = true
                attachListData = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.msgId)).list()
            }
            if (attachListEntityNode == null || attachListEntityNode.size == 0) {
                var isDownload = true
                if (attachListData.size > 0) {
                    var listAccath: ArrayList<MailAttachment> = ArrayList<MailAttachment>()
                    var i = 0;
                    for (attach in attachListData) {
                        var file = File(save_dir + attach.account + "_" + attach.msgId + "_" + attach.name)
                        if (addMenu) {
                            file = File(save_dir + attach.account + "_" + emailMeaasgeInfoData!!.menu_ + "_" + attach.msgId + "_" + attach.name)
                        }
                        if (!file.exists()) {
                            isDownload = false
                        }
                        attach.localPath = save_dir + attach.account + "_" + attach.msgId + "_" + attach.name
                        AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.update(attach)

                        var fileName = attach.name
                        if (fileName.contains("jpg") || fileName.contains("JPG") || fileName.contains("png")) {
                            val localMedia = LocalMedia()
                            localMedia.isCompressed = false
                            localMedia.duration = 0
                            localMedia.height = 100
                            localMedia.width = 100
                            localMedia.isChecked = false
                            localMedia.isCut = false
                            localMedia.mimeType = 0
                            localMedia.num = 0
                            localMedia.path = attach.localPath
                            localMedia.pictureType = "image/jpeg"
                            localMedia.setPosition(i)
                            localMedia.sortIndex = i
                            previewImages.add(localMedia)
                            ImagesObservable.getInstance().saveLocalMedia(previewImages, "chat")
                        }

                        i++

                    }

                } else {
                    isDownload = false
                }

                if (!isDownload) {
                    showProgressDialog(getString(R.string.Attachmentdownloading))
                    val emailReceiveClient = EmailReceiveClient(AppConfig.instance.emailConfig())
                    if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                        emailReceiveClient
                                .imapDownloadEmailAttach(this@EmailSendActivity, object : GetAttachCallback {
                                    override fun gainSuccess(messageList: List<MailAttachment>, count: Int) {
                                        //tipDialog.dismiss()
                                        closeProgressDialog()
                                        runOnUiThread {

                                            var iFlag = 0;
                                            for (attachItem in messageList) {
                                                var attachListTemp = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.menu_ + "_" + emailMeaasgeInfoData!!.msgId), EmailAttachEntityDao.Properties.Name.eq(attachItem.name)).list()
                                                if (attachListTemp.size == 0) {
                                                    attachListTemp = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.msgId)).list()

                                                }
                                                if (attachListTemp == null || attachListTemp.size == 0) {
                                                    var eamilAttach = EmailAttachEntity()
                                                    eamilAttach.account = AppConfig.instance.emailConfig().account
                                                    eamilAttach.msgId = emailMeaasgeInfoData!!.menu_ + "_" + emailMeaasgeInfoData!!.msgId
                                                    eamilAttach.name = attachItem.name
                                                    eamilAttach.data = attachItem.byt
                                                    eamilAttach.hasData = true
                                                    eamilAttach.isCanDelete = false
                                                    var savePath = save_dir + eamilAttach.account + "_" + eamilAttach.msgId + "_" + eamilAttach.name
                                                    eamilAttach.localPath = savePath
                                                    AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.insert(eamilAttach)

                                                    var fileName = eamilAttach.name
                                                    if (fileName.contains("jpg") || fileName.contains("JPG") || fileName.contains("png")) {
                                                        val localMedia = LocalMedia()
                                                        localMedia.isCompressed = false
                                                        localMedia.duration = 0
                                                        localMedia.height = 100
                                                        localMedia.width = 100
                                                        localMedia.isChecked = false
                                                        localMedia.isCut = false
                                                        localMedia.mimeType = 0
                                                        localMedia.num = 0
                                                        localMedia.path = eamilAttach.localPath
                                                        localMedia.pictureType = "image/jpeg"
                                                        localMedia.setPosition(iFlag)
                                                        localMedia.sortIndex = iFlag
                                                        previewImages.add(localMedia)
                                                        ImagesObservable.getInstance().saveLocalMedia(previewImages, "chat")
                                                    }

                                                    iFlag++
                                                }
                                            }

                                            attachListData = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.menu_ + "_" + emailMeaasgeInfoData!!.msgId)).list()
                                            if (attachListData.size == 0) {
                                                attachListData = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.msgId)).list()

                                            }
                                            for (item in attachListData) {
                                                item.isHasData = true
                                                item.isCanDelete = true
                                            }
                                            attachListEntity.addAll(attachListData)
                                            updataAttachUI()
                                        }
                                    }

                                    override fun gainFailure(errorMsg: String) {
                                        //tipDialog.dismiss()
                                        closeProgressDialog()
                                        Toast.makeText(this@EmailSendActivity, getString(R.string.Attachment_download_failed), Toast.LENGTH_SHORT).show()
                                    }
                                }, menu, emailMeaasgeInfoData!!.msgId, save_dir, emailMeaasgeInfoData!!.aesKey)
                    } else {
                        var gmailService = GmailQuickstart.getGmailService(AppConfig.instance, ConstantValue.currentEmailConfigEntity!!.account);
                        emailReceiveClient
                                .gmailDownloadEmailAttach(this@EmailSendActivity, object : GetAttachCallback {
                                    override fun gainSuccess(messageList: List<MailAttachment>, count: Int) {
                                        //tipDialog.dismiss()
                                        closeProgressDialog()
                                        runOnUiThread {

                                            var iFlag = 0;
                                            for (attachItem in messageList) {
                                                var attachListTemp = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.menu_ + "_" + emailMeaasgeInfoData!!.msgId), EmailAttachEntityDao.Properties.Name.eq(attachItem.name)).list()
                                                if (attachListTemp.size == 0) {
                                                    attachListTemp = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.msgId)).list()

                                                }
                                                if (attachListTemp == null || attachListTemp.size == 0) {
                                                    var eamilAttach = EmailAttachEntity()
                                                    eamilAttach.account = AppConfig.instance.emailConfig().account
                                                    eamilAttach.msgId = emailMeaasgeInfoData!!.menu_ + "_" + emailMeaasgeInfoData!!.msgId
                                                    eamilAttach.name = attachItem.name
                                                    eamilAttach.data = attachItem.byt
                                                    eamilAttach.hasData = true
                                                    eamilAttach.isCanDelete = false
                                                    var savePath = save_dir + eamilAttach.account + "_" + eamilAttach.msgId + "_" + eamilAttach.name
                                                    eamilAttach.localPath = savePath
                                                    AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.insert(eamilAttach)

                                                    var fileName = eamilAttach.name
                                                    if (fileName.contains("jpg") || fileName.contains("JPG") || fileName.contains("png")) {
                                                        val localMedia = LocalMedia()
                                                        localMedia.isCompressed = false
                                                        localMedia.duration = 0
                                                        localMedia.height = 100
                                                        localMedia.width = 100
                                                        localMedia.isChecked = false
                                                        localMedia.isCut = false
                                                        localMedia.mimeType = 0
                                                        localMedia.num = 0
                                                        localMedia.path = eamilAttach.localPath
                                                        localMedia.pictureType = "image/jpeg"
                                                        localMedia.setPosition(iFlag)
                                                        localMedia.sortIndex = iFlag
                                                        previewImages.add(localMedia)
                                                        ImagesObservable.getInstance().saveLocalMedia(previewImages, "chat")
                                                    }

                                                    iFlag++
                                                }
                                            }

                                            attachListData = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.menu_ + "_" + emailMeaasgeInfoData!!.msgId)).list()
                                            if (attachListData.size == 0) {
                                                attachListData = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.msgId)).list()

                                            }
                                            for (item in attachListData) {
                                                item.isHasData = true
                                                item.isCanDelete = true
                                            }
                                            attachListEntity.addAll(attachListData)
                                            updataAttachUI()
                                        }
                                    }

                                    override fun gainFailure(errorMsg: String) {
                                        //tipDialog.dismiss()
                                        closeProgressDialog()
                                        Toast.makeText(this@EmailSendActivity, getString(R.string.Attachment_download_failed), Toast.LENGTH_SHORT).show()
                                    }
                                }, menu, emailMeaasgeInfoData!!.msgId, save_dir, emailMeaasgeInfoData!!.aesKey, gmailService, "me")
                    }

                } else {
                    attachListData = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.menu_ + "_" + emailMeaasgeInfoData!!.msgId)).list()
                    if (attachListData.size == 0) {
                        attachListData = AppConfig.instance.mDaoMaster!!.newSession().emailAttachEntityDao.queryBuilder().where(EmailAttachEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.msgId)).list()

                    }
                    for (item in attachListData) {
                        item.isHasData = true
                        item.isCanDelete = true
                    }
                    attachListEntity.addAll(attachListData)
                    updataAttachUI()
                }
            } else {
                for (item in attachListEntityNode) {
                    item.isHasData = true
                    item.isCanDelete = true
                }
                attachListEntity.addAll(attachListEntityNode)
                updataAttachUI()
            }

        } else {
            updataAttachUI()
        }
    }

    fun updataAttachUI() {

        var emailAttachEntity = EmailAttachEntity()
        emailAttachEntity.isHasData = false
        emailAttachEntity.isCanDelete = false
        attachListEntity.add(emailAttachEntity)
        emaiAttachAdapter = EmaiAttachAdapter(attachListEntity)
        emaiAttachAdapter!!.setOnItemLongClickListener { adapter, view, position ->

            true
        }
        recyclerViewAttach.setLayoutManager(GridLayoutManager(AppConfig.instance, 2));
        recyclerViewAttach.adapter = emaiAttachAdapter

        emaiAttachAdapter!!.setOnItemClickListener { adapter, view, position ->
            /* var intent = Intent(activity!!, ConversationActivity::class.java)
             intent.putExtra("user", coversationListAdapter!!.getItem(position)!!.userEntity)
             startActivity(intent)*/
        }
        emaiAttachAdapter!!.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.deleteBtn -> {
                    emaiAttachAdapter!!.remove(position)
                    emaiAttachAdapter!!.notifyDataSetChanged();
                    if (emaiAttachAdapter!!.itemCount > 1) {
                        addSubjectImg.setImageResource(R.mipmap.tabbar_attach1_selected)
                        addSubject.text = (emaiAttachAdapter!!.itemCount - 1).toString()
                        addSubject.visibility = View.GONE
                    } else {
                        addSubjectImg.setImageResource(R.mipmap.tabbar_attach1_unselected)
                        addSubject.text = ""
                        addSubject.visibility = View.GONE
                    }
                }
                R.id.iv_add -> {
                    hideSoftKeyboard()
                    var menuArray = arrayListOf<String>(getString(R.string.attach_picture), getString(R.string.attach_take_pic), getString(R.string.attach_video), getString(R.string.attach_file))
                    var iconArray = arrayListOf<String>("sheet_album", "sheet_camera", "sheet_video", "sheet_file")
                    PopWindowUtil.showPopAttachMenuWindow(this@EmailSendActivity, itemParent, menuArray, iconArray, object : PopWindowUtil.OnSelectListener {
                        override fun onSelect(position: Int, obj: Any) {
                            KLog.i("" + position)
                            when (position) {
                                0 -> {
                                    selectPicFromLocal()
                                }
                                1 -> {
                                    AndPermission.with(AppConfig.instance)
                                            .requestCode(101)
                                            .permission(
                                                    Manifest.permission.CAMERA
                                            )
                                            .callback(permission)
                                            .start()
                                }
                                2 -> {
                                    AndPermission.with(AppConfig.instance)
                                            .requestCode(101)
                                            .permission(
                                                    Manifest.permission.CAMERA
                                            )
                                            .callback(permissionVideo)
                                            .start()
                                }
                                3 -> {
//                                    val i = Intent(Intent.ACTION_GET_CONTENT)
//                                    i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
////                                    i.addCategory(Intent.CATEGORY_OPENABLE)
//                                    i.type = "*/*"
//                                    startActivityForResult(Intent.createChooser(i, null), REQUEST_CODE_FILE)

//                                    startActivityForResult(Intent(this@EmailSendActivity, FileChooseActivity::class.java).putExtra("fileType", 2), REQUEST_CODE_FILE)
                                    startActivityForResult(Intent(this@EmailSendActivity, EmailSelectAttachmentActivity::class.java), REQUEST_CODE_FILE)
                                    overridePendingTransition(R.anim.activity_translate_in, R.anim.activity_translate_out)
                                }

                            }
                        }

                    })
                }
            }
        }
    }

    fun showImagList(showIndex: Int) {
        val selectedImages = java.util.ArrayList<LocalMedia>()
        val previewImages = ImagesObservable.getInstance().readLocalMedias("chat")
        if (previewImages != null && previewImages.size > 0) {

            val intentPicturePreviewActivity = Intent(this, PicturePreviewActivity::class.java)
            val bundle = Bundle()
            //ImagesObservable.getInstance().saveLocalMedia(previewImages);
            bundle.putSerializable(PictureConfig.EXTRA_SELECT_LIST, selectedImages as Serializable)
            bundle.putInt(PictureConfig.EXTRA_POSITION, showIndex)
            bundle.putString("from", "chat")
            intentPicturePreviewActivity.putExtras(bundle)
            startActivity(intentPicturePreviewActivity)
        }
    }

    private fun sendCheck(flag: Boolean) {
        contactMapList = HashMap<String, String>()
        isSendCheck = flag
        var toAdress = getEditText(toAdressEdit)
        toAdress = toAdress.replace(",,", "")
        var toAdressArr = toAdress.split(",");
        var num = 0
        if (flag == true && toAdress == "") {
            toast(R.string.The_recipient_cant_be_empty)
            return
        }
        var subjectStr = subject.getText().toString()
        if (flag == true && subjectStr == "") {
            toast(R.string.The_subject_cant_be_empty)
            return
        }
        var toAdressBase64 = ""
        for (item in toAdressArr) {
            if (item != "") {
                var temp = item.trim()
                temp = temp.toLowerCase()
                var isEmail = StringUitl.isEmail(temp)
                if (!isEmail) {
                    toast(temp + " " + getString(R.string.Some_addresses_are_illegal))
                    return;
                }
                num++;
                toAdressBase64 += RxEncodeTool.base64Encode2String(temp.toByteArray()) + ","
            }
        }
        if (toAdressBase64.length > 0) {
            toAdressBase64 = toAdressBase64.substring(0, toAdressBase64.length - 1)
        }

        var toAdressBase64CC = ""
        var ccAdress = getEditText(ccAdressEdit)
        ccAdress = ccAdress.replace(",,", "")
        var ccAdressArr = ccAdress.split(",");
        for (item in ccAdressArr) {
            if (item != "") {
                var temp = item.trim()
                temp = temp.toLowerCase()
                var isEmail = StringUitl.isEmail(temp)
                if (!isEmail) {
                    toast(temp + " " + getString(R.string.Some_addresses_are_illegal))
                    return;
                }
                num++;
                toAdressBase64CC += RxEncodeTool.base64Encode2String(temp.toByteArray()) + ","
            }
        }
        if (toAdressBase64CC.length > 0) {
            toAdressBase64CC = toAdressBase64CC.substring(0, toAdressBase64CC.length - 1)
        }
        var toAdressBase64BCC = ""
        var bccAdress = getEditText(bccAdressEdit)
        bccAdress.replace(",,", "")
        var bccAdressArr = bccAdress.split(",");
        for (item in bccAdressArr) {
            if (item != "") {
                var temp = item.trim()
                temp = temp.toLowerCase()
                var isEmail = StringUitl.isEmail(temp)
                if (!isEmail) {
                    toast(temp + getString(R.string.Some_addresses_are_illegal))
                    return;
                }
                num++;
                toAdressBase64BCC += RxEncodeTool.base64Encode2String(temp.toByteArray()) + ","
            }
        }
        if (toAdressBase64BCC.length > 0) {
            toAdressBase64BCC = toAdressBase64BCC.substring(0, toAdressBase64BCC.length - 1)
        }
        addressBase64 = toAdressBase64
        if (toAdressBase64CC != "") {
            addressBase64 += "," + toAdressBase64CC
        }
        if (toAdressBase64BCC != "") {
            addressBase64 += "," + toAdressBase64BCC
        }
        if (addressBase64 == "") {
            lockTips.visibility = View.GONE
            return
        }
        if (this.flag == 100) {
            FireBaseUtils.logEvent(this, FireBaseUtils.FIR_INVATE_FRIEND)
        }
        if (isSendCheck) {
            runOnUiThread {
                showProgressDialog(getString(R.string.waiting))
            }
        }
        var checkmailUkey = CheckmailUkey(num, 1, addressBase64)
        AppConfig.instance.getPNRouterServiceMessageSender().send(BaseData(6, checkmailUkey))
    }

    /**
     * 发送邮件
     */
    private fun sendEmail(send: Boolean) {
        /*if(BuildConfig.DEBUG)
         {
             contactMapList = HashMap<String, String>()
         }*/
        var needEncryptEmail = false;
        var fileKey = RxEncryptTool.generateAESKey()
        if (userPassWord != "") {
            var len = userPassWord.length
            if (len > 32) {
                userPassWord = userPassWord.substring(0, 32)
            } else if (len < 32) {
                var need = 32 - len;
                for (index in 1..need) {
                    userPassWord += "0"
                }
            }
            fileKey = userPassWord;
        }
        var contentHtml = re_main_editor.html
        if (flag == 1 && emailMeaasgeInfoData != null && emailMeaasgeInfoData!!.content != null) {
            var from = emailMeaasgeInfoData!!.from_
            var toStr = emailMeaasgeInfoData!!.to_
            var centerStr = " <br />" +
                    " <br />" +
                    " <br />" +
                    "<div style=\"background: #f2f2f2;\">" +
                    getString(R.string.Original_mail) +
                    "   <br />" + getString(R.string.From) + "：&quot;" + from + "&quot;" +
                    "   <br />" + getString(R.string.To) + "：&quot;" + toStr + "&quot;" +
                    "   <br />" + getString(R.string.Subject) + "：&quot;" + emailMeaasgeInfoData!!.subject_ + "&quot;" +
                    "   <br />" + getString(R.string.Date) + "：" + emailMeaasgeInfoData!!.date_ +
                    "  </div>" +
                    "   <br />" +
                    "   <br />";
            contentHtml += centerStr
            if (emailMeaasgeInfoData!!.originalText != "") {
                contentHtml += emailMeaasgeInfoData!!.originalText
            } else {
                contentHtml += emailMeaasgeInfoData!!.content
            }
            if (contentHtml.contains("id=\"newconfidantkey")) {
                var beginFlag = contentHtml.indexOf("<span style=\"display:none\" id=\"newconfidantkey")
                if (beginFlag < 0) {
                    beginFlag = contentHtml.indexOf("<span style=\"display:none\" id=\"newconfidantkey")
                }
                if (beginFlag >= 0) {
                    contentHtml = contentHtml.substring(0, beginFlag)
                }
            }

        }
        var needTipsShow = true;
        if (contentHtml.contains("id=\"newmyconfidantbegin")) {
            var endIndex = contentHtml.indexOf("<div id=\"newmyconfidantbegin")
            if (endIndex > 0) {
                contentHtml = contentHtml.substring(0, endIndex)
            }
        }

        //解析cid资源
        var cidNameList = ""
        var cidList = ""
        var uuid = (System.currentTimeMillis() / 1000).toString()
        if (emailMeaasgeInfoData != null) {
            var citList = AppConfig.instance.mDaoMaster!!.newSession().emailCidEntityDao.queryBuilder().where(EmailCidEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.menu_ + "_" + emailMeaasgeInfoData!!.msgId)).list()
            if (citList.size == 0) {
                citList = AppConfig.instance.mDaoMaster!!.newSession().emailCidEntityDao.queryBuilder().where(EmailCidEntityDao.Properties.MsgId.eq(emailMeaasgeInfoData!!.msgId)).list()
            }
            for (item in citList) {
                val save_dir = PathUtils.getInstance().filePath.toString() + "/"
                var savePath = save_dir + AppConfig.instance.emailConfig().account + "_" + emailMeaasgeInfoData!!.menu_ + "_" + emailMeaasgeInfoData!!.msgId + "_" + item.name
                if (!contentHtml.equals("")) {
                    contentHtml = replaceImgCidByLocalPath(contentHtml, item.cid, savePath, uuid)
                }
                if (item.localPath != null) {
                    if (contactMapList.size == needSize) {
                        val base58files_dir = PathUtils.getInstance().tempPath.toString() + "/" + item.name
                        val code = FileUtil.copySdcardToxFileAndEncrypt(item.localPath, base58files_dir, fileKey.substring(0, 16))
                        if (code == 1) {
                            cidNameList += base58files_dir + ","

                        }
                    } else {
                        cidNameList += item.localPath + ","
                    }
                    cidList += item.cid + ","
                }

            }
            if (cidNameList.length > 0) {
                cidNameList = cidNameList.substring(0, cidNameList.length - 1)
            }
            if (cidList.length > 0) {
                cidList = cidList.substring(0, cidList.length - 1)
            }
        }

        if (contactMapList.size == needSize) {
            val contentBuffer = contentHtml.toByteArray()
            var fileKey16 = fileKey.substring(0, 16)
            Log.i("fileKey16", fileKey16)
            if (!contentHtml.equals("")) {
                KLog.i("加密前为：" + contentHtml)
                var contentBufferMiStr = RxEncodeTool.base64Encode2String(AESToolsCipher.aesEncryptBytes(contentBuffer, fileKey16!!.toByteArray(charset("UTF-8"))))
                contentHtml = contentBufferMiStr
                KLog.i("解密结果为：" + String(AESToolsCipher.aesDecryptBytes(RxEncodeTool.base64Decode(contentBufferMiStr), fileKey16!!.toByteArray(charset("UTF-8")))))
            }
        }
        var userId = SpUtil.getString(this, ConstantValue.userId, "")
        if (userPassWord == "") {
            var confidantKey = "";
            for (item in contactMapList) {
                var account = item.key
                var friendMiPublicKey = item.value
                var dstKey = String(RxEncodeTool.base64Encode(LibsodiumUtil.EncryptShareKey(fileKey, friendMiPublicKey)))
                confidantKey += account + "&&" + dstKey + "##";
            }
            if (confidantKey != "") {
                var myAccountBase64 = String(RxEncodeTool.base64Encode(AppConfig.instance.emailConfig().account))
                var dstKey = String(RxEncodeTool.base64Encode(LibsodiumUtil.EncryptShareKey(fileKey, ConstantValue.libsodiumpublicMiKey!!)))
                confidantKey += myAccountBase64 + "&&" + dstKey;
            }

            if (contactMapList.size == needSize) {
                contentHtml += "<span style=\"display:none\" id=\"" + "newconfidantkey" + confidantKey + "\"></span>"; //confidantkey
            }
        } else {
            contentHtml += "<span style=\"display:none\" id=\"" + "newconfidantpass" + userPassWordTips + "\"></span>"; //手动加密标记
        }
        contentHtml += "<span style=\"display:none\" id=\"newconfidantuserid" + userId + "\"></span>";
        var endStr = "<div id=\"newmyconfidantbegin\">" +
                "<br />" +
                " <br />" +
                " <br />" +
                "<span>" +
                getString(R.string.sendfromconfidant) +
                "</span>" +
                "</div>"


        var myAccountStr = ConstantValue.currentEmailConfigEntity!!.account
        var addEnd = "<div id=\"box\"> " +
                "   <style type=\"text/css\">/*<![CDATA[*/* {padding: 0;border: 0;outline: 0;margin: 0;}a {    text-decoration: none;    background-color: transparent}a:hover,a:active {    outline-width: 0;    text-decoration: none}#box {width: 100vw;box-sizing: border-box;}#box section {padding: 16px;}#box header .Star {float: right;}.userHead {display: flex;width: 100%;    box-sizing: border-box;    border-bottom: 1px solid #e6e6e6;}.userHeadA {width: 44px;height: 44px;padding: 18px 0;}.userHeadB {width: 240px;height: 44px;padding: 18px 0;outline: 0px solid #ccc;}.userHeadC {flex: 1;    text-align: right;height: 44px;padding: 18px 0;outline: 0px solid #ccc;}.userHeadAimg {width: 44px;height: 44px;    border-radius: 22px;}.userHeadBdate {color: #ccc;    margin-left: 8px;}.rowDiv {padding: 20px 0;    text-align: center;    border-bottom: 1px solid #e6e6e6;}button {background: rgba(102, 70, 247, 1);    border-radius: 7px;color: #fff;}.rowDiv3Btn {padding: 12px 34px;background: rgba(102, 70, 247, 1);    border-radius: 7px;color: #fff;} .jusCenter {display: flex;justify-content: center;align-items: center;} .rowDiv h3 {    font-size: 18px;    line-height: 18px;}#box p {line-height: 20px;font-size: 12px;}#box h3 {line-height: 40px;}.h3logo {" +
                "            position: relative;" +
                "            top: 5px;" +
                "            width: 24px;" +
                "            margin-right: 5px;" +
                "        }/*]]>*/</style> " +
                "   <section> " +
                "    <div class=\"rowDiv\"> " +
                "    </div> " +
                "    <div class=\"rowDiv\" style=\"border: 0;\"> " +
                "     <p>I’m using Confidant to send and receive secure emails.&nbsp;Click the&nbsp;link below to decrypt and view&nbsp;my&nbsp;message.</p> " +
                "    </div>" +
                "    <div class=\"rowDiv jusCenter\" style=\"text-align: center;padding: 0;\">" +
                "     <div style=\"padding:15px;\">" +
                "      <a href=\"https://apps.apple.com/us/app/my-confidant/id1456735273?l=zh&amp;ls=1\"><img width=\'140\' src=\'https://confidant.oss-cn-hongkong.aliyuncs.com/images/apps_tore.png\'></a>" +
                "     </div>" +
                "     <div style=\"padding:15px;\">" +
                "      <a href=\"https://play.google.com/store/apps/details?id=com.stratagile.pnrouter\"><img width=\'140\' src=\'https://confidant.oss-cn-hongkong.aliyuncs.com/images/google_play.png\'></a>" +
                "     </div>" +
                "    </div>  " +
                "   </section>" +
                "  </div>";

        if (userPassWord == "") {
            if (contactMapList.size == needSize)//需要加密
            {
                needEncryptEmail = true;
            }
        } else {//手动加密
            needEncryptEmail = true;
        }
        if (needEncryptEmail) {
            contentHtml += endStr
        } else {
            contentHtml += addEnd
        }
        var toAdress = getEditText(toAdressEdit)
        var ccAdress = getEditText(ccAdressEdit)
        var bccAdress = getEditText(bccAdressEdit)


        var toAdressArr = toAdress.split(",")
        for (item in toAdressArr) {
            if (item != "") {
                if (!StringUitl.checkEmail(item)) {
                    toast(R.string.Illegal_address)
                    return
                }
                var name = item.substring(0, item.indexOf("@"))
                name = name.replace("\"", "")
                name = name.replace("\"", "")
                var account = item
                account = account.toLowerCase()
                var localEmailContacts = AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.queryBuilder().where(EmailContactsEntityDao.Properties.Account.eq(account)).list()
                if (localEmailContacts.size == 0) {
                    var emailContactsEntity = EmailContactsEntity();
                    emailContactsEntity.name = name
                    emailContactsEntity.account = account
                    emailContactsEntity.createTime = System.currentTimeMillis()
                    AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.insert(emailContactsEntity)
                } else {
                    var emailContactsEntity = localEmailContacts.get(0)
                    emailContactsEntity.createTime = System.currentTimeMillis()
                    AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.update(emailContactsEntity)
                }
            }

        }
        var ccAdressArr = ccAdress.split(",")
        for (item in ccAdressArr) {
            if (item != "") {
                var name = item.substring(0, item.indexOf("@"))
                name = name.replace("\"", "")
                name = name.replace("\"", "")
                var account = item
                account = account.toLowerCase()
                var localEmailContacts = AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.queryBuilder().where(EmailContactsEntityDao.Properties.Account.eq(account)).list()
                if (localEmailContacts.size == 0) {
                    var emailContactsEntity = EmailContactsEntity();
                    emailContactsEntity.name = name
                    emailContactsEntity.account = account
                    emailContactsEntity.createTime = System.currentTimeMillis()
                    AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.insert(emailContactsEntity)
                } else {
                    var emailContactsEntity = localEmailContacts.get(0)
                    emailContactsEntity.createTime = System.currentTimeMillis()
                    AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.update(emailContactsEntity)
                }
            }

        }
        var bccAdressArr = bccAdress.split(",")
        for (item in bccAdressArr) {
            if (item != "") {
                var name = item.substring(0, item.indexOf("@"))
                name = name.replace("\"", "")
                name = name.replace("\"", "")
                var account = item
                account = account.toLowerCase()
                var localEmailContacts = AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.queryBuilder().where(EmailContactsEntityDao.Properties.Account.eq(account)).list()
                if (localEmailContacts.size == 0) {
                    var emailContactsEntity = EmailContactsEntity();
                    emailContactsEntity.name = name
                    emailContactsEntity.account = account
                    emailContactsEntity.createTime = System.currentTimeMillis()
                    AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.insert(emailContactsEntity)
                } else {
                    var emailContactsEntity = localEmailContacts.get(0)
                    emailContactsEntity.createTime = System.currentTimeMillis()
                    AppConfig.instance.mDaoMaster!!.newSession().emailContactsEntityDao.update(emailContactsEntity)
                }
            }

        }
        if (toAdress == "") {
            toast(R.string.The_recipient_cant_be_empty)
            return
        }
        if (userPassWord != "" && contentHtml == "") {
            toast(R.string.The_body_cannot_be_empty)
            return
        }
        var attachList = ""
        var emaiAttachAdapterList = emaiAttachAdapter!!.data
        for (item in emaiAttachAdapterList) {
            if (item.localPath != null) {
                if (contactMapList.size == needSize) {
                    val base58files_dir = PathUtils.getInstance().tempPath.toString() + "/" + item.name
                    val code = FileUtil.copySdcardToxFileAndEncrypt(item.localPath, base58files_dir, fileKey.substring(0, 16))
                    if (code == 1) {
                        attachList += base58files_dir + ","
                    }
                } else {
                    attachList += item.localPath + ","
                }


            }

        }
        if (attachList.length > 0) {
            attachList = attachList.substring(0, attachList.length - 1)
        }
        if (galleryPath != "") {
            attachList = galleryPath + "circleCode.jpg"
        }
        val emailSendClient = EmailSendClient(AppConfig.instance.emailConfig())
        var myAccount = AppConfig.instance.emailConfig().account
        var name = myAccount.substring(0, myAccount.indexOf("@"))
        if (AppConfig.instance.emailConfig().name != null && AppConfig.instance.emailConfig().name != "") {
            name = AppConfig.instance.emailConfig().name;
        }
        var subjectStr = emailSendClient.getUTFStr(subject.getText().toString())
        if (send) {
            FireBaseUtils.logEvent(this, FireBaseUtils.FIR_EMAIL_SEND)
            runOnUiThread {
                showProgressDialog(getString(R.string.Sending))
            }
            var drafts = ""
            var draftsId = ""
            if (foward == 3) {
                drafts = ConstantValue.currentEmailConfigEntity!!.drafMenu
                draftsId = emailMeaasgeInfoData!!.msgId
            }
            var myAccount = ConstantValue.currentEmailConfigEntity!!.account
            var addBefore = "<div id=\"box\"> " +
                    "   <style type=\"text/css\">/*<![CDATA[*/* {padding: 0;border: 0;outline: 0;margin: 0;}a {    text-decoration: none;    background-color: transparent}a:hover,a:active {    outline-width: 0;    text-decoration: none}#box {width: 100vw;box-sizing: border-box;}#box section {padding: 16px;}#box header .Star {float: right;}.userHead {display: flex;width: 100%;    box-sizing: border-box;    border-bottom: 1px solid #e6e6e6;}.userHeadA {width: 44px;height: 44px;padding: 18px 0;}.userHeadB {width: 240px;height: 44px;padding: 18px 0;outline: 0px solid #ccc;}.userHeadC {flex: 1;    text-align: right;height: 44px;padding: 18px 0;outline: 0px solid #ccc;}.userHeadAimg {width: 44px;height: 44px;    border-radius: 22px;}.userHeadBdate {color: #ccc;    margin-left: 8px;}.rowDiv {padding: 20px 0;    text-align: center;    border-bottom: 1px solid #e6e6e6;}button {background: rgba(102, 70, 247, 1);    border-radius: 7px;color: #fff;}.rowDiv3Btn {padding: 12px 34px;background: rgba(102, 70, 247, 1);    border-radius: 7px;color: #fff;} .jusCenter {display: flex;justify-content: center;align-items: center;} .rowDiv h3 {    font-size: 18px;    line-height: 18px;}#box p {line-height: 20px;font-size: 12px;}#box h3 {line-height: 40px;}.h3logo {" +
                    "            position: relative;" +
                    "            top: 5px;" +
                    "            width: 24px;" +
                    "            margin-right: 5px;" +
                    "        }/*]]>*/</style> " +
                    "   <section> " +
                    "    <div class=\"rowDiv\"> " +
                    "     <h3><img class=\"h3logo\" src=\"https://confidant.oss-cn-hongkong.aliyuncs.com/images/confidant_logo_n.png\" />Encrypted Email</h3> " +
                    "     <p>Encrypted email client and beyond - your comprehensive privacy&nbsp;protection tool</p> " +
                    "    </div> " +
                    "    <div class=\"rowDiv\" style=\"border: 0;\"> " +
                    "     <p style=\"font-size: 14px;\">You just received a secure message from</p> " +
                    "     <h3 style=\"color:#6646F7\">" + myAccount + "</h3> " +
                    "     <p>I’m using Confidant to send and receive secure emails. Download and install Confidant to decrypt and read the email content via the link below.</p> " +
                    "    </div>" +
                    "    <div class=\"rowDiv jusCenter\" style=\"text-align: center;padding: 0;\">" +
                    "     <div style=\"padding:15px;\">" +
                    "      <a href=\"https://apps.apple.com/us/app/my-confidant/id1456735273?l=zh&amp;ls=1\"><img width=\'140\' src=\'https://confidant.oss-cn-hongkong.aliyuncs.com/images/apps_tore.png\'></a>" +
                    "     </div>" +
                    "     <div style=\"padding:15px;\">" +
                    "      <a href=\"https://play.google.com/store/apps/details?id=com.stratagile.pnrouter\"><img width=\'140\' src=\'https://confidant.oss-cn-hongkong.aliyuncs.com/images/google_play.png\'></a>" +
                    "     </div>" +
                    "    </div>  " +
                    "   </section>" +
                    "  </div>";
            if (userPassWord == "") {
                if (contactMapList.size == needSize)//需要加密
                {
                    var contentHtmlBase64 = String(RxEncodeTool.base64Encode(contentHtml))
                    contentHtml = addBefore + "<span style=\"display:none\" id=\"" + "newconfidantcontent" + contentHtmlBase64 + "\"></span>";
                }
            } else {//手动加密
                var contentHtmlBase64 = String(RxEncodeTool.base64Encode(contentHtml))
                contentHtml = addBefore + "<span style=\"display:none\" id=\"" + "newconfidantcontent" + contentHtmlBase64 + "\"></span>";
            }
            if (InviteURLText != "") {
                contentHtml = InviteURLText;
            }
            if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                emailSendClient
                        .setTo(toAdress)                //收件人的邮箱地址
                        .setCc(ccAdress)
                        .setBcc(bccAdress)
                        .setNickname(name)                                    //发件人昵称
                        .setSubject(subject.getText().toString())             //邮件标题
                        .setContent(contentHtml)              //邮件正文
                        .setCidPath(cidNameList)                 //cid资源
                        .setCid(cidList)
                        .setUUID(uuid)
                        .setAttach(attachList)              //附件
                        .sendAsyn(this, object : GetSendCallback {
                            override fun sendSuccess() {
                                runOnUiThread {
                                    closeProgressDialog()
                                    var emailConfigEntityChooseList = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
                                    if (emailConfigEntityChooseList.size > 0) {
                                        var emailConfigEntityChoose = emailConfigEntityChooseList.get(0)
                                        emailConfigEntityChoose.sendMenuRefresh = true
                                        AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.update(emailConfigEntityChoose)
                                    }
                                    if (foward == 0 && emailMeaasgeInfoData != null && emailMeaasgeInfoData!!.userId != null && emailMeaasgeInfoData!!.userId != "") {
                                        var selfUserId = SpUtil.getString(AppConfig.instance, ConstantValue.userId, "")
                                        var emailId = RxEncodeTool.base64Encode2String(ConstantValue.currentEmailConfigEntity!!.account.toByteArray())
                                        var AddFriendsAutoReq = AddFriendsAutoReq(1, selfUserId!!, emailMeaasgeInfoData!!.userId, emailId)
                                        var sendData = BaseData(6, AddFriendsAutoReq);
                                        if (ConstantValue.isWebsocketConnected) {
                                            AppConfig.instance.getPNRouterServiceMessageSender().send(sendData)
                                        } else if (ConstantValue.isToxConnected) {
                                            var baseData = sendData
                                            var baseDataJson = baseData.baseDataToJson().replace("\\", "")
                                            if (ConstantValue.isAntox) {
                                                //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                                                //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
                                            } else {
                                                ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.currentRouterId.substring(0, 64))
                                            }
                                        }
                                    }

                                    var addFriendReq = MailSendNotice(addressBase64)
                                    var sendData = BaseData(6, addFriendReq);
                                    if (ConstantValue.isWebsocketConnected) {
                                        AppConfig.instance.getPNRouterServiceMessageSender().send(sendData)
                                    } else if (ConstantValue.isToxConnected) {
                                        var baseData = sendData
                                        var baseDataJson = baseData.baseDataToJson().replace("\\", "")
                                        if (ConstantValue.isAntox) {
                                            //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                                            //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
                                        } else {
                                            ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.currentRouterId.substring(0, 64))
                                        }
                                    }
                                    EventBus.getDefault().post(SendEmailSuccess(positionIndex))
                                    Toast.makeText(this@EmailSendActivity, R.string.success, Toast.LENGTH_SHORT).show()

                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }

                            override fun sendFailure(errorMsg: String) {
                                runOnUiThread {
                                    closeProgressDialog()
                                }
                                try {
                                    Islands.ordinaryDialog(this@EmailSendActivity)
                                            .setText(null, getString(R.string.error) + ":" + errorMsg)
                                            .setButton(getString(R.string.close), null, null)
                                            .click().show()
                                } catch (e: Exception) {

                                }

                            }
                        }, ConstantValue.currentEmailConfigEntity!!.sendMenu, drafts, draftsId)
            } else {
                var gmailService = GmailQuickstart.getGmailService(AppConfig.instance, ConstantValue.currentEmailConfigEntity!!.account);
                emailSendClient
                        .setTo(toAdress)                //收件人的邮箱地址
                        .setCc(ccAdress)
                        .setBcc(bccAdress)
                        .setNickname(name)                                    //发件人昵称
                        .setSubject(subject.getText().toString())             //邮件标题
                        .setContent(contentHtml)              //邮件正文
                        .setCidPath(cidNameList)                 //cid资源
                        .setCid(cidList)
                        .setUUID(uuid)
                        .setAttach(attachList)              //附件
                        .gmailSendAsyn(this, object : GetSendCallback {
                            override fun sendSuccess() {
                                runOnUiThread {
                                    closeProgressDialog()
                                    var emailConfigEntityChooseList = AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.queryBuilder().where(EmailConfigEntityDao.Properties.IsChoose.eq(true)).list()
                                    if (emailConfigEntityChooseList.size > 0) {
                                        var emailConfigEntityChoose = emailConfigEntityChooseList.get(0)
                                        emailConfigEntityChoose.sendMenuRefresh = true
                                        AppConfig.instance.mDaoMaster!!.newSession().emailConfigEntityDao.update(emailConfigEntityChoose)
                                    }

                                    if (foward == 0 && emailMeaasgeInfoData != null && emailMeaasgeInfoData!!.userId != null && emailMeaasgeInfoData!!.userId != "") {
                                        var selfUserId = SpUtil.getString(AppConfig.instance, ConstantValue.userId, "")
                                        var emailId = RxEncodeTool.base64Encode2String(ConstantValue.currentEmailConfigEntity!!.account.toByteArray())
                                        var AddFriendsAutoReq = AddFriendsAutoReq(1, selfUserId!!, emailMeaasgeInfoData!!.userId, emailId)
                                        var sendData = BaseData(6, AddFriendsAutoReq);
                                        if (ConstantValue.isWebsocketConnected) {
                                            AppConfig.instance.getPNRouterServiceMessageSender().send(sendData)
                                        } else if (ConstantValue.isToxConnected) {
                                            var baseData = sendData
                                            var baseDataJson = baseData.baseDataToJson().replace("\\", "")
                                            if (ConstantValue.isAntox) {
                                                //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                                                //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
                                            } else {
                                                ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.currentRouterId.substring(0, 64))
                                            }
                                        }
                                    }

                                    var addFriendReq = MailSendNotice(addressBase64)
                                    var sendData = BaseData(6, addFriendReq);
                                    if (ConstantValue.isWebsocketConnected) {
                                        AppConfig.instance.getPNRouterServiceMessageSender().send(sendData)
                                    } else if (ConstantValue.isToxConnected) {
                                        var baseData = sendData
                                        var baseDataJson = baseData.baseDataToJson().replace("\\", "")
                                        if (ConstantValue.isAntox) {
                                            //var friendKey: FriendKey = FriendKey(ConstantValue.currentRouterId.substring(0, 64))
                                            //MessageHelper.sendMessageFromKotlin(AppConfig.instance, friendKey, baseDataJson, ToxMessageType.NORMAL)
                                        } else {
                                            ToxCoreJni.getInstance().senToxMessage(baseDataJson, ConstantValue.currentRouterId.substring(0, 64))
                                        }
                                    }
                                    EventBus.getDefault().post(SendEmailSuccess(positionIndex))
                                    Toast.makeText(this@EmailSendActivity, R.string.success, Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }

                            override fun sendFailure(errorMsg: String) {
                                runOnUiThread {
                                    closeProgressDialog()
                                }
                                Islands.ordinaryDialog(this@EmailSendActivity)
                                        .setText(null, getString(R.string.error) + ":" + errorMsg)
                                        .setButton(getString(R.string.close), null, null)
                                        .click().show()
                            }
                        }, ConstantValue.currentEmailConfigEntity!!.sendMenu, drafts, draftsId, gmailService, "me")
            }


        } else {
            runOnUiThread {
                showProgressDialog(getString(R.string.Saving))
            }
            if (ConstantValue.currentEmailConfigEntity!!.userId == null || ConstantValue.currentEmailConfigEntity!!.userId == "") {
                emailSendClient
                        .setTo(toAdress)                //收件人的邮箱地址
                        .setCc(ccAdress)
                        .setBcc(bccAdress)
                        .setNickname(name)                                    //发件人昵称
                        .setSubject(subject.getText().toString())             //邮件标题
                        .setContent(contentHtml)              //邮件正文
                        .setCidPath(cidNameList)                 //cid资源
                        .setCid(cidList)
                        .setUUID(uuid)
                        .setAttach(attachList)
                        .saveDraftsAsyn(this, object : GetSendCallback {
                            override fun sendSuccess() {
                                runOnUiThread {
                                    closeProgressDialog()
                                    Toast.makeText(this@EmailSendActivity, R.string.success, Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }

                            override fun sendFailure(errorMsg: String) {
                                runOnUiThread {
                                    closeProgressDialog()
                                }
                                Islands.ordinaryDialog(this@EmailSendActivity)
                                        .setText(null, getString(R.string.error))
                                        .setButton(getString(R.string.close), null, null)
                                        .click().show()
                            }
                        }, ConstantValue.currentEmailConfigEntity!!.drafMenu, "draf")
            } else {
                var gmailService = GmailQuickstart.getGmailService(AppConfig.instance, ConstantValue.currentEmailConfigEntity!!.account);
                emailSendClient
                        .setTo(toAdress)                //收件人的邮箱地址
                        .setCc(ccAdress)
                        .setBcc(bccAdress)
                        .setNickname(name)                                    //发件人昵称
                        .setSubject(subject.getText().toString())             //邮件标题
                        .setContent(contentHtml)              //邮件正文
                        .setCidPath(cidNameList)                 //cid资源
                        .setCid(cidList)
                        .setUUID(uuid)
                        .setAttach(attachList)
                        .gmailSaveDraftsAsyn(this, object : GetSendCallback {
                            override fun sendSuccess() {
                                runOnUiThread {
                                    closeProgressDialog()
                                    Toast.makeText(this@EmailSendActivity, R.string.success, Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }

                            override fun sendFailure(errorMsg: String) {
                                runOnUiThread {
                                    closeProgressDialog()
                                }
                                Islands.ordinaryDialog(this@EmailSendActivity)
                                        .setText(null, getString(R.string.error))
                                        .setButton(getString(R.string.close), null, null)
                                        .click().show()
                            }
                        }, ConstantValue.currentEmailConfigEntity!!.drafMenu, "draf", gmailService, "me")
            }

        }

    }

    private val permission = object : PermissionListener {
        override fun onSucceed(requestCode: Int, grantedPermissions: List<String>) {

            // 权限申请成功回调。
            if (requestCode == 101) {
                selectPicFromCamera()
            }
        }

        override fun onFailed(requestCode: Int, deniedPermissions: List<String>) {
            // 权限申请失败回调。
            if (requestCode == 101) {
                KLog.i("权限申请失败")

            }
        }
    }
    private val permissionVideo = object : PermissionListener {
        override fun onSucceed(requestCode: Int, grantedPermissions: List<String>) {

            // 权限申请成功回调。
            if (requestCode == 101) {
                selectVideoFromCamera()
            }
        }

        override fun onFailed(requestCode: Int, deniedPermissions: List<String>) {
            // 权限申请失败回调。
            if (requestCode == 101) {
                KLog.i("权限申请失败")

            }
        }
    }

    fun getEditText(edit: EditText): String {
        val toSelectionEnd = edit.length()
        val toSelectionStart = 0
        val toSpans = edit!!.getText()!!.getSpans(toSelectionStart, toSelectionEnd, User::class.java)
        var toAdress = ""
        var toIndex = 0
        for (span in toSpans) {
            var id = span!!.id
            if (id != null) {
                id.replace("\"", "")
                id.replace("\"", "")
                id = id.trim()
            }
            if (span != null && id != null && id != "") {
                if (toIndex > 0) {
                    toAdress += "," + id
                } else {
                    toAdress += id
                }
                toIndex++
            }
        }
        return toAdress
    }

    /**
     * capture new image
     */
    protected fun selectPicFromCamera() {
        if (!EaseCommonUtils.isSdcardExist()) {
            Toast.makeText(this, R.string.sd_card_does_not_exist, Toast.LENGTH_SHORT).show()
            return
        }
        cameraFile = File(PathUtils.getInstance().tempPath, (System.currentTimeMillis() / 1000).toString() + ".jpg")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            cameraFile = File(Environment.getExternalStorageDirectory().toString() + ConstantValue.localPath + "/PicAndVideoTemp", (System.currentTimeMillis() / 1000).toString() + ".jpg")
        }

        try {
            cameraFile!!.getParentFile().mkdirs()
            val uri = EaseCompat.getUriForFile(this, cameraFile)
            startActivityForResult(
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, uri),
                    REQUEST_CODE_CAMERA)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.Permissionerror, Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * capture new video
     */
    protected fun selectVideoFromCamera() {
        if (!EaseCommonUtils.isSdcardExist()) {
            Toast.makeText(this, R.string.sd_card_does_not_exist, Toast.LENGTH_SHORT).show()
            return
        }
        videoFile = File(PathUtils.getInstance().videoPath, (System.currentTimeMillis() / 1000).toString() + ".mp4")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            videoFile = File(Environment.getExternalStorageDirectory().toString() + ConstantValue.localPath + "/PicAndVideoTemp", (System.currentTimeMillis() / 1000).toString() + ".mp4")
        }
        KLog.i(videoFile!!.getPath())

        videoFile!!.getParentFile().mkdirs()
        startActivityForResult(
                Intent(MediaStore.ACTION_VIDEO_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, EaseCompat.getUriForFile(this, videoFile)).putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30).putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0),
                REQUEST_CODE_VIDEO)
    }

    /**
     * select local image
     * //todo
     */
    protected fun selectPicFromLocal() {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofAll())
                .maxSelectNum(9)
                .minSelectNum(1)
                .imageSpanCount(3)
                .selectionMode(PictureConfig.MULTIPLE)
                .previewImage(true)
                .previewVideo(true)
                .enablePreviewAudio(false)
                .isCamera(false)
                .imageFormat(PictureMimeType.PNG)
                .isZoomAnim(true)
                .sizeMultiplier(0.5f)
                .setOutputCameraPath("/CustomPath")
                .enableCrop(false)
                .compress(false)
                .glideOverride(160, 160)
                .hideBottomControls(false)
                .isGif(false)
                .openClickSound(false)
                .minimumCompressSize(100)
                .synOrAsy(true)
                .rotateEnabled(true)
                .scaleEnabled(true)
                .videoMaxSecond(60 * 60 * 3)
                .videoMinSecond(1)
                .isDragFrame(false)
                .forResult(REQUEST_CODE_LOCAL)
    }

    /**
     * 初始化文本编辑器
     */
    private fun initEditor() {
        re_main_editor.setEditorHeight(120);
        //输入框显示字体的大小
        re_main_editor.setEditorFontSize(16)
        //输入框显示字体的颜色
        re_main_editor.setEditorFontColor(Color.GRAY)
        //输入框背景设置
        re_main_editor.setEditorBackgroundColor(Color.WHITE)
        //re_main_editor.setBackgroundColor(Color.BLUE);
        //re_main_editor.setBackgroundResource(R.drawable.bg);
        //re_main_editor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
        //输入框文本padding
        //re_main_editor.setPadding(10, 10, 10, 10)
        //输入提示文本
        re_main_editor.setPlaceholder(getString(R.string.Compose_email))
        //是否允许输入
        //re_main_editor.setInputEnabled(false);
        //文本输入框监听事件
        re_main_editor.setOnTextChangeListener(object : RichEditor.OnTextChangeListener {
            override fun onTextChange(text: String) {
                re_main_editor.setEditorFontColor(Color.BLACK)
                Log.d("re_main_editor", "html文本：$text")
            }
        })
        re_main_editor.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (hasFocus) {
                    EditorIconParent.visibility = View.VISIBLE
                } else {
                    EditorIconParent.visibility = View.GONE
                }
            }
        });
    }

    /**
     * 初始化颜色选择器
     */
    private fun initColorPicker() {
        cpv_main_color.setOnColorPickerChangeListener(object : ColorPickerView.OnColorPickerChangeListener {
            override fun onColorChanged(picker: ColorPickerView, color: Int) {
                button_text_color.setBackgroundColor(color)
                re_main_editor.setTextColor(color)
            }

            override fun onStartTrackingTouch(picker: ColorPickerView) {

            }

            override fun onStopTrackingTouch(picker: ColorPickerView) {

            }
        })
    }

    /**
     * 初始化菜单按钮
     */
    private fun initMenu() {
        getViewMeasureHeight()
    }

    /**
     * 获取控件的高度
     */
    private fun getViewMeasureHeight() {
        //获取像素密度
        val mDensity = resources.displayMetrics.density
        //获取布局的高度
        val w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED)
        val h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED)
        ll_main_color.measure(w, h)
        val height = ll_main_color.getMeasuredHeight()
        mFoldedViewMeasureHeight = (mDensity * height + 0.5).toInt()
    }

    private fun initClickListener() {
        button_bold.setOnClickListener(this)
        button_text_color!!.setOnClickListener(this)
        tv_main_preview.setOnClickListener(this)
        button_image.setOnClickListener(this)
        button_list_ol.setOnClickListener(this)
        button_list_ul.setOnClickListener(this)
        button_underline.setOnClickListener(this)
        button_italic.setOnClickListener(this)
        button_align_left.setOnClickListener(this)
        button_align_right.setOnClickListener(this)
        button_align_center.setOnClickListener(this)
        button_indent.setOnClickListener(this)
        button_outdent.setOnClickListener(this)
        action_blockquote.setOnClickListener(this)
        action_strikethrough.setOnClickListener(this)
        action_superscript.setOnClickListener(this)
        action_subscript.setOnClickListener(this)

        backBtn.setOnClickListener(this)
        addTo.setOnClickListener(this)
        showCcAndBcc.setOnClickListener(this)
        addCc.setOnClickListener(this)
        addBcc.setOnClickListener(this)

        sendBtn.setOnClickListener(this)
        addKeyImg.setOnClickListener(this)
        addSubjectImg.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.button_bold) {//字体加粗
            if (isClickBold) {
                button_bold.setImageResource(R.mipmap.bold)
            } else {  //加粗
                button_bold.setImageResource(R.mipmap.bold_)
            }
            isClickBold = !isClickBold
            re_main_editor.setBold()
        } else if (id == R.id.button_text_color) {//设置字体颜色
            //如果动画正在执行,直接return,相当于点击无效了,不会出现当快速点击时,
            // 动画的执行和ImageButton的图标不一致的情况
            if (isAnimating) return
            //如果动画没在执行,走到这一步就将isAnimating制为true , 防止这次动画还没有执行完毕的
            //情况下,又要执行一次动画,当动画执行完毕后会将isAnimating制为false,这样下次动画又能执行
            isAnimating = true

            if (ll_main_color.getVisibility() == View.GONE) {
                //打开动画
                animateOpen(ll_main_color)
            } else {
                //关闭动画
                animateClose(ll_main_color)
            }
        } else if (id == R.id.button_image) {//插入图片
            //这里的功能需要根据需求实现，通过insertImage传入一个URL或者本地图片路径都可以，这里用户可以自己调用本地相
            //或者拍照获取图片，传图本地图片路径，也可以将本地图片路径上传到服务器（自己的服务器或者免费的七牛服务器），
            //返回在服务端的URL地址，将地址传如即可（我这里传了一张写死的图片URL，如果你插入的图片不现实，请检查你是否添加
            // 网络请求权限<uses-permission android:name="android.permission.INTERNET" />）
            re_main_editor.insertImage("http://www.1honeywan.com/dachshund/image/7.21/7.21_3_thumb.JPG",
                    "dachshund")
        } else if (id == R.id.button_list_ol) {
            if (isListOl) {
                button_list_ol.setImageResource(R.mipmap.list_ol)
            } else {
                button_list_ol.setImageResource(R.mipmap.list_ol_)
            }
            isListOl = !isListOl
            re_main_editor.setNumbers()
        } else if (id == R.id.button_list_ul) {
            if (isListUL) {
                button_list_ul.setImageResource(R.mipmap.list_ul)
            } else {
                button_list_ul.setImageResource(R.mipmap.list_ul_)
            }
            isListUL = !isListUL
            re_main_editor.setBullets()
        } else if (id == R.id.button_underline) {
            if (isTextLean) {
                button_underline.setImageResource(R.mipmap.underline)
            } else {
                button_underline.setImageResource(R.mipmap.underline_)
            }
            isTextLean = !isTextLean
            re_main_editor.setUnderline()
        } else if (id == R.id.button_italic) {
            if (isItalic) {
                button_italic.setImageResource(R.mipmap.lean)
            } else {
                button_italic.setImageResource(R.mipmap.lean_)
            }
            isItalic = !isItalic
            re_main_editor.setItalic()
        } else if (id == R.id.button_align_left) {
            if (isAlignLeft) {
                button_align_left.setImageResource(R.mipmap.align_left)
            } else {
                button_align_left.setImageResource(R.mipmap.align_left_)
            }
            isAlignLeft = !isAlignLeft
            re_main_editor.setAlignLeft()
        } else if (id == R.id.button_align_right) {
            if (isAlignRight) {
                button_align_right.setImageResource(R.mipmap.align_right)
            } else {
                button_align_right.setImageResource(R.mipmap.align_right_)
            }
            isAlignRight = !isAlignRight
            re_main_editor.setAlignRight()
        } else if (id == R.id.button_align_center) {
            if (isAlignCenter) {
                button_align_center.setImageResource(R.mipmap.align_center)
            } else {
                button_align_center.setImageResource(R.mipmap.align_center_)
            }
            isAlignCenter = !isAlignCenter
            re_main_editor.setAlignCenter()
        } else if (id == R.id.button_indent) {
            if (isIndent) {
                button_indent.setImageResource(R.mipmap.indent)
            } else {
                button_indent.setImageResource(R.mipmap.indent_)
            }
            isIndent = !isIndent
            re_main_editor.setIndent()
        } else if (id == R.id.button_outdent) {
            if (isOutdent) {
                button_outdent.setImageResource(R.mipmap.outdent)
            } else {
                button_outdent.setImageResource(R.mipmap.outdent_)
            }
            isOutdent = !isOutdent
            re_main_editor.setOutdent()
        } else if (id == R.id.action_blockquote) {
            if (isBlockquote) {
                action_blockquote.setImageResource(R.mipmap.blockquote)
            } else {
                action_blockquote.setImageResource(R.mipmap.blockquote_)
            }
            isBlockquote = !isBlockquote
            re_main_editor.setBlockquote()
        } else if (id == R.id.action_strikethrough) {
            if (isStrikethrough) {
                action_strikethrough.setImageResource(R.mipmap.strikethrough)
            } else {
                action_strikethrough.setImageResource(R.mipmap.strikethrough_)
            }
            isStrikethrough = !isStrikethrough
            re_main_editor.setStrikeThrough()
        } else if (id == R.id.action_superscript) {
            if (isSuperscript) {
                action_superscript.setImageResource(R.mipmap.superscript)
            } else {
                action_superscript.setImageResource(R.mipmap.superscript_)
            }
            isSuperscript = !isSuperscript
            re_main_editor.setSuperscript()
        } else if (id == R.id.action_subscript) {
            if (isSubscript) {
                action_subscript.setImageResource(R.mipmap.subscript)
            } else {
                action_subscript.setImageResource(R.mipmap.subscript_)
            }
            isSubscript = !isSubscript
            re_main_editor.setSubscript()
        } else if (id == R.id.tv_main_preview) {//预览
            /* val intent = Intent(this@EmailSendActivity, WebDataActivity::class.java)
             intent.putExtra("diarys", re_main_editor.getHtml())
             startActivity(intent)*/
        }//H1--H6省略，需要的自己添加
        else if (id == R.id.showCcAndBcc) {//预览
            if (ccParent.visibility == View.VISIBLE) {
                var drawable = getResources().getDrawable(R.mipmap.tabbar_arrow_lower)
                drawable.setBounds(0, 0, 48, 48);
                showCcAndBcc.setCompoundDrawables(drawable, null, null, null);
                ccParent.visibility = View.GONE
                bccParent.visibility = View.GONE
            } else {
                var drawable = getResources().getDrawable(R.mipmap.tabbar_arrow_upper)
                drawable.setBounds(0, 0, 48, 48);
                showCcAndBcc.setCompoundDrawables(drawable, null, null, null);
                ccParent.visibility = View.VISIBLE
                bccParent.visibility = View.VISIBLE
            }

        } else if (id == R.id.addTo) {
            val intent = Intent(this@EmailSendActivity, SelectEmailFriendActivity::class.java)
            oldAdress = getEditText(toAdressEdit)
            intent.putExtra("oldAdress", oldAdress)
            startActivityForResult(intent, REQUEST_CODE_TO)
        } else if (id == R.id.addCc) {
            val intent = Intent(this@EmailSendActivity, SelectEmailFriendActivity::class.java)
            oldAdress = getEditText(ccAdressEdit)
            intent.putExtra("oldAdress", oldAdress)
            startActivityForResult(intent, REQUEST_CODE_CC)
        } else if (id == R.id.addBcc) {
            val intent = Intent(this@EmailSendActivity, SelectEmailFriendActivity::class.java)
            oldAdress = getEditText(bccAdressEdit)
            intent.putExtra("oldAdress", oldAdress)
            startActivityForResult(intent, REQUEST_CODE_BCC)
        } else if (id == R.id.backBtn) {
            var toAdress = toAdressEdit.text.toString()
            var subject = subject.text.toString()
            var re_main_editorStr = re_main_editor.html
            if (toAdress != "" || subject != "" || re_main_editorStr != "") {
                if (foward != 3 && InviteURLText == "") {
                    showDialog()
                } else {
                    finish()
                }
            } else {
                finish()
            }
            //onBackPressed()
        } else if (id == R.id.sendBtn) {
            allSpan(toAdressEdit)
            allSpan(ccAdressEdit)
            allSpan(bccAdressEdit)
            sendCheck(true);
            //sendEmail()
        } else if (id == R.id.addKeyImg) {
            PopWindowUtil.showPopKeyMenuWindow(this@EmailSendActivity, addKeyImg, userPassWord, userPassWordTips, object : PopWindowUtil.OnSelectListener {
                override fun onSelect(position: Int, obj: Any) {
                    var map = obj as HashMap<String, String>
                    userPassWord = map.get("password") as String
                    userPassWordTips = map.get("passTips") as String
                    if (userPassWord != "" && InviteURLText == "") {
                        addKeyImg.setImageResource(R.mipmap.tabbar_email1_selected)
                        lockTips.visibility = View.VISIBLE
                    } else {
                        addKeyImg.setImageResource(R.mipmap.tabbar_email1_unselected)
                        if (contactMapList.size == needSize && InviteURLText == "") {
                            lockTips.visibility = View.VISIBLE
                        } else {
                            lockTips.visibility = View.GONE
                        }
                    }
                }
            })
        } else if (id == R.id.addSubjectImg) {
            sendRoot.fullScroll(NestedScrollView.FOCUS_DOWN)
            subject.requestFocus()
            iv_add.performClick()
        }

    }

    /**
     * 开启动画
     *
     * @param view 开启动画的view
     */
    private fun animateOpen(view: LinearLayout) {
        view.visibility = View.VISIBLE
        val animator = createDropAnimator(view, 0, mFoldedViewMeasureHeight)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isAnimating = false
            }
        })
        animator.start()
    }

    /**
     * 关闭动画
     *
     * @param view 关闭动画的view
     */
    private fun animateClose(view: LinearLayout) {
        val origHeight = view.height
        val animator = createDropAnimator(view, origHeight, 0)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE
                isAnimating = false
            }
        })
        animator.start()
    }


    /**
     * 创建动画
     *
     * @param view  开启和关闭动画的view
     * @param start view的高度
     * @param end   view的高度
     * @return ValueAnimator对象
     */
    private fun createDropAnimator(view: View, start: Int, end: Int): ValueAnimator {
        val animator = ValueAnimator.ofInt(start, end)
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.height = value
            view.layoutParams = layoutParams
        }
        return animator
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.getAction() === MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (isShouldHideInput(v, ev)) {

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.hideSoftInputFromWindow(v!!.windowToken, 0)
            }
            return super.dispatchTouchEvent(ev)
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return if (window.superDispatchTouchEvent(ev)) {
            true
        } else onTouchEvent(ev)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) { // capture new image
                if (cameraFile != null && cameraFile!!.exists()) {
                    var videoFilePath = cameraFile!!.getAbsolutePath()
                    var file = File(videoFilePath);
                    var isHas = file.exists();
                    if (isHas) {
                        if (file.length() > 1024 * 1024 * 100) {
                            runOnUiThread {
                                longToast(R.string.Files_100M)
                            }
                            return;
                        }
                    }
                    var emailAttachEntity = EmailAttachEntity()
                    emailAttachEntity.isHasData = true
                    emailAttachEntity.localPath = videoFilePath
                    emailAttachEntity.name = videoFilePath.substring(videoFilePath.lastIndexOf("/") + 1, videoFilePath.length)
                    emailAttachEntity.isCanDelete = true
                    emaiAttachAdapter!!.addData(0, emailAttachEntity)
                    emaiAttachAdapter!!.notifyDataSetChanged();
                }
            } else if (requestCode == REQUEST_CODE_VIDEO) {
                if (videoFile != null && videoFile!!.exists()) {
                    var videoFilePath = videoFile!!.getAbsolutePath()
                    var file = File(videoFilePath);
                    var isHas = file.exists();
                    if (isHas) {
                        if (file.length() > 1024 * 1024 * 100) {
                            runOnUiThread {
                                longToast(R.string.Files_100M)
                            }
                            return;
                        }
                    }
                    var emailAttachEntity = EmailAttachEntity()
                    emailAttachEntity.isHasData = true
                    emailAttachEntity.localPath = videoFilePath
                    emailAttachEntity.name = videoFilePath.substring(videoFilePath.lastIndexOf("/") + 1, videoFilePath.length)
                    emailAttachEntity.isCanDelete = true
                    emaiAttachAdapter!!.addData(0, emailAttachEntity)
                    emaiAttachAdapter!!.notifyDataSetChanged();
                    if (emaiAttachAdapter!!.itemCount > 1) {
                        addSubjectImg.setImageResource(R.mipmap.tabbar_attach1_selected)
                        addSubject.text = (emaiAttachAdapter!!.itemCount - 1).toString()
                        addSubject.visibility = View.GONE
                    } else {
                        addSubjectImg.setImageResource(R.mipmap.tabbar_attach1_unselected)
                        addSubject.text = ""
                        addSubject.visibility = View.GONE
                    }
                }
            } else if (requestCode == REQUEST_CODE_LOCAL) { // send local image
                KLog.i("选照片或者视频返回。。。")
                val list = data!!.getParcelableArrayListExtra<LocalMedia>(PictureConfig.EXTRA_RESULT_SELECTION)
                KLog.i(list)
                if (list != null && list.size > 0) {
                    var len = list.size
                    //emaiAttachAdapter!!.remove(emaiAttachAdapter!!.itemCount)
                    var itemCount = emaiAttachAdapter!!.itemCount
                    for (i in 0 until len) {
                        var file = File(list.get(i).path);
                        var isHas = file.exists();
                        if (isHas) {
                            if (file.length() > 1024 * 1024 * 100) {
                                runOnUiThread {
                                    longToast(R.string.Files_100M)
                                }
                                continue;
                            }
                        }
                        var emailAttachEntity = EmailAttachEntity()
                        emailAttachEntity.isHasData = true
                        emailAttachEntity.localPath = list.get(i).path
                        emailAttachEntity.name = list.get(i).path.substring(list.get(i).path.lastIndexOf("/") + 1, list.get(i).path.length)
                        emailAttachEntity.isCanDelete = true
                        emaiAttachAdapter!!.addData(0, emailAttachEntity)

                    }
                    emaiAttachAdapter!!.notifyDataSetChanged();
                    if (emaiAttachAdapter!!.itemCount > 1) {
                        addSubjectImg.setImageResource(R.mipmap.tabbar_attach1_selected)
                        addSubject.text = (emaiAttachAdapter!!.itemCount - 1).toString()
                        addSubject.visibility = View.GONE
                    } else {
                        addSubjectImg.setImageResource(R.mipmap.tabbar_attach1_unselected)
                        addSubject.text = ""
                        addSubject.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this, getString(R.string.select_resource_error), Toast.LENGTH_SHORT).show()
                }
            } else if (requestCode == REQUEST_CODE_FILE) {
                if (data!!.getIntExtra("offsetRequestCode", 0) == 0) {
                    if (data!!.hasExtra("path")) {
                        val filePath = data.getStringExtra("path")
                        if (filePath != null) {
                            val file = File(filePath)
                            val md5Data = ""
                            if (file.exists()) {
                                if (file.length() > 1024 * 1024 * 100) {
                                    runOnUiThread {
                                        longToast(R.string.Files_100M)
                                    }
                                    return;
                                }
                                var emailAttachEntity = EmailAttachEntity()
                                emailAttachEntity.isHasData = true
                                emailAttachEntity.localPath = file.path
                                emailAttachEntity.name = file.path.substring(file.path.lastIndexOf("/") + 1, file.path.length)
                                emailAttachEntity.isCanDelete = true
                                emaiAttachAdapter!!.addData(0, emailAttachEntity)
                                emaiAttachAdapter!!.notifyDataSetChanged();
                                if (emaiAttachAdapter!!.itemCount > 1) {
                                    addSubjectImg.setImageResource(R.mipmap.tabbar_attach1_selected)
                                    addSubject.text = (emaiAttachAdapter!!.itemCount - 1).toString()
                                    addSubject.visibility = View.GONE
                                } else {
                                    addSubjectImg.setImageResource(R.mipmap.tabbar_attach1_unselected)
                                    addSubject.text = ""
                                    addSubject.visibility = View.GONE
                                }
                            }
                        }
                    } else {
                        val fileData = data.getParcelableExtra<JPullFileListRsp.ParamsBean.PayloadBean>("fileData")
                        //sendFileFileForward(fileData)
                    }
                } else if (data!!.getIntExtra("offsetRequestCode", 0) == 1) {
                    onActivityResult(REQUEST_CODE_LOCAL, Activity.RESULT_OK, data)
                } else if (data!!.getIntExtra("offsetRequestCode", 0) == 2) {
                    onActivityResult(REQUEST_CODE_VIDEO, Activity.RESULT_OK, data)
                }
            } else if (requestCode == REQUEST_CODE_TO) {
                toAdressEdit.requestFocus()
                if (data!!.hasExtra("selectAdressStr")) {
                    var selectAdressStr = data!!.getStringExtra("selectAdressStr")
                    var nameAdressStr = data!!.getStringExtra("nameAdressStr")
                    var selectAdressStrArray = selectAdressStr.split(",")
                    var nameAdressStrArray = nameAdressStr.split(",")
                    var i = 0;
                    for (item in selectAdressStrArray) {
                        if (!oldAdress.contains(item)) {
                            var adress = item
                            var name = nameAdressStrArray.get(i)
                            var user = User(adress, name, name)
                            (toAdressEdit.text as SpannableStringBuilder)
                                    .append(methodContext.newSpannable(user))
                                    .append(";")
                        }
                        i++;
                    }
                    sendCheck(false)
                }
            } else if (requestCode == REQUEST_CODE_CC) {
                ccAdressEdit.requestFocus()
                if (data!!.hasExtra("selectAdressStr")) {
                    var selectAdressStr = data!!.getStringExtra("selectAdressStr")
                    var nameAdressStr = data!!.getStringExtra("nameAdressStr")
                    var selectAdressStrArray = selectAdressStr.split(",")
                    var nameAdressStrArray = nameAdressStr.split(",")
                    var i = 0;
                    for (item in selectAdressStrArray) {
                        if (!oldAdress.contains(item)) {
                            var adress = item
                            var name = nameAdressStrArray.get(i)
                            var user = User(adress, name, name)
                            (ccAdressEdit.text as SpannableStringBuilder)
                                    .append(methodContext.newSpannable(user))
                                    .append(";")
                        }
                        i++;
                    }
                    sendCheck(false)
                }
            } else if (requestCode == REQUEST_CODE_BCC) {
                bccAdressEdit.requestFocus()
                if (data!!.hasExtra("selectAdressStr")) {
                    var selectAdressStr = data!!.getStringExtra("selectAdressStr")
                    var nameAdressStr = data!!.getStringExtra("nameAdressStr")
                    var selectAdressStrArray = selectAdressStr.split(",")
                    var nameAdressStrArray = nameAdressStr.split(",")
                    var i = 0;
                    for (item in selectAdressStrArray) {
                        if (!oldAdress.contains(item)) {
                            var adress = item
                            var name = nameAdressStrArray.get(i)
                            var user = User(adress, name, name)
                            (bccAdressEdit.text as SpannableStringBuilder)
                                    .append(methodContext.newSpannable(user))
                                    .append(";")
                        }
                        i++;
                    }
                    sendCheck(false)
                }
            }

        }
    }

    fun replaceImgCidByLocalPath(content: String, fileName: String, filePath: String, pre: String): String {
        return content.replace("file://" + filePath + "\"", "cid:" + pre + fileName + "\"").toString();
    }

    override fun setupActivityComponent() {
        DaggerEmailSendComponent
                .builder()
                .appComponent((application as AppConfig).applicationComponent)
                .emailSendModule(EmailSendModule(this))
                .build()
                .inject(this)
    }

    override fun setPresenter(presenter: EmailSendContract.EmailSendContractPresenter) {
        mPresenter = presenter as EmailSendPresenter
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun closeProgressDialog() {
        progressDialog.hide()
    }

    private fun switch() {
        val method = circularMethod()
        methodContext.method = method
        methodContext.init(toAdressEdit)
        methodContextCc.method = method
        methodContextCc.init(ccAdressEdit)
        methodContextBcc.method = method
        methodContextBcc.init(bccAdressEdit)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            var toAdress = toAdressEdit.text.toString()
            var subject = subject.text.toString()
            var re_main_editorStr = re_main_editor.html
            if (toAdress != "" || subject != "" || re_main_editorStr != "") {
                if (foward != 3 && InviteURLText == "") {
                    showDialog()
                } else {

                    finish()
                }

            } else {
                finish()
            }

        }
        return true
    }

    fun showDialog() {
        SweetAlertDialog(this, SweetAlertDialog.BUTTON_NEUTRAL)
                .setCancelText(getString(R.string.no))
                .setConfirmText(getString(R.string.yes))
                .setContentText(getString(R.string.Save_it_in_the_draft_box))
                .setConfirmClickListener {
                    sendEmail(false)
                }.setCancelClickListener {
                    finish()
                }
                .show()

    }

    private tailrec fun circularMethod(): Method {
        return if (iterator.hasNext()) {
            iterator.next()
        } else {
            iterator = methods.iterator()
            circularMethod()
        }
    }

    override fun onDestroy() {
        AppConfig.instance.messageReceiver?.checkmailUkeyCallback = null
        super.onDestroy()
    }


}