package com.wk.unichat.WebRequests

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.wk.unichat.Channels.Channel
import com.wk.unichat.Channels.Msg
import com.wk.unichat.Utils.URL_GET_CHANNELS
import com.wk.unichat.Utils.URL_GET_MESSAGES
import org.json.JSONException

object MsgService {

    val channels = ArrayList<Channel>() // Pusta ArrayLista
    val messages = ArrayList<Msg>()


    fun channels (context: Context, complete: (Boolean) -> Unit) {

        // Pytamy o objekt typu JsonArray
        val channelsReg = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null, Response.Listener {response->

            try {
                // Pętla po wszystkich kanałach znajdujących się w bazie danych
                for ( i in 0 until response.length()) {
                    val channel = response.getJSONObject(i)

                    val name = channel.getString("name")
                    val channelInfo = channel.getString("description")
                    val channelId = channel.getString("_id")

                    val createChannel = Channel(name,channelInfo,channelId)

                    this.channels.add(createChannel)
                }
                complete(true)
            } catch(e: JSONException) {
                Log.d("JSON", "Exception: " + e.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener {error->
            Log.d("ERROR", "Channels loading fail")
            complete(false)
        }) {

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8" // Typ szyfrowania
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${Requests.logToken}")
                return headers
            }
        }
        Volley.newRequestQueue(context).add(channelsReg)
    }

    fun getMsg(context: Context, channelId: String, complete: (Boolean) -> Unit) {

        val url = "$URL_GET_MESSAGES$channelId"

        val messageRequest = object : JsonArrayRequest(Method.GET, url, null, Response.Listener {response ->  
            clearMsg()
            try {
                for (x in 0 until response.length()) {
                    val msg = response.getJSONObject(x)
                    val msgBody = msg.getString("messageBody")
                    val channelId = msg.getString("channelId")
                    val id = msg.getString("_id")
                    val userName = msg.getString("userName")
                    val userAvatar = msg.getString("userAvatar")
                    val userAvatarColor = msg.getString("userAvatarColor")
                    val timeStamp = msg.getString("timeStamp")

                    val newMessage = Msg(msgBody, userName, channelId, userAvatar, userAvatarColor, id, timeStamp)
                    this.messages.add(newMessage)
                }
                complete(true)
            } catch(e: JSONException) {
                complete(false)
            }

        }, Response.ErrorListener {
            complete(false)
        }) {

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8" // Typ szyfrowania
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${Requests.logToken}")
                return headers
            }
        }
        Volley.newRequestQueue(context).add(messageRequest)
    }

    fun clearMsg () {
        messages.clear()
    }

}