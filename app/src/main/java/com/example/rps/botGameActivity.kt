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
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import java.util.*


class botGameActivity : AppCompatActivity() {
    var soundX:Boolean = true
    lateinit var selectTimer: CountDownTimer
    lateinit var timer :CountDownTimer
    lateinit var adapter : GridAdapter
    val lastChoicesList = mutableListOf<Int>()
    var ult: Boolean= false
    val names = arrayOf(
        "Артем", "Александр", "Дмитрий", "Михаил", "Иван", "Андрей", "Максим", "Сергей", "Кирилл",
        "Павел", "Никита", "Алексей", "Роман", "Юрий", "Евгений", "Владимир", "Константин",
        "Денис", "Глеб", "Игорь", "Станислав", "Тимофей", "Егор", "Олег", "Владислав", "Федор",
        "Артур", "Георгий", "Тимур", "Николай", "Марк", "Даниил", "Виктор", "Руслан", "Антон",
        "Савелий", "Жан", "Матвей", "Леонид", "Ринат", "Григорий", "Виталий", "Вячеслав",
        "Илья", "Валентин", "Семен", "Захар", "Мирослав", "Тарас", "Арсений", "Давид", "Василий",
        "Герман", "Платон", "Арнольд", "Савва", "Эдуард", "Ярослав", "Родион", "Серафим", "Святослав",
        "Всеволод", "Феликс", "Лев", "Мстислав", "Нестор", "Спартак", "Мефодий", "Лука", "Клим",
        "Анна", "Алиса", "Дарья", "Екатерина", "Ксения", "Елена", "Мария","Машечка", "Ольга", "Надежда",
        "Елизавета", "Ангелина", "Анастасия", "Ирина", "Алена", "Светлана", "Вера", "Наталья", "Юлия",
        "Милана", "Виктория", "Евгения", "Кристина", "Татьяна", "Людмила", "Инна", "Валерия", "Софья",
        "Полина", "Марина", "Варвара", "Диана", "Доминика", "Есения", "Зинаида", "Зарина", "Лариса",
        "Майя", "Ника", "Рената", "Таисия", "Ульяна", "Фаина", "Христина", "Эльвира", "Яна",
        "Тузик", "Барбос", "Рекс", "Шарик", "Бобик", "Мухтар", "Бим", "Шаман", "Чак", "Тайсон",
        "Барон", "Каспер", "Борзик", "Дружок")
    var playerWins :Int =0
    var botWins:Int =0
    val random = Random()
    var playedRounds =0;
    var ultFlagb:Boolean = false
    val pictureList = arrayOf(R.drawable.duck, R.drawable.paper, R.drawable.stone, R.drawable.scissors)


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
        setContentView(R.layout.activity_bot_game)
        val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
        val roundTime: Int = sharP.getInt("valueT", 5)
        val roundAmount: Int = sharP.getInt("valueR", 3)
         ult = sharP.getBoolean("valueU", false)
        var stCount = roundTime
        /*0- утка 1 -бумага 2- камень 3-ножницы */
        val nextroundT = findViewById<TextView>(R.id.nextRoundTimer)
        val botTV = findViewById<TextView>(R.id.botName_TV)
        var sound = sharP.getBoolean("soundSt",true)
        soundX= sound
        val gridView: GridView = findViewById(R.id.lastTools)
        adapter = GridAdapter(this, lastChoicesList)
        gridView.adapter = adapter
        gridView.horizontalSpacing = 6



        val botTool_IV = findViewById<ImageView>(R.id.botTool_IV)
        val playerTool_IV = findViewById<ImageView>(R.id.playerTool_IV)
        Glide.with(this).load(R.drawable.wait).into(playerTool_IV)
        Glide.with(this).load(R.drawable.wait).into(botTool_IV)

        botTV.text = "БОТ " + names.random()
        var result =""
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customDialog.setContentView(R.layout.customdialog)
        customDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        customDialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        customDialog.setCancelable(false)

