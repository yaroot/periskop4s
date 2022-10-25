package periskop4s

class ExceptionAggregateSuite extends munit.FunSuite {

  val e: Throwable = new RuntimeException("foo")
  val empty        = ExceptionAggregate()

  test("starts with empty exceptions and zero count") {
    assertEquals(empty.totalCount, 0L)
    assert(empty.latestExceptions.isEmpty)
  }

  test("adds new exceptions to the queue and count") {
    val a: ExceptionAggregate = empty
      .add(ExceptionWithContext(e, Severity.Error))
      .add(ExceptionWithContext(e, Severity.Error))
      .add(ExceptionWithContext(e, Severity.Error))

    assertEquals(a.totalCount, 3L)
    assertEquals(a.latestExceptions.length, 3)
    assertEquals(a.latestExceptions.head.asInstanceOf[ExceptionWithContext].throwable, e)
    assertEquals(a.latestExceptions.head.severity, Severity.Error)
    assertEquals(
      a.latestExceptions.last.asInstanceOf[ExceptionWithContext].throwable,
      a.latestExceptions.head.asInstanceOf[ExceptionWithContext].throwable
    )
    assertNotEquals(a.latestExceptions.last, a.latestExceptions.head)
  }

  test("returns the aggregation key") {
    val a: ExceptionAggregate = empty
      .add(ExceptionWithContext(e, Severity.Error))

    assertEquals(a.aggregationKey, ExceptionWithContext(e, Severity.Error).aggregationKey)
  }

  test("keeps only N exceptions in queue") {
    val a: ExceptionAggregate = (1 to 15).foldLeft(empty) { case (agg, i) =>
      agg.add(ExceptionWithContext(new RuntimeException(s"foo $i"), Severity.Error))
    }

    assertEquals(a.totalCount, 15L)
    assertEquals(a.latestExceptions.length, 10)
    assertEquals(a.latestExceptions.head.asInstanceOf[ExceptionWithContext].throwable.getMessage, "foo 6")
    assertEquals(a.latestExceptions.last.asInstanceOf[ExceptionWithContext].throwable.getMessage, "foo 15")
  }

  test("throws if the aggregation key does not match") {
    val a: ExceptionAggregate = empty
      .add(ExceptionWithContext(new RuntimeException("foo"), Severity.Error))

    intercept[IllegalArgumentException](
      a.add(ExceptionWithContext(new RuntimeException("foo"), Severity.Error))
    )

  }
}
