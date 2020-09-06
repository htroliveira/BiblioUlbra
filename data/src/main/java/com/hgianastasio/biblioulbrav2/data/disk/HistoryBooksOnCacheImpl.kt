package com.hgianastasio.biblioulbrav2.data.disk

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import com.google.gson.Gson
import com.hgianastasio.biblioulbrav2.data.base.historybooks.HistoryBooksOnCache
import com.hgianastasio.biblioulbrav2.data.disk.db.DBHelper
import com.hgianastasio.biblioulbrav2.data.disk.preferences.BasePreferences
import com.hgianastasio.biblioulbrav2.data.models.historybooks.HistoryBookEntity
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Created by heitorgianastasio on 4/29/17.
 */
class HistoryBooksOnCacheImpl @Inject constructor(private val dbHelper: DBHelper, private val preferences: HistoryBookPreferences) : HistoryBooksOnCache {
    @Throws(IOException::class)
    override fun get(): List<HistoryBookEntity> {
        val db = dbHelper.readableDatabase
        val columns = arrayOf(HISTORY_CACHE_IDFIELD, HISTORY_CACHE_JSONFIELD)
        val cursor = db.query(HISTORY_CACHE_TABLENAME, columns, null, null, null, null, null)
        val result: MutableList<HistoryBookEntity> = LinkedList()
        if (cursor.moveToFirst()) {
            do {
                result.add(fromCursor(cursor))
            } while (cursor.moveToNext())
        }
        return result
    }

    override fun save(historyBookEntities: List<HistoryBookEntity>): Boolean {
        clearCache()
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (entity in historyBookEntities) db.insertOrThrow(HISTORY_CACHE_TABLENAME, null, toValues(entity))
            preferences.setUpdateTime(System.currentTimeMillis())
            db.setTransactionSuccessful()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        db.endTransaction()
        return true
    }

    override fun clearCache(): Boolean {
        return try {
            dbHelper
                    .writableDatabase
                    .execSQL(String.format("DELETE FROM %s", HISTORY_CACHE_TABLENAME))
            preferences.clear()
        } catch (e: SQLException) {
            false
        }
    }

    //1 Hour
    override val isUpdated: Boolean
        get() {
            val updateTime = preferences.updateTime
            val difference = updateTime * 1000 - System.currentTimeMillis()
            return difference > -3600000 //1 Hour
        }

    @Throws(IOException::class)
    private fun fromCursor(cursor: Cursor): HistoryBookEntity {
        val idFieldIndex = cursor.getColumnIndex(HISTORY_CACHE_IDFIELD)
        val jsonFieldIndex = cursor.getColumnIndex(HISTORY_CACHE_JSONFIELD)
        val entity = Gson().fromJson(cursor.getString(jsonFieldIndex), HistoryBookEntity::class.java)
        entity.id = cursor.getLong(idFieldIndex)
        return entity
    }

    @Throws(IOException::class)
    private fun toValues(entity: HistoryBookEntity): ContentValues {
        val result = ContentValues()
        result.put(HISTORY_CACHE_JSONFIELD, Gson().toJson(entity))
        return result
    }

    class HistoryBookPreferences @Inject constructor(mContext: Context) : BasePreferences(mContext) {
        fun setUpdateTime(time: Long): Boolean {
            return preferences
                    .edit()
                    .putLong(HISTORY_BOOKS_UPDATE_TIME_KEY, time)
                    .commit()
        }

        val updateTime: Long
            get() = preferences
                    .getLong(HISTORY_BOOKS_UPDATE_TIME_KEY, -1)

        fun clear(): Boolean {
            return preferences.edit().remove(HISTORY_BOOKS_UPDATE_TIME_KEY).commit()
        }

        companion object {
            private const val HISTORY_BOOKS_UPDATE_TIME_KEY = "biblioulbra.pref.history_time"
        }
    }

    companion object {
        private const val HISTORY_CACHE_TABLENAME = "historybooks"
        private const val HISTORY_CACHE_IDFIELD = "_id"
        private const val HISTORY_CACHE_JSONFIELD = "json"
    }

}