        val caption = customDialog.findViewById<TextView>(R.id.captionTV)
        val message = customDialog.findViewById<TextView>(R.id.messageTV)
        val negativeButton = customDialog.findViewById<ImageButton>(R.id.negativeButton)
        val positiveButton = customDialog.findViewById<ImageButton>(R.id.positiveButton)
        Glide.with(this).load(R.drawable.play_again_butt).into(positiveButton)
        Glide.with(this).load(R.drawable.leave_game_button).into(negativeButton)

        negativeButton.setOnClickListener {
            finish()
            timer.cancel()
            selectTimer.cancel()
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        positiveButton.setOnClickListener{
            val intent = Intent (this, botGameActivity::class.java)
            timer.cancel()
            selectTimer.cancel()
            finish()
            startActivity(intent)
        }
        val botWinIV = findViewById<ImageView>(R.id.botWinIV)
        val playerWinIV =findViewById<ImageView>(R.id.playerWinIV3)
        botWinIV.visibility = View.GONE
        playerWinIV.visibility =View.GONE
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

        if (!ult) {
            val trB = dialog.findViewById<ImageButton>(R.id.duck_Button)
            trB.visibility = View.GONE
        }

         timer = object : CountDownTimer(4000, 1000) {
            var i :Int = 4
            override fun onTick(millisUntilFinished: Long) {
                i--
                nextroundT.text = "Следующий раунд через $i"
            }

            override fun onFinish() {
                botWinIV.visibility = View.GONE
                playerWinIV.visibility =View.GONE
                dialog.show()
                i=4
                if(playedRounds>roundAmount ){
                    timer.cancel()
                    selectTimer.cancel()
                    dialog.dismiss()
                }
                else{
                    selectTimer.start()
                    dialogRoundinfo.text="Раунд ${playedRounds+1} / $roundAmount"
                }
            }
        }

          selectTimer = object : CountDownTimer((roundTime * 1000).toLong(), 1000) {

            override fun onTick(p0: Long) {
                stCount--
                dialogtimeTV.text = stCount.toString()
                playSound(this@botGameActivity,R.raw.timertick)
            }

            override fun onFinish() {

                    val playerChoice = random.nextInt(3)+1
                    Play(playerChoice)
                    dialog.dismiss()
                    timer.start()
                    stCount=roundTime
            }

        }

        val scissors = dialog.findViewById<ImageButton>(R.id.scissors_Button)
        scissors.setOnClickListener() {
            dialog.dismiss()
            intent.putExtra("chosenTool", 3)
            selectTimer.cancel()
            val playerChoice = intent.getIntExtra("chosenTool",0)
            Play(playerChoice)
            if(playedRounds<roundAmount ){
                timer.start()

            }
            else{
                timer.cancel()
                selectTimer.cancel()

                if(playerWins>botWins){
                    result = "Вы победили !"
                    playSound(this,R.raw.winsound)
                    caption.text = result
                    message.text="Игра завершена со счётом \n"+findViewById<TextView>(R.id.score_Textview).text
                    customDialog.show()
                }
                else if(playerWins<botWins){
                    result= "${botTV.text} победил :("
                    playSound(this,R.raw.losesound)
                    caption.text = result
                    message.text="Игра завершена со счётом \n"+findViewById<TextView>(R.id.score_Textview).text
                    customDialog.show()
                }
                else{
                    result= "Ничья"
                    playSound(this,R.raw.nichyaaaa)
                    caption.text = result
                    message.text="Игра завершена со счётом \n"+findViewById<TextView>(R.id.score_Textview).text
                    customDialog.show()
                }
            }
            stCount=roundTime
        }

        val stone = dialog.findViewById<ImageButton>(R.id.stone_Button)
        stone.setOnClickListener() {
            dialog.dismiss()
            intent.putExtra("chosenTool", 2)
            selectTimer.cancel()
            val playerChoice = intent.getIntExtra("chosenTool",0)
            Play(playerChoice)
            if(playedRounds<roundAmount ){
                timer.start()

            }
            else{

                timer.cancel()
                selectTimer.cancel()
                if(playerWins>botWins){
                    result = "Вы победили !"
                    playSound(this,R.raw.winsound)
                    caption.text = result
                    message.text="Игра завершена со счётом \n"+findViewById<TextView>(R.id.score_Textview).text
                    customDialog.show()
                }
                else if(playerWins<botWins){
                    result= "${botTV.text} победил :("
                    playSound(this,R.raw.losesound)
                    caption.text = result
                    message.text="Игра завершена со счётом \n"+findViewById<TextView>(R.id.score_Textview).text
                    customDialog.show()
                }
                 else{
                     result= "Ничья "
                    playSound(this,R.raw.nichyaaaa)
                    caption.text = result
                    message.text="Игра завершена со счётом \n"+findViewById<TextView>(R.id.score_Textview).text
                    customDialog.show()
                 }
            }
            stCount=roundTime
        }

        val paper = dialog.findViewById<ImageButton>(R.id.paper_Button)
        paper.setOnClickListener() {
            dialog.dismiss()
            intent.putExtra("chosenTool", 1)
            selectTimer.cancel()
            val playerChoice = intent.getIntExtra("chosenTool",0)
            Play(playerChoice)
            if(playedRounds<roundAmount ){
                timer.start()

            }
            else{

                timer.cancel()
                selectTimer.cancel()
                if(playerWins>botWins){
                    result = "Вы победили !"
                    playSound(this,R.raw.winsound)
                    caption.text = result
                    message.text="Игра завершена со счётом \n"+findViewById<TextView>(R.id.score_Textview).text
                    customDialog.show()
                }
                else if(playerWins<botWins){
                    result= "${botTV.text} победил :("
                    playSound(this,R.raw.losesound)
                    caption.text = result
                    message.text="Игра завершена со счётом \n"+findViewById<TextView>(R.id.score_Textview).text
                    customDialog.show()
                }
                else{
                    result= "Ничья "
                    playSound(this,R.raw.nichyaaaa)
                    caption.text = result
                    message.text="Игра завершена со счётом \n"+findViewById<TextView>(R.id.score_Textview).text
                    customDialog.show()
                }
            }
            stCount=roundTime
        }

        val duck = dialog.findViewById<ImageButton>(R.id.duck_Button)
        duck.setOnClickListener() {
            dialog.dismiss()
            intent.putExtra("chosenTool", 0)
            selectTimer.cancel()
            val playerChoice = intent.getIntExtra("chosenTool",0)
            Play(playerChoice)
            if(playedRounds<roundAmount ){
                timer.start()

            }
            else{

                timer.cancel()
                selectTimer.cancel()
                if(playerWins>botWins){
                    result = "Вы победили !"
                    playSound(this,R.raw.winsound)
                    caption.text = result
                    message.text="Игра завершена со счётом \n"+findViewById<TextView>(R.id.score_Textview).text
                    customDialog.show()
                }
                else if(playerWins<botWins){
                    result= "${botTV.text} победил :("
                    playSound(this,R.raw.losesound)
                    caption.text = result
                    message.text="Игра завершена со счётом \n"+findViewById<TextView>(R.id.score_Textview).text
                    customDialog.show()
                }
                else{ result= "Ничья !"}
                caption.text = result
                message.text="Игра завершена со счётом \n"+findViewById<TextView>(R.id.score_Textview).text
                customDialog.show()

            }
            stCount=roundTime
            val trB = dialog.findViewById<ImageButton>(R.id.duck_Button)
            trB.visibility = View.GONE
        }

       timer.start()

    }





