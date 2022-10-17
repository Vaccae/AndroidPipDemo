package dem.vaccae.androidpipdemo

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dem.vaccae.androidpipdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val ACTION_TEXTVIEW = "textview"
    private val ACTION_TOAST = "toast"
    private val ACTION_VALUE = "value"
    //定义广播接收
    private var mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val str = it.getStringExtra(ACTION_VALUE)
                when (it.action) {
                    ACTION_TEXTVIEW -> {
                        binding.textView.text = str
                    }
                    ACTION_TOAST -> {
                        context?.let { mContext ->
                            Toast.makeText(mContext, str, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    else -> {
                        return@let
                    }
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.text = "正常模式"
        binding.btnpip.setOnClickListener {
            if (isCanPipModel()) {
                enterPipModel()
            } else {
                Toast.makeText(this, "无法进入PIP模式", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    //判断是否可以进入画中画模式
    private fun isCanPipModel(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }

    private fun enterPipModel() {
        val builder = PictureInPictureParams.Builder()

        //设置Actions
        val actions = arrayListOf<RemoteAction>()
        //1.返回全屏窗口
        val pIntent1 = getPendingIntent(intent, 1)
        val remoteAction1 = RemoteAction(
            Icon.createWithResource(
                this,
                R.mipmap.ic_phone_round
            ), "画中画", "Vaccae", pIntent1
        )
        actions.add(remoteAction1)

        //2.修改Text显示
        val intent2 = Intent(ACTION_TEXTVIEW).putExtra(ACTION_VALUE, "Vaccae点击了Text Action")
        val pIntent2 = getPendingIntent(intent2, 2, 2)
        actions.add(
            RemoteAction(
                Icon.createWithResource(
                    this,
                    R.mipmap.ic_launcher
                ), "TextView", "Vaccae", pIntent2
            )
        )

        //3.实现Toast控制
        val intent3 = Intent(ACTION_TOAST).putExtra(ACTION_VALUE, "关注微卡智享")
        val pIntent3 = getPendingIntent(intent3, 3, 2)
        actions.add(
            RemoteAction(
                Icon.createWithResource(
                    this,
                    R.mipmap.ic_launcher
                ), "Toast", "Vaccae", pIntent3
            )
        )

        builder.setActions(actions)
        //设置宽高比例，第一个是分子，第二个是分母,指定宽高比，必须在 2.39:1或1:2.39 之间，否则会抛出IllegalArgumentException异常。
        val rational = Rational(5, 11)
        builder.setAspectRatio(rational)

        //Android12下加入的画中画配置，对于非视频内容停用无缝大小调整
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setSeamlessResizeEnabled(false)
            builder.setAutoEnterEnabled(true)
        }

        enterPictureInPictureMode(builder.build())
    }


    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            binding.textView.text = "画中画模式"
            binding.btnpip.visibility = View.GONE

            //进入画中画时注册广播接收
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_TEXTVIEW)
            intentFilter.addAction(ACTION_TOAST)
            registerReceiver(mBroadcastReceiver, intentFilter)

        } else {
            binding.textView.text = "正常模式"
            binding.btnpip.visibility = View.VISIBLE

            //退出画中画时停止广播接收
            unregisterReceiver(mBroadcastReceiver)
        }
    }

    private fun getPendingIntent(intent: Intent, requestcode: Int, flag: Int = 1): PendingIntent {
        when (flag) {
            1 -> return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    this, requestcode,
                    intent,
                     PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getActivity(
                    this, requestcode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            else -> return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    this, requestcode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getBroadcast(
                    this, requestcode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isCanPipModel()) {
            enterPipModel()
        } else {
            Toast.makeText(this, "无法进入PIP模式", Toast.LENGTH_SHORT)
                .show()
        }
    }
}