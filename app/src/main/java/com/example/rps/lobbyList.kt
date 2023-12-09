package com.example.rps

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class LobbyAdapter(private val context: Context, private var rooms: List<Room>) :
    RecyclerView.Adapter<LobbyAdapter.LobbyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LobbyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.lobby_list_item,
            parent,
            false
        )
        return LobbyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LobbyViewHolder, position: Int) {
        val room = rooms[position]
        holder.bind(room)
    }

    override fun getItemCount(): Int {
        return rooms.size
    }

    fun submitList(newRooms: List<Room>) {
        rooms = newRooms
        notifyDataSetChanged()
    }

    inner class LobbyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lobbyNameTextView: TextView = itemView.findViewById(R.id.roomNameTextView)
        private val lobbyIconImageView: ImageView = itemView.findViewById(R.id.roomIconImageView)
        private val lobbyInfoButton :ImageButton = itemView.findViewById(R.id.lobbyInfoButt)
        fun bind(room: Room) {
            lobbyNameTextView.text = room.name + "("+room.players.size+"/2)"

            val infoDialog = Dialog(context)
            infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            infoDialog.setContentView(R.layout.lobbyinfo)
            infoDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            infoDialog.window?.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            infoDialog.setCancelable(true)
            val lobbyRoundAmountInfo :TextView = infoDialog.findViewById(R.id.lobbyRoundAmountInfoTV)
            val lobbyRoundTimetInfo : TextView = infoDialog.findViewById(R.id.lobbyRoundTimetInfoTV)
            val withOrWithoutDuckTV : TextView = infoDialog.findViewById(R.id.withOrWithoutDuckTV)
            val averageRatingInLobbyInfo :TextView = infoDialog.findViewById(R.id.averageRatingInLobbyInfo)
            val lobbyadmininfo :TextView = infoDialog.findViewById(R.id.lobbyadmininfo)
            lobbyInfoButton.setOnClickListener {
             lobbyRoundAmountInfo.text = "- Количество раундов: ${room.gameStats.roundAmount}"
             lobbyRoundTimetInfo.text ="- Время на рунд: ${room.gameStats.roundTime}"
              if(room.gameStats.ult){
                  withOrWithoutDuckTV.text="С уточкой"
              }
                else{
                    withOrWithoutDuckTV.text="Без уточки"
                }
               var averageRating :Int =0
               for (player in room.players){
                   averageRating+=player.value.rating
               }
               averageRating = averageRating / room.players.size
              averageRatingInLobbyInfo.text  ="- Средний рейтинг в лобби : $averageRating"
                var adminOfTheRoom =room.players[room.admin]?.name
                lobbyadmininfo.text =  "- Админ: ${adminOfTheRoom}"
                infoDialog.show()
            }
            if(room.privatable) {
                lobbyIconImageView.setImageResource(R.drawable.lock)
            }
            else    {
                lobbyIconImageView.setImageResource(R.drawable.unlock)
            }
            val sharP = context.getSharedPreferences("RPS", AppCompatActivity.MODE_PRIVATE)
            val currentPlayerId = sharP.getString("playerID", "-")
            var waspressed = false
            var ispresent = false
            val database = FirebaseDatabase.getInstance()
            val roomsRef = database.getReference("Rooms")

            // Добавьте обработчик клика на элемент списка
            itemView.setOnClickListener {

                for (room in rooms) {
                    if (room.players.containsKey(currentPlayerId) ) {
                        ispresent= true
                        val dialog = Dialog(context)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setContentView(R.layout.customdialog)
                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.window?.setLayout(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT
                        )
                        dialog.setCancelable(false)

                        val message = dialog.findViewById<TextView>(R.id.messageTV)
                        val negativeButton = dialog.findViewById<ImageButton>(R.id.negativeButton)
                        val positiveButton = dialog.findViewById<ImageButton>(R.id.positiveButton)

                        message.text = "Вы уже состоите в комнате с названием ${room.name}.\nХотите покинуть комнату, чтобы войти в другую?"

                        Glide.with(context).load(R.drawable.yes_button).into(positiveButton)
                        Glide.with(context).load(R.drawable.no_button).into(negativeButton)

                        negativeButton.setOnClickListener {
                            dialog.dismiss()
                        }

                        positiveButton.setOnClickListener {
                            roomsRef.child(room.roomID).child("players").child(currentPlayerId.toString()).removeValue().addOnSuccessListener {
                                ispresent = false
                                Toast.makeText(context, "Вы покинули комнату ${room.name}", Toast.LENGTH_SHORT).show()
                            }
                            dialog.dismiss()
                        }

                        dialog.show()
                }}

                if(!waspressed){

                if(!ispresent) {
                    if (room.privatable) {
                        val playerId = sharP.getString("playerID", "-")
                        val database = FirebaseDatabase.getInstance()
                        var playerTA: Player
                        val dialog = Dialog(context)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setContentView(R.layout.password)
                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.window?.setLayout(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT
                        )
                        dialog.setCancelable(true)
                        val passwordET = dialog.findViewById<EditText>(R.id.passwordET)
                        val dialogIB = dialog.findViewById<ImageButton>(R.id.tryPassIB)
                        dialog.show()
                        dialogIB.setOnClickListener() {

                            if (!waspressed) {
                                waspressed = true
                                if (passwordET.text.toString() == room.password) {
                                    val playersRef = database.getReference("Players")
                                    val playerRef = playersRef.child(playerId.toString())
                                    playerRef.addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val player = snapshot.getValue(Player::class.java)
                                            if (player != null) {
                                                playerTA = player
                                                val text: String = passwordET.text.toString()
                                                if (text == room.password) {
                                                    val roomId = room.roomID
                                                    val roomsRef = database.getReference("Rooms")
                                                    val roomQuery = roomsRef.child(roomId)
                                                    roomQuery.addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                            val room =
                                                                dataSnapshot.getValue(Room::class.java)
                                                            if (room != null) {
                                                                if (playerId != null && player != null) {
                                                                    // Проверяем, не находится ли игрок уже в комнате
                                                                    if (!room.players.containsKey(
                                                                            playerId
                                                                        )
                                                                    ) {
                                                                        room.players[playerId] =
                                                                            playerTA // Добавляем игрока в HashMap с использованием его ID в качестве ключа
                                                                    }
                                                                    roomsRef.child(roomId)
                                                                        .setValue(room)
                                                                        .addOnSuccessListener {
                                                                            if (room.players.size <= 2) {
                                                                                roomsRef.child(
                                                                                    roomId
                                                                                )
                                                                                    .setValue(room)
                                                                                    .addOnSuccessListener {
                                                                                        Toast.makeText(
                                                                                            context,
                                                                                            "Комната полная",
                                                                                            Toast.LENGTH_SHORT
                                                                                        ).show()
                                                                                        val intent =
                                                                                            Intent(
                                                                                                context,
                                                                                                inLobby::class.java
                                                                                            )
                                                                                        intent.putExtra(
                                                                                            "roomId",
                                                                                            roomId
                                                                                        )
                                                                                        context.startActivity(
                                                                                            intent
                                                                                        )
                                                                                        dialog.dismiss()
                                                                                    }
                                                                                    .addOnFailureListener { exception ->
                                                                                        // Обработка ошибки записи данных в базу данных
                                                                                    }
                                                                            } else {
                                                                                Toast.makeText(
                                                                                    context,
                                                                                    "Комната полная",
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                                waspressed = false
                                                                                dialog.dismiss()
                                                                            }
                                                                        }

                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(databaseError: DatabaseError) {
                                                            Toast.makeText(
                                                                context,
                                                                "Что-то пошло не так ",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            waspressed = false
                                                        }
                                                    })
                                                }

                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(
                                                context,
                                                error.toString(),
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            waspressed = false
                                        }


                                    })
                                } else {
                                    Toast.makeText(context, "неверный пароль", Toast.LENGTH_SHORT)
                                        .show()
                                    dialog.dismiss()
                                    waspressed = false
                                }
                            } else {
                                Toast.makeText(context, "Немного подождите", Toast.LENGTH_SHORT)
                                    .show()
                            }

                        }
                    } else {
                        val sharP =
                            context.getSharedPreferences("RPS", AppCompatActivity.MODE_PRIVATE)
                        val playerId = sharP.getString("playerID", "-")
                        val database = FirebaseDatabase.getInstance()
                        var playerTA: Player
                        val playersRef = database.getReference("Players")
                        val playerRef = playersRef.child(playerId.toString())

                        playerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val player = snapshot.getValue(Player::class.java)
                                if (player != null) {
                                    playerTA = player
                                    val roomId = room.roomID
                                    val roomsRef = database.getReference("Rooms")
                                    val roomQuery = roomsRef.child(roomId)
                                    roomQuery.addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            val room = dataSnapshot.getValue(Room::class.java)
                                            if (room != null) {
                                                if (playerId != null && player != null) {
                                                    // Проверяем, не находится ли игрок уже в комнате
                                                    if (!room.players.containsKey(playerId)) {

                                                        room.players[playerId] =
                                                            playerTA // Добавляем игрока в HashMap с использованием его ID в качестве ключа
                                                    }
                                                    if (room.players.size <= 2) {
                                                        roomsRef.child(roomId).setValue(room)
                                                            .addOnSuccessListener {
                                                                if (!waspressed) {
                                                                    waspressed = true
                                                                    val intent =
                                                                        Intent(
                                                                            context,
                                                                            inLobby::class.java
                                                                        )
                                                                    intent.putExtra(
                                                                        "roomId",
                                                                        roomId
                                                                    )
                                                                    context.startActivity(intent)
                                                                } else {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "немного подождите",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                            .addOnFailureListener { exception ->
                                                                // Обработка ошибки записи данных в базу данных
                                                            }
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Комната полная",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        waspressed = false
                                                    }

                                                }
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            Toast.makeText(
                                                context,
                                                "Что-то пошло не так ",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            waspressed = false
                                        }
                                    })
                                }

                            }


                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT)
                                    .show()
                                waspressed = false
                            }


                        })

                    }
                }
                }
                else{
                    Toast.makeText(context,"Немного подождите",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


class lobbyList : AppCompatActivity() {
    private lateinit var adapter: LobbyAdapter
    private val rooms = mutableListOf<Room>()
    var soundX:Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby_list)
        val database = FirebaseDatabase.getInstance()
        val roomsRef = database.getReference("Rooms")
        val etcase = findViewById<TextView>(R.id.et_case_tv)
        val searchFOrLObbyET = findViewById<EditText>(R.id.searchForLobbyET)
        val searchButton =findViewById<ImageButton>(R.id.searchButton)
        val createRoomButton :ImageButton = findViewById(R.id.createRoomIB)
        val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
        var sound = sharP.getBoolean("soundSt",true)
        soundX= sound
        createRoomButton.setOnClickListener {
            playSound(this,R.raw.click_sound)
            val intent = Intent (this,game_Settings::class.java)
            intent.putExtra("gameMode713" ,2)
            startActivity(intent)
        }
        adapter = LobbyAdapter(this, rooms)
        searchButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val searchText = searchFOrLObbyET.text.toString()
                filterLobbyList(searchText)
                playSound(this,R.raw.click_sound)
                // Закрываем клавиатуру
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(searchFOrLObbyET.windowToken, 0)
            }
            false
        }

        searchFOrLObbyET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Не требуется
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Вызывается при изменении текста в EditText
                val searchText = s.toString()
                filterLobbyList(searchText)
            }

            override fun afterTextChanged(s: Editable?) {
                // Не требуется
            }
        })
        searchFOrLObbyET.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Выполните действия, которые вам нужны после нажатия на клавишу "Done"
                // Например, скрыть клавиатуру
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(searchFOrLObbyET.windowToken, 0)

                // Возвращаем true, чтобы указать, что событие обработано
                true
            } else {
                false
            }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.lobbiesRV)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        val lobbyListListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                rooms.clear()
                for (roomSnapshot in dataSnapshot.children) {
                    val room = roomSnapshot.getValue(Room::class.java)
                    if (room != null) {
                        if(room.exists ==false || room.players.size <=0){
                             roomsRef.child(room.roomID).removeValue()
                        }
                        else {
                           rooms.add(room)
                        }

                    }
                }
                if(rooms.isEmpty()){
                   etcase.visibility = View.VISIBLE
                }
                else{
                    etcase.visibility = View.GONE
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }

        roomsRef.addValueEventListener(lobbyListListener)
    }
    fun playSound(context: Context, soundResId: Int) {
        if(soundX){
            MediaPlayer.create(context, soundResId)?.apply {
                setOnCompletionListener { release() }
                start()
            }}
    }

    private fun filterLobbyList(searchText: String) {
        val filteredRooms = if (searchText.isEmpty()) {
            rooms
        } else {
            rooms.filter { room ->
                room.name.contains(searchText, ignoreCase = true)
            }
        }
        adapter.submitList(filteredRooms)
    }


}

