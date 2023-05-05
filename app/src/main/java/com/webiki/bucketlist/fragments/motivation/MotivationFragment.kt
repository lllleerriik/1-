package com.webiki.bucketlist.fragments.motivation

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.webiki.bucketlist.databinding.FragmentMotivationBinding

class MotivationFragment : Fragment() {

    private var _binding: FragmentMotivationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val motivationViewModel =
            ViewModelProvider(this).get(MotivationViewModel::class.java)

        _binding = FragmentMotivationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        val videoView: WebView = binding.videoView
        val videoURL = "https://www.youtube.com/watch?v=R6dk8FJkyMA"

        videoView.settings.javaScriptEnabled = true
        videoView.webViewClient  = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }
        }
        videoView.loadData("<html><body><iframe width=\\\"320\\\" height=\\\"315\\\"\" +\n" +
                "            \" src=\\\"https://www.youtube.com/embed/watch?v=5kbwx78zdag\\\" frameborder=\\\"0\\\" \" +\n" +
                "            \"allowfullscreen></iframe></body></html>",
        "text/html",
        "utf-8")

        motivationViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}