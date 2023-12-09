package com.example.rps
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*


class onlineGameActivity : AppCompatActivity() {

    var soundX:Boolean = true
    val lastChoicesList = mutableListOf<Int>()
    lateinit var adapter :GridAdapter
    var playerWins :Int =0
    var opWins:Int =0
    var playedRounds =0;
    var ultFlagb:Boolean = false
    val pictureList = arrayOf(R.drawable.duck, R.drawable.paper, R.drawable.stone, R.drawable.scissors)
    val emojiSoundList = arrayOf(R.raw.please,R.raw.omg,R.raw.love,R.raw.laughing,R.raw.glasses,R.raw.clapping)
    val emojiList = arrayOf(R.drawable.pleaseee, R.drawable.omg, R.drawable.loveee, R.drawable.haha,R.drawable.glasses,R.drawable.clapclap)
    lateinit var selectTimer: CountDownTimer
    lateinit var timer :CountDownTimer
    lateinit var timer713 :CountDownTimer
    var currentRoom :Room = Room()
    private var lobbyEventListener: ValueEventListener? = null
    private var emojiEventListener: ValueEventListener? = null
    var opName :String = ""
    var connectionTimerValue :Int = 10
    var playerName : String =" "
    val database = FirebaseDatabase.getInstance()
    var hasStarted = false
    var isOnGoing = false
    var restarReq =false
    private lateinit var connectionsDialog: Dialog
    var welefttogether = false
    var admin = false
    var waiting = false
    var leftToLobbyFlag = false
    var Emoji = true
    var roundAmount2 :Int = 3
    var  wasDuckUsed = false
    private var isTimerRunning = false
    class GridAdapter(private val context: Context, private val pictures: List<Int>) : BaseAdapter() {

        override fun getCount(): Int {
            return pictures.size
        }

