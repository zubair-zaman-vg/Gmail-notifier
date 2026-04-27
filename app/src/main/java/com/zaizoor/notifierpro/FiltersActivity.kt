package com.zaizoor.notifierpro

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class FiltersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)

        val etSubject = findViewById<TextInputEditText>(R.id.etSubjectKeywords)
        val etBody = findViewById<TextInputEditText>(R.id.etBodyKeywords)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener { finish() }

        // Load existing keywords
        val subjects = KeywordManager.getSubjectKeywords(this).joinToString(", ")
        val body = KeywordManager.getBodyKeywords(this).joinToString(", ")

        etSubject.setText(subjects)
        etBody.setText(body)

        findViewById<MaterialButton>(R.id.btnSaveFilters).setOnClickListener {
            val newSubjects = etSubject.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            val newBody = etBody.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

            if (newSubjects.isEmpty() || newBody.isEmpty()) {
                Toast.makeText(this, "Keywords cannot be empty!", Toast.LENGTH_SHORT).show()
            } else {
                KeywordManager.saveSubjectKeywords(this, newSubjects)
                KeywordManager.saveBodyKeywords(this, newBody)
                Toast.makeText(this, "Royal Filters Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
