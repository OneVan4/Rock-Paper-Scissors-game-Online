package com.example.rps


import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class chatActivity : AppCompatActivity() {
    private val chatList: MutableList<String> = mutableListOf()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var adapter: PlayerAdapter
    val database = FirebaseDatabase.getInstance()
    val playerList: MutableMap<String, Player> = HashMap()
    private var chatEventListener: ValueEventListener? = null
    private var playerEventListener:ChildEventListener?=null
    var soundX:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
        val playerName = sharP.getString("playerName", "")
        adapter= PlayerAdapter(playerList,this)
        val chatRef = database.getReference("Chat")
        val playerID = sharP.getString("playerID", "-")
        fetchPlayerFromDatabase(playerID.toString())
        var sound = sharP.getBoolean("soundSt",true)
        soundX= sound
        val playersRef = chatRef.child("players")

       playerEventListener= playersRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val playerId = snapshot.key
                playSound(this@chatActivity,R.raw.lobbychange)
                val player = snapshot.getValue(Player::class.java)
                if (playerId != null && player != null) {
                    playerList[playerId] = player
                    addPlayerToColumn(player)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                playSound(this@chatActivity,R.raw.lobbychange)
                val playerId = snapshot.key
                val player = snapshot.getValue(Player::class.java)
                if (playerId != null && player != null) {
                    playerList[playerId] = player
                    addPlayerToColumn(player)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val playerId = snapshot.key
                playSound(this@chatActivity,R.raw.lobbychange)
                if (playerId != null) {
                    playerList.remove(playerId)
                    adapter.notifyDataSetChanged() // Обновляем адаптер
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Не требуется
            }

            override fun onCancelled(error: DatabaseError) {
                // Обработка ошибок
            }
        })
        val chatRecyclerView = findViewById<RecyclerView>(R.id.worldChatRV)
        chatAdapter = ChatAdapter(chatList,this)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter
        val composeET = findViewById<EditText>(R.id.composeMesET2)
        val sendButton = findViewById<ImageButton>(R.id.sendMessageBtn)
        sendButton.setOnClickListener() {
            val message =playerName+": "+composeET.text.toString()
            if (composeET.text.toString().isNotEmpty()&& composeET.text.isNotBlank()) {
                sendMessage(message)
                composeET.text.clear()
            }
            // Make the button transparent and disabled
            sendButton.alpha = 0.5f
            sendButton.isEnabled = false

            // Use a Handler to reset the button after 1 second
            val handler = Handler()
            handler.postDelayed({
                sendButton.alpha = 1.0f
                sendButton.isEnabled = true
            }, 1000) // 1000 milliseconds = 1 second
        }

        val layoutManager = GridLayoutManager(this, 2) // Установка GridLayoutManager с 2 столбцами
        val recyclerView = findViewById<RecyclerView>(R.id.playersInRoomRV)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        loadChatMessages()
    }


    private fun loadChatMessages() {
        val chatRef = database.getReference("Chat").child("chat")
        chatEventListener = chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatList.clear()
                playSound(this@chatActivity,R.raw.message)
                for (messageSnapshot in dataSnapshot.children) {
                    val message = messageSnapshot.getValue(String::class.java)
                    if (message != null) {
                        chatList.add(message)
                        val lastPosition =  chatAdapter.itemCount - 1
                        val chatRecyclerView = findViewById<RecyclerView>(R.id.worldChatRV)
                        chatRecyclerView.layoutManager?.scrollToPosition(lastPosition)
                    }
                }
                chatAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки при считывании сообщений чата
                Toast.makeText(
                    applicationContext,
                    "Ошибка при считывании сообщений чата",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun sendMessage(message: String) {
        val chatRef = database.getReference("Chat").child("chat")
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val chatList: MutableList<String> = if (dataSnapshot.exists()) {
                    dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {})
                        ?: mutableListOf()
                } else {
                    mutableListOf()
                }
                chatList.add(message)
                if(chatList.size > 1000){
                    chatList.removeAt(0)
                }
                chatRef.setValue(chatList)
                    .addOnFailureListener { error ->
                        // Обработка ошибки при отправке сообщения
                        Toast.makeText(
                            applicationContext,
                            "Ошибка при отправке сообщения: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки при считывании сообщений чата
                Toast.makeText(
                    applicationContext,
                    "Ошибка при считывании сообщений чата",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onBackPressed() {
            updatePlayerListOnDestroy()
            super.onBackPressed()
    }


    override fun onStop() {
        super.onStop()
        updatePlayerListOnDestroy()
    }

    override fun onDestroy() {

        updatePlayerListOnDestroy()
        super.onDestroy()
    }

    private fun updatePlayerListOnDestroy() {
        val chatRef = database.getReference("Chat")
        val chatReff = database.getReference("Chat").child("chat")
        chatEventListener?.let {
            chatReff.removeEventListener(it)
        }
        if (chatEventListener != null) {
            chatReff.removeEventListener(chatEventListener!!)
        }
        playerEventListener?.let {
            chatRef.removeEventListener(it)
        }
        val playersRef = chatRef.child("players")
        if(playerEventListener!=null) {
            playersRef.removeEventListener(playerEventListener!!)
        }
        val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
        val currentPlayerId = sharP.getString("playerID", "-")


        if (playerList.containsKey(currentPlayerId)) {
            playerList.remove(currentPlayerId)
            val playersRef = chatRef.child("players")
            playersRef.setValue(playerList)
        }
    }

    fun playSound(context: Context, soundResId: Int) {
        if(soundX){
            MediaPlayer.create(context, soundResId)?.apply {
                setOnCompletionListener { release() }
                start()
            }}
    }


    private fun fetchPlayerFromDatabase(playerID: String) {
        val chatRef = database.getReference("Chat")
        val playersRef = database.getReference("Players")
        val playerscRef = chatRef.child("players")
        playersRef.child(playerID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val player = dataSnapshot.getValue(Player::class.java)
                if (player != null) {

                    playerscRef.child(playerID).setValue(player)
                    adapter.notifyDataSetChanged() // Обновляем адаптер
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки при считывании данных игрока
                Toast.makeText(
                    applicationContext,
                    "Ошибка при считывании данных игрока",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    var currentColumn = 0

    private fun addPlayerToColumn(player: Player) {
        if (currentColumn == 0) {
            currentColumn = 1
        } else {
            currentColumn = 0
        }
        adapter.notifyDataSetChanged() // Обновить адаптер
    }



}