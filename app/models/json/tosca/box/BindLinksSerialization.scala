/*
** Copyright [2013-2015] [Megam Systems]
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
package models.json.tosca.box

import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import scala.collection.mutable.ListBuffer
import models.tosca._
import models.json.tosca._
import java.nio.charset.Charset
/**
 * @author rajthilak
 *
 */
object BindLinksSerialization extends models.json.SerializationBase[BindLinks] {
  implicit val formats = DefaultFormats
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val ResultsKey = "components"

  implicit override val writer = new JSONW[BindLinks] {
    override def write(h: BindLinks): JValue = {
      val nrsList: Option[List[JValue]] = h.map {
        nrOpt: String => toJSON(nrOpt)
      }.some

      JArray(nrsList.getOrElse(List.empty[JValue]))
    }
  }

  implicit override val reader = new JSONR[BindLinks] {
    override def read(json: JValue): Result[BindLinks] = {
      var list = new ListBuffer[String]()
      json match {
        case JArray(jObjectList) => {
         jObjectList.foreach { jValue: JValue =>
            list += jValue.extract[String]
          }.some

          val nrs: BindLinks = list.toList
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[BindLinks]
      }
    }
  }
}
