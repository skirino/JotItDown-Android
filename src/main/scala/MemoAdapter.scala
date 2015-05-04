package com.access_company.jotitdown

import java.util.ArrayList
import android.view._
import android.widget._

class MemoAdapter(context: MainActivity, var list: ArrayList[Memo])
    extends ArrayAdapter[Memo](context, R.layout.memo_row, list) {
  import TypedResource._

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val view = if(convertView == null) context.getLayoutInflater().inflate(R.layout.memo_row, null) else convertView
    val memo = getItem(position).asInstanceOf[Memo]

    view.findView(TR.memo_title).setText(memo.title)
    view.findView(TR.memo_time) .setText(memo.updatedAtStr)
    view.findView(TR.memo_text) .setText(memo.text)
    view
  }
}
