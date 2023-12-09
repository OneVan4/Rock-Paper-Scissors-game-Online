package com.example.rps

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.view.WindowManager
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream


class profile : AppCompatActivity() {
    private lateinit var imageButton: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var storageRef : StorageReference
    private lateinit var bitmap: Bitmap
    var wasPressed = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
        val playerID = sharP.getString("playerID", "-")
        val database = FirebaseDatabase.getInstance()
        val playersRef = database.getReference("Players")
        val playerRef = playersRef.child(playerID.toString())
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.guest) // Загрузка изображения по умолчанию
        val storage = FirebaseStorage.getInstance()
        storageRef= storage.reference
        val editor = sharP.edit()
        val playerNameTV =findViewById<TextView>(R.id.nameTV)
        val save = findViewById<ImageButton>(R.id.saveButton)
        val editText = findViewById<EditText>(R.id.changeNameET)
        val bioET = findViewById<EditText>(R.id.bioET)
        val aboutMeTV = findViewById<TextView>(R.id.aboutMeTV)
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Check if the text contains a line break
                if (s != null && s.contains("\n")) {
                    // Remove the line break from the text
                    val modifiedText = s.toString().replace("\n", "")
                    editText.setText(modifiedText)
                    // Move the cursor to the end of the text
                    editText.setSelection(modifiedText.length)
                }
            }
        })

        var imageUri = ""
        imageButton = findViewById(R.id.profileImagePV)
        imageButton.setOnClickListener {
            openGallery()
        }
        Glide.with(this)
            .load(R.drawable.guest)
            .into(imageButton)
        playerRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val player = snapshot.getValue(Player::class.java)
                if(player!=null){
                    imageUri = player.profileimage.toString()
                    playerNameTV.text = player.name
                    aboutMeTV.text = aboutMeTV.text.toString() + player.bio
                    val options = RequestOptions()
                        .placeholder(R.drawable.guest) // Заглушка, отображаемая до загрузки изображения
                        .error(R.drawable.stone) // Изображение ошибки, отображаемое при возникновении ошибки загрузки

                    Glide.with(this@profile)
                        .load(imageUri)
                        .apply(options)
                        .into(imageButton)

                }

                else {
                    Toast.makeText(this@profile, "Ошибка загрузки изображения:Игрок с заданным id не найден",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@profile,error.toString(),Toast.LENGTH_SHORT).show()
            }


        })


        save.setOnClickListener() {
            if (!editText.text.toString().isNullOrEmpty() || !bioET.text.isNullOrEmpty()) {
                if(wasPressed){
                    Toast.makeText(this,"Немного подождите,или проверьте интернет-соединение",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                else{
                wasPressed = true
                Toast.makeText(this,"Немного подождите",Toast.LENGTH_SHORT).show()
                        playerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val player = snapshot.getValue(Player::class.java)
                                if (player != null) {
                                    if(!editText.text.toString().isNullOrEmpty()){
                                    player.name = editText.text.toString()
                                        editor.putString("playerName", editText.text.toString())
                                            .apply()
                                    }
                                    if(!bioET.text.isNullOrEmpty()){
                                        player.bio = bioET.text.toString()

                                    }
                                    playerRef.setValue(player).addOnSuccessListener {
                                        Toast.makeText(
                                            this@profile,
                                            "Ваши данные были успешно изменены",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent = Intent(this@profile, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            this@profile,
                                            "Не удалось изменить данные,проверьте соединение с интернетом ",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        this@profile,
                                        "Игрок с таким ID не найден",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Toast.makeText(
                                    this@profile,
                                    "Ошибка при получении данных игрока: ${databaseError.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })

            }}
            else {
                Toast.makeText(this@profile,"Изменений не найдено! ",Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri = data.data ?: return

            val inputStream = contentResolver.openInputStream(imageUri)
            val fileSize = inputStream?.available() ?: 0
            val maxFileSizeBytes = 2 * (1024 * 1024) // 2 MB

            if (fileSize <= maxFileSizeBytes) {
                val sharP = getSharedPreferences("RPS", MODE_PRIVATE)
                val playerID = sharP.getString("playerID", "-")
                val database = FirebaseDatabase.getInstance()
                val playersRef = database.getReference("Players")
                val playerRef = playersRef.child(playerID.toString())
                bitmap = BitmapFactory.decodeStream(inputStream)


                val filename = playerID + ".jpg"

                val fileRef = storageRef.child(filename)

                // Конвертируйте Bitmap в массив байтов
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val uploadTask = fileRef.putBytes(data)


                val dialog = Dialog(this)
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
                Glide.with(this).load(R.drawable.wait).into(waitingIV)
                waitFor.text ="Дождитесь окончания загрузки изображения профиля"
                dialog.show()

                uploadTask.addOnSuccessListener { taskSnapshot ->
                    // Получите ссылку на загруженное изображение
                    val imageDownloadUrlTask = taskSnapshot.storage.downloadUrl
                    imageDownloadUrlTask.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        // Сохраните ссылку на изображение в объекте Player

                        playerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val player = snapshot.getValue(Player::class.java)
                                if (player != null) {
                                    player.profileimage = imageUrl
                                    playerRef.setValue(player).addOnSuccessListener {
                                        Toast.makeText(
                                            this@profile,
                                            "Ваши данные были успешно изменены",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        Glide.with(this@profile)
                                            .load(imageUri)
                                            .apply(RequestOptions())
                                            .into(imageButton)
                                       dialog.dismiss()
                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            this@profile,
                                            "Не удалось изменить данные,проверьте соединение с интернетом ",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        this@profile,
                                        "Игрок с таким ID не найден",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Toast.makeText(
                                    this@profile,
                                    "Ошибка при получении данных игрока: ${databaseError.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }.addOnFailureListener {
                        Toast.makeText(
                            this@profile,
                            "Ошибка получения ссылки на изображение: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener {
                    // Ошибка загрузки
                    Toast.makeText(
                        this@profile,
                        "Ошибка загрузки изображения: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else {
                Toast.makeText(
                    this,
                    "Размер изображения слишком велик!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            }
        }
    }





