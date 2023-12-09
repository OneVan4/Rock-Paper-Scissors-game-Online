package com.example.rps

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class gameMods : AppCompatActivity() {

    var soundX :Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_mods)
        val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
        var sound = sharP.getBoolean("soundSt",true)
        soundX= sound

        val bot_But = findViewById<ImageButton>(R.id.botbattle_Button)
        bot_But.setOnClickListener(){
            playSound(this,R.raw.click_sound)
           startGamesettings(1)
        }
        val online_But = findViewById<ImageButton>(R.id.onlinebattle_Button)
        online_But.setOnClickListener(){
            playSound(this,R.raw.click_sound)
            startGamesettings(2)
        }
    }


    fun startGamesettings(mode :Int){
        val intent =if(mode==1) Intent(this, game_Settings::class.java)else Intent(this,lobbyList::class.java)
        intent.putExtra("gameMode713" ,mode)
        startActivity(intent)
    }

    fun playSound(context: Context, soundResId: Int) {
        if(soundX){
            MediaPlayer.create(context, soundResId)?.apply {
                setOnCompletionListener { release() }
                start()
            }}
    }

}