package pol.rubiano.docdimtool.app.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pol.rubiano.docdimtool.app.domain.usecases.CalculateDocumentUseCase
import pol.rubiano.docdimtool.app.presentation.CalculatorViewModel

val appModule = module {
    factory { CalculateDocumentUseCase() }
    viewModel { CalculatorViewModel(get()) }
}