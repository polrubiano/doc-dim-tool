package pol.rubiano.docdimtool.app.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pol.rubiano.docdimtool.app.domain.usecases.CalculateDocumentUseCase
import pol.rubiano.docdimtool.app.domain.usecases.ReverseCalculateDocumentUseCase
import pol.rubiano.docdimtool.app.presentation.CalculatorViewModel
import pol.rubiano.docdimtool.app.presentation.ReverseCalculatorViewModel

val appModule = module {
    factory { CalculateDocumentUseCase() }
    factory { ReverseCalculateDocumentUseCase() }
    viewModel { CalculatorViewModel(get()) }
    viewModel { ReverseCalculatorViewModel(get()) }
}