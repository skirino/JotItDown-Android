package com.access_company.jotitdown

import android.widget.ListView
import android.view.View
import android.content.DialogInterface
import org.scaloid.common._
import com.actionbarsherlock.app._
import com.actionbarsherlock.view.{Menu, MenuItem}
import spray.json._

class MainActivity extends SherlockListActivity with JIDBaseActivity {
  def setLayoutOnCreate() {
    setListAdapter(MemoList.getAdapter(this))
    SyncMemoTask.init(this)
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getSupportMenuInflater().inflate(R.menu.memo_list, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    item.getItemId match {
      case R.id.action_add        => addCallback
      case R.id.action_reload     => reloadCallback
      case R.id.action_preference => preferenceCallback
    }
    true
  }

  def addCallback() {
    startActivity(SIntent[EditMemoActivity])
  }

  def reloadCallback() {
    SyncMemoTask.fetchAll
  }

  def preferenceCallback() {
    var usernameEdit: SEditText = null
    var passwordEdit: SEditText = null

    new AlertDialogBuilder("Registration", "Input your user name and password") {
      setView(new SVerticalLayout {
        usernameEdit = SEditText("").hint("user name")
        passwordEdit = SEditText("").hint("password")
      })
      positiveButton("OK", (d: DialogInterface, id: Int) => {
        SyncMemoTask.register(usernameEdit.getText.toString, passwordEdit.getText.toString)
      })
    }.show
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val memo = l.getItemAtPosition(position).asInstanceOf[Memo]
    val intent = SIntent[EditMemoActivity]
    intent.putExtra("com.access_company.jotitdown.MEMO", memo.toJsonStr)
    startActivity(intent)
  }
}
