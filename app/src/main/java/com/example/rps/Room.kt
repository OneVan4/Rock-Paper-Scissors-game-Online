package com.example.rps




data class Room(
    val name: String = "",
    val privatable:Boolean = false ,
    val password : String = "",
    var players: HashMap<String, Player> ,
    var playerAmount: Int = 2,
    val gameStats: GameStats = GameStats(),
    var chat : MutableList<String>,
    var roomID :String = "",
    var admin :String ="",
    var exists :Boolean,
    var ready :Boolean

){
    constructor() : this("", false, "",  hashMapOf(),0 , GameStats(), mutableListOf(), "","",false,false)
}

data class GameStats(
    val roundAmount: Int = 1,
    val roundTime: Int = 3,
    val ult :Boolean =false
)

 data class Player (
    var name : String =" ",
    var age:String = "",
    var hasRoom:Boolean = false ,
    var roomID :String = "",
    var emoji:Int = -1,
    var rating :Int = 0,
    var profileimage: String? = null,
    var bio:String ="",
    var gameAmount:Int =0,
    var winAmount: Int = 0,
    var id:String ="",
    var likes: HashMap<String,Boolean> = hashMapOf(),
    var dislikes:HashMap<String,Boolean> = hashMapOf()
        )



data class Game(
    val room : Room = Room(),
    val playerChoices: HashMap<String,Int>,
    var connections : HashMap<String,Boolean>,
    var over : Boolean,
    var winner : String
){
    constructor() : this( Room(), hashMapOf(), hashMapOf(),false,"me")
}