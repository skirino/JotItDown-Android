package com.access_company.jotitdown

import android.content._
import android.widget.Toast

trait JIDUtil {
  def toastShort(c: Context, s: String) {
    println("JotItDown debugging : " + s)
    Toast.makeText(c, s, Toast.LENGTH_SHORT).show()
  }
}
