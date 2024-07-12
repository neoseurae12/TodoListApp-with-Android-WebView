package com.daangn.todolistapp

import android.content.Context
import android.content.res.Configuration
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.daangn.todolistapp.databinding.ActivityMainBinding
import com.daangn.todolistapp.model.TodoEntity
import java.time.LocalDate
import java.util.UUID

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

//    private lateinit var todoList: List<TodoEntity>
    private lateinit var todo: TodoEntity

    private var webAddButtonEnabled = true
    private var hasInitialized = false

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
                        if (webAddButtonEnabled) {
                            todoAddImageButton.isEnabled = !s.isNullOrBlank()
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })
            }

            todoListWebView.apply {
                settings.javaScriptEnabled = true
                webChromeClient = WebChromeClient()
                addJavascriptInterface(WebAndroidBridge(this@MainActivity), "Android")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        // 페이지가 로드된 후 LiveData 관찰 시작
                        mainViewModel.todoListLiveData.observe(this@MainActivity) { todos ->

                            // 데이터가 처음 로드된 경우에만 리스트 순회
                            if (!hasInitialized && !todos.isNullOrEmpty()) {
                                for (todo in todos) {
                                    val js = "javascript:addTodoItem('${todo.id}', '${todo.content}', '${todo.dueDate ?: ""}', ${todo.isDone})"
                                    binding.todoListWebView.loadUrl(js)
                                }

                                // 데이터가 로드되었음을 표시
                                hasInitialized = true
                                Log.d(TAG, "hasInitialized: $hasInitialized")

                                mainViewModel.loadTodo(todos[0].id)
                            }

                            for (todo in todos) {
                                Log.d(TAG, "$todo")
                            }
                            Log.d(TAG, "Got ${todos.size} todo(s)")
                        }
                    }
                }

                loadUrl("file:///android_asset/www/index.html")
            }

            todoAddImageButton.apply {
                isEnabled = false

                setOnClickListener {
                    Log.d(TAG, todoTextInputEditText.text.toString())

                    val content = todoTextInputEditText.text.toString()

                    val js = "javascript:addTodoItem('', '$content', '', false)"
                    todoListWebView.loadUrl(js)

                    todoTextInputEditText.text?.clear()
                    todoTextInputEditText.clearFocus()
                }
            }
        }

        mainViewModel.apply {

//            todoListLiveData.observe(this@MainActivity) { todos ->
//                todos?.let {
//                    Log.i(TAG, "Got ${todos.size} todo(s)")
//
//                    // todoList = todos
//
//                    // val js = "javascript:setTodos(${todos.size})"
//                    // binding.todoListWebView.loadUrl(js)
//                }
//
//                Log.i(TAG, "${hasInitialized}")
//
//                if (!hasInitialized && !todos.isNullOrEmpty()) {
//                    for (todo in todos) {
//                        val js = "javascript:addTodoItem('${todo.content}', '${todo.dueDate}')"
//                        binding.todoListWebView.loadUrl(js)
//                    }
//
//                    hasInitialized = true
//                }
//
////                for (todo in todos) {
////                    binding.todoListWebView.evaluateJavascript("javascript:addTodoItem('${todo.content}', '${todo.dueDate}')", null)
////                }
//            }

            todoLiveData.observe(this@MainActivity) { todo ->
                todo?.let {
                    this@MainActivity.todo = todo
                }
                // Log.i(TAG, "todoLiveData: $todo")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // '모든 투두 삭제하기' 메뉸 아이템 => 위험한 동작임을 암시하는 스타일 적용
        val deleteAllTodosMenuItem = menu?.findItem(R.id.deleteAllTodos)
        deleteAllTodosMenuItem?.let {
            val title = SpannableString(it.title)
            title.apply {
                setSpan(ForegroundColorSpan(Color.RED), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)  // 글자 색상: 빨강색
                setSpan(StyleSpan(Typeface.BOLD), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)    // 글자 스타일: 볼드
            }
            it.title = title
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.deleteAllTodos -> {
                // TODO: 다이얼로그 띄워서 한 번 더 '진짜 삭제할 거냐' 묻기

                val js = "javascript:removeAllTodoItems()"
                binding.todoListWebView.loadUrl(js)

                mainViewModel.deleteAllTodos()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 화면 회전 시 MainActivity 가 재생성되지 않음
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    inner class WebAndroidBridge(private val context: Context) {

        @JavascriptInterface
        fun updateTodo(id: String, content: String, dueDate: String, isDone: Boolean) {
            mainViewModel.updateTodo(id, content, dueDate, isDone)

            mainViewModel.apply {
                loadTodo(UUID.fromString(id))
                updateTodoContent(id, content)
                updateTodoDueDate(id, dueDate)
                updateTodoDone(id, isDone)
            }
        }

        @JavascriptInterface
        fun updateTodoContent(id: String, content: String) {
            mainViewModel.apply {
                loadTodo(UUID.fromString(id))
                todo.content = content
                updateTodoContent(todo)
            }
        }

        @JavascriptInterface
        fun updateTodoDueDate(id: String, dueDate: String) {
            mainViewModel.apply {
                loadTodo(UUID.fromString(id))
                todo.dueDate = LocalDate.parse(dueDate)
                updateTodoDueDate(todo)
            }
        }

        @JavascriptInterface
        fun updateTodoDone(id: String, isDone: Boolean) {
            mainViewModel.apply {
                loadTodo(UUID.fromString(id))
                todo.isDone = isDone
                updateTodoDone(todo)
            }
        }

        @JavascriptInterface
        fun insertTodo(content: String): String {
            val insertedTodo = mainViewModel.insertTodo(content)
            Log.d(TAG, "inserted todo's UUID: ${insertedTodo.id}")
            mainViewModel.loadTodo(insertedTodo.id)
            return insertedTodo.id.toString()
        }

        @JavascriptInterface
        fun deleteTodo(id: String) {
            mainViewModel.apply {
                loadTodo(UUID.fromString(id))
                Log.d(TAG, "deleteTodo: ${todo.content}")
                deleteTodo(todo)
            }
        }

        @JavascriptInterface
        fun updateWebAddButtonEnabled(enabled: Boolean) {
            runOnUiThread {
                webAddButtonEnabled = enabled
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
