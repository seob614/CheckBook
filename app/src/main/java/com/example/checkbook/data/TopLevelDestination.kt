package com.example.checkbook.data

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.checkbook.R
import com.example.checkbook.icon.BnjIcons
import com.example.checkbook.ui.navigation.CheckRoute
import com.example.checkbook.ui.navigation.ExchangeRoute
import com.example.checkbook.ui.navigation.SearchRoute
import com.example.checkbook.ui.navigation.UserRoute

/**
 * Type for the top level destinations in the application. Contains metadata about the destination
 * that is used in the top app bar and common navigation UI.
 *
 * @param selectedIcon The icon to be displayed in the navigation UI when this destination is
 * selected.
 * @param unselectedIcon The icon to be displayed in the navigation UI when this destination is
 * not selected.
 * @param iconTextId Text that to be displayed in the navigation UI.
 * @param titleTextId Text that is displayed on the top app bar.
 * @param route The route to use when navigating to this destination.
 * @param baseRoute The highest ancestor of this destination. Defaults to [route], meaning that
 * there is a single destination in that section of the app (no nested destinations).
 */

data class TopLevelRoute<T : Any>(
    val selectedIcon: Int,
    val unselectedIcon: Int,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
    val route: T,
)


val TOP_LEVEL_DESTINATIONS= listOf(
    TopLevelRoute(
        selectedIcon = BnjIcons.SearchSelected,
        unselectedIcon = BnjIcons.SearchUnSelected,
        iconTextId = R.string.feature_search_title,
        titleTextId = R.string.feature_search_title,
        route = SearchRoute,
    ),
    TopLevelRoute(
        selectedIcon = BnjIcons.CheckSelected,
        unselectedIcon = BnjIcons.CheckUnSelected,
        iconTextId = R.string.feature_check_title,
        titleTextId = R.string.feature_check_title,
        route = CheckRoute,
    ),
    /*
    TopLevelRoute(
        selectedIcon = BnjIcons.ExchangeSelected,
        unselectedIcon = BnjIcons.ExchangeUnSelected,
        iconTextId = R.string.feature_exchange_title,
        titleTextId = R.string.feature_exchange_title,
        route = ExchangeRoute,
    ),

     */
    TopLevelRoute(
        selectedIcon = BnjIcons.UserSelected,
        unselectedIcon = BnjIcons.UserUnSelected,
        iconTextId = R.string.feature_user_title,
        titleTextId = R.string.feature_user_title,
        route = UserRoute,
    ),
)