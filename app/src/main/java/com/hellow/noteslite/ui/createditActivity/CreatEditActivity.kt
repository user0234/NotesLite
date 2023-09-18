package com.hellow.noteslite.ui.createditActivity

import android.graphics.Color
import android.graphics.PorterDuff
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
    private lateinit var noteItemReceived: NoteItem
    private lateinit var themeList: List<ThemeItem>
    private lateinit var descriptionListLatest: List<NoteSubItem>

    private var focusOnTitle = false
    private var keyboardNotTriggerChange = false
    private var focusOnDescriptionItem = false
    private var keyBoardVisible = false

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

        // whole viewModelSetup
        setUpViewModel()

        // set up the adaptors for theme data

        themeList = viewModel.getAllThemes()
        setUpThemeAdaptor()
        // setUp app bar with different menu

        // initial color of app bar
        viewBinding.appBar.setBackgroundColor(Color.parseColor(themeList[noteItemReceived.backgroundColor].toolBarColor))
        setUpToolBar()

        // setUp title values
        setUpTitleView()
        viewBinding.tvTime.text = ConstantValues.dateConvert(noteItemReceived.id)
        // set up description items
        //  descriptionListLatest = noteItem Received .description
        setUpDescriptionAdaptor()

        setUpCheckBoxButton()

        setKeyboardVisibilityListener(this)

        viewBinding.btRemoveFocus.setOnClickListener {

        }
    }

    private fun removeFocusTitle() {
        viewModel.setTitle(viewBinding.etTitle.text.toString())
        focusOnTitle = false

        clearKeyBoard()

        setUpToolBar()
    }

    private fun addFocusTitle() {
        focusOnTitle = true
        requestKeyBoard()
        setUpToolBar()
        changeThemeVisibility(false)
    }

    private fun setUpTitleView() {
        // assign initial value
        viewBinding.etTitle.setText(noteItemReceived.title)
        viewBinding.etTitle.setTextColor(Color.parseColor(themeList[noteItemReceived.backgroundColor].editTextColor))
        viewBinding.etTitle.setHintTextColor(Color.parseColor(themeList[noteItemReceived.backgroundColor].hintTextColor))
        viewBinding.removeFocusText.setOnFocusChangeListener { _, hasFocus ->

        }
        viewBinding.btRemoveFocus.setOnClickListener {
            doRemoveFocusFromEveryOne()
        }
        viewBinding.etTitle.setOnFocusChangeListener { _, hasFocus ->

            if (hasFocus) {
                // someone clicked on it
                addFocusTitle()

            } else {
                // some one clicked on tick or change focus to description item
                removeFocusTitle()
            }

        }
    }
    override fun onVisibilityChanged(visible: Boolean) {
        // when keyboard hides or shows
            keyBoardVisible = visible
    }
    private fun doRemoveFocusFromEveryOne() {
        viewBinding.removeFocusText.requestFocus()
        viewBinding.removeFocusText.clearFocus()
    }


    private fun removeFocusDesc(position: Int, currentText: String) {
        descriptionAdaptor.focusItemPosition = -1
        descriptionAdaptor.differ.currentList[position].textValue = currentText
        viewModel.updateDescriptionItem(descriptionAdaptor.differ.currentList)
        viewBinding.softInputAboutView.visibility = View.GONE
        clearKeyBoard()
        setUpToolBar()
    }

    private fun onAddFocusDesc(position: Int) {
        viewBinding.softInputAboutView.visibility = View.GONE
        descriptionAdaptor.focusItemPosition = position
        setUpToolBar()
        requestKeyBoard()
        changeThemeVisibility(false)
    }

    private fun changeDescItemCurrent(){
        focusOnDescriptionItem = true
        viewBinding.removeFocusText.requestFocus()
        viewBinding.removeFocusText.clearFocus()
        focusOnDescriptionItem = false
    }

    private fun changeFocusItemTypeFocusRemoved(position: Int, type: NoteSubItemType, text:String){
                descriptionListLatest[position].type = type
                descriptionListLatest[position].textValue = text
                descriptionListLatest[position].checkBox = false
                requestFocusOnDesc(position)
    }

    private fun requestFocusOnDesc(position: Int) {
        descriptionAdaptor.focusItemPosition = -1
        descriptionAdaptor.differ.submitList(null)
        descriptionAdaptor.focusItemPosition = position
        descriptionAdaptor.differ.submitList(descriptionListLatest)
    }


    private fun setUpDescriptionAdaptor() {
        // description adaptor is create using the current theme item
        descriptionAdaptor =
            EditCreateDescriptionItemAdaptor(themeList[noteItemReceived.backgroundColor])

        viewBinding.rvDescription.adapter = descriptionAdaptor

        viewBinding.rvDescription.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )

        // size will be changing
        viewBinding.rvDescription.setHasFixedSize(false)

        // this will be called when we perform addition or deletion on EditCreateDescriptionItemAdaptor
        descriptionAdaptor.setOnItemAddDeleteListener { isCreate, noteSubItemType, currentItemPos, text ->
            // this will be used to delete the item only
            doRemoveFocusFromEveryOne()
            if (!isCreate) {
                // delete current item

                viewModel.editDescriptionItem(false, currentItemPos, noteSubItemType, text)
              //  removeFocusAll()
                descriptionAdaptor.focusItemPosition = currentItemPos - 1
                descriptionAdaptor.differ.submitList(descriptionListLatest)
                requestFocusOnDesc(descriptionAdaptor.focusItemPosition)
            } else {
                //  create new item

                viewModel.editDescriptionItem(true, currentItemPos, noteSubItemType, text)
              // removeFocusAll()
                descriptionAdaptor.focusItemPosition = currentItemPos + 1
                descriptionAdaptor.differ.submitList(descriptionListLatest)
                requestFocusOnDesc(descriptionAdaptor.focusItemPosition)
            }

        }
        // this will be called when we click on an item or remove focus from an item
        descriptionAdaptor.setOnItemChangeFocusListener { hasfocus, position, text ->
            if(focusOnDescriptionItem){
                if(!hasfocus){
                   val type = if(descriptionListLatest[position].type == NoteSubItemType.CheckBox){
                       NoteSubItemType.String
                   }else{
                       NoteSubItemType.CheckBox
                   }
                    changeFocusItemTypeFocusRemoved(position,type, text)
                }
            }else{
                if (hasfocus) {
                    onAddFocusDesc(position)
                } else {
                    removeFocusDesc(position, text)
                }
            }

        }
        // initial list data setup
        descriptionAdaptor.differ.submitList(noteItemReceived.description)

        viewModel.descListLiveData.observe(this) {
            descriptionListLatest = it
        }
    }

       private fun setUpCheckBoxButton() {
        viewBinding.btCheckBox.setOnClickListener {
            changeDescItemCurrent()

        }

    }

    private fun setUpThemeAdaptor() {

        // first value of selected theme when the list is create
        themeAdaptor = ThemeAdaptor(noteItemReceived.backgroundColor)

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

    private fun requestKeyBoard() {
        (applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
            viewBinding.root,
            InputMethodManager.SHOW_FORCED
        )
    }

    private fun clearKeyBoard() {
        keyboardNotTriggerChange = true
        (applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            viewBinding.root.windowToken,
            0
        )
        keyboardNotTriggerChange = false
    }

    private fun setUpViewModel() {
        val notesRepository = NotesRepository(NotesDataBase(this)!!)
        val viewModelProviderFactory =
            CreatEditViewModelProvider(application, notesRepository, noteItemReceived)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[CreatEditViewModel::class.java]

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
        Log.i("themeStup", "setThemeToView: ${item.timeTextColor}")

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

        viewBinding.toolbar.navigationIcon!!.setColorFilter(Color.parseColor(item.editTextColor), PorterDuff.Mode.SRC_ATOP);
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

        if (focusOnTitle || descriptionAdaptor.focusItemPosition != -1) {
            if(focusOnTitle){
                inflater.inflate(R.menu.action_bar_menu, menu)
            }else{
                inflater.inflate(R.menu.list_item_menu, menu)
            }

        } else {
            inflater.inflate(R.menu.note_menu, menu)
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

            R.id.menu_item_check_box -> {
                changeDescItemCurrent()
            }

            R.id.menu_item_delete -> {
                viewModel.deleteNote()
                finish()
            }

            R.id.menu_done -> {
                // remove focus from everything
                 doRemoveFocusFromEveryOne()
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