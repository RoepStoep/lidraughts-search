package lila.search

import scala.concurrent.Future

import com.sksamuel.elastic4s.ElasticDsl.{ RichFuture => _, _ }
import com.sksamuel.elastic4s.mappings.{ TypedFieldDefinition }
import com.sksamuel.elastic4s.{ ElasticDsl, ElasticClient }
import play.api.libs.json._

final class ESClient(client: ElasticClient) {

  def search(index: Index, query: JsObject, from: From, size: Size) = client execute {
    ElasticDsl.search in index.withType rawQuery Json.stringify(query) from from.value size size.value
  } map SearchResponse.apply

  def count(index: Index, query: JsObject) = client execute {
    ElasticDsl.count from index.withType rawQuery Json.stringify(query)
  } map CountResponse.apply

  def store(index: Index, id: Id, obj: JsObject) = client execute {
    ElasticDsl.index into index.withType source Json.stringify(obj) id id.value
  }

  def storeBulk(index: Index, objs: JsObject) = client execute {
    ElasticDsl.bulk {
      objs.fields.collect {
        case (id, obj: JsObject) =>
          ElasticDsl.index into index.withType source Json.stringify(obj) id id
      }
    }
  }

  def deleteById(index: Index, id: Id) = client execute {
    ElasticDsl.delete id id.value from index.withType
  }

  def deleteByQuery(index: Index, query: StringQuery) = client execute {
    ElasticDsl.delete from index.withType where query.value
  }

  def putMapping(index: Index, fields: Seq[TypedFieldDefinition]) =
    resetIndex(index) >>
      client.execute {
        ElasticDsl.put mapping index.indexType as fields
      }

  private def resetIndex(index: Index) =
    client.execute {
      ElasticDsl.delete index index.name
    } >>
      client.execute {
        ElasticDsl.create index index.name
      }
}
