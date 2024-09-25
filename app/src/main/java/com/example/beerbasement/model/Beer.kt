package com.example.beerbasement.model

class Beer(
    val id: Int,
    val user: String,
    val brewery: String,
    val name: String,
    val style: String,
    val abv: Double,
    val volume: Int,
    val pictureUrl: String,
    val howMany: Int
) {

    constructor(
        user: String,
        brewery: String,
        name: String,
        style: String,
        abv: Double,
        volume: Int,
        pictureUrl: String,
        howMany: Int
    ) : this(0, user, brewery, name, style, abv, volume, pictureUrl, howMany)
}