        override fun getItem(position: Int): Any {
            return pictures[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = convertView ?: inflater.inflate(R.layout.grid_item_last_choice, null)
            val imageView: ImageView = view.findViewById(R.id.imageView)
            imageView.setImageResource(pictures[position])
            return view
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_game)
        val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
        var sound = sharP.getBoolean("soundSt",true)
        soundX= sound
        val playersRef = database.getReference("Players")
        val opID = intent.getStringExtra("opID")
        val playerID = sharP.getString("playerID", "-")
         admin = intent.getBooleanExtra("admin",true)
        val gameId = intent.getStringExtra("gameID")
        val gameRef = database.getReference("Games").child(gameId.toString())
        var roundTime: Int
        var roundAmount: Int
        var stCount :Int
        val  opEmojiImageView = findViewById<ImageView>(R.id.opEmojiImageView)
        val  myEmojiImageView = findViewById<ImageView>(R.id.myEmojiImageView)
        myEmojiImageView.visibility=View.GONE
        opEmojiImageView.visibility= View.GONE
        val emojiButton = findViewById<ImageButton>(R.id.emojiButton)
        val gridView: GridView = findViewById(R.id.lastTools)
        adapter = GridAdapter(this, lastChoicesList)
        gridView.adapter = adapter
        gridView.horizontalSpacing = 6
        val playerEmoji: CardView = findViewById(R.id.playerEmojiIV)
        val opEmoji : CardView = findViewById(R.id.opEmojiCV)
        playerEmoji.visibility=View.GONE
        opEmoji.visibility=View.GONE
        val opWinIV :ImageView = findViewById(R.id.opWinIV)
        val playerWinIV :ImageView = findViewById(R.id.playerWinIV)
        opWinIV.visibility =View.GONE
        playerWinIV.visibility=View.GONE

        emojiButton.setOnClickListener {
            val dialogEmoji = Dialog(this)
            dialogEmoji.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogEmoji.setContentView(R.layout.emoji)
            dialogEmoji.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogEmoji.window?.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialogEmoji.setCancelable(true)
              val pleaseEmojiIV :ImageButton = dialogEmoji.findViewById(R.id.pleaseEmojiIV)
              val omgEmojiIV :ImageButton = dialogEmoji.findViewById(R.id.omgEmojiIV)
              val loveEmojiIV :ImageButton= dialogEmoji.findViewById(R.id.loveEmojiIV)
              val hahaEmojiIV :ImageButton = dialogEmoji.findViewById(R.id.hahaEmojiIV)
              val glassesEmojiIV :ImageButton = dialogEmoji.findViewById(R.id.glassesEmojiIV)
              val clapclapEmojiIV: ImageButton = dialogEmoji.findViewById(R.id.clapclapEmojiIV)
              val emojiFlagButton :ImageButton = dialogEmoji.findViewById(R.id.emojiFlagButton)
            Glide.with(this).load(emojiList[0]).into(pleaseEmojiIV)
            Glide.with(this).load(emojiList[1]).into(omgEmojiIV)
            Glide.with(this).load(emojiList[2]).into(loveEmojiIV)
            Glide.with(this).load(emojiList[3]).into(hahaEmojiIV)
            Glide.with(this).load(emojiList[4]).into(glassesEmojiIV)
            Glide.with(this).load(emojiList[5]).into(clapclapEmojiIV)
            if(Emoji){
                Glide.with(this).load(R.drawable.allowedemoji).into(emojiFlagButton)
            }
            else{
                Glide.with(this).load(R.drawable.deademoji).into(emojiFlagButton)
            }
              emojiFlagButton.setOnClickListener {
                  Emoji = if(Emoji==false){true} else{false}
                  if(Emoji){
                      Glide.with(this).load(R.drawable.allowedemoji).into(emojiFlagButton)
                  }
                  else{
                      Glide.with(this).load(R.drawable.deademoji).into(emojiFlagButton)
                  }
              }

            pleaseEmojiIV.setOnClickListener {
                emojiButton.isEnabled = false
                emojiButton.alpha = 0.5f
                playersRef.child(playerID.toString()).child("emoji").setValue(0).addOnSuccessListener {
                    myEmojiImageView.visibility = View.VISIBLE
                    playerEmoji.visibility=View.VISIBLE
                    playSound(this,emojiSoundList[0])
                    Glide.with(this).load(emojiList[0]).into(myEmojiImageView)
                    Handler().postDelayed({
                        myEmojiImageView.visibility = View.GONE
                        playerEmoji.visibility=View.GONE
                    }, 2000)

                    Handler().postDelayed({
                        // Вернуть кнопке emojiButton доступность и прозрачность
                        emojiButton.isEnabled = true
                        emojiButton.alpha = 1.0f
                    }, 2000 ) // Задержка на 1,5 секунды до восстановления кнопки
                }
                dialogEmoji.dismiss()
            }

            omgEmojiIV.setOnClickListener {
                emojiButton.isEnabled = false
                emojiButton.alpha = 0.5f
                playersRef.child(playerID.toString()).child("emoji").setValue(1).addOnSuccessListener {
                    myEmojiImageView.visibility = View.VISIBLE
                    playerEmoji.visibility=View.VISIBLE
                    playSound(this,emojiSoundList[1])
                    Glide.with(this).load(emojiList[1]).into(myEmojiImageView)
                    Handler().postDelayed({
                        playerEmoji.visibility=View.GONE
                        myEmojiImageView.visibility = View.GONE
                    }, 2000)

                    Handler().postDelayed({
                        // Вернуть кнопке emojiButton доступность и прозрачность
                        emojiButton.isEnabled = true
                        emojiButton.alpha = 1.0f
                    }, 2000 ) // Задержка на 1,5 секунды до восстановления кнопки
                }
                dialogEmoji.dismiss()
            }

            loveEmojiIV.setOnClickListener {
                emojiButton.isEnabled = false
                emojiButton.alpha = 0.5f
                playersRef.child(playerID.toString()).child("emoji").setValue(2).addOnSuccessListener {
                    myEmojiImageView.visibility = View.VISIBLE
                    playerEmoji.visibility=View.VISIBLE
                    playSound(this,emojiSoundList[2])
                    Glide.with(this).load(emojiList[2]).into(myEmojiImageView)
                    Handler().postDelayed({
                        playerEmoji.visibility=View.GONE
                        myEmojiImageView.visibility = View.GONE
                    }, 2000)

                    Handler().postDelayed({
                        // Вернуть кнопке emojiButton доступность и прозрачность
                        emojiButton.isEnabled = true
                        emojiButton.alpha = 1.0f
                    }, 2000 ) // Задержка на 1,5 секунды до восстановления кнопки
                }
                dialogEmoji.dismiss()
            }

            hahaEmojiIV.setOnClickListener {
                emojiButton.isEnabled = false
                emojiButton.alpha = 0.5f
                playersRef.child(playerID.toString()).child("emoji").setValue(3).addOnSuccessListener {
                    myEmojiImageView.visibility = View.VISIBLE
                    playerEmoji.visibility=View.VISIBLE
                    playSound(this,emojiSoundList[3])
                    Glide.with(this).load(emojiList[3]).into(myEmojiImageView)
                    Handler().postDelayed({
                        playerEmoji.visibility=View.GONE
                        myEmojiImageView.visibility = View.GONE
                    }, 2000)

                    Handler().postDelayed({
                        // Вернуть кнопке emojiButton доступность и прозрачность
                        emojiButton.isEnabled = true
                        emojiButton.alpha = 1.0f
                    }, 2000 ) // Задержка на 1,5 секунды до восстановления кнопки
                }
                dialogEmoji.dismiss()
            }

            glassesEmojiIV.setOnClickListener {
                emojiButton.isEnabled = false
                emojiButton.alpha = 0.5f
                playersRef.child(playerID.toString()).child("emoji").setValue(4).addOnSuccessListener {
                    myEmojiImageView.visibility = View.VISIBLE
                    playerEmoji.visibility=View.VISIBLE
                    playSound(this,emojiSoundList[4])
                    Glide.with(this).load(emojiList[4]).into(myEmojiImageView)
                    Handler().postDelayed({
                        playerEmoji.visibility=View.GONE
                        myEmojiImageView.visibility = View.GONE
                    }, 2000)

                    Handler().postDelayed({
                        // Вернуть кнопке emojiButton доступность и прозрачность
                        emojiButton.isEnabled = true
                        emojiButton.alpha = 1.0f
                    }, 2000 ) // Задержка на 1,5 секунды до восстановления кнопки
                }
                dialogEmoji.dismiss()
            }

            clapclapEmojiIV.setOnClickListener {
                emojiButton.isEnabled = false
                emojiButton.alpha = 0.5f
                playersRef.child(playerID.toString()).child("emoji").setValue(5).addOnSuccessListener {
                    myEmojiImageView.visibility = View.VISIBLE
                    playerEmoji.visibility=View.VISIBLE
                    playSound(this,emojiSoundList[5])
                    Glide.with(this).load(emojiList[5]).into(myEmojiImageView)
                    Handler().postDelayed({
                        playerEmoji.visibility=View.GONE
                        myEmojiImageView.visibility = View.GONE
                    }, 2000)

                    Handler().postDelayed({
                        // Вернуть кнопке emojiButton доступность и прозрачность
                        emojiButton.isEnabled = true
                        emojiButton.alpha = 1.0f
                    }, 2000 ) // Задержка на 1,5 секунды до восстановления кнопки
                }
                dialogEmoji.dismiss()
            }


            dialogEmoji.show()
        }

        val scoreTV = findViewById<TextView>(R.id.score_Textview2)
        scoreTV.text ="Ожидаем начала игры"
        val playerIV = findViewById<ImageView>(R.id.playerTool_IVon)
        val opIV = findViewById<ImageView>(R.id.opponentTool_IV)
        val playerNameTV =findViewById<TextView>(R.id.playerName_TVon)
        val opNameTV= findViewById<TextView>(R.id.opName_TV)
        val nextroundT  = findViewById<TextView>(R.id.timerInfo_TV)

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_toolchoicer)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val dialogtimeTV = dialog.findViewById<TextView>(R.id.choiceTimeTV)
        val dialogRoundinfo = dialog.findViewById<TextView>(R.id.roundTV)
        val duck = dialog.findViewById<ImageButton>(R.id.duck_Button)
        val scissors = dialog.findViewById<ImageButton>(R.id.scissors_Button)
        val stone = dialog.findViewById<ImageButton>(R.id.stone_Button)
        val paper = dialog.findViewById<ImageButton>(R.id.paper_Button)

