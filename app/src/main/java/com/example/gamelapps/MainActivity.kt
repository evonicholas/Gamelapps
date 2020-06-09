package com.example.gamelapps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import android.R.layout.simple_list_item_1
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.add_queue_song_dialog.view.*
import kotlinx.android.synthetic.main.queue_list_layout.*
import kotlinx.android.synthetic.main.queue_list_layout.view.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private var listView :ListView? = null
    private var songList :MutableList<Songs>? = null
    private var queueList :MutableList<Songs>? = null
    var songNameListHolder :MutableList<String> = ArrayList()
    var idLaguListHolder :MutableList<String> = ArrayList()
    var artistNameListHolder :MutableList<String> = ArrayList()
    var nadaListHolder :MutableList<String> = ArrayList()
    var idLaguHolder :String? = null
    var client : MqttAndroidClient? = null
    var playFlag = false
    var timerFlag = false
    var state:Int = 0
    var aState = false
    var bState = false
    var cState = false
    var songchecker = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.lvSonglist)
        songList = mutableListOf()
        queueList = mutableListOf()
        queueList?.clear()
        loadSong()
        initmqtt()

        switchA.setOnCheckedChangeListener { buttonView, isChecked ->
            aState = !aState
        }

        switchB.setOnCheckedChangeListener { buttonView, isChecked ->
            bState = !bState
        }
        switchC.setOnCheckedChangeListener { buttonView, isChecked ->
            cState = !cState
        }

        fun stateChecker(){
            if(state == 0){
                buttonPlay.setOnClickListener {
                    if (idLaguHolder == null) Toast.makeText(applicationContext,
                        "No song selected", Toast.LENGTH_SHORT).show()
                    else if (aState.toInt() == 0 && bState.toInt() == 0 && cState.toInt() == 0) Toast.makeText(applicationContext,
                        "Please select at least one instrument", Toast.LENGTH_SHORT).show()
                    else {
                        publish(aState.toInt().toString()+bState.toInt().toString()+cState.toInt().toString()+idLaguHolder.toString() + "M")
                        switchlayout.isVisible = false
                        songchecker = idLaguHolder.toString()
                        playFlag = true
                        buttonPlay.setText("Pause")
                        state = 1
                        stateChecker()
                    }
                }
            }
            else if(state == 1){
                buttonStop.isEnabled = true
                buttonStop.setOnClickListener {
                    if (idLaguHolder.toString() != songchecker){
                        //Toast.makeText(applicationContext,"Different song selected while system running",Toast.LENGTH_SHORT).show()
                        publish(aState.toInt().toString()+bState.toInt().toString()+cState.toInt().toString()+songchecker.toString() + "S")
                        state = 0
                        switchlayout.isVisible = true
                        buttonPlay.setText("Play")
                        stateChecker()
                    }
                    else{
                        publish(aState.toInt().toString()+bState.toInt().toString()+cState.toInt().toString()+idLaguHolder.toString() + "S")
                        state = 0
                        switchlayout.isVisible = true
                        buttonPlay.setText("Play")
                        stateChecker()
                    }

                }
                buttonPlay.setOnClickListener {
                    if (idLaguHolder.toString() != songchecker){
                        Toast.makeText(applicationContext,"Different song selected while system running",Toast.LENGTH_SHORT).show()

                    }
                    else{
                        publish(aState.toInt().toString()+bState.toInt().toString()+cState.toInt().toString()+idLaguHolder.toString() + "P")
                        playFlag = false
                        buttonPlay.setText("Unpause")
                        switchA.isClickable = true
                        switchB.isClickable = true
                        switchC.isClickable = true
                        state = 2
                        stateChecker()
                    }
                }

            }
            else if(state == 2){
                buttonStop.setOnClickListener {
                    if (idLaguHolder.toString() != songchecker){
                        Toast.makeText(applicationContext,"Different song selected while system running",Toast.LENGTH_SHORT).show()

                    }
                    else{
                        publish(aState.toInt().toString()+bState.toInt().toString()+cState.toInt().toString()+idLaguHolder.toString() + "S")
                        state = 0
                        switchlayout.isVisible = true
                        buttonPlay.setText("Play")
                        stateChecker()
                    }
                }
                buttonPlay.setOnClickListener {
                    if (idLaguHolder.toString() != songchecker){
                        Toast.makeText(applicationContext,"Different song selected while system running",Toast.LENGTH_SHORT).show()

                    }
                    else{
                        publish(aState.toInt().toString()+bState.toInt().toString()+cState.toInt().toString()+idLaguHolder.toString() + "U")
                        buttonPlay.setText("Pause")
                        switchlayout.isVisible = false
                        state = 1
                        stateChecker()
                    }
                }
            }
            else{
                Toast.makeText(applicationContext,"Error occured, please check state value",Toast.LENGTH_SHORT).show()
            }
        }
        stateChecker()



        lvSonglist.setOnItemClickListener{
            parent, view, position, id ->
            var songNameHolder = songNameListHolder[position]
            Toast.makeText(applicationContext,"Song: $songNameHolder selected",Toast.LENGTH_SHORT).show()
            idLaguHolder = idLaguListHolder[position]

        }

        lvSonglist.setOnItemLongClickListener { parent, view, position, id ->
            var songNameHolder = songNameListHolder[position]
            var songIDholder = idLaguListHolder[position]
            var songArtistHolder = artistNameListHolder[position]
            var nadaHolder = nadaListHolder[position]
            val dialogView = LayoutInflater.from(this).inflate(R.layout.add_queue_song_dialog, null)
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Queue $songNameHolder")
            builder.setView(dialogView)
            val alertdialog = builder.show()
            val okbutton = dialogView.dialogButtonQueueOk
            val dialogcancelbutton = dialogView.dialogButtonQueueCancel
            val aCB = dialogView.checkBoxAngklung
            val bCB = dialogView.checkBoxBonang
            val cCb = dialogView.checkBoxSuling
            var stateCbA = false
            var stateCbB = false
            var stateCbC = false
            aCB.setOnClickListener {
                stateCbA = !stateCbA
            }
            bCB.setOnClickListener {
                stateCbB = !stateCbB
            }
            cCb.setOnClickListener {
                stateCbC = !stateCbC
            }
            okbutton.setOnClickListener {
                if (stateCbA.toInt() == 0 && stateCbB.toInt() == 0 && stateCbC.toInt() == 0){
                    Toast.makeText(applicationContext,"Please select at least one instrument", Toast.LENGTH_SHORT).show()
                }
                else{
                    publish(stateCbA.toInt().toString()+stateCbB.toInt().toString()+stateCbC.toInt().toString()+songIDholder + "Q")
                    val songs = Songs(
                        songIDholder,
                        songNameHolder,
                        songArtistHolder,
                        nadaHolder
                    )
                    if(playFlag == true){
                        queueList!!.add(songs)
                        Toast.makeText(applicationContext,"Added $songNameHolder to queue list",Toast.LENGTH_SHORT).show()
                        alertdialog.dismiss()
                    }
                    else{
                        alertdialog.dismiss()
                        switchlayout.isVisible = false
                        songchecker = idLaguHolder.toString()
                        playFlag = true
                        buttonPlay.setText("Pause")
                        state = 1
                        stateChecker()
                    }
                }

            }
            dialogcancelbutton.setOnClickListener {
                alertdialog.dismiss()

            }


            true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.my_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.getItemId()
        if (id == R.id.action_refresh){
            try{
                loadSong()
                Toast.makeText(applicationContext,"Page reloaded",Toast.LENGTH_SHORT).show()
            }
            catch (e:Exception){

            }
        }
        if (id == R.id.action_queuelist){
            val dialogView = LayoutInflater.from(this).inflate(R.layout.queue_list_layout, null)
            val builder = AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            builder.setView(dialogView)
            val alertdialog = builder.show()
            val playlistokbutton = dialogView.buttonQueueOk
            val clearallbutton = dialogView.buttonClearAll
            val lvplaylist = dialogView.lvPlayList
            val adapter = LVAdapter(this, queueList!!)
            lvplaylist?.adapter = adapter
            playlistokbutton.setOnClickListener {
                alertdialog.dismiss()
            }
            clearallbutton.setOnClickListener {
                publish("1110000C")
                queueList!!.clear()
                val adapter = LVAdapter(this, queueList!!)
                lvplaylist?.adapter = adapter
            }


        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadSong() {
        songList?.clear()
        val stringreq =
            StringRequest(Request.Method.GET, EndPoints.READ, Response.Listener<String> { s ->

                val obj = JSONObject(s)
                Toast.makeText(applicationContext, "Songs are loaded", Toast.LENGTH_SHORT).show()
                val array = obj.getJSONArray("getsong")
                for (i in 0..array.length() - 1) {
                    val objectSongs = array.getJSONObject(i)
                    val songs = Songs(
                        objectSongs.getString("idGamelanNotasi"),
                        objectSongs.getString("judul"),
                        objectSongs.getString("pengarang"),
                        objectSongs.getString("nadaDasar")
                    )
                    songNameListHolder.add(objectSongs.getString("judul"))
                    idLaguListHolder.add(objectSongs.getString("idGamelanNotasi"))
                    artistNameListHolder.add(objectSongs.getString("pengarang"))
                    nadaListHolder.add(objectSongs.getString("nadaDasar"))
                    songList!!.add(songs)
                    val adapter = LVAdapter(this, songList!!)
                    listView!!.adapter = adapter
                }


            }, Response.ErrorListener { volleyError ->
                Toast.makeText(
                    applicationContext, volleyError.message,
                    Toast.LENGTH_SHORT
                ).show()
            })
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add<String>(stringreq)
    }

    fun initmqtt(){
        var clientid = MqttClient.generateClientId()
        client = MqttAndroidClient(applicationContext, SERVERURL, clientid)
        connect()
    }

    fun connect(){
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.setAutomaticReconnect(true)
        mqttConnectOptions.setCleanSession(false)
        mqttConnectOptions.setUserName(USERNAME)
        mqttConnectOptions.setPassword(PASSWORD.toCharArray())
        try
        {
            client!!.connect(mqttConnectOptions, null, object: IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.setBufferEnabled(true)
                    disconnectedBufferOptions.setBufferSize(100)
                    disconnectedBufferOptions.setPersistBuffer(false)
                    disconnectedBufferOptions.setDeleteOldestMessages(false)
                    client!!.setBufferOpts(disconnectedBufferOptions)
                    subscribe("feedback")

                }
                override fun onFailure(asyncActionToken: IMqttToken, exception:Throwable) {
                    Log.e(TAG,"connect failure")
                }
            })
        }
        catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(topic :String){
        try
        {
            client!!.subscribe(topic, 0, null, object:IMqttActionListener {
                override fun onSuccess(asyncActionToken:IMqttToken) {
                    Toast.makeText(applicationContext,"Subscribed $topic", Toast.LENGTH_SHORT).show()
                    //code below edited for receive msg
                    if(client!!.isConnected()){
                        receive()
                    }
                    //end of edited code
                }
                override fun onFailure(asyncActionToken:IMqttToken, exception:Throwable) {

                }
            })
        }
        catch (e: MqttException) {
            System.err.println("Exception subscribing")
            e.printStackTrace()
        }
    }

    fun publish(message :String){
        try{
            client!!.publish(TOPIC, message.toByteArray(), 0, false, null, object :IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {

                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {

                }
            })
        }
        catch (e :MqttException){

        }
    }

    fun receive(){
        client!!.setCallback(object :MqttCallback{
            val timer = object :CountDownTimer(7000,1000){
                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {
                    timerFlag = false
                }
            }
            override fun connectionLost(cause: Throwable?) {
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                try{
                    val data = String(message!!.payload, charset("UTF-8"))
                    if(data == "DONEA" || data == "DONEB" || data == "DONEC"){
                        if(timerFlag == false){
                            timer.start()
                            queueList?.removeAt(0)
                            timerFlag = true
                        }
                    }
                }
                catch (e :Exception){
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }
        })

    }

    fun Boolean.toInt(): Int {
        return if (this) 1 else 0
    }


    companion object{
        val USERNAME = "wzvpnnzz"
        val PASSWORD = "6iAkalhGAkTq"
        val SERVERURL = "tcp://farmer.cloudmqtt.com:15434"
        val TOPIC = "command"
        val TAG = "MQTT"
    }

}
