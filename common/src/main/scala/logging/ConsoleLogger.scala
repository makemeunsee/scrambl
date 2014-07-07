package logging

object ConsoleLogger {
  
  val debug = false
  
  def logwrap[R](taskname: String)(f: => R) = {
    log(s"starting $taskname")
    val res = f
    log(s"done $taskname")
    res
  }
  
  def log(message: String) {
    if (debug) println(message)
  }
  
  def log(e: Exception) {
    if (debug) println(s"Exception: ${e.getMessage}")
  }
}