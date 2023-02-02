package com.group.libraryapp.calculator

import java.lang.Exception

fun main() {
    val calculatorTest = CalculatorTest()
    calculatorTest.addTest()
    calculatorTest.minusTest()
    calculatorTest.multiflyTest()
    calculatorTest.divideTest()
    calculatorTest.divideExceprionTest()
}

class CalculatorTest {

    fun addTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.add(3)

        //then
        if (calculator.number != 8) {
            throw IllegalArgumentException()
        }
    }

    fun minusTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.minus(3)

        //then
        if (calculator.number != 2) {
            throw IllegalArgumentException()
        }
    }

    fun multiflyTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.multifly(3)

        //then
        if (calculator.number != 15) {
            throw IllegalArgumentException()
        }
    }

    fun divideTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.divide(2)

        //then
        if (calculator.number != 2) {
            throw IllegalArgumentException()
        }
    }

    fun divideExceprionTest() {
        // given
        val calculator = Calculator(5)

        // when
        try {
            calculator.divide(0)
        } catch (e: IllegalArgumentException) {
            if (e.message != "0으로 나눌 수 없습니다.") {
                throw IllegalArgumentException("메세지가 다릅니다.")
            }
            // test success
            return
        } catch (e: Exception) {
            throw IllegalArgumentException()
        }
        throw java.lang.IllegalArgumentException("기대하는 예외가 발생하지 않았습니다.")
    }
}