    fun Play(playerChoice: Int) {
        val botWinIV = findViewById<ImageView>(R.id.botWinIV)
        val playerWinIV =findViewById<ImageView>(R.id.playerWinIV3)
        var botChoice = 2
        if (ult) {
            botChoice = random.nextInt(4)
        } else {
            botChoice = random.nextInt(3) + 1
        }

        if(botChoice==0 && ultFlagb){
            while(botChoice==0){
                botChoice = random.nextInt(4)
            }
        }
        if(botChoice==0){
            ultFlagb= true
        }

        var animation:Int = when(playerChoice){
            0-> when(botChoice){
                0->0
                1->2
                2->1
                3->3
                else->33
            }
            1-> when(botChoice){
                0->2
                1->6
                2->7
                3->8
                else->33
            }
            2->when(botChoice){
                0->1
                1->7
                2->4
                3->9
                else->33
            }
            3->when(botChoice){
                0->3
                1->8
                2->9
                3->5
                else->33
            }
            else->777
        }

        animate(animation)
        val playerTool = findViewById<ImageView>(R.id.playerTool_IV)
        val botTool = findViewById<ImageView>(R.id.botTool_IV)
        val botName = findViewById<TextView>(R.id.botName_TV).text
        // Определение победителя
        if (playerChoice == botChoice) {
            // Ничья
        } else if (playerChoice == 0) {  // Утка побеждает любой предмет
            // Игрок побеждает
            playerWinIV.visibility =View.VISIBLE
            Glide.with(this).load(R.drawable.playerwin).into(playerWinIV)
            playerWins++
        } else if (botChoice == 0) {  // Утка побеждает любой предмет
            // Бот побеждает
            botWinIV.visibility =View.VISIBLE
            Glide.with(this).load(R.drawable.opwin).into(botWinIV)
            botWins++
        } else {
            // Другие комбинации: бумага побеждает камень, камень побеждает ножницы, ножницы побеждают бумагу
            if ((playerChoice == 1 && botChoice == 2) || (playerChoice == 2 && botChoice == 3) || (playerChoice == 3 && botChoice == 1) ) {
                // Игрок побеждает
                playerWinIV.visibility =View.VISIBLE
                Glide.with(this).load(R.drawable.playerwin).into(playerWinIV)
                playerWins++
            } else {
                botWinIV.visibility =View.VISIBLE
                Glide.with(this).load(R.drawable.opwin).into(botWinIV)
                botWins++
            }
        }


        playerTool.setImageResource(pictureList[playerChoice])
        botTool.setImageResource(pictureList[botChoice])
        playedRounds ++
        addLastChoice(botChoice)
        val scoreTV = findViewById<TextView>(R.id.score_Textview)
        scoreTV.text = "Вы $playerWins : $botWins $botName "
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

        val caption = dialog.findViewById<TextView>(R.id.captionTV)
        val message = dialog.findViewById<TextView>(R.id.messageTV)
        val negativeButton = dialog.findViewById<ImageButton>(R.id.negativeButton)
        val positiveButton = dialog.findViewById<ImageButton>(R.id.positiveButton)

        caption.text = "Выход"
        message.text = "Вы действительно хотите покинуть игру?"
        Glide.with(this).load(R.drawable.yes_button).into(positiveButton)
        Glide.with(this).load(R.drawable.no_button).into(negativeButton)

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        positiveButton.setOnClickListener {
            finish()
            timer.cancel()
            selectTimer.cancel()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun animate (animation:Int){
        val bot_animation_IV: ImageView = findViewById(R.id.bot_animation_IV)
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

    private fun playSound (context: Context,soundResId:Int){
        if(soundX){
            MediaPlayer.create(context,soundResId)?.apply {
                setOnCompletionListener { release() }
                start()
            }
        }
    }

    private fun addLastChoice(choice: Int) {
        lastChoicesList.add(pictureList[choice])

        if (lastChoicesList.size > 3) {
            lastChoicesList.removeAt(0)
        }
        adapter.notifyDataSetChanged()
    }
}