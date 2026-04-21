package pol.rubiano.docdimtool.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pol.rubiano.docdimtool.app.di.appModule

class DocDimToolApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DocDimToolApp)
            modules(appModule)
        }
    }
}