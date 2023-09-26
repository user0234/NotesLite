package com.hellow.noteslite.ui.createditActivity

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellow.noteslite.R
import com.hellow.noteslite.adaptor.ThemeAdaptor
import com.hellow.noteslite.adaptor.descAdaptor.EditAdaptor
import com.hellow.noteslite.database.NotesDataBase
import com.hellow.noteslite.databinding.ActivityCreatEditBinding
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.repository.NotesRepository
import com.hellow.noteslite.utils.ConstantValues
import com.hellow.noteslite.utils.CreatEditViewModelProvider
import com.hellow.noteslite.utils.hideKeyboard
import com.hellow.noteslite.utils.observeEvent


class CreatEditActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityCreatEditBinding
    private lateinit var viewModel: CreatEditViewModel
    private lateinit var adaptor: EditAdaptor
    private lateinit var noteItemReceived: NoteItem
    private var isFocused = false
    private lateinit var themeAdapter: ThemeAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set up view Binding
        viewBinding = ActivityCreatEditBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // get the note item passed
        if (intent.hasExtra("noteItem")) {
            noteItemReceived = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(
                    "noteItem",
                    NoteItem::class.java
                )!!

            } else {
                intent.getParcelableExtra("noteItem")!!

            }


        } else {
            // kill the activity if note item not found
            finish()
        }
        // setUpCurrentItemsInView

        setUpViewModel()
        setUpToolBar()
        setUpViewData()
        setUpAdaptor()
        setUpLiveData()
        setUpTitleData()
        setUpThemeDataAndAdaptor()
        //  setUpChangeItemType()
        setUpDescButton()
        viewBinding.softInputAboutView.visibility = View.GONE
        changeThemeInView(noteItemReceived.backgroundColor)
    }

    private fun setUpThemeDataAndAdaptor() {
        themeAdapter = ThemeAdaptor(noteItemReceived.backgroundColor)

        viewBinding.listBackgroundTheme.adapter = themeAdapter
        viewBinding.listBackgroundTheme.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL, false
        )
        // size will be changing
        viewBinding.listBackgroundTheme.setHasFixedSize(true)
        themeAdapter.differ.submitList(viewModel.getAllThemes())
        themeAdapter.setOnItemClickListener { item ->
            changeTheme(item)
        }
    }

    private fun changeTheme(value: Int) {

        viewModel.setThemeValue(value)
        themeAdapter.currentSelected = value
        themeAdapter.notifyDataSetChanged()
        viewBinding.listBackgroundTheme.scrollToPosition(value)
        changeThemeInView(value)
    }

    private fun changeThemeInView(value: Int) {
        val item = viewModel.getThemeItem(value)
        // edit text colors
        viewBinding.etTitle.setTextColor(Color.parseColor(item.editTextColor))
        viewBinding.etTitle.setHintTextColor(Color.parseColor(item.hintTextColor))

        // time value color
        viewBinding.tvTime.setTextColor(Color.parseColor(item.hintTextColor))
        // whole background color
        viewBinding.root.setBackgroundColor(Color.parseColor(item.backGroundColor))
        // tool bar color
        viewBinding.toolbar.setBackgroundColor(Color.parseColor(item.toolBarColor))
        viewBinding.toolbar.setTitleTextColor(Color.parseColor(item.editTextColor))
        viewBinding.toolbar.navigationIcon!!.setColorFilter(
            Color.parseColor(item.editTextColor),
            PorterDuff.Mode.SRC_ATOP
        );
        // theme list background color
        viewBinding.listBackgroundTheme.setBackgroundColor(Color.parseColor(item.toolBarColor))
        // adding color to the keyboard shown item
        viewBinding.softInputAboutView.setBackgroundColor(Color.parseColor(item.toolBarColor))

        // TODO adding color to items of description -  just add and remove the list but first update the theme item

        adaptor.noteItemTheme = item
        // to update the view theme
        adaptor.notifyDataSetChanged()
    }

    private fun setUpDescButton() {
        viewBinding.btCheckBox.setOnClickListener {
            viewModel.changeCheckBoxVisibility()
        }
    }

    private fun setUpTitleData() {
        viewBinding.etTitle.addTextChangedListener {
            // set the text in viewModel
            viewModel.setTitle(it.toString())
        }
        viewBinding.etTitle.setText(noteItemReceived.title)
        viewBinding.etTitle.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                titleFocused()
            }
        }
    }

    private fun titleFocused() {
        isFocused = true
        changeThemeVisibility(false)
        setUpToolBar()
        viewBinding.softInputAboutView.visibility = View.GONE
    }

    private fun descFocused(focused: Boolean) {
        if (!focused) {
            return
        }
        isFocused = focused
        changeThemeVisibility(false)
        setUpToolBar()
        viewBinding.softInputAboutView.visibility = View.VISIBLE
    }

    private fun setUpLiveData() {
        viewModel.focusEvent.observeEvent(this, adaptor::setItemFocus)
        viewModel.focusGainEvent.observe(this) {
            Log.i("Focus Changed", "Focus - $it")
            descFocused(it)
        }

        viewModel.editItems.observe(this) {
            adaptor.differ.submitList(it)
        }

        viewModel.checkBoxVisibilityEvent.observeEvent(this, adaptor::changeCheckVisibility)

    }

    private fun setUpAdaptor() {
        adaptor = EditAdaptor(ConstantValues.themeList[1], viewModel)
        viewBinding.rvDescription.adapter = adaptor

        viewBinding.rvDescription.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )

        // size will be changing
        viewBinding.rvDescription.setHasFixedSize(false)

    }


    // setUp initial data to the view
    private fun setUpViewData() {
        viewBinding.tvTime.text = ConstantValues.dateConvert(noteItemReceived.id)
    }


    private fun setUpViewModel() {
        val notesRepository = NotesRepository(NotesDataBase(this)!!)
        val viewModelProviderFactory =
            CreatEditViewModelProvider(application, notesRepository, noteItemReceived)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[CreatEditViewModel::class.java]
    }

    private fun setUpToolBar() {
        setSupportActionBar(viewBinding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater
        if (isFocused) {
            inflater.inflate(R.menu.action_bar_menu, menu)
        } else {
            inflater.inflate(R.menu.note_menu, menu)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_change_theme -> {
                changeThemeVisibility(
                    viewBinding.themeCardView.visibility != View.VISIBLE
                )
            }

            R.id.menu_item_delete -> {
                viewModel.deleteNote()
                finish()
            }

            R.id.menu_done -> {
                // remove focus from everything
                onMenuDoneButtonClicked()
            }

            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun onMenuDoneButtonClicked() {
        viewModel.resetFocusGainEvent()
        viewBinding.removeFocusText.requestFocus()
        isFocused = false
        setUpToolBar()
        viewBinding.removeFocusText.clearFocus()
        viewBinding.removeFocusText.hideKeyboard()
        viewBinding.softInputAboutView.visibility = View.GONE
    }

    override fun onStop() {
        viewModel.updateNote()
        super.onStop()
    }

}