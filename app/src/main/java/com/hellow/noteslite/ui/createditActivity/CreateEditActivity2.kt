package com.hellow.noteslite.ui.createditActivity

import android.annotation.SuppressLint
import android.content.res.Configuration
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
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellow.noteslite.R
import com.hellow.noteslite.adaptor.EditCreateDescriptionItemAdaptor
import com.hellow.noteslite.adaptor.ThemeAdaptor
import com.hellow.noteslite.database.NotesDataBase
import com.hellow.noteslite.databinding.ActivityCreatEditBinding
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.model.ThemeItem
import com.hellow.noteslite.repository.NotesRepository
import com.hellow.noteslite.utils.CreatEditViewModelProvider
import com.hellow.noteslite.utils.OnKeyBoardListener

class CreateEditActivity2 : AppCompatActivity(), OnKeyBoardListener {

    private lateinit var viewBinding: ActivityCreatEditBinding
    private lateinit var viewModel: CreatEditViewModel
    private lateinit var themeAdaptor: ThemeAdaptor
    private lateinit var descriptionAdaptor: EditCreateDescriptionItemAdaptor
    private lateinit var currentItem: NoteItem

    private var actionMode: ActionMode? = null
    private val themeList: MutableList<ThemeItem> = mutableListOf()
    private var toolBarColor: String = "#0f0f0f"
    private var isActionTabBar = false


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityCreatEditBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (intent.hasExtra("noteItem")) {
            currentItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(
                    "noteItem",
                    NoteItem::class.java
                )!!

            } else {
                intent.getParcelableExtra("noteItem")!!

            }
            viewBinding.toolbar.title = ""

        } else {
            // kill the activity if note item not found
            finish()
        }

        val notesRepository = NotesRepository(NotesDataBase(this)!!)
        val viewModelProviderFactory =
            CreatEditViewModelProvider(application, notesRepository, currentItem)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[CreatEditViewModel::class.java]

        setUpToolBar()

        setUpCheckBoxButton()
        setUpDescriptionRecyclerView()

        viewModel.descListLiveData.observe(this) {
            // to make the list refresh
            descriptionAdaptor.differ.submitList(null)
            descriptionAdaptor.differ.submitList(it)
        }

        viewModel.themeLiveData.observe(this) {
            setThemeValue(it)
            themeAdaptor.currentSelected = it
        }


        setKeyboardVisibilityListener(this)
    }

    private fun setUpCheckBoxButton() {

    }

    override fun onVisibilityChanged(visible: Boolean) {
    }

    private fun setKeyboardVisibilityListener(onKeyboardVisibilityListener: OnKeyBoardListener){
        val parentView = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
        parentView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
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



    private fun setUpDescriptionRecyclerView() {
        descriptionAdaptor = EditCreateDescriptionItemAdaptor(themeList[currentItem.backgroundColor])

        viewBinding.rvDescription.adapter = descriptionAdaptor
        viewBinding.rvDescription.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )
        viewBinding.rvDescription.setHasFixedSize(false)


    }




    private fun changeThemeVisibility(isVisible: Boolean) {

        // check for focusablity

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

        viewBinding.etTitle.setTextColor(Color.parseColor(item.editTextColor))
        viewBinding.etTitle.setHintTextColor(Color.parseColor(item.hintTextColor))
        viewBinding.tvTime.setTextColor(Color.parseColor(item.hintTextColor))
        viewBinding.root.setBackgroundColor(Color.parseColor(item.backGroundColor))
        viewBinding.toolbar.setBackgroundColor(Color.parseColor(item.toolBarColor))
        viewBinding.toolbar.setTitleTextColor(Color.parseColor(item.editTextColor))
        viewBinding.listBackgroundTheme.setBackgroundColor(Color.parseColor(item.toolBarColor))
        toolBarColor = item.toolBarColor
        setUpToolBar()


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

        if (!isActionTabBar && descriptionAdaptor.focusItemPosition == -1) {

            inflater.inflate(R.menu.note_menu, menu)
            viewBinding.softInputAboutView.visibility = View.GONE
        } else {
            inflater.inflate(R.menu.action_bar_menu, menu)
            viewBinding.softInputAboutView.visibility = View.VISIBLE
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
                if(descriptionAdaptor.focusItemPosition != -1){
                    descriptionAdaptor.focusItemPosition = -1
                    viewBinding.etTitle.requestFocus()
                    viewBinding.etTitle.clearFocus()
                    // fix this to update other way

                }else {
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