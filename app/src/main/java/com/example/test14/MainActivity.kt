package com.example.test14

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alertpopup.view.*
import kotlinx.android.synthetic.main.deleteategorypopup.view.*
import kotlinx.android.synthetic.main.helppopup.view.*
import java.io.IOException


class MainActivity : AppCompatActivity() {
    lateinit var ideaViewModel: IdeaViewModel
    var filter = false
    lateinit var adapter: IdeaAdapter
    private var mediaPlayer: MediaPlayer? = null

    private val listPopupView by lazy { ListPopupWindow(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaPlayer = MediaPlayer()

        ideaViewModel = ViewModelProvider(this).get(IdeaViewModel::class.java)

        initialSetup()

        createIdeaButton.setOnClickListener {
            val intent = Intent(this, IdeaCreationActivity::class.java);
            startActivityForResult(intent, 1)
        }

        adapter = IdeaAdapter(this)
        showIdeas.adapter = adapter
        showIdeas.layoutManager = LinearLayoutManager(this)

        ideaViewModel.ideas.observe(this, Observer { words ->
            words?.let { adapter.setWords(it) }
        })


        ideaViewModel.categories.observe(this, Observer { spinnerData ->
            listPopupView.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, spinnerData))
            selectionButton.text = "Random"
            listPopupView.setOnItemClickListener { _, _, position, _ ->
                selectionButton.text = listPopupView.listView?.getItemAtPosition(position).toString()
                listPopupView.dismiss()
                if(filter)
                {
                    ideaViewModel.filter(selectionButton.text as String)
                    adapter.notifyDataSetChanged()
                    //To update the view. Otherwise it will not update until you click on it
                    showIdeas.smoothScrollToPosition(0)
                }
            }
            listPopupView.anchorView = selectionButton
            selectionButton.setOnClickListener { listPopupView.show() }
            selectionButton.setOnLongClickListener {
                if(selectionButton.text != "Random")
                {
                    val dialogView = LayoutInflater.from(this).inflate(R.layout.deleteategorypopup, null)
                    //Build the dialog
                    val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
                    //View the dialog
                    dialogView.categoryLabel.text = dialogView.categoryLabel.text as String + selectionButton.text as String + "?";
                    val alertDialog = dialogBuilder.show()

                    dialogView.cancleButton2.setOnClickListener{
                        alertDialog.dismiss();
                    }
                    dialogView.deleteButton2.setOnClickListener {
                        ideaViewModel.deleteCategory(selectionButton.text as String);
                        ideaViewModel.getAllIdeas();
                        selectionButton.text = spinnerData[0].toString()
                        alertDialog.dismiss();
                    }
                    updateRecycleView()
                }
                false
            }
        })

        filterSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
            {
                ideaViewModel.filter(selectionButton.text.toString())
                filter = true
            }
            else
            {
                ideaViewModel.getAllIdeas()
                filter = false
            }
            adapter.notifyDataSetChanged()
            showIdeas.smoothScrollToPosition(0);
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            updateRecycleView()
        }
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            //If it isn't in the category currently displayed, we don't need to update it
            updateRecycleView()
        }
    }
    fun updateActivity(id:Int)
    {
        val intent = Intent(this, IdeaUpdateActivity::class.java);
        intent.putExtra("id", id)
        startActivityForResult(intent, 2);
    }
    fun updateIdea(ideaObject: IdeaObject)
    {
        ideaViewModel.updateIdea(ideaObject)
    }
    fun playAudio(path:String?) {
        try{
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(path)
            mediaPlayer?.prepare()
            mediaPlayer?.start();
        } catch (e: IOException)
        {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    fun updateRecycleView()
    {
        if(filter)
        {
            ideaViewModel.filter(selectionButton.text as String)
            adapter.notifyDataSetChanged()
        }
        else
        {
            ideaViewModel.getAllIdeas()
            adapter.notifyDataSetChanged()
        }
        showIdeas.smoothScrollToPosition(0);
    }

    fun initialSetup()
    {
        val PREFS_NAME = "MyPrefsFile"

        val settings = getSharedPreferences(PREFS_NAME, 0)

        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            val dialogView = LayoutInflater.from(this).inflate(R.layout.helppopup, null)
            //Build the dialog
            val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
            //View the dialog
            val alertDialog = dialogBuilder.show()

            dialogView.close_button.setOnClickListener{
                alertDialog.dismiss()
            }
            ideaViewModel.insert(CategoryObject("Random"))
            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).apply()
        }
    }
}