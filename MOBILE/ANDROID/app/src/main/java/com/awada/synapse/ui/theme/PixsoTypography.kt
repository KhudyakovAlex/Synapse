package com.awada.synapse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.awada.synapse.R

/**
 * Auto-generated from Pixso design tokens
 * Typography styles using available Pixso tokens
 */

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val ibmPlexSansFont = GoogleFont("IBM Plex Sans")

val IBMPlexSansFamily = FontFamily(
    Font(googleFont = ibmPlexSansFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = ibmPlexSansFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = ibmPlexSansFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
)

// Display styles
val DisplayLarge = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = PixsoDimens.Display_Display_L_Size,
    lineHeight = PixsoDimens.Display_Display_L_Line_Height,
    letterSpacing = 0.sp
)

val DisplayMedium = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = PixsoDimens.Display_Display_M_Size,
    lineHeight = PixsoDimens.Display_Display_M_Line_Height,
    letterSpacing = 0.sp
)

val DisplaySmall = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = PixsoDimens.Display_Display_S_Size,
    lineHeight = PixsoDimens.Display_Display_S_Line_Height,
    letterSpacing = 0.sp
)

// Headline styles
val HeadlineLarge = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = PixsoDimens.Headline_Headline_L_Size,
    lineHeight = PixsoDimens.Headline_Headline_L_Line_Height,
    letterSpacing = 0.sp
)

val HeadlineMedium = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = PixsoDimens.Headline_Headline_M_Size,
    lineHeight = PixsoDimens.Headline_Headline_M_Line_Height,
    letterSpacing = 0.sp
)

val HeadlineSmall = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = PixsoDimens.Headline_Headline_S_Size,
    lineHeight = PixsoDimens.Headline_Headline_S_Line_Height,
    letterSpacing = 0.sp
)

// Title styles (using Headline XS as fallback)
val TitleLarge = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = PixsoDimens.Headline_Headline_XS_Size,
    lineHeight = PixsoDimens.Headline_Headline_XS_Line_Height,
    letterSpacing = 0.sp
)

val TitleMedium = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

val TitleSmall = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp
)

// Body styles
val BodyLarge = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = PixsoDimens.Body_Body_L_Size,
    lineHeight = PixsoDimens.Body_Body_L_Line_Height,
    letterSpacing = 0.sp
)

val BodyMedium = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = PixsoDimens.Body_Body_M_Size,
    lineHeight = PixsoDimens.Body_Body_M_Line_Height,
    letterSpacing = 0.sp
)

val BodySmall = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = PixsoDimens.Body_Body_S_Size,
    lineHeight = PixsoDimens.Body_Body_S_Line_Height,
    letterSpacing = 0.sp
)

// Label styles
val LabelLarge = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = PixsoDimens.Label_Label_L_Size,
    lineHeight = PixsoDimens.Label_Label_L_Line_Height,
    letterSpacing = 0.sp
)

val LabelMedium = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = PixsoDimens.Label_Label_M_Size,
    lineHeight = PixsoDimens.Label_Label_M_Line_Height,
    letterSpacing = 0.sp
)

val LabelSmall = TextStyle(
    fontFamily = IBMPlexSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = PixsoDimens.Label_Label_S_Size,
    lineHeight = PixsoDimens.Label_Label_S_Line_Height,
    letterSpacing = 0.sp
)

// Material3 Typography with Pixso tokens
val PixsoTypography = Typography(
    displayLarge = DisplayLarge,
    displayMedium = DisplayMedium,
    displaySmall = DisplaySmall,
    headlineLarge = HeadlineLarge,
    headlineMedium = HeadlineMedium,
    headlineSmall = HeadlineSmall,
    titleLarge = TitleLarge,
    titleMedium = TitleMedium,
    titleSmall = TitleSmall,
    bodyLarge = BodyLarge,
    bodyMedium = BodyMedium,
    bodySmall = BodySmall,
    labelLarge = LabelLarge,
    labelMedium = LabelMedium,
    labelSmall = LabelSmall
)
