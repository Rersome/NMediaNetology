package ru.netology.nmedia.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.ActivityAppBinding
import ru.netology.nmedia.viewmodel.AuthViewModel

class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text.isNullOrBlank()) {
                Snackbar.make(
                    binding.root,
                    R.string.error_empty_content,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(getString(R.string.OK)) {
                        finish()
                    }.show()
                return@let
            }
            findNavController(R.id.navHost).navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply { textArg = text }
            )
        }

        val viewModel by viewModels<AuthViewModel>()


        addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.auth_menu, menu)

                    viewModel.data.observe(this@AppActivity) {
                        menu.setGroupVisible(R.id.unauthorized, !viewModel.isAuthorized)
                        menu.setGroupVisible(R.id.authorized, viewModel.isAuthorized)
                    }
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.signIn -> {
                            findNavController(R.id.navHost).navigate(
                                R.id.action_feedFragment_to_signInFragment
                            )
                            true
                        }
                        R.id.signUp -> {
                            findNavController(R.id.navHost).navigate(
                                R.id.action_feedFragment_to_signUpFragment
                            )
                            true
                        }
                        R.id.logout -> {
                            if (findNavController(R.id.navHost).currentDestination?.id == R.id.newPostFragment) {
                                MaterialAlertDialogBuilder(this@AppActivity)
                                    .setTitle("Предупреждение")
                                    .setMessage("Вы уверены, что хотите выйти из профиля?")
                                    .setPositiveButton("OK") { _, _ ->
                                        findNavController(R.id.navHost).navigateUp()
                                        AppAuth.getInstance().clear()
                                    }
                                    .setNegativeButton("Отмена") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .show()
                                true
                            } else {
                                AppAuth.getInstance().clear()
                                true
                            }
                        }
                        else -> false
                    }
            }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }
}