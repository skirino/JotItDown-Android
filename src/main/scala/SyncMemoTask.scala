package com.access_company.jotitdown

import collection.JavaConversions._
import com.loopj.android.http._
import org.apache.http.entity.StringEntity
import spray.json._, DefaultJsonProtocol._
import android.content._

object SyncMemoTask extends JIDUtil {
  val PREF_CRED_KEY = "jotitdown-credential"
  val MAX_RETRIES   = 3
  val API_BASE_URL  = "https://skirino-jotitdown-backend.herokuapp.com/"

  val client = new AsyncHttpClient()
  var context   : Context           = null
  var credential: String            = null
  var prefs     : SharedPreferences = null

  def init(implicit c: Context) {
    if(prefs == null) {
      context = c
      prefs = context.getSharedPreferences("jotitdown-preferences", Context.MODE_PRIVATE)
      credential = prefs.getString(PREF_CRED_KEY, null)
    }
  }

  def register(u: String, p: String) {
    sendRequest('post, "register", Map("username" -> u, "password" -> p)) { body =>
      credential = body
      prefs.edit().putString(PREF_CRED_KEY, body).commit()
    }
  }

  def save(m: Memo) {
    save(List(m))
  }

  def save(ms: Iterable[Memo]) {
    if(credential == null) return
    val memosJsonStr = ms.map { m => (m.key -> m.toEncryptedJsonStr(credential)) }.toMap.toJson.compactPrint
    sendRequest('post, "values", null, memosJsonStr) { _ => }
  }

  def delete(m: Memo) {
    delete(List(m))
  }

  def delete(ms: List[Memo]) {
    if(credential == null) return
    sendRequest('delete, "keys?keys=" + ms.map{_.key}.mkString(",")) { _ => }
  }

  def fetchAll() {
    if(credential == null) return
    sendRequest('get, "keys") { body1 =>
      val keys = body1.asJson.convertTo[List[String]]
      if(keys.isEmpty) {
        MemoList.sync(List())
      } else {
        sendRequest('get, "values", Map("keys" -> keys.mkString(","))) { body2 =>
          val memos = body2.asJson.convertTo[Map[String, String]].flatMap { case(k, v) =>
            if(k.startsWith(Memo.KEY_PREFIX)) Memo.fromEncryptedJsonStr(credential, v) else None
          }
          MemoList.sync(memos)
        }
      }
    }
  }

  private def sendRequest(method: Symbol, path: String, paramMap: java.util.Map[String, String] = null, body: String = null, numRetry: Int = 0)(onSuccessFunc: String => Unit) {
    if(numRetry == MAX_RETRIES) {
      toastShort(context, "Sync memos: Failed!")
      return
    }

    client.addHeader("Authorization", credential)
    val url = API_BASE_URL + path
    val handler = new AsyncHttpResponseHandler() {
      override def onSuccess(body: String) {
        toastShort(context, "Sync memos: Finished.")
        onSuccessFunc(body)
      }
      override def onFailure(err: Throwable, body: String) {
        println("!!! " + method + " request to " + path + " failed " + numRetry + " time(s)")
        sendRequest(method, path, paramMap, body, numRetry + 1)(onSuccessFunc)
      }
    }

    method match {
      case 'get    =>
        val params = if(paramMap == null) null else new RequestParams(paramMap)
        client.get(url, params, handler)
      case 'post   =>
        if(paramMap == null) {
          val bodyEntity = if(body == null) null else new StringEntity(body)
          client.post(null, url, bodyEntity, "application/json", handler)
        } else {
          client.post(url, new RequestParams(paramMap), handler)
        }
      case 'delete => client.delete(url, handler)
    }
  }
}
