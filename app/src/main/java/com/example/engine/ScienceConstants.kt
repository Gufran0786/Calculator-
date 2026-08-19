package com.example.engine

data class ScienceConstant(
    val name: String,
    val symbol: String,
    val value: Double,
    val valueStr: String,
    val unit: String,
    val category: String,
    val description: String
)

data class MathScienceSymbol(
    val symbol: String,
    val name: String,
    val category: String,
    val description: String,
    val example: String,
    val insertableAs: String = symbol
)

object ScienceConstants {

    val CONSTANTS = listOf(
        // Physics & Universal
        ScienceConstant(
            name = "Speed of Light in Vacuum",
            symbol = "c",
            value = 299792458.0,
            valueStr = "2.99792458 × 10⁸",
            unit = "m/s",
            category = "Universal",
            description = "Exact speed of light in vacuum (relativistic speed limit)"
        ),
        ScienceConstant(
            name = "Planck Constant",
            symbol = "h",
            value = 6.62607015e-34,
            valueStr = "6.62607015 × 10⁻³⁴",
            unit = "J·s",
            category = "Quantum",
            description = "Fundamental quantum of action relating photon energy to frequency"
        ),
        ScienceConstant(
            name = "Reduced Planck Constant (Dirac)",
            symbol = "ħ",
            value = 1.054571817e-34,
            valueStr = "1.054571817 × 10⁻³⁴",
            unit = "J·s",
            category = "Quantum",
            description = "Planck constant divided by 2π"
        ),
        ScienceConstant(
            name = "Gravitational Constant",
            symbol = "G",
            value = 6.67430e-11,
            valueStr = "6.67430 × 10⁻¹¹",
            unit = "m³/(kg·s²)",
            category = "Astrophysics",
            description = "Newtonian constant of gravitation"
        ),
        ScienceConstant(
            name = "Elementary Charge",
            symbol = "e",
            value = 1.602176634e-19,
            valueStr = "1.602176634 × 10⁻¹⁹",
            unit = "C",
            category = "Electromagnetism",
            description = "Magnitude of electrical charge carried by a single proton or electron"
        ),
        ScienceConstant(
            name = "Standard Gravitational Acceleration",
            symbol = "g",
            value = 9.80665,
            valueStr = "9.80665",
            unit = "m/s²",
            category = "Mechanics",
            description = "Standard nominal acceleration due to Earth's gravity"
        ),
        ScienceConstant(
            name = "Boltzmann Constant",
            symbol = "k_B",
            value = 1.380649e-23,
            valueStr = "1.380649 × 10⁻²³",
            unit = "J/K",
            category = "Thermodynamics",
            description = "Relates temperature to kinetic energy of particles"
        ),
        ScienceConstant(
            name = "Avogadro Number",
            symbol = "N_A",
            value = 6.02214076e23,
            valueStr = "6.02214076 × 10²³",
            unit = "mol⁻¹",
            category = "Chemistry",
            description = "Number of constituent particles per mole of substance"
        ),
        ScienceConstant(
            name = "Universal Gas Constant",
            symbol = "R",
            value = 8.314462618,
            valueStr = "8.314462618",
            unit = "J/(mol·K)",
            category = "Thermodynamics",
            description = "Molar gas constant in ideal gas law PV = nRT"
        ),
        ScienceConstant(
            name = "Stefan-Boltzmann Constant",
            symbol = "σ",
            value = 5.670374419e-8,
            valueStr = "5.670374419 × 10⁻⁸",
            unit = "W/(m²·K⁴)",
            category = "Thermodynamics",
            description = "Black body radiant emittance proportionality constant"
        ),
        ScienceConstant(
            name = "Electron Rest Mass",
            symbol = "m_e",
            value = 9.1093837015e-31,
            valueStr = "9.1093837015 × 10⁻³¹",
            unit = "kg",
            category = "Atomic",
            description = "Stationary rest mass of an electron (0.511 MeV/c²)"
        ),
        ScienceConstant(
            name = "Proton Rest Mass",
            symbol = "m_p",
            value = 1.67262192369e-27,
            valueStr = "1.67262192369 × 10⁻²⁷",
            unit = "kg",
            category = "Atomic",
            description = "Rest mass of an isolated proton"
        ),
        ScienceConstant(
            name = "Neutron Rest Mass",
            symbol = "m_n",
            value = 1.67492749804e-27,
            valueStr = "1.67492749804 × 10⁻²⁷",
            unit = "kg",
            category = "Atomic",
            description = "Rest mass of an isolated neutron"
        ),
        ScienceConstant(
            name = "Vacuum Electric Permittivity",
            symbol = "ε₀",
            value = 8.8541878128e-12,
            valueStr = "8.8541878128 × 10⁻¹²",
            unit = "F/m",
            category = "Electromagnetism",
            description = "Permittivity of free space / dielectric constant"
        ),
        ScienceConstant(
            name = "Vacuum Magnetic Permeability",
            symbol = "μ₀",
            value = 1.25663706212e-6,
            valueStr = "1.25663706212 × 10⁻⁶",
            unit = "N/A²",
            category = "Electromagnetism",
            description = "Magnetic constant of free space"
        ),
        ScienceConstant(
            name = "Coulomb's Constant",
            symbol = "k_e",
            value = 8.9875517923e9,
            valueStr = "8.9875517923 × 10⁹",
            unit = "N·m²/C²",
            category = "Electromagnetism",
            description = "Electrostatic force constant 1 / (4πε₀)"
        ),
        ScienceConstant(
            name = "Rydberg Constant",
            symbol = "R_∞",
            value = 10973731.568160,
            valueStr = "1.0973731568 × 10⁷",
            unit = "m⁻¹",
            category = "Atomic",
            description = "Physical constant relating to atomic spectra transitions"
        ),
        ScienceConstant(
            name = "Faraday Constant",
            symbol = "F",
            value = 96485.33212,
            valueStr = "96485.33212",
            unit = "C/mol",
            category = "Chemistry",
            description = "Magnitude of electric charge per mole of electrons"
        ),
        ScienceConstant(
            name = "Standard Atmospheric Pressure",
            symbol = "P_atm",
            value = 101325.0,
            valueStr = "101325",
            unit = "Pa",
            category = "Universal",
            description = "Nominal pressure equivalent to 1 atmosphere (760 mmHg)"
        ),
        // Mathematics
        ScienceConstant(
            name = "Pi (Archimedes Constant)",
            symbol = "π",
            value = Math.PI,
            valueStr = "3.141592653589793",
            unit = "dimensionless",
            category = "Math",
            description = "Ratio of a circle's circumference to its diameter"
        ),
        ScienceConstant(
            name = "Euler's Number (e)",
            symbol = "e",
            value = Math.E,
            valueStr = "2.718281828459045",
            unit = "dimensionless",
            category = "Math",
            description = "Base of the natural logarithm ln(x)"
        ),
        ScienceConstant(
            name = "Golden Ratio",
            symbol = "φ",
            value = 1.618033988749895,
            valueStr = "1.618033988749895",
            unit = "dimensionless",
            category = "Math",
            description = "Divine proportion (1 + √5) / 2"
        ),
        ScienceConstant(
            name = "Euler-Mascheroni Constant",
            symbol = "γ",
            value = 0.5772156649015329,
            valueStr = "0.577215664901533",
            unit = "dimensionless",
            category = "Math",
            description = "Limiting difference between harmonic series and natural log"
        ),
        ScienceConstant(
            name = "Tau (2π)",
            symbol = "τ",
            value = 2 * Math.PI,
            valueStr = "6.283185307179586",
            unit = "dimensionless",
            category = "Math",
            description = "Ratio of circumference to radius"
        ),
        ScienceConstant(
            name = "Square Root of 2 (Pythagoras)",
            symbol = "√2",
            value = 1.4142135623730951,
            valueStr = "1.414213562373095",
            unit = "dimensionless",
            category = "Math",
            description = "Diagonal of a unit square"
        ),
        ScienceConstant(
            name = "Square Root of 3 (Theodorus)",
            symbol = "√3",
            value = 1.7320508075688772,
            valueStr = "1.732050807568877",
            unit = "dimensionless",
            category = "Math",
            description = "Length of the diagonal of a unit cube"
        )
    )

