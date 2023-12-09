package com.example.rps


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*


class PlayerAdapter(private val playerList: MutableMap<String, Player>,private val context: Context) :
    RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.player_row,
            parent,
            false
        )
        return PlayerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val currentPlayer = playerList.values.toList()[position]
        holder.bind(currentPlayer)
    }

    override fun getItemCount(): Int {
        return playerList.size
    }

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerNameTextView: TextView = itemView.findViewById(R.id.playerNameTV)
        private val playerPicImageButton: ImageView = itemView.findViewById(R.id.playerImageIB)
        private val playerRatingTV: TextView = itemView.findViewById(R.id.playerRatingTV)
        private val isLeaderImageView : ImageView = itemView.findViewById(R.id.isLeaderImageView)

        fun bind(player: Player) {
            playerNameTextView.text = player.name
            playerRatingTV.text = player.rating.toString()
            if( playerIDAdmin== player.name){
                isLeaderImageView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(R.drawable.crown)
                    .into(isLeaderImageView)
            }
            else{
                isLeaderImageView.visibility = View.GONE
            }

            Glide.with(itemView.context)
                .load(player.profileimage)
                .apply(RequestOptions().placeholder(R.drawable.guest))
                .into(playerPicImageButton)
            playerPicImageButton.setOnClickListener{
                val playerInfoDialog = Dialog(itemView.context)
                playerInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                playerInfoDialog.setContentView(R.layout.playerprofile)
                playerInfoDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                playerInfoDialog.window?.setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                playerInfoDialog.setCancelable(true)
                val profileGamesPlayed_TV = playerInfoDialog.findViewById<TextView>(R.id.profileGamesPlayed_TV)
                val profileWinPercent_TV = playerInfoDialog.findViewById<TextView>(R.id.profileWinPercent_TV)
                profileGamesPlayed_TV.text = "Сыграно игр: "+player.gameAmount.toString()
                var playerWins = player.winAmount.toFloat()
                var playerGameAmount = player.gameAmount.toFloat()
                val number  = ((playerWins/playerGameAmount)*100)
                val formattedNumber = String.format("%.1f", number)
                profileWinPercent_TV.text = "Процент побед: "+formattedNumber+"%"
                val profilePic = playerInfoDialog.findViewById<ImageView>(R.id.profilePicIV)
                val playerName = playerInfoDialog.findViewById<TextView>(R.id.profileNameTV)
                val playerRating = playerInfoDialog.findViewById<TextView>(R.id.profileratingTV)
                val playerBio = playerInfoDialog.findViewById<TextView>(R.id.profileBioTV)

                val database = FirebaseDatabase.getInstance()
                val playersRef = database.getReference("Players")
                val playerRef = playersRef.child(player.id)
                val socialRating = playerInfoDialog.findViewById<TextView>(R.id.socialRatingTV)
                var socialValue :Int = player.likes.size - player.dislikes.size
                socialRating.text= socialValue.toString()
                val sharedPreferences  = context.getSharedPreferences("RPS", AppCompatActivity.MODE_PRIVATE)
                val playerID = sharedPreferences.getString("playerID", "-")
                val likebutton = playerInfoDialog.findViewById<ImageButton>(R.id.likeIB)
                val dislikebutton = playerInfoDialog.findViewById<ImageButton>(R.id.dislikeIB)

                if(player.dislikes.containsKey(playerID.toString())){
                    Glide.with(itemView.context).load(R.drawable.dislikedislike).into(dislikebutton)
                }
                else{Glide.with(itemView.context).load(R.drawable.dislike).into(dislikebutton)}

                if(player.likes.containsKey(playerID.toString())){
                    Glide.with(itemView.context).load(R.drawable.likelike).into(likebutton)
                }
                else{Glide.with(itemView.context).load(R.drawable.like).into(likebutton)}

                dislikebutton.setOnClickListener {
                    playerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val player = snapshot.getValue(Player::class.java)
                            if (player != null) {

                                if(!player.dislikes.containsKey(playerID.toString())){
                                    playerRef.child("dislikes").child(playerID.toString()).setValue(true).addOnCompleteListener{
                                        if(player.likes.containsKey(playerID)){
                                            playSound(context,R.raw.like)
                                            playerRef.child("likes").child(playerID.toString()).removeValue().addOnSuccessListener {
                                                socialValue-=2
                                                socialRating.text= socialValue.toString()
                                                Glide.with(itemView.context).load(R.drawable.dislikedislike).into(dislikebutton)
                                                Glide.with(itemView.context).load(R.drawable.like).into(likebutton)
                                            }

                                        }
                                        else{
                                            playSound(context,R.raw.like)
                                            socialValue-=1
                                            socialRating.text= socialValue.toString()
                                            Glide.with(itemView.context).load(R.drawable.dislikedislike).into(dislikebutton)
                                            Glide.with(itemView.context).load(R.drawable.like).into(likebutton)
                                        }
                                    }
                                }
                                else{
                                    playerRef.child("dislikes").child(playerID.toString()).removeValue().addOnCompleteListener{
                                        socialValue+=1
                                        Glide.with(itemView.context).load(R.drawable.dislike).into(dislikebutton)
                                        socialRating.text= socialValue.toString()
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(itemView.context,error.toString(),Toast.LENGTH_SHORT).show()
                        }


                    })

                }
                likebutton.setOnClickListener {
                    playerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val player = snapshot.getValue(Player::class.java)
                            if (player != null) {

                                if(!player.likes.containsKey(playerID.toString())){
                                    playerRef.child("likes").child(playerID.toString()).setValue(true).addOnCompleteListener{
                                        if(player.dislikes.containsKey(playerID)){
                                            playerRef.child("dislikes").child(playerID.toString()).removeValue().addOnSuccessListener {
                                                playSound(context,R.raw.like)
                                                socialValue+=2
                                                socialRating.text= socialValue.toString()
                                                Glide.with(itemView.context).load(R.drawable.likelike).into(likebutton)
                                                Glide.with(itemView.context).load(R.drawable.dislike).into(dislikebutton)
                                            }

                                        }
                                        else{
                                            playSound(context,R.raw.like)
                                            socialValue+=1
                                            socialRating.text= socialValue.toString()
                                            Glide.with(itemView.context).load(R.drawable.likelike).into(likebutton)
                                            Glide.with(itemView.context).load(R.drawable.dislike).into(dislikebutton)
                                        }
                                    }
                                }
                                else{
                                    playerRef.child("likes").child(playerID.toString()).removeValue().addOnCompleteListener {
                                        socialValue-=1
                                        Glide.with(itemView.context).load(R.drawable.like).into(likebutton)
                                        socialRating.text= socialValue.toString()
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(itemView.context,error.toString(),Toast.LENGTH_SHORT).show()
                        }


                    })
                }




                playerName.text=player.name
                playerRating.text ="Рейтинг: "+player.rating
                playerBio.text="Bio: "+player.bio
                Glide.with(itemView.context)
                    .load(player.profileimage)
                    .apply(RequestOptions().placeholder(R.drawable.guest))
                    .into(profilePic)
                playerInfoDialog.show()
            }
        }




    }
    }

