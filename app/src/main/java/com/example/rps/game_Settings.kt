package com.example.rps


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class game_Settings : AppCompatActivity() {



    var soundX:Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gamesettings)



        val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
        var sound = sharP.getBoolean("soundSt",true)
        soundX= sound
        val sharedP = getSharedPreferences("RPS", MODE_PRIVATE)
        val duckSettingsIV :ImageView = findViewById(R.id.duckSettingsIV)
        val roundseekbar = findViewById<SeekBar>(R.id.round_seekBar)
        val timeseekbar = findViewById<SeekBar>(R.id.time_seekBar)
        val timeTV = findViewById<TextView>(R.id.time_textView)
        val roundsTV = findViewById<TextView>(R.id.rounds_Textview)
        val startB = findViewById<ImageButton>(R.id.start_Round_Button)
       val secretmoveS = findViewById<Switch>(R.id.ultsign_Switcher)
        val duckyIV713:ImageView = findViewById(R.id.duckyIV713)
        if(!secretmoveS.isChecked){
            duckSettingsIV.visibility=View.GONE
            duckyIV713.visibility=View.GONE
        }

        secretmoveS.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                duckSettingsIV.visibility=View.VISIBLE
                duckyIV713.visibility = View.VISIBLE
            } else {
                duckSettingsIV.visibility = View.GONE
                duckyIV713.visibility=View.GONE
            }
        }
       val start_r_b= findViewById<ImageButton>(R.id.start_Round_Button)
        val privateCB = findViewById<CheckBox>(R.id.isPrivate)
        val privateET = findViewById<EditText>(R.id.passwordEditText)
        val lobbyET = findViewById<EditText>(R.id.lobbyNameEditText)
        privateET.visibility = View.GONE

        start_r_b.setImageResource(R.drawable.startgamebut)
        val textView6:TextView = findViewById(R.id.textView6)

        var mode = intent.getIntExtra("gameMode713",1)
        if(mode==2){
            textView6.text="Настройки комнаты"
            start_r_b.setImageResource(R.drawable.create_room_button)
            privateCB.setOnCheckedChangeListener{buttonView, isChecked ->
                if(isChecked){
                      privateET.visibility = View.VISIBLE
                }
                else{
                    privateET.visibility = View.GONE
                }
            }
        }
        else{

             val roomTV = findViewById<TextView>(R.id.lobbyName)

             var elementsToHidelist = arrayOf(privateCB,privateET,roomTV,lobbyET)
             for (element in elementsToHidelist){
                 element.visibility = View.GONE
             }
        }
        secretmoveS.isChecked = sharedP.getBoolean("valueU",false)
        roundseekbar.progress=sharedP.getInt("valueR",3)
        timeseekbar.progress= sharedP.getInt("valueT",3)
        timeTV.text = "${timeseekbar.progress}"
        roundsTV.text="${roundseekbar.progress}"

       var wasPressed = false

        startB .setOnClickListener(){
            playSound(this,R.raw.click_sound)
            if(mode == 2){
            var isPrivate = privateCB.isChecked

            if(lobbyET.text.toString().isNullOrEmpty()){
                Toast.makeText(this,"Введите название комнаты!",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(isPrivate ){
             if(privateET.text.toString().isNullOrEmpty()){
                 Toast.makeText(this,"Введите пароль!",Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
             }
            }

                val playerList: HashMap<String, Player> = HashMap()
                val database = FirebaseDatabase.getInstance()
                val roomRef = database.getReference("Rooms")
                val playersRef = database.getReference("Players")
                val playerID= sharedP.getString("playerID","-")
                val playerRef = playersRef.child(playerID.toString())
                val roomsRef = database.getReference("Rooms")
                var roomID =""
                var roomName = ""

                playerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val player = dataSnapshot.getValue(Player::class.java)

                        if (player != null) {
                            player.hasRoom=false
                            val playerRoomID = player.roomID
                            roomsRef.addListenerForSingleValueEvent(object:ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                         for (roomSnapshot in snapshot.children)  {
                                             val room = roomSnapshot.getValue(Room::class.java)
                                             if(room!= null){
                                            if(playerRoomID==room.roomID)
                                            {
                                                player.hasRoom=true
                                                roomID = room.roomID
                                                roomName = room.name
                                            }
                                             }
                                         }
                                    if(!player.hasRoom) {
                                        val roomID = roomRef.push().key
                                        player.hasRoom = true
                                        player.roomID = roomID.toString()
                                        playerList.put(playerID.toString(), player)
                                        playerRef.setValue(player)

                                        val curGameStats = GameStats(
                                            roundseekbar.progress,
                                            timeseekbar.progress,
                                            secretmoveS.isChecked
                                        )

                                        val room = Room(
                                            lobbyET.text.toString(),
                                            isPrivate,
                                            privateET.text.toString(),
                                            playerList,
                                            2,
                                            curGameStats,
                                            mutableListOf(),
                                            roomID.toString(),
                                            playerID.toString(),
                                            true,
                                            false
                                        )
                                        if(!wasPressed){
                                            wasPressed=true
                                        roomRef.child(roomID.toString()).setValue(room).addOnCompleteListener{
                                        var intent = Intent(this@game_Settings,inLobby::class.java)
                                            intent.putExtra("roomId", roomID)
                                        startActivity(intent)
                                            dataSave(timeseekbar.progress, roundseekbar.progress, secretmoveS.isChecked)
                                        finish()

                                        }
                                        }
                                        else{
                                            Toast.makeText(this@game_Settings,"Немного подождите",Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else {
                                        val dialog = Dialog(this@game_Settings)
                                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                        dialog.setContentView(R.layout.customdialog)
                                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                        dialog.window?.setLayout(
                                            WindowManager.LayoutParams.WRAP_CONTENT,
                                            WindowManager.LayoutParams.WRAP_CONTENT
                                        )
                                        dialog.setCancelable(false)

                                        val caption = dialog.findViewById<TextView>(R.id.captionTV)
                                        val message = dialog.findViewById<TextView>(R.id.messageTV)
                                        val negativeButton = dialog.findViewById<ImageButton>(R.id.negativeButton)
                                        val positiveButton = dialog.findViewById<ImageButton>(R.id.positiveButton)

                                        caption.text = "Комната уже существует"
                                        message.text = "У вас уже есть комната с названием $roomName.Одновременно,вы можете иметь только 1 комнату. Хотите удалить её?"
                                        Glide.with(this@game_Settings).load(R.drawable.yes_button).into(positiveButton)
                                        Glide.with(this@game_Settings).load(R.drawable.no_button).into(negativeButton)


                                        negativeButton.setOnClickListener {
                                            playSound(this@game_Settings,R.raw.click_sound)
                                            dialog.dismiss()
                                        }

                                        positiveButton.setOnClickListener {
                                            playSound(this@game_Settings,R.raw.click_sound)
                                            val roomsRef = database.getReference("Rooms")
                                            val lobbyRef = roomsRef.child(roomID)
                                            lobbyRef.child("exists").setValue(false)
                                            lobbyRef.removeValue().addOnSuccessListener {
                                                player.hasRoom = false
                                                playerRef.setValue(player)
                                                Toast.makeText(this@game_Settings, "Комната была удалена успешно", Toast.LENGTH_SHORT).show()
                                            }.addOnFailureListener {
                                                Toast.makeText(this@game_Settings, "Ошибка удаления комнаты", Toast.LENGTH_SHORT).show()
                                            }
                                            dialog.dismiss()
                                        }

                                        dialog.show()

                                        wasPressed = false
                                    }


                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@game_Settings,"Ошибка сети",Toast.LENGTH_SHORT).show()
                                }

                            })

                        } else {

                            Toast.makeText(this@game_Settings,  "Что-то пошло не так, обратитесь к разработчику!",Toast.LENGTH_SHORT).show()

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@game_Settings, "${databaseError.toString()}", Toast.LENGTH_SHORT).show()
                    }
                })}
              if(mode == 1 ){
                startGame(mode,"-")
                dataSave(timeseekbar.progress, roundseekbar.progress, secretmoveS.isChecked)}


        }


        // Добавляем слушатель событий для roundseekbar
        roundseekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                roundsTV.text="${progress}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Добавляем слушатель событий для timeseekbar
        timeseekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                timeTV.text = "${progress}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })



    }
    fun playSound(context: Context, soundResId: Int) {
        if(soundX){
            MediaPlayer.create(context, soundResId)?.apply {
                setOnCompletionListener { release() }
                start()
            }}
    }

    fun dataSave (valueT:Int, valueR:Int, valueU:Boolean){
     val sharedPref = getSharedPreferences("RPS", MODE_PRIVATE)
      val editor = sharedPref.edit()
      editor.putInt("valueT",valueT)
      editor.putInt("valueR",valueR)
        editor.putBoolean("valueU",valueU)
      editor.apply()
    }

    fun startGame (mode:Int,roomId:String){
       var intent = if(mode==1){
      Intent(this,botGameActivity::class.java)
        }
        else {
            Intent(this@game_Settings,inLobby::class.java)
            intent.putExtra("roomId", roomId)
        }
        startActivity(intent)
        finish()
    }
}



