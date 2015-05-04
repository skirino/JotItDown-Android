package com.access_company.jotitdown

import java.util.Date
import java.text.SimpleDateFormat
import spray.json._
import DefaultJsonProtocol._

final case class Memo(createdAt: Long, updatedAt: Long, title: String, text: String) extends java.lang.Comparable[Memo] {
  def createdAtStr() = {
    "Created at: " + Memo.formatter.format(new Date(createdAt))
  }
  def updatedAtStr() = {
    "Last modified: " + Memo.formatter.format(new Date(updatedAt))
  }

  def compareTo(rhs: Memo): Int = {
    // updatedAt ASC
    (rhs.updatedAt - updatedAt).toInt
  }

  def key = Memo.KEY_PREFIX + createdAt

  def toJsonStr(): String = this.toJson(MemoJsonProtocol.memoFormat).compactPrint

  def toEncryptedJsonStr(cred: String): String = EncryptHelper.encrypt(cred, toJsonStr)
}

object Memo {
  val KEY_PREFIX = "jotitdown-memo-"

  def apply(title: String, text: String) = {
    val now = (new Date).getTime
    new Memo(now, now, title, text)
  }

  def apply(t: Long, title: String, text: String) = {
    val now = (new Date).getTime
    new Memo(t, now, title, text)
  }

  val formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

  import MemoJsonProtocol._

  def fromJsonStr(j: String): Option[Memo] = {
    j.asJson.convertTo[Option[Memo]]
  }

  def fromEncryptedJsonStr(cred: String, s: String): Option[Memo] = {
    fromJsonStr(EncryptHelper.decrypt(cred, s))
  }
}

object MemoJsonProtocol extends DefaultJsonProtocol {
  implicit val memoFormat = jsonFormat(Memo.apply, "createdAt", "updatedAt", "title", "text")
}

object EncryptHelper {
  val SALT = "jot it down!!!"
  val OFFSET_MAX = 100
  val ASCII_MIN  = 32
  val ASCII_MAX  = 126

  def encrypt(cred: String, text: String): String = {
    val (i1, i2, i3) = calcOffsets(cred)
    text.toCharArray.zipWithIndex.map { case(c, i) =>
      val offset = (math.pow(i, i1).asInstanceOf[Int] * i2 + i3) % OFFSET_MAX
      toVisibleCharCode(c + offset).toChar
    }.mkString
  }

  def decrypt(cred: String, data: String): String = {
    val (i1, i2, i3) = calcOffsets(cred)
    data.toCharArray.zipWithIndex.map { case(c, i) =>
      val offset = (math.pow(i, i1).asInstanceOf[Int] * i2 + i3) % OFFSET_MAX
      toVisibleCharCode(c - offset).toChar
    }.mkString
  }

  private def calcOffsets(cred: String): (Int, Int, Int) = {
    val i = (cred + SALT).hashCode.abs % 100000
    (i % 2 + 1, i % 100, i / 1000)
  }

  private def toVisibleCharCode(code: Int): Int = {
    if(code < ASCII_MIN)
      ASCII_MAX + 1 - (ASCII_MIN - code)
    else if(code <= ASCII_MAX)
      code
    else
      ASCII_MIN - 1 + (code - ASCII_MAX)
  }
}