    val SYMBOLS = listOf(
        // Operators & Calculus
        MathScienceSymbol("∫", "Integral", "Calculus", "Calculates continuous area or accumulation", "∫ f(x) dx", "∫"),
        MathScienceSymbol("∬", "Double Integral", "Calculus", "2D surface integration", "∬ f(x,y) dA", "∬"),
        MathScienceSymbol("∭", "Triple Integral", "Calculus", "3D volume integration", "∭ f(x,y,z) dV", "∭"),
        MathScienceSymbol("∮", "Contour Integral", "Calculus", "Integral along a closed curve", "∮ E · dl = 0", "∮"),
        MathScienceSymbol("∂", "Partial Derivative", "Calculus", "Derivative with respect to one variable", "∂f / ∂x", "∂"),
        MathScienceSymbol("∇", "Del / Nabla", "Vector Calculus", "Gradient, divergence, or curl vector differential operator", "∇ × B", "∇"),
        MathScienceSymbol("Δ", "Delta / Change", "Calculus & Science", "Finite difference or increment", "Δx = x₂ - x₁", "Δ"),
        MathScienceSymbol("∑", "Summation (Sigma)", "Algebra", "Sum of sequence of numbers", "∑ i² from 1 to n", "∑"),
        MathScienceSymbol("∏", "Product (Pi)", "Algebra", "Product of sequence of terms", "∏ (1 + 1/n)", "∏"),
        MathScienceSymbol("√", "Square Root", "Algebra", "Non-negative radical of order 2", "√(x² + y²)", "sqrt("),
        MathScienceSymbol("∛", "Cube Root", "Algebra", "Radical of order 3", "∛(27) = 3", "cbrt("),
        MathScienceSymbol("∜", "Fourth Root", "Algebra", "Radical of order 4", "∜(16) = 2", "∜"),
        MathScienceSymbol("lim", "Limit", "Calculus", "Value that a function approaches", "lim(x→0) sin(x)/x", "lim"),
        MathScienceSymbol("∞", "Infinity", "Math", "Unbounded quantity or limit", "x → ∞", "∞"),
        MathScienceSymbol("!", "Factorial", "Combinatorics", "Product of integers from 1 to n", "5! = 120", "!"),

        // Relations & Logic
        MathScienceSymbol("≈", "Approximately Equal", "Relations", "Approximation or near equality", "π ≈ 3.1416", "≈"),
        MathScienceSymbol("≠", "Not Equal", "Relations", "Inequality", "x ≠ 0", "≠"),
        MathScienceSymbol("≤", "Less Than or Equal", "Relations", "Non-strict inequality", "x ≤ 10", "<="),
        MathScienceSymbol("≥", "Greater Than or Equal", "Relations", "Non-strict inequality", "x ≥ 0", ">="),
        MathScienceSymbol("±", "Plus-Minus", "Arithmetic", "Represents both addition and subtraction (tolerance/roots)", "x = ±4", "±"),
        MathScienceSymbol("∝", "Proportional To", "Relations", "Direct variation between quantities", "F ∝ a", "∝"),
        MathScienceSymbol("≡", "Identical / Congruent", "Relations", "Equivalence or modular congruence", "a ≡ b (mod m)", "≡"),
        MathScienceSymbol("∈", "Element Of", "Set Theory", "Belongs to a set", "x ∈ ℝ", "∈"),
        MathScienceSymbol("∉", "Not Element Of", "Set Theory", "Does not belong to a set", "x ∉ ∅", "∉"),
        MathScienceSymbol("⊂", "Proper Subset", "Set Theory", "Contained strictly within a set", "A ⊂ B", "⊂"),
        MathScienceSymbol("⊆", "Subset", "Set Theory", "Subset or equal", "A ⊆ B", "⊆"),
        MathScienceSymbol("∩", "Intersection", "Set Theory", "Common elements between sets", "A ∩ B", "∩"),
        MathScienceSymbol("∪", "Union", "Set Theory", "Combined elements of sets", "A ∪ B", "∪"),
        MathScienceSymbol("∅", "Empty Set", "Set Theory", "Set with no elements", "A ∩ B = ∅", "∅"),
        MathScienceSymbol("∀", "For All", "Logic", "Universal quantifier", "∀ x > 0", "∀"),
        MathScienceSymbol("∃", "There Exists", "Logic", "Existential quantifier", "∃ x : f(x) = 0", "∃"),
        MathScienceSymbol("⇒", "Implies", "Logic", "Material implication", "P ⇒ Q", "⇒"),
        MathScienceSymbol("⇔", "If and Only If", "Logic", "Logical biconditional", "P ⇔ Q", "⇔"),
        MathScienceSymbol("∴", "Therefore", "Logic", "Conclusion marker", "∴ x = 5", "∴"),
        MathScienceSymbol("∵", "Because / Since", "Logic", "Premise marker", "∵ y > 0", "∵"),

        // Greek Alphabet
        MathScienceSymbol("α", "Alpha", "Greek Alphabet", "Angles, angular acceleration, thermal diffusivity", "α = dω/dt", "α"),
        MathScienceSymbol("β", "Beta", "Greek Alphabet", "Angles, beta particles, relativistic velocity ratio v/c", "β = v/c", "β"),
        MathScienceSymbol("γ", "Gamma", "Greek Alphabet", "Lorentz factor, heat capacity ratio, gamma rays", "γ = 1/√(1-v²/c²)", "γ"),
        MathScienceSymbol("δ", "Delta (lower)", "Greek Alphabet", "Dirac delta function, small infinitesimal change", "δ(x)", "δ"),
        MathScienceSymbol("ε", "Epsilon", "Greek Alphabet", "Permittivity, strain, error bound", "ε > 0", "ε"),
        MathScienceSymbol("θ", "Theta", "Greek Alphabet", "Angle in polar/spherical coordinates, temperature", "sin(θ)", "θ"),
        MathScienceSymbol("λ", "Lambda", "Greek Alphabet", "Wavelength, eigenvalue, decay constant", "λ = c / f", "λ"),
        MathScienceSymbol("μ", "Mu (Micro)", "Greek Alphabet", "Micro prefix (10⁻⁶), friction coefficient, permeability", "μ = 0.5", "μ"),
        MathScienceSymbol("ν", "Nu", "Greek Alphabet", "Frequency of electromagnetic radiation, kinematic viscosity", "E = hν", "ν"),
        MathScienceSymbol("π", "Pi", "Greek Alphabet", "Mathematical constant 3.14159...", "π", "pi"),
        MathScienceSymbol("ρ", "Rho", "Greek Alphabet", "Density, electrical resistivity", "ρ = m / V", "ρ"),
        MathScienceSymbol("σ", "Sigma (lower)", "Greek Alphabet", "Standard deviation, normal stress, electrical conductivity", "σ = √(Var)", "σ"),
        MathScienceSymbol("τ", "Tau", "Greek Alphabet", "Torque, time constant, shear stress", "τ = r × F", "τ"),
        MathScienceSymbol("φ", "Phi", "Greek Alphabet", "Magnetic flux, electric potential, golden ratio", "φ = B · A", "φ"),
        MathScienceSymbol("ψ", "Psi", "Greek Alphabet", "Quantum wave function", "Ψ(x,t)", "ψ"),
        MathScienceSymbol("ω", "Omega (lower)", "Greek Alphabet", "Angular velocity, angular frequency 2πf", "ω = 2πf", "ω"),
        MathScienceSymbol("Ω", "Omega (Capital)", "Greek Alphabet", "Electrical resistance (Ohms), solid angle", "R = 100 Ω", "Ω")
    )
}
