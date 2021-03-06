package ru.fantlab.android.data.dao.response

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.JsonParser
import ru.fantlab.android.data.dao.model.WorkType
import ru.fantlab.android.provider.rest.DataManager
import java.util.*

data class WorkTypesResponse(
		val types: ArrayList<WorkType.WorkTypeItem>
) {
	class Deserializer : ResponseDeserializable<WorkTypesResponse> {

		override fun deserialize(content: String): WorkTypesResponse {
			val items: ArrayList<WorkType.WorkTypeItem> = arrayListOf()
			val json = JsonParser().parse(content)
			if (json != null) {
				val array = json.asJsonObject.getAsJsonArray("work_types")
				array.map {
					items.add(DataManager.gson.fromJson(it, WorkType.WorkTypeItem::class.java))
				}
			}
			return WorkTypesResponse(items)
		}
	}
}