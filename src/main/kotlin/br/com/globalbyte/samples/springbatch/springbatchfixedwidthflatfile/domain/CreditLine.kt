package br.com.globalbyte.samples.springbatch.springbatchfixedwidthflatfile.domain

import java.math.BigDecimal
import java.time.LocalDate

enum class CreditLineType {
    HEADER, REGISTRATION, CREDIT, TRAILER
}

abstract class CreditLine(code: String) {

    private val type: CreditLineType;

    init {
        type = typeFromCode(code)
    }

    private fun typeFromCode(code: String): CreditLineType {
        return when(code) {
            "01" -> CreditLineType.HEADER
            "02" -> CreditLineType.REGISTRATION
            "03" -> CreditLineType.CREDIT
            "04" -> CreditLineType.TRAILER
            else -> throw Exception("Unknown credit line type");
        }
    }

}

data class BatchHeader(val batchNumber: Int, val generationDate: LocalDate): CreditLine("01")

data class Registration(
    val clientCode: String,
    val clientName: String,
    val clientType: Int,
    val personType: String,
    val paymentType: String
): CreditLine("02")

data class Credit(
    val clientCode: String,
    val creditDate: LocalDate,
    val value: BigDecimal
): CreditLine("03")

data class BatchTrailer(val registrationCount: Int, val creditCount: Int): CreditLine("04")