package com.access_company.jotitdown

import android.os.Bundle
import android.content.Intent
import org.scaloid.common._

trait JIDBaseActivity extends TypedActivity with SActivity with JIDUtil {
  override def onCreate(bundle: Bundle) {
    setTheme(R.style.Theme_Sherlock)
    super.onCreate(bundle)
    setLayoutOnCreate()
  }

  def setLayoutOnCreate()

  def toastShort(s: String) {
    toastShort(this, s)
  }

  def startActivityWithEmptyStack(intent: Intent) {
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
  }
}
