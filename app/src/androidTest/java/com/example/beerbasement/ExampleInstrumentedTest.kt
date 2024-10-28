package com.example.beerbasement

import com.example.beerbasement.model.Beer
import com.example.beerbasement.model.BeersViewModelState
import com.example.beerbasement.repository.BeersRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class BeerListTest {
    private lateinit var beersRepository: BeersRepository
    private lateinit var beerListViewModel: BeersViewModelState

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        beersRepository = mock(BeersRepository::class.java)
        beerListViewModel = BeersViewModelState()
    }

    @Test
    fun testFilterByTitleWithEmptyTermRetrievesAllBeers() {
        // Arrange
        val userEmail = "123@123.dk"
        val beerList = listOf(
            Beer(1, "123@123.dk", "Tuborg", "Guld", "strong beer",  5.0f, 33.0f, "url", 1),
        )
        `when`(beersRepository.getBeersByUsername(userEmail)).thenReturn(beerList)

        // Act
        val result = beerListViewModel.filterByTitle("")

        // Assert
        assertEquals(beerList, result)
    }

    @Test
    fun testFilterByTitleWIthMatch() {
        // Arrange
        val userEmail = "user@example.com"
        val beerList = listOf(
            Beer(2, "user@example.com", "Carlsberg", "Classic", "strong beer",  4.0f, 33.0f, "url", 1),
        )
        `when`(beersRepository.getBeersByUsername(userEmail)).thenReturn(beerList)

        // Act
        val result = beerListViewModel.filterByTitle("Classic")

        // Assert
        assertEquals(listOf(Beer(2, "user@example.com", "Carlsberg", "Classic", "Ale", 5.0f, 33.0f, "url", 1)), result)
    }

    @Test
    fun TestFilterByTitleWithNoMatch() {
        // Arrange
        val userEmail = "user@example.com"
        val beerList = listOf(
            Beer(3, "123@123.dk", "Royal", "", "Nordic",  5.0f, 33.0f, "url", 1),
        )
        `when`(beersRepository.getBeersByUser(userEmail)).thenReturn(beerList)

        // Act
        val result = beerListViewModel.filterByTitle("Lager")

        // Assert
        assertTrue(result.isEmpty())
    }
}