class ChatAdapter(private val chatList: MutableList<String>,private val context: Context) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.rowconversation,
            parent,
            false
        )
        return ChatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val currentMessage = chatList[position]
        holder.bind(currentMessage)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val nameTV :TextView = itemView.findViewById(R.id.nameTextViewConversation)
        fun bind(message: String) {
            // Find the index of the first colon (':') in the message
            val colonIndex = message.indexOf(':')
            // Extract the name from the message using the colon index
            val senderName = if (colonIndex != -1) message.substring(0, colonIndex + 1) else ""
            // Extract the message without the sender's name
            val messageContent = if (colonIndex != -1) message.substring(colonIndex + 2) else message
            // Update the TextViews
            messageTextView.text = messageContent
            nameTV.text = senderName
        }
    }
}


 var playerIDAdmin =""


class inLobby : AppCompatActivity() {
    private val chatList: MutableList<String> = mutableListOf()
    private lateinit var chatAdapter: ChatAdapter
    val database = FirebaseDatabase.getInstance()
    val playerList: MutableMap<String, Player> = HashMap()
    var admin : Boolean = false
    var hasStarted :Boolean = false
    private var lobbyEventListener: ValueEventListener? = null
    private var chatEventListener: ValueEventListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_lobby)
        val roomId = intent.getStringExtra("roomId")
        val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
        val playerName = sharP.getString("playerName", "")
        val playerID = sharP.getString("playerID", "-")
        val adapter = PlayerAdapter(playerList,this)
        val roomsRef = database.getReference("Rooms")
        val roomRef = roomsRef.child(roomId.toString())
        val deleteLobbyBut = findViewById<ImageButton>(R.id.deleteLobbyButton)
        val startgameBut = findViewById<ImageButton>(R.id.startLobbyGameButton)
        var sound = sharP.getBoolean("soundSt",true)
        soundX= sound
        //TODO НОВОЕ
       val playerRef = database.getReference("Players").child(playerID.toString())
        playerRef.child("emoji").setValue(-1)
        roomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue(Room::class.java)
                if (room != null) {


                    if (room.admin == playerID.toString()) {
                        admin = true
                    }
                    if (!admin) {
                        deleteLobbyBut.visibility = View.GONE
                        startgameBut.visibility = View.GONE
                    } else {
                        deleteLobbyBut.setOnClickListener() {
                            playSound(this@inLobby,R.raw.click_sound)
                            room.exists = false
                            roomRef.setValue(room)
                            deleteLobbyFromDatabase(roomId.toString())
                        }
                        startgameBut.setOnClickListener() {
                            playSound(this@inLobby,R.raw.click_sound)
                            if (playerList.size != 2) {
                                room.ready = false
                                Toast.makeText(
                                    this@inLobby,
                                    "Недостаточно игроков для запуска игры",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }

                            if (!hasStarted) {
                                room.ready = true
                                roomRef.setValue(room)
                            } else {
                                Toast.makeText(
                                    this@inLobby,
                                    "игра уже запущена",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@inLobby, error.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        val chatRecyclerView = findViewById<RecyclerView>(R.id.worldChatRV)
        chatAdapter = ChatAdapter(chatList,this)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter
        val composeET = findViewById<EditText>(R.id.composeMesET)
        val sendButton = findViewById<ImageButton>(R.id.imageButton2)
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

        val layoutManager = LinearLayoutManager(this)
        val recyclerView = findViewById<RecyclerView>(R.id.playersInRoomRV)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
       var playerAmount :Int = 1
       lobbyEventListener= roomRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val room = dataSnapshot.getValue(Room::class.java)
                if (room != null) {
                    // Очищаем текущий список игроков
                    playerList.clear()
                    // Добавляем игроков из комнаты в playerList
                    for (playerSnapshot in dataSnapshot.child("players").children) {
                        if(playerAmount!=room.players.size){
                            playSound(this@inLobby,R.raw.lobbychange)
                            playerAmount=room.players.size
                        }
                        val playerId = playerSnapshot.key
                        val player = playerSnapshot.getValue(Player::class.java)
                        if(playerId == room.admin){
                            playerIDAdmin = player?.name.toString()
                        }
                        if (playerId != null && player != null) {
                            playerList[playerId] = player
                        }
                    }
                    adapter.notifyDataSetChanged()
                    if (!room.exists) {
                        if(!hasStarted){
                        Toast.makeText(this@inLobby, "Лобби было удалено", Toast.LENGTH_SHORT)
                            .show()}
                        deleteLobbyFromDatabase(roomId.toString())
                        finish()
                    } else {
                        if (playerID == room.admin) {
                            admin = true
                            val deleteLobbyBut = findViewById<ImageButton>(R.id.deleteLobbyButton)
                            val startgameBut = findViewById<ImageButton>(R.id.startLobbyGameButton)
                            deleteLobbyBut.visibility = View.VISIBLE
                            startgameBut.visibility = View.VISIBLE

                            deleteLobbyBut.setOnClickListener() {
                                room.exists = false
                                roomRef.setValue(room)
                                playSound(this@inLobby,R.raw.click_sound)
                            }
                            startgameBut.setOnClickListener() {
                                playSound(this@inLobby,R.raw.click_sound)
                                if (playerList.size != 2) {
                                    Toast.makeText(
                                        this@inLobby,
                                        "Недостаточно игроков для запуска игры",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    if (!hasStarted) {
                                        room.ready = true
                                        roomRef.child("ready").setValue(true)
                                    } else {
                                        Toast.makeText(
                                            this@inLobby,
                                            "Игра уже запущена",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            deleteLobbyBut.visibility = View.GONE
                            startgameBut.visibility = View.GONE
                            admin = false
                        }

                        if (playerList.size == 2 && room.ready && !hasStarted) {
                            hasStarted = true
                            Toast.makeText(
                                this@inLobby,
                                "Игра начнется через 3 секунды",
                                Toast.LENGTH_SHORT
                            ).show()
                            val handler = Handler()
                            handler.postDelayed({
                                var opID = ""
                                for (id in playerList.keys) {
                                    if (id != playerID) {
                                        opID = id
                                    }
                                }

                                if (room.ready && playerList.size == 2) {
                                    val gamesRef = database.getReference("Games")
                                    val dialog = Dialog(this@inLobby)
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                    dialog.setContentView(R.layout.waiting)
                                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                    dialog.window?.setLayout(
                                        WindowManager.LayoutParams.WRAP_CONTENT,
                                        WindowManager.LayoutParams.WRAP_CONTENT
                                    )
                                    dialog.setCancelable(false)
                                    val waitingIV= dialog.findViewById<ImageView>(R.id.waitingIV)
                                    val  waitFor = dialog.findViewById<TextView>(R.id.waitForTV)
                                    Glide.with(this@inLobby).load(R.drawable.wait).into(waitingIV)
                                    waitFor.text ="Ожидание создания игры"
                                    var left = false
                                    val lobbyId = roomId.toString()
                                    val lobbyReadyListener = object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            if (dataSnapshot.hasChild(lobbyId)) {
                                                if(!left){
                                                    vibrateWithIntensity(this@inLobby,100)
                                                    val intent =
                                                        Intent(this@inLobby, onlineGameActivity::class.java)
                                                    left = true
                                                    roomRef.child("ready").setValue(false).addOnSuccessListener {
                                                        dialog.dismiss()
                                                        intent.putExtra("opID", opID)
                                                        intent.putExtra("gameID",roomId.toString())
                                                        intent.putExtra("admin",admin)
                                                        startActivity(intent)
                                                        deleteLobbyFromDatabase(roomId.toString())
                                                        finish()
                                                    }
                                                }
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                        }
                                    }


                                    gamesRef.addValueEventListener(lobbyReadyListener)
                                    if(!left){
                                        dialog.show()}
                                    else{  gamesRef.removeEventListener(lobbyReadyListener)}

                                    if(admin) {
                                       val connections = hashMapOf<String,Boolean>()
                                       connections.put(playerID.toString(), false)
                                       connections.put(opID, false)
                                       val game = Game(room, hashMapOf(),connections,false,"me")
                                      gamesRef.child(roomId.toString()).setValue(game)
                                   }

                                    val handler = Handler(Looper.getMainLooper())
                                    handler.postDelayed({
                                        if(!left) {
                                            roomRef.child("ready").setValue(false).addOnSuccessListener {
                                                gamesRef.removeEventListener(lobbyReadyListener)
                                                dialog.dismiss()
                                                finish()
                                                Toast.makeText(
                                                    this@inLobby,
                                                    "Время ожидания начала игры истекло",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }}
                                    }, 5000)




                                } else {
                                    Toast.makeText(
                                        this@inLobby,
                                        "Недостаточно игроков для запуска игры",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    hasStarted = false
                                    room.ready = false
                                    roomRef.child("ready").setValue(false)
                                }
                            }, 4000)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки при считывании данных комнаты
                Toast.makeText(
                    applicationContext,
                    "Ошибка при считывании данных комнаты",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
        loadChatMessages()
    }

        private fun loadChatMessages() {
        val roomId = intent.getStringExtra("roomId")
        val chatRef = database.getReference("Rooms/$roomId/chat")
            chatEventListener = chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatList.clear()
                playSound(this@inLobby,R.raw.message)
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
        val roomId = intent.getStringExtra("roomId")
        val chatRef = database.getReference("Rooms/$roomId/chat")
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val chatList: MutableList<String> = if (dataSnapshot.exists()) {
                    dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {})
                        ?: mutableListOf()
                } else {
                    mutableListOf()
                }
                chatList.add(message)
                if(chatList.size > 200){
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
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.customdialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val roomId = intent.getStringExtra("roomId")
        val chatRef = database.getReference("Rooms/$roomId/chat")
        val roomsRef = database.getReference("Rooms")
        val roomRef = roomsRef.child(roomId.toString())
        val caption = dialog.findViewById<TextView>(R.id.captionTV)
        val message = dialog.findViewById<TextView>(R.id.messageTV)
        val negativeButton = dialog.findViewById<ImageButton>(R.id.negativeButton)
        val positiveButton = dialog.findViewById<ImageButton>(R.id.positiveButton)

        caption.text = "Покинуть лобби"
        message.text = "Вы действительно хотите покинуть лобби?"
        Glide.with(this).load(R.drawable.yes_button).into(positiveButton)
        Glide.with(this).load(R.drawable.no_button).into(negativeButton)
        negativeButton.setOnClickListener {
            dialog.dismiss()

        }
        positiveButton.setOnClickListener {
            chatEventListener?.let {
                chatRef.removeEventListener(it)
            }
            lobbyEventListener?.let {
                roomRef.removeEventListener(it)
            }

            updatePlayerListOnDestroy()
            dialog.dismiss()
            super.onBackPressed()
        }
        dialog.show()
    }


    override fun onStop() {
        super.onStop()
        updatePlayerListOnDestroy()
        finish()
    }

    private fun updatePlayerListOnDestroy() {

        val roomId = intent.getStringExtra("roomId")
        val roomsRef = database.getReference("Rooms")
        val roomRef = roomsRef.child(roomId.toString())

        if (lobbyEventListener != null) {
            roomRef.removeEventListener(lobbyEventListener!!)
        }
        val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
        val currentPlayerId = sharP.getString("playerID", "-")


        if (playerList.containsKey(currentPlayerId)) {
            playerList.remove(currentPlayerId)

            val database = FirebaseDatabase.getInstance()
            val roomsRef = database.getReference("Rooms")
            val roomRef = roomsRef.child(roomId.toString())

            if (admin && playerList.isNotEmpty()) {
                val newLeaderId = playerList.keys.elementAt(0)
                roomRef.child("admin").setValue(newLeaderId)
            }
            if(playerList.isEmpty()){
                deleteLobbyFromDatabase(roomId.toString())
            }
            else {
                roomRef.child("players").setValue(playerList)
            }
        }
    }

    fun deleteLobbyFromDatabase(lobbyId: String) {
        val database = FirebaseDatabase.getInstance()
        val roomsRef = database.getReference("Rooms")
        val lobbyRef = roomsRef.child(lobbyId)
        lobbyRef.removeValue()


    }
    fun vibrateWithIntensity(context: Context, intensity: Int) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(150, intensity)
            vibrator.vibrate(vibrationEffect)
        } else {
            // For devices with lower API levels
            @Suppress("DEPRECATION")
            vibrator.vibrate(150)
        }
    }
    fun playSound(context: Context, soundResId: Int) {
        if(soundX){
            MediaPlayer.create(context, soundResId)?.apply {
                setOnCompletionListener { release() }
                start()
            }}
    }



}
