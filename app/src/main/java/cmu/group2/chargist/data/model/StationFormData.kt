package cmu.group2.chargist.data.model

import android.net.Uri
import androidx.core.net.toUri
import cmu.group2.chargist.bundleChargers

data class StationFormData(
    var name: String,
    var location: GeoLocation?,
    var imageUri: Uri?,
    var imageBytes: ByteArray?,
    var bundles: List<ChargerBundle>,
    var paymentMethods: List<PaymentMethod>,
    var nearbyServices: List<NearbyService>
) {
    companion object {
        fun fromStation(station: Station): StationFormData {
            return StationFormData(
                name = station.name,
                location = station.location,
                imageUri = station.imageUrl?.toUri(),
                imageBytes = null,
                bundles = bundleChargers(station.chargers),
                paymentMethods = station.paymentMethods,
                nearbyServices = station.nearbyServices
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StationFormData

        if (name != other.name) return false
        if (location != other.location) return false
        if (imageUri != other.imageUri) return false
        if (!imageBytes.contentEquals(other.imageBytes)) return false
        if (bundles != other.bundles) return false
        if (paymentMethods != other.paymentMethods) return false
        if (nearbyServices != other.nearbyServices) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (location?.hashCode() ?: 0)
        result = 31 * result + (imageUri?.hashCode() ?: 0)
        result = 31 * result + (imageBytes?.contentHashCode() ?: 0)
        result = 31 * result + bundles.hashCode()
        result = 31 * result + paymentMethods.hashCode()
        result = 31 * result + nearbyServices.hashCode()
        return result
    }
}

val EmptyStationFormData = StationFormData(
    name = "",
    location = null,
    imageUri = null,
    imageBytes = null,
    bundles = emptyList(),
    paymentMethods = emptyList(),
    nearbyServices = emptyList()
)
