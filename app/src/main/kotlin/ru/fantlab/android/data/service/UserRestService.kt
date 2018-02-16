package ru.fantlab.android.data.service

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import ru.fantlab.android.data.dao.model.Login
import ru.fantlab.android.data.dao.model.User

interface UserRestService {

	@GET("user/{id}")
	fun getUser(@Path(value = "id") id: Int) : Observable<User?>

	// todo удалить после появления в API метода получения инфы о юзере по логину
	@GET("user/{id}")
	fun getLoggedUser(@Path(value = "id") id: Int) : Observable<Login>

	@GET("user/{login}")
	fun getLoggedUser(@Path(value = "login") login: String) : Observable<Login>
}