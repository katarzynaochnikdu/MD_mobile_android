package pl.medidesk.mobile.core.network

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import pl.medidesk.mobile.core.datastore.AuthDataStore
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authDataStore: AuthDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { authDataStore.tokenFlow.firstOrNull() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
