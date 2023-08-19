package com.hellow.noteslite.ui.createditActivity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellow.noteslite.R
import com.hellow.noteslite.adaptor.EditCreateDescriptionItemAdaptor
import com.hellow.noteslite.adaptor.ThemeAdaptor
import com.hellow.noteslite.database.NotesDataBase
import com.hellow.noteslite.databinding.ActivityCreatEditBinding
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.model.NoteSubItem
import com.hellow.noteslite.model.NoteSubItemType
import com.hellow.noteslite.model.ThemeItem
import com.hellow.noteslite.repository.NotesRepository
import com.hellow.noteslite.utils.ConstantValues
import com.hellow.noteslite.utils.CreatEditViewModelProvider
import com.hellow.noteslite.utils.OnKeyBoardListener


class CreatEditActivity : AppCompatActivity(), OnKeyBoardListener {

    private lateinit var viewBinding: ActivityCreatEditBinding
    private lateinit var viewModel: CreatEditViewModel
    private lateinit var themeAdaptor: ThemeAdaptor
    private lateinit var descriptionAdaptor: EditCreateDescriptionItemAdaptor
    private lateinit var noteItemRecieved: NoteItem
    private lateinit var themeList: List<ThemeItem>
    private lateinit var descriptionListLatest: List<NoteSubItem>

    private var isActionTabBar = false
    private var focusChangeTriggeredByKeyBoard = false

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set up view Binding
        viewBinding = ActivityCreatEditBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        // get the note item passed
        if (intent.hasExtra("noteItem")) {
            noteItemRecieved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

        // whole viewModelSetup
        setUpViewModel()

        // set up the adaptors for theme data

        themeList = viewModel.getAllThemes()
        setUpThemeAdaptor()
        // setUp app bar with different menu

        // initial color of app bar
        viewBinding.appBar.setBackgroundColor(Color.parseColor(themeList[noteItemRecieved.backgroundColor].toolBarColor))
        setUpToolBar()

        // setUp title values
        setUpTitleView()
        viewBinding.tvTime.text = ConstantValues.dateConvert(noteItemRecieved.id)
        // set up description items
        //  descriptionListLatest = noteItemRecived.description
        setUpDescriptionAdaptor()

        setUpCheckBoxButton()

        setKeyboardVisibilityListener(this)
    }

    private fun setUpTitleView() {
        // assign initial value
        viewBinding.etTitle.setText(noteItemRecieved.title)
        viewBinding.etTitle.setTextColor(Color.parseColor(themeList[noteItemRecieved.backgroundColor].editTextColor))
        viewBinding.etTitle.setHintTextColor(Color.parseColor(themeList[noteItemRecieved.backgroundColor].hintTextColor))

        viewBinding.etTitle.setOnFocusChangeListener { _, hasFocus ->

            if(focusChangeTriggeredByKeyBoard){

                if(!hasFocus){
                    setUpToolBar()
                }

                return@setOnFocusChangeListener
            }
            if (hasFocus) {

                isActionTabBar = true
                changeThemeVisibility(false)
                setUpToolBar()

                // this will trigger show keyboard code
                requestKeyBoard()
                viewBinding.softInputAboutView.visibility = View.GONE

            } else {

                // this will trigger if we click on tick or we change focus to description item and update the current stored item in the view model value
                isActionTabBar = false
                descriptionAdaptor.wasFocusTitle = true
                setUpToolBar()
                clearKeyBoard()
                viewModel.setTitle(viewBinding.etTitle.text.toString())
            }

        }
    }

    private fun setUpDescriptionAdaptor() {
        // description adaptor is create using the current theme item
        descriptionAdaptor =
            EditCreateDescriptionItemAdaptor(themeList[noteItemRecieved.backgroundColor])

        viewBinding.rvDescription.adapter = descriptionAdaptor

        viewBinding.rvDescription.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )

        // size will be changing
        viewBinding.rvDescription.setHasFixedSize(false)

        // this will be called when we perform addition or deletion on EditCreateDescriptionItemAdaptor
        descriptionAdaptor.setOnItemAddDeleteListener { isCreate, noteSubItemType, currentItemPos, text ->
            if (isCreate) {
                // create a new item
                viewModel.editDescriptionItem(isCreate, currentItemPos, noteSubItemType, text)

                descriptionAdaptor.keyBoardFocusItem = currentItemPos + 1
                descriptionAdaptor.differ.submitList(descriptionListLatest)


            } else {
                // delete the current item and add focus to previous
                viewModel.editDescriptionItem(isCreate, currentItemPos, noteSubItemType, text)

                descriptionAdaptor.keyBoardFocusItem = currentItemPos - 1
                descriptionAdaptor.differ.submitList(descriptionListLatest)
            }

        }
        // this will be called when we click on an item or remove focus from an item
        descriptionAdaptor.setOnItemChangeFocusListener { hasfocus, position ->
            if (hasfocus) {

                changeThemeVisibility(false)
                viewBinding.softInputAboutView.visibility = View.VISIBLE
                requestKeyBoard()

            } else {
                clearKeyBoard()
                viewBinding.softInputAboutView.visibility = View.GONE
                // to update the values of description in viewModel and differ
                viewModel.updateDescriptionItem(descriptionAdaptor.differ.currentList)

            }
            setUpToolBar()
        }
        // initial list data setup
        descriptionAdaptor.differ.submitList(noteItemRecieved.description)

