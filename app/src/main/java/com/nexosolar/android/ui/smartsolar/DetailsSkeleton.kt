package com.nexosolar.android.ui.smartsolar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nexosolar.android.R
import com.nexosolar.android.ui.common.shimmerEffect

/**
 * **Skeleton de carga para la pantalla de Detalles**
 *
 * Muestra 5 bloques shimmer (uno por cada campo de la instalación).
 */
@Composable
fun DetailsSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(dimensionResource(id = R.dimen.detail_content_padding))
    ) {
        repeat(5) {
            SkeletonDetailItem()
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.detail_section_margin_top)))
        }
    }
}

/**
 * **Item individual del skeleton**
 *
 * Label pequeño + Value más grande + Divider.
 */
@Composable
private fun SkeletonDetailItem() {
    Column(
        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.detail_skeleton_padding_bottom))
    ) {
        // Label skeleton
        Box(
            modifier = Modifier
                .width(dimensionResource(id = R.dimen.detail_skeleton_title_width))
                .height(dimensionResource(id = R.dimen.detail_skeleton_title_height))
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.detail_skeleton_value_margin_top)))

        // Value skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.detail_skeleton_value_height))
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.detail_skeleton_divider_margin_top)))

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.detail_skeleton_divider_height))
                .background(colorResource(id = R.color.detail_skeleton_divider_color))
        )
    }
}

@Preview(showBackground = true, name = "Details Skeleton")
@Composable
private fun DetailsSkeletonPreview() {
    DetailsSkeleton()
}