         connectionsDialog = Dialog(this)
        connectionsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        connectionsDialog.setContentView(R.layout.playerstates)
        connectionsDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        connectionsDialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        connectionsDialog.setCancelable(false)

        val playerConTV = connectionsDialog.findViewById<TextView>(R.id.playerNameConTV)
        val playerStateImage = connectionsDialog.findViewById<ImageView>(R.id.playerState)
        val opStateImage = connectionsDialog.findViewById<ImageView>(R.id.opState)
        val opConTV = connectionsDialog.findViewById<TextView>(R.id.opNameConTV)
        val infoConnectionTV =connectionsDialog.findViewById<TextView>(R.id.infoConnectionTV)
        val playerCircularIV :ImageView = findViewById(R.id.playerCircularIV)
        val opCircularIV : ImageView = findViewById(R.id.opCircularIV)

        var connectiontimer :CountDownTimer = object : CountDownTimer(10000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                connectionTimerValue--
                infoConnectionTV.text="Ожидание подключения игроков : "  + connectionTimerValue.toString()
            }

            override fun onFinish() {
            }
        }

        Glide.with(this).load(R.drawable.wait).into(playerIV)
        Glide.with(this).load(R.drawable.wait).into(opIV)

        playersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val opPlayer = snapshot.child(opID.toString()).getValue(Player::class.java)
                val currentPlayer = snapshot.child(playerID.toString()).getValue(Player::class.java)

