package com.devyash.healthcaredoctorsapp.VideoCalling.repository

import android.util.Log
import com.devyash.healthcaredoctorsapp.VideoCalling.models.TYPE
import com.devyash.healthcaredoctorsapp.VideoCalling.utils.NewMessageInterface
import com.devyash.healthcaredoctorsapp.models.MessageModel.MessageModel
import com.google.gson.Gson
import com.devyash.healthcaredoctorsapp.others.Constants.VIDEOCALLINGWEBRTC
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

class SocketRepository(private val messageInterface: NewMessageInterface) {
    private var webSocket:WebSocketClient?=null
    private var UID:String?=null
    private val TAG = "SocketRepository"
    private val gson = Gson()

    fun initSocket(uid:String){
        UID = uid

        webSocket = object:WebSocketClient(URI("wss://webrtcdeploy-6f29016af3e8.herokuapp.com/")){
            override fun onOpen(handshakedata: ServerHandshake?) {
                sendMessageToSocket(
                    MessageModel(
                        TYPE.STORE_USER,uid,null,null
                    )
                )

            }

            override fun onMessage(message: String?) {
                try {
                    Log.d("OFEERWEBRTC","UId:- "+gson.fromJson(message,MessageModel::class.java).name.toString())
                    messageInterface.onNewMessage(gson.fromJson(message,MessageModel::class.java))

                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(VIDEOCALLINGWEBRTC, "onClose: $reason")
            }

            override fun onError(ex: Exception?) {
                Log.d(VIDEOCALLINGWEBRTC, "onError: $ex")
            }

        }
        webSocket?.connect()
        Log.d("OFEERWEBRTC",webSocket?.socket?.isConnected.toString())
    }

    fun sendMessageToSocket(message: MessageModel) {
        try {
            Log.d(VIDEOCALLINGWEBRTC, "sendMessageToSocket: $message")
            webSocket?.send(Gson().toJson(message))
        } catch (e: Exception) {
            Log.d(VIDEOCALLINGWEBRTC, "sendMessageToSocket: $e")
        }
    }

    fun closeConnection(){
        webSocket?.connection?.closeConnection(0,"Close")
        webSocket = null
    }
}