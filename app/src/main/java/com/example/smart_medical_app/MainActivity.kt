package com.example.smart_medical_app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pedro.vlc.VlcListener
import com.pedro.vlc.VlcVideoLibrary
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*


class MainActivity : AppCompatActivity(),VlcListener {
    private var uri:String="rtsp://192.168.0.126:8554/cam"
    private lateinit var vlcVideoLibrary: VlcVideoLibrary
    private lateinit var surfaceView_monitor: SurfaceView
    private lateinit var mqttClient:MqttAndroidClient
    private lateinit var fell_down_notification:Notification
    private lateinit var notificationManager: NotificationManager
    private val options:Array<String> = arrayOf(":fullscreen")

    private fun MQTT_connect(context:Context){
        val serverURI="ssl://95cdf9091ac24bf5931fb43b3260cac8.s1.eu.hivemq.cloud:8883"
        var recCount=0
        mqttClient= MqttAndroidClient(context,serverURI,"kotlin_client")
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                recCount = recCount + 1
                Log.e("JAMES", "Received message ${recCount}: ${message.toString()} from topic: $topic")
                if (message.toString()=="fell_Down"){
                    notificationManager.notify(0,fell_down_notification)
                }
            }

            override fun connectionLost(cause: Throwable?) {
                Log.e("JAMES", "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
        val options = MqttConnectOptions()
        options.userName = "JamesTsao"
        options.password = "Jj0928338296".toCharArray()
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.e("JAMES", "Connection success")
                    MQTT_subscribe("medical/#")
                    MQTT_publish("medical/fell_down_alarm", "hello!world")

                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("JAMES", "Connection failure")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun MQTT_subscribe(topic: String,qos:Int=1) {
        try {
            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.e("JAMES", "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("JAMES", "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun MQTT_publish(topic: String, msg: String,qos:Int=1,retained:Boolean=false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.e("JAMES", "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("JAMES", "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun MQTT_disconnect(){
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.e("JAMES", "Disconnected")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("JAMES", "Failed to disconnect")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun notification_Setting(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel= NotificationChannel("fell_down_alarm","Fell_Down_Alarm",NotificationManager.IMPORTANCE_HIGH)
            val fell_down_builder =Notification.Builder(this,"fell_down_alarm")
            fell_down_builder.setSmallIcon(R.drawable.ic_baseline_medical_services_24)
                .setContentTitle("緊急通知")
                .setContentText("有人跌倒了，請注意監控")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
            fell_down_notification=fell_down_builder.build()
            notificationManager=getSystemService(NOTIFICATION_SERVICE)as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Log.e("JAMES","onCreate")
        setContentView(R.layout.activity_main)
        notification_Setting()
        MQTT_connect(this)
        surfaceView_monitor=findViewById(R.id.surfaceView_monitor)
        vlcVideoLibrary= VlcVideoLibrary(this,this,surfaceView_monitor)
    }

    override fun onResume() {
        super.onResume()
        Log.e("JAMES","onResume")
        vlcVideoLibrary.play(uri)
        vlcVideoLibrary.setOptions(options.toMutableList())
    }
    override fun onDestroy() {
        Log.e("JAMES","onDestroy")
        vlcVideoLibrary.stop()
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.e("JAMES","onBackPressed")
        vlcVideoLibrary.stop()
        finish()
    }
    override fun onComplete() {
        Toast.makeText(this,"playing",Toast.LENGTH_SHORT).show()
    }

    override fun onError() {
        Toast.makeText(this,"Error, make sure your endpoint is correct",Toast.LENGTH_SHORT).show()
        vlcVideoLibrary.stop()
    }

}
