package com.access_company.jotitdown.tests

import com.access_company.jotitdown._
import junit.framework.Assert._
import _root_.android.test.AndroidTestCase
import _root_.android.test.ActivityInstrumentationTestCase2

class AndroidTests extends AndroidTestCase {
  def testPackageIsCorrect() {
    assertEquals("com.access_company.jotitdown", getContext.getPackageName)
  }
}

class ActivityTests extends ActivityInstrumentationTestCase2(classOf[MainActivity]) {
  def testHelloWorldIsShown() {
    val activity = getActivity
    val textview = activity.findView(TR.textview)
    assertEquals(textview.getText, "hello, world!")
  }
}
