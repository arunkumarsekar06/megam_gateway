/* 
** Copyright [2012-2013] [Megam Systems]

**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
/**
 * @author rajthilak
 *
 */
package test

import com.stackmob.newman._
import com.stackmob.newman.response._
import org.specs2.matcher.{ MatchResult, Expectable, Matcher }
import org.specs2.execute.{ Failure => SpecsFailure, Result => SpecsResult }
import scalaz._
import Scalaz._
import net.liftweb.json.scalaz.JsonScalaz._
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.codec.binary.Base64
import com.stackmob.newman._
import com.stackmob.newman.response.{ HttpResponse, HttpResponseCode }
import com.stackmob.newman.dsl._

import java.net.URL
import java.util.Calendar
import java.text.SimpleDateFormat
import controllers.stack.SecurityActions._

trait BaseContext {

  val currentDate = new SimpleDateFormat("yyy-MM-dd HH:mm") format Calendar.getInstance.getTime
  val defaultHeaderOpt = Map(Content_Type -> application_json,
    X_Megam_EMAIL -> "sandy@megamsandbox.com", X_Megam_APIKEY -> "IamAtlas{74}NobodyCanSeeME#07",
    X_Megam_DATE -> currentDate, Accept -> application_vnd_megam_json)

  protected class HeadersAreEqualMatcher(expected: Headers) extends Matcher[Headers] {
    override def apply[S <: Headers](r: Expectable[S]): MatchResult[S] = {
      val other: Headers = r.value
      val res = expected === other
      result(res, "Headers are equal", expected + " does not equal " + other, r)
    }
  }

  protected class HttpResponseCodeAreEqualMatcher(expected: HttpResponseCode = HttpResponseCode.Ok) extends Matcher[HttpResponseCode] {
    override def apply[S <: HttpResponseCode](r: Expectable[S]): MatchResult[S] = {
      val other: HttpResponseCode = r.value
      val res = (expected === other)
      result(res, "HttpResponse core are equal", expected + " does not equal " + other, r)
    }
  }

  protected class HttpResponsesAreEqualMatcher(expected: HttpResponse) extends Matcher[HttpResponse] {
    override def apply[S <: HttpResponse](r: Expectable[S]): MatchResult[S] = {
      val other: HttpResponse = r.value
      val res = (expected.code === other.code) && (expected.headers === other.headers) && (expected.bodyString === other.bodyString)
      result(res, "HttpResponses are equal", expected + " does not equal " + other, r)
    }
  }

  protected def haveTheSameHeadersAs(h: Headers) = new HeadersAreEqualMatcher(h)

  protected def beTheSameResponseAs(h: HttpResponse) = new HttpResponsesAreEqualMatcher(h)

  protected def beTheSameResponseCodeAs(h: HttpResponseCode) = new HttpResponseCodeAreEqualMatcher(h)

  protected def logAndFail(t: Throwable): SpecsResult = {
    SpecsFailure("failed with exception %s".format(t.getMessage))
  }

  private def errorString(err: Error) = err match {
    case UnexpectedJSONError(was, expected)  => "unexpected JSON. was %s, expected %s".format(was.toString, expected.toString)
    case NoSuchFieldError(name, json)        => "no such field %s in json %s".format(name, json.toString)
    case UncategorizedError(key, desc, args) => "uncategorized JSON error for key %s: %s (args %s)".format(key, desc, args.mkString("&"))
  }

  protected def logAndFail(errs: NonEmptyList[Error]): SpecsResult = {
    val totalErrString = errs.map(errorString(_)).list.mkString("\n")
    SpecsFailure("JSON errors occurred: %s".format(totalErrString))
  }

  protected def sandboxHeaderAndBody(contentToEncodeOpt: Option[String],
    headerOpt: Option[Map[String, String]], path: String): (Headers, RawBody) = {
    val headerMap: Map[String, String] = headerOpt.getOrElse(defaultHeaderOpt)
    play.api.Logger.debug("--------hmap  ======>\n" + headerMap)
    play.api.Logger.debug("--------" + X_Megam_APIKEY + "  ======>\n" + headerMap.getOrElse(X_Megam_APIKEY, "blank_key"))
    play.api.Logger.debug("--------" + X_Megam_DATE + "  ======>\n" + headerMap.getOrElse(X_Megam_DATE, currentDate))

    val signWithHMAC = headerMap.getOrElse(X_Megam_DATE, currentDate) + "\n" + path + "\n" + calculateMD5(contentToEncodeOpt)
    val signedWithHMAC = calculateHMAC((headerMap.getOrElse(X_Megam_APIKEY, "blank_key")), signWithHMAC)
    val finalHMAC = headerMap.getOrElse(X_Megam_EMAIL, "blank_email") + ":" + signedWithHMAC

    (Headers((headerOpt.getOrElse(Map()) + (X_Megam_HMAC -> finalHMAC)).toList),
      RawBody(contentToEncodeOpt.getOrElse(new String())))
  }
}

trait Context extends BaseContext {

  val httpClient = new ApacheHttpClient

  protected def urlSuffix: String
  protected def bodyToStick: Option[String] = Some(new String())
  protected def headersOpt: Option[Map[String, String]]

  lazy val url = new URL("http://localhost:9000/v1/" + urlSuffix)
  play.api.Logger.debug("--------url  ======>\n" + url)
  play.api.Logger.debug("--------body ======>\n" + bodyToStick)

  val headerAndBody = sandboxHeaderAndBody(this.bodyToStick, headersOpt, url.getPath)
  play.api.Logger.debug("body post =>" + headerAndBody)

  protected val headers = headerAndBody._1
  play.api.Logger.debug("--------head for HTTP REQ =>\n" + headers)

  protected val body = headerAndBody._2
  play.api.Logger.debug("--------body for HTTP REQ =>\n" + body)

  protected def execute[T](t: Builder) = {
    t.executeUnsafe
  }
}