        viewModel.descListLiveData.observe(this) {
            descriptionListLatest = it
        }
    }

    private fun setUpThemeAdaptor() {

        // first value of selected theme when the list is create
        themeAdaptor = ThemeAdaptor(noteItemRecieved.backgroundColor)

        viewBinding.listBackgroundTheme.adapter = themeAdaptor
        viewBinding.listBackgroundTheme.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL, false
        )
        viewBinding.listBackgroundTheme.setHasFixedSize(true)
        // to hide move the theme item and hide it
        viewBinding.themeCardView.animate()
            .translationY(viewBinding.themeCardView.height.toFloat())
            .setDuration(1)
            .withStartAction {
                viewBinding.themeCardView.visibility = View.GONE
            }
            .start()


        themeAdaptor.differ.submitList(themeList)

        // this is will be called when we set the theme using the theme item

        themeAdaptor.setOnItemClickListener {
            // make the background as selected theme
            //   setThemeToView(themeList[it])
            viewModel.setThemeValue(it)

        }

        // this will be called when we change the theme using fun =${viewModel.setThemeValue(it)}
        viewModel.themeLiveData.observe(this) {

            themeAdaptor.currentSelected = it
            themeAdaptor.differ.submitList(null)
            themeAdaptor.differ.submitList(themeList)
            // to get current list based on dark mode and any other factor
            val currentTheme = viewModel.getThemeItem(it)
            setThemeToView(currentTheme)

        }

    }

    private fun setUpViewModel() {
        val notesRepository = NotesRepository(NotesDataBase(this)!!)
        val viewModelProviderFactory =
            CreatEditViewModelProvider(application, notesRepository, noteItemRecieved)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[CreatEditViewModel::class.java]

    }

    private fun setUpCheckBoxButton() {
        viewBinding.btCheckBox.setOnClickListener {
            val list = descriptionAdaptor.differ.currentList
            val currentFocused = descriptionAdaptor.keyBoardFocusItem
            // change the item type to check box
            if (list[currentFocused].type == NoteSubItemType.CheckBox) {

                viewModel.changeDescriptionItem(
                    NoteSubItem(
                        currentFocused,
                        NoteSubItemType.String,
                        false,
                        list[currentFocused].textValue
                    )
                )
            } else {
                viewModel.changeDescriptionItem(
                    NoteSubItem(
                        currentFocused,
                        NoteSubItemType.CheckBox,
                        false,
                        list[currentFocused].textValue
                    )
                )
            }

            viewBinding.etTitle.requestFocus()
            viewBinding.etTitle.clearFocus()
            descriptionAdaptor.keyBoardFocusItem = currentFocused
            descriptionAdaptor.notifyItemChanged(currentFocused)

        }

    }

    private fun requestKeyBoard() {
        (applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
            viewBinding.root,
            InputMethodManager.SHOW_FORCED
        )
    }

    private fun clearKeyBoard() {
        (applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            viewBinding.root.windowToken,
            0
        )
    }

    override fun onVisibilityChanged(visible: Boolean) {

        // when keyboard hides or shows
        if (!visible) {
            focusChangeTriggeredByKeyBoard = true
            descriptionAdaptor.keyBoardFocusItem = -1
            isActionTabBar = false
            setUpToolBar()
            viewBinding.etTitle.requestFocus()
            viewBinding.etTitle.clearFocus()
            focusChangeTriggeredByKeyBoard = false
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

    private fun setThemeToView(item: ThemeItem) {
        timber.log.Timber.i("Theme_selected")

        // edit text colors
        viewBinding.etTitle.setTextColor(Color.parseColor(item.editTextColor))
        viewBinding.etTitle.setHintTextColor(Color.parseColor(item.hintTextColor))

        // time value color
        viewBinding.tvTime.setTextColor(Color.parseColor(item.hintTextColor))
        // whole background color
        viewBinding.root.setBackgroundColor(Color.parseColor(item.backGroundColor))
        // tool bar color
        viewBinding.toolbar.setBackgroundColor(Color.parseColor(item.toolBarColor))
        // theme list background color
        viewBinding.listBackgroundTheme.setBackgroundColor(Color.parseColor(item.toolBarColor))
        // adding color to the keyboard shown item
        viewBinding.softInputAboutView.setBackgroundColor(Color.parseColor(item.toolBarColor))

        // TODO adding color to items of description -  just add and remove the list but first update the theme item

        descriptionAdaptor.noteItemTheme = item
        // to update the view theme
        descriptionAdaptor.notifyDataSetChanged()

    }

    private fun setUpToolBar() {
        setSupportActionBar(viewBinding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater

        if (!isActionTabBar && descriptionAdaptor.keyBoardFocusItem == -1) {

            inflater.inflate(R.menu.note_menu, menu)
        } else {
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

            R.id.menu_item_delete -> {
                viewModel.deleteNote()
                finish()
            }

            R.id.menu_done -> {

                viewBinding.softInputAboutView.visibility = View.GONE
                if (descriptionAdaptor.keyBoardFocusItem != -1) {
                    descriptionAdaptor.keyBoardFocusItem = -1
                    viewBinding.etTitle.requestFocus()
                    viewBinding.etTitle.clearFocus()
                    // fix this to update other way

                } else {
                    viewBinding.etTitle.clearFocus()
                }

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

    private fun setKeyboardVisibilityListener(onKeyboardVisibilityListener: OnKeyBoardListener) {
        val parentView = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
        parentView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            private var alreadyOpen = false
            private val defaultKeyboardHeightDP = 100
            private val EstimatedKeyboardDP =
                defaultKeyboardHeightDP + 48
            private val rect = Rect()
            override fun onGlobalLayout() {
                val estimatedKeyboardHeight = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    EstimatedKeyboardDP.toFloat(),
                    parentView.resources.displayMetrics
                ).toInt()
                parentView.getWindowVisibleDisplayFrame(rect)
                val heightDiff = parentView.rootView.height - (rect.bottom - rect.top)
                val isShown = heightDiff >= estimatedKeyboardHeight
                if (isShown == alreadyOpen) {
                    Log.i("Keyboard state", "Ignoring global layout change...")
                    return
                }
                alreadyOpen = isShown
                onKeyboardVisibilityListener.onVisibilityChanged(isShown)
            }
        })

    }
//
//
//    private val actionModeCallBackSubTitle: ActionMode.Callback = object : ActionMode.Callback {
//        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
//            mode!!.menuInflater.inflate(R.menu.action_bar_menu, menu)
//            return true
//        }
//
//        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
//            return false
//        }
//
//        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
//            return when (item!!.itemId) {
//                R.id.menu_done -> {
//                    viewBinding.etTitle.clearFocus()
//                    mode?.finish()
//                    true
//                }
//
//                android.R.id.home -> {
//                    this@CreatEditActivity.finish()
//                    return true
//                }
//
//                else -> false
//            }
//
//        }
//
//        override fun onDestroyActionMode(mode: ActionMode?) {
//            actionMode = null
//            //    viewBinding.etDescription.clearFocus()
//            viewBinding.appBar.visibility = View.VISIBLE
//        }
//
//    }
//
//    private val actionModeCallBackTitle: ActionMode.Callback = object : ActionMode.Callback {
//        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
//            mode!!.menuInflater.inflate(R.menu.action_bar_menu, menu)
//            return true
//        }
//
//        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
//            return false
//        }
//
//        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
//            return when (item!!.itemId) {
//                R.id.menu_done -> {
//                    viewBinding.etTitle.clearFocus()
//                    mode?.finish()
//                    true
//                }
//
//                android.R.id.home -> {
//                    this@CreatEditActivity.finish()
//                    return true
//                }
//
//                else -> false
//            }
//
//        }
//
//        override fun onDestroyActionMode(mode: ActionMode?) {
//            actionMode = null
//            viewBinding.etTitle.clearFocus()
//            viewBinding.appBar.visibility = View.VISIBLE
//        }
//
//    }
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if (event!!.action == KeyEvent.ACTION_DOWN) {
//            when (keyCode) {
//                KeyEvent.KEYCODE_BACK -> {
//                     if((applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).isActive){
//                         // remove focus
//                         if(isActionTabBar||descriptionAdaptor.keyBoardFocusItem != -1) {
//                             if (descriptionAdaptor.keyBoardFocusItem != -1) {
//                                 descriptionAdaptor.keyBoardFocusItem = -1
//                                 viewBinding.etTitle.requestFocus()
//                                 viewBinding.etTitle.clearFocus()
//                                 // fix this to update other way
//
//                             } else {
//                                 viewBinding.etTitle.clearFocus()
//                             }
//                     }
//
//                    }
//                }
//            }
//        }
//        return super.onKeyDown(keyCode, event)
//    }

}