package com.example.engine

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

data class UnitCategory(
    val id: String,
    val name: String,
    val iconName: String,
    val units: List<ConversionUnit>
)

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val toBaseFactor: Double, // Multiplied with value to convert to base unit
    val customConvert: ((Double, Boolean) -> Double)? = null // For Temperature: (value, toBase) -> Double
)

object UnitConverter {

    val CATEGORIES = listOf(
        UnitCategory(
            id = "length",
            name = "Length",
            iconName = "straighten",
            units = listOf(
                ConversionUnit("m", "Meter", "m", 1.0),
                ConversionUnit("km", "Kilometer", "km", 1000.0),
                ConversionUnit("cm", "Centimeter", "cm", 0.01),
                ConversionUnit("mm", "Millimeter", "mm", 0.001),
                ConversionUnit("um", "Micrometer", "µm", 1e-6),
                ConversionUnit("nm", "Nanometer", "nm", 1e-9),
                ConversionUnit("mi", "Mile", "mi", 1609.344),
                ConversionUnit("yd", "Yard", "yd", 0.9144),
                ConversionUnit("ft", "Foot", "ft", 0.3048),
                ConversionUnit("in", "Inch", "in", 0.0254),
                ConversionUnit("nmi", "Nautical Mile", "nmi", 1852.0)
            )
        ),
        UnitCategory(
            id = "mass",
            name = "Mass & Weight",
            iconName = "scale",
            units = listOf(
                ConversionUnit("kg", "Kilogram", "kg", 1.0),
                ConversionUnit("g", "Gram", "g", 0.001),
                ConversionUnit("mg", "Milligram", "mg", 1e-6),
                ConversionUnit("ug", "Microgram", "µg", 1e-9),
                ConversionUnit("t", "Metric Ton", "t", 1000.0),
                ConversionUnit("lb", "Pound", "lb", 0.45359237),
                ConversionUnit("oz", "Ounce", "oz", 0.028349523125),
                ConversionUnit("st", "Stone", "st", 6.35029318),
                ConversionUnit("ct", "Carat", "ct", 0.0002)
            )
        ),
        UnitCategory(
            id = "temperature",
            name = "Temperature",
            iconName = "thermostat",
            units = listOf(
                ConversionUnit(
                    id = "C",
                    name = "Celsius",
                    symbol = "°C",
                    toBaseFactor = 1.0,
                    customConvert = { v, toBase -> if (toBase) v else v } // Base is Celsius
                ),
                ConversionUnit(
                    id = "F",
                    name = "Fahrenheit",
                    symbol = "°F",
                    toBaseFactor = 1.0,
                    customConvert = { v, toBase ->
                        if (toBase) (v - 32.0) * 5.0 / 9.0 else (v * 9.0 / 5.0) + 32.0
                    }
                ),
                ConversionUnit(
                    id = "K",
                    name = "Kelvin",
                    symbol = "K",
                    toBaseFactor = 1.0,
                    customConvert = { v, toBase ->
                        if (toBase) v - 273.15 else v + 273.15
                    }
                ),
                ConversionUnit(
                    id = "R",
                    name = "Rankine",
                    symbol = "°R",
                    toBaseFactor = 1.0,
                    customConvert = { v, toBase ->
                        if (toBase) (v - 491.67) * 5.0 / 9.0 else (v + 273.15) * 9.0 / 5.0
                    }
                )
            )
        ),
        UnitCategory(
            id = "speed",
            name = "Speed",
            iconName = "speed",
            units = listOf(
                ConversionUnit("mps", "Meters per second", "m/s", 1.0),
                ConversionUnit("kmh", "Kilometers per hour", "km/h", 1.0 / 3.6),
                ConversionUnit("mph", "Miles per hour", "mph", 0.44704),
                ConversionUnit("knot", "Knot", "kn", 0.514444),
                ConversionUnit("fps", "Feet per second", "ft/s", 0.3048),
                ConversionUnit("mach", "Mach (at sea level)", "Mach", 340.29)
            )
        ),
        UnitCategory(
            id = "area",
            name = "Area",
            iconName = "aspect_ratio",
            units = listOf(
                ConversionUnit("sqm", "Square Meter", "m²", 1.0),
                ConversionUnit("sqkm", "Square Kilometer", "km²", 1e6),
                ConversionUnit("ha", "Hectare", "ha", 10000.0),
                ConversionUnit("ac", "Acre", "ac", 4046.8564224),
                ConversionUnit("sqft", "Square Foot", "ft²", 0.09290304),
                ConversionUnit("sqyd", "Square Yard", "yd²", 0.83612736),
                ConversionUnit("sqmi", "Square Mile", "mi²", 2589988.11),
                ConversionUnit("sqin", "Square Inch", "in²", 0.00064516)
            )
        ),
        UnitCategory(
            id = "volume",
            name = "Volume",
            iconName = "water_drop",
            units = listOf(
                ConversionUnit("L", "Liter", "L", 0.001),
                ConversionUnit("mL", "Milliliter", "mL", 1e-6),
                ConversionUnit("cum", "Cubic Meter", "m³", 1.0),
                ConversionUnit("gal", "Gallon (US)", "gal", 0.00378541),
                ConversionUnit("qt", "Quart (US)", "qt", 0.000946353),
                ConversionUnit("pt", "Pint (US)", "pt", 0.000473176),
                ConversionUnit("floz", "Fluid Ounce (US)", "fl oz", 2.95735e-5),
                ConversionUnit("cup", "Cup (US)", "cup", 0.000236588),
                ConversionUnit("tbsp", "Tablespoon (US)", "tbsp", 1.47868e-5),
                ConversionUnit("tsp", "Teaspoon (US)", "tsp", 4.92892e-6),
                ConversionUnit("cuft", "Cubic Foot", "ft³", 0.0283168),
                ConversionUnit("cuin", "Cubic Inch", "in³", 1.6387e-5)
            )
        ),
        UnitCategory(
            id = "time",
            name = "Time",
            iconName = "schedule",
            units = listOf(
                ConversionUnit("s", "Second", "s", 1.0),
                ConversionUnit("ms", "Millisecond", "ms", 0.001),
                ConversionUnit("us", "Microsecond", "µs", 1e-6),
                ConversionUnit("ns", "Nanosecond", "ns", 1e-9),
                ConversionUnit("min", "Minute", "min", 60.0),
                ConversionUnit("hr", "Hour", "hr", 3600.0),
                ConversionUnit("day", "Day", "d", 86400.0),
                ConversionUnit("wk", "Week", "wk", 604800.0),
                ConversionUnit("mo", "Month (Avg 30.44d)", "mo", 2629800.0),
                ConversionUnit("yr", "Year (365.25d)", "yr", 31557600.0)
            )
        ),
        UnitCategory(
            id = "storage",
            name = "Digital Storage",
            iconName = "storage",
            units = listOf(
                ConversionUnit("B", "Byte", "B", 1.0),
                ConversionUnit("KB", "Kilobyte (KB)", "KB", 1024.0),
                ConversionUnit("MB", "Megabyte (MB)", "MB", 1024.0 * 1024),
                ConversionUnit("GB", "Gigabyte (GB)", "GB", 1024.0 * 1024 * 1024),
                ConversionUnit("TB", "Terabyte (TB)", "TB", 1024.0 * 1024 * 1024 * 1024),
                ConversionUnit("PB", "Petabyte (PB)", "PB", 1024.0 * 1024 * 1024 * 1024 * 1024),
                ConversionUnit("b", "Bit", "b", 0.125),
                ConversionUnit("Kb", "Kilobit", "Kb", 128.0),
                ConversionUnit("Mb", "Megabit", "Mb", 131072.0),
                ConversionUnit("Gb", "Gigabit", "Gb", 134217728.0)
            )
        ),
        UnitCategory(
            id = "energy",
            name = "Energy & Work",
            iconName = "bolt",
            units = listOf(
                ConversionUnit("J", "Joule", "J", 1.0),
                ConversionUnit("kJ", "Kilojoule", "kJ", 1000.0),
                ConversionUnit("cal", "Calorie (therm)", "cal", 4.184),
                ConversionUnit("kcal", "Kilocalorie (food)", "kcal", 4184.0),
                ConversionUnit("Wh", "Watt-hour", "Wh", 3600.0),
                ConversionUnit("kWh", "Kilowatt-hour", "kWh", 3.6e6),
                ConversionUnit("eV", "Electronvolt", "eV", 1.602176634e-19),
                ConversionUnit("btu", "British Thermal Unit", "BTU", 1055.06),
                ConversionUnit("ftlb", "Foot-pound", "ft·lb", 1.355818)
            )
        ),
        UnitCategory(
            id = "pressure",
            name = "Pressure",
            iconName = "compress",
            units = listOf(
                ConversionUnit("Pa", "Pascal", "Pa", 1.0),
                ConversionUnit("kPa", "Kilopascal", "kPa", 1000.0),
                ConversionUnit("bar", "Bar", "bar", 100000.0),
                ConversionUnit("psi", "Pounds per sq inch", "psi", 6894.757),
                ConversionUnit("atm", "Standard Atmosphere", "atm", 101325.0),
                ConversionUnit("mmHg", "Millimeter of Mercury", "mmHg", 133.322),
                ConversionUnit("Torr", "Torr", "Torr", 133.322)
            )
        ),
        UnitCategory(
            id = "power",
            name = "Power",
            iconName = "electric_bolt",
            units = listOf(
                ConversionUnit("W", "Watt", "W", 1.0),
                ConversionUnit("kW", "Kilowatt", "kW", 1000.0),
                ConversionUnit("MW", "Megawatt", "MW", 1e6),
                ConversionUnit("hp", "Horsepower (mechanical)", "hp", 745.69987),
                ConversionUnit("btuh", "BTU per hour", "BTU/h", 0.293071)
            )
        )
    )

    fun convert(value: Double, fromUnit: ConversionUnit, toUnit: ConversionUnit): Double {
        if (fromUnit.id == toUnit.id) return value

        // Custom conversion (e.g. Temperature)
        if (fromUnit.customConvert != null && toUnit.customConvert != null) {
            val baseVal = fromUnit.customConvert.invoke(value, true)
            return toUnit.customConvert.invoke(baseVal, false)
        }

        // Standard linear conversion: Value * FromFactor / ToFactor
        val baseVal = value * fromUnit.toBaseFactor
        return baseVal / toUnit.toBaseFactor
    }

    fun formatConvertedValue(value: Double): String {
        if (value.isNaN()) return "0"
        val absVal = abs(value)
        if (absVal != 0.0 && (absVal >= 1e10 || absVal < 1e-5)) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            return df.format(value).replace("E", " × 10^")
        }
        val df = DecimalFormat("0.########", DecimalFormatSymbols(Locale.US))
        val formatted = df.format(value)
        return if (formatted == "-0") "0" else formatted
    }
}
