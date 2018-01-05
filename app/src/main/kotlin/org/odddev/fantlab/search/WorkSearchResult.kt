package org.odddev.fantlab.search

data class WorkSearchResult(
		val allAuthorName: String,
		val allAuthorRusName: String,
		val altName: String,
		val author1Id: Int,
		val author1IsOpened: Boolean,
		val author1RusName: String,
		val author2Id: Int,
		val author2IsOpened: Boolean,
		val author2RusName: String,
		val author3Id: Int,
		val author3IsOpened: Boolean,
		val author3RusName: String,
		val author4Id: Int,
		val author4IsOpened: Boolean,
		val author4RusName: String,
		val author5Id: Int,
		val author5IsOpened: Boolean,
		val author5RusName: String,
		val markCount: Int,
		val midMark: Float,
		val name: String,
		val workType: String,
		val rusName: String,
		val workId: Int,
		val year: Int,
		val coverEditionId: Int
)