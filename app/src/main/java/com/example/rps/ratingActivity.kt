package com.example.rps

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ratingAdapter (private val playerList :MutableList<Player>,private val context: Context):RecyclerView.Adapter<ratingAdapter.ratingViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ratingAdapter.ratingViewHolder {
      val itemView =LayoutInflater.from(parent.context).inflate(R.layout.ratingrow,parent,false)
      return ratingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ratingViewHolder, position: Int) {
        val currentPlayer = playerList[position]
        holder.bind(currentPlayer)
    }

    override fun getItemCount(): Int {
        return playerList.size
    }

    inner class ratingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ratingPosition : TextView = itemView.findViewById(R.id.ratingPosition)
        val ratingPlayerName : TextView = itemView.findViewById(R.id.ratingPlayerName)
        val playerRating:TextView = itemView.findViewById(R.id.playerRating)
        val linearLayoutBackground = itemView.findViewById<LinearLayout>(R.id.backGroundTC)
        val ratingCard :CardView = itemView.findViewById(R.id.ratingCard)


        fun bind(player:Player) {
            itemView.setOnClickListener{
                val sharP = context.getSharedPreferences("RPS", AppCompatActivity.MODE_PRIVATE)
                var sound = sharP.getBoolean("soundSt",true)
                soundX= sound
                val sharedPreferences = context.getSharedPreferences("RPS", AppCompatActivity.MODE_PRIVATE)
                val playerID = sharedPreferences.getString("playerID", "-")
                playSound(itemView.context,R.raw.click_sound)
                val playerInfoDialog = Dialog(itemView.context)
                playerInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                playerInfoDialog.setContentView(R.layout.playerprofile)
                playerInfoDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                playerInfoDialog.window?.setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                playerInfoDialog.setCancelable(true)
                val profilePic = playerInfoDialog.findViewById<ImageView>(R.id.profilePicIV)
                val playerName = playerInfoDialog.findViewById<TextView>(R.id.profileNameTV)
                val playerRating = playerInfoDialog.findViewById<TextView>(R.id.profileratingTV)
                val playerBio = playerInfoDialog.findViewById<TextView>(R.id.profileBioTV)
                val profileGamesPlayed_TV = playerInfoDialog.findViewById<TextView>(R.id.profileGamesPlayed_TV)
                val profileWinPercent_TV = playerInfoDialog.findViewById<TextView>(R.id.profileWinPercent_TV)
                val database = FirebaseDatabase.getInstance()
                val playersRef = database.getReference("Players")
                val playerRef = playersRef.child(player.id)
                val socialRating = playerInfoDialog.findViewById<TextView>(R.id.socialRatingTV)
                var socialValue :Int = player.likes.size - player.dislikes.size
                socialRating.text= socialValue.toString()

                val likebutton = playerInfoDialog.findViewById<ImageButton>(R.id.likeIB)
                val dislikebutton = playerInfoDialog.findViewById<ImageButton>(R.id.dislikeIB)

                if(player.dislikes.containsKey(playerID.toString())){
                    Glide.with(context).load(R.drawable.dislikedislike).into(dislikebutton)
                }
                else{Glide.with(context).load(R.drawable.dislike).into(dislikebutton)}

                if(player.likes.containsKey(playerID.toString())){
                    Glide.with(context).load(R.drawable.likelike).into(likebutton)
                }
                else{Glide.with(context).load(R.drawable.like).into(likebutton)}

                dislikebutton.setOnClickListener {
                    playerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val player = snapshot.getValue(Player::class.java)
                            if (player != null) {

                                if(!player.dislikes.containsKey(playerID.toString())){
                                    playerRef.child("dislikes").child(playerID.toString()).setValue(true).addOnCompleteListener{
                                        if(player.likes.containsKey(playerID)){
                                            playerRef.child("likes").child(playerID.toString()).removeValue().addOnSuccessListener {
                                                socialValue-=2
                                                playSound(context,R.raw.like)
                                                socialRating.text= socialValue.toString()
                                                Glide.with(context).load(R.drawable.dislikedislike).into(dislikebutton)
                                                Glide.with(context).load(R.drawable.like).into(likebutton)
                                            }

                                        }
                                        else{   socialValue-=1
                                            playSound(context,R.raw.like)
                                            socialRating.text= socialValue.toString()
                                            Glide.with(context).load(R.drawable.dislikedislike).into(dislikebutton)
                                            Glide.with(context).load(R.drawable.like).into(likebutton)
                                        }
                                    }
                                }
                                else{
                                    playerRef.child("dislikes").child(playerID.toString()).removeValue().addOnCompleteListener{
                                    socialValue+=1
                                        Glide.with(context).load(R.drawable.dislike).into(dislikebutton)
                                        socialRating.text= socialValue.toString()
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context,error.toString(),Toast.LENGTH_SHORT).show()
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
                                                    Glide.with(context).load(R.drawable.likelike).into(likebutton)
                                                    Glide.with(context).load(R.drawable.dislike).into(dislikebutton)
                                                }

                                            }
                                          else{
                                                playSound(context,R.raw.like)
                                              socialValue+=1
                                                socialRating.text= socialValue.toString()
                                                Glide.with(context).load(R.drawable.likelike).into(likebutton)
                                                Glide.with(context).load(R.drawable.dislike).into(dislikebutton)
                                          }
                                    }
                                }
                                else{
                                    playerRef.child("likes").child(playerID.toString()).removeValue().addOnCompleteListener {
                                        socialValue-=1
                                        Glide.with(context).load(R.drawable.like).into(likebutton)
                                        socialRating.text= socialValue.toString()
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context,error.toString(),Toast.LENGTH_SHORT).show()
                        }


                    })
                }









                profileGamesPlayed_TV.text = "Сыграно игр: "+player.gameAmount.toString()
                var playerWins = player.winAmount.toFloat()
                var playerGameAmount = player.gameAmount.toFloat()
                val number  = ((playerWins/playerGameAmount)*100)
                val formattedNumber = String.format("%.1f", number)
                profileWinPercent_TV.text = "Процент побед: "+formattedNumber+"%"

                playerName.text=player.name
                playerRating.text ="Рейтинг: "+player.rating
                playerBio.text="Bio: "+player.bio
                Glide.with(itemView.context)
                    .load(player.profileimage).
                    placeholder(R.drawable.guest)
                    .apply(RequestOptions())
                    .into(profilePic)
                playerInfoDialog.show()
            }
           ratingPlayerName.text=player.name
           playerRating.text = player.rating.toString()
            val playerIndex = playerList.indexOf(player)+1
            ratingPosition.text = playerIndex.toString()
            val sharedPreferences = context.getSharedPreferences("RPS", AppCompatActivity.MODE_PRIVATE)
            val playerID = sharedPreferences.getString("playerID", "-")
            if(player.id == playerID){
                ratingCard.setBackgroundResource(R.drawable.alt_et_shape)
                val dialogColor = ContextCompat.getColor(itemView.context, R.color.dialogColor)
                playerRating.setTextColor(dialogColor)
                ratingPlayerName.setTextColor(dialogColor)
            }
            when (playerIndex){
                1-> linearLayoutBackground.setBackgroundResource(R.color.gold)
                2-> linearLayoutBackground.setBackgroundResource(R.color.silver)
                3-> linearLayoutBackground.setBackgroundResource(R.color.bronze)
            }
        }
    }


}

