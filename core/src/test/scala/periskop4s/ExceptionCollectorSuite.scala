package periskop4s

class ExceptionCollectorSuite extends munit.FunSuite {

  class FooException(msg: String) extends RuntimeException(msg)

  test("saves exceptions internally and exposes them") {
    val collector    = new ExceptionCollector
    val e: Throwable = new FooException("bar")

    collector.add(e)
    collector.add(e)
    collector.add(new RuntimeException("bar"))

    val a: Seq[ExceptionAggregate] = collector.getExceptionAggregates.sortBy(_.totalCount).reverse
    assertEquals(a.length, 2)
    assertEquals(a.head.totalCount, 2L)
    assertEquals(a.head.latestExceptions.head.asInstanceOf[ExceptionWithContext].throwable, e)
    assertEquals(a.last.totalCount, 1L)
  }

  test("accepts raw exceptions") {
    val collector    = new ExceptionCollector
    val e: Throwable = new FooException("bar")
    collector.add(e)

    assertEquals(
      collector.getExceptionAggregates.head.latestExceptions.head.asInstanceOf[ExceptionWithContext].throwable,
      e
    )
  }

  test("saves messages internally and exposes them") {
    val collector = new ExceptionCollector
    collector.addMessage("key1", "message1", Severity.Info)
    collector.addMessage("key1", "message2", Severity.Info)
    collector.addMessage("key2", "message3", Severity.Info)

    val a: Seq[ExceptionAggregate] = collector.getExceptionAggregates.sortBy(_.aggregationKey)
    assertEquals(a.length, 2)
    assertEquals(a.head.totalCount, 2L)
    assertEquals(a.head.latestExceptions.head.asInstanceOf[ExceptionMessage].message, "message1")
    assertEquals(a.last.totalCount, 1L)
  }
}
