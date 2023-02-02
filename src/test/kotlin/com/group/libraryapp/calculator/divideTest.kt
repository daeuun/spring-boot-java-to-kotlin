package com.group.libraryapp.calculator

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