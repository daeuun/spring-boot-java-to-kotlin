package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {

    @AfterEach
    fun clean() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
        // deleteAll() 자식 테이블도 찾아서 delete
        // OneToMany 구조라서 UserLoanHistoryRepository 추가 안해도 됨
    }

    @Test
    @DisplayName("책 등록 성공")
    fun saveBookTest() {
        //given
        val request = BookRequest("긴긴밤", BookType.COMPUTER)

        //when
        bookService.saveBook(request)

        //then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo("긴긴밤")
        assertThat(books[0].type).isEqualTo(BookType.COMPUTER)
    }

    @Test
    @DisplayName("책 대출 성공")
    fun loanBookTest() {
        //given
        bookRepository.save(Book.fixture("긴긴밤"))
        val savedUser = userRepository.save(User("daeun", null))
        val request = BookLoanRequest("daeun", "긴긴밤")

        //when
        bookService.loanBook(request)

        //then
        val result = userLoanHistoryRepository.findAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].bookName).isEqualTo("긴긴밤")
        assertThat(result[0].user.id).isEqualTo(savedUser.id)
        assertThat(result[0].status).isEqualTo(UserLoanStatus.LOANED)
    }

    @Test
    @DisplayName("책이 이미 대출되어 있다면, 신규 대출 실패")
    fun loanBookFailTest() {
        //given
        bookRepository.save(Book.fixture("긴긴밤"))
        val savedUser = userRepository.save(User("daeun", null))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedUser, "긴긴밤"))
        val request = BookLoanRequest("daeun", "긴긴밤")

        //when & then
        val message = assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.message
        assertThat(message).isEqualTo("이미 대출되어 있는 책입니다.")
    }
    
    @Test
    @DisplayName("책 반납 성공")
    fun returnBookTest() {
        //given
        val savedUser = userRepository.save(User("daeun", null))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedUser, "긴긴밤"))
        val request = BookReturnRequest("daeun", "긴긴밤")

        //when
        bookService.returnBook(request)
        
        //then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].status).isEqualTo(UserLoanStatus.RETURNED)
    }
    
    @Test
    @DisplayName("책 대여 권수를 정상 확인한다")
    fun countLoanedBookTest() {
        //given
        val savedUser = userRepository.save(User("daeun", null))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser, "A"),
            UserLoanHistory.fixture(savedUser, "B", UserLoanStatus.RETURNED),
            UserLoanHistory.fixture(savedUser, "C", UserLoanStatus.RETURNED),
        ))
        
        //when
        val results = bookService.countLoanedBook()
        
        //then
        assertThat(results).isEqualTo(1)
    }
    
    @Test
    @DisplayName("분야별 책 권수를 정상 확인한다")
    fun getBookStatisticsTest() {
        //given
        bookRepository.saveAll(listOf(
            Book.fixture("A", BookType.COMPUTER),
            Book.fixture("B", BookType.COMPUTER),
            Book.fixture("C", BookType.ECONOMY),
        ))
        
        //when
        val results = bookService.getBookStatistics()
        
        //then
        assertThat(results).hasSize(2)
        assertCount(results, BookType.COMPUTER, 2L)
        assertCount(results, BookType.ECONOMY, 1L)

        /*  반복되는 구문을 하나의 함수 assertCount 로 만들어서 씀
        val computerDto = results.first { result -> result.type == BookType.COMPUTER }
        assertThat(computerDto.count).isEqualTo(2)
        val economyDto = results.first { result -> result.type == BookType.ECONOMY }
        assertThat(economyDto.count).isEqualTo(1)
        */
    }

    private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Long) {
        assertThat(results.first { result -> result.type == type }.count).isEqualTo(count)
    }
}