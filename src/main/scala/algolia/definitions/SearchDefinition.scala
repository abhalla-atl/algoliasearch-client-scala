package algolia.definitions

import algolia._
import algolia.http.HttpPayload
import algolia.responses.Search
import org.json4s.native.Serialization.write

import scala.concurrent.Future

case class SearchDefinition(index: String,
                            query: Option[String] = None,
                            hitsPerPage: Option[Int] = None) extends Definition {

  def into(index: String): SearchDefinition = this

  def hitsPerPage(h: Int): SearchDefinition = copy(index, hitsPerPage = Some(h), query = query)

  def query(q: String): SearchDefinition = copy(index, query = Some(q), hitsPerPage = hitsPerPage)

  implicit val formats = org.json4s.DefaultFormats

  override private[algolia] def build(): HttpPayload = {
    val params = Seq() ++
      query.map(q => s"query=$q") ++
      hitsPerPage.map(h => s"hitsPerPage=$h")

    val body = Map("params" -> params.mkString("&"))

    HttpPayload(http.GET, Seq("1", "indexes", index, "query"), body = Some(write(body)))
  }
}


trait SearchDsl {

  implicit object SearchDefinitionExecutable extends Executable[SearchDefinition, Search] {
    override def apply(client: AlgoliaClient, query: SearchDefinition): Future[Search] = {
      client request[Search] query.build()
    }
  }

}