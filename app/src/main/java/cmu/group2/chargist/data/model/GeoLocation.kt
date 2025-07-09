package cmu.group2.chargist.data.model

data class GeoLocation(
    val latitude: Double = 38.736691,
    val longitude: Double = -9.138769
) {
    override fun toString(): String {
        return "%f; %f".format(latitude, longitude)
    }
}