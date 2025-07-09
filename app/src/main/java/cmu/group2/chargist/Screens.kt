package cmu.group2.chargist

sealed class Screens(val route: String) {
    data object Opening : Screens("opening")
    data object Login : Screens("login")
    data object Register : Screens("register")
    data object Map : Screens("map")
    data object Profile : Screens("profile")
    data object AddStation : Screens("add_station")
    data object StationDetails : Screens("station_details")
    data object Reviews : Screens("reviews")
    data object ChargerDetails : Screens("charger_details")
    data object EditStationDetails : Screens("edit_station_details")
}