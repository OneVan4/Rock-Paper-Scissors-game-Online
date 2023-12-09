package com.example.rps

import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import org.w3c.dom.Text

class rulesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rules)
        val rulesTV = findViewById<TextView>(R.id.rulesTV)
        rulesTV.text ="Добро пожаловать в Quack Attack!\n\nНаверное каждому  в детстве приходилось играть  в игру \"камень ножницы бумага \", решая какой-либо серьезный спор , будь то вопрос , кто пойдет за мячиком , или ,кто будет сегодня убирать комнату .  Наиболее аристократичным и честным способом решения любого спора , или вопроса , где нужно было случайным образом выбрать  проигравшего , была эта игра . Это приложение представляет ее модифицированную версию . В Quack Attack можно  решать споры , соревноваться , весело проводить время в режиме онлайн . Правила игры простые : Всё та же игра детства , только появилась возможность включить утенка , который придет на помощь в трудную минуту и решит твои проблемы , победив любой инструмент противника . Обратите внимание : Утенка можно использовать лишь один  раз за игру !\n\nЖелаю вам удачи и веселого времяпровождения :)Cheers\n"
        val imageView :ImageView = findViewById(R.id.duckyRulesIV)
        imageView.setOnClickListener{
            playSound(this, R.raw.duck_sound)
        }
    }
    fun playSound(context: Context, soundResId: Int) {
        MediaPlayer.create(context, soundResId)?.apply {
            setOnCompletionListener { release() }
            start()
        }
    }
}