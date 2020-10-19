package com.example.test14

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_ideacreation.*
import kotlinx.android.synthetic.main.activity_ideacreation.categoryButton
import kotlinx.android.synthetic.main.activity_ideacreation.categorySpinner
import kotlinx.android.synthetic.main.activity_ideacreation.recordPopupButton
import kotlinx.android.synthetic.main.activity_ideaupdate.*
import kotlinx.android.synthetic.main.activity_ideaupdate.editTextTextMultiLine
import kotlinx.android.synthetic.main.alertpopup.*
import kotlinx.android.synthetic.main.alertpopup.view.*
import kotlinx.android.synthetic.main.categorypopup.view.*
import kotlinx.android.synthetic.main.imagepopup.view.*
import kotlinx.android.synthetic.main.recordpopup.view.*
import java.io.*
import java.io.File.separator
import java.text.SimpleDateFormat
import java.util.*

class IdeaUpdateActivity : AppCompatActivity(){

    var removed = false;
    var recording_deleted = false;

    val REQUEST_CODE = 100
    //temporary storage when capturing images with the camera
    var local_uri: Uri? = null;
    var imagePath: String? = null;

    //Sound recording
    private var mediaRecorder: MediaRecorder? = null
    private var lastRecordedPath: String? = null

    private var mediaPlayer: MediaPlayer? = null;

    lateinit var ideaViewModel : IdeaViewModel;

    private lateinit var currentIdea: IdeaObject;

    var recordinChanged = false;
    var imageChanged = false;


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ideaupdate)


        try{
            ideaViewModel = ViewModelProvider(this).get(IdeaViewModel::class.java)
        }
        catch (e: Exception)
        {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
        ideaViewModel.categories.observe(this, Observer { spinnerData ->
            var spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerData)
            categorySpinner.adapter = spinnerAdapter
            var i = -1;
            for(index in 0 until spinnerAdapter.count)
            {
                if(spinnerAdapter.getItem(index).toString() == currentIdea.category)
                {
                    i = index;
                }
            }
            if(i != -1)
            {
                categorySpinner.setSelection(i)
            }
        })

        var id = intent.getIntExtra("id", -1)

        //If it is defaultValue, something has gone wrong
        if (id == -1) {
            Toast.makeText(this, "An error has occurred opening the idea", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED, Intent())
            finish()
        }
        currentIdea = ideaViewModel.getById(id);
        if(currentIdea.img != null)
        {
            imagePopupButton.setImageURI(Uri.parse(currentIdea.img))
        }
        else
        {
            imagePopupButton.setImageURI(null)
        }
        lastRecordedPath = currentIdea.rcd;

        editTextTextMultiLine.setText(currentIdea.text)
        imagePopupButton.setOnClickListener {
            //inflates the popup to select from camera or gallery
            val dialogView = LayoutInflater.from(this).inflate(R.layout.imagepopup, null)
            //Build the dialog
            val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
            //View the dialog
            val alertDialog = dialogBuilder.show()
            dialogView.gallerybutton.setOnClickListener {
                openGalleryForImage()
                alertDialog.dismiss()
            }
            dialogView.camerabutton.setOnClickListener {
                openCameraForImage()
                alertDialog.dismiss()
            }
            dialogView.clearButton.setOnClickListener {
                local_uri = null;
                imagePopupButton.setImageURI(local_uri)
                removed = true;
                alertDialog.dismiss()
            }
        }

        categoryButton.setOnClickListener{
            val dialogView = LayoutInflater.from(this).inflate(R.layout.categorypopup, null)
            //Build the dialog
            val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
            //View the dialog
            val alertDialog = dialogBuilder.show()

            dialogView.canclebutton.setOnClickListener{
                alertDialog.dismiss()
            }
            dialogView.addButton.setOnClickListener {
                try{
                    ideaViewModel.insert(CategoryObject(dialogView.categoryName.text.toString()))
                } catch(e : Exception)
                {
                    Toast.makeText(this, "Error accured, Please report issue: " + e.toString(), Toast.LENGTH_SHORT).show()
                }
                alertDialog.dismiss();
            }
        }

        recordPopupButton.setOnClickListener{

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, permissions,0)
            } else {

                mediaPlayer = MediaPlayer();

                //inflates the popup to select from camera or gallery
                val dialogView = LayoutInflater.from(this).inflate(R.layout.recordpopup, null)
                //Build the dialog
                val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
                //View the dialog
                val alertDialog = dialogBuilder.show()

                dialogView.recordbutton.setOnTouchListener { v, event ->
                    when(event.action){
                        MotionEvent.ACTION_DOWN -> {
                            dialogView.recordbutton.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
                            startRecording()
                        }
                        MotionEvent.ACTION_UP -> {
                            dialogView.recordbutton.clearColorFilter();
                            stopRecording();
                        }
                    }
                    return@setOnTouchListener true
                }
                dialogView.playbutton.setOnClickListener {
                    playAudio()
                }
            }
        }

        updateButton.setOnClickListener{

            if ((lastRecordedPath != null && lastRecordedPath != currentIdea.rcd) || recording_deleted)
            {
                deleteTempFile(currentIdea.rcd);
                currentIdea.rcd = lastRecordedPath
            }
            if(local_uri != null) {
                var imagePath: File? = null
                val parcelFileDescriptor = contentResolver.openFileDescriptor(local_uri!!, "r", null)
                parcelFileDescriptor?.let {
                    val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                    imagePath = File(getExternalFilesDir(null)?.absolutePath + separator.toString(), contentResolver.getFileName(local_uri!!))
                    val outputStream = FileOutputStream(imagePath)
                    copyStreams(inputStream, outputStream)
                }
                deleteTempFile(currentIdea.img)
                currentIdea.img = imagePath.toString()
            }
            else if(removed)
            {
                deleteTempFile(currentIdea.img)
                currentIdea.img = null
            }
            currentIdea.text = editTextTextMultiLine.text.toString()
            currentIdea.category = categorySpinner.selectedItem.toString()
            ideaViewModel.updateIdea(currentIdea);
            setResult(Activity.RESULT_OK, Intent())
            finish()
        }
        deleteButton.setOnClickListener{

            val dialogView = LayoutInflater.from(this).inflate(R.layout.alertpopup, null)
            //Build the dialog
            val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
            //View the dialog
            val alertDialog = dialogBuilder.show()

            dialogView.confirmButton.setOnClickListener{
                deleteTempFile(currentIdea.img)
                deleteTempFile(currentIdea.rcd)

                var returnIntent = Intent();
                returnIntent.putExtra("category", currentIdea.category)
                ideaViewModel.deleteItem(currentIdea.id)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
            dialogView.cancleButton.setOnClickListener{
                alertDialog.dismiss()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1){
            //Image selected from gallery
            imagePopupButton.setImageURI(data?.data) // handle chosen image
            local_uri = data?.data;
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == 2){
            imagePopupButton.setImageURI(local_uri)
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }
    private fun openCameraForImage() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        local_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, local_uri)
        startActivityForResult(cameraIntent, 2)
    }

    private fun startRecording() {

        //The first thing to do is delete the old temp_file

        if (lastRecordedPath != currentIdea.rcd)
        {
            deleteTempFile(lastRecordedPath)
        }
        lastRecordedPath = null;


        mediaRecorder = MediaRecorder()
        var outPutFile = createFilePath("mp3")
        mediaRecorder?.setOutputFile(outPutFile)
        lastRecordedPath = outPutFile;
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    private fun stopRecording() {
        try{
            mediaRecorder?.stop()
            mediaRecorder?.release()
        }catch(e: java.lang.Exception)
        {
            mediaRecorder?.release()
            deleteTempFile(lastRecordedPath)
            lastRecordedPath = null
            Toast.makeText(this, "Recording cleared", Toast.LENGTH_SHORT).show()
            recording_deleted = true;
        }
    }
    private fun playAudio() {
        if (lastRecordedPath != null) {
            try{
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(lastRecordedPath)
                mediaPlayer?.prepare()
                mediaPlayer?.start();
            } catch (e: IOException)
            {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        else Toast.makeText(this, "No Audio Recorded", Toast.LENGTH_SHORT).show()
    }
    private fun deleteTempFile(dir : String?)
    {
        if (dir != null)
        {
            val file = File(dir)
            file.delete()
        }
    }

    @Throws(IOException::class)
    private fun copyFile(sourceFolder: String?, destFolder: String?, fileName: String?) {
        var inFile: InputStream? = null
        var outFile: OutputStream? = null
        try {

            //create output directory if it doesn't exist
            val dir = File(destFolder)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            inFile = FileInputStream(sourceFolder + fileName)
            outFile = FileOutputStream(destFolder + fileName)
            val buffer = ByteArray(1024)
            var read: Int
            read = inFile.read(buffer);
            while (read != -1) {
                outFile.write(buffer, 0, read)
                    read = inFile.read(buffer);
            }
            inFile.close()
            inFile = null

            // write the output file
            outFile.flush()
            outFile.close()
            outFile = null

        } catch (e: FileNotFoundException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveFile(sourceFolder: String?, destFolder: String?, fileName: String?)
    {
        copyFile(sourceFolder, destFolder, fileName)
        File(sourceFolder).delete()
    }

    fun copyStreams(inFile: InputStream, outFile: OutputStream)
    {
        try{
            val buffer = ByteArray(1024)
            var read: Int
            read = inFile.read(buffer);
            while (read != -1) {
                outFile.write(buffer, 0, read)
                read = inFile.read(buffer);
            }
            inFile.close()

            // write the output file
            outFile.flush()
            outFile.close()
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "Fel i streamen: " + e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun createFilePath(fileType: String) : String
    {
        var folder = getExternalFilesDir(null)?.absolutePath + separator.toString();
        var sufflix = SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + "." + fileType
        return folder+sufflix;
    }


    private fun getRealPathFromURI(contentURI: Uri): String? {
        val result: String?
        val cursor: Cursor? = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }
}