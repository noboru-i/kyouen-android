package hm.orz.chaos114.android.tumekyouen.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tume_kyouen",
        indices = arrayOf(
        Index(value = ["stage_no"], unique = true)
))
data class TumeKyouen(
        @PrimaryKey(autoGenerate = true) var uid: Int,
        @ColumnInfo(name = "stage_no") var stageNo: Int,
        @ColumnInfo(name = "size") var size: Int,
        @ColumnInfo(name = "stage") var stage: String,
        @ColumnInfo(name = "creator") var creator: String,
        @ColumnInfo(name = "clear_flag") var clearFlag: Int,
        @ColumnInfo(name = "clear_date") var clearDate: Long
)