package ru.fantlab.android.data.dao.newmodel

import com.google.gson.annotations.SerializedName

data class WorkRootSaga(
		val prefix: String?,
		@SerializedName("work_id") val id: Int,
		@SerializedName("work_name") val name: String,
		@SerializedName("work_type") val type: String,
		@SerializedName("work_type_id") val typeId: Int,
		@SerializedName("work_type_in") val typeIn: String,
		@SerializedName("work_year") val year: Int?
)
