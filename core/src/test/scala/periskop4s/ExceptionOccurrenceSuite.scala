package periskop4s

class ExceptionOccurrenceSuite extends munit.FunSuite {

  class TestExceptionWithStacktrace(stacktrace: Array[StackTraceElement]) extends RuntimeException {
    override def getStackTrace: Array[StackTraceElement] = stacktrace
  }

  test("className is the exception class name") {
    val e: Throwable = new RuntimeException("foo")
    assertEquals(ExceptionWithContext(e, Severity.Error).className, "java.lang.RuntimeException")
  }

  test("aggregationKey is based on the class name and stacktrace only") {
    val eArr          = Array(1, 2).map { i => new RuntimeException(s"foo $i") }
    val (e1, e2)      = (eArr(0), eArr(1))
    val e3: Throwable = new RuntimeException("foo 2")

    val key = ExceptionWithContext(e1, Severity.Error).aggregationKey
    assert("""\Ajava.lang.RuntimeException@[0-9a-f]{4,8}\z""".r.matches(key))

    assertEquals(
      ExceptionWithContext(e1, Severity.Error).aggregationKey,
      ExceptionWithContext(e2, Severity.Error).aggregationKey
    )

    assertNotEquals(
      ExceptionWithContext(e2, Severity.Error).aggregationKey,
      ExceptionWithContext(e3, Severity.Error).aggregationKey
    )
  }

  test("aggregationKey is using only first 5 lines of stacktrace") {
    val e1: Throwable = new TestExceptionWithStacktrace(
      Array(
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 1)
      )
    )

    val e2: Throwable = new TestExceptionWithStacktrace(
      Array(
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 99)
      )
    )

    val e3: Throwable = new TestExceptionWithStacktrace(
      Array(
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 1),
        new StackTraceElement("x", "x", "x", 99),
        new StackTraceElement("x", "x", "x", 1)
      )
    )

    assertEquals(
      ExceptionWithContext(e1, Severity.Error).aggregationKey,
      ExceptionWithContext(e2, Severity.Error).aggregationKey
    )

    assertNotEquals(
      ExceptionWithContext(e1, Severity.Error).aggregationKey,
      ExceptionWithContext(e3, Severity.Error).aggregationKey
    )
  }

  test("UUID values are different for each instance") {
    val e: Throwable = new RuntimeException("foo")
    assertNotEquals(ExceptionWithContext(e, Severity.Error).uuid, ExceptionWithContext(e, Severity.Error).uuid)
  }

  test("UUID values are different for each instance") {
    val a = ExceptionMessage("key", "message", Severity.Info)
    val b = ExceptionMessage("key2", "message2", Severity.Info)
    assertNotEquals(a.uuid, b.uuid)
  }
}
