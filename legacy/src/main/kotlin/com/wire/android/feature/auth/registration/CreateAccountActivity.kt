package com.wire.android.feature.auth.registration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wire.android.R
import kotlinx.android.synthetic.main.activity_create_account.*

class CreateAccountActivity : AppCompatActivity(R.layout.activity_create_account) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBackButton()
    }

    private fun initBackButton() {
        createAccountBackButton.setOnClickListener { onBackPressed() }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, CreateAccountActivity::class.java)
    }
}