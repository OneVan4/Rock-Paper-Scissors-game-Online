package com.example.rps

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.ims.stub.ImsRegistrationImplBase
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    var soundX:Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
        val editor = sharP.edit()
        var sound = sharP.getBoolean("soundSt",true)
        soundX= sound
        val sound_ImageButton :ImageButton = findViewById(R.id.sound_ImageButton)
        if(sound){
            Glide.with(this).load(R.drawable.sound).into(sound_ImageButton)
        }
        else{
            Glide.with(this).load(R.drawable.mute).into(sound_ImageButton)
        }
        sound_ImageButton.setOnClickListener {
            if(sound){
                Glide.with(this).load(R.drawable.mute).into(sound_ImageButton)
                sound=false
                soundX= sound
                editor.putBoolean("soundSt",false).apply()
            }
            else{
                Glide.with(this).load(R.drawable.sound).into(sound_ImageButton)
                sound = true
                soundX= sound
                playSound(this,R.raw.click_sound)
                editor.putBoolean("soundSt",true).apply()
            }
        }

        val playerName = sharP.getString("playerName",null)
        val playerID = sharP.getString("playerID",null)
        val database = FirebaseDatabase.getInstance()
        val playersRef = database.getReference("Players")
        val playerRef = playersRef.child(playerID.toString())
        if(playerID.isNullOrEmpty() || playerName.isNullOrEmpty()){
            val intent = Intent(this,register::class.java)
            startActivity(intent)
            finish()
        }
        else {
            playerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val player = snapshot.getValue(Player::class.java)
                    if (player != null) {
                        Toast.makeText(this@MainActivity,"Привет,$playerName !", Toast.LENGTH_LONG).show()
                    }
                    else{
                        val intent = Intent(this@MainActivity,register::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity,error.toString(),Toast.LENGTH_SHORT).show()
                }


            })

        }

        val logoIV = findViewById<ImageView>(R.id.logoIV)
        Glide.with(this).load(R.drawable.rpslogo).into(logoIV)

        val chatIB:ImageButton = findViewById(R.id.chatIB)
        chatIB.setOnClickListener {
            playSound(this,R.raw.click_sound)
            val intent = Intent(this, chatActivity::class.java)
            startActivity(intent)
        }

        val ave = findViewById<ImageButton>(R.id.how2Play_Button)
        ave.setOnClickListener() {
            playSound(this,R.raw.click_sound)
            val intent = Intent(this, authors::class.java)
            startActivity(intent)

        }
        val rulesButton = findViewById<ImageButton>(R.id.authors_Button)
        rulesButton.setOnClickListener {
            playSound(this,R.raw.click_sound)
            rulesActivity()
        }

        val ratingButton=findViewById<ImageButton>(R.id.rating_Button)
        ratingButton.setOnClickListener {
            playSound(this,R.raw.click_sound)
            ratingActivity()
        }
        val profileButton = findViewById<ImageButton>(R.id.profile_Button)
        profileButton.setOnClickListener(){
            playSound(this,R.raw.click_sound)
          profileActivity()
        }
        val sgButton = findViewById<ImageButton>(R.id.start_Button)
        sgButton.setOnClickListener(){
            playSound(this,R.raw.click_sound)
            startGame()
        }

        val exitBut = findViewById<ImageButton>(R.id.exit_button)
        exitBut.setOnClickListener(){
            exit()
        }

    }
     fun  profileActivity (){
        val intent = Intent(this, profile::class.java)
        startActivity(intent)

          }

    fun startGame (){
        val  intent =  Intent (this, gameMods::class.java )
        startActivity(intent)

    }

    fun rulesActivity(){
        val  intent =  Intent (this, rulesActivity::class.java )
        startActivity(intent)
    }

    fun exit (){
        finish()
        finishAffinity()
    }

    fun ratingActivity(){
        val  intent =  Intent (this, ratingActivity::class.java )
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