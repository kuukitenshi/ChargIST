package cmu.group2.chargist

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cmu.group2.chargist.data.database.AppDatabase
import cmu.group2.chargist.data.model.GeoLocation
import cmu.group2.chargist.services.FavoritesSyncService
import cmu.group2.chargist.services.LocationService
import cmu.group2.chargist.services.MapSyncService
import cmu.group2.chargist.services.SearchSyncService
import cmu.group2.chargist.services.SessionService
import cmu.group2.chargist.services.ViewSyncService
import cmu.group2.chargist.ui.screens.AddStationScreen
import cmu.group2.chargist.ui.screens.ChargerDetailsScreen
import cmu.group2.chargist.ui.screens.EditStationDetailsScreen
import cmu.group2.chargist.ui.screens.LoginScreen
import cmu.group2.chargist.ui.screens.MapsScreen
import cmu.group2.chargist.ui.screens.OpeningScreen
import cmu.group2.chargist.ui.screens.ProfileScreen
import cmu.group2.chargist.ui.screens.RegisterScreen
import cmu.group2.chargist.ui.screens.ReviewsScreen
import cmu.group2.chargist.ui.screens.StationDetailsScreen
import cmu.group2.chargist.ui.theme.ChargISTTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Configuration.getInstance()
            .load(this, application.getSharedPreferences("default", MODE_PRIVATE))
        AppDatabase.init(this)
        startServices()
        setContent {
            val limitedPerms = stringResource(R.string.limited_perms)
            val noPerms = stringResource(R.string.no_perms)
            val locationLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                    when {
                        permissions.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            false
                        ) -> {
                            startService(Intent(this, LocationService::class.java))
                        }

                        permissions.getOrDefault(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            false
                        ) -> {
                            startService(Intent(this, LocationService::class.java))
                            Toast.makeText(this, limitedPerms, Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            Toast.makeText(this, noPerms, Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            ChargISTTheme {
                AppNavigator(locationLauncher)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppDatabase.close()
    }

    private fun startServices() {
        startService(Intent(this, FavoritesSyncService::class.java))
        startService(Intent(this, SessionService::class.java))
        startService(Intent(this, ViewSyncService::class.java))
        startService(Intent(this, MapSyncService::class.java))
        startService(Intent(this, SearchSyncService::class.java))
    }
}

@Composable
fun AppNavigator(locationPermissions: ActivityResultLauncher<Array<String>>) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screens.Opening.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popExitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None }
    ) {
        composable(Screens.Opening.route) { OpeningScreen(navController) }
        composable(Screens.Login.route) { LoginScreen(navController) }
        composable(Screens.Register.route) { RegisterScreen(navController) }
        composable(Screens.Profile.route) { ProfileScreen(navController) }
        composable(Screens.AddStation.route) { AddStationScreen(navController) }

        // --------- specific station card ----------
        composable(
            route = Screens.StationDetails.route + "/{stationId}",
            arguments = listOf(navArgument("stationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val stationId = backStackEntry.arguments?.getLong("stationId") ?: -1
            StationDetailsScreen(stationId = stationId, navController = navController)
        }

        //---------------- specific charger card ------------
        composable(
            route = Screens.ChargerDetails.route + "/{stationId}/type={type}&power={power}",
            arguments = listOf(
                navArgument("stationId") { type = NavType.LongType },
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("power") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val stationId = backStackEntry.arguments?.getLong("stationId") ?: -1
            val type = backStackEntry.arguments?.getString("type") ?: ""
            val power = backStackEntry.arguments?.getString("power") ?: ""
            ChargerDetailsScreen(
                stationId = stationId,
                bundleType = type,
                bundlePower = power,
                navController = navController
            )
        }

        // ------------- review for a specific station ------------
        composable(
            route = Screens.Reviews.route + "/{stationId}",
            arguments = listOf(navArgument("stationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val stationId = backStackEntry.arguments?.getLong("stationId") ?: -1
            ReviewsScreen(stationId = stationId, navController = navController)
        }

        // ---------------- edit a specific station ----------------
        composable(
            route = Screens.EditStationDetails.route + "/{stationId}",
            arguments = listOf(navArgument("stationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val stationId = backStackEntry.arguments?.getLong("stationId") ?: -1
            EditStationDetailsScreen(stationId = stationId, navController = navController)
        }

        //-------- specific coords ----------------
        composable(
            route = Screens.Map.route + "?lat={lat}&lon={lon}",
            arguments = listOf(
                navArgument("lat") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("lon") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull()
            val location = lat?.let { lat -> lon?.let { lon -> GeoLocation(lat, lon) } }
            MapsScreen(navController, locationPermissions, location)
        }
    }
}