                if (opPlayer != null && currentPlayer != null) {
                    Glide.with(this@onlineGameActivity).load(currentPlayer.profileimage).into(playerCircularIV)
                    Glide.with(this@onlineGameActivity).load(opPlayer.profileimage).into(opCircularIV)
                    opCircularIV.setOnClickListener {
                        val playerInfoDialog = Dialog(this@onlineGameActivity)
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
                        profileGamesPlayed_TV.text = "Сыграно игр: "+opPlayer.gameAmount.toString()
                        var playerWins = opPlayer.winAmount.toFloat()
                        var playerGameAmount = opPlayer.gameAmount.toFloat()
                        val number  = ((playerWins/playerGameAmount)*100)
                        val formattedNumber = String.format("%.1f", number)
                        profileWinPercent_TV.text = "Процент побед: "+formattedNumber+"%"
                        val profilePic = playerInfoDialog.findViewById<ImageView>(R.id.profilePicIV)
                        val playerName = playerInfoDialog.findViewById<TextView>(R.id.profileNameTV)
                        val playerRating = playerInfoDialog.findViewById<TextView>(R.id.profileratingTV)
                        val playerBio = playerInfoDialog.findViewById<TextView>(R.id.profileBioTV)
                        val likebutton = playerInfoDialog.findViewById<ImageButton>(R.id.likeIB)
                        val dislikebutton = playerInfoDialog.findViewById<ImageButton>(R.id.dislikeIB)
                        val socialRating = playerInfoDialog.findViewById<TextView>(R.id.socialRatingTV)
                        var view23 = playerInfoDialog.findViewById<View>(R.id.view23)
                        likebutton.visibility = View.GONE
                        dislikebutton.visibility =View.GONE
                        socialRating.visibility=View.GONE
                        view23.visibility=View.GONE

                        playerName.text=opPlayer.name
                        playerRating.text ="Рейтинг: "+opPlayer.rating
                        playerBio.text="Bio: "+opPlayer.bio
                        Glide.with(this@onlineGameActivity)
                            .load(opPlayer.profileimage)
                            .apply(RequestOptions().placeholder(R.drawable.guest))
                            .into(profilePic)
                        playerInfoDialog.show()
                    }
                    playerCircularIV.setOnClickListener {
                        val playerInfoDialog = Dialog(this@onlineGameActivity)
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
                        profileGamesPlayed_TV.text = "Сыграно игр: "+currentPlayer.gameAmount.toString()
                        var playerWins = currentPlayer.winAmount.toFloat()
                        var playerGameAmount = currentPlayer.gameAmount.toFloat()
                        val number  = ((playerWins/playerGameAmount)*100)
                        val formattedNumber = String.format("%.1f", number)
                        profileWinPercent_TV.text = "Процент побед: "+formattedNumber+"%"
                        val profilePic = playerInfoDialog.findViewById<ImageView>(R.id.profilePicIV)
                        val playerName = playerInfoDialog.findViewById<TextView>(R.id.profileNameTV)
                        val playerRating = playerInfoDialog.findViewById<TextView>(R.id.profileratingTV)
                        val playerBio = playerInfoDialog.findViewById<TextView>(R.id.profileBioTV)
                        val likebutton = playerInfoDialog.findViewById<ImageButton>(R.id.likeIB)
                        val dislikebutton = playerInfoDialog.findViewById<ImageButton>(R.id.dislikeIB)
                        val socialRating = playerInfoDialog.findViewById<TextView>(R.id.socialRatingTV)
                        var view23 = playerInfoDialog.findViewById<View>(R.id.view23)
                        likebutton.visibility = View.GONE
                        dislikebutton.visibility =View.GONE
                        socialRating.visibility=View.GONE
                        view23.visibility=View.GONE
                        playerName.text=currentPlayer.name
                        playerRating.text ="Рейтинг: "+currentPlayer.rating
                        playerBio.text="Bio: "+currentPlayer.bio
                        Glide.with(this@onlineGameActivity)
                            .load(currentPlayer.profileimage)
                            .apply(RequestOptions().placeholder(R.drawable.guest))
                            .into(profilePic)
                        playerInfoDialog.show()
                    }
                    playerNameTV.text = currentPlayer.name
                    opNameTV.text=opPlayer.name
                    opName= opPlayer.name
                    playerName = currentPlayer.name
                    playerConTV.text=playerName
                    opConTV.text=opName
                    infoConnectionTV.text="Ожидание подключения игроков: "
                    connectionsDialog.show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@onlineGameActivity,
                    error.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

       emojiEventListener= playersRef.child(opID.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val player = dataSnapshot.getValue(Player::class.java)
                if (player != null && Emoji && player.emoji >= 0) {
                    val emoji = emojiList[player.emoji]
                    if (emoji != null) {


                        if(playedRounds>0) {
                            playSound(this@onlineGameActivity,emojiSoundList[player.emoji])
                            opEmoji.visibility=View.VISIBLE
                            Glide.with(this@onlineGameActivity).load(emoji).into(opEmojiImageView)
                            opEmojiImageView.visibility = View.VISIBLE
                        }

                        Handler().postDelayed({
                            opEmojiImageView.visibility = View.GONE
                            opEmoji.visibility=View.GONE
                        }, 2000)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки чтения данных из базы данных
            }
        })

        gameRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange (snapshot: DataSnapshot){
                val room = snapshot.child("room").getValue(Room::class.java)
                if (room!=null) {
                    currentRoom = room
                    val GameStats = room.gameStats
                    roundTime = GameStats.roundTime
                    roundAmount = GameStats.roundAmount
                    roundAmount2 = GameStats.roundAmount
                    ultFlagb = GameStats.ult
                    stCount = roundTime
                    if(!ultFlagb){
                        duck.visibility = View.GONE
                    }
                    //TODO сказончый таймер
                    var i :Int = 6
                    timer = object : CountDownTimer((i*1000).toLong(), 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            i--
                            if(playedRounds>=roundAmount){
                                nextroundT.text = "Конец игры через $i"}
                            else{
                            nextroundT.text = "Следующий раунд через $i"
                            }
                        }

                        override fun onFinish() {
                            //TODO ОЙОЙОЙОЙОЙООЙ НЕ СТАВИТСЯ ТАКОЕ ВРЕМЯ ОЙОЙОЙООЙЙООЙОЙОЙОЙ
                           i=6
                             opWinIV.visibility =View.GONE
                             playerWinIV.visibility=View.GONE
                            isTimerRunning = false
                            if(wasDuckUsed) {
                                duck.visibility = View.GONE
                            }
                            else{
                                duck.visibility=View.VISIBLE
                            }
                            dialog.show()
                            val animation :ImageView = findViewById(R.id.animation_IV)
                            animation.visibility =View.GONE

                            if(playedRounds>=roundAmount ){
                                timer.cancel()
                                dialog.dismiss()
                                if (playerWins > opWins) {
                                    calculateRating(
                                        1,
                                        playerID.toString(),
                                        opID.toString()
                                    )
                                } else if (playerWins < opWins) {
                                    calculateRating(
                                        2,
                                        playerID.toString(),
                                        opID.toString()
                                    )
                                } else {
                                    calculateRating(
                                        0,
                                        playerID.toString(),
                                        opID.toString()
                                    )
                                }
                                selectTimer.cancel()
                                timer713.cancel()
                                dialog.dismiss()
                            }
                            else{
                                Glide.with(this@onlineGameActivity).load(R.drawable.wait).into(playerIV)
                                Glide.with(this@onlineGameActivity).load(R.drawable.wait).into(opIV)
                                selectTimer.start()
                                dialogRoundinfo.text="Раунд ${playedRounds+1} / $roundAmount"
                            }
                        }
                    }
                    timer713 = object : CountDownTimer(((roundTime+1)*1000).toLong(), 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val secondsRemaining = millisUntilFinished / 1000
                            scoreTV.text = "Время на ход : $secondsRemaining сек."
                            playSound(this@onlineGameActivity,R.raw.timertick)
                        }

                        override fun onFinish() {
                            timer.cancel()
                            selectTimer.cancel()
                            dialog.dismiss()
                            gameRef.child("over").addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val isGameOver = dataSnapshot.getValue(Boolean::class.java)
                                    if (isGameOver != null && isGameOver) {
                                        timer.cancel()
                                        selectTimer.cancel()
                                        dialog.dismiss()
                                        calculateRating(2, playerID.toString(),opID.toString())
                                    }
                                 else{
                                        timer.cancel()
                                        selectTimer.cancel()
                                        dialog.dismiss()
                                        gameRef.child("winner").setValue(playerID.toString()).addOnSuccessListener {  gameRef.child("over").setValue(true)  }}
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                  Toast.makeText(this@onlineGameActivity,"Ошибка базы данных",Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }

                    selectTimer = object : CountDownTimer((roundTime * 1000).toLong(), 1000) {

                        override fun onTick(p0: Long) {
                            stCount--
                            dialogtimeTV.text = stCount.toString()
                        }

                        override fun onFinish() {
                            gameRef.child("playerChoices").child(playerID.toString()).setValue(-1).addOnSuccessListener {
                                dialog.dismiss()
                            }
                            selectTimer.cancel()
                            stCount=roundTime
                        }

                    }


                    scissors.setOnClickListener() {
                        gameRef.child("playerChoices").child(playerID.toString()).setValue(3).addOnSuccessListener {
                            dialog.dismiss()
                        }
                        selectTimer.cancel()
                        stCount=roundTime

                    }


                    stone.setOnClickListener() {
                        gameRef.child("playerChoices").child(playerID.toString()).setValue(2).addOnSuccessListener {
                            dialog.dismiss()
                        }
                        selectTimer.cancel()
                        stCount=roundTime
                    }


                    paper.setOnClickListener() {
                        gameRef.child("playerChoices").child(playerID.toString()).setValue(1).addOnSuccessListener {
                            dialog.dismiss()
                        }
                        selectTimer.cancel()
                        stCount=roundTime
                    }

                    duck.setOnClickListener() {
                        gameRef.child("playerChoices").child(playerID.toString()).setValue(0).addOnSuccessListener {
                            dialog.dismiss()
                        }
                        wasDuckUsed = true
                        selectTimer.cancel()
                        stCount=roundTime
                    }

                    gameRef.child("connections").child(playerID.toString()).setValue(true).addOnSuccessListener {
                    lobbyEventListener = gameRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            val game = dataSnapshot.getValue(Game::class.java)
                            if (game == null) {
                                Toast.makeText(
                                    this@onlineGameActivity,
                                    "Игра не найдена",
                                    Toast.LENGTH_SHORT
                                ).show()
                                returnToLobby()
                            } else {
                                if (restarReq) {
                                    if (game.connections[opID] == false) {
                                        Glide.with(this@onlineGameActivity).clear(opStateImage)
                                        Glide.with(this@onlineGameActivity).load(R.drawable.none).into(opStateImage)
                                        gameRef.removeValue().addOnSuccessListener {
                                            val handler = Handler(Looper.getMainLooper())
                                            handler.postDelayed({
                                                connectionsDialog.dismiss()
                                                connectionsDialog.cancel()
                                                connectiontimer.cancel()
                                                connectionTimerValue=10
                                                returnToLobby()
                                                Toast.makeText(
                                                    this@onlineGameActivity,
                                                    "Соперник отказался играть снова",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }, 2000)
                                        }
                                    }
                                    if (game.connections[playerID] == true) {
                                        val handler = Handler(Looper.getMainLooper())
                                        handler.postDelayed({
                                            if (!welefttogether) {
                                                gameRef.child("connections")
                                                    .child(playerID.toString()).setValue(false)
                                                    .addOnSuccessListener {
                                                        gameRef.removeValue()
                                                        connectionsDialog.dismiss()
                                                        connectiontimer.cancel()
                                                        connectionTimerValue=10
                                                        returnToLobby()
                                                        Toast.makeText(
                                                            this@onlineGameActivity,
                                                            "время ожидания игрока истекло",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                        }, 10000)
                                        Glide.with(this@onlineGameActivity)
                                            .load(R.drawable.connected).into(playerStateImage)
                                        connectiontimer.start()
                                    }
                                    if (game.connections[opID] == true) {
                                        Glide.with(this@onlineGameActivity)
                                            .load(R.drawable.connected).into(opStateImage)
                                    }
                                    //TODO новое шото
                                    if (game.connections[playerID] == true && game.connections[opID] == true) {
                                        connectiontimer.cancel()
                                        connectionTimerValue=10
                                        val handler = Handler(Looper.getMainLooper())
                                        val playerRef = database.getReference("Players").child(playerID.toString())
                                        playerRef.child("emoji").setValue(-1)
                                        val playerChoicesSnapshot =
                                            dataSnapshot.child("playerChoices")
                                        playerChoicesSnapshot.ref.removeValue()
                                        gameRef.child("connections").removeValue()
                                        gameRef.child("over").setValue(false).addOnSuccessListener {
                                        handler.postDelayed({
                                                //TODO DOUBLETIMER last chhange
                                            timer.start()
                                            selectTimer.cancel()
                                          lastChoicesList.clear()
                                         opWins = 0
                                          wasDuckUsed = false
                                         playerWins = 0
                                         hasStarted=true
                                         connectionsDialog.dismiss()
                                         dialog.dismiss()
                                            connectiontimer.cancel()
                                            Glide.with(this@onlineGameActivity)
                                                .load(R.drawable.wait).into(opStateImage)
                                            Glide.with(this@onlineGameActivity)
                                                .load(R.drawable.wait).into(playerStateImage)
                                            connectionTimerValue=10
                                            isOnGoing = true
                                            restarReq = false
                                            welefttogether = true
                                            playedRounds=0
                                            val scoreTV = findViewById<TextView>(R.id.score_Textview2)
                                            scoreTV.text = "Вы 0:0 $opName"
                                        }, 1037)
                                    }}
                                }

                                if (!hasStarted && game != null) {
                                    if (game.connections[playerID] == true) {

                                        val handler = Handler(Looper.getMainLooper())
                                        handler.postDelayed({
                                            if (!hasStarted) {
                                                gameRef.child("connections")
                                                    .child(playerID.toString()).setValue(false)
                                                    .addOnSuccessListener {
                                                        gameRef.removeValue()
                                                        connectionsDialog.dismiss()
                                                        connectiontimer.cancel()
                                                        connectionTimerValue=10
                                                        returnToLobby()
                                                        Toast.makeText(
                                                            this@onlineGameActivity,
                                                            "время ожидания игрока истекло",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                        }, 10000)
                                        Glide.with(this@onlineGameActivity)
                                            .load(R.drawable.connected).into(playerStateImage)

                                        connectiontimer.start()
                                    }
                                    if (game.connections[opID] == true) {
                                        Glide.with(this@onlineGameActivity).clear(opStateImage)
                                        Glide.with(this@onlineGameActivity)
                                            .load(R.drawable.connected).into(opStateImage)
                                    }
                                    if (game.connections[playerID] == true && game.connections[opID] == true && !hasStarted) {
                                        hasStarted = true
                                        isOnGoing = true
                                        connectiontimer.cancel()
                                        connectionTimerValue=10
                                        val handler = Handler(Looper.getMainLooper())
                                        handler.postDelayed({
                                            gameRef.child("connections").removeValue()
                                            connectionsDialog.dismiss()
                                            Glide.with(this@onlineGameActivity)
                                                .load(R.drawable.wait).into(opStateImage)
                                            Glide.with(this@onlineGameActivity)
                                                .load(R.drawable.wait).into(playerStateImage)
                                                //TODO DOUBLETIMER
                                            if(!isTimerRunning){
                                            timer.start()
                                            }
                                        }, 1500)
                                    }
                                }
                                if (hasStarted && isOnGoing) {
                                    if (game != null) {
                                        if (game.over) {
                                            if (game.winner == playerID) {
                                                timer.cancel()
                                                selectTimer.cancel()
                                                timer713.cancel()
                                                dialog.dismiss()
                                                calculateRating(
                                                    1,
                                                    playerID.toString(),
                                                    opID.toString()
                                                )
                                            } else {
                                                timer.cancel()
                                                timer713.cancel()
                                                selectTimer.cancel()
                                                dialog.dismiss()
                                                calculateRating(
                                                    2,
                                                    playerID.toString(),
                                                    opID.toString()
                                                )
                                            }
                                        } else {
                                            val playerChoicesSnapshot =
                                                dataSnapshot.child("playerChoices")
                                            if (playerChoicesSnapshot.hasChild(playerID.toString())) {
                                                val playerChoice =
                                                    playerChoicesSnapshot.child(playerID.toString())
                                                        .getValue(Int::class.java)
                                                if (playerChoice != null) {
                                                    waiting = true
                                                    timer713.start()
                                                }
                                            }
                                            if (playerChoicesSnapshot.hasChild(playerID.toString()) && playerChoicesSnapshot.hasChild(
                                                    opID.toString()
                                                )
                                            ) {
                                                val playerChoice =
                                                    playerChoicesSnapshot.child(playerID.toString())
                                                        .getValue(Int::class.java)
                                                val opChoice =
                                                    playerChoicesSnapshot.child(opID.toString())
                                                        .getValue(Int::class.java)
                                                if (playerChoice != null && opChoice != null) {
                                                    waiting = false
                                                    timer713.cancel()
                                                    if (playedRounds >= roundAmount ) {
                                                        // Игра завершена
                                                        timer.cancel()
                                                        selectTimer.cancel()
                                                        dialog.dismiss()
                                                        if (playerWins > opWins) {
                                                            calculateRating(
                                                                1,
                                                                playerID.toString(),
                                                                opID.toString()
                                                            )
                                                        } else if (playerWins < opWins) {
                                                            calculateRating(
                                                                2,
                                                                playerID.toString(),
                                                                opID.toString()
                                                            )
                                                        } else {
                                                            calculateRating(
                                                                0,
                                                                playerID.toString(),
                                                                opID.toString()
                                                            )
                                                        }
                                                    } else {
                                                        stCount = roundTime
                                                        if(!isTimerRunning){
                                                        timer.start()}
                                                        dialogRoundinfo.text =
                                                            "Раунд ${playedRounds + 1} / $roundAmount"
                                                        play(playerChoice, opChoice)
                                                        // Очистка выборов игроков в базе данных после обработки раунда
                                                        playerChoicesSnapshot.ref.removeValue()
                                                    }
                                                }

                                            } else {
                                                if (playerChoicesSnapshot.hasChild(playerID.toString())) {
                                                    val playerChoice =
                                                        playerChoicesSnapshot.child(playerID.toString())
                                                            .getValue(Int::class.java)
                                                    if (playerChoice != null && playerChoice != -1) {
                                                        Glide.with(this@onlineGameActivity)
                                                            .load(pictureList[playerChoice])
                                                            .into(playerIV)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            // Обработка ошибки при считывании данных комнаты
                            Toast.makeText(
                                applicationContext,
                                "Ошибка при считывании данных комнаты",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    })}


                }

            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@onlineGameActivity,
                    error.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }





    fun returnToLobby() {
        val opID = intent.getStringExtra("opID")
        val playersRef = database.getReference("Players")
        val  playerRef = playersRef.child(opID.toString())
        val gameId = intent.getStringExtra("gameID")
        val gameRef = database.getReference("Games").child(gameId.toString())
        lobbyEventListener?.let {
            gameRef.removeEventListener(it)
        }
        emojiEventListener?.let {
            playerRef.removeEventListener(it)
            playersRef.removeEventListener(it)
        }
        if (emojiEventListener != null) {
            playersRef.removeEventListener(emojiEventListener!!)
            playerRef.removeEventListener(emojiEventListener!!)
        }
        if(!leftToLobbyFlag) {
            leftToLobbyFlag = true
            finish()

        }

    }

    private fun play(playerChoice: Int, opChoice: Int) {
        val playerIV = findViewById<ImageView>(R.id.playerTool_IVon)
        val opIV = findViewById<ImageView>(R.id.opponentTool_IV)
        var animation:Int = when(playerChoice){
            0-> when(opChoice){
                0->0
                1->2
                2->1
                3->3
                else->33
            }
            1-> when(opChoice){
                0->2
                1->6
                2->7
                3->8
                else->33
            }
            2->when(opChoice){
                0->1
                1->7
                2->4
                3->9
                else->33
            }
            3->when(opChoice){
                0->3
                1->8
                2->9
                3->5
                else->33
            }
            else->777
        }

        animate(animation)
        val opWinIV :ImageView = findViewById(R.id.opWinIV)
        val playerWinIV :ImageView = findViewById(R.id.playerWinIV)

    if(playerChoice== -1 || opChoice == -1) {
        if (opChoice == -1 && playerChoice == -1) {

            val scoreTV = findViewById<TextView>(R.id.score_Textview2)
            scoreTV.text = "Вы $playerWins : $opWins Оппонент"
            playedRounds
            // Отображение выбранных инструментов игрока и оппонента
            Glide.with(this).load(R.drawable.none).into(playerIV)
            Glide.with(this).load(R.drawable.none).into(opIV)
        }
        if (opChoice == -1 && playerChoice!=-1) {
            playedRounds++
            playerWins++
            val scoreTV = findViewById<TextView>(R.id.score_Textview2)
            scoreTV.text = "Вы $playerWins : $opWins Оппонент"
            // Отображение выбранных инструментов игрока и оппонента
            Glide.with(this).load(pictureList[playerChoice]).into(playerIV)
            Glide.with(this).load(R.drawable.none).into(opIV)
            playerWinIV.visibility=View.VISIBLE
            Glide.with(this).load(R.drawable.playerwin).into(playerWinIV)
        }

        if (playerChoice == -1 && opChoice!=-1) {
            playedRounds++
            opWins++
            val scoreTV = findViewById<TextView>(R.id.score_Textview2)
            scoreTV.text = "Вы $playerWins : $opWins Оппонент"
            // Отображение выбранных инструментов игрока и оппонента
            Glide.with(this).load(R.drawable.none).into(playerIV)
            Glide.with(this).load(pictureList[opChoice]).into(opIV)
            opWinIV.visibility=View.VISIBLE
            Glide.with(this).load(R.drawable.opwin).into(opWinIV)
        }
    }
   else {
        playedRounds++
        // Отображение выбранных инструментов игрока и оппонента
        Glide.with(this).load(pictureList[playerChoice]).into(playerIV)
        Glide.with(this).load(pictureList[opChoice]).into(opIV)

        // Определение победителя
        if (playerChoice == opChoice) {
            // Ничья
        } else if (playerChoice == 0) {  // Утка побеждает любой предмет
            // Игрок побеждает
            playerWinIV.visibility=View.VISIBLE
            Glide.with(this).load(R.drawable.playerwin).into(playerWinIV)
            playerWins++
        } else if (opChoice == 0) {  // Утка побеждает любой предмет
            // Оппонент побеждает
            opWinIV.visibility=View.VISIBLE
            Glide.with(this).load(R.drawable.opwin).into(opWinIV)
            opWins++
        } else {
            // Другие комбинации: бумага побеждает камень, камень побеждает ножницы, ножницы побеждают бумагу
            if ((playerChoice == 1 && opChoice == 2) || (playerChoice == 2 && opChoice == 3) || (playerChoice == 3 && opChoice == 1)) {
                // Игрок побеждает
                playerWinIV.visibility=View.VISIBLE
                Glide.with(this).load(R.drawable.playerwin).into(playerWinIV)
                playerWins++
            } else {
                // Оппонент побеждает
                opWinIV.visibility=View.VISIBLE
                Glide.with(this).load(R.drawable.opwin).into(opWinIV)
                opWins++
            }
        }

    }    // Обновление счета
        val scoreTV = findViewById<TextView>(R.id.score_Textview2)
        scoreTV.text = "Вы $playerWins : $opWins $opName"
        addLastChoice(opChoice)

    }

    private fun addLastChoice(choice: Int) {
        if(choice>=0){
        lastChoicesList.add(pictureList[choice])

        if (lastChoicesList.size > 3) {
            lastChoicesList.removeAt(0)
        }
        adapter.notifyDataSetChanged()
        }
    }

    private fun animate (animation:Int){
        val bot_animation_IV: ImageView = findViewById(R.id.animation_IV)
        bot_animation_IV.visibility = View.VISIBLE
        when (animation){
            0->{
                Glide.with(this).load(R.drawable.dxd).into(bot_animation_IV)
                playSound(this,R.raw.nichyaaaa)
            }
            1->{
                Glide.with(this).load(R.drawable.dxr).into(bot_animation_IV)
                playSound(this,R.raw.duckwinsound)
            }
            2->{
                Glide.with(this).load(R.drawable.dxp).into(bot_animation_IV)
                playSound(this,R.raw.duckwinsound)
            }
            3->{
                Glide.with(this).load(R.drawable.dxs).into(bot_animation_IV)
                playSound(this,R.raw.duckwinsound)
            }
            4->{
                Glide.with(this).load(R.drawable.rxr).into(bot_animation_IV)
                playSound(this,R.raw.nichyaaaa)
            }
            5->{
                Glide.with(this).load(R.drawable.sxs).into(bot_animation_IV)
                playSound(this,R.raw.nichyaaaa)
            }
            6->{
                Glide.with(this).load(R.drawable.pxp).into(bot_animation_IV)
                playSound(this,R.raw.nichyaaaa)
            }
            7->{
                playSound(this,R.raw.paperwin)
                Glide.with(this).load(R.drawable.pxr).into(bot_animation_IV)
            }
            8->{
                Glide.with(this).load(R.drawable.sxp).into(bot_animation_IV)
                playSound(this,R.raw.scissorswin)
            }
            9->{
                playSound(this,R.raw.rockwin)
                Glide.with(this).load(R.drawable.rxs).into(bot_animation_IV)
            }
        }

    }

    private fun playSound (context: Context, soundResId:Int){
        if(soundX){
            MediaPlayer.create(context,soundResId)?.apply {
                setOnCompletionListener { release() }
                start()
            }
        }
    }

    fun calculateRating(gameResult: Int, playerID: String, opID: String) {
        isOnGoing =false
        waiting = false
        val database = FirebaseDatabase.getInstance()
        val playersRef = database.getReference("Players")
        val playerRef = playersRef.child(playerID)
        val opponentRef = playersRef.child(opID)

        playerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(playerSnapshot: DataSnapshot) {
                val player = playerSnapshot.getValue(Player::class.java)
                if (player != null) {
                    val currentPlayerRating = player.rating
                    opponentRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(opponentSnapshot: DataSnapshot) {
                            val opponent = opponentSnapshot.getValue(Player::class.java)
                            if (opponent != null) {
                                val currentOpponentRating = opponent.rating

                                val ratingChange:Int = when (gameResult) {
                                    1 -> {
                                         playSound(this@onlineGameActivity,R.raw.winsound)
                                        // Игрок победил, увеличение рейтинга на 10 + разница в рейтингах / 10
                                        val ratingDifference = currentOpponentRating - currentPlayerRating
                                        (30 + ratingDifference / 10)
                                    }
                                    2 -> {
                                        playSound(this@onlineGameActivity,R.raw.losesound)
                                        val ratingDifference = currentOpponentRating - currentPlayerRating
                                        (-30 + ratingDifference / 10)
                                    }
                                    else -> {
                                        playSound(this@onlineGameActivity,R.raw.nichyaaaa)
                                        0
                                    }
                                }

                                var newPlayerRating = currentPlayerRating + ratingChange
                                if (newPlayerRating < 0) {
                                    newPlayerRating = 0
                                }
                                var playedGames =1
                                var playerWins = 1
                                if(player.gameAmount!=null){
                                playedGames = player.gameAmount + 1
                                }

                                if(gameResult==1){
                                    if(player.winAmount!= null){
                                        playerWins = player.winAmount+1
                                    }
                                }
                                else {
                                    if(player.winAmount!= null){
                                        playerWins = player.winAmount
                                    }
                                    else{playerWins = 0}
                                }
                                playerRef.child("gameAmount").setValue(playedGames)
                                playerRef.child("winAmount").setValue(playerWins)
                                playerRef.child("rating").setValue(newPlayerRating)
                                    .addOnCompleteListener { playerRatingUpdateTask ->
                                        if (playerRatingUpdateTask.isSuccessful) {
                                            showFinalResult(gameResult, ratingChange)
                                        } else {
                                            finish()
                                            // Ошибка при обновлении рейтинга игрока
                                            Toast.makeText(this@onlineGameActivity," Ошибка при обновлении рейтинга игрока",Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        }

                        override fun onCancelled(opponentError: DatabaseError) {
                            // Обработка ошибки при считывании данных соперника
                            Toast.makeText(this@onlineGameActivity,"ошибка при считывании данных соперника ",Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onCancelled(playerError: DatabaseError) {
                // Обработка ошибки при считывании данных игрока
                Toast.makeText(this@onlineGameActivity,"ошибка при считывании данных игрока ",Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showFinalResult (gameResult: Int,ratingChange:Int){
        restarReq = true
        timer713.cancel()
        timer.cancel()
        val scoreTV = findViewById<TextView>(R.id.score_Textview2)
        scoreTV.text = "Вы $playerWins : $opWins $opName"
        val opID = intent.getStringExtra("opID")
        val playersRef = database.getReference("Players")
        val  playerRef = playersRef.child(opID.toString())
        val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
        val playerID= sharP.getString("playerID","-")
        val gameId = intent.getStringExtra("gameID")
        val gameRef = database.getReference("Games").child(gameId.toString())
        val dialog = Dialog(this)
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
        Glide.with(this).load(R.drawable.leave_game_button).into(negativeButton)
        Glide.with(this).load(R.drawable.play_again_butt).into(positiveButton)

        negativeButton.setOnClickListener {
            gameRef.child("connections").child(playerID.toString()).setValue(false).addOnSuccessListener {
            dialog.dismiss()
            finish()
                lobbyEventListener?.let {
                    gameRef.removeEventListener(it)
                }
                emojiEventListener?.let {
                    playerRef.removeEventListener(it)
                    playersRef.removeEventListener(it)
                }
                if (emojiEventListener != null) {
                    playersRef.removeEventListener(emojiEventListener!!)
                    playerRef.removeEventListener(emojiEventListener!!)
                }
            }
        }

        positiveButton.setOnClickListener {
            dialog.dismiss()
            gameRef.child("connections").child(playerID.toString()).setValue(true).addOnSuccessListener {
            connectionsDialog.show()
            }
        }


        when (gameResult) {
            1 -> {
                if(playedRounds<roundAmount2){ caption.text = "Противник сдался "}
                else{caption.text = "Победа!"}
                message.text = "Игра завершена со счетом: \n${scoreTV.text}.\nВы получаете $ratingChange рейтинга!"
            }
            2 -> {
                if(playedRounds<roundAmount2){ caption.text = "Вы сдались =("}
                else{caption.text = "Поражение =("}
                message.text = "Игра завершена со счетом: \n${scoreTV.text}.\nВы теряете $ratingChange рейтинга=("
            }
           0 ->{
               caption.text = "Ничья"
               message.text = "Игра завершена со счетом: \n${scoreTV.text}.\nВаш рейтинг не изменился"
           }
        }


        dialog.show()

    }

    var pressed = false

    override fun onBackPressed() {
        val gameId = intent.getStringExtra("gameID")
        val gameRef = database.getReference("Games").child(gameId.toString())
         if(hasStarted &&isOnGoing) {
             val opID = intent.getStringExtra("opID")
             val dialog = Dialog(this)
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

             caption.text = "Выход"
             message.text = "Вы действительно хотите сдаться?"

             Glide.with(this).load(R.drawable.yes_button).into(positiveButton)
             Glide.with(this).load(R.drawable.no_button).into(negativeButton)

             negativeButton.setOnClickListener {
                 dialog.dismiss()
             }

             positiveButton.setOnClickListener {
                 timer.cancel()
                 selectTimer.cancel()
                 dialog.dismiss()

                 gameRef.child("winner").setValue(opID).addOnSuccessListener {
                     gameRef.child("over").setValue(true)

                 }
             }
             dialog.show()

         }
        else {
             if (!hasStarted && !pressed) {
                 pressed = true
                 val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
                 val playerID = sharP.getString("playerID", "-")
                 gameRef.child("connections").child(playerID.toString()).setValue(false)
                     .addOnSuccessListener {
                         finish()
                     }
             }
         }

    }
    }
