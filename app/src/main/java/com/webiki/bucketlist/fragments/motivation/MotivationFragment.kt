package com.webiki.bucketlist.fragments.motivation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import com.webiki.bucketlist.R
import com.webiki.bucketlist.databinding.FragmentMotivationBinding
import org.json.JSONArray
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.Scanner
import kotlin.reflect.typeOf

class MotivationFragment : Fragment() {

    private var _binding: FragmentMotivationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var motivationArticlesLayout: LinearLayout
    private var articlesList = mutableListOf(mutableListOf<String>())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMotivationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        motivationArticlesLayout = binding.motivationArticlesLayout

        return root
    }

    override fun onResume() {
        super.onResume()

        articlesList =
            getArticlesFromJson(requireContext().assets.open(getString(R.string.articlesFileName)))

        addArticlesToLayout(articlesList, motivationArticlesLayout)
    }

    /**
     * Заполняет макет статьями
     *
     * @param articlesList Список со статьям
     * @param rootLayout Макет для статей
     */
    private fun addArticlesToLayout(
        articlesList: MutableList<MutableList<String>>,
        rootLayout: LinearLayout
    ) {
        rootLayout.removeAllViews()
        articlesList.toList().forEach {
//            Log.d("DEB", createArticleViewByList(it)::class.simpleName.toString())
            rootLayout.removeView(createArticleViewByList(it))
            rootLayout.addView(createArticleViewByList(it))
        }
    }

    /**
     * Создаёт и отдаёт view статьи
     *
     * @param articleProperties Список свойств статьи
     * @return View статьи
     */
    private fun createArticleViewByList(articleProperties: MutableList<String>): View {
        val article = layoutInflater.inflate(
            resources.getLayout(R.layout.simple_motivation_article),
            null,
            true
        )
        val articlePreview = article.findViewById<ImageView>(R.id.simpleMotivationArticleImage)
        val articleTitle = article.findViewById<TextView>(R.id.simpleMotivationArticleTitle)

        article.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(articleProperties[0])))
        }
//        Log.d("DEB", resources.getIdentifier(articleProperties[1], "drawable", requireContext().packageName).toString())
        articlePreview.setImageDrawable(
            getDrawable(
                requireContext(),
                resources.getIdentifier(
                    articleProperties[1],
                    "drawable",
                    requireContext().packageName
                )
            )
        )
        articleTitle.text = articleProperties[2]

        return article
    }

    /**
     * Парсит список статей из JSON-файла
     *
     * @param open Поток ввода json
     * @return Список статей в формате [сылка_на_сайт, имя_файла_превью, заголовок_статьи]
     * @exception FileNotFoundException если введён поток с некорректным именем файла
     */
    private fun getArticlesFromJson(open: InputStream): MutableList<MutableList<String>> {
        val scan = Scanner(open)
        val stringBuilder = StringBuilder()

        while (scan.hasNext()) stringBuilder.append(scan.nextLine())
        scan.close()

        val jsonArray = JSONArray(stringBuilder.toString())

        return (0..5).map { array ->
            (0..2).map { line ->
                jsonArray.getJSONArray(array).getString(line)
            }.toMutableList()
        }.toMutableList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}