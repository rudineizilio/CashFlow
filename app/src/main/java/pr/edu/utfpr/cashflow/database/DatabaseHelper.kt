package pr.edu.utfpr.cashflow.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "appDatabase.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "cash_flow"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_DETAIL = "detail"
        private const val COLUMN_VALUE = "value"
        private const val COLUMN_DATE = "date"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_TYPE TEXT NOT NULL, " +
                    "$COLUMN_DETAIL TEXT NOT NULL, " +
                    "$COLUMN_VALUE REAL NOT NULL, " +
                    "$COLUMN_DATE TEXT NOT NULL) "

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun insert(type: String, detail: String, value: Double, date: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TYPE, type)
            put(COLUMN_DETAIL, detail)
            put(COLUMN_VALUE, value)
            put(COLUMN_DATE, date)
        }
        db.insert(TABLE_NAME, null, values)
    }
}