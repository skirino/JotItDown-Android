package com.access_company.jotitdown

import scala.collection.mutable.ListBuffer
import android.content._
import android.database.sqlite._

class MemoDBHelper(context: Context) extends SQLiteOpenHelper(context, MemoDB.DATABASE_NAME, null, 1) {
  override def onCreate(db: SQLiteDatabase) {
    db.execSQL(MemoDB.TABLE_SCHEMA)
  }

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
  }

  def loadAll(): List[Memo] = {
    val db = getReadableDatabase
    val l = new ListBuffer[Memo]
    val c = db.query(MemoDB.TABLE_NAME, null, null, null, null, null, null, null)
    c.moveToFirst()
    for(i <- Range(0, c.getCount)){
      l += Memo(c.getLong(0), c.getLong(1), c.getString(2), c.getString(3))
      c.moveToNext()
    }
    c.close()
    l.toList
  }

  def insertOne(memo: Memo) {
    val db = getWritableDatabase
    db.insert(MemoDB.TABLE_NAME, null, createContentValuesFor(memo))
  }

  def deleteOne(memo: Memo) {
    val db = getWritableDatabase
    db.delete(MemoDB.TABLE_NAME, "createdAt = ?", Array(memo.createdAt.toString))
  }

  def persistAll(memos: List[Memo]) {
    val db = getWritableDatabase
  }

  private def createContentValuesFor(m: Memo): ContentValues = {
    val cvs = new ContentValues
    cvs.put("createdAt", new java.lang.Long(m.createdAt))
    cvs.put("updatedAt", new java.lang.Long(m.updatedAt))
    cvs.put("title", m.title)
    cvs.put("text",  m.text)
    cvs
  }
}

object MemoDB {
  val DATABASE_NAME = "JotItDown"
  val TABLE_NAME    = "memos"
  val TABLE_SCHEMA =
    "CREATE TABLE memos ("              +
    "   createdAt INTEGER PRIMARY KEY," +
    "   updatedAt INTEGER,"             +
    "   title TEXT,"                    +
    "   text  TEXT)"
}
