package com.hellow.noteslite.ui.createditActivity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
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
import com.hellow.noteslite.utils.ConstantValues.logI
import com.hellow.noteslite.utils.CreatEditViewModelProvider
import java.util.Timer
import kotlin.concurrent.timerTask


class CreatEditActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityCreatEditBinding
    private lateinit var viewModel: CreatEditViewModel
    private lateinit var themeAdaptor: ThemeAdaptor

    private var actionMode: ActionMode? = null
    private val themeList: MutableList<ThemeItem> = mutableListOf()
    private var toolBarColor: String = "#0f0f0f"
    private var isActionTabBar = false

    init {
        for (i in 0..3) {
            themeList.add(
                ThemeItem(
                    ConstantValues.titleColor[i],
                    ConstantValues.subTitleColor[i],
                    ConstantValues.BackGroundColor[i],
                    ConstantValues.toolBarColor[i]
                )
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityCreatEditBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
         val notesRepository = NotesRepository(NotesDataBase(this)!!)
        val viewModelProviderFactory = CreatEditViewModelProvider(application, notesRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[CreatEditViewModel::class.java]


        if (intent.hasExtra("newNote")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                viewModel.setCurrentNote(
                    intent.getSerializableExtra(
                        "newNote",
                        NoteItem::class.java
                    )!!
                )
            } else {
                val note = intent.getSerializableExtra("newNote") as NoteItem?
                viewModel.setCurrentNote(note!!)
            }
            viewBinding.toolbar.title = "Create Note"

        }

        if (intent.hasExtra("oldNote")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                viewModel.setCurrentNote(
                    intent.getSerializableExtra(
                        "oldNote",
                        NoteItem::class.java
                    )!!
                )
            } else {
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
            //   setThemeToView(themeList[it])
            viewModel.setThemeValue(it)
            // for now to update the list for selectable
            themeAdaptor.notifyDataSetChanged()
        }

        viewBinding.etTitle.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                (applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                    view,
                    InputMethodManager.SHOW_FORCED
                )
                changeThemeVisibility(false)

                isActionTabBar = true
                setUpToolBar()
//                actionMode = if (actionMode != null) {
//                    actionMode!!.finish()
//                    startSupportActionMode(actionModeCallBackTitle)!!
//                } else {
//                    startSupportActionMode(actionModeCallBackTitle)!!
//                }
            } else {
                isActionTabBar = false
                setUpToolBar()
//                viewBinding.appBar.visibility = View.VISIBLE
//                if (actionMode != null) {
//                    actionMode!!.finish()
//                }
                (applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                    view.windowToken,
                    0
                )
            }

        }

        viewBinding.etTitle.addTextChangedListener {
            viewModel.setTitle(it.toString())
        }

        viewBinding.etDescription.addTextChangedListener {
            viewModel.setDesc(it.toString())
        }

        viewBinding.etDescription.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                (applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                    view,
                    InputMethodManager.SHOW_FORCED
                )
                isActionTabBar = true
                setUpToolBar()
//                viewBinding.appBar.visibility = View.GONE
                changeThemeVisibility(false)

            //
            //                actionMode =
//                    if (actionMode != null) {
//                        actionMode!!.finish()
//                        startSupportActionMode(actionModeCallBackSubTitle)!!
//                    } else {
//                        startSupportActionMode(actionModeCallBackSubTitle)!!
//                    }
            } else {
                (applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                    view.windowToken,
                    0
                )
                isActionTabBar = true

                viewBinding.appBar.visibility = View.VISIBLE
                setUpToolBar()
//                if (actionMode != null) {
//                    actionMode!!.finish()
//                }
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
            setThemeValue(it)
            themeAdaptor.currentSelected = it
        }
        viewModel.timeLiveData.observe(this) {
            viewBinding.tvTime.text = ConstantValues.dateConvert(it)
        }
    }



    private fun changeThemeVisibility(isVisible: Boolean) {
        if (isVisible) {
             viewBinding.themeCardView.animate()
                .translationY(1F)
                .setDuration(300)
                .withStartAction {
                    viewBinding.themeCardView.visibility = View.VISIBLE
                }
                .start()

        } else {
             viewBinding.themeCardView.animate()
                .translationY(viewBinding.themeCardView.height.toFloat())
                .setDuration(300)
                .withEndAction {
                   viewBinding.themeCardView.visibility = View.GONE
                }
                .start()
     }
    }

    private fun setThemeValue(value: Int) {
        when (applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                if (value == 0) {
                    setThemeToView(ConstantValues.NightModeDefaultTheme)
                } else {
                    setThemeToView(themeList[value])
                }
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                setThemeToView(themeList[value])
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                setThemeToView(themeList[value])
            }
        }
    }


    private fun setThemeToView(item: ThemeItem) {
        timber.log.Timber.i("Theme_selected")
        viewBinding.etTitle.setTextColor(Color.parseColor(item.title_color))
        viewBinding.etTitle.setHintTextColor(Color.parseColor(item.subTitle_color))
        viewBinding.etDescription.setTextColor(Color.parseColor(item.title_color))
        viewBinding.etDescription.setHintTextColor(Color.parseColor(item.subTitle_color))
        viewBinding.tvTime.setTextColor(Color.parseColor(item.subTitle_color))
        viewBinding.root.setBackgroundColor(Color.parseColor(item.backGround_color))
        viewBinding.toolbar.setBackgroundColor(Color.parseColor(item.toolBarColor))
        viewBinding.toolbar.setTitleTextColor(Color.parseColor(item.title_color))
        viewBinding.listBackgroundTheme.setBackgroundColor(Color.parseColor(item.toolBarColor))
        toolBarColor = item.toolBarColor
        setUpToolBar()
    }

    private val actionModeCallBackSubTitle: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode!!.menuInflater.inflate(R.menu.action_bar_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item!!.itemId) {
                R.id.menu_done -> {
                    viewBinding.etTitle.clearFocus()
                    mode?.finish()
                    true
                }

                android.R.id.home -> {
                    this@CreatEditActivity.finish()
                    return true
                }

                else -> false
            }

        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            viewBinding.etDescription.clearFocus()
            viewBinding.appBar.visibility = View.VISIBLE
        }

    }

    private val actionModeCallBackTitle: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode!!.menuInflater.inflate(R.menu.action_bar_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item!!.itemId) {
                R.id.menu_done -> {
                    viewBinding.etTitle.clearFocus()
                    mode?.finish()
                    true
                }

                android.R.id.home -> {
                    this@CreatEditActivity.finish()
                    return true
                }

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
        viewBinding.listBackgroundTheme.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL, false
        )
        viewBinding.listBackgroundTheme.setHasFixedSize(true)
        viewBinding.themeCardView.animate()
            .translationY(viewBinding.themeCardView.height.toFloat())
            .setDuration(3)
            .withEndAction {
                viewBinding.themeCardView.visibility = View.GONE
            }
            .start()
    }

    private fun setUpToolBar() {
        setSupportActionBar(viewBinding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        logI("option menu setteded")
        val inflater = menuInflater
        if(!isActionTabBar){
            inflater.inflate(R.menu.note_menu, menu)
        }else{
            inflater.inflate(R.menu.action_bar_menu, menu)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_change_theme -> {
                if (!viewBinding.themeCardView.isVisible) {
                    changeThemeVisibility(true)

                } else {
                    changeThemeVisibility(false)

                }
            }

            R.id.menu_item_delete ->
            {
                viewModel.deleteNote()
                finish()
            }

            R.id.menu_done ->
            {
                viewBinding.etTitle.clearFocus()
                viewBinding.etDescription.clearFocus()
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