package app.poc.wifi

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    var WEB_URL: String? = ""
    lateinit var cpWebview: WebView

    //info: onclick on back button
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (cpWebview.canGoBack()) {
            cpWebview.goBack()
        } else {
            super.onBackPressed()
        }
    }

    /* fun loadErrorPage(webview:WebView){
         if(webview!=null){

             val htmlData: String  ="<html><body><div align=\"center\" >\"This is the description for the load fail : /\+description+\"\nThe failed url is : \"+failingUrl+\"\n\"</div></body>"

             webview.loadUrl("about:blank");
             webview.loadDataWithBaseURL(null,htmlData, "text/html", "UTF-8",null)
             webview.invalidate()

         }
     }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        cpWebview = findViewById(R.id.cpWebview)

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "RailWire Wi-Fi"//"Captive portal"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

        getIntentData()
        cpWebview.clearCache(true) // info: use for clear cache
        cpWebview.settings.loadsImagesAutomatically = true
        cpWebview.settings.javaScriptEnabled = true
        cpWebview.settings.domStorageEnabled = true
        cpWebview.settings.javaScriptCanOpenWindowsAutomatically = true
        cpWebview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        cpWebview.webViewClient = object : WebViewClient() {

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                Log.e("test", "Thread onLoadResource...........................")

            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.e("test", "onPageStarted ...........................")

            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.e("test", "onPageFinished ...........................")

            }

            override
            fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                val builder = AlertDialog.Builder(this@WebViewActivity)
                builder.setMessage("SSL Certificate Error")
                builder.setPositiveButton("continue") { dialog, which -> handler?.proceed() }
                builder.setNegativeButton("cancel") { dialog, which -> handler?.cancel() }
                val dialog = builder.create()
                dialog.show()

                Log.e("test", "onReceivedSslError ...........................")

            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                Toast.makeText(
                    this@WebViewActivity,
                    "description: $description",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("test", "onReceivedError ...........................")
            }

        }

        //cpWebview.setWebChromeClient(WebChromeClient())
        //urlText.text = WEB_URL
        cpWebview.loadUrl(WEB_URL ?: "")

    }

    private fun getIntentData() {

        WEB_URL = intent.getStringExtra("url")
        Log.e("test", "WEB_URL-> $WEB_URL ...........................")

    }

}