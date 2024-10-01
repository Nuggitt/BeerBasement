package com.example.beerbasement.model

data class Beer(
    val id: Int,
    val user: String,
    val brewery: String,
    val name: String,
    val style: String,
    val abv: Float, // Change from Int to Float to accept decimal values
    val volume: Float,
    val pictureUrl: String,
    val howMany: Int
) {
    constructor(
        user: String,
        brewery: String,
        name: String,
        style: String,
        abv: Float, // Change from Int to Float in the constructor as well
        volume: Float,
        pictureUrl: String,
        howMany: Int
    ) : this(0, user, brewery, name, style, abv, volume, pictureUrl, howMany)

    override fun toString(): String {
        return "Beer(id=$id, user='$user', brewery='$brewery', name='$name', style='$style', abv=$abv, volume=$volume, pictureUrl='$pictureUrl', howMany=$howMany)"
    }
}
