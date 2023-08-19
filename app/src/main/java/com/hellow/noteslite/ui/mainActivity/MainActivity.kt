package com.hellow.noteslite.ui.mainActivity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Explode
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hellow.noteslite.adaptor.NotesAdaptor
import com.hellow.noteslite.database.NotesDataBase
import com.hellow.noteslite.databinding.ActivityMainBinding
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.repository.NotesRepository
import com.hellow.noteslite.ui.createditActivity.CreatEditActivity
import com.hellow.noteslite.utils.MainViewModelProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {


    private lateinit var viewBinding:ActivityMainBinding
    private lateinit var notesAdaptor:NotesAdaptor
    private lateinit var viewModel:MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val notesRepository = NotesRepository(NotesDataBase(this)!!)

        val viewModelProviderFactory = MainViewModelProvider(application,notesRepository)

        viewModel = ViewModelProvider(this,viewModelProviderFactory)[MainActivityViewModel::class.java]

        setUpFullScreenUI()
        setUpToolBar()
        setUpRecyclerViewList()
        val explode = Explode()
        explode.duration = 300;
        window.exitTransition = explode;
        window.enterTransition = explode;

        viewBinding.fabCreate.setOnClickListener {
            val intent = Intent(this,CreatEditActivity::class.java)
            val noteId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy HH:mm:ss a")).toString()
            val note = NoteItem(noteId)
            viewModel.addNote(note)
            intent.putExtra("noteItem",note)
             startActivity(intent)
        }

            viewModel.getNotesList().observe(this) {
                if (it == null) {
                    viewBinding.rvNotesList.visibility = View.GONE
                    viewBinding.emptyListView.visibility = View.VISIBLE
                } else {
                    if(it.isEmpty()){
                        viewBinding.rvNotesList.visibility = View.GONE
                        viewBinding.emptyListView.visibility = View.VISIBLE
                    } else {
                        notesAdaptor.differ.submitList(it)
                        viewBinding.rvNotesList.visibility = View.VISIBLE
                        viewBinding.emptyListView.visibility = View.GONE
                    }
                }
            }
    }


    private fun setUpItemTouchHelper() {
        val itemTouchHelperCallBack  = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN , ItemTouchHelper.LEFT or
                    ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = notesAdaptor.differ.currentList[position]
                viewModel.deleteNote(note)
                Snackbar.make(viewBinding.root,"deleted forever", Snackbar.LENGTH_LONG).apply {
                    setAction("StopDelete"){
                        viewModel.addNote(note)
                    }
                    show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallBack).apply {
            attachToRecyclerView(viewBinding.rvNotesList)
        }
    }

    private fun setUpRecyclerViewList() {
        notesAdaptor = NotesAdaptor()
        viewBinding.rvNotesList.adapter = notesAdaptor
        viewBinding.rvNotesList.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,false)
        viewBinding.rvNotesList.setHasFixedSize(true)
        setUpItemTouchHelper()
          notesAdaptor.setOnItemClickListener { noteItem, view, _ ->
              val intent = Intent(this,CreatEditActivity::class.java)
              intent.putExtra("noteItem",noteItem)
              val activityOptionsCompat =
                  ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "transition_card")
              startActivity(intent,activityOptionsCompat.toBundle())
          }
    }
    private fun setUpToolBar() {
        setSupportActionBar(viewBinding.toolbar)

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