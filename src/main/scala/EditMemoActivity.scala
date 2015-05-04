package com.access_company.jotitdown

import android.graphics.Color
import android.widget.EditText
import android.content.DialogInterface
import org.scaloid.common._
import com.actionbarsherlock.app._
import com.actionbarsherlock.view.{Menu, MenuItem}
import spray.json._

class EditMemoActivity extends SherlockActivity with JIDBaseActivity {
  import TypedResource._

  var targetMemo: Option[Memo] = None
  var titleEdit: EditText = null
  var textEdit:  EditText = null

  def setLayoutOnCreate() {
    val memoStr = getIntent().getStringExtra("com.access_company.jotitdown.MEMO")
    targetMemo = Option(memoStr).flatMap { Memo.fromJsonStr _ }
    val title     = targetMemo.map { _.title        }.getOrElse("")
    val text      = targetMemo.map { _.text         }.getOrElse("")
    val createdAt = targetMemo.map { _.createdAtStr }.getOrElse("")
    val updatedAt = targetMemo.map { _.updatedAtStr }.getOrElse("")
    val gray = Color.parseColor("#606060")

    setContentView(
      if(targetMemo.isDefined) {
        new SVerticalLayout {
          STextView(createdAt).textSize(10 sp).backgroundColor(gray).textColor(Color.BLACK)
          STextView(updatedAt).textSize(10 sp).backgroundColor(gray).textColor(Color.BLACK)
          textEdit = SEditText(text).textSize(10 sp)
        }
      } else {
        new SVerticalLayout {
          textEdit = SEditText(text).textSize(10 sp)
        }
      }
    )

    val actionBar = getSupportActionBar()
    actionBar.setCustomView(R.layout.edit_actionbar)
    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME)
    titleEdit = actionBar.getCustomView().findView(TR.edit_title)
    titleEdit.setText(title)

    titleEdit.setHint("Title")
    textEdit .setHint("Body")

    if(targetMemo.isDefined) {// to edit an existing memo (to create a new one, titleEdit gets focused by default)
      textEdit.requestFocus
    }
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getSupportMenuInflater().inflate(R.menu.edit_memo, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    item.getItemId match {
      case R.id.action_save   => saveCallback()
      case R.id.action_delete => deleteCallback()
    }
    true
  }

  def saveCallback() {
    val title = titleEdit.getText.toString
    val text  = textEdit .getText.toString
    targetMemo match {
      case Some(m) => MemoList.replace(m, Memo(m.createdAt, title, text))
      case _       => MemoList.add(Memo(title, text))
    }
    startActivityWithEmptyStack(SIntent[MainActivity])
  }

  def deleteCallback() {
    new AlertDialogBuilder("Delete", "Are you sure you want to delete this memo?") {
      positiveButton("OK", (d: DialogInterface, id: Int) => {
        targetMemo match {
          case Some(m) => MemoList.delete(m)
          case _       =>
        }
        startActivityWithEmptyStack(SIntent[MainActivity])
      })
      negativeButton("Cancel")
    }.show()
  }
}
