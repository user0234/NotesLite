package com.hellow.noteslite.ui.createditActivity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Note
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellow.noteslite.R
import com.hellow.noteslite.adaptor.ThemeAdaptor
import com.hellow.noteslite.database.NotesDataBase
import com.hellow.noteslite.databinding.ActivityCreatEditBinding
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.model.ThemeItem
import com.hellow.noteslite.repository.NotesRepository
import com.hellow.noteslite.utils.ConstantValues
import com.hellow.noteslite.utils.CreatEditViewModelProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CreatEditActivity : AppCompatActivity() {

        private lateinit var viewBinding: ActivityCreatEditBinding
        private lateinit var viewModel: CreatEditViewModel
        private lateinit var themeAdaptor: ThemeAdaptor

        private var actionMode: ActionMode? = null
        private lateinit var noteId:String
        private val themeList:MutableList<ThemeItem>  =  mutableListOf()

         init {
                 for(i in 0..5 ){
                 themeList.add(ThemeItem(ConstantValues.titleColor[i],ConstantValues.subTitleColor[i],ConstantValues.BackGroundColor[i]))
                 }
          }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCreatEditBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val notesRepository = NotesRepository(NotesDataBase(this)!!)
        val viewModelProviderFactory = CreatEditViewModelProvider(application,notesRepository)
        viewModel = ViewModelProvider(this,viewModelProviderFactory)[CreatEditViewModel::class.java]


        if(intent.hasExtra("newNote")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                viewModel.setCurrentNote(intent.getSerializableExtra("newNote",NoteItem::class.java)!!)
            } else{
                val note = intent.getSerializableExtra("newNote") as NoteItem?
                viewModel.setCurrentNote(note!!)
            }
            viewBinding.toolbar.title = "Create Note"

        }

        if(intent.hasExtra("oldNote")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                viewModel.setCurrentNote(intent.getSerializableExtra("oldNote",NoteItem::class.java)!!)
            } else{
                val note = intent.getSerializableExtra("oldNote") as NoteItem?
                viewModel.setCurrentNote(note!!)
            }
            viewBinding.toolbar.title = "Edit Note"
        }

        setUpToolBar()
        setUpThemeListView()
        themeAdaptor.differ.submitList(themeList)
        themeAdaptor.setOnItemClickListener {
            // make the background as selected theme
            setTheme(themeList[it])
            viewModel.setThemeValue(it)
            // for now to update the list for selectable
            themeAdaptor.notifyDataSetChanged()
        }

        viewBinding.etTitle.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus){
                viewBinding.appBar.visibility = View.GONE
                viewBinding.listBackgroundTheme.visibility = View.GONE
                actionMode = if(actionMode != null){
                    actionMode!!.finish()
                    startSupportActionMode(actionModeCallBackTitle)!!
                }else{
                    startSupportActionMode(actionModeCallBackTitle)!!
                }
            }else{
                setUpToolBar()
                viewBinding.appBar.visibility = View.VISIBLE
                 if(actionMode != null){
                    actionMode!!.finish()
                }
            }

        }

        viewBinding.etTitle.addTextChangedListener {
               viewModel.setTitle(it.toString())
        }

        viewBinding.etDescription.addTextChangedListener{
            viewModel.setDesc(it.toString())
        }

        viewBinding.etDescription.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus){
                viewBinding.appBar.visibility = View.GONE
                viewBinding.listBackgroundTheme.visibility = View.GONE
                actionMode = if(actionMode != null) {
                    actionMode!!.finish()
                    startSupportActionMode(actionModeCallBackSubTitle)!!
                }else{
                    startSupportActionMode(actionModeCallBackSubTitle)!!
                }
            }else{
                viewBinding.appBar.visibility = View.VISIBLE
                setUpToolBar()
                if(actionMode != null) {
                    actionMode!!.finish()
                }
            }
        }

        viewModel.titleLiveData.observe(this) {
            viewBinding.etTitle.setText(it.toString())
            viewBinding.etTitle.setSelection(viewBinding.etTitle.length())

        }
        viewModel.descLiveData.observe(this) {
            viewBinding.etDescription.setText(it.toString())
            viewBinding.etDescription.setSelection(viewBinding.etDescription.length())
        }
        viewModel.themeLiveData.observe(this) {
            setTheme(themeList[it])
        }
        viewModel.timeLiveData.observe(this) {
               viewBinding.tvTime.text = ConstantValues.dateConvert(it)
        }
    }

    private fun setTheme(item: ThemeItem){
        timber.log.Timber.i("Theme_selected")
        viewBinding.etTitle.setTextColor(Color.parseColor(item.title_color))
        viewBinding.etTitle.setHintTextColor(Color.parseColor(item.subTitle_color))
        viewBinding.etDescription.setTextColor(Color.parseColor(item.title_color))
        viewBinding.etDescription.setHintTextColor(Color.parseColor(item.subTitle_color))
        viewBinding.tvTime.setTextColor(Color.parseColor(item.subTitle_color))
        viewBinding.root.setBackgroundColor(Color.parseColor(item.backGround_color))
        viewBinding.toolbar.setBackgroundColor(Color.parseColor(item.backGround_color))
        viewBinding.toolbar.setTitleTextColor(Color.parseColor(item.title_color))

        setUpToolBar()
    }

    private val actionModeCallBackSubTitle : ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode!!.menuInflater.inflate(R.menu.action_bar_menu,menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item!!.itemId) {
                R.id.menu_done -> { viewBinding.etTitle.clearFocus()
                    mode?.finish()
                    true
                }
                android.R.id.home -> {
                    this@CreatEditActivity.finish()
                    return true }

                else -> false
            }

        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            viewBinding.etDescription.clearFocus()
            viewBinding.appBar.visibility = View.VISIBLE
        }

    }

    private val actionModeCallBackTitle : ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode!!.menuInflater.inflate(R.menu.action_bar_menu,menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item!!.itemId) {
                R.id.menu_done -> { viewBinding.etTitle.clearFocus()
                    mode?.finish()
                    true
                }
                 android.R.id.home -> {
                     this@CreatEditActivity.finish()
                return true }

                else -> false
            }

        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            viewBinding.etTitle.clearFocus()
            viewBinding.appBar.visibility = View.VISIBLE
        }

    }

    private fun setUpThemeListView() {
        themeAdaptor = ThemeAdaptor()
        viewBinding.listBackgroundTheme.adapter = themeAdaptor
        viewBinding.listBackgroundTheme.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,false)
        viewBinding.listBackgroundTheme.setHasFixedSize(true)
    }

    private fun setUpToolBar() {
        setSupportActionBar(viewBinding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.note_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem):Boolean {
        when(item.itemId) {
            R.id.menu_change_theme -> {
                if(!viewBinding.listBackgroundTheme.isVisible) {
                    viewBinding.listBackgroundTheme.visibility = View.VISIBLE
                }else{
                    viewBinding.listBackgroundTheme.visibility = View.GONE
                }
            }
            R.id.menu_item_delete -> {
                viewModel.deleteNote()
                finish()
            }

            android.R.id.home -> {
                finish()
                }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        viewModel.updateNote()
        super.onStop()
    }

}