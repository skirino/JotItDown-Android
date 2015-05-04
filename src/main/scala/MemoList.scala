package com.access_company.jotitdown

import collection.JavaConversions._
import java.util.ArrayList
import java.util.Collections
import android.database.sqlite._

object MemoList {
  var list                   = new ArrayList[Memo]
  var dbhelper: MemoDBHelper = null
  var adapter:  MemoAdapter  = null

  def add(m: Memo, doNotifyServer: Boolean = true, doUpdate: Boolean = true) {
    list.add(m)
    dbhelper.insertOne(m)
    if(doNotifyServer) SyncMemoTask.save(m)
    if(doUpdate) update()
  }

  def delete(m: Memo, doNotifyServer: Boolean = true, doUpdate: Boolean = true) {
    var i = list.indexOf(m)
    list.remove(i)
    dbhelper.deleteOne(m)
    if(doNotifyServer) SyncMemoTask.delete(m)
    if(doUpdate) update()
  }

  def replace(from: Memo, to: Memo) {
    delete(from, true, false)
    add(to)
  }

  def update() {
    Collections.sort(list)
    adapter.clear()
    for(i <- Range(0, list.size)) {
      adapter.add(list.get(i))
    }
    adapter.notifyDataSetChanged()
  }

  def sync(memos: Iterable[Memo]) {
    val tsLocal  = list .map { _.createdAt }.toSet
    val tsRemote = memos.map { _.createdAt }.toSet
    val tsLocalOnly  = tsLocal  -- tsRemote
    val tsRemoteOnly = tsRemote -- tsLocal
    val tsBoth       = tsLocal  &  tsRemote

    val memosLocalOnly  = list .filter { m => tsLocalOnly (m.createdAt) }
    val memosRemoteOnly = memos.filter { m => tsRemoteOnly(m.createdAt) }

    val pairs = tsBoth.map { t =>
      ( list. find { m => m.createdAt == t }.get,
        memos.find { m => m.createdAt == t }.get )
    }
    val memosLocallyUpdated  = pairs.filter { case(ml, mr) => ml.updatedAt > mr.updatedAt }.map { _._1 }
    val memosRemotelyUpdated = pairs.filter { case(ml, mr) => ml.updatedAt < mr.updatedAt }.map { _._2 }

    SyncMemoTask.save(memosLocalOnly ++ memosLocallyUpdated)
    for(m <- memosRemoteOnly ++ memosRemotelyUpdated) { add(m, false, false) }

    update()
  }

  def getAdapter(context: MainActivity) = {
    if(adapter == null){
      val adapterBuffer = new ArrayList[Memo] // adapter's buffer is different from "list" field
      dbhelper = new MemoDBHelper(context)
      val memos = dbhelper.loadAll()
      for(m <- memos) {
        list.add(m)
        adapterBuffer.add(m)
      }
      adapter = new MemoAdapter(context, adapterBuffer)
    }
    adapter
  }
}
