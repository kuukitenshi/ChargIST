package cmu.group2.chargist.ui.components.station

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmu.group2.chargist.R
import cmu.group2.chargist.data.model.SOptions
import cmu.group2.chargist.data.model.Station
import cmu.group2.chargist.ui.components.map.sort.StationSortIndicator
import coil.compose.AsyncImage

@Composable
fun StationCard(
    station: Station,
    onClick: () -> Unit,
    onNavigateToLocation: (Double, Double) -> Unit,
    isFavorite: Boolean = true,
    selectedOption: SOptions?,
    userLocation: Location?,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --------- img ---------
            AsyncImage(
                model = station.imageUrl,
                contentDescription = null,
                fallback = painterResource(R.drawable.default_station_image),
                error = painterResource(R.drawable.default_station_image),
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))

            // --------- body ---------
            Column(modifier = Modifier.weight(1f)) {
                //---------------name ----------------
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(4.dp))

                //-------------- dynamic indicator -------------------
                if (!isFavorite) {
                    StationSortIndicator(
                        station = station,
                        selectedOption = selectedOption,
                        userLocation = userLocation
                    )
                }
            }
            //--------- location here btn -------------
            val (lat, lon) = station.location
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .clickable { onNavigateToLocation(lat, lon) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.locate_here),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            //----------arrow --------------
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.see_station_details_desc),
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
