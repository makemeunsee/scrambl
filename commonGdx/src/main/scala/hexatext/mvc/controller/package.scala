package hexatext.mvc

import scala.collection.concurrent.TrieMap

package object controller {

  val defaultDelay = 50l // 50 ms

  // concurrent maps as updates by any number of other parties
  private val actionDelay = TrieMap[String, Long]().withDefaultValue(defaultDelay)
  private val lastAction = TrieMap[String, Long]().withDefaultValue(0l)

  def inputSpamProof[R](name: String = "")(f: => R): Option[R] = {
    val now = System.currentTimeMillis
    if (now > lastAction(name) + actionDelay(name)) {
      setLastActionOf(name, now)
      Some(f)
    }
    else None
  }

  def defineSpamDelayFor(name: String, delay: Long) = {
    actionDelay(name) = delay
  }

  def setLastActionOf(name: String, time: Long) {
    lastAction(name) = time
  }
}