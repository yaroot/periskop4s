package periskop4s

import org.typelevel.jawn.ast.*

import scala.collection.mutable
import java.time.format.DateTimeFormatter

class ExceptionExporter(exceptionCollector: ExceptionCollector) {

  val rfc3339TimeFormat: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

  def render: String = {
    val payload = jsonResult(
      exceptionAggregates = exceptionCollector.getExceptionAggregates
    )

    FastRenderer.render(payload)
  }

  def jString(x: String): JValue = {
    Option(x).fold(JNull: JValue)(JString(_))
  }

  private def jsonExceptionWithContext(t: Throwable): JValue = {
    JObject(
      mutable.Map(
        "class"      -> jString(t.getClass.getName),
        "message"    -> jString(t.getMessage),
        "stacktrace" -> JArray(t.getStackTrace.map(_.toString).map(jString).filter(_.nonNull)),
        "cause"      -> Option(t.getCause).fold(JNull: JValue)(jsonExceptionWithContext)
      )
    )
  }

  private def jsonExceptionMessage(m: String): JValue =
    JObject(mutable.Map("message" -> jString(m)))

  private def jsonHttpContext(httpContext: HttpContext): JValue =
    JObject(
      mutable.Map(
        "request_method"  -> jString(httpContext.requestMethod),
        "request_url"     -> jString(httpContext.requestUrl),
        "request_headers" -> JObject(mutable.Map.from(httpContext.requestHeaders.view.mapValues(jString))),
        "request_body"    -> httpContext.requestBody.fold(JNull: JValue)(jString)
      )
    )

  private def jsonErrorWithContext(e: ExceptionOccurrence): JValue = {
    val error = e match {
      case ExceptionWithContext(throwable, _, _, _, _) => jsonExceptionWithContext(throwable)
      case ExceptionMessage(_, message, _, _, _, _)    => jsonExceptionMessage(message)
      case _                                           => JNull
    }

    JObject(
      mutable.Map(
        "error"        -> error,
        "severity"     -> jString(Severity.toString(e.severity)),
        "uuid"         -> jString(e.uuid.toString),
        "timestamp"    -> jString(e.timestamp.format(rfc3339TimeFormat)),
        "http_context" -> e.httpContext.fold(JNull: JValue)(jsonHttpContext)
      )
    )
  }

  private def jsonAggregatedErrors(aggregate: ExceptionAggregate): JValue =
    JObject(
      mutable.Map(
        "aggregation_key" -> jString(aggregate.aggregationKey),
        "total_count"     -> JNum(aggregate.totalCount),
        "severity"        -> jString(Severity.toString(aggregate.severity)),
        "created_at"      -> jString(aggregate.createdAt.format(rfc3339TimeFormat)),
        "latest_errors"   -> JArray.fromSeq(aggregate.latestExceptions.map(jsonErrorWithContext))
      )
    )

  private def jsonResult(exceptionAggregates: Seq[ExceptionAggregate]) =
    JObject(
      mutable.Map(
        "aggregated_errors" -> JArray.fromSeq(exceptionAggregates.map(jsonAggregatedErrors)),
        "target_uuid"       -> jString(exceptionCollector.uuid.toString)
      )
    )

}
