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
package models.json.tosca


import scalaz._
import scalaz.NonEmptyList._
import scalaz.Validation
import scalaz.Validation._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.Date
import java.nio.charset.Charset
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import controllers.funnel.SerializationBase
import models.tosca.{ ContiniousIntegrationResult }
/**
 * @author morpheyesh
 *
 */
class ContiniousIntegrationResultSerialization(charset: Charset = UTF8Charset) extends SerializationBase[ContiniousIntegrationResult] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  
     protected val ScmKey = "scm"
     protected val EnableKey = "enable"
     protected val ComponentIDKey = "component_id"
     protected val AssemblyIDKey = "assembly_id"
     protected val IdKey = "id"
     protected val CreatedAtKey ="created_at"
 

  override implicit val writer = new JSONW[ContiniousIntegrationResult] {
   
    override def write(h: ContiniousIntegrationResult): JValue = {
      JObject(
       
          JField(EnableKey, toJSON(h.enable)) ::
          JField(ScmKey, toJSON(h.scm)) ::
          JField(ComponentIDKey, toJSON(h.component_id)) ::
          JField(AssemblyIDKey, toJSON(h.assembly_id)) ::
           JField(IdKey, toJSON(h.id)) ::
           JField(CreatedAtKey, toJSON(h.created_at))   ::          
          Nil)
    }
  }

  override implicit val reader = new JSONR[ContiniousIntegrationResult] {

    override def read(json: JValue): Result[ContiniousIntegrationResult] = {
      
       val idField = field[String](IdKey)(json)
       val enableField = field[String](EnableKey)(json)
      val scmField = field[String](ScmKey)(json)
      val componentIDField = field[String](ComponentIDKey)(json)
      val assemblyIDField = field[String](AssemblyIDKey)(json)
      val createdAtField = field[String](CreatedAtKey)(json)
      
      (idField |@| enableField |@| scmField |@| componentIDField |@| assemblyIDField |@| createdAtField) {
        (id: String, enable: String, scm: String, component_id: String, assembly_id: String, created_at: String) =>
          new ContiniousIntegrationResult(id, enable, scm, component_id, assembly_id, created_at)
      }
    }
  }
}