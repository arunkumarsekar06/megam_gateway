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
import scalaz.Validation
import scalaz.Validation._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.Date
import java.nio.charset.Charset
import controllers.funnel.FunnelErrors._
import controllers.Constants._
import models.json.tosca._
import models.tosca.{ ComponentResult, ComponentsList, Artifacts, KeyValueList, OperationList, BindLinks, Repo }

/**
 * @author rajthilak
 *
 */
class ComponentResultSerialization(charset: Charset = UTF8Charset) extends models.json.SerializationBase[ComponentResult] {

  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val IdKey = "id"
  protected val NameKey = "name"
  protected val ToscaTypeKey = "tosca_type"
  protected val InputsKey = "inputs"
  protected val OutputsKey = "outputs"
  protected val EnvsKey = "envs"
  protected val ArtifactsKey = "artifacts"
  protected val RelatedComponentsKey = "related_components"
  protected val OperationsKey = "operations"
  protected val StatusKey = "status"
  protected val RepoKey = "repo"
  protected val CreatedAtKey = "created_at"

  override implicit val writer = new JSONW[ComponentResult] {

    import models.json.tosca.box.ArtifactsSerialization.{ writer => ArtifactsWriter }
    import models.json.tosca.KeyValueListSerialization.{ writer => KeyValueListWriter }
    import models.json.tosca.OperationListSerialization.{ writer => OperationListWriter }
    import models.json.tosca.box.RepoSerialization.{ writer => RepoWriter }
    import models.json.tosca.box.BindLinksSerialization.{ writer => BindLinksWriter }

    override def write(h: ComponentResult): JValue = {
      JObject(
        JField(IdKey, toJSON(h.id)) ::
          JField(JSONClazKey, toJSON("Megam::Components")) ::
          JField(NameKey, toJSON(h.name)) ::
          JField(ToscaTypeKey, toJSON(h.tosca_type)) ::
          JField(InputsKey, toJSON(h.inputs)(KeyValueListWriter)) ::
          JField(OutputsKey, toJSON(h.outputs)(KeyValueListWriter)) ::
          JField(EnvsKey, toJSON(h.envs)(KeyValueListWriter)) ::
          JField(ArtifactsKey, toJSON(h.artifacts)(ArtifactsWriter)) ::
          JField(RelatedComponentsKey, toJSON(h.related_components)(BindLinksWriter)) ::
          JField(OperationsKey, toJSON(h.operations)(OperationListWriter)) ::
          JField(StatusKey, toJSON(h.status)) ::
          JField(RepoKey, toJSON(h.repo)(RepoWriter)) ::
          JField(CreatedAtKey, toJSON(h.created_at)) :: Nil)
    }
  }

  override implicit val reader = new JSONR[ComponentResult] {

    import models.json.tosca.box.ArtifactsSerialization.{ reader => ArtifactsReader }
    import models.json.tosca.KeyValueListSerialization.{ reader => KeyValueListReader }
    import models.json.tosca.OperationListSerialization.{ reader => OperationListReader }
    import models.json.tosca.box.RepoSerialization.{ reader => RepoReader }
    import models.json.tosca.box.BindLinksSerialization.{ reader => BindLinksReader }

    override def read(json: JValue): Result[ComponentResult] = {
      val idField = field[String](IdKey)(json)
      val nameField = field[String](NameKey)(json)
      val toscaTypeField = field[String](ToscaTypeKey)(json)
      val inputsField = field[KeyValueList](InputsKey)(json)(KeyValueListReader)
      val outputsField = field[KeyValueList](OutputsKey)(json)(KeyValueListReader)
      val envsField = field[KeyValueList](EnvsKey)(json)(KeyValueListReader)
      val artifactsField = field[Artifacts](ArtifactsKey)(json)(ArtifactsReader)
      val relatedComponentsField = field[BindLinks](RelatedComponentsKey)(json)(BindLinksReader)
      val operationsField = field[OperationList](OperationsKey)(json)(OperationListReader)
      val statusField = field[String](StatusKey)(json)
      val repoField = field[Repo](RepoKey)(json)(RepoReader)
      val createdAtField = field[String](CreatedAtKey)(json)

      (idField |@| nameField |@| toscaTypeField |@| inputsField |@| outputsField |@| envsField |@| artifactsField |@| relatedComponentsField |@| operationsField |@| statusField |@| repoField |@| createdAtField) {
        (id: String, name: String, tosca_type: String, inputs: KeyValueList, outputs: KeyValueList, envs: KeyValueList, artifacts: Artifacts, related_components: BindLinks, operations: OperationList, status: String, repo: Repo, createdAt: String) =>
          new ComponentResult(id, name, tosca_type, inputs, outputs, envs, artifacts, related_components, operations, status, repo, createdAt)
      }
    }
  }
}
