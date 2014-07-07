//import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._
import scala.collection.concurrent.TrieMap

package object perf {

  private val calls = new TrieMap[String, (Int, Duration)]
  
  def perfed[R](name: String = "")(f: => R) = {
    val t0 = System.nanoTime
    val res = f
    val lapse = System.nanoTime - t0
    updateMap(name, lapse.nanos)
    res
  }
  
  private def updateMap(name: String, value: Duration) = {
    if(name != "") {
      calls.get(name) match {
        case Some((c, l)) => calls += ((name, (c+1, l+value)))
        case _ => calls += ((name, (1, value)))
      }
    } else {
      println(s"$name: ${value.toMillis} ms")
    }
  }
  
  val storePerf = updateMap _
  
  def printResults() {
    calls.foreach { case (name, (count, timeSpent)) =>
      println(s"Called $name")
      println(s"\t$count times")
      val millis = timeSpent.toMillis
      println(s"\tspending $millis ms")
      println(s"\taverage: ${millis/count} ms/call")
    }
  }
}