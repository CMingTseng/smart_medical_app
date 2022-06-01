package com.example.smart_medical_app

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private var uri:String="rtsp://192.168.0.126:8554/cam"
    private lateinit var surfaceHolder_monitor:SurfaceHolder
    private lateinit var surfaceView_monitor: SurfaceView
    private lateinit var player: MediaPlayer
    private lateinit var mqttClient:MqttAndroidClient

    private fun MQTT_connect(context:Context){
        val serverURI="ssl://95cdf9091ac24bf5931fb43b3260cac8.s1.eu.hivemq.cloud:8883"
        var recCount=0
        mqttClient= MqttAndroidClient(context,serverURI,"kotlin_client")
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                recCount = recCount + 1
                Log.e("JAMES", "Received message ${recCount}: ${message.toString()} from topic: $topic")
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
                    MQTT_publish("medical/#", "hello!world")

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("JAMES","onCreate")
        setContentView(R.layout.activity_main)
        surfaceView_monitor=findViewById(R.id.surfaceView_monitor)
        player = MediaPlayer()
        try {
            player.setDataSource(this, Uri.parse(uri))
            surfaceHolder_monitor=surfaceView_monitor.holder
            surfaceHolder_monitor.addCallback(object:SurfaceHolder.Callback{
                override fun surfaceCreated(holder: SurfaceHolder) {
                    player.setDisplay(holder)
                }
                override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

                }
                override fun surfaceDestroyed(p0: SurfaceHolder) {
                }

            })
            player.prepare()
            player.setOnPreparedListener(object:MediaPlayer.OnPreparedListener{
                override fun onPrepared(p0: MediaPlayer?) {
                    player.start()
                    player.isLooping=true
                }

            })
        }catch (e:Exception){
            e.printStackTrace()
        }
        MQTT_connect(this)
    }

    override fun onDestroy() {
        Log.e("JAMES","onDestroy")
        if(player.isPlaying){
            player.stop()
        }
        player.release()
        super.onDestroy()
    }
}
