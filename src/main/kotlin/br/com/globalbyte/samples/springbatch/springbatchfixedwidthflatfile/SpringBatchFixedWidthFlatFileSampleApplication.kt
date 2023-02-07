package br.com.globalbyte.samples.springbatch.springbatchfixedwidthflatfile

import br.com.globalbyte.samples.springbatch.springbatchfixedwidthflatfile.domain.*
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.batch.item.file.transform.FixedLengthTokenizer
import org.springframework.batch.item.file.transform.LineTokenizer
import org.springframework.batch.item.file.transform.Range
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.transaction.PlatformTransactionManager
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SpringBootApplication
class SpringBatchFixedWidthFlatFileSampleApplication

fun main(args: Array<String>) {
    runApplication<SpringBatchFixedWidthFlatFileSampleApplication>(*args)
}

@Configuration
class BatchConfiguration() {

    val fileDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    @Bean
    fun job(jobRepository: JobRepository, readFileStep: Step): Job {
        return JobBuilder("fixedWidthFlatFileJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(readFileStep)
            .build()
    }

    @Bean
    fun readFileStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        return StepBuilder("readFileStep", jobRepository)
            .chunk<CreditLine, CreditLine>(10, transactionManager)
            .reader(creditFileReader())
            .writer {chunk ->
                chunk.items.forEach { println("   ====>   $it") } // simply print the credit line
            }
            .build()
    }

    @Bean
    fun creditFileReader(): FlatFileItemReader<CreditLine> {
        return FlatFileItemReaderBuilder<CreditLine>()
            .name("creditFileReader")
            .resource(ClassPathResource("sample-file.cred"))
            .lineMapper(creditLineMapper())
            .build()
    }

    @Bean
    fun creditLineMapper(): PatternMatchingCompositeLineMapper<CreditLine> {
        val lineMapper = PatternMatchingCompositeLineMapper<CreditLine>()

        val tokenizers = mapOf<String, LineTokenizer>(
            "01*" to headerTokenizer(),
            "02*" to registrationTokenizer(),
            "03*" to creditTokenizer(),
            "04*" to trailerTokenizer()
        )
        lineMapper.setTokenizers(tokenizers)

        val fieldSetMappers = mapOf<String, FieldSetMapper<CreditLine>>(
            "01*" to headerFieldSetMapper(),
            "02*" to registrationFieldSetMapper(),
            "03*" to creditFieldSetMapper(),
            "04*" to trailerFieldSetMapper()
        )
        lineMapper.setFieldSetMappers(fieldSetMappers)

        return lineMapper;
    }

    @Bean
    fun headerTokenizer(): FixedLengthTokenizer {
        val tokenizer = FixedLengthTokenizer()

        tokenizer.setNames("LineType", "BatchNumber", "GenerationDate", "Filler")
        tokenizer.setColumns(
            Range(1, 2),
            Range(3, 5),
            Range(6, 13),
            Range(14, 81)
        )

        return tokenizer
    }

    @Bean
    fun registrationTokenizer(): FixedLengthTokenizer {
        val tokenizer = FixedLengthTokenizer()

        tokenizer.setNames("LineType", "ClientCode", "ClientName", "ClientType", "PersonType", "PaymentType", "Filler")
        tokenizer.setColumns(
            Range(1, 2),
            Range(3, 16),
            Range(17, 66),
            Range(67, 68),
            Range(69, 70),
            Range(71, 80),
            Range(81, 81)
        )

        return tokenizer
    }

    @Bean
    fun creditTokenizer(): FixedLengthTokenizer {
        val tokenizer = FixedLengthTokenizer()

        tokenizer.setNames("LineType", "ClientCode", "CreditDate", "Value", "Filler")
        tokenizer.setColumns(
            Range(1, 2),
            Range(3, 16),
            Range(17, 24),
            Range(25, 31),
            Range(32, 81)
        )

        return tokenizer
    }

    @Bean
    fun trailerTokenizer(): FixedLengthTokenizer {
        val tokenizer = FixedLengthTokenizer()

        tokenizer.setNames("LineType", "RegistrationCount", "CreditCount", "Filler")
        tokenizer.setColumns(
            Range(1, 2),
            Range(3, 5),
            Range(6, 8),
            Range(9,81)
        )

        return tokenizer
    }

    @Bean
    fun headerFieldSetMapper() = FieldSetMapper<CreditLine> { fieldSet ->
        BatchHeader(
            fieldSet.readInt("BatchNumber"),
            LocalDate.parse(fieldSet.readString("GenerationDate"), fileDateFormatter)
        )
    }

    @Bean
    fun registrationFieldSetMapper() = FieldSetMapper<CreditLine> { fieldSet ->
        Registration(
            fieldSet.readString("ClientCode"),
            fieldSet.readString("ClientName"),
            fieldSet.readInt("ClientType"),
            fieldSet.readString("PersonType"),
            fieldSet.readString("PaymentType")
        )
    }

    @Bean
    fun creditFieldSetMapper() = FieldSetMapper<CreditLine> { fieldSet ->
        Credit(
            fieldSet.readString("ClientCode"),
            LocalDate.parse(fieldSet.readString("CreditDate"), fileDateFormatter),
            fieldSet.readCreditValue("Value")
        )
    }

    @Bean
    fun trailerFieldSetMapper() = FieldSetMapper<CreditLine> { fieldSet ->
        BatchTrailer(fieldSet.readInt("RegistrationCount"), fieldSet.readInt("CreditCount"))
    }

}

fun FieldSet.readCreditValue(name: String): BigDecimal {
    val string = this.readString(name)
    val formattedString = "${string.substring(0, 5)}.${string.substring(5)}"
    return BigDecimal(formattedString)
}
