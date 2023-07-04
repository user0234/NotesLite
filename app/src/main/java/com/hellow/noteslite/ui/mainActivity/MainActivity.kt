package com.hellow.noteslite.ui.mainActivity

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellow.noteslite.adaptor.NotesAdaptor
import com.hellow.noteslite.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private lateinit var viewBinding:ActivityMainBinding
    private lateinit var notesAdaptor:NotesAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setUpFullScreenUI()
        setUpToolBar()
        setUpRecyclerView()



    }
    private fun setUpRecyclerView() {
        notesAdaptor = NotesAdaptor()
        viewBinding.rvNotesList.adapter = notesAdaptor
        viewBinding.rvNotesList.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,false)
        viewBinding.rvNotesList.setHasFixedSize(true)

    }
    private fun setUpToolBar() {
        setSupportActionBar(viewBinding.toolbar)
        viewBinding.toolbar.title = "Notes"
      }
    private fun setUpFullScreenUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

}