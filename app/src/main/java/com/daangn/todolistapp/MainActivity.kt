package com.daangn.todolistapp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.daangn.todolistapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var todo: TodoEntity

    private var loadOldTodos = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.todoListToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.apply {

            todoTextInputEditText.apply {
                setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) {
                        hideKeyboard(v);
                    }
                }

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (mainViewModel.webAddButtonEnabled) {
                            todoAddImageButton.isEnabled = !s.isNullOrBlank()
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })
            }

            todoListWebView.apply {
                settings.javaScriptEnabled = true
                webChromeClient = WebChromeClient()
                addJavascriptInterface(WebAndroidBridge(), "Android")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        // 웹 페이지가 로드된 후 (UI) + 기존 투두들이 모두 로드된 후 (Data) => 기존 투두들을 화면에 랜더링
                        lifecycleScope.launch {
                            mainViewModel.oldTodosReady.collect { oldTodosReady ->
                                if (oldTodosReady) {
                                    val oldTodos = mainViewModel.getOldTodos()
                                    oldTodos.forEach { oldTodo ->
                                        val js =
                                            "javascript:addTodoItem('${oldTodo.id}', '${oldTodo.content}', '${oldTodo.dueDate ?: ""}', ${oldTodo.isDone})"
                                        runOnUiThread {
                                            binding.todoListWebView.loadUrl(js)
                                        }
                                    }

                                    Log.d(TAG, "old todos are loaded: ${oldTodos.size}")
                                }
                            }
                        }
                    }
                }

                loadUrl("file:///android_asset/www/index.html")
            }

            todoAddImageButton.apply {
                isEnabled = false

                setOnClickListener {
                    val content = todoTextInputEditText.text.toString()
                    Log.d(TAG, content)

                    val js = "javascript:addTodoItem('', '$content', '', false)"
                    todoListWebView.loadUrl(js)

                    todoTextInputEditText.text?.clear()
                    todoTextInputEditText.clearFocus()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // '모든 투두 삭제하기' 메뉴 아이템 => 위험한 동작임을 암시하는 스타일 적용
        val deleteAllTodosMenuItem = menu?.findItem(R.id.deleteAllTodos)
        deleteAllTodosMenuItem?.let {
            val title = SpannableString(it.title)
            title.apply {
                setSpan(
                    ForegroundColorSpan(Color.RED),
                    0,
                    title.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )  // 글자 색상: 빨강색
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    title.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )    // 글자 스타일: 볼드
            }
            it.title = title
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.deleteAllTodos -> {
                val js = "javascript:removeAllTodoItems()"
                binding.todoListWebView.loadUrl(js)

                mainViewModel.deleteAllTodos()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    inner class WebAndroidBridge {

        @JavascriptInterface
        fun updateTodoContent(id: String, content: String) {
            mainViewModel.updateTodo(id) { oldTodo ->
                oldTodo.copy(content = content)
            }
        }

        @JavascriptInterface
        fun updateTodoDueDate(id: String, dueDate: String) {
            mainViewModel.updateTodo(id) { oldTodo ->
                oldTodo.copy(dueDate = LocalDate.parse(dueDate))
            }
        }

        @JavascriptInterface
        fun updateTodoDone(id: String, isDone: Boolean) {
            mainViewModel.updateTodo(id) { oldTodo ->
                oldTodo.copy(isDone = isDone)
            }
        }

        @JavascriptInterface
        fun insertTodo(content: String): String {
            val insertedTodo = mainViewModel.insertTodo(content)
            return insertedTodo.id.toString()
        }

        @JavascriptInterface
        fun deleteTodo(id: String) {
            if (id.isBlank()) return

            mainViewModel.deleteTodo(id)
        }

        @JavascriptInterface
        fun updateWebAddButtonEnabled(enabled: Boolean) {
            runOnUiThread {
                mainViewModel.webAddButtonEnabled = enabled
                binding.todoAddImageButton.apply {
                    isEnabled = enabled
                    if (enabled) {
                        setImageResource(R.drawable.add_todo_button)
                    } else {
                        setImageResource(R.drawable.add_todo_button_disabled)
                    }
                }
            }
        }
    }
}
