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
import androidx.appcompat.app.ActionBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream


class register : AppCompatActivity() {

    var wasClicked :Boolean = false
    private lateinit var imageButton :ImageView
    lateinit var bitmap :Bitmap
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var storageRef : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        bitmap = BitmapFactory.decodeResource(resources, R.drawable.guest)
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("Players")

        val ageET = findViewById<EditText>(R.id.ageET)
        val nameET =findViewById<EditText>(R.id.nameRigisterET)
        nameET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Check if the text contains a line break
                if (s != null && s.contains("\n")) {
                    // Remove the line break from the text
                    val modifiedText = s.toString().replace("\n", "")
                    nameET.setText(modifiedText)
                    // Move the cursor to the end of the text
                    nameET.setSelection(modifiedText.length)
                }
            }
        })
        val regBut = findViewById<ImageButton>(R.id.registerButton)

        imageButton = findViewById(R.id.imageButton)
        imageButton.setOnClickListener(){
            openGallery()
        }
        Glide.with(this)
            .load(R.drawable.guest)
            .apply(RequestOptions().fitCenter())
            .into(imageButton)

        val storage = FirebaseStorage.getInstance()
        storageRef= storage.reference

        regBut.setOnClickListener(){
             if(ageET.text.toString().isNullOrEmpty() || nameET.text.toString().isNullOrEmpty()){
                  Toast.makeText(this,"Поля не могут быть пустыми !",Toast.LENGTH_SHORT).show()

             }
            else {

                 val playerName = nameET.text.toString()
                 val playerID= ref.push().key
                 val playerAge = ageET.text.toString()
                 val filename = playerID + ".jpg"

                 val fileRef = storageRef.child(filename)

                 // Конвертируйте Bitmap в массив байтов
                 val baos = ByteArrayOutputStream()
                 bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                 val data = baos.toByteArray()

                 if(wasClicked==false){

                     val uploadTask = fileRef.putBytes(data)
                     Toast.makeText(this,"Немного подождите",Toast.LENGTH_SHORT).show()

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
                             val bioET = findViewById<EditText>(R.id.bioET2)
                             var bio :String = "Мне нечего сказать:("
                             if(!bioET.text.isNullOrEmpty()){
                                 bio = bioET.text.toString()
                             }
                             val player = Player(playerName,playerAge,false,"",-1,1000,imageUrl,bio,0,0,playerID.toString(),
                                 hashMapOf(),
                                 hashMapOf())
                 ref.child(playerID.toString()).setValue(player).addOnCompleteListener { task ->
                     if (task.isSuccessful) {
                         val shared = getSharedPreferences("RPS", MODE_PRIVATE)
                         val editor = shared.edit()
                         editor.putString("playerName",playerName)
                         editor.putString("playerAge",playerAge)
                         editor.putString("playerID",playerID.toString())
                         editor.apply()
                         dialog.dismiss()
                         Toast.makeText(this,"Регистрация прошла успешно!",Toast.LENGTH_LONG).show()
                        val intent = Intent(this,MainActivity::class.java)
                         startActivity(intent)
                         finish()
                     } else {
                         Toast.makeText(this, "Проверьте подключение к интернету.", Toast.LENGTH_SHORT).show()
                     }
                 }
                     wasClicked = true
                 }}.addOnFailureListener() { // Ошибка загрузки
                     Toast.makeText(
                         this@register,
                         "Ошибка загрузки изображения: ${it.message}",
                         Toast.LENGTH_SHORT
                     ).show()
                 }
                 }
                 else{Toast.makeText(this,"Запрос отправлен,ожидается ответ  сервера!",Toast.LENGTH_SHORT).show()}
            }

        }

    }

private fun openGallery (){
    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    startActivityForResult(intent,PICK_IMAGE_REQUEST)
}


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri = data.data ?: return

            val inputStream = contentResolver.openInputStream(imageUri)
            val fileSize = inputStream?.available() ?: 0
            val maxFileSizeBytes = 2 * (1024 * 1024) // 2 MB

            if (fileSize <= maxFileSizeBytes) {
                Glide.with(this)
                    .load(imageUri)
                    .apply(RequestOptions())
                    .into(imageButton)
                bitmap = BitmapFactory.decodeStream(inputStream)
            } else {
                Toast.makeText(
                    this,
                    "Размер изображения слишком велик",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}