package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PhotoAlbum
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.MediaItem
import com.example.ui.GalleryViewModel

sealed class Screen(val route: String, val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    object Library : Screen("library", "Library", Icons.Filled.GridView, Icons.Outlined.GridView)
    object Albums : Screen("albums", "Albums", Icons.Filled.PhotoAlbum, Icons.Outlined.PhotoAlbum)
    object Search : Screen("search", "Search", Icons.Filled.Search, Icons.Outlined.Search)
}

@Composable
fun MainScreen(viewModel: GalleryViewModel) {
    val navController = rememberNavController()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val isBottomBarVisible = currentRoute in listOf(Screen.Library.route, Screen.Albums.route, Screen.Search.route)

    Scaffold(
        bottomBar = {
            AnimatedVisibility(visible = isBottomBarVisible, enter = fadeIn(), exit = fadeOut()) {
                NavigationBar {
                    val screens = listOf(Screen.Library, Screen.Albums, Screen.Search)
                    screens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = { Icon(if (selected) screen.activeIcon else screen.inactiveIcon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavigationHost(navController = navController, viewModel = viewModel)
        }
    }
}

@Composable
fun NavigationHost(navController: NavHostController, viewModel: GalleryViewModel) {
    NavHost(navController, startDestination = Screen.Library.route) {
        composable(Screen.Library.route) {
            LibraryScreen(viewModel = viewModel, onPhotoSelected = { photoId ->
                navController.navigate("photo_detail/$photoId")
            })
        }
        composable(Screen.Albums.route) {
            AlbumsScreen()
        }
        composable(Screen.Search.route) {
            SearchScreen()
        }
        composable("photo_detail/{photoId}") { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId")?.toLongOrNull()
            if (photoId != null) {
                PhotoDetailScreen(photoId = photoId, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
