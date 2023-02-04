package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
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
        val request = BookRequest("긴긴밤")

        //when
        bookService.saveBook(request)

        //then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo("긴긴밤")
    }

    @Test
    @DisplayName("책 대출 성공")
    fun loanBookTest() {
        //given
        bookRepository.save(Book("긴긴밤"))
        val savedUser = userRepository.save(User("daeun", null))
        val request = BookLoanRequest("daeun", "긴긴밤")

        //when
        bookService.loanBook(request)

        //then
        val result = userLoanHistoryRepository.findAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].bookName).isEqualTo("긴긴밤")
        assertThat(result[0].user.id).isEqualTo(savedUser.id)
        assertThat(result[0].isReturn).isFalse
    }

    @Test
    @DisplayName("책이 이미 대출되어 있다면, 신규 대출 실패")
    fun loanBookFailTest() {
        //given
        bookRepository.save(Book("긴긴밤"))
        val savedUser = userRepository.save(User("daeun", null))
        userLoanHistoryRepository.save(UserLoanHistory(savedUser, "긴긴밤", false))
        val request = BookLoanRequest("daeun", "긴긴밤")

        //when & then
        val message = assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.message
        assertThat(message).isEqualTo("이미 대출된 책입니다.")
    }
    
    @Test
    @DisplayName("책 반납 성공")
    fun returnBookTest() {
        //given
        val savedUser = userRepository.save(User("daeun", null))
        userLoanHistoryRepository.save(UserLoanHistory(savedUser, "긴긴밤", false))
        val request = BookReturnRequest("daeun", "긴긴밤")

        //when
        bookService.returnBook(request)
        
        //then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].isReturn).isTrue
    }
}