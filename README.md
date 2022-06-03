# smart_medical_app

程式筆記:

1.maven 引入:

您可以在 settings.gradle中的dependencyResolutionManagement中添加jitpack.io作為存儲庫

    dependencyResolutionManagement {
      repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
      repositories {
          google()
          mavenCentral()
          maven { url 'https://jitpack.io' }
      }
    }

參考網址:

https://stackoverflow.com/questions/69163511/build-was-configured-to-prefer-settings-repositories-over-project-repositories-b

2.如何查詢內網IP:

cmd輸入指令:Ipconfig

3.Hivemq (android kotlin)寫法:

權限設定:

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

build.gradle(:app):

    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.2.1"
    implementation 'com.mapbox.mapboxsdk:mapbox-android-sdk:7.1.0'
    implementation 'com.jakewharton.timber:timber:4.7.1'
    implementation 'androidx.room:room-runtime:2.3.0'
    implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.4'
    implementation files('libs/serviceLibrary-release.aar')
    
 gradle.properties:

    android.enableJetifier=true


code(kotlin):

    fun connect(context: Context) {
        val serverURI = "ssl://{Your Instance}.s1.eu.hivemq.cloud:8883"
        var recCount = 0
        mqttClient = MqttAndroidClient(context, serverURI, "kotlin_client")
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                recCount = recCount + 1
                Log.d(TAG, "Received message ${recCount}: ${message.toString()} from topic: $topic")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
        val options = MqttConnectOptions()
        options.userName = "{YOUR USERNAME}"
        options.password = "{YOUR PASSWORD}".toCharArray()
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                    subscribe("{YOUR TOPIC}")
                    publish("{YOUR TOPIC}", "{YOUR MESSAGE}")

                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Connection failure")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }
    fun subscribe(topic: String, qos: Int = 1) {
        try {
            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(topic: String) {
        try {
            mqttClient.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Unsubscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to unsubscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
    fun disconnect() {
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
 
 4.增加arr file in gradle:
 
 參考網址:
 https://stackoverflow.com/questions/67724242/how-to-import-aar-module-on-android-studio-4-2
 
 For Android Studio Bumblebee | 2021.1.1 Patch 3
I have followed steps suggested by the Android developer site:
1.	Copy .aar file into the libs folder of the app
2.	File -> Project Structure... -> Dependencies

![image](https://github.com/JamesTsao15/smart_medical_app/blob/master/note_image/arr_file_setting1.png)

3.	Click on "+" icon and select JR/AAR Dependency and select app module

![image](https://github.com/JamesTsao15/smart_medical_app/blob/master/note_image/arr_file_setting2.png)
 
4.	Add .aar file path in step 1.

![image](https://github.com/JamesTsao15/smart_medical_app/blob/master/note_image/arr_file_setting3.png)
 
5.	Check your app’s build.gradle file to confirm a declaration.

 
 