var soundX:Boolean = true
fun playSound(context: Context, soundResId: Int) {
    if(soundX){
        MediaPlayer.create(context, soundResId)?.apply {
            setOnCompletionListener { release() }
            start()
        }}
}

class ratingActivity : AppCompatActivity() {
    private lateinit var ratingAdapter: ratingAdapter
    private var playerEventListener: ValueEventListener? = null
    val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)
        val playerList = mutableListOf<Player>()
        val ratingRV :RecyclerView = findViewById(R.id.ratingRV)
        val playersRef = database.getReference("Players")
          playerEventListener= playersRef.addValueEventListener(object :  ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                playerList.clear()
                for (playerSnapshot in snapshot.children){
                    val player = playerSnapshot.getValue(Player::class.java)
                     if(player!=null) {
                         if(player.gameAmount!=null && player.gameAmount >= 0){
                         playerList.add(player)
                         }
                     }

                }

                playerList.sortByDescending { player -> player.rating }
                ratingAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ratingActivity,error.toString(), Toast.LENGTH_SHORT).show()
            }
        })
        ratingAdapter = ratingAdapter(playerList,this)
        ratingRV.adapter = ratingAdapter
        ratingRV.layoutManager = LinearLayoutManager(this)




    }

    override fun onBackPressed() {
        val players = database.getReference("Players")
        super.onBackPressed()
        playerEventListener?.let {
            players.removeEventListener(it)
        }
        if (playerEventListener != null) {
            players.removeEventListener(playerEventListener!!)
        }
    }
}