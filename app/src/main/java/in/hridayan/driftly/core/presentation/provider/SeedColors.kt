package `in`.hridayan.driftly.core.presentation.provider

data class SeedColor(
    val primary: Int,
    val secondary: Int,
    val tertiary: Int
)

sealed class AppSeedColors(val colors: SeedColor) {

    // Green Shades
    data object GreenLight : AppSeedColors(
        SeedColor(
            primary = 0xFF7D9B36.toInt(),
            secondary = 0xFF8C9476.toInt(),
            tertiary = 0xFF6A9A92.toInt()
        )
    )

    data object GreenMedium : AppSeedColors(
        SeedColor(
            primary = 0xFF5BA053.toInt(),
            secondary = 0xFF84967E.toInt(),
            tertiary = 0xFF69999D.toInt()
        )
    )

    data object GreenDark : AppSeedColors(
        SeedColor(
            primary = 0xFF30A370.toInt(),
            secondary = 0xFF7E9686.toInt(),
            tertiary = 0xFF6D97A6.toInt()
        )
    )

    // Sky Blue Shades
    data object SkyBlueLight : AppSeedColors(
        SeedColor(
            primary = 0xFF169EB7.toInt(),
            secondary = 0xFF7F949B.toInt(),
            tertiary = 0xFF898FB0.toInt()
        )
    )

    data object SkyBlueMedium : AppSeedColors(
        SeedColor(
            primary = 0xFF389AC7.toInt(),
            secondary = 0xFF81939F.toInt(),
            tertiary = 0xFF938CAF.toInt()
        )
    )

    // Blue Shades
    data object BlueLight : AppSeedColors(
        SeedColor(
            primary = 0xFF5695D2.toInt(),
            secondary = 0xFF8692A2.toInt(),
            tertiary = 0xFF9D8AAB.toInt()
        )
    )

    data object BlueMedium : AppSeedColors(
        SeedColor(
            primary = 0xFF728FD8.toInt(),
            secondary = 0xFF8B90A3.toInt(),
            tertiary = 0xFFA687A4.toInt()
        )
    )

    // Navy Blue Shades
    data object NavyBlueLight : AppSeedColors(
        SeedColor(
            primary = 0xFF1E3A8A.toInt(),
            secondary = 0xFF6B7280.toInt(),
            tertiary = 0xFF7C3AED.toInt()
        )
    )

    data object NavyBlueMedium : AppSeedColors(
        SeedColor(
            primary = 0xFF1E40AF.toInt(),
            secondary = 0xFF6B7A99.toInt(),
            tertiary = 0xFF8B5CF6.toInt()
        )
    )

    data object NavyBlueDark : AppSeedColors(
        SeedColor(
            primary = 0xFF172554.toInt(),
            secondary = 0xFF4B5563.toInt(),
            tertiary = 0xFF6366F1.toInt()
        )
